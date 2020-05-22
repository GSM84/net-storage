package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.CountDownLatch;

public class Controller implements Initializable {
    private static final int    PORT        = 8190;
    private static final String HOST        = "127.0.0.1";

    private static final byte   FILE_LIST   = 31;

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
        File.getInstance().setController(this);
        File.loadFileList(clientFiles);

        try {
            CountDownLatch networkStarter = new CountDownLatch(1);
            new Thread(() -> Network.getInstance().start(networkStarter, HOST, PORT)).start();
            networkStarter.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void authUser(){
        System.out.println(String.format("User login: %s, password: %s", loginField.getText(), passField.getText()).toString());

        if (Network.getInstance().authorizeUser(loginField.getText(), passField.getText())) {
            Network.getInstance().getFileList();
            authBox.setVisible(false);
            clientBox.setVisible(true);
            serverBox.setDisable(false);
        }
    }

    public void uploadFile(ActionEvent event) {
        if (clientFiles.getSelectionModel().getSelectedItems().isEmpty()){
            return;
        }
        String fileName = clientFiles.getSelectionModel().getSelectedItem().toString();
        File.transferFile(fileName, 1);
    }

    public ListView getServerFiles() {
        return serverFiles;
    }


}
