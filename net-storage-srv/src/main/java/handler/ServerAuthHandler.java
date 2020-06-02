package handler;

import dictionaryes.Dictionary;
import dictionaryes.HandelerState;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import services.AuthService;

public class ServerAuthHandler extends ChannelInboundHandlerAdapter {
    private HandelerState currentState      = HandelerState.WAITING_FOR_AUTH;
    private int           credentialLength;
    private AuthService   authService       = new AuthService();
    private boolean       isNewUser         = false;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buf = ((ByteBuf) msg);

        while (buf.readableBytes() > 0) {
            if (currentState == HandelerState.WAITING_FOR_AUTH){
                byte controlByte = buf.readByte();
                if (controlByte == Dictionary.AUTH) {
                    currentState = HandelerState.GET_CREDENTIAL_LENGTH;

                } else if (controlByte == Dictionary.AUTH_REG_NEW_USER) {
                    isNewUser = true;
                    currentState = HandelerState.GET_CREDENTIAL_LENGTH;

                } else {
                    System.out.println("ERROR: Invalid first byte - " + controlByte);
                }
            } else if (currentState == HandelerState.GET_CREDENTIAL_LENGTH){
                if (buf.readableBytes() >= Dictionary.INT_LENGTH) {
                    credentialLength = buf.readInt();
                    if (authService.getLogin() == null){
                        currentState = HandelerState.GET_LOGIN;
                    } else {
                        currentState = HandelerState.GET_PASS;
                    }
                }
            } else if (currentState == HandelerState.GET_LOGIN || currentState == HandelerState.GET_PASS){
                if (buf.readableBytes() >= credentialLength) {
                    byte[] credentialByte = new byte[credentialLength];
                    buf.readBytes(credentialByte);

                    if (authService.getLogin() == null){
                        authService.setLogin(new String(credentialByte, Dictionary.CHAR_SET));
                        currentState = HandelerState.GET_CREDENTIAL_LENGTH;

                    } else {
                        authService.setPassword(new String(credentialByte, Dictionary.CHAR_SET));

                        if (!isNewUser && authService.authorizeClient()) {
                            ctx.pipeline().addLast(new ServerIncomeHandler(authService.getUserId()));
                            authService.sendResponseToClient(Dictionary.SUCCESSEFUL_AUTH, ctx);
                            currentState = HandelerState.AUHORIZED;

                        } else if (isNewUser && authService.registerNewUser()){
                            ctx.pipeline().addLast(new ServerIncomeHandler(authService.getUserId()));
                            authService.sendResponseToClient(Dictionary.SUCCESSEFUL_AUTH, ctx);
                            currentState = HandelerState.AUHORIZED;

                        } else {
                            authService.sendResponseToClient(Dictionary.FAILED_AUTH, ctx);
                            authService.clearCredentials();
                            currentState = HandelerState.WAITING_FOR_AUTH;

                        }
                    }
                }

            } else if (currentState == HandelerState.AUHORIZED) {
                ctx.fireChannelRead(msg);
            }
        }

        if (buf.readableBytes() == 0) {
            buf.release();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
    }

    public AuthService getAuthService() {
        return authService;
    }
}
