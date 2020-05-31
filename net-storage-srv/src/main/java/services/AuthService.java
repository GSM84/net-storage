package services;

import dictionaryes.Dictionary;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

public class AuthService {
    private String  login;
    private String  password;
    private ByteBuf buffer;

    private int     currUserId;

    public boolean authorizeClient(){
        if (DBService.authUser(login, password)){
            currUserId = DBService.getAuthUserId();
            DBService.unsetUserId();
            return true;

        } else {
            System.err.println("Unsuccessful authorization.");
        }
        return false;
    }

    public boolean registerNewUser(){
        if (!DBService.authUser(login, password)){
            if (DBService.regUser(login, password)) {
                currUserId = DBService.getAuthUserId();
                DBService.unsetUserId();
                return true;

            }
            return false;

        } else {
            System.err.println(String.format("User with login: %s already exists.", login));
        }
        return false;
    }

    public int getUserId(){
        return currUserId;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void sendResponseToClient(Byte _signalByte, ChannelHandlerContext _context){
        buffer = _context.alloc().buffer(Dictionary.BYTE_LENGTH);
        buffer.writeByte(_signalByte);
        _context.writeAndFlush(buffer);
    }

    public void clearCredentials(){
        login    = null;
        password = null;
    }
}
