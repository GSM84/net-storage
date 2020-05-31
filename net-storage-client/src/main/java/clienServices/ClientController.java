package clienServices;

import dictionaryes.Dictionary;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javax.swing.*;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.CountDownLatch;

public class ClientController implements Initializable {
    private static final int    PORT        = 8190;
    private static final String HOST        = "127.0.0.1";

    @FXML
    private Button        uploadFile;

    @FXML
    private ListView      clientFiles;

    @FXML
    private ListView      serverFiles;

    @FXML
    private Button singIn;

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
        try {
            CountDownLatch networkStarter = new CountDownLatch(1);
            new Thread(() -> Network.getInstance().start(networkStarter, HOST, PORT)).start();
            networkStarter.await();

            // set client file list callback
            Network.getInstance().setClientFileListCallBack(() -> {
                ClientFileService.refreshClientFileList(clientFiles);
            });

            // set server file list callback
            Network.getInstance().serServerFileListCallBack(() -> {
                return serverFiles;
            });

            // set auth callback
            Network.getInstance().setAuthCallBack(() ->{
                Network.getInstance().getServerFileList();
                authBox.setVisible(false);
                clientBox.setVisible(true);
                serverBox.setDisable(false);
            });

            // set alert callback
            Network.getInstance().setAlertCallBack(() -> {
                JOptionPane.showMessageDialog(null,
                        "Incorrect login/password.",
                        "Unsuccessful authorization.",
                        JOptionPane.ERROR_MESSAGE);
            });

            // refresh client file list
            ClientFileService.refreshClientFileList(clientFiles);

        } catch (InterruptedException e) {
            Network.getInstance().stop();
            e.printStackTrace();
        }
    }

    @FXML
    private void singInUser(){
        System.out.println(String.format("User login: %s, password: %s", loginField.getText(), passField.getText()));
        Network.getInstance().authorizeUser(loginField.getText(), passField.getText());
    }

    @FXML
    private void singUpUser(){
        System.out.println("SingUP");
        Network.getInstance().registerUser(loginField.getText(), passField.getText());
    }

    public void uploadFile(ActionEvent event) {
        if (clientFiles.getSelectionModel().getSelectedItems().isEmpty()){
            return;
        }
        String fileName = clientFiles.getSelectionModel().getSelectedItem().toString();
        ClientFileService.transferFileToServer(fileName);
    }

    public void getFileFromServer(ActionEvent event){
        if (serverFiles.getSelectionModel().getSelectedItems().isEmpty()){
            return;
        }
        String fileName = serverFiles.getSelectionModel().getSelectedItem().toString();
        // send file request
        Network.getInstance().sendFileHeader(Dictionary.GET_FILE_FROM_SERVER, fileName);
    }

    public ListView getServerFiles() {
        return serverFiles;
    }

    public ListView getClientFiles() {
        return clientFiles;
    }
}
