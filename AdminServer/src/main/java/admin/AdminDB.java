package admin;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import container.User;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Slf4j
public class AdminDB {

    private long timeToWait=150;

    public static void main(String[] args) throws Exception {
        AdminServer.getAdminServer();
        AdminDB adminDB = new AdminDB();
        //System.out.println(adminDB.addSearchEngines("localhost","30005","Kashyap"));
        //System.out.println(user.getFileHash());
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
        log.trace("Executing query : "+query);
        String echoResponse = null;
        try {
            // Connect to the pipe
            String path = System.getProperty("user.dir")+"\\AdminServer\\src\\main\\java\\python\\"+funName+".py";
            Process p = Runtime.getRuntime().exec("python "+path);

            blockingFunction(p);

            RandomAccessFile pipe = new RandomAccessFile("\\\\.\\pipe\\"+funName, "rw");

            String echoText = query;
            // write to pipe
            pipe.write ( echoText.getBytes() );
            // read response
            echoResponse = pipe.readLine();
            log.debug(echoResponse);
            pipe.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return echoResponse;
    }

    /**
     * Get the list of search engines which are registered to AdminDB
     *
     * @return Strings in format "name^ip^port"
     */
    public String[] getSearchEngines() throws Exception {
        String[] sList = null;
        String echoResponse = namePipe("getSearchEngines", "{\n" +
                "  searchEngine {\n" +
                "    name\n" +
                "    ip\n" +
                "    port\n" +
                "  }\n" +
                "}\n");

        if(echoResponse != null){
            JsonParser parser = new JsonParser();
            JsonElement element = parser.parse(echoResponse);
            //System.out.println(element);

            try{

                JsonArray fileList = element.getAsJsonArray();
                sList = new String[fileList.size()];
                int i = 0;
                for (JsonElement f : fileList) {
                    JsonObject file = f.getAsJsonObject();
                    String name = file.get("name").getAsString();
                    String ip = file.get("ip").getAsString();
                    String port = file.get("port").getAsString();
                    sList[i++]=name+"^"+ip+"^"+port;

                }

            } catch (NullPointerException e) {
                log.error("Filelist not selected");
            }

            return sList;

        } else
            throw new Exception("User not found");
    }

    public boolean addSearchEngines(String ip, String port, String name) {
        String echoResponse = namePipe("addSearchEngines", "mutation {\n" +
                "  insert_searchEngine(objects: {ip: \"" + ip + "\", name: \"" + name + "\", port: \"" + port + "\"}) {\n" +
                "    affected_rows\n" +
                "  }\n" +
                "}\n");
        return (echoResponse != null);
    }

    public String[] getDefaultSeeders() {
        String echoResponse = namePipe("getDefaultSeeders", "{\n" +
                "  defaults {\n" +
                "    ip\n" +
                "    port\n" +
                "  }\n" +
                "}\n");


        JsonParser parser = new JsonParser();
        JsonArray element = (JsonArray) parser.parse(echoResponse);
        String[] seeders = new String[element.size()];
        int i = 0;
        for (JsonElement f : element) {
            JsonObject file = f.getAsJsonObject();
            String s = "";
            try {
                s += file.get("ip").getAsString();
            } catch (NullPointerException e) {
                log.error("ip field null");
            }
            s += "^";
            try {
                s += file.get("port").getAsString();
            } catch (NullPointerException e) {
                log.error("port field null");
            }
            seeders[i++] = s;
        }
        return seeders;
    }
}
