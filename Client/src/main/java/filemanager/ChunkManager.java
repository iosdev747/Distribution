package filemanager;

import dao.ClientConfig;
import dao.DAO;
import utils.FileHash;

import java.io.*;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

// Credits to @Pshemo for chunk manager (https://stackoverflow.com/a/10864346).
public class ChunkManager {

    /**
     * Split given file to chunks of size <code>chunkSize</code>
     *
     * @param file     file which is to be converted in chunks.
     * @param fileHash file hash.
     * @return list of chunkHash created.
     * @throws IOException if unable to read file or create file.
     */
    public static List<String> splitFile(File file, String fileHash) throws IOException {
        List<String> chunkHashList = new ArrayList<>();
        int chunkSize = DAO.chunkSize;// size in bytes ,currently 1MB
        byte[] buffer = new byte[chunkSize];
        try (FileInputStream fis = new FileInputStream(file);
             BufferedInputStream bis = new BufferedInputStream(fis)) {
            int bytesAmount = 0;
            while ((bytesAmount = bis.read(buffer)) > 0) {
                System.out.print(".");
                StringBuilder stringBuilder = new StringBuilder();
                for (byte _byte : buffer) {
                    stringBuilder.append(_byte);
                }
                String chunkHash = FileHash.hash(stringBuilder.toString());
                chunkHashList.add(chunkHash);
                String chunkName = String.format("%s_%s.chunk", fileHash, chunkHash);
//                File newFile = new File(file.getParent(), chunkName);
                File newFile = new File(ClientConfig.path + "\\" + chunkName);
                try (FileOutputStream out = new FileOutputStream(newFile)) {
                    out.write(buffer, 0, bytesAmount);
                    out.flush();
                }
            }
        }
        System.out.println();
        return chunkHashList;
    }

    /**
     * Merge chunks back to original file.
     *
     * @param parentHash     parent hash.
     * @param path           path where file will made.
     * @param outputFileName otuput file name.
     * @param chunkHashList  list of chunk hashes.
     * @throws IOException if unable to read/write chunk/new file.
     */
    public static void mergeFiles(String parentHash, String path, String outputFileName, String[] chunkHashList) throws IOException {
        File outputFile = new File(path + "\\" + outputFileName);
        System.out.println("fileName = " + outputFile.getName());
        System.out.println(outputFile.getAbsolutePath());
        try (FileOutputStream fos = new FileOutputStream(outputFile);
             BufferedOutputStream mergingStream = new BufferedOutputStream(fos)) {
            for (String chunk : chunkHashList) {
                File f = new File(path + "\\" + parentHash + '_' + chunk + ".chunk");
                System.out.println(f.getAbsolutePath());
                Files.copy(f.toPath(), mergingStream);
            }
        }
    }

    /**
     * test main
     */
    public static void main(String[] args) {
        String[] chunkHashList = null;
        String fileHash = null;
        try {
            File file__ = new File("C:\\Users\\iOSDev747\\Desktop\\Desktop\\here\\Among.Us.v2019.8.14s.rar");
            byte[] b = Files.readAllBytes(Paths.get(file__.getAbsolutePath()));
            StringBuilder stringBuilder = new StringBuilder();
            for (byte _byte : b) {
                stringBuilder.append(_byte);
            }
            System.out.println("calculating file hash...");
            fileHash = FileHash.hash(stringBuilder.toString());

            System.out.println("Spliting file...");
            chunkHashList = splitFile(file__, String.valueOf(fileHash)).toArray(new String[0]);

            System.out.println("merge to newFile.rar...");
            mergeFiles(String.valueOf(fileHash), "C:\\Users\\iOSDev747\\Desktop\\Desktop\\here", "newFile.rar", chunkHashList);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}