package downloadmanager;

import client.Client;
import com.google.common.collect.Multimap;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import container.*;
import dao.ClientConfig;
import dao.DAO;
import filemanager.ChunkManager;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import utils.FileHash;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;

/**
 * <code>DownloadManager</code> class is responsible to download single file.
 */
@Slf4j
public class DownloadManager implements Runnable {
    File file;
    int numberOfParallelDownloads;
    Flag flagStatus;
    Multimap<String, String> downloadLinks;
    @Getter
    float percentage;
    volatile int count;

    /**
     * Constructor of <code>DownloadManager</code>.
     *
     * @param file          which is to be downloaded.
     * @param downloadLinks map of chunkHash and address.
     */
    public DownloadManager(File file, Multimap<String, String> downloadLinks) {
        this.file = file;
        this.downloadLinks = downloadLinks;
        this.numberOfParallelDownloads = 5;
        this.flagStatus = Flag.START;
        this.percentage = 0.0f;
        this.count = 0;
        Thread thread = new Thread(this);
        thread.start();
    }

    @Override
    public String toString() {
        return "File Name: " + file.getFileName() + "            Percentage downloaded: " + (percentage * 100);
    }

    /**
     * this method will pause download
     */
    public void pause() {
        this.flagStatus = Flag.STOP;
    }

    /**
     * this method will play download
     */
    public void play() {
        this.flagStatus = Flag.START;
        this.run();
    }

    // TODO: 03-09-2019 confirm this method if it's for single file only.
    public boolean checkHash() {
        String[] chunkHashes = file.getTorrentFile().getChunkHashes();
        int numberOfChunks = file.getTorrentFile().getNumberOfChunks();
        for (String chunk : chunkHashes) {
            // TODO: 12-09-2019 manage partial downloaded chunks here
            //if chunk hash dont match remove file
        }
        //cal hash for all chunks available
        //and return if all chunk available
        return true;
    }

    /**
     * <code>percentageDownload</code> method calculate percentage download.
     *
     * @return percentage downloaded.
     */
    float percentageDownload() {
        String[] chunks = file.getTorrentFile().getChunkHashes();
        int c = 0;
        log.info("######################");
        for (String chunk : chunks) {
            log.error("{} {}", Client.getClient().chunkExist(file.getTorrentFile().getFileHash() + "_" + chunk), chunkHashMatch(chunk));
            if (Client.getClient().chunkExist(file.getTorrentFile().getFileHash() + "_" + chunk) && chunkHashMatch(chunk)) {
//                if (chunk.equals("11606654441054741698")) {
//                }
                log.info("{}", chunk);
                c++;
            }
        }
        if (c != 0) c++;
        this.percentage = ((float) c / (float) file.getTorrentFile().getNumberOfChunks());
        log.info("percentage : {}, {}", this.percentage, c);
        return this.percentage;
    }

    private boolean chunkHashMatch(String chunk) {
        byte[] b = new byte[0];
        try {
            b = Files.readAllBytes(Paths.get(ClientConfig.downloadPath + "\\" + file.getTorrentFile().getFileHash() + "_" + chunk + ".chunk"));
            StringBuilder stringBuilder = new StringBuilder();
            for (byte _byte : b) {
                stringBuilder.append(_byte);
            }
            return FileHash.hash(stringBuilder.toString()).equals(chunk);
        } catch (IOException e) {
            e.printStackTrace();
            log.error("chunk not not exist");
            return false;
        }
    }

    @Override
    public void run() {
        // TODO: 18-09-2019 if list is empty then remove infinite loop
        log.info("starting chunk downloading: {}", file.getFileName());
        boolean firstTime = true;
        while (true) {
            while (this.flagStatus == Flag.START && percentageDownload() != 1.0f) {
                List<ChunkDownloader> chunkDownloaders = createList();
                for (ChunkDownloader chunkDownloader : chunkDownloaders) {
                    log.info("list: {}", chunkDownloader);
                }
                log.info("downloading chunklist of size: {}", chunkDownloaders.size());
                this.count = chunkDownloaders.size();
                for (int i = 0; i < chunkDownloaders.size(); i++) {
                    Thread thread = new Thread(chunkDownloaders.get(i));
                    thread.start();
                }
                log.info("count is = {}", count);
                while (count > 0) {
//                    log.info("count: {}", count);
                }

//                ForkJoinPool forkJoinPool = new ForkJoinPool(chunkDownloaders.size());
//                try {
//                    log.info("count is = {}", count);
//                    while (!firstTime && this.count > 0) {
//                        log.info("count: {}", count);
//                    }
//                    firstTime = false;
//                    forkJoinPool.submit(() -> downloadChunkList(chunkDownloaders)).get();
//                    log.info("after forkjoinpool");
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                } catch (ExecutionException e) {
//                    e.printStackTrace();
//                }
                // TODO: 19-09-2019 remove below comment
                DAO.controller.updateListView();
            }
            if (percentageDownload() == 1.0f) {
                log.info("All chunks downloaded of file: {}", file.getFileName());
                try {
                    ChunkManager.mergeFiles(this.file.getTorrentFile().getFileHash(), ClientConfig.downloadPath, this.file.getFileName(), this.file.getTorrentFile().getChunkHashes());
                    log.info("File {} created at {}", file.getFileName(), ClientConfig.downloadPath);
                    String[] searchEngines = Client.getClient().getSearchEngines();
                    Packet requestPacket = new Packet();
                    requestPacket.call = "SearchEngine.incDownload";
                    Gson gson = new Gson();
                    requestPacket.data = gson.toJson(file.getTorrentFile(), Torrent.class);
                    String requestJson = gson.toJson(requestPacket, Packet.class);
                    log.info("calling SearchEngine.incDownload with json:{}", requestJson);
                    if (searchEngines != null) {
                        for (String searchEngine : searchEngines) {
                            String ip = searchEngine.split("\\^")[1];
                            int port = Integer.parseInt(searchEngine.split("\\^")[2]);
                            log.info("ip: {}, port: {}", ip, port);
                            try {
                                String response = Client.getClient().netManager.send(requestJson, ip, port);
                                Packet responsePacket = gson.fromJson(response, Packet.class);
                                gson.fromJson(responsePacket.data, Boolean.class);
                            } catch (IOException e) {
                                log.error("Search engine not reachable:{}", searchEngine);
                                e.printStackTrace();
                            }
                        }
                    }
                    DAO.controller.updateListView();
//                    DAO.controller.sabKaLabel.setText("Download Complete");
                } catch (IOException e) {
                    log.info("Error while merging chunks of file: {}", file.getFileName());
                    e.printStackTrace();
                }
                break;
            } else {
                log.info("download stopped: {}", file.getFileName());
            }
        }
    }

    /**
     * Download list of chunks
     *
     * @param chunkDownloaders list of chunks to be downloaded
     * @return list of chunks downloaded
     */
//    private List<ChunkDownloader> downloadChunkList(List<ChunkDownloader> chunkDownloaders) {
//        return chunkDownloaders
//                .stream()
//                .parallel()
//                .map(chunkDownloader -> downloadSingleChunk(chunkDownloader))
//                .collect(Collectors.toList());
//    }

    /**
     * manage single chunk download
     *
     * @param chunkDownloader start download for given chunk
     * @return reference of <code>chunkDownloader</code>
     */
//    private ChunkDownloader downloadSingleChunk(ChunkDownloader chunkDownloader) {
//        try {
//            log.info("downloading {}_{}.chunk", chunkDownloader.fileHash, chunkDownloader.chunkHash);
//            if (chunkDownloader.run()) {
//                OwlLink[] owlLinks = file.getTorrentFile().getOwlLinks();
//                for (OwlLink owlLink : owlLinks) {
//                    Packet requestPacket = new Packet();
//                    requestPacket.call = "Owl.addClient";
//                    Gson gson = new Gson();
//                    Seeder seeder = new Seeder();
//                    seeder.setIp(ClientConfig.ip);
//                    seeder.setPort(ClientConfig.port);
//                    seeder.setChunkHash(chunkDownloader.chunkHash);
//                    seeder.setFileHash(chunkDownloader.fileHash);
//                    requestPacket.data = gson.toJson(seeder, Seeder.class);
//                    String requestJson = gson.toJson(requestPacket, Packet.class);
//                    log.info("calling Owl.addClient with json:{}", requestJson);
//                    try {
//                        String response = Client.getClient().netManager.send(requestJson, owlLink.getIp(), owlLink.getPort());
//                        Packet responsePacket = gson.fromJson(response, Packet.class);
//                        if (gson.fromJson(responsePacket.data, Boolean.class)) {
//                            log.info("added to owl: {}:{}", owlLink.getIp(), owlLink.getPort());
//                        }
//                    } catch (IOException e) {
//                        log.error("unable to send to owl {}:{}", owlLink.getIp(), owlLink.getPort());
//                        e.printStackTrace();
//                    } catch (JsonSyntaxException e) {
//                        log.error("error in JSON parsing.. please check it out");
//                        e.printStackTrace();
//                    }
//                }
//            }
//            log.info("downloaded {}_{}.chunk", chunkDownloader.fileHash, chunkDownloader.chunkHash);
//        } catch (IOException e) {
//            log.error("error in chunkDownloader: {}_{}.chunk", chunkDownloader.fileHash, chunkDownloader.chunkHash);
//            e.printStackTrace();
//        }
//        this.count--;
//        return chunkDownloader;
//    }

    /**
     * Create list of max size <code>numberOfParallelDownloads</code>.
     * All elements in this list will download in parallel.
     *
     * @return list of chunks which will download in parallel.
     */
    private List<ChunkDownloader> createList() {
        // TODO: 16-09-2019 update logic for download
        int c = 0;
        List<ChunkDownloader> chunkDownloaders = new ArrayList<>();
        String[] chunkHashes = file.getTorrentFile().getChunkHashes();
//        log.info("Chunk Hashes: {}", chunkHashes.toString());
        for (String _chunkHash : chunkHashes) {
            if (c >= numberOfParallelDownloads) {
                break;
            }
            boolean fileExist = Client.getClient().chunkExist(file.getTorrentFile().getFileHash() + "_" + _chunkHash);
            boolean chunkmatch = chunkHashMatch(_chunkHash);
            if (!fileExist || (fileExist && !chunkmatch)) {
                Collection<String> _downloadLink = downloadLinks.get(_chunkHash);
                c++;
                Iterator<String> iterator = _downloadLink.iterator();
                if (!iterator.hasNext()) {
                    c--;
                    continue;
                }
                String downloadLink = iterator.next();
                chunkDownloaders.add(new ChunkDownloader(
                        downloadLink.split(":")[0],
                        Integer.parseInt(downloadLink.split(":")[1]),
                        ".\\Downlaods",
                        _chunkHash,
                        file.getTorrentFile().getFileHash(),
                        this
                ));
                downloadLinks.remove(_chunkHash, downloadLink);
            } else {
                log.info("chunk already exist {}", _chunkHash);
            }
        }
        return chunkDownloaders;
    }
}
