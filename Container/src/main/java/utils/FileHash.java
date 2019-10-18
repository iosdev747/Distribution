package utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.DigestInputStream;
import java.security.NoSuchAlgorithmException;

public class FileHash {

    private static final String ALPHA_NUMERIC_STRING = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    public static String checksum(String filepath) throws IOException {
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            // unable to get message digest instance, returns 64 char string
            return randomAlphaNumeric(64);
        }
        // file hashing with DigestInputStream
        try (DigestInputStream dis = new DigestInputStream(new FileInputStream(filepath), md)) {
            while (dis.read() != -1) ; //empty loop to clear the data
            md = dis.getMessageDigest();
        }
        // bytes to hex
        StringBuilder result = new StringBuilder();
        for (byte b : md.digest()) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }

    // random alpha-numeric string generator
    private static String randomAlphaNumeric(int count) {
        StringBuilder builder = new StringBuilder();
        while (count-- != 0) {
            int character = (int) (Math.random() * ALPHA_NUMERIC_STRING.length());
            builder.append(ALPHA_NUMERIC_STRING.charAt(character));
        }
        return builder.toString();
    }

    public static String hash(String text) {
        MessageDigest digest = null;
        try {
            digest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return randomAlphaNumeric(64);
        }
        byte[] hash = digest.digest(text.getBytes(StandardCharsets.UTF_8));
        StringBuilder temp = new StringBuilder();
        for (byte b : hash) {
            temp.append(b);
        }
        return temp.toString();
    }

}