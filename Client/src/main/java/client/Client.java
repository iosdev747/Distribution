package client;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multimap;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import container.*;
import container.call.*;
import dao.AdminServerConfig;
import dao.ClientConfig;
import dao.DAO;
import downloadmanager.DownloadManager;
import filemanager.FileManager;
import lombok.extern.slf4j.Slf4j;
import netmanager.NetManager;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.*;

/**
 * This class represents client and contains all methods that client requires.
 * This class is a singleton class so we will have only single object of this class ever created.
 */
@Slf4j
public class Client {

    public NetManager netManager;
    public ClientData clientData;

    private static Client client;

    /**
     * Constructor of Client.
     * It's private so only getClient can access this constructor.
     */
    private Client() {
        ClientConfig.port = (new Scanner(System.in)).nextInt();
        netManager = NetManager.getNetManager(ClientConfig.port);
        clientData = new ClientData();
//        DAO.clientData = clientData;
//        clientData.user = new User();
//        clientData.user.setName("some random userName");
        clientData.downloadManagers = new ArrayList<>();
    }

    /**
     * <code>getClient</code> method is used to get object of this class.
     * If not instantiated than create object, assign static <code>client</code> and return.
     *
     * @return object of <code>Client</code> class
     */
    public static Client getClient() {
        return client == null ? client = new Client() : client;
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
            requestPacket.data = gson.toJson(new AdminServerAddNewFileTemplate(file.getTorrentFile(), ClientConfig.ip, ClientConfig.port, searchEngines), AdminServerAddNewFileTemplate.class);
            String requestJson = gson.toJson(requestPacket);
            log.info("calling AdminServer.addNewFile with request: {}", requestJson);
            log.info("ip: {}, port: {}", AdminServerConfig.getIP(), AdminServerConfig.getPORT());
            String response = netManager.send(requestJson, AdminServerConfig.getIP(), AdminServerConfig.getPORT());
            Packet responsePacket = gson.fromJson(response, Packet.class);
            log.info("received response: {}", response);
            Boolean result = gson.fromJson(responsePacket.data, Boolean.class);
            if (result == null || !result) {
                return false;
            } else {
                requestPacket.call = "AdminServer.insertMyFile";
                InsertMyFileRequestTemplate template = new InsertMyFileRequestTemplate();
                template.emailId = ClientConfig.getConfig().email;
                template.fileName = file.getFileName();
                template.fileHash = file.getTorrentFile().getFileHash();
                requestPacket.data = gson.toJson(template);
                log.info("calling AdminServer.insertMyFile with request: {}", requestJson);
                log.info("ip: {}, port: {}", AdminServerConfig.getIP(), AdminServerConfig.getPORT());
                response = netManager.send(requestJson, AdminServerConfig.getIP(), AdminServerConfig.getPORT());
                responsePacket = gson.fromJson(response, Packet.class);
                log.info("received response: {}", response);
                gson.fromJson(responsePacket.data, Boolean.class);
//                    log.info("file added to userdb");
//                } else {
//                    log.info("file wasn't added to userdb");
//                }
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * <code>getSearchEngines</code> method fetches all the searchEngines which are registered with <code>AdminServer</code>.
     *
     * @return array of <code>searchEngines</code>, eg "searchEngineName1^ip^port"
     */
    public String[] getSearchEngines() {
        try {
            Gson gson = new Gson();
            Packet requestPacket = new Packet();
            requestPacket.call = "AdminServer.getSearchEngines";
            String requestJson = gson.toJson(requestPacket);
            log.info("calling AdminServer.getSearchEngines with request: {}", requestJson);
            log.info("ip: {}, port: {}", AdminServerConfig.getIP(), AdminServerConfig.getPORT());
            String response = netManager.send(requestJson, AdminServerConfig.getIP(), AdminServerConfig.getPORT());
            Packet responsePacket = gson.fromJson(response, Packet.class);
            log.info("received response: {}", response);
            GetStrings result = gson.fromJson(responsePacket.data, GetStrings.class);
            if (result != null)
                return result.strings;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    // TODO: 08-09-2019 tell @kashyap to gime ip and port of search engines else this method will become orphan
    Torrent[] search(String fileName, String[] searchEngines) {
        List<Torrent> torrentList = new ArrayList<>();
        Packet requestPacket = new Packet();
        requestPacket.call = "SearchEngine.searchByFileName";
        Gson gson = new Gson();
        requestPacket.data = gson.toJson(fileName, String.class);
        String requestJson = gson.toJson(requestPacket, Packet.class);
        log.info("calling SearchEngine.searchByFileName with json:{}", requestJson);
        for (String searchEngine : searchEngines) {
            String response = null;
            try {
                response = netManager.send(requestJson, searchEngine.split("\\^")[0], Integer.parseInt(searchEngine.split("\\^")[1]));
                Packet responsePacket = gson.fromJson(response, Packet.class);
                TorrentArrayTemplate torrents = gson.fromJson(responsePacket.data, TorrentArrayTemplate.class);
                for (Torrent torrent : torrents.getTorrents()) {
                    torrentList.add(torrent);
                }
            } catch (IOException e) {
                log.error("Unable to find from server: {}", searchEngine);
                e.printStackTrace();
            }
        }
        return torrentList.toArray(new Torrent[0]);
    }

    /**
     * <code>download</code> method fetch all the chunks from owls and add to <code>downloadManagers</code> list.
     *
     * @param file file from which all chunk and owl information is fetched.
     */
    public void download(File file) {
        log.info("trying to download file: {}", file.getFileName());
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                // file already downloaded, no need to do anything
                if (allChunkDownloaded(file)) {
                    file.setState(DownloadState.COMPLETED);
                    return;
                }
                // for all trackers do
                // netManagerService.send(fileHash, tracker.ip, tracker.port)
                // manage multiple parallel downloads by threading
                Multimap<String, String> multimap = HashMultimap.create();
                Torrent torrentFile = file.getTorrentFile();
                OwlLink[] owlLinks = torrentFile.getOwlLinks();
                String[] chunkHashes = torrentFile.getChunkHashes();
//                boolean success = false;
                for (OwlLink owlLink : owlLinks) {
                    if (netManager.ping(owlLink.getIp(), owlLink.getPort())) {
                        for (String chunkHash : chunkHashes) {
                            try {
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
//                                    downloadLinks.put(chunkHash, address);
                                    multimap.put(chunkHash, address);
                                }
                                log.info("printing multimap");
//                                for (String key : multimap.keySet()) {
//                                    List<String> list = (List<String>) multimap.get(key);
//                                    for (int i = 0; i < list.size(); i++) {
//                                        for (int j = i + 1; j < list.size(); j++) {
//                                            System.out.println();
//                                            log.info("{}, {}", list.get(i), list.get(j));
//                                        }
//                                    }
//                                }
                                log.info(String.valueOf(multimap));
                            } catch (IOException | NullPointerException e) {
                                log.error("there is an error owl: {}:{}, chunk: {}", owlLink.getIp(), owlLink.getPort(), chunkHash);
                                e.printStackTrace();
                            } catch (JsonSyntaxException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
                clientData.downloadManagers.add(new DownloadManager(file, multimap));
                if (allChunkDownloaded(file)) {
                    file.setState(DownloadState.COMPLETED);
                }
            }
        });
        thread.start();
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
     * <code>getAvailability</code> method will fetch availability of a file.
     *
     * @param torrentFile whose availability is to be fetched.
     * @return availability in <code>float</code>.
     */
    float getAvailability(Torrent torrentFile) {
        try {
            OwlLink[] owlLinks = torrentFile.getOwlLinks();
            String[] chunkHashes = torrentFile.getChunkHashes();
            int numberOfChunksPresent = 0;
            for (OwlLink owlLink : owlLinks) {
                if (netManager.ping(owlLink.getIp(), owlLink.getPort())) {
                    for (String chunkHash : chunkHashes) {
                        Packet requestPacket = new Packet();
                        Gson gson = new Gson();
                        requestPacket.call = "Owl.getDownloadLink";
                        requestPacket.data = gson.toJson(chunkHash, String.class);
                        String requestJson = gson.toJson(requestPacket, Packet.class);
                        log.info("requesting Owl.getDownloadLink with json :{}", requestJson);
                        String responseJson = netManager.send(requestJson, owlLink.getIp(), owlLink.getPort());
                        Packet responsePacket = gson.fromJson(responseJson, Packet.class);
                        GetStrings downloadLinks = gson.fromJson(responsePacket.data, GetStrings.class);
                        for (String downloadLink : downloadLinks.strings) {
                            requestPacket = new Packet();
                            requestPacket.call = "Client.chunkExist";
                            requestPacket.data = gson.toJson(torrentFile.getFileHash() + "_" + chunkHash, String.class);
                            requestJson = gson.toJson(requestPacket, Packet.class);
                            log.info("requesting Client.chunkExist for chunkHash :{} with json :{}", chunkHash, requestJson);
                            responseJson = netManager.send(requestJson, downloadLink.split(":")[0], Integer.parseInt(downloadLink.split(":")[1]));
                            responsePacket = gson.fromJson(responseJson, Packet.class);
                            Boolean isChunkExist = gson.fromJson(responseJson, Boolean.class);
                            if (isChunkExist) {
                                numberOfChunksPresent++;
                                break;
                            }
                        }
                    }
                }
            }
            return numberOfChunksPresent / torrentFile.getNumberOfChunks();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0.0f;
    }

    /**
     * <code>chunkExist</code> checks if chunk exist locally.
     *
     * @param chunkHashWithParent parent hash followed by '_' then chunk hash.
     * @return <code>true</code> if chunk exist locally else <code>false</code>.
     */
    public boolean chunkExist(String chunkHashWithParent) {
        boolean result = FileManager.getFileManager().isFileExist(chunkHashWithParent + ".chunk");
        log.info("chunk exist: {} {}", result, chunkHashWithParent);
        return result;
    }

    /**
     * <code>authUser</code> call <code>AdminServer</code> to authenticate user.
     *
     * @param emailId    email ID of user
     * @param passwdHash password hash of user
     * @return <code>true</code> if user is successfully authenticated from <code>AdminServer</code> else <code>false</code>.
     */
    public boolean authUser(String emailId, String passwdHash) {
        try {
            UserCreds userCreds = new UserCreds(emailId, passwdHash);
            if (netManager.ping(AdminServerConfig.getIP(), AdminServerConfig.getPORT())) {
                Packet requestPacket = new Packet();
                Gson gson = new Gson();
                requestPacket.call = "AdminServer.authUser";
                requestPacket.data = gson.toJson(userCreds, UserCreds.class);
                String requestJson = gson.toJson(requestPacket, Packet.class);
                log.info("requesting AdminServer.authUser with json :{}", requestJson);
                String responseJson = netManager.send(requestJson, AdminServerConfig.getIP(), AdminServerConfig.getPORT());
                Packet responsePacket = gson.fromJson(responseJson, Packet.class);
                Boolean result = gson.fromJson(responsePacket.data, Boolean.class);
                return result != null && result;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean registerUser(User user) {
        try {
            if (netManager.ping(AdminServerConfig.getIP(), AdminServerConfig.getPORT())) {
                Packet requestPacket = new Packet();
                Gson gson = new Gson();
                requestPacket.call = "AdminServer.createNewAccount";
                requestPacket.data = gson.toJson(user, User.class);
                String requestJson = gson.toJson(requestPacket, Packet.class);
                log.info("requesting AdminServer.authUser with json :{}", requestJson);
                String responseJson = netManager.send(requestJson, AdminServerConfig.getIP(), AdminServerConfig.getPORT());
                Packet responsePacket = gson.fromJson(responseJson, Packet.class);
                return gson.fromJson(responsePacket.data, Boolean.class);
            }
            return false;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public String[] fetchUserFiles() {
        UserCreds userCreds = new UserCreds(ClientConfig.getConfig().email, ClientConfig.getConfig().password);
        if (netManager.ping(AdminServerConfig.getIP(), AdminServerConfig.getPORT())) {
            Packet requestPacket = new Packet();
            Gson gson = new Gson();
            requestPacket.call = "AdminServer.getMyFileChunks";
            requestPacket.data = gson.toJson(userCreds, UserCreds.class);
            String requestJson = gson.toJson(requestPacket, Packet.class);
            log.info("requesting AdminServer.getMyFileChunks with json :{}", requestJson);
            try {
                String responseJson = netManager.send(requestJson, AdminServerConfig.getIP(), AdminServerConfig.getPORT());
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

    public boolean comment(String comment, Torrent selectedTorrent, boolean isAnonymous, String[] searchEngines) {
        Packet requestPacket = new Packet();
        Gson gson = new Gson();
        requestPacket.call = "SearchEngine.comment";
        CallAddComment _comment = new CallAddComment();
        _comment.setComment(comment);
        _comment.setTorrent(selectedTorrent);
        if (isAnonymous)
            _comment.setUserName("Anonymous");
        else
            _comment.setUserName(ClientConfig.getConfig().email);
        requestPacket.data = gson.toJson(_comment, CallAddComment.class);
        String requestJson = gson.toJson(requestPacket, Packet.class);
        log.info("requesting SearchEngine.comment with json :{}", requestJson);
        for (String searchEngine : searchEngines)
            try {
                String ip = searchEngine.split("\\^")[1];
                int port = Integer.parseInt(searchEngine.split("\\^")[2]);
                String responseJson = netManager.send(requestJson, ip, port);
                Packet responsePacket = gson.fromJson(responseJson, Packet.class);
                Boolean isAdded = gson.fromJson(responsePacket.data, Boolean.class);
                return isAdded != null && isAdded;
            } catch (IOException e) {
                log.error("unable to call AdminServer.getMyFileChunks");
                e.printStackTrace();
            }
        return false;
    }

    public void saveHistory() {
        utils.Utility.save("./client_data", clientData);
    }

    public Torrent[] getHistory() {
        clientData = (ClientData) utils.Utility.load("./client_data");
        return clientData.history.toArray(new Torrent[0]);
    }
}
