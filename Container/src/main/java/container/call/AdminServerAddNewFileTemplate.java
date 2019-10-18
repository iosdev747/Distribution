package container.call;

import container.Torrent;

import java.io.Serializable;

public class AdminServerAddNewFileTemplate  implements Serializable {
    Torrent torrentFile;
    String sourceIP;
    int sourcePort;
    String[] searchEngines;

    public AdminServerAddNewFileTemplate(Torrent torrentFile, String sourceIP, int sourcePort, String[] searchEngines) {
        this.torrentFile = torrentFile;
        this.sourceIP = sourceIP;
        this.sourcePort = sourcePort;
        this.searchEngines = searchEngines;
    }
}
