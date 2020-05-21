package handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;

public class IncomeHandler extends ChannelInboundHandlerAdapter {

    private static final long   ZERO_LENGTH = 0L;
    private static final byte   FILE        = 28;

    private static final byte   FILE_LIST   = 31;
    private static final String CHAR_SET    = "UTF-8";

    private static final int    NAME_LENGTH = 4;
    private static final int    FILE_LENGTH = 8;

    private static final String SERVER_BASE_FOLDER  = "Storage\\1\\";

    private HandelerState        currentState       = HandelerState.IDLE;
    private int                  nextLength;
    private long                 fileLength;
    private long                 receivedFileLength;
    private BufferedOutputStream out;

    private String stringName;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buf = ((ByteBuf) msg);

        while (buf.readableBytes() > 0) {
            if (currentState == HandelerState.IDLE) {
                byte readed = buf.readByte();
                if (readed == FILE) {
                    currentState = HandelerState.GET_NAME_LENGTH;
                    receivedFileLength = ZERO_LENGTH;
                    System.out.println("STATE: Start file receiving");
                } else {
                    System.out.println("ERROR: Invalid first byte - " + readed);
                }
            } else if (currentState == HandelerState.GET_NAME_LENGTH) {
                if (buf.readableBytes() >= NAME_LENGTH) {
                    System.out.println("STATE: Get filename length");
                    nextLength = buf.readInt();
                    currentState = HandelerState.GET_NAME;
                }
            } else if (currentState == HandelerState.GET_NAME) {
                if (buf.readableBytes() >= nextLength) {
                    byte[] fileName = new byte[nextLength];
                    buf.readBytes(fileName);
                    stringName = new String(fileName, CHAR_SET);
                    System.out.println("STATE: Filename received - " + stringName);
                    out = new BufferedOutputStream(new FileOutputStream(SERVER_BASE_FOLDER + stringName));
                    currentState = HandelerState.GET_FILE_LENGTH;
                }
            } else if (currentState == HandelerState.GET_FILE_LENGTH) {
                if (buf.readableBytes() >= FILE_LENGTH) {
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
                        ctx.writeAndFlush(stringName.getBytes(CHAR_SET));
                        out.close();
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
