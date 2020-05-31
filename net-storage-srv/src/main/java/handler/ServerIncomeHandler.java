package handler;

import dictionaryes.Dictionary;
import dictionaryes.HandelerState;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import services.ServerFileService;
import java.nio.file.Paths;

public class ServerIncomeHandler extends ChannelInboundHandlerAdapter {
    private HandelerState        currentState       = HandelerState.IDLE;
    private int                  userId;
    private ServerFileService    fileService;

    public ServerIncomeHandler(int _userId){
        this.userId      = _userId;
        this.fileService = new ServerFileService(_userId);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buf = (ByteBuf) msg;

        while (buf.readableBytes() > 0) {
            if (currentState == HandelerState.IDLE) {
                byte readed = buf.readByte();
                if (readed == Dictionary.GET_FILE_FROM_CLIENT) {
                    currentState = HandelerState.PROCESS_INCOME_FILE;

                } else if (readed == Dictionary.SERVER_FILE_LIST) {
                    fileService.getFileList(Paths.get(ServerFileService.SERVER_BASE_FOLDER, String.valueOf(userId)), ctx);
                    currentState = HandelerState.IDLE;

                } else if (readed == Dictionary.SEND_FILE_TO_CLIENT) {
                    if (buf.readableBytes() >= Dictionary.INT_LENGTH
                            && fileService.getIncomeFileNameLength() == 0
                    ){
                        fileService.setIncomeFileNameLength(buf.readInt());
                    }

                    if (buf.readableBytes() >= fileService.getIncomeFileNameLength()){
                        byte[] fileName = new byte[fileService.getIncomeFileNameLength()];
                        buf.readBytes(fileName);
                        fileService.setIncomeFileName(new String(fileName, Dictionary.CHAR_SET));
                        fileService.sendFileToClient(userId, ctx);

                        currentState    = HandelerState.IDLE;
                    }

                } else {
                    System.out.println("ERROR: Invalid first byte - " + readed);

                }
            } else if (currentState == HandelerState.PROCESS_INCOME_FILE) {
                if (buf.readableBytes() >= Dictionary.INT_LENGTH
                        && fileService.getIncomeFileNameLength() == 0
                ) {
                    fileService.setIncomeFileNameLength(buf.readInt());

                } else if (buf.readableBytes() >= fileService.getIncomeFileNameLength()
                        && fileService.getIncomeFileName() == null
                ) {
                    byte[] fileName = new byte[fileService.getIncomeFileNameLength()];
                    buf.readBytes(fileName);
                    fileService.setIncomeFileName(new String(fileName, Dictionary.CHAR_SET));
                    fileService.setFileLocator(new String(fileName, Dictionary.CHAR_SET), userId);

                } else if (buf.readableBytes() >= Dictionary.LONG_LENGTH
                            && fileService.getIncomeFileLength() == 0
                ) {
                    fileService.setIncomeFileLength(buf.readLong());

                } else if (buf.readableBytes() > 0
                            && fileService.getIncomeFileLength() > 0
                ) {
                    while (buf.readableBytes() > 0) {
                        fileService.processIncomeFileBody(buf.readByte());

                        if (fileService.getIncomeFileLength() == fileService.getReceivedFileLength()) {
                            fileService.finalizeIncomeFile();
                            fileService.getFileList(Paths.get(ServerFileService.SERVER_BASE_FOLDER, String.valueOf(userId)), ctx);
                            currentState = HandelerState.IDLE;

                            System.out.println("Income file was processed.");
                            break;
                        }
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
