package netmanager;

import com.google.gson.Gson;
import container.Packet;
import container.Torrent;
import container.call.*;
import lombok.extern.slf4j.Slf4j;
import net.manager.NetManagerService;
import searchengine.SearchEngine;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

@Slf4j
public class NetManager extends NetManagerService {

    private static NetManager netManager;

    private NetManager(int port) {
        super(port);
    }

    public static NetManager getNetManager(int port) {
        if (netManager == null) {
            netManager = new NetManager(port);
        }
        return netManager;
    }

    @Override
    public void recieve(Socket clientSocket) {
        new Thread(this).start();
        String httpResponse = null;
        httpResponse = "";

        InputStreamReader isr = null;
        BufferedReader reader = null;
//        int c = 0;        //c is the parsed object
        //use above reader to parse json and put it in object
        try {
            isr = new InputStreamReader(clientSocket.getInputStream());
            reader = new BufferedReader(isr);
            String request = "";
            while (true) {
                int temp = reader.read();
                if (temp == (int) '\0') {
                    break;
                }
                request += (char) temp;
            }
//            c = request.length();
            log.info("received request:{}", request);
            Gson gson = new Gson();
            Packet requestPacket = gson.fromJson(request, Packet.class);
            Packet responsePacket = new Packet();
            if (requestPacket.call.equals("SearchEngine.fileExist")) {
                String fileHash = gson.fromJson(requestPacket.data, String.class);
                Boolean isExist = SearchEngine.getSearchEngine().fileExist(fileHash);
                responsePacket.data = gson.toJson(isExist, Boolean.class);
            } else if (requestPacket.call.equals("SearchEngine.addNewFile")) {
                AddNewFileRequestTemplate template = gson.fromJson(requestPacket.data, AddNewFileRequestTemplate.class);
                Boolean isAdded = SearchEngine.getSearchEngine().addNewFile(template.torrentFile, template.sourceIP, template.sourcePort, template.searchEngines);
                responsePacket.data = gson.toJson(isAdded, Boolean.class);
            } else if (requestPacket.call.equals("SearchEngine.searchByFileName")) {
                SearchQuery searchQuery = gson.fromJson(requestPacket.data, SearchQuery.class);
                Torrent[] torrents = SearchEngine.getSearchEngine().searchByFileName(searchQuery);
                TorrentArrayTemplate response = new TorrentArrayTemplate();
                response.setTorrents(torrents);
                responsePacket.data = gson.toJson(response, TorrentArrayTemplate.class);
            } else if (requestPacket.call.equals("SearchEngine.getDownloads")) {
                String fileHash = gson.fromJson(requestPacket.data, String.class);
                Integer downloads = SearchEngine.getSearchEngine().getDownloads(fileHash);
                responsePacket.data = gson.toJson(downloads, Integer.class);
            } else if (requestPacket.call.equals("SearchEngine.comment")) {
                CallAddComment comment = gson.fromJson(requestPacket.data, CallAddComment.class);
                Boolean isAdded = SearchEngine.getSearchEngine().comment(comment.getComment(), comment.getTorrent(), comment.getUserName());
                responsePacket.data = gson.toJson(isAdded, Boolean.class);
            } else if (requestPacket.call.equals("SearchEngine.getComments")) {
                Torrent torrent = gson.fromJson(requestPacket.data, Torrent.class);
                String[] comments = SearchEngine.getSearchEngine().getComments(torrent);
                GetStrings strings = new GetStrings();
                strings.setStrings(comments);
                responsePacket.data = gson.toJson(strings, GetStrings.class);
            } else if (requestPacket.call.equals("SearchEngine.incDownload")) {
                Torrent torrent = gson.fromJson(requestPacket.data, Torrent.class);
                SearchEngine.getSearchEngine().incDownload(torrent);
            } else if (requestPacket.call.equals("SearchEngine.getSuggestions")) {
                TorrentArrayTemplate torrent = gson.fromJson(requestPacket.data, TorrentArrayTemplate.class);
                Torrent[] torrrents = SearchEngine.getSearchEngine().getSuggestions(torrent.torrents);
                TorrentArrayTemplate torren = new TorrentArrayTemplate();
                torren.setTorrents(torrrents);
                responsePacket.data = gson.toJson(torren, TorrentArrayTemplate.class);
            }
            httpResponse += gson.toJson(responsePacket);
        } catch (IOException e) {
            log.error("Unable to read request");
            Packet responsePacket = new Packet();
            responsePacket.call = "Return.Error";
            responsePacket.data = "";
            httpResponse += new Gson().toJson(responsePacket);
        } catch (Exception e) {
            log.error("Unable to process request");
            e.printStackTrace();
            Packet responsePacket = new Packet();
            responsePacket.call = "Return.Error";
            responsePacket.data = "";
            httpResponse += new Gson().toJson(responsePacket);
        }
        try {
            log.info("Sending response " + httpResponse + "\r\n\r\n");
//            new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream())).write(httpResponse);
            httpResponse += '\0';
            clientSocket.getOutputStream().write(httpResponse.getBytes("UTF-8"));
        } catch (IOException e) {
            log.error("Unable to send response");
        }
    }

    @Override
    public void start() {

    }
}
