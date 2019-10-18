package container;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class File {
    String fileName;
    DownloadState state;
    Torrent torrentFile;
    float percentageDownloaded;


    public File(String fileName, Torrent torrentFile, DownloadState state) {
        this.fileName = fileName;
        this.state = state;
        this.torrentFile = torrentFile;
        this.percentageDownloaded = 0.0f;
    }

    float getPercentage() {
        //return percentage of file downlaod by local chunk hash matching
        return this.percentageDownloaded;
    }

}