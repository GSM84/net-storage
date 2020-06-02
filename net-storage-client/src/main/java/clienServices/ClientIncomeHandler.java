package clienServices;

import callBackInterface.ClientCallBack;
import callBackInterface.ServerFileListCallBack;
import dictionaryes.Dictionary;
import dictionaryes.HandelerState;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class ClientIncomeHandler extends ChannelInboundHandlerAdapter {
    private HandelerState          currentState;
    private ClientFileService      clientFileService;
    private ClientCallBack         clientFileListCallBack;
    private ServerFileListCallBack serverFileListCallBack;
    private ClientCallBack         authCallBack;
    private ClientCallBack         alertCallBack;

    public ClientIncomeHandler(){
        this.currentState      = HandelerState.IDLE;
        this.clientFileService = new ClientFileService();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buf = ((ByteBuf) msg);

        while (buf.readableBytes() > 0) {
            if (currentState == HandelerState.IDLE) {
                byte controlByte = buf.readByte();
                if (controlByte == Dictionary.SERVER_FILE_LIST) {
                    System.out.println("Process file list.");
                    currentState = HandelerState.PROCESS_FILE_LIST;

                } else if (controlByte == Dictionary.GET_FILE_FROM_SERVER) {
                    System.out.println("Process incoming file.");
                    currentState = HandelerState.PROCESS_INCOME_FILE;

                } else if (controlByte == Dictionary.FAILED_AUTH) {
                    alertCallBack.callBack();
                    System.out.println("Failed authorization");

                    currentState = HandelerState.IDLE;
                } else if (controlByte == Dictionary.SUCCESSEFUL_AUTH) {
                    authCallBack.callBack();
                    System.out.println("Successfull authorization");
                    currentState = HandelerState.IDLE;
                } else {
                    System.out.println("ERROR: Invalid contol byte - " + controlByte);
                }

            } else if (currentState == HandelerState.PROCESS_FILE_LIST) {
                if (buf.readableBytes() >= Dictionary.INT_LENGTH
                        && clientFileService.getIncomFileNameLength() == 0
                ){
                    clientFileService.setIncomeFileNameLength(buf.readInt());

                } else if (clientFileService.getIncomFileNameLength() > 0){
                    while (buf.readableBytes() > 0) {
                        clientFileService.collectFileList(buf.readByte());

                        if (clientFileService.getIncomFileNameLength() == clientFileService.getReceivedMessageLength()) {
                            clientFileService.converFileList(serverFileListCallBack.getServerList());
                            clientFileService.setIncomeFileNameLength(0);
                            currentState = HandelerState.IDLE;

                            System.out.println("File list was processed.");

                            break;
                        }
                    }
                }

            } else if (currentState == HandelerState.PROCESS_INCOME_FILE){
                if (buf.readableBytes() >= Dictionary.INT_LENGTH
                        && clientFileService.getIncomFileNameLength() == 0
                ) {
                    clientFileService.setIncomeFileNameLength(buf.readInt());

                } else if (clientFileService.getIncomFileNameLength() > 0
                        && clientFileService.getIncomeFileName() == null
                ) {
                    if(buf.readableBytes() >= clientFileService.getIncomFileNameLength()) {
                        byte[] fileNameByte = new byte[clientFileService.getIncomFileNameLength()];
                        buf.readBytes(fileNameByte);
                        clientFileService.setIncomeFileName(new String(fileNameByte, Dictionary.CHAR_SET));
                    }

                } else if (buf.readableBytes() > 0
                            && clientFileService.getIncomeFileName().length() > 0
                            && clientFileService.getMessageLength() == 0
                ) {
                    if (buf.readableBytes() >= 0) {
                        clientFileService.setMessageLength(buf.readLong());
                    }

                } else if (buf.readableBytes() > 0
                            && clientFileService.getMessageLength() > 0
                ) {
                    while (buf.readableBytes() > 0) {
                        clientFileService.processIncomeFileBody(buf.readByte());

                        if (clientFileService.getMessageLength() == clientFileService.getReceivedMessageLength()) {
                            clientFileService.finalizeIncomeFile();
                            clientFileListCallBack.callBack();
                            currentState = HandelerState.IDLE;

                            System.out.println("Income file was processd.");
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

    public void setClientFileListCallBack(ClientCallBack _clientFileListCallBack) {
        this.clientFileListCallBack = _clientFileListCallBack;
    }

    public void setServerFileListCallBack(ServerFileListCallBack _serverFileListCallBack) {
        this.serverFileListCallBack = _serverFileListCallBack;
    }

    public void setAuthCallBack(ClientCallBack _authCallBack) {
        this.authCallBack = _authCallBack;
    }

    public void setAlertCallBack(ClientCallBack _alertCallBack){
        this.alertCallBack = _alertCallBack;
    }
}
