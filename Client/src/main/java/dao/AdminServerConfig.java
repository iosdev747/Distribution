package dao;

/**
 * Configuration class for <code>AdminServer</code>.
 * Contains admin server information
 */
public class AdminServerConfig {
    static String IP = "127.0.0.1";
    static int PORT = 1111;

    public static String getIP() {
        return IP;
    }

    public static int getPORT() {
        return PORT;
    }
}
