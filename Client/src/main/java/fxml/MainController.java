package fxml;

import client.Client;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.jfoenix.controls.*;
import container.DownloadState;
import container.OwlLink;
import container.Packet;
import container.Torrent;
import container.call.GetStrings;
import container.call.SearchQuery;
import container.call.TorrentArrayTemplate;
import dao.AdminServerConfig;
import dao.ClientConfig;
import dao.DAO;
import downloadmanager.DownloadManager;
import filemanager.ChunkManager;
import filemanager.FileManager;
import javafx.beans.InvalidationListener;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Bounds;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.input.MouseDragEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import lombok.extern.slf4j.Slf4j;
import utils.FileHash;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

@Slf4j
public class MainController implements Initializable {
    @FXML
    private JFXButton uploadButton;
    @FXML
    private JFXButton addFileButton;
    @FXML
    private JFXButton downloadButton;
    @FXML
    private JFXButton cancelButton;
    @FXML
    private JFXButton searchButton;
    @FXML
    private JFXButton searchPaneButton;
    @FXML
    private JFXButton okButton;
    @FXML
    private JFXButton goButton;
    @FXML
    private JFXButton playButton;
    @FXML
    private JFXButton pauseButton;
    @FXML
    private JFXButton dashboardButton;
    boolean[] chunkDownloaded;
    @FXML
    private JFXButton videoPlayButton;
    @FXML
    private JFXButton videoPaneButton;
    @FXML
    private JFXButton commentButton;
    @FXML
    private JFXTextField typeTextField;
    @FXML
    private JFXTextField searchTextField;
    @FXML
    private JFXTextField fileTypeTextField;
    @FXML
    private JFXTextField fileHashTextField;
    @FXML
    private JFXTextField commentFiled;
    @FXML
    private JFXChipView tagsChipView;
    @FXML
    private JFXChipView insideFileTypesChipView;
    @FXML
    private JFXChipView insidefileSearchChipView;
    @FXML
    private JFXChipView tagsSearchChipView;
    @FXML
    private Pane uploadPane;
    @FXML
    private Pane downloadPane;
    @FXML
    private Pane searchPane;
    @FXML
    private Pane dashboardPane;
    @FXML
    private Pane torrentInfoPane;
    @FXML
    private Pane searchTorrentInfoPane;
    @FXML
    private Pane searchCommentPane;
    @FXML
    private javafx.scene.control.Label fileselectedLabel;
    @FXML
    private Pane videoPane;
    @FXML
    private JFXListView<DownloadManager> listView;
    @FXML
    private JFXListView<Torrent> torrentSearchListView;
    @FXML
    private JFXListView<String> myFileList;
    @FXML
    private JFXListView<Object> searchenginesListView;
    @FXML
    private JFXListView<String> torrentinsideFileTypeList;
    @FXML
    private JFXListView<String> torrenttagsList;
    @FXML
    private JFXListView<String> torrentchunksList;
    @FXML
    private JFXListView<String> InsideFileType;
    @FXML
    private JFXListView<String> Tags;
    @FXML
    private JFXListView<String> commentsListView;
    @FXML
    private Pane controls;
    @FXML
    private Label downloadsLabel;
    @FXML
    private Label torrentName;
    @FXML
    private Label torrentHash;
    @FXML
    private Label torrentCreatedOn;
    @FXML
    private Label torrentSize;
    @FXML
    private Label emailLabel;
    @FXML
    private Label torrentNumberOfChunks;
    @FXML
    private Label searchHashLabel;
    @FXML
    private Label searchNoOfOwlLinksLabel;
    @FXML
    private Label searchNameLabel;
    @FXML
    private Label searchTypeLabel;
    @FXML
    private Label searchAuthorLabel;
    @FXML
    private Label searchCreatedOnLabel;
    @FXML
    private Label searchSizeLabel;
    @FXML
    private Label searchNoOfChunksLabel;
    @FXML
    private JFXCheckBox nameCheckBox;
    @FXML
    private JFXCheckBox typeCheckBox;
    @FXML
    private JFXCheckBox insideFileCheckBox;
    @FXML
    private JFXCheckBox tagsCheckBox;
    @FXML
    private JFXCheckBox sizeCheckBox;
    @FXML
    private JFXCheckBox fileHashCheckBox;
    @FXML
    private JFXCheckBox isAnonymous;
    @FXML
    private JFXSlider sizeSlider;
    @FXML
    private JFXListView<Object> searchenginesListView1;
    @FXML
    private MediaView mediaView;
    @FXML
    private JFXButton play;
    @FXML
    private JFXButton stop;
    @FXML
    private Slider volumeSlider;
    @FXML
    public Label sabKaLabel;
    @FXML
    private JFXButton recommendButton;

    File file;
    @FXML
    private Slider timeSlider;
    private MediaPlayer mediaPlayer;

    public void updateListView() {
        log.debug("list updated");
        List<DownloadManager> downloadManagers = Client.getClient().clientData.downloadManagers;
        listView.getItems().clear();
        downloadManagers.forEach(downloadManager -> {
            listView.getItems().add(downloadManager);
        });
    }

    public void recommendAction(ActionEvent actionEvent) {
        sabKaLabel.setText("Fetching recommendation");
        downloadPane.setVisible(false);
        uploadPane.setVisible(false);
        searchPane.setVisible(true);
        dashboardPane.setVisible(false);
        videoPane.setVisible(false);
        List<Torrent> torrentList = new ArrayList<>();
        Packet requestPacket = new Packet();
        requestPacket.call = "SearchEngine.getSuggestions";
        Gson gson = new Gson();
        Torrent[] _torrents = Client.getClient().getHistory();
        TorrentArrayTemplate template = new TorrentArrayTemplate();
        template.setTorrents(_torrents);
        requestPacket.data = gson.toJson(template, TorrentArrayTemplate.class);
        String requestJson = gson.toJson(requestPacket, Packet.class);
        String[] searchEngines = Client.getClient().getSearchEngines();
        if (searchEngines == null || searchEngines.length == 0) {
            sabKaLabel.setText("No search engine found :(");
            return;
        }
        log.info("calling SearchEngine.getSuggestions with json:{}", requestJson);
        for (String searchEngine : searchEngines) {
            String response = null;
            try {
//                log.error("{}", searchEngine);
                response = Client.getClient().netManager.send(requestJson, searchEngine.split("\\^")[1], Integer.parseInt(searchEngine.split("\\^")[2]));
                Packet responsePacket = gson.fromJson(response, Packet.class);
                log.info("received response: {}", response);
                TorrentArrayTemplate responseResult = gson.fromJson(responsePacket.data, TorrentArrayTemplate.class);
                if (responseResult != null) {
                    torrentSearchListView.getItems().clear();
                    sabKaLabel.setText("Updating recommendation");
                    for (Torrent torrent : responseResult.torrents) {
                        torrentSearchListView.getItems().add(torrent);
                    }
                } else {
                    sabKaLabel.setText("No recommendation for you.");
                }
            } catch (IOException e) {
                log.error("Unable to find from server: {}", searchEngine);
                e.printStackTrace();
            }
        }
    }

    // TODO: 04-09-2019 add last used path
    private volatile int chunkNeeded;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
//        File file = new File("\"C:/Users/iOSDev747/IdeaProjects/This3Bhushan/This3Bhushan/Client/src/main/resources/profile.jpg\"")
//        photoCircle.setFill(new ImagePattern(new Image(file m)));
        sabKaLabel.setText("Feeding ducks");
        torrentSearchListView.setOnMouseClicked(e -> {
            if (torrentSearchListView.getSelectionModel().getSelectedItem() == null) {
                return;
            }
            if (DAO.selectedTorrent == (Torrent) torrentSearchListView.getSelectionModel().getSelectedItem()) {
                return;
            }
            DAO.selectedTorrent = (Torrent) torrentSearchListView.getSelectionModel().getSelectedItem();
            String[] searchEngines = Client.getClient().getSearchEngines();
            if (searchEngines == null)
                return;
            String[] comments = getComment(DAO.selectedTorrent, searchEngines);
            if (comments != null) {
                commentsListView.getItems().clear();
                for (String comment : comments) {
                    commentsListView.getItems().add(comment);
                }
            }
            searchHashLabel.setText(DAO.selectedTorrent.getFileHash());
            searchNoOfOwlLinksLabel.setText(String.valueOf(DAO.selectedTorrent.getOwlLinks().length));
            searchNameLabel.setText(DAO.selectedTorrent.getFileName());
            searchTypeLabel.setText(DAO.selectedTorrent.getFileType());
            searchAuthorLabel.setText(DAO.selectedTorrent.getAuthor());
            searchCreatedOnLabel.setText(DAO.selectedTorrent.getCreatedOn());
            searchSizeLabel.setText(String.valueOf(DAO.selectedTorrent.getSize()));
            searchNoOfChunksLabel.setText(String.valueOf(DAO.selectedTorrent.getNumberOfChunks()));
            InsideFileType.getItems().clear();
            Tags.getItems().clear();
            for (String type : DAO.selectedTorrent.getInsideFileTypes()) {
                InsideFileType.getItems().add(type);
            }
            for (String tag : DAO.selectedTorrent.getTags()) {
                Tags.getItems().add(tag);
            }
        });
        listView.setOnMouseClicked(e -> {
            DAO.selectedDownloadManager = (DownloadManager) listView.getSelectionModel().getSelectedItem();
        });
        myFileList.setOnMouseClicked(e -> {
            String fileHash = myFileList.getSelectionModel().getSelectedItem();
            if (fileHash == null || fileHash.length() == 0 || fileHash.equals("Currently you havn't uploaded any file.") || fileHash.equals("Please upload useful files to help community.")) {
                torrentInfoPane.setVisible(false);
                return;
            }
            Torrent torrent = null;
            boolean notNull = false;
            String[] searchEngines = Client.getClient().getSearchEngines();
            int downloads = 0;
            for (String searchEngine : searchEngines) {
                Gson gson = new Gson();
                Packet requestPacket = new Packet();
                requestPacket.call = "SearchEngine.getDownloads";
                requestPacket.data = gson.toJson(fileHash.split("\\^")[1]);
                String requestJson = gson.toJson(requestPacket);
                log.info("calling SearchEngine.getDownloads with request: {}", requestJson);
                String name = searchEngine.split("\\^")[0];
                String ip = searchEngine.split("\\^")[1];
                int port = Integer.parseInt(searchEngine.split("\\^")[2]);
                log.info("SearchEngine name: {}, Ip: {}, port: {}", name, ip, port);
                try {
                    String response = Client.getClient().netManager.send(requestJson, ip, port);
                    Packet responsePacket = gson.fromJson(response, Packet.class);
                    log.info("received response: {}", response);
                    Integer result = gson.fromJson(responsePacket.data, Integer.class);
                    if (result != null) {
                        notNull = true;
                        downloads += result;
                    }
                } catch (IOException ex) {
                    log.error("unable to resolve search engine: {}", searchEngine);
                    ex.printStackTrace();
                }
            }
            if (!notNull) {
                return;
            }
            for (String searchEngine : searchEngines) {
                Gson gson = new Gson();
                Packet requestPacket = new Packet();
                requestPacket.call = "SearchEngine.searchByFileName";
                SearchQuery searchQuery = new SearchQuery(new boolean[]{false, false, false, false, false, true}, "", "", new String[]{}, new String[]{}, 0, fileHash.split("\\^")[1]);
                requestPacket.data = gson.toJson(searchQuery);
                String requestJson = gson.toJson(requestPacket);
                log.info("calling SearchEngine.searchByFileName with request: {}", requestJson);
                String name = searchEngine.split("\\^")[0];
                String ip = searchEngine.split("\\^")[1];
                int port = Integer.parseInt(searchEngine.split("\\^")[2]);
                log.info("SearchEngine name: {}, Ip: {}, port: {}", name, ip, port);
                try {
                    String response = Client.getClient().netManager.send(requestJson, ip, port);
                    Packet responsePacket = gson.fromJson(response, Packet.class);
                    log.info("received response: {}", response);
                    TorrentArrayTemplate torrents = gson.fromJson(responsePacket.data, TorrentArrayTemplate.class);
                    if (torrents.getTorrents().length == 0) {
                        torrent = null;
                    } else {
                        torrent = torrents.getTorrents()[0];
                    }
                } catch (IOException ex) {
                    log.error("unable to resolve search engine: {}", searchEngine);
                    ex.printStackTrace();
                }
            }
            if (torrent != null) {
                torrentInfoPane.setVisible(true);
                torrentName.setText(torrent.getFileName());
                torrentHash.setText(torrent.getFileHash());
                torrentCreatedOn.setText(torrent.getCreatedOn());
                torrentSize.setText(String.valueOf(torrent.getSize()));
                torrentNumberOfChunks.setText(String.valueOf(torrent.getNumberOfChunks()));
                downloadsLabel.setText(String.valueOf(downloads));
                torrenttagsList.getItems().clear();
                torrentchunksList.getItems().clear();
                torrentinsideFileTypeList.getItems().clear();
                for (String tag : torrent.getTags()) {
                    torrenttagsList.getItems().add(tag);
                }
                for (String chunk : torrent.getChunkHashes()) {
                    torrentchunksList.getItems().add(chunk);
                }
                for (String insideFileType : torrent.getInsideFileTypes()) {
                    torrentinsideFileTypeList.getItems().add(insideFileType);
                }
            } else {
                torrentInfoPane.setVisible(false);
            }
        });
        nameCheckBox.setOnMouseClicked(e -> {
            if (nameCheckBox.isSelected()) {
                fileHashCheckBox.setSelected(false);
            }
        });
        typeCheckBox.setOnMouseClicked(e -> {
            if (typeCheckBox.isSelected()) {
                fileHashCheckBox.setSelected(false);
            }
        });
        insideFileCheckBox.setOnMouseClicked(e -> {
            if (insideFileCheckBox.isSelected()) {
                fileHashCheckBox.setSelected(false);
            }
        });
        tagsCheckBox.setOnMouseClicked(e -> {
            if (tagsCheckBox.isSelected()) {
                fileHashCheckBox.setSelected(false);
            }
        });
        sizeCheckBox.setOnMouseClicked(e -> {
            if (sizeCheckBox.isSelected()) {
                fileHashCheckBox.setSelected(false);
            }
        });
        fileHashCheckBox.setOnMouseClicked(e -> {
            if (fileHashCheckBox.isSelected()) {
                nameCheckBox.setSelected(false);
                typeCheckBox.setSelected(false);
                insideFileCheckBox.setSelected(false);
                tagsCheckBox.setSelected(false);
                sizeCheckBox.setSelected(false);
            }
        });
    }

    public void downloadAction(ActionEvent actionEvent) {
        downloadPane.setVisible(true);
        uploadPane.setVisible(false);
        searchPane.setVisible(false);
        dashboardPane.setVisible(false);
        videoPane.setVisible(false);
    }

    public void uploadAction(ActionEvent event) {
        downloadPane.setVisible(false);
        uploadPane.setVisible(true);
        searchPane.setVisible(false);
        dashboardPane.setVisible(false);
        videoPane.setVisible(false);
        log.info("gettign searchengines");
        String[] searchEngines = Client.getClient().getSearchEngines();
        log.info("got searchengines : {}", searchEngines.length);
        searchenginesListView1.getItems().clear();
        for (String searchEngine : searchEngines) {
            log.info("..");
            JFXCheckBox checkBox = new JFXCheckBox(searchEngine.split("\\^")[0] + "        IP:" + searchEngine.split("\\^")[1] + "    Port:" + searchEngine.split("\\^")[2]);
            searchenginesListView1.getItems().add(checkBox);
        }
    }

    public void searchPaneButtonAction(ActionEvent actionEvent) {
        downloadPane.setVisible(false);
        uploadPane.setVisible(false);
        searchPane.setVisible(true);
        dashboardPane.setVisible(false);
        videoPane.setVisible(false);
        String[] searchEngines = Client.getClient().getSearchEngines();
        if (searchEngines != null) {
            searchenginesListView.getItems().clear();
            for (String searchEngine : searchEngines) {
                JFXCheckBox checkBox = new JFXCheckBox(searchEngine);
                searchenginesListView.getItems().add(checkBox);
            }
        }
    }

    public void playAction(ActionEvent actionEvent) {
        if (DAO.selectedDownloadManager != null) {
            DAO.selectedDownloadManager.play();
            DAO.selectedDownloadManager = null;
        }
    }

    public void pauseAction(ActionEvent actionEvent) {
        if (DAO.selectedDownloadManager != null) {
            DAO.selectedDownloadManager.pause();
            DAO.selectedDownloadManager = null;
        }
    }

    public void dashboardButtonAction(ActionEvent actionEvent) {
        downloadPane.setVisible(false);
        uploadPane.setVisible(false);
        searchPane.setVisible(false);
        dashboardPane.setVisible(true);
        videoPane.setVisible(false);
        fetchUserFiles(actionEvent);
    }

    public void videoAction(ActionEvent actionEvent) {
        downloadPane.setVisible(false);
        uploadPane.setVisible(false);
        searchPane.setVisible(false);
        dashboardPane.setVisible(false);
        videoPane.setVisible(true);
    }

    public void showcontrolsAction(MouseEvent event) {
        log.info("shown");
        controls.setVisible(true);
    }

    public void hidecontrolsAction(MouseEvent event) {
        log.info("hidden");
        controls.setVisible(true);
    }

    public void setPlayer(Torrent torrent, int attempts) {
        if (torrent == null || attempts == 0) {
            return;
        }

        String filePath = "http://localhost:8080/playVideo.mp4";
        log.info("Path: {}", filePath);

        Media media = new Media(filePath);
        mediaPlayer = new MediaPlayer(media);
        mediaView.setMediaPlayer(mediaPlayer);
        mediaPlayer.play();
        mediaPlayer.setOnError(() -> {
            log.error("unable to read file");
            setPlayer(torrent, attempts - 1);
        });
        InvalidationListener resizeMediaView = observable -> {
            mediaView.setFitWidth(videoPane.getWidth());
            mediaView.setFitHeight(videoPane.getHeight());
            Bounds actualVideoSize = mediaView.getLayoutBounds();
            mediaView.setX(((mediaView.getFitWidth() - actualVideoSize.getWidth()) / 2) - 6);
            mediaView.setY((mediaView.getFitHeight() - actualVideoSize.getHeight()) / 2);
        };
        videoPane.heightProperty().addListener(resizeMediaView);
        videoPane.widthProperty().addListener(resizeMediaView);

        volumeSlider.setValue(mediaPlayer.getVolume() * 100);
        volumeSlider.valueProperty().addListener(observable -> {
            mediaPlayer.setVolume(volumeSlider.getValue() / 100);
            log.info("invalidated");
        });

        mediaPlayer.currentTimeProperty().addListener((observable, oldValue, newValue) -> {
            timeSlider.setValue(newValue.toSeconds());
            timeSlider.setMax(mediaPlayer.getTotalDuration().toSeconds());
            chunkNeeded = (int) (torrent.getNumberOfChunks() * (timeSlider.getValue() / timeSlider.getMax()));
            log.info("changed");
            log.info("time: {}, sound: {}", timeSlider.getValue(), volumeSlider.getValue());
        });

        timeSlider.setOnMouseClicked(event -> {
            mediaPlayer.seek(Duration.seconds(timeSlider.getValue()));
            log.info("handle");
        });

        timeSlider.setOnMouseDragEntered(event -> {
            mediaPlayer.seek(Duration.seconds(timeSlider.getValue()));
            log.info("mouse drag changed");
        });

    }

    public String[] getComment(Torrent selectedTorrent, String[] searchEngines) {
        Packet requestPacket = new Packet();
        Gson gson = new Gson();
        requestPacket.call = "SearchEngine.getComments";
        requestPacket.data = gson.toJson(selectedTorrent, Torrent.class);
        String requestJson = gson.toJson(requestPacket, Packet.class);
        log.info("requesting SearchEngine.getComments with json :{}", requestJson);
        for (String searchEngine : searchEngines) {
            try {
                String ip = searchEngine.split("\\^")[1];
                int port = Integer.parseInt(searchEngine.split("\\^")[2]);
                log.info("ip: {}, port: {}", ip, port);
                String responseJson = Client.getClient().netManager.send(requestJson, ip, port);
                Packet responsePacket = gson.fromJson(responseJson, Packet.class);
                GetStrings strings = gson.fromJson(responsePacket.data, GetStrings.class);
                if (strings == null) {
                    return null;
                }
                return strings.getStrings();
            } catch (IOException e) {
                log.error("unable to call AdminServer.getMyFileChunks");
                e.printStackTrace();
            }
        }
        return null;
    }

    @FXML
    public void doComment(ActionEvent actionEvent) {
        if (commentFiled.getText().isEmpty() || commentFiled.getText().length() == 0 || DAO.selectedTorrent == null) {
            return;
        }
        String[] searchEngines = Client.getClient().getSearchEngines();
        if (searchEngines == null) {
            return;
        }
        Client.getClient().comment(commentFiled.getText(), DAO.selectedTorrent, isAnonymous.isSelected(), searchEngines);
        String[] comments = getComment(DAO.selectedTorrent, searchEngines);
        if (comments != null) {
            commentsListView.getItems().clear();
            for (String comment : comments) {
                commentsListView.getItems().add(comment);
            }
        }
    }

    @FXML
    void PAUSE(ActionEvent event) {
        mediaPlayer.pause();
    }

    @FXML
    void PLAY(ActionEvent event) {
        mediaPlayer.play();
    }

    @FXML
    void STOP(ActionEvent event) {
        mediaPlayer.stop();
    }

    public void okButtonAction(ActionEvent actionEvent) {
        if (DAO.selectedTorrent == null) {
            return;
        }
        container.File file = new container.File(DAO.selectedTorrent.getFileName(), DAO.selectedTorrent, DownloadState.DOWNLOADING);
        log.info("file made: {}", file);
        sabKaLabel.setText("Downloading torrent: " + file.getTorrentFile().getFileName() + " of size: " + file.getTorrentFile().getSize());
        Client.getClient().download(file);
        Client.getClient().clientData.history.add(DAO.selectedTorrent);
        Client.getClient().saveHistory();
        DAO.selectedTorrent = null;
    }

    public void addFileAction(ActionEvent actionEvent) {
//            Thread thread = new Thread(() -> file = chooseFile(event));
//            thread.start();
        String path;
        String property = System.getProperty("os.name");
        String home = System.getProperty("user.home");
        if ("Linux".equals(property)) {
            path = home + "/Downloads";
        } else if ("Windows".equals(property)) {
            path = home + "\\Downloads";
        } else {
            path = home;
        }
        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(new File(path));
        file = fileChooser.showOpenDialog(null);
//            if (Thread.currentThread() != thread) {
//                searchEngines = {"sample1^1234", "sample2^9876", "sample3^8656"};
//            }
//            thread.join();
        if (file == null) {
            log.info("no file selected");
            fileselectedLabel.setText("No File Selected");
            //no file selected
            return;
        }
        fileselectedLabel.setText(file.getName());
        log.info("selected file: {}", file.getName());
    }

    public void goAction(ActionEvent actionEvent) {
        try {
            //// TODO: 20-09-2019 exception handling for wrong input
            sabKaLabel.setText("Uploading your file");
            byte[] b = null;
            String[] searchEngines;
            List<String> searchEngineList = new ArrayList<>();
            Iterator iterator = searchenginesListView1.getItems().iterator();
            while (iterator.hasNext()) {
                JFXCheckBox checkBox = (JFXCheckBox) iterator.next();
                if (checkBox.isSelected()) {
                    String s = checkBox.getText().split(" ")[0] + "^" + (checkBox.getText().split(":")[1]).split(" ")[0] + "^" + checkBox.getText().split(":")[2];
                    searchEngineList.add(s);
                    System.out.println(s);
                }
            }
            searchEngines = searchEngineList.toArray(new String[0]);
            if (searchEngines.length == 0) {
                //no search engines found
                log.error("no search engines found/selected");
                return;
            }
            log.info("got search engines: {}", searchEngines.length);
            b = Files.readAllBytes(Paths.get(file.getAbsolutePath()));
            StringBuilder stringBuilder = new StringBuilder();
            for (byte _byte : b) {
                stringBuilder.append(_byte);
            }
            String fileHash = FileHash.hash(stringBuilder.toString());
            String fileName = file.getName();
            String fileType = typeTextField.getText();
            Iterator it = tagsChipView.getChips().iterator();
            Set<String> _tags = new HashSet<String>();
            while (it.hasNext()) {
                _tags.add((String) it.next());
            }
            String[] tags = (String[]) _tags.toArray(new String[0]);
//            tags = {"some random zip", "testing zip", "idk what it is :3"};
            it = insideFileTypesChipView.getChips().iterator();
            Set<String> _insideFileType = new HashSet<String>();
            while (it.hasNext()) {
                _insideFileType.add((String) it.next());
            }
            String[] insideFileTypes = (String[]) _insideFileType.toArray(new String[0]);
//            String[] insideFileTypes = {".txt", ".xyz", ".img", ".bin"};
            String author = ClientConfig.getConfig().email;
            log.info("myID##################: {}", author);
            // TODO: 11-09-2019 createdOn date is to be added on server side
            String createdOn = "currentlyRandom Date";
            long size = file.length();
            // TODO: 12-09-2019 remove below comment before commit
            String[] chunkHashes = (String[]) ChunkManager.splitFile(file, fileHash).toArray(new String[0]);
//            String[] chunkHashes = {"h1", "h2", "h3", "h4"};
            int numberOfChunks = chunkHashes.length;
            log.info("Torrent generated fileName: {}, fileHash: {}, insideTypes:{}, tags: {}, author: {}, size: {}, numberOfChunks:{}", fileName, fileHash, insideFileTypes.length, tags.length, author, size, numberOfChunks);
            Torrent torrent = new Torrent(fileHash, fileName, fileType, tags, insideFileTypes, author, createdOn, size, numberOfChunks, chunkHashes, null);
            container.File file1 = new container.File(fileName, torrent, DownloadState.UPLOADED);
            Client.getClient().addNewFile(file1, searchEngines);
            sabKaLabel.setText("File uploaded");
//                if (Client.getClient().addNewFile(file1, searchEngines)) {
////                System.out.println("file added");
//            } else {
////                System.out.println("unable to add file");
//            }
        } catch (IOException e) {
            e.printStackTrace();
        }/* catch (InterruptedException e) {
            e.printStackTrace();
        }*/
    }

    public void searchAction(ActionEvent actionEvent) {
        boolean[] searchParams = {false, false, false, false, false, false};
        if (nameCheckBox.isSelected() && !searchTextField.getText().equals("")) {
            searchParams[0] = true;
        }
        if (typeCheckBox.isSelected() && !fileTypeTextField.getText().equals("")) {
            searchParams[1] = true;
        }
        if (insideFileCheckBox.isSelected() && !insidefileSearchChipView.getChips().isEmpty()) {
            searchParams[2] = true;
        }
        if (tagsCheckBox.isSelected() && !tagsSearchChipView.getChips().isEmpty()) {
            searchParams[3] = true;
        }
        if (sizeCheckBox.isSelected()) {
            searchParams[4] = true;
        }
        if (fileHashCheckBox.isSelected() && !fileHashTextField.getText().equals("")) {
            searchParams[5] = true;
        }
        boolean check = false;
        for (boolean bool : searchParams) {
            check |= bool;
        }
        if (!check) {
            return;
        }
        List<String> searchEngineList = new ArrayList<>();
        for (Object item : searchenginesListView.getItems()) {
            JFXCheckBox checkBox = (JFXCheckBox) item;
            if (checkBox.isSelected()) {
                searchEngineList.add(((JFXCheckBox) item).getText());
            }
        }
        String[] searchEngines = searchEngineList.toArray(new String[0]);
        log.info("got {} searchEngines", searchEngines.length);
        for (String searchEngine : searchEngines) {
            Gson gson = new Gson();
            Packet requestPacket = new Packet();
            requestPacket.call = "SearchEngine.searchByFileName";
            sabKaLabel.setText("searching for torrents... :)");
            // TODO: 11-09-2019 fill ip and port below
            SearchQuery searchQuery = new SearchQuery();
            searchQuery.setBooleans(searchParams);
            searchQuery.setFileHash(fileHashTextField.getText());
            searchQuery.setFileTypeText(fileTypeTextField.getText());
            List<String> tags = new ArrayList<>();
            for (Object o : tagsSearchChipView.getChips()) {
                tags.add(String.valueOf(o));
            }
            searchQuery.setTags(tags.toArray(new String[0]));
            List<String> insideTypes = new ArrayList<>();
            for (Object o : insidefileSearchChipView.getChips()) {
                insideTypes.add(String.valueOf(o));
            }
            searchQuery.setInsideFileTypes(insideTypes.toArray(new String[0]));
            searchQuery.setSearchText(searchTextField.getText());
            searchQuery.setSize((int) sizeSlider.getValue() * 1024 * 1024);
            requestPacket.data = gson.toJson(searchQuery);
            String requestJson = gson.toJson(requestPacket);
            log.info("calling SearchEngine.searchByFileName with request: {}", requestJson);
            String name = searchEngine.split("\\^")[0];
            String ip = searchEngine.split("\\^")[1];
            int port = Integer.parseInt(searchEngine.split("\\^")[2]);
            log.info("SearchEngine name: {}, Ip: {}, port: {}", name, ip, port);
            try {
                String response = Client.getClient().netManager.send(requestJson, ip, port);
                Packet responsePacket = gson.fromJson(response, Packet.class);
                log.info("received response: {}", response);
                TorrentArrayTemplate responseResult = gson.fromJson(responsePacket.data, TorrentArrayTemplate.class);
                if (responseResult != null) {
                    sabKaLabel.setText("Updating torrent list");
                    torrentSearchListView.getItems().clear();
                    for (Torrent torrent : responseResult.torrents) {
                        torrentSearchListView.getItems().add(torrent);
                    }
                    sabKaLabel.setText("Updated torrent list");
                }
            } catch (IOException e) {
                log.error("unable to resolve search engine: {}", searchEngine);
                e.printStackTrace();
            }
        }
//            torrentSearchListView.getSelectionModel().getSelectedItems();
//            torrentSearchListView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Object>() {
//                @Override
//                public void changed(ObservableValue<? extends Object> observable, Object oldValue, Object newValue) {
//                    System.out.println("torrentSearchListView selection changed from oldValue = "
//                            + oldValue + " to newValue = " + newValue);
//                }
//            });

    }

    public void fetchUserFiles(ActionEvent actionEvent) {
        sabKaLabel.setText("Fetching your data. Please hold on");
        myFileList.getItems().clear();
        emailLabel.setText(ClientConfig.getConfig().email);
        String[] files = Client.getClient().fetchUserFiles();
        if (files == null || files.length == 0) {
            log.error("No files found for user: {}, passwd: {}", ClientConfig.getConfig().email, ClientConfig.getConfig().password);
            myFileList.getItems().add("Currently you havn't uploaded any file.");
            myFileList.getItems().add("Please upload useful files to help community.");
            torrentInfoPane.setVisible(false);
        } else {
            torrentInfoPane.setVisible(true);
            List<String> stringList = new ArrayList<>();
            HashSet set = new HashSet(Arrays.asList(files));
            for (Object file : set) {
                myFileList.getItems().add((String) file);
            }
        }
    }

    public void playVideoAction(ActionEvent actionEvent) {
        if (DAO.selectedTorrent != null) {
            PlayVideo(DAO.selectedTorrent);
        }
    }

    byte[] downloadChunk(Torrent torrent, String chunkHash) {
        Set<String> downloadLinks = new HashSet<>();
        Torrent torrentFile = torrent;
        OwlLink[] owlLinks = torrentFile.getOwlLinks();
        for (OwlLink owlLink : owlLinks) {
            if (Client.getClient().netManager.ping(owlLink.getIp(), owlLink.getPort())) {
                try {
                    Packet requestPacket = new Packet();
                    requestPacket.call = "Owl.getDownloadLink";
                    Gson gson = new Gson();
                    requestPacket.data = gson.toJson(chunkHash, String.class);
                    String requestJson = gson.toJson(requestPacket, Packet.class);
                    log.info("calling Owl.getDownloadLink with json:{}", requestJson);
                    String response = Client.getClient().netManager.send(requestJson, owlLink.getIp(), owlLink.getPort());
                    Packet responsePacket = gson.fromJson(response, Packet.class);
                    GetStrings addresses = gson.fromJson(responsePacket.data, GetStrings.class);
                    for (String address : addresses.strings) {
                        System.out.println(address);
                        if (address.split(":")[0].equals(ClientConfig.ip)) {
                            log.info("same ip conflict");
                            continue;
                        }
                        downloadLinks.add(address);
                    }
                    log.info("printing link set: {}", downloadLinks);
                } catch (IOException | NullPointerException e) {
                    log.error("there is an error owl: {}:{}, chunk: {}", owlLink.getIp(), owlLink.getPort(), chunkHash);
                    e.printStackTrace();
                } catch (JsonSyntaxException e) {
                    e.printStackTrace();
                }
            }
        }
        for (String link : downloadLinks) {
            try {
                if (downloadChunk(torrent.getFileHash(), chunkHash, link.split(":")[0])) {
                    File file = new File(FileManager.getFileManager().path + "\\" + torrent.getFileHash() + "_" + chunkHash + ".chunk");
                    return Files.readAllBytes(file.toPath());
                } else {
                    log.error("unable to download chunk: {} from ip: {}", chunkHash, link.split(":")[0]);
                }
            } catch (IOException e) {
                log.error("error while downloading chunk: {} from ip: {}", chunkHash, link.split(":")[0]);
                e.printStackTrace();
            }
        }
        return null;
    }

    public boolean downloadChunk(String fileHash, String chunkHash, String sourceIp) throws IOException {
        String fileName = fileHash + "_" + chunkHash + ".chunk";
        String url = "http://" + sourceIp + ":8080/" + fileName;
        log.info("downloading chunk with URL: {}", url);
        String downloadPath = FileManager.getFileManager().path + "\\" + fileName;
        BufferedInputStream in = new BufferedInputStream(new URL(url).openStream());
        FileOutputStream fileOutputStream = new FileOutputStream(downloadPath);
        byte dataBuffer[] = new byte[1024];
        int bytesRead;
        while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
            fileOutputStream.write(dataBuffer, 0, bytesRead);
        }
        fileOutputStream.close();
        in.close();
        log.info("chunk downloaded: {}", fileName);
        return true;
    }

    private void PlayVideo(Torrent selectedTorrent) {
        sabKaLabel.setText("Now playing : " + selectedTorrent.getFileName());
        chunkDownloaded = new boolean[selectedTorrent.getNumberOfChunks()];
        for (int i = 0; i < chunkDownloaded.length; i++) {
            chunkDownloaded[i] = false;
        }
        chunkNeeded = 0;

        downloadPane.setVisible(false);
        uploadPane.setVisible(false);
        searchPane.setVisible(false);
        dashboardPane.setVisible(false);
        videoPane.setVisible(true);
        try {
            byte[] b = new byte[Math.toIntExact(selectedTorrent.getSize())];
            String filePath = ClientConfig.path + "\\" + "playVideo.mp4";
            //todo remove file if filepath exist
            FileOutputStream stream = new FileOutputStream(filePath);
            stream.write(b);
            String chunk0 = selectedTorrent.getChunkHashes()[0], chunk1 = null, chunkn = null;
            byte[] chunk0arr = downloadChunk(selectedTorrent, chunk0);
            byte[] chunk1arr, chunknarr;
            RandomAccessFile randomAccessFile = new RandomAccessFile(filePath, "rw");
            randomAccessFile.seek(0);
            log.error("Downloading chunk id: {}, data: {}", 0, chunk0);
            log.error("seek: {}", 0 * DAO.chunkSize);
            randomAccessFile.write(chunk0arr, 0, chunk0arr.length);
            chunkDownloaded[0] = true;
            sabKaLabel.setText("Fetching video headers. Please wait");
            if (selectedTorrent.getNumberOfChunks() == 1) {
                ;   //do nothing
            } else if (selectedTorrent.getNumberOfChunks() == 2) {
                chunk1 = selectedTorrent.getChunkHashes()[1];
                chunk1arr = downloadChunk(selectedTorrent, chunk1);
                randomAccessFile.seek(DAO.chunkSize);
                log.error("Downloading chunk id: {}, data: {}", 1, chunk0);
                log.error("seek: {}", 1 * DAO.chunkSize);
                randomAccessFile.write(chunk1arr, 0, chunk1arr.length);
                chunkDownloaded[1] = true;
            } else {
                chunk1 = selectedTorrent.getChunkHashes()[1];
                chunk1arr = downloadChunk(selectedTorrent, chunk1);
                randomAccessFile.seek(DAO.chunkSize);
                log.error("Downloading chunk id: {}, data: {}", 1, chunk0);
                log.error("seek: {}", 1 * DAO.chunkSize);
                randomAccessFile.write(chunk1arr, 0, chunk1arr.length);
                chunkDownloaded[1] = true;
                chunkn = selectedTorrent.getChunkHashes()[selectedTorrent.getNumberOfChunks() - 1];
                chunknarr = downloadChunk(selectedTorrent, chunkn);
                randomAccessFile.seek((selectedTorrent.getNumberOfChunks() - 1) * DAO.chunkSize);
                log.error("Downloading chunk id: {}, data: {}", (selectedTorrent.getNumberOfChunks() - 1), chunk0);
                log.error("seek: {}", (selectedTorrent.getNumberOfChunks() - 1) * DAO.chunkSize);
                randomAccessFile.write(chunknarr, 0, chunknarr.length);
                chunkDownloaded[selectedTorrent.getNumberOfChunks() - 1] = true;
            }
            setPlayer(selectedTorrent, 1000);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    String chunki = null;
                    byte[] chunkiarr = null;
                    do {
                        for (int i = 0; i < selectedTorrent.getNumberOfChunks(); i++) {
                            if (!chunkDownloaded[chunkNeeded]) {
                                i = chunkNeeded;
                            }
                            if (!chunkDownloaded[i]) {
                                try {
                                    chunki = selectedTorrent.getChunkHashes()[i];
                                    chunkiarr = downloadChunk(selectedTorrent, chunki);
                                    final String chunkHash = chunki;
                                    log.error("Downloading chunk id: {}, data: {}", i, chunkHash);
                                    randomAccessFile.seek(i * DAO.chunkSize);
                                    log.error("seek: {}", i * DAO.chunkSize);
                                    randomAccessFile.write(chunkiarr, 0, chunkiarr.length);
                                    chunkDownloaded[i] = true;
                                } catch (IOException e) {
                                    log.error("Unable to download chunk {}", i);
                                }
                            }
                        }
                    } while (allChunkDownloaded());
                }

                private boolean allChunkDownloaded() {
                    for (int i = 0; i < chunkDownloaded.length; i++) {
                        if (!chunkDownloaded[i])
                            return false;
                    }
                    return true;
                }
            }).start();
        } catch (ArithmeticException e) {
            log.error("file too large");
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            log.error("unable to find file: {}", ClientConfig.path + "\\" + "playVideo.mp4");
            e.printStackTrace();
        } catch (IOException e) {
            log.error("IO Error");
            e.printStackTrace();
        }
    }

    public void cancelAll(ActionEvent actionEvent) {
        fileselectedLabel.setText("No File Selected");
        typeTextField.setText("");
        insideFileTypesChipView.getChips().clear();
        tagsChipView.getChips().clear();
        sabKaLabel.setText("Cancel kae diya");
    }

}
