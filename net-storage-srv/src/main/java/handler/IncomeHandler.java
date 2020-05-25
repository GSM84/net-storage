package handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import services.FileService;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.nio.file.Paths;

public class IncomeHandler extends ChannelInboundHandlerAdapter {
    private HandelerState        currentState       = HandelerState.IDLE;
    private int                  nextLength;
    private long                 fileLength;
    private long                 receivedFileLength = 0;
    private int                  userId;
    private BufferedOutputStream out;

    private String      stringName;
    private FileService fileService = new FileService();

    public IncomeHandler(int _userId){
        this.userId = _userId;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buf = (ByteBuf) msg;

        while (buf.readableBytes() > 0) {
            if (currentState == HandelerState.IDLE) {
                byte readed = buf.readByte();
                if (readed == Dictionary.RECIVE_FILE) {
                    currentState = HandelerState.GET_NAME_LENGTH;
                    receivedFileLength = Dictionary.ZERO_LENGTH;
                    System.out.println("STATE: Start file receiving");

                } else if (readed == Dictionary.FILE_LIST) {
                    fileService.getFileList(Paths.get(Dictionary.SERVER_BASE_FOLDER, String.valueOf(userId)), ctx);
                    currentState = HandelerState.IDLE;

                } else if (readed == Dictionary.SEND_FILE) {
                    if (buf.readableBytes() >= Dictionary.INT_LENGTH && fileService.getIncomeFileNameLength() == 0){
                        fileService.setIncomeFileNameLength(buf.readInt());
                    }

                    if (buf.readableBytes() >= fileService.getIncomeFileNameLength()){
                        byte[] fileName = new byte[fileService.getIncomeFileNameLength()];
                        buf.readBytes(fileName);
                        fileService.setIncomeFileName(new String(fileName, Dictionary.CHAR_SET));
                        fileService.getFile(userId, ctx);

                        currentState = HandelerState.IDLE;
                    }

                } else {
                    System.out.println("ERROR: Invalid first byte - " + readed);

                }
            } else if (currentState == HandelerState.GET_NAME_LENGTH) {
                if (buf.readableBytes() >= Dictionary.INT_LENGTH) {
                    System.out.println("STATE: Get filename length");
                    nextLength = buf.readInt();
                    currentState = HandelerState.GET_NAME;

                }
            } else if (currentState == HandelerState.GET_NAME) {
                if (buf.readableBytes() >= nextLength) {
                    byte[] fileName = new byte[nextLength];
                    buf.readBytes(fileName);
                    stringName = new String(fileName, Dictionary.CHAR_SET);
                    System.out.println("STATE: Filename received - " + stringName);
                    out = new BufferedOutputStream(new FileOutputStream(Paths.get(Dictionary.SERVER_BASE_FOLDER, String.valueOf(userId), stringName).toFile()));
                    currentState = HandelerState.GET_FILE_LENGTH;

                }
            } else if (currentState == HandelerState.GET_FILE_LENGTH) {
                if (buf.readableBytes() >= Dictionary.LONG_LENGTH) {
                    fileLength = buf.readLong();
                    System.out.println("STATE: File length received - " + fileLength);
                    currentState = HandelerState.GET_FILE;

                }
            } else if (currentState == HandelerState.GET_FILE) {
                while (buf.readableBytes() > 0) {
                    out.write(buf.readByte());
                    receivedFileLength++;
                    if (fileLength == receivedFileLength) {
                        currentState = HandelerState.IDLE;
                        System.out.println("File received return name");
                        out.close();

                        fileService.getFileList(Paths.get(Dictionary.SERVER_BASE_FOLDER, String.valueOf(userId)), ctx);
                        break;
                    }
                }
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
