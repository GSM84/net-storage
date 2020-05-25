package services;

import handler.Dictionary;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.DefaultFileRegion;
import io.netty.channel.FileRegion;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;

public class FileService {
    private int    incomeFileNameLength = 0;
    private String incomeFileName;

    public void getFileList(Path _filePath, ChannelHandlerContext _context){
        System.out.println("processing request");
        try {
            String fileList = Files.list(_filePath)
                    .filter(p -> !Files.isDirectory(p))
                    .map(p -> p.getFileName().toString())
                    .collect(Collectors.joining(" "));

            if (fileList.length() > 0) {
                byte[] listByte = fileList.getBytes(Dictionary.CHAR_SET);

                ByteBuf buffer = _context.alloc().buffer(1 + 4 + listByte.length);
                buffer.writeByte(Dictionary.FILE_LIST);
                buffer.writeInt(listByte.length);
                buffer.writeBytes(listByte);
                _context.writeAndFlush(buffer);
            } else {
                System.out.println("Client folder is empty");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void getFile(int _userId, ChannelHandlerContext ctx){
        Path filePath = Paths.get(Dictionary.SERVER_BASE_FOLDER, String.valueOf(_userId), incomeFileName);
        if (filePath.toFile().exists()){
            System.out.println("Запрошен файл "+ incomeFileName);
            try {
                // send file header
                sendFileHeader(Dictionary.SEND_FILE, filePath, ctx);
                // send file length
                sendFileLength(Files.size(filePath), ctx);
                // send file body
                sendFileBody(filePath, ctx);
            } catch (IOException e) {
                e.printStackTrace();
            }


            incomeFileName       = null;
            incomeFileNameLength = 0;
        } else {
            System.out.println("Запрашиваемый файл не существует");
            incomeFileName       = null;
            incomeFileNameLength = 0;
        }
    }

    public int getIncomeFileNameLength() {
        return incomeFileNameLength;
    }

    public void setIncomeFileNameLength(int incoleFileNameLength) {
        this.incomeFileNameLength = incoleFileNameLength;
    }

    public String getIncomeFileName() {
        return incomeFileName;
    }

    public void setIncomeFileName(String incomeFileName) {
        this.incomeFileName = incomeFileName;
    }

    private void sendFileHeader(byte _signalByte, Path _filePath, ChannelHandlerContext _ctx){
        try {
            byte[] nameByte = _filePath.getFileName().toString().getBytes(Dictionary.CHAR_SET);
            ByteBuf buffer = ByteBufAllocator.DEFAULT.directBuffer(Dictionary.BYTE_LENGTH
                                                                    + Dictionary.INT_LENGTH
                                                                    + nameByte.length);
            buffer.writeByte(_signalByte);
            buffer.writeInt(nameByte.length);
            buffer.writeBytes(nameByte);
            _ctx.writeAndFlush(buffer);

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    private void sendFileLength(long _fileLength, ChannelHandlerContext _ctx){
        ByteBuf buffer = ByteBufAllocator.DEFAULT.directBuffer(Dictionary.LONG_LENGTH);
        buffer.writeLong(_fileLength);
        _ctx.writeAndFlush(buffer);
    }

    private void sendFileBody(Path _filePath, ChannelHandlerContext _ctx){
        FileRegion region;
        try {
            region = new DefaultFileRegion(_filePath.toFile(), 0, Files.size(_filePath));
            ChannelFuture transferOperationFuture = _ctx.writeAndFlush(region);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
