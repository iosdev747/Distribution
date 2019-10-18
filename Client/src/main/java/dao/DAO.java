package dao;

import client.Client;
import client.ClientData;
import container.Torrent;
import downloadmanager.DownloadManager;
import fxml.MainController;

/**
 * store all necessary static data which is required throughout application
 */
public class DAO {
    public static ClientData clientData;
    public static Client client;
    public static String configurationLocation = "Config.ser";
    public volatile static boolean status = false;
    public static String projectName = "This3Bhushan";
    public static String sceneLocation = "src/main/java/scene/";
    public static String landingPage = sceneLocation + "LoginPage.fxml";
    public static String homePage = sceneLocation + "HomePage.fxml";
    public static int projectWidth = 600;
    public static int projectHeight = 400;
    public static int defaultPort = 8080;
    public static Torrent selectedTorrent;
    public static MainController controller;
    public static DownloadManager selectedDownloadManager;
    public static int chunkSize = 1024 * 1024;
}
