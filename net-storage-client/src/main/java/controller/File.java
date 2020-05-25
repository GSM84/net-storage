package controller;

import io.netty.channel.ChannelFuture;
import io.netty.channel.DefaultFileRegion;
import io.netty.channel.FileRegion;
import javafx.scene.control.ListView;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

public class File {
    private static final String CLIENT_ROOT_FOLDER = "C:\\GITHUB\\test\\src\\1";
    private static byte[]               fileListByte;
    private static BufferedOutputStream fileLocator;
    private static java.io.File         incomeFile;

    private static File   file           = new File();
    private static int    fileNameLength = 0;
    private static String fileName;
    private static long   fileLength     = 0L;

    private static Controller controller;

    private File(){}

    public static File getInstance(){
        return file;
    }

    public void setController(Controller controller) {
        this.controller = controller;
    }

    public static void transferFile(String _fileName){
        try {
            FileRegion region = new DefaultFileRegion(Paths.get(CLIENT_ROOT_FOLDER, _fileName).toFile(), 0, Files.size(Paths.get(CLIENT_ROOT_FOLDER, _fileName)));
            // send file header
            Network.getInstance().sendFileHeader(Dictionary.SEND_FILE, Paths.get(CLIENT_ROOT_FOLDER, _fileName));
            // send file length
            Network.getInstance().sendFileLength(Files.size(Paths.get(CLIENT_ROOT_FOLDER, _fileName)));
            // send file
            ChannelFuture transferOperationFuture = Network.getInstance().getCurrentChannel().writeAndFlush(region);
        } catch (IOException e) {
            System.out.println("File transfer comlite.");
            e.printStackTrace();
        }
    }

    public static void loadFileList(ListView _listView){
        try {
            _listView.getItems().clear();
            Files.list(Paths.get(CLIENT_ROOT_FOLDER))
                    .filter(p -> !Files.isDirectory(p))
                    .map(p -> p.getFileName().toString())
                    .forEach(o -> _listView.getItems().add(o));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void collectFileList(long _position, byte _byte){
        if (fileListByte == null) {
            fileListByte = new byte[fileNameLength];
        }

        fileListByte[(int) _position] = _byte;
    }

    public static void converFIleList(){
        controller.getServerFiles().getItems().clear();

        Arrays.stream(new String(fileListByte)
                .split(" "))
                .forEach(o -> controller.getServerFiles().getItems().add(o));

        fileListByte   = null;
        fileNameLength = 0;
    }

    public static int getFileNameLength() {
        return fileNameLength;
    }

    public static void setFileNameLength(int _fileLength) {
        File.fileNameLength = _fileLength;
    }

    public static String getFileName() {
        return fileName;
    }

    public static void setFileName(String fileName) {
        File.fileName = fileName;
    }

    public static long getFileLength() {
        return fileLength;
    }

    public static void setFileLength(long fileLength) {
        File.fileLength = fileLength;
    }

    public static void processFileBody(byte _byte){
        if (fileLocator == null){
            try {
                incomeFile = Paths.get(CLIENT_ROOT_FOLDER, fileName).toFile();
                fileLocator = new BufferedOutputStream(new FileOutputStream(incomeFile));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }

        try {
            fileLocator.write(_byte);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void finalizeFile(){
        try {
            fileLocator.close();
            fileLocator    = null;
            incomeFile     = null;
            fileName       = null;
            fileNameLength = 0;
            fileLength     = 0L;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Controller getController() {
        return controller;
    }
}
