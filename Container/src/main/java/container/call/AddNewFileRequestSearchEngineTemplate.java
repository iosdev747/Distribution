package container.call;

import container.Torrent;

import java.io.Serializable;

public class AddNewFileRequestSearchEngineTemplate  implements Serializable {
    public Torrent torrent;
    public String ip;
    public int port;
}
