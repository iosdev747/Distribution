package fxml;

import client.Client;
import com.jfoenix.controls.JFXDatePicker;
import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.controls.JFXTextField;
import container.User;
import dao.ClientConfig;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.Pane;
import lombok.extern.slf4j.Slf4j;
import scene.SceneManager;
import utils.FileHash;

import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.time.LocalDate;
import java.util.ResourceBundle;

@Slf4j
public class LoginController extends SceneManager implements Initializable {

    private boolean loginVisible;

    @FXML
    private Pane loginPane;

    @FXML
    private JFXTextField usernameTextField;

    @FXML
    private JFXPasswordField passwordField;

    @FXML
    private Pane registerPane;

    @FXML
    private JFXTextField registerUsernameTextField;

    @FXML
    private JFXTextField registerEmailTextField;

    @FXML
    private JFXTextField registerNameTextField;

    @FXML
    private JFXDatePicker dobDatePicker;

    @FXML
    private JFXPasswordField registerPasswordField;

    @FXML
    void login(ActionEvent event) {
        if (!loginVisible) {
            loginVisible = true;
            refresh();
            return;
        }
        if (usernameTextField.getText().isEmpty() || passwordField.getText().isEmpty())
            return;
        log.info("Email: {}, passHash: {}", usernameTextField.getText(), FileHash.hash(passwordField.getText()));
//        if (Client.getClient().authUser(usernameTextField.getText(), FileHash.hash(passwordField.getText()))) {
            ClientConfig.getConfig().email = usernameTextField.getText();
            ClientConfig.getConfig().password = FileHash.hash(passwordField.getText());
            String hostname = "Unknown";
            try {
                ClientConfig.getConfig().userName = System.getenv("");
                InetAddress.getLocalHost().getHostName();
            } catch (UnknownHostException ex) {
                System.out.println("Hostname can not be resolved");
                ClientConfig.getConfig().userName = "System";
            }
            changeScene(event, "Client/src/main/resources/Main.fxml");
//        }
    }

    @FXML
    void register(ActionEvent event) {
        if (loginVisible) {
            loginVisible = false;
            refresh();
            return;
        }
        if (registerNameTextField.getText().isEmpty() || registerUsernameTextField.getText().isEmpty() || registerEmailTextField.getText().isEmpty() || registerPasswordField.getText().isEmpty()) {
            return;
        }
        User user = new User();
        user.setName(registerNameTextField.getText());
        user.setEmail(registerEmailTextField.getText());
        user.setUsrname(registerUsernameTextField.getText());
        user.setPassHash(FileHash.hash(passwordField.getText()));
        LocalDate date = dobDatePicker.getValue();
        log.info("date : {}", String.valueOf(date));
        user.setDOB(String.valueOf(date));
//todo date picker
        Client.getClient().registerUser(user);

    }

    void refresh() {
        loginPane.setVisible(loginVisible);
        registerPane.setVisible(!loginVisible);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loginVisible = true;
        refresh();
    }
}
