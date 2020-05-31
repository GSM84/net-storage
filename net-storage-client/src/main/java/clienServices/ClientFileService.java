package clienServices;

import dictionaryes.Dictionary;
import io.netty.channel.ChannelFuture;
import io.netty.channel.DefaultFileRegion;
import io.netty.channel.FileRegion;
import javafx.scene.control.ListView;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

public class ClientFileService {
    private static String               clientRootFolder = "";
    private        byte[]               fileListByte;

    private        File                 fileLocator;
    private        BufferedOutputStream streamLocator;
    private        int                  fileNameLength        = 0;
    private        String               fileName;
    private        long                 messageLength         = 0;
    private        long                 receivedMessageLength = 0;

    public static void transferFileToServer(String _fileName){
        try {
            FileRegion region = new DefaultFileRegion(Paths.get(clientRootFolder, _fileName).toFile(), 0, Files.size(Paths.get(clientRootFolder, _fileName)));
            // send file header
            Network.getInstance().sendFileHeader(Dictionary.SEND_FILE_TO_SERVER, Paths.get(clientRootFolder, _fileName));
            // send file length
            Network.getInstance().sendFileLength(Files.size(Paths.get(clientRootFolder, _fileName)));
            // send file
            ChannelFuture transferOperationFuture = Network.getInstance().getCurrentChannel().writeAndFlush(region);
        } catch (IOException e) {
            System.out.println("File transfer comlite.");
            e.printStackTrace();
        }
    }

    public static void refreshClientFileList(ListView _listView){
        try {
            _listView.getItems().clear();
            Files.list(Paths.get(clientRootFolder))
                    .filter(p -> !Files.isDirectory(p))
                    .map(p -> p.getFileName().toString())
                    .forEach(o -> _listView.getItems().add(o));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void collectFileList(byte _byte){
        if (fileListByte == null) {
            fileListByte = new byte[fileNameLength];
        }

        fileListByte[(int) receivedMessageLength] = _byte;
        receivedMessageLength++;
    }

    public void converFileList(ListView _serverFileList){
        _serverFileList.getItems().clear();

        Arrays.stream(new String(fileListByte)
                .split(Dictionary.FILE_NAME_SEPARATOR))
                .forEach(o -> _serverFileList.getItems().add(o));

        fileListByte         = null;
        setIncomeFileNameLength(0);
        setReceivedMessageLength(0);
    }

    public void setIncomeFileNameLength(int _fileLength) {
        fileNameLength = _fileLength;
    }

    public int getIncomFileNameLength() {
        return fileNameLength;
    }

    public String getIncomeFileName() {
        return fileName;
    }

    public void setIncomeFileName(String _fileName) {
        System.out.println("Income file nameis " + _fileName);
        this.fileName = _fileName;
    }

    public long getMessageLength() {
        return messageLength;
    }

    public void setMessageLength(long _fileLength) {
        this.messageLength = _fileLength;
    }

    public void processIncomeFileBody(byte _byte){
        if (streamLocator == null){
            try {
                fileLocator = Paths.get(clientRootFolder, fileName).toFile();
                streamLocator = new BufferedOutputStream(new FileOutputStream(fileLocator));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }

        try {
            streamLocator.write(_byte);
            receivedMessageLength++;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void finalizeIncomeFile(){
        try {
            streamLocator.close();
            streamLocator = null;
            fileLocator   = null;
            fileName      = null;
            setIncomeFileNameLength(0);
            setReceivedMessageLength(0);
            setMessageLength(0);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public long getReceivedMessageLength() {
        return receivedMessageLength;
    }

    public void setReceivedMessageLength(long receivedMessageLength) {
        this.receivedMessageLength = receivedMessageLength;
    }
}
