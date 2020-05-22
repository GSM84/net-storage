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
    public static final byte   AUTH              = 14;
    public static final byte   FILE_TRANSFER     = 28;
    public static final byte   FILE_LIST         = 31;
    public static final byte   GET_FILE          = 30;
    private static final String CHAR_SET         = "UTF-8";

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
        EventLoopGroup group = new NioEventLoopGroup();
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
            byte[] loginByte = _login.getBytes(CHAR_SET);
            byte[] passByte  = _password.getBytes(CHAR_SET);
            ByteBuf buff = ByteBufAllocator.DEFAULT.directBuffer(1 + 4 + loginByte.length + 4 + passByte.length);
            buff.writeByte(AUTH);
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

    public void writeFileHeader(Path _filePath){
        try {
            byte[]  nameByte   = _filePath.getFileName().toString().getBytes(CHAR_SET);
            long    fileLength = Files.size(_filePath);
            ByteBuf buff       = ByteBufAllocator.DEFAULT.directBuffer(1 + 4 + nameByte.length + 8);
            buff.writeByte(FILE_TRANSFER);
            buff.writeInt(nameByte.length);
            buff.writeBytes(nameByte);
            buff.writeLong(fileLength);
            currentChannel.writeAndFlush(buff);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void getFileList(){
        ByteBuf buff       = ByteBufAllocator.DEFAULT.directBuffer(1);
        buff.writeByte(FILE_LIST);
        currentChannel.writeAndFlush(buff);
    }

    public void getFile(String _fileName){
        try {
            byte[]  nameByte   = _fileName.getBytes(CHAR_SET);
            ByteBuf buff       = ByteBufAllocator.DEFAULT.directBuffer(1 + 4 + nameByte.length);
            buff.writeByte(GET_FILE);
            buff.writeInt(nameByte.length);
            buff.writeBytes(nameByte);
            currentChannel.writeAndFlush(buff);

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

    }
}
