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
                if (readed == Dictionary.FILE_LIST) {
                    currentState       = ReaderState.PROCESS_FILE_LIST;
                    receivedFileLength = ZERO_LENGTH;

                    System.out.println("STATE: Start file list processing");
                } else if (readed == Dictionary.GET_FILE) {
                    System.out.println("Porcess income file");
                    currentState = ReaderState.PROCESS_INCOME_FILE;

                } else {
                    System.out.println("ERROR: Invalid first byte - " + readed);
                }
            } else if (currentState == ReaderState.PROCESS_FILE_LIST) {
                if (buf.readableBytes() >= 4 && File.getFileNameLength() == 0){
                    File.setFileNameLength(buf.readInt());

                }

                if (File.getFileNameLength() > 0){
                    while (buf.readableBytes() > 0) {
                        File.collectFileList(receivedFileLength, buf.readByte());
                        receivedFileLength++;
                        if (File.getFileNameLength() == receivedFileLength) {
                            currentState = ReaderState.IDLE;
                            File.getInstance().converFIleList();
                            receivedFileLength = 0;
                            break;
                        }
                    }
                }
            } else if (currentState == ReaderState.PROCESS_INCOME_FILE){
                if (buf.readableBytes() >= 4 && File.getFileNameLength() == 0) {
                    File.setFileNameLength(buf.readInt());

                } else if (File.getFileNameLength() > 0 && File.getFileName() == null) {
                    if(buf.readableBytes() >= File.getFileNameLength()) {
                        byte[] fileNameByte = new byte[File.getFileNameLength()];
                        buf.readBytes(fileNameByte);
                        File.setFileName(new String(fileNameByte, Dictionary.CHAR_SET));
                    }

                } else if (buf.readableBytes() > 0 && File.getFileName() != null && File.getFileLength() == 0) {
                    if (buf.readableBytes() >= 8) {
                        File.setFileLength(buf.readLong());
                    }

                } else if (buf.readableBytes() > 0 && File.getFileLength() > 0){
                    while (buf.readableBytes() > 0) {
                        File.processFileBody(buf.readByte());
                        receivedFileLength++;
                        if (File.getFileLength() == receivedFileLength) {
                            currentState = ReaderState.IDLE;
                            receivedFileLength = 0;
                            File.finalizeFile();
                            File.loadFileList(File.getController().getClientFiles());
                            break;
                        }
                    }
                }
            }
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
