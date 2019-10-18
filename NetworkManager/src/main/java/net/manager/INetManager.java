package net.manager;

import java.io.IOException;
import java.net.Socket;

public interface INetManager {
    default boolean ping(String ip, int port) {
        return false;
    }

    void recieve(Socket clientSocket) throws IOException;

    String send(String data, String ip, int port) throws IOException;
}
