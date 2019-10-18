package dao;

import client.Client;
import filemanager.FileManager;
import lombok.extern.slf4j.Slf4j;
import utils.Utility;

import java.io.Serializable;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

/**
 * Configuration class for <code>Client</code>.
 * Contains client configurations and methods for persistent save of client configuration.
 */
@Slf4j
public class ClientConfig implements Serializable {
    private static ClientConfig config;

    public String userName;
    public String email;
    public String password;
    public static String path;
    public static int port;
    public static String ip;
    public static int HttpPort;
    public static String downloadPath;

    /**
     * Constructor of <code>ClientConfig</code>
     */
    ClientConfig() {
        userName = "";
        password = "";
        email = "";
        downloadPath = FileManager.getFileManager().path;
        String ip;
        final DatagramSocket socket;
        try {
            socket = new DatagramSocket();
            socket.connect(InetAddress.getByName("8.8.8.8"), 10002);
            ip = socket.getLocalAddress().getHostAddress();
            ClientConfig.ip = ip;
            log.error("Client ip set to {}", ClientConfig.ip);
        } catch (SocketException | UnknownHostException e) {
            ClientConfig.ip = "localhost";
            log.error("unable to set Client ip (setting default, IP = localhost)");
            e.printStackTrace();
        }
    }

    /**
     * <code>loadConfiguration</code> loads configuration using load util.
     *
     * @return Client configuration.
     */
    private static ClientConfig loadConfiguration() {
        return (ClientConfig) Utility.load(DAO.configurationLocation);
    }

    /**
     * <code>getConfig</code> fetches client configuration
     *
     * @return client configuration
     */
    public static ClientConfig getConfig() {
        if (config == null) {
            config = loadConfiguration();
            if (config == null) {
                log.warn("No Configuration Found. New Configuration created");
                config = new ClientConfig();
            } else {
                log.info("Configuration loaded");
            }
        }
        return config;
    }

    /**
     * <code>saveConfiguration</code> method saves client configuration
     *
     * @return <code>true</code> if saved config successfully else <code>false</code>.
     */
    public boolean saveConfiguration() {
        return Utility.save(DAO.configurationLocation, this);
    }
}