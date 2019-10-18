package admin;

import LRUCache.LRUCache;
import com.google.gson.Gson;
import container.*;
import container.call.AddNewFileRequestSearchEngineTemplate;
import container.call.AddNewFileRequestTemplate;
import lombok.extern.slf4j.Slf4j;
import netmanager.NetManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

@Slf4j
public class AdminServer {

    private UserDB userDB;
    private AdminDB adminDB;
    private static AdminServer adminServer;
    NetManager netManager;
    String[] defaultClients;
    LRUCache<String, User> userLRUCache;

    private AdminServer() {
        userDB = new UserDB();
        adminDB = new AdminDB();
        userLRUCache = new LRUCache<String, User>(25);
        netManager = NetManager.getNetManager();
    }

    public static AdminServer getAdminServer() {
        return (adminServer == null) ? adminServer = new AdminServer() : adminServer;
    }

    public boolean authUser(UserCreds userCreds) {
        return userDB.userAuth(userCreds.getEmailId(), userCreds.getPasswdHash());
    }

    public User getUser(UserCreds userCreds) throws Exception {
        String pass = userCreds.getPasswdHash();
        if (userLRUCache.readValue(userCreds.getEmailId()) != null && userLRUCache.readValue(userCreds.getEmailId()).equals(pass)) {
            log.debug("Used cache for username {}", userCreds.getEmailId());
            return userLRUCache.getValue(userCreds.getEmailId());
        }
        return userDB.getUserDataWithEmail(userCreds.getEmailId(), userCreds.getPasswdHash());
    }

    public boolean createNewAccount(User user) throws Exception {
        if (userDB.addUser(user)) {
            userLRUCache.addValue(user.getUsrname(), user);
            log.debug("Used cache for for account {}", user.getEmail());
            return true;
        }
        return false;
    }

    public boolean insertMyFile(String emailId, String fileHash, String fileName) {
        if (userLRUCache.readValue(emailId) != null) {
            User user = userLRUCache.getValue(emailId);
            String[] fileHashes = user.getFileHash();
            List<String> files = Arrays.asList(fileHashes);
            files.add(fileHash);
            user.setFileHash(files.toArray(new String[0]));
            userLRUCache.replace(fileHash, user);
        }
        return userDB.addFile(emailId, fileHash, fileName);
    }

    public String[] getMyFileChunks(UserCreds userCreds) throws Exception {
        if (userLRUCache.readValue(userCreds.getEmailId()) != null) {
            log.debug("Used cache for get my fle chunks {}", userCreds.getEmailId());
            return userLRUCache.getValue(userCreds.getEmailId()).getFileHash();
        }
        User user = userDB.getUserDataWithEmail(userCreds.getEmailId(), userCreds.getPasswdHash());
        userLRUCache.addValue(user.getUsrname(), user);
        String[] chunkHashes = user.getFileHash();
        List<String > l = Arrays.asList(chunkHashes);
        HashSet<String > temp = new HashSet<String >(l);
        return temp.toArray(new String[0]);

    }

    public String[] getSearchEngines() throws Exception {
//        return new String[]{"s1^localhost^3333", "s2^localhost^3333", "s3^localhost^3333"};
        List<String > l = Arrays.asList(adminDB.getSearchEngines());
        HashSet<String > temp = new HashSet<String >(l);
        return temp.toArray(new String[0]);
    }


    public boolean addSearchEngine(String ip, String port, String name) {
        return adminDB.addSearchEngines(ip, port, name);
    }


    public boolean addNewFile(Torrent torrentFile, String sourceIP, int sourcePort, String[] searchEngines) throws Exception {
        // [X]for all a' upload all chunks
        // Owl.addClient(new Seeders(all a' are seeders), for all chunks)
        // update owls in torrent and pass to all searchEngines given in parameters
        torrentFile.setCreatedOn(String.valueOf(System.currentTimeMillis()));
        defaultClients = adminDB.getDefaultSeeders();
        boolean isFileAdded = false, isAddedToSearchEngine = false;
        AddNewFileRequestSearchEngineTemplate template = new AddNewFileRequestSearchEngineTemplate();
        template.ip = sourceIP;
        template.port = sourcePort;
        template.torrent = torrentFile;
        Gson gson = new Gson();
        Packet requestPacket = new Packet();
        requestPacket.call = "DefaultClient.download";
        requestPacket.data = gson.toJson(template, AddNewFileRequestSearchEngineTemplate.class);
        String requestJson = gson.toJson(requestPacket, Packet.class);
        for (String defaultClient : defaultClients) {
            String ip = defaultClient.split("\\^")[0];
            int port = Integer.parseInt(defaultClient.split("\\^")[1]);
            log.info("calling DefaultClient.download with json:{}", requestJson);
            log.info("ip: {}, port: {}", ip, port);
            try {
                String response = netManager.send(requestJson, ip, port);
                Packet responsePacket = gson.fromJson(response, Packet.class);
                Boolean isDownloading = gson.fromJson(responsePacket.data, Boolean.class);
                isFileAdded |= isDownloading;
            } catch (IOException e) {
                log.error("Default client not reachable: {}", defaultClient);
                e.printStackTrace();
            }
        }
        AddNewFileRequestTemplate template1 = new AddNewFileRequestTemplate();
        template1.setTorrentFile(torrentFile);
        template1.setSearchEngines(defaultClients);
        template1.setSourceIP(sourceIP);
        template1.setSourcePort(sourcePort);
        requestPacket = new Packet();
        requestPacket.call = "SearchEngine.addNewFile";
        requestPacket.data = gson.toJson(template1, AddNewFileRequestTemplate.class);
        requestJson = gson.toJson(requestPacket, Packet.class);
        log.info("calling SearchEngine.addNewFile with json:{}", requestJson);
        for (String searchEngine : searchEngines) {
            String ip = searchEngine.split("\\^")[1];
            int port = Integer.parseInt(searchEngine.split("\\^")[2]);
            log.info("ip: {}, port: {}", ip, port);
            try {
                String response = netManager.send(requestJson, ip, port);
                Packet responsePacket = gson.fromJson(response, Packet.class);
                Boolean isUploaded = gson.fromJson(responsePacket.data, Boolean.class);
                isAddedToSearchEngine |= isUploaded;
            } catch (IOException e) {
                log.error("Search engine not reachable:{}", searchEngine);
                e.printStackTrace();
            }
        }
        log.info("1");
        log.info("4");
        log.info("user file added to db");
        insertMyFile(torrentFile.getAuthor(), torrentFile.getFileHash(), torrentFile.getFileName());
        return isFileAdded && isAddedToSearchEngine;
    }
}
