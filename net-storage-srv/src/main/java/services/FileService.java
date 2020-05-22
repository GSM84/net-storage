package services;

import handler.Dictionary;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

import java.io.IOException;
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

            byte[] listByte = fileList.getBytes(Dictionary.CHAR_SET);

            ByteBuf buffer = _context.alloc().buffer(1 + 4 + listByte.length);
            buffer.writeByte(Dictionary.FILE_LIST);
            buffer.writeInt(listByte.length);
            buffer.writeBytes(listByte);
            _context.writeAndFlush(buffer);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void getFile(String _fileName, ChannelHandlerContext ctx){
        if (Paths.get(_fileName).toFile().exists()){

        } else {
            System.out.println("Запрашиваемый файл не существует");
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
}
