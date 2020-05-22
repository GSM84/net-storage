package controller;

import io.netty.channel.ChannelFuture;
import io.netty.channel.DefaultFileRegion;
import io.netty.channel.FileRegion;
import javafx.scene.control.ListView;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

public class File {
    private static final String CLIENT_ROOT_FOLDER = "C:\\GITHUB\\test\\src\\1";
    private static       byte[] fileListByte;

    private static File file = new File();
    private static int fileListLength = 0;

    private Controller controller;

    private File(){}

    public static File getInstance(){
        return file;
    }

    public void setController(Controller controller) {
        this.controller = controller;
    }

    public static void transferFile(String _fileName, int _userId){
        try {
            FileRegion region = new DefaultFileRegion(Paths.get(CLIENT_ROOT_FOLDER, _fileName).toFile(), 0, Files.size(Paths.get(CLIENT_ROOT_FOLDER, _fileName)));
            Network.getInstance().writeFileHeader(Paths.get(CLIENT_ROOT_FOLDER, _fileName));
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
            fileListByte = new byte[fileListLength];
        }

        fileListByte[(int) _position] = _byte;
    }

    public void converFIleList(){
        controller.getServerFiles().getItems().clear();
        Arrays.stream(new String(fileListByte)
                .split(" "))
                .forEach(o -> controller.getServerFiles().getItems().add(o));

        fileListByte = null;
    }

    public static int getFileListLength() {
        return fileListLength;
    }

    public static void setFileListLength(int fileListLength) {
        File.fileListLength = fileListLength;
    }
}
