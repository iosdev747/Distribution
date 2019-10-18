package filemanager;

import container.*;

import java.io.File;

/**
 * <code>FileManager</code> manages file
 */
public class FileManager {
    public String path;
    OS osType;
    static FileManager fileManager;

    /**
     * Constructor of <code>FileManager</code>
     */
    private FileManager() {
        String property = System.getProperty("os.name");
        String home = System.getProperty("user.home");
        if (property.toLowerCase().contains("Linux".toLowerCase())) {
            this.path = home + "/Downloads";
            this.osType = OS.LINUX;
        } else if (property.toLowerCase().contains("Windows".toLowerCase())) {
            this.path = home + "\\Downloads";
            this.osType = OS.WINDOWS;
        } else {
            this.path = home;
            this.osType = OS.OTHER;
        }
    }

    /**
     * <code>getFileManager</code> method is used to get object of this class.
     * If not instantiated than create object, assign static <code>fileManager</code> and return.
     *
     * @return object of <code>FileManager</code> class
     */
    public static FileManager getFileManager() {
        return (fileManager == null) ? fileManager = new FileManager() : fileManager;
    }

    /**
     * checks if file exist
     *
     * @param fileName file name which is to be checked
     * @param path     path of possible existence of file
     * @return <code>true</code> if file exists else <code>false</code>
     */
    public boolean isFileExist(String fileName, String path) {
        File file = new File(path + "\\" + fileName);
        return file.isFile();
    }

    /**
     * checks if file exist
     *
     * @param fileName file name which is to be checked with default path
     * @return <code>true</code> if file exists else <code>false</code>
     */
    public boolean isFileExist(String fileName) {
        File file = new File(path + "\\" + fileName);
        return file.exists();
    }

    /**
     * create new empty file
     *
     * @param fileName file name
     * @param path     path where file will be created
     * @return <code>true</code> if file is created successfully else <code>false</code>
     */
    boolean createNewEmptyFile(String fileName, String path) {
        try {
            File file = new File(path + "\\" + fileName);
            return file.createNewFile();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

}
