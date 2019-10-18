package searchengine;

import LRUCache.LRUCache;
import com.google.gson.Gson;
import container.*;
import container.call.SearchQuery;
import lombok.extern.slf4j.Slf4j;
import netmanager.NetManager;

import java.io.IOException;
import java.util.*;

@Slf4j
public class SearchEngine {
    public OwlLink[] owlLinks;
    public List<DatabaseFileTuple> databaseEntries;
    private SearchEngineDB searchEngineDB;
    private static SearchEngine searchEngine;
    NetManager netManager;
    LRUCache<String, Torrent> fileSearchLRU;

    private SearchEngine() {
        searchEngineDB = new SearchEngineDB(25);
        int port = (new Scanner(System.in)).nextInt();
        netManager = NetManager.getNetManager(port);
        databaseEntries = new ArrayList<>();
        owlLinks=searchEngineDB.getOwlLinks();
        fileSearchLRU = new LRUCache<String, Torrent>(25);
    }

    public static SearchEngine getSearchEngine() {
        return (searchEngine == null) ? searchEngine = new SearchEngine() : searchEngine;
    }

    public boolean fileExist(String fileHash) throws Exception {
        if(fileSearchLRU.readValue(fileHash)!=null) {
            fileSearchLRU.getValue(fileHash);
            log.debug("Used cache for fileExist {}", fileHash);
            return true;
        }
        return searchEngineDB.fileExist(fileHash);
    }

    public boolean addNewFile(Torrent torrent, String ip, int port, String[] defaultClients) {
        owlLinks = searchEngineDB.getOwlLinks();
        searchEngineDB.addInfo(torrent, defaultClients.length, 1);
        boolean isAdded = false;

        for (String chunkHash : torrent.getChunkHashes()) {
            for (String defaultClient : defaultClients) {
                for (OwlLink owlLink : owlLinks) {
                    Seeder seeder = new Seeder();
                    seeder.setIp(defaultClient.split("\\^")[0]);
                    seeder.setPort(Integer.parseInt(defaultClient.split("\\^")[1]));
                    seeder.setChunkHash(chunkHash);
                    seeder.setFileHash(torrent.getFileHash());
                    Gson gson = new Gson();
                    Packet requestPacket = new Packet();
                    requestPacket.call = "Owl.addClient";
                    requestPacket.data = gson.toJson(seeder, Seeder.class);
                    String requestJson = gson.toJson(requestPacket, Packet.class);
                    log.info("calling Owl.addClient with json:{}", requestJson);
                    try {
                        String response = netManager.send(requestJson, owlLink.getIp(), owlLink.getPort());
                        Packet responsePacket = gson.fromJson(response, Packet.class);
                        Boolean isDownloading = gson.fromJson(responsePacket.data, Boolean.class);
                        isAdded |= isDownloading;
                    } catch (IOException e) {
                        log.error("unreachable Owl: {}:{}", owlLink.getIp(), owlLink.getPort());
                        e.printStackTrace();
                    }
                }
            }
        }
        for (String chunkHash : torrent.getChunkHashes()) {
            for (OwlLink owlLink : owlLinks) {
                Seeder seeder = new Seeder();
                seeder.setIp(ip);
                seeder.setPort(port);
                seeder.setChunkHash(chunkHash);
                seeder.setFileHash(torrent.getFileHash());
                Gson gson = new Gson();
                Packet requestPacket = new Packet();
                requestPacket.call = "Owl.addClient";
                requestPacket.data = gson.toJson(seeder, Seeder.class);
                String requestJson = gson.toJson(requestPacket, Packet.class);
                log.info("calling Owl.addClient with json:{}", requestJson);
                try {
                    String response = netManager.send(requestJson, owlLink.getIp(), owlLink.getPort());
                    Packet responsePacket = gson.fromJson(response, Packet.class);
                    Boolean isDownloading = gson.fromJson(responsePacket.data, Boolean.class);
                    isAdded |= isDownloading;
                } catch (IOException e) {
                    log.error("unreachable Owl: {}:{}", owlLink.getIp(), owlLink.getPort());
                    e.printStackTrace();
                }
            }
        }

        torrent.setOwlLinks(owlLinks);
        if (isAdded) {
            isAdded = searchEngineDB.addInfo(torrent, defaultClients.length, 2);
        }
        fileSearchLRU.addValue(torrent.getFileHash(),torrent);
        return isAdded;
    }

    //currently search by names

    public Torrent[] searchByFileName(SearchQuery query) {
        Torrent[] torrents = null;
        try {
            log.debug("searching query: {}", query);
            torrents = searchEngineDB.getFile(query);
        } catch (Exception e) {
            log.error("problem while fetching: {}", query);
            e.printStackTrace();
        }
        return torrents;
    }

    public boolean comment(String comment, Torrent torrent, String userName) {
        return searchEngineDB.addComment(comment,torrent,userName);
    }

    public String[] getComments(Torrent torrent) {
        return searchEngineDB.getComments(torrent);
    }

    public void incDownload(Torrent torrent){ searchEngineDB.incDownloads(torrent);}

    public Integer getDownloads(String fileHash) {
        return searchEngineDB.getDownloads(fileHash);
    }

    public Torrent[] getSuggestions(Torrent[] torrents) throws Exception {
        return searchEngineDB.getSuggestions(torrents);
    }
}