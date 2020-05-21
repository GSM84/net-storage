package handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import services.AuthService;

public class AuthHandler extends ChannelInboundHandlerAdapter {
    private HandelerState       currentState      = HandelerState.WAITING_FOR_AUTH;
    private int                 credentialLength;
    private String              userLogin;
    private String              userPass;

    private static final byte   AUTH              = 14;
    private static final int    CREDENTIAL_LENGTH = 4;
    private static final String CHAR_SET          = "UTF-8";

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buf = ((ByteBuf) msg);

        while (buf.readableBytes() > 0) {
            if (currentState == HandelerState.WAITING_FOR_AUTH){
                byte readed = buf.readByte();
                if (readed == AUTH) {
                    System.out.println("STATE: Start processing authorization");
                    currentState = HandelerState.GET_CREDENTIAL_LENGTH;
                } else {
                    System.out.println("ERROR: Invalid first byte - " + readed);
                }
            } else if (currentState == HandelerState.GET_CREDENTIAL_LENGTH){
                if (buf.readableBytes() >= CREDENTIAL_LENGTH) {
                    System.out.println("STATE: Get credential length");

                    credentialLength = buf.readInt();
                    if (userLogin == null){
                        currentState = HandelerState.GET_LOGIN;
                    } else {
                        currentState = HandelerState.GET_PASS;
                    }
                }
            } else if (currentState == HandelerState.GET_LOGIN || currentState == HandelerState.GET_PASS){
                if (buf.readableBytes() >= credentialLength) {
                    System.out.println("STATE: get login/password");

                    byte[] credentialByte = new byte[credentialLength];
                    buf.readBytes(credentialByte);

                    if (userLogin == null){
                        userLogin = new String(credentialByte, CHAR_SET);
                        System.out.println("STATE: Login recived - " + userLogin);
                        currentState = HandelerState.GET_CREDENTIAL_LENGTH;
                    } else {
                        System.out.println("STATE: Password received.");
                        userPass  = new String(credentialByte, CHAR_SET);

                        if (AuthService.authorize(userLogin, userPass)){
                            // добавить блок обработки запросов
                            System.out.println("STATE: Authorized.");
                            currentState = HandelerState.AUHORIZED;
                        } else {
                            System.err.println("Incorrect password for user - " + userLogin);
                            currentState = HandelerState.WAITING_FOR_AUTH;
                        }
                    }
                }
            } else if (currentState == HandelerState.AUHORIZED) {
                // fire message to next handler
                ctx.fireChannelRead(msg);
            }
        }

        if (buf.readableBytes() == 0) {
            buf.release();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        System.out.println(String.format("Client %s has closed connection.", userLogin));
        ctx.close();
    }
}
