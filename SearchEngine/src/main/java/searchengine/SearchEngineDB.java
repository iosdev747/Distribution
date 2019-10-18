package searchengine;

import LRUCache.LRUCache;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.internal.$Gson$Preconditions;
import container.OwlLink;
import container.Torrent;
import container.call.SearchQuery;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.util.*;

@Slf4j
@Getter
@Setter
public class SearchEngineDB {

    private long timeToWait = 150;
    LRUCache<String,Torrent> fileLRU;

    public SearchEngineDB(int size){
        fileLRU = new LRUCache<String, Torrent>(size);
    }

    public static void main(String[] args) {
//       SearchEngineDB searchEngineDB = new SearchEngineDB(253000);
//        searchEngineDB.fileExist("A");
        SearchEngine.getSearchEngine();
//        String[] insideFileTypes = new String[5];
//        insideFileTypes[0]="JAR0";
//        insideFileTypes[1]="JAR1";
//        insideFileTypes[2]="JAR2";
//        insideFileTypes[3]="JAR3";
//        insideFileTypes[4]="JAR4";
//
//        String[] tags = new String[3];
//        tags[0]="tag0";
//        tags[1]="tag1";
//        tags[2]="tag2";
//
//        OwlLink[] owlLink = new OwlLink[2];
//        owlLink[0] = new OwlLink();
//        owlLink[0].setPort(22222);
//        owlLink[0].setIp("localhost");
//        owlLink[1] = new OwlLink();
//        owlLink[1].setIp("localhost");
//        owlLink[1].setPort(33333);
//
//        String[] chunkHashes = new String[3];
//        chunkHashes[0] = "ABC";
//        chunkHashes[1] = "DEF";
//        chunkHashes[2] = "GHI";
//
//        Torrent torrent = new Torrent();
//        torrent.setInsideFileTypes(insideFileTypes);
//        torrent.setTags(tags);
//        torrent.setOwlLinks(owlLink);
//        torrent.setChunkHashes(chunkHashes);
//        torrent.setNumberOfChunks(3);
//        torrent.setFileType("PATA nahi");
//        torrent.setFileName("Try and Error");
//        torrent.setFileHash("-100973364-591097238126-32-114-72-55-47-10042");
//        torrent.setCreatedOn("1234567890");
//        torrent.setAuthor("Kashyap");
//        torrent.setSize(123456);
//        String[] strings = searchEngineDB.getComments(torrent);
//        for(String s : strings)
//            System.out.println(s);
    }

    private void blockingFunction(Process p) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String line = "kashyap";
        log.debug("Waiting for python");
        while ((line = bufferedReader.readLine()) != null) {
            if (line.equalsIgnoreCase("waiting for client"))
                break;
        }
        log.debug("Done with python");
        new Thread(new Runnable() {
            @Override
            public void run() {
                String line = "";
                while (true) {
                    try {
                        if (!((line = bufferedReader.readLine()) != null)) break;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    System.out.println(line);
                }
            }
        }).start();
        //Thread.sleep(timeToWait);
    }

