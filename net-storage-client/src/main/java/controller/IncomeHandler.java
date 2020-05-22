package controller;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class IncomeHandler extends ChannelInboundHandlerAdapter {
    private ReaderState currentState = ReaderState.IDLE;
    private static final long ZERO_LENGTH = 0L;

    private long                 receivedFileLength;
    private long                 messageLength;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buf = ((ByteBuf) msg);

        while (buf.readableBytes() > 0) {
            if (currentState == ReaderState.IDLE) {
                byte readed = buf.readByte();
                if (readed == Network.FILE_LIST) {
                    currentState       = ReaderState.PROCESS_FILE_LIST;
                    receivedFileLength = ZERO_LENGTH;

                    System.out.println("STATE: Start file list processing");
                } else {
                    System.out.println("ERROR: Invalid first byte - " + readed);
                }
            } else if (currentState == ReaderState.PROCESS_FILE_LIST) {
                if (buf.readableBytes() >= 4){
                    File.setFileListLength(buf.readInt());
                }

                if (File.getFileListLength() > 0){
                    while (buf.readableBytes() > 0) {
                        File.collectFileList(receivedFileLength, buf.readByte());
                        receivedFileLength++;
                        if (File.getFileListLength() == receivedFileLength) {
                            currentState = ReaderState.IDLE;
                            System.out.println("File received return name");
                            File.getInstance().converFIleList();
                            break;
                        }
                    }
                }

            }
            //System.out.println((char)buf.readByte());
        }

        if (buf.readableBytes() == 0) {
            buf.release();
        }

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
