package container.call;

import container.Torrent;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class TorrentArrayTemplate implements Serializable {
    public Torrent[] torrents;
}