    /**
     * Creates ame pipe and executes python file.
     *
     * @return Search result for the given query
     */
    private String namePipe(String name, String query) {
        log.trace("Executing query: {}", query);
        String echoResponse = null;

        try {
            // Connect to the pipe
            String path = System.getProperty("user.dir") + "\\SearchEngine\\src\\main\\java\\python\\" + name + ".py";
            log.debug("Path of python script: " + path);
            Process p = Runtime.getRuntime().exec("python " + path);

            blockingFunction(p);
            if(name.equals("getComments"))
                Thread.sleep(200);
            RandomAccessFile pipe = new RandomAccessFile("\\\\.\\pipe\\" + name, "rw");

            String echoText = query;
            // write to pipe
            pipe.write(echoText.getBytes());
            // read response
            echoResponse = pipe.readLine();
            log.debug("DB response" + echoResponse);
            pipe.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return echoResponse;
    }

    public Torrent[] getSuggestions(Torrent[] torrents) throws Exception {
        log.debug("In getSuggestions");
        Set<String > tags = new HashSet<>(50);
        Set<String > fileHashes = new HashSet<>(50);
        for(Torrent t : torrents){
            fileHashes.add(t.getFileHash());
            for(String tag : t.getTags())
                tags.add(tag);
        }
        SearchQuery searchQuery = new SearchQuery();
        boolean[] filters = new boolean[6];
        filters[0]=filters[1]=filters[2]=filters[3]=filters[4]=filters[5]=false;
        filters[3]=true; //Search on tags
        searchQuery.setBooleans(filters);
        searchQuery.setTags(tags.toArray(new String[0]));
        Torrent[] results = getFile(searchQuery);

        List<Torrent> toReturn = new ArrayList<>(50);
        for(Torrent t : results){
            if(!fileHashes.contains(t.getFileHash())){
                toReturn.add(t);
            }
        }
        return toReturn.toArray(new Torrent[0]);
    }



    /**
     * Search Database for given file name and other parameters
     *
     * @return Torrents of matched files.
     */
    public Torrent[] getFile(SearchQuery fileName) throws Exception {

        /*
        NOTE: boolean[6] - represent what filters are applied
        1 fileName
        2 fileType
        3 insideFileType
        4 tags
        5 size ??idk about that much
        6 FileHash
        constrain pre defined: if(bool[5] == 1) then all will be zero and if(bool[0 to 4] == any 1) then bool[5] is zero
        */
        String funName = "getFile";
        String where = " ";
        if(fileName.getBooleans()[0]){
            where += "fileName: {_ilike: \"%" + fileName.getSearchText() + "%\"},";
        }
        if(fileName.getBooleans()[1]){
            where += "fileType: {_ilike: \"%" + fileName.getFileTypeText() + "%\"},";
        }
        if(fileName.getBooleans()[2]){
            String types= "[ ";
            for(String t : fileName.getInsideFileTypes()){
                types+="\""+t+"\", ";
            }
            types+=" ]";
            where+="inside_file_types: {type: {_in: "+types+"}},";
        }
        if(fileName.getBooleans()[3]){
            String tags= "[ ";
            for(String t : fileName.getTags()){
                tags+="\""+t+"\", ";
            }
            tags+=" ]";
            where+="tags: {tag: {_in: "+tags+"}},";
        }
        if(fileName.getBooleans()[4]){
            where += "size: {_lte: " + fileName.getSize() + "},";
        }
        if(fileName.getBooleans()[5]){
            where+="fileHash: {_ilike: \""+fileName.getFileHash()+"\"},";
        }
        String query = "{\n" +
                "  db_search_engine(order_by: {downloadsByFilehash_aggregate: {count: asc_nulls_last}}, where:  { "+where+"  }) {\n" +
                "    auther\n" +
                "    created_on\n" +
                "    no_of_chunks" +
                "    downloads_torrents\n" +
                "    fileHash\n" +
                "    fileName\n" +
                "    fileType\n" +
                "    inside_file_types {\n" +
                "      type\n" +
                "    }\n" +
                "    tags {\n" +
                "      tag\n" +
                "    }\n" +
                "    trackers {\n" +
                "      ip\n" +
                "      port\n" +
                "    }\n" +
                "    chunks {\n" +
                "      chunkHash\n" +
                "    }\n" +
                "    owl_links {\n" +
                "      ip\n" +
                "      port\n" +
                "    }\n" +
                "    size\n" +
                "  }\n" +
                "}\n";
        log.trace("Query: {}", query);
        String echoResponse = namePipe(funName, query);
        Torrent[] torrents = null;


        if (echoResponse != null) {
            JsonParser parser = new JsonParser();
            JsonArray element = (JsonArray) parser.parse(echoResponse);
            torrents = new Torrent[element.size()];

            int i = 0;
            for (JsonElement f : element) {
                JsonObject file = f.getAsJsonObject();
                if(fileLRU.readValue(file.get("fileHash").getAsString())!=null){
                    log.debug("Cache : file {}", file.get("fileHash").getAsString());
                    torrents[i]=fileLRU.getValue(file.get("fileHash").getAsString());
                    continue;
                }

                torrents[i] = new Torrent();
                torrents[i].setAuthor(file.get("auther").getAsString());
                torrents[i].setCreatedOn(file.get("created_on").getAsString());
                torrents[i].setFileHash(file.get("fileHash").getAsString());
                torrents[i].setFileName(file.get("fileName").getAsString());
                torrents[i].setFileType(file.get("fileType").getAsString());
                torrents[i].setNumberOfChunks(file.get("no_of_chunks").getAsInt());
                torrents[i].setSize(file.get("size").getAsInt());

                JsonArray cArray = file.get("chunks").getAsJsonArray();
                String[] chunkHash = new String[cArray.size()];
                int j = 0;
                for (JsonElement t : cArray) {
                    JsonObject to = t.getAsJsonObject();
                    chunkHash[j] = to.get("chunkHash").getAsString();
                    j++;
                }
                torrents[i].setChunkHashes(chunkHash);


                JsonArray trackerArray = file.get("owl_links").getAsJsonArray();
                OwlLink[] owls = new OwlLink[trackerArray.size()];
                j = 0;
                for (JsonElement t : trackerArray) {
                    owls[j] = new OwlLink();
                    //System.out.println(t);
                    JsonObject to = t.getAsJsonObject();
                    owls[j].setIp(to.get("ip").getAsString());
                    owls[j].setPort(to.get("port").getAsInt());
                    j++;
                }
                torrents[i].setOwlLinks(owls);

                JsonArray tags = file.get("tags").getAsJsonArray();
                String[] tag = new String[tags.size()];
                j = 0;
                for (JsonElement t : tags) {
                    JsonObject to = t.getAsJsonObject();
                    tag[j] = to.get("tag").getAsString();
                    j++;
                }
                torrents[i].setTags(tag);

                JsonArray inside_file_types = file.get("inside_file_types").getAsJsonArray();
                String[] inside_file = new String[inside_file_types.size()];
                j = 0;
                for (JsonElement t : inside_file_types) {
                    JsonObject to = t.getAsJsonObject();
                    inside_file[j] = to.get("type").getAsString();
                    j++;
                }
                torrents[i].setInsideFileTypes(inside_file);

                fileLRU.addValue(torrents[i].getFileHash(),torrents[i]);
                // TODO: 15-09-2019 add owls also in torrent --------------done already
                i++;
            }


        }
        if (torrents != null)
            return torrents;
        else
            throw new Exception("File not found");
    }

    /**
     * Checks if given file exist in database or not.
     *
     * @return if exists then true else false
     */
    public boolean fileExist(String fileHash) throws Exception {
        if(fileLRU.readValue(fileHash)!=null){
            log.debug("Cache : fileExist {}", fileHash);
            fileLRU.getValue(fileHash);
            return true;
        }

        String funName = "getFile";
        String echoResponse = namePipe(funName, "{\n" +
                "  db_search_engine(where: {fileHash: {_eq: \"" + fileHash + "\"}}) {\n" +
                "    fileHash\n" +
                "    \n" +
                "  }\n" +
                "}\n");

        if (echoResponse != null) {
            JsonParser parser = new JsonParser();
            JsonArray element = (JsonArray) parser.parse(echoResponse);
            return (element.get(0).getAsJsonObject().get("fileHash").getAsString().length() > 0);

        } else
            throw new Exception("File not found");
    }

    public OwlLink[] getOwlLinks() {

        String echoResponse = namePipe("getOwlLinks", "{\n" +
                "  owls {\n" +
                "    ip\n" +
                "    port\n" +
                "  }\n" +
                "}\n");

        JsonParser parser = new JsonParser();
        JsonArray element = (JsonArray) parser.parse(echoResponse);
        OwlLink[] owlLinks = new OwlLink[element.size()];

        int i = 0;
        for (JsonElement f : element) {
            JsonObject file = f.getAsJsonObject();

            OwlLink owlLink = new OwlLink();
            owlLink.setIp(file.get("ip").getAsString());
            owlLink.setPort((file.get("port")).getAsInt());
            owlLinks[i] = owlLink;

            i++;
        }

        return owlLinks;
    }

    private boolean addChunkInfo(String[] chunks, String fileHash) {
        boolean isDone = true;
        for (String chunk : chunks) {
            String echoResponse = namePipe("addChunkInfo", "mutation {\n" +
                    "  insert_chunks(objects: {chunkHash: \"" + chunk + "\", fileHash: \"" + fileHash + "\"}) {\n" +
                    "    returning {\n" +
                    "      chunkHash\n" +
                    "    }" +
                    "  }" +
                    "}");
            log.debug("updating db chunk: {}", chunk);
            if (echoResponse == null) {
                isDone = false;
                break;
            }
        }
        return isDone;
    }

    private boolean addInsideFileType(String[] insideFileTypes, String fileHash) {
        boolean isDone = true;
        for (String insideFileType : insideFileTypes) {
            String echoResponse = namePipe("addInsideFileType", "mutation {\n" +
                    "  insert_inside_file_type(objects: {fileHash: \"" + fileHash + "\", type: \"" + insideFileType + "\"}) {\n" +
                    "    affected_rows\n" +
                    "  }" +
                    "}");
            if (echoResponse == null) {
                isDone = false;
                break;
            }
        }
        return isDone;
    }

    private boolean insertDownloads(String fileHash) {
        String echoResponse = namePipe("insertDownloads", "mutation {\n" +
                "  insert_downloads(objects: {downloads: 0, fileHash: \"" + fileHash + "\"}) {\n" +
                "    affected_rows\n" +
                "  }\n" +
                "}\n");

        return (echoResponse != null);
    }
    private boolean addTags(String[] tags, String fileHash) {
        boolean isDone = true;
        for (String tag : tags) {
            String echoResponse = namePipe("addTags", "mutation {\n" +
                    "  insert_tags(objects: {fileHash: \"" + fileHash + "\", tag: \"" + tag + "\"}) {\n" +
                    "    returning {\n" +
                    "      tag\n" +
                    "    }" +
                    "  }" +
                    "}\n");
            if (echoResponse == null) {
                isDone = false;
                break;
            }
        }
        return isDone;
    }

    private boolean addOwlLinks(OwlLink[] owlLinks, String fileHash) {
        boolean isDone = true;
        for (OwlLink owlLink : owlLinks) {
            String echoResponse = namePipe("addOwlLinks", "mutation {\n" +
                    "insert_owl_links(objects: {FileHash: \"" + fileHash + "\", ip: \"" + owlLink.getIp() + "\", port: " + owlLink.getPort() + "}) {\n" +
                    "    returning {\n" +
                    "      ip" +
                    "      port\n" +
                    "       }" +
                    "   }" +
                    "}\n");
            if (echoResponse == null) {
                isDone = false;
                break;
            }
        }
        return isDone;
    }


    public boolean incDownloads(Torrent torrent){
        String echoResponse = namePipe("incDownloads","mutation {\n" +
                "  update_downloads(where: {fileHash: {_eq: \""+torrent.getFileHash()+"\"}}, _inc: {downloads: 1}) {\n" +
                "    affected_rows\n" +
                "  }\n" +
                "}\n");

        return (echoResponse!=null);
    }

    public boolean addInfo(Torrent torrent, int number_of_downloads, int stage) {
        if (stage == 1) {
            String echoResponse = namePipe("addInfo", "mutation {\n" +
                    "  insert_db_search_engine(objects: {auther: \"" + torrent.getAuthor() + "\", created_on: \"" + torrent.getAuthor() + "\", fileHash: \"" + torrent.getFileHash() + "\", fileName: \"" + torrent.getFileName() + "\", fileType: \"" + torrent.getFileType() + "\", no_of_chunks: " + torrent.getNumberOfChunks() + ", size: " + torrent.getSize() + "}) {\n" +
                    "    returning {\n" +
                    "      fileHash\n" +
                    "    }" +
                    "   }" +
                    "}");

            if (echoResponse != null) {
                log.debug("File info added");
            } else {
                log.error("failed to add basic file info");
                return false;
            }
        } else if (stage == 2) {
            if (addChunkInfo(torrent.getChunkHashes(), torrent.getFileHash()))
                log.debug("ChunkInfo added");
            else {
                log.error("Failed to add chunk info");
                return false;
            }

            if (addInsideFileType(torrent.getInsideFileTypes(), torrent.getFileHash()))
                log.debug("Inside file type added");
            else {
                log.error("Failed to add inside file type");
                return false;
            }

            if (addOwlLinks(torrent.getOwlLinks(), torrent.getFileHash()))
                log.debug("Owl links added");
            else {
                log.error("Failed to add owl links");
                return false;
            }

            if (addTags(torrent.getTags(), torrent.getFileHash()))
                log.debug("Tags added");
            else {
                log.error("Failed to add tags");
                return false;
            }
            if (insertDownloads(torrent.getFileHash())) {
                log.debug("Downloads added");
            } else {
                log.error("Failed to add downloads");
                return false;
            }
            // TODO: 19-09-2019 add  
        }
        fileLRU.addValue(torrent.getFileHash(),torrent);
        return true;
    }

    public Integer getDownloads(String fileHash){
        String echoResponse = namePipe("getDownload","{\n" +
                "  downloads(where: {fileHash: {_eq: \""+fileHash+"\"}}) {\n" +
                "    downloads\n" +
                "  }\n" +
                "}");

        JsonParser jsonParser = new JsonParser();
        JsonArray jsonArray = (JsonArray) jsonParser.parse(echoResponse);
        JsonElement jsonElement = jsonArray.get(0);
        return jsonElement.getAsJsonObject().get("downloads").getAsInt();

    }


    public boolean addComment(String comment, Torrent torrent, String userName){
        String echoResponse = namePipe("addComment","mutation {\n" +
                "  insert_comments(objects: {comment: \""+comment+"\", email: \""+userName+"\", fileHash: \""+torrent.getFileHash()+"\"}) {\n" +
                "    affected_rows\n" +
                "  }\n" +
                "}\n");

        return echoResponse!=null;
    }

    public String[] getComments(Torrent torrent) {

        String echoResponse = namePipe("gc", "{\n" +
                "  comments(where: {fileHash: {_eq: \""+torrent.getFileHash()+"\"}}) {\n" +
                "    comment\n" +
                "  }\n" +
                "}\n");

        JsonParser parser = new JsonParser();
        JsonArray jsonArray = (JsonArray) parser.parse(echoResponse);
        List<String > comments = new ArrayList<>(jsonArray.size());
        for(JsonElement c : jsonArray){
            JsonObject o = c.getAsJsonObject();
            comments.add(o.get("comment").getAsString());
        }
        return comments.toArray(new String[0]);
    }
}
/*
mutation {
  insert_db_search_engine(objects: {auther: "", created_on: "", downloads: 10, fileHash: "", fileName: "", fileType: "", no_of_chunks: 10, size: 10}) {
    affected_rows
  }
  insert_chunks(objects: {chunkHash: "", fileHash: ""}) {
    affected_rows
  }
  insert_inside_file_type(objects: {fileHash: "", type: ""}) {
    affected_rows
  }
  insert_owl_links(objects: {FileHash: "", ip: "", port: 10}) {
    affected_rows
  }
}

 */