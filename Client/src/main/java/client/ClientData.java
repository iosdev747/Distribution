package client;

import container.File;
import container.Torrent;
import container.User;
import downloadmanager.DownloadManager;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * <code>ClientData</code> class contains all the data client requires.
 */
public class ClientData implements Serializable {
    public List<String> chunkHash;  // hash of chunks user have
    public List<File> files;        // files in application download/complete queue
    //    public User user;               // user.fileHash are files uploaded by user
    public List<DownloadManager> downloadManagers;  // list of DownloadManager to handle download
    public List<Torrent> history;
    /**
     * Constructor of <code>ClientData</code>
     */
    public ClientData() {
        this.chunkHash = new ArrayList<>();
        this.files = new ArrayList<>();
//        this.user = new User();
        this.downloadManagers = new ArrayList<>();
        history = new ArrayList<>();
    }
}
