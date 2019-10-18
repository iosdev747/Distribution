package scene;

import dao.DAO;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;

/**
 * contains all event handling functions required in every controller file
 */
@Slf4j
public abstract class SceneManager {

    public static volatile boolean running = false;

    public void changeScene(Event event, String scene) {
        try {
            FXMLLoader loader = new FXMLLoader(new File(scene).toURI().toURL());
            Parent home_parent = loader.load();
            Scene Home = new Scene(home_parent);
            running = false;
            Stage window = (Stage) ((Node) event.getSource()).getScene().getWindow();
            window.setScene(Home);
            window.show();
            DAO.controller = loader.getController();
        } catch (IOException e) {
            log.error("Error while changing scene.");
            e.printStackTrace();
        }
    }

}