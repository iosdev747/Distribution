import container.Torrent;

public interface SearchEngine {
    boolean fileExist(String fileHash) throws Exception;

    boolean addNewFile(Torrent torrent, String ip, int port, String[] defaultClients);

    Torrent[] searchByFileName(String fileName) throws Exception;
}
