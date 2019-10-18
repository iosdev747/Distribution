package client;

import container.OS;
import dao.ClientConfig;
import dao.DAO;
import fxml.MainController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        String path = "";
        String property = System.getProperty("os.name");
        String home = System.getProperty("user.home");
        if (property.toLowerCase().contains("Linux".toLowerCase())) {
            path = home + "/Downloads";
        } else if (property.toLowerCase().contains("Windows".toLowerCase())) {
            path = home + "\\Downloads";
        } else {
            path = home;
        }
        ClientConfig.HttpPort = 8080;
        Runtime.getRuntime().exec("python -m http.server " + ClientConfig.HttpPort + " -d " + path);
        Client.getClient();
        ClientConfig.path = path;
        FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("Login.fxml"));
        Parent root = loader.load();
        primaryStage.setTitle("This3Bhushan");
        primaryStage.setScene(new Scene(root, 1280, 720));
//        primaryStage.initStyle(StageStyle.UNDECORATED);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
