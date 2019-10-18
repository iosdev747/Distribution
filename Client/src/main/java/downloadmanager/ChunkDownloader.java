package downloadmanager;

import client.Client;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import container.*;
import dao.ClientConfig;
import filemanager.FileManager;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;

/**
 * <code>ChunkDownloader</code> class is responsible to download individual chunk
 */
@Slf4j
public class ChunkDownloader implements Runnable {
    String sourceIp;
    int sourcePort;
    String fileLocation;
    String chunkHash;
    String fileHash;
    DownloadManager downloadManager;

    /**
     * Constructor of ChunkDownloader
     *
     * @param sourceIp
     * @param sourcePort
     * @param fileLocation
     * @param chunkHash
     * @param fileHash
     */
    public ChunkDownloader(String sourceIp, int sourcePort, String fileLocation, String chunkHash, String fileHash, DownloadManager downloadManager) {
        this.sourceIp = sourceIp;
        this.sourcePort = sourcePort;
        this.fileLocation = fileLocation;
        this.chunkHash = chunkHash;
        this.fileHash = fileHash;
        this.downloadManager = downloadManager;
//        new Thread(this).start();
    }

    /**
     * This method downloads chunk
     */
    private boolean download() throws IOException {
        log.info("chunk downloading {}", chunkHash);
        if (sourceIp.equals(ClientConfig.ip)) {
            log.info("same ip conflict");
            return false;
        }
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

    @Override
    public String toString() {
        return this.fileHash + "_" + this.chunkHash + ".chunk" + " (" + this.sourceIp + ":" + this.sourcePort + ") with location: " + this.fileLocation;
    }

    @Override
    public void run() {
        try {
            log.info("before download");
            download();
            log.info("after download");
            OwlLink[] owlLinks = downloadManager.file.getTorrentFile().getOwlLinks();
            for (OwlLink owlLink : owlLinks) {
                Packet requestPacket = new Packet();
                requestPacket.call = "Owl.addClient";
                Gson gson = new Gson();
                Seeder seeder = new Seeder();
                seeder.setIp(ClientConfig.ip);
                seeder.setPort(ClientConfig.port);
                seeder.setChunkHash(chunkHash);
                seeder.setFileHash(fileHash);
                requestPacket.data = gson.toJson(seeder, Seeder.class);
                String requestJson = gson.toJson(requestPacket, Packet.class);
                log.info("calling Owl.addClient with json:{}", requestJson);
                try {
                    String response = Client.getClient().netManager.send(requestJson, owlLink.getIp(), owlLink.getPort());
                    Packet responsePacket = gson.fromJson(response, Packet.class);
                    if (gson.fromJson(responsePacket.data, Boolean.class)) {
                        log.info("added to owl: {}:{}", owlLink.getIp(), owlLink.getPort());
                    }
                } catch (IOException e) {
                    log.error("unable to send to owl {}:{}", owlLink.getIp(), owlLink.getPort());
                    e.printStackTrace();
                } catch (JsonSyntaxException e) {
                    log.error("error in JSON parsing.. please check it out");
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            log.error("unable to download chunk {}", this.fileHash + "_" + this.chunkHash + ".chunk");
            e.printStackTrace();
        }
        downloadManager.count--;
    }
}
