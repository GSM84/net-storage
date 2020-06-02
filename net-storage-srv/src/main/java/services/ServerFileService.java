package services;

import dictionaryes.Dictionary;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.DefaultFileRegion;
import io.netty.channel.FileRegion;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;

public class ServerFileService {
    public static final String               SERVER_BASE_FOLDER   = "Storage";
    private             File                 fileLocator;
    private             BufferedOutputStream streamLocator;
    private             int                  incomeFileNameLength = 0;
    private             String               incomeFileName;
    private             long                 incomeFileLength     = 0;
    private             long                 receivedFileLength   = 0;

    public ServerFileService(int _userId){
        File userDir = Paths.get(SERVER_BASE_FOLDER, String.valueOf(_userId)).toFile();
        if (!userDir.exists()){
            userDir.mkdir();
        }
    }

    public void getFileList(Path _filePath, ChannelHandlerContext _context){
        System.out.println("processing request");
        try {
            String fileList = Files.list(_filePath)
                    .filter(p -> !Files.isDirectory(p))
                    .map(p -> p.getFileName().toString())
                    .collect(Collectors.joining(Dictionary.FILE_NAME_SEPARATOR));

            if (fileList.length() > 0) {
                byte[] listByte = fileList.getBytes(Dictionary.CHAR_SET);

                ByteBuf buffer = _context.alloc().buffer(Dictionary.BYTE_LENGTH
                                                          + Dictionary.INT_LENGTH
                                                          + listByte.length);
                buffer.writeByte(Dictionary.SERVER_FILE_LIST);
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

    public void sendFileToClient(int _userId, ChannelHandlerContext ctx){
        Path filePath = Paths.get(SERVER_BASE_FOLDER, String.valueOf(_userId), incomeFileName);
        if (filePath.toFile().exists()){
            System.out.println("Запрошен файл "+ incomeFileName);
            try {
                // send file header
                sendFileHeader(Dictionary.SEND_FILE_TO_CLIENT, filePath, ctx);
                // send file length
                sendFileLength(Files.size(filePath), ctx);
                // send file body
                sendFileBody(filePath, ctx);
            } catch (IOException e) {
                e.printStackTrace();
            }

            resetFileVariables();
        } else {
            System.err.println("Запрашиваемый файл не существует");
            resetFileVariables();
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

    private void resetFileVariables(){
        incomeFileName       = null;
        incomeFileNameLength = 0;
    }

    public long getReceivedFileLength() {
        return receivedFileLength;
    }

    public void setReceivedFileLength(long receivedFileLength) {
        this.receivedFileLength = receivedFileLength;
    }

    public void processIncomeFileBody(byte _byte){
        if (streamLocator == null){
            try {
                streamLocator = new BufferedOutputStream(new FileOutputStream(fileLocator));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }

        try {
            streamLocator.write(_byte);
            receivedFileLength++;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void finalizeIncomeFile(){
        try {
            streamLocator.close();
            streamLocator  = null;
            fileLocator    = null;
            incomeFileName = null;
            setIncomeFileNameLength(0);
            setReceivedFileLength(0);
            setIncomeFileLength(0);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setFileLocator(String _fileName, int _userId){
        fileLocator   = Paths.get(SERVER_BASE_FOLDER, String.valueOf(_userId), _fileName).toFile();
    }

    public long getIncomeFileLength() {
        return incomeFileLength;
    }

    public void setIncomeFileLength(long incomeFileLength) {
        this.incomeFileLength = incomeFileLength;
    }
}
