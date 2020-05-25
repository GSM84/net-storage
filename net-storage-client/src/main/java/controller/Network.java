package controller;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CountDownLatch;

public class Network {
    private static Network ourInstance = new Network();

    public static Network getInstance() {
        return ourInstance;
    }

    private Network() {
    }

    private Channel currentChannel;

    public Channel getCurrentChannel() {
        return currentChannel;
    }

    public void start(CountDownLatch countDownLatch, String _host, int _port) {
        EventLoopGroup group = new NioEventLoopGroup(1);
        try {
            Bootstrap clientBootstrap = new Bootstrap();
            clientBootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .remoteAddress(new InetSocketAddress(_host, _port))
                    .handler(new ChannelInitializer<SocketChannel>() {
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline().addLast(new IncomeHandler());
                            currentChannel = socketChannel;
                        }
                    });
            ChannelFuture channelFuture = clientBootstrap.connect().sync();
            countDownLatch.countDown();
            channelFuture.channel().closeFuture().sync();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                group.shutdownGracefully().sync();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void stop() {
        currentChannel.close();
    }

    public boolean authorizeUser(String _login, String _password){
        try {
            byte[] loginByte = _login.getBytes(Dictionary.CHAR_SET);
            byte[] passByte  = _password.getBytes(Dictionary.CHAR_SET);
            ByteBuf buff = ByteBufAllocator.DEFAULT.directBuffer(1 + 4 + loginByte.length + 4 + passByte.length);
            buff.writeByte(Dictionary.AUTH);
            buff.writeInt(loginByte.length);
            buff.writeBytes(loginByte);
            buff.writeInt(passByte.length);
            buff.writeBytes(passByte);
            currentChannel.writeAndFlush(buff);

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return true;
    }

    public void sendFileHeader(byte _signalByte, Path _filePath){
        try {
            byte[]  nameByte   = _filePath.getFileName().toString().getBytes(Dictionary.CHAR_SET);
            ByteBuf buff       = ByteBufAllocator.DEFAULT.directBuffer(1 + 4 + nameByte.length);
            buff.writeByte(_signalByte);
            buff.writeInt(nameByte.length);
            buff.writeBytes(nameByte);
            currentChannel.writeAndFlush(buff);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendFileHeader(byte _signalByte, String _fileName){
        try {
            byte[]  nameByte   = _fileName.getBytes(Dictionary.CHAR_SET);
            ByteBuf buff       = ByteBufAllocator.DEFAULT.directBuffer(1 + 4 + nameByte.length);
            buff.writeByte(_signalByte);
            buff.writeInt(nameByte.length);
            buff.writeBytes(nameByte);
            currentChannel.writeAndFlush(buff);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendFileLength(long _fileLength){
        ByteBuf buff = ByteBufAllocator.DEFAULT.directBuffer(Dictionary.LONG_LENGTH);
        buff.writeLong(_fileLength);
        currentChannel.writeAndFlush(buff);
    }

    public void getFileList(){
        ByteBuf buff       = ByteBufAllocator.DEFAULT.directBuffer(1);
        buff.writeByte(Dictionary.FILE_LIST);
        currentChannel.writeAndFlush(buff);
    }

    public void getFile(String _fileName){
        try {
            byte[]  nameByte   = _fileName.getBytes(Dictionary.CHAR_SET);
            ByteBuf buff       = ByteBufAllocator.DEFAULT.directBuffer(1 + 4 + nameByte.length);
            buff.writeByte(Dictionary.GET_FILE);
            buff.writeInt(nameByte.length);
            buff.writeBytes(nameByte);
            currentChannel.writeAndFlush(buff);

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

    }
}
