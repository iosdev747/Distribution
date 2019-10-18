package container.call;

import container.Torrent;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class AddNewFileRequestTemplate  implements Serializable {
    public Torrent torrentFile;
    public String sourceIP;
    public int sourcePort;
    public String[] searchEngines;
}
