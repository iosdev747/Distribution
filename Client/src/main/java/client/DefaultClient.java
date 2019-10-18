package client;

import com.google.gson.Gson;
import container.*;
import container.call.AdminServerAddNewFileTemplate;
import container.call.GetStrings;
import dao.AdminServerConfig;
import dao.DAO;
import downloadmanager.DownloadManager;
import filemanager.FileManager;
import lombok.extern.slf4j.Slf4j;
import netmanager.NetManager;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * This class represents client and contains all methods that client requires.
 * This class is a singleton class so we will have only single object of this class ever created.
 */
@Slf4j
public class DefaultClient {

    private static DefaultClient defaultClient;
    public NetManager netManager;
    public ClientData clientData;

    /**
     * Constructor of Client.
     * It's private so only getClient can access this constructor.
     */
    private DefaultClient() {
        netManager = NetManager.getNetManager((new Scanner(System.in)).nextInt());
        clientData = new ClientData();
//        DAO.clientData = clientData;
        clientData.downloadManagers = new ArrayList<>();
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
        try {
            Runtime.getRuntime().exec("python -m http.server " + 8080 + " -d " + path);
        } catch (IOException e) {
            log.error("unable to start HTTP server");
            e.printStackTrace();
        }
    }

    /**
     * <code>getClient</code> method is used to get object of this class.
     * If not instantiated than create object, assign static <code>client</code> and return.
     *
     * @return object of <code>Client</code> class
     */
    public static DefaultClient getDefaultClient() {
        return defaultClient == null ? defaultClient = new DefaultClient() : defaultClient;
    }

    public static void main(String[] args) {
        DefaultClient.getDefaultClient();
    }
    /**
     * <code>addNewFile</code> method is used to add new file to network.
     *
     * @param file          file which is to be uploaded.
     * @param searchEngines array of search engines on which upload records will be saved.
     * @return <code>true</code> if file is successfully uploaded and <code>false</code> if there was some error while uploading a file
     */
    public Boolean addNewFile(File file, String[] searchEngines) {
        try {
            // generate torrentFile and pass to below constructor call
            clientData.files.add(file);
            log.info("Adding file {}", file.getFileName());
            // call AdminServer.addNewFile

            Gson gson = new Gson();
            Packet requestPacket = new Packet();
            requestPacket.call = "AdminServer.addNewFile";
            // TODO: 11-09-2019 fill ip and port below
            requestPacket.data = gson.toJson(new AdminServerAddNewFileTemplate(file.getTorrentFile(), null, 0, searchEngines), AdminServerAddNewFileTemplate.class);
            String requestJson = gson.toJson(requestPacket);
            log.info("calling AdminServer.addNewFile with request: {}", requestJson);
            log.info("ip: {}, port: {}", AdminServerConfig.getIP(), AdminServerConfig.getPORT());
            String response = netManager.send(requestJson, AdminServerConfig.getIP(), AdminServerConfig.getPORT());
            Packet responsePacket = gson.fromJson(response, Packet.class);
            log.info("received response: {}", response);
            return gson.fromJson(responsePacket.data, Boolean.class);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    // TODO: 15-09-2019 download path is to be changed.// current path for A' is here but it needs to be "~/Downloads"
    public boolean download(Torrent torrent, String ip, int port) {
        for (String chunkHash : torrent.getChunkHashes()) {
            String fileName = torrent.getFileHash() + "_" + chunkHash + ".chunk";
            String url = "http://" + ip + ":8080/" + fileName;
            log.info("Downloading from URL: {}", url);
            try {
                BufferedInputStream in = new BufferedInputStream(new URL(url).openStream());
                String path = FileManager.getFileManager().path + "\\" + fileName;
                log.info("Path: {}", path);
                FileOutputStream fileOutputStream = new FileOutputStream(path);
                byte dataBuffer[] = new byte[1024];
                int bytesRead;
                while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
                    fileOutputStream.write(dataBuffer, 0, bytesRead);
                }
                fileOutputStream.close();
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
                // handle exception
            }
        }
        /*Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                // file already downloaded, no need to do anything
                if (allChunkDownloaded(file)) {
                    file.setState(DownloadState.COMPLETED);
                    return;
                }
                try {
                    Map<String, String> downloadLinks = new HashMap<>();
                    Torrent torrentFile = file.getTorrentFile();
                    OwlLink[] owlLinks = torrentFile.getOwlLinks();
                    String[] chunkHashes = torrentFile.getChunkHashes();
                    for (OwlLink owlLink : owlLinks) {
                        if (netManager.ping(owlLink.getIp(), owlLink.getPort())) {
                            for (String chunkHash : chunkHashes) {
                                Packet requestPacket = new Packet();
                                requestPacket.call = "Owl.getDownloadLink";
                                Gson gson = new Gson();
                                requestPacket.data = gson.toJson(chunkHash, String.class);
                                String requestJson = gson.toJson(requestPacket, Packet.class);
                                log.info("calling Owl.getDownloadLink with json:{}", requestJson);
                                String response = netManager.send(requestJson, owlLink.getIp(), owlLink.getPort());
                                Packet responsePacket = gson.fromJson(response, Packet.class);
                                GetStrings addresses = gson.fromJson(responsePacket.data, GetStrings.class);
                                for (String address : addresses.strings) {
                                    System.out.println(address);
                                    downloadLinks.put(chunkHash, address);
                                }
                            }
                        }
                    }
                    clientData.downloadManagers.add(new DownloadManager(file, downloadLinks));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (allChunkDownloaded(file)) {
                    file.setState(DownloadState.COMPLETED);
                }
            }
        });
        thread.start();*/
        return true;
    }

    /**
     * <code>allChunkDownloaded</code> method checks if all chunks are present.
     *
     * @param file file whose chunks is to be checked.
     * @return <code>true</code> if all chunks are present and <code>false</code> if not.
     */
    private boolean allChunkDownloaded(File file) {
        //calc all chunk present of given file
        Torrent torrent = file.getTorrentFile();
        int numberOfChunksPresent = 0;
        for (String chunkHash : torrent.getChunkHashes()) {
            if (FileManager.getFileManager().isFileExist(torrent.getFileName() + '_' + chunkHash + ".chunk")) {
                numberOfChunksPresent++;
            }
        }
        return numberOfChunksPresent == torrent.getNumberOfChunks();
    }

    /**
     * <code>chunkExist</code> checks if chunk exist locally.
     *
     * @param chunkHashWithParent parent hash followed by '_' then chunk hash.
     * @return <code>true</code> if chunk exist locally else <code>false</code>.
     */
    public boolean chunkExist(String chunkHashWithParent) {
        return FileManager.getFileManager().isFileExist(chunkHashWithParent + ".chunk");
    }

}
