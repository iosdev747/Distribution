package container;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DatabaseFileTuple {
    Torrent torrent;
    int Downloads;
    int TorrentDownloads;
    Comment[] comments;
}