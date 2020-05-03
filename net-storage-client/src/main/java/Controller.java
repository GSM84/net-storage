import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;

import java.io.IOException;
import java.net.URL;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ResourceBundle;
import java.util.TreeSet;

public class Controller implements Initializable {
    private static final String CLIENT_ROOT_FOLDER = "C:\\GITHUB\\test\\src";
    private static final String SERVER_BASE_FOLDER  = "Storage";

    @FXML
    private Button uploadFile;

    @FXML
    private ListView clientFiles;

    @FXML
    private ListView serverFiles;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        clientFiles.setItems(loadClientFilesList());
        serverFiles.setItems(loadServerFilesList(1));

    }

    public void uploadFile(ActionEvent event) {
        System.out.println("Button Clicked!");

    }

    private ObservableList<String> loadClientFilesList(){
        return FXCollections.observableArrayList(loadFileList(Paths.get(CLIENT_ROOT_FOLDER)));
    }

    private ObservableList<String> loadServerFilesList(int _userId){
        return FXCollections.observableArrayList(loadFileList(Paths.get(SERVER_BASE_FOLDER, String.valueOf(_userId))));
    }

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

}
