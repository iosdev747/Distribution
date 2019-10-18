package owl;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import container.*;
import netmanager.NetManager;

@Slf4j
@Getter
@Setter
public class Owl {
    private Seeder[] seeders;
    private static Owl owl;
    NetManager netManager;
    private long timeToWait = 150;

    private Owl() {
        int port = (new Scanner(System.in)).nextInt();
        netManager = NetManager.getNetManager(port);
    }

    public static Owl getOwl() {
        return (owl == null) ? owl = new Owl() : owl;
    }

    public static void main(String[] args) {
//        Seeder seeder = new Seeder();
//        seeder.setChunkHash("BBBBB");
//        seeder.setIp("localhost");
//        seeder.setPort(32000);
        Owl.getOwl();
    }

    private void blockingFunction(Process p) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String line = "";
        log.debug("Waiting for python");
        while ((line = bufferedReader.readLine()) != null) {
            if (line.equalsIgnoreCase("waiting for client"))
                break;
        }
        log.debug("Done with python");
        //Thread.sleep(timeToWait);
    }

    private String namePipe(String funName, String query) {
        log.trace("Executing query: {}", query);
        String echoResponse = null;
        try {
            // Connect to the pipe
            String path = System.getProperty("user.dir") + "\\Owl\\src\\main\\java\\python\\" + funName + ".py";
            Process p = Runtime.getRuntime().exec("python " + path);

            blockingFunction(p);

            RandomAccessFile pipe = new RandomAccessFile("\\\\.\\pipe\\" + funName, "rw");

            String echoText = query;
            // write to pipe
            pipe.write(echoText.getBytes());
            // read response
            echoResponse = pipe.readLine();
            log.debug(echoResponse);
            pipe.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return echoResponse;
    }

    // handle exception in netmanager
    // TODO: 03-09-2019 not sure about handling this exception in net manager
    public String[] getDownloadLinks(String chunkHash) throws Exception {
        log.info("sending link for chunk: {}", chunkHash);
        seeders = getTrackers(chunkHash);
        for (Seeder seeder : seeders) {
            log.debug("seeder <chunkHash, address>: <{}, {}:{}>", seeder.getChunkHash(), seeder.getIp(), seeder.getPort());
        }
        List<String> downloadLinks = new ArrayList<String>();
        for (Seeder seeder : seeders) {
            if (seeder.getChunkHash().equals(chunkHash)) {
                //check for chunk exist if not take removing measures if ip is pingable
                downloadLinks.add(seeder.getIp() + ":" + seeder.getPort());
            }
        }
        if (!downloadLinks.isEmpty()) {
            return downloadLinks.toArray(new String[0]);
        }
        //return no link found
        log.warn("no link found for given chunkHash :{}", chunkHash);
        throw new Exception("no link found for given chunkHash :" + chunkHash);
    }

    public boolean addClient(Seeder seeder) {
        String echoResponce = namePipe("addCLient", "mutation {\n" +
                "  insert_trackers(objects: {chukHash: \"" + seeder.getChunkHash() + "\",fileHash: \"" + seeder.getFileHash() + "\", ip: \"" + seeder.getIp() + "\", port: \"" + seeder.getPort() + "\"}) {\n" +
                "    affected_rows\n" +
                "  }\n" +
                "}");
        return (echoResponce != null);
    }

    public Seeder[] getTrackers(String chunkHash) {
        String echoResponse = namePipe("getTrackers", "{\n" +
                "  trackers(where: {chukHash: {_ilike: \"" + chunkHash + "\"}}) {\n" +
                "    ip\n" +
                "    port\n" +
                "    fileHash\n" +
                "    chukHash\n" +
                "  }\n" +
                "}\n");


        Seeder[] toReturn = null;
        if (echoResponse != null) {
            JsonParser parser = new JsonParser();
            JsonArray element = (JsonArray) parser.parse(echoResponse);
            toReturn = new Seeder[element.size()];
            int i = 0;
            for (JsonElement f : element) {
                JsonObject file = f.getAsJsonObject();
                Seeder seeder = new Seeder();
                seeder.setFileHash(file.get("fileHash").getAsString());
                seeder.setChunkHash(file.get("chukHash").getAsString());
                seeder.setPort(file.get("port").getAsInt());
                seeder.setIp(file.get("ip").getAsString());
                toReturn[i] = seeder;
                i++;
            }

        }
        return toReturn;
    }

}
