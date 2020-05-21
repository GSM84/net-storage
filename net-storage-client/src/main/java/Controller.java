import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;


import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ResourceBundle;
import java.util.TreeSet;

public class Controller implements Initializable {
    private static final String CLIENT_ROOT_FOLDER = "C:\\GITHUB\\test\\src";
    //private static final String SERVER_BASE_FOLDER  = "Storage";

    private static final int    BUFFER_SIZE = 1048576;
    private static final int    PORT        = 8190;
    private static final byte[] IP          = new byte[]{127, 0, 0, 1};

    @FXML
    private Button        uploadFile;

    @FXML
    private ListView      clientFiles;

    @FXML
    private ListView      serverFiles;

    @FXML
    private Button        sendCredential;

    @FXML
    private VBox          clientBox;

    @FXML
    private VBox          serverBox;

    @FXML
    private VBox          authBox;

    @FXML
    private TextField     loginField;

    @FXML
    private PasswordField passField;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        clientFiles.setItems(loadClientFilesList(1));
        Network.start(IP, PORT);
        Thread readThread = new Thread(() ->{
            try{
                while (true) {
                    System.out.println("listening");
                    System.out.println(Network.getInputStream().available());
                    System.out.println((char) Network.getInputStream().readByte());
                    System.out.println("read after");
                }
            } catch (IOException e){
                e.printStackTrace();
            } finally {
                Network.stop();
            }
        });
        readThread.setDaemon(true);
        readThread.start();
    }

    public void uploadFile(ActionEvent event) {
        if (clientFiles.getSelectionModel().getSelectedItems().isEmpty()){
            return;
        }
        System.out.println(clientFiles.getSelectionModel().getSelectedItem().toString());
        transferFile(1, clientFiles.getSelectionModel().getSelectedItem().toString());
        //serverFiles.setItems(loadServerFilesList(1));
    }

    private ObservableList<String> loadClientFilesList(int _userId){
        return FXCollections.observableArrayList(loadFileList(Paths.get(CLIENT_ROOT_FOLDER, String.valueOf(_userId))));
    }

//    private ObservableList<String> loadServerFilesList(int _userId){
//        return FXCollections.observableArrayList(loadFileList(Paths.get(SERVER_BASE_FOLDER, String.valueOf(_userId))));
//    }

    private TreeSet<String> loadFileList(Path _path){
        TreeSet<String> fileSet   = new TreeSet<>();
        try {
            Files.walkFileTree(_path, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    fileSet.add(file.getFileName().toString());
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

        return fileSet;
    }

    @FXML
    private void authUser(){
        System.out.println(String.format("User login: %s, password: %s", loginField.getText(), passField.getText()).toString());

        if (Network.authorizeUser(loginField.getText(), passField.getText())) {
            authBox.setVisible(false);
            clientBox.setVisible(true);
            serverBox.setDisable(false);
        }
    }

    private void transferFile(int _userId, String _fileName){
        try {
            RandomAccessFile src        = new RandomAccessFile(Paths.get(CLIENT_ROOT_FOLDER, String.valueOf(_userId), _fileName).toFile(), "r");
            FileChannel      srcChannel = src.getChannel();
            // signal
            Network.writeSignalByte((byte)28);
            // file name
            Network.writeShorMessage(_fileName);
            //file size
            Network.getOutputStream().writeLong(Files.size(Paths.get(CLIENT_ROOT_FOLDER, String.valueOf(_userId), _fileName)));

            ByteBuffer buff = ByteBuffer.allocate(BUFFER_SIZE);
            int readed = srcChannel.read(buff);
            while (readed > 0){
                Network.getOutputStream().write(buff.array(), 0, buff.position());
                Network.getOutputStream().flush();
                buff.clear();
                readed = srcChannel.read(buff);
            }
            srcChannel.close();
            src.close();
        } catch (IOException e) {
            System.out.println("File transfer comlite.");
            e.printStackTrace();
        }
    }
}
