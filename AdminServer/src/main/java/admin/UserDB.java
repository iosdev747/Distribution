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

@Slf4j
@Getter
@Setter
public class UserDB {
    private long timeToWait=150;

    public static void main(String[] args) throws Exception {
        UserDB userDB = new UserDB();

        System.out.println(userDB.addFile("kashyapnasit@gmail.com","XXX","File2"));

    }

    /**
     * @param user         user object to return
     * @param pass         Password Hash for user username.
     * @param echoResponse response of database
     *                     <p>
     *                     Helper function which fill all the details of search result in user object
     * @return null if result is null else details in form of user object
     */
    private User getUser(String pass, String echoResponse, User user) throws Exception {
        if(echoResponse != null){
            JsonParser parser = new JsonParser();
            JsonElement element = parser.parse(echoResponse);
            if (element.isJsonObject()) {
                JsonObject u = element.getAsJsonObject();
                //System.out.println(u.get("name").getAsString());

                user = new User();
                try {
                    user.setName(u.get("name").getAsString());
                } catch (NullPointerException e) {
                    log.error("Name not selected");
                }
                try {
                    user.setEmail(u.get("email").getAsString());
                } catch (NullPointerException e) {
                    log.error("Email not selected");
                }
                try {
                    user.setPassHash(u.get("passHash").getAsString());
                } catch (NullPointerException e) {
                    log.error("passHash not selected");
                }
                try {
                    user.setDOB(u.get("DOB").getAsString());
                } catch (NullPointerException e) {
                    log.error("DOB not selected");
                }
                try {
                    user.setUsrname(u.get("usrname").getAsString());
                } catch (NullPointerException e) {
                    log.error("usrname not selected");
                }
                try{
                    JsonArray fileList = u.get("fileLists").getAsJsonArray();
                    String[] fileNames = new String[fileList.size()];
                    int i = 0;
                    for (JsonElement f : fileList) {
                        JsonObject file = f.getAsJsonObject();
                        String name = file.get("fileName").getAsString();
                        String hash = file.get("fileHash").getAsString();
                        fileNames[i++]=name+"^"+hash;
                    }
                    user.setFileHash(fileNames);
                } catch (NullPointerException e) {
                    log.error("Filelist not selected");
                }
            }

        }
        if(user!=null && user.getPassHash().compareTo(pass)==0)
            return user;
        else
            throw new Exception("User not found");
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
        log.trace("Executing query : " + query);
        String echoResponse = null;
        try {
            // Connect to the pipe
            String path = System.getProperty("user.dir") + "\\AdminServer\\src\\main\\java\\python\\" + funName + ".py";
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

    /**
     * @param usrname Username Of the person
     * @param pass    Password Hash for user username.
     *                Checks if user with username usrname and password 'pass' exist or not
     * @return if user exist return details of that user
     */
    public User getUserDataWithUsrname(String usrname, String pass) throws Exception {
        String echoResponse = namePipe("getUserDataWithUsrname", "{\n" +
                "  user(where: {usrname: {_eq: \"" + usrname + "\"}}) {\n" +
                "    name\n" +
                "    passHash\n" +
                "    email\n" +
                "    usrname\n" +
                "    DOB\n" +
                "    fileLists {\n" +
                "      fileName\n" +
                "      fileHash\n" +
                "    }" +
                "  }\n" +
                "}\n");
        User user = null;

        return getUser(pass, echoResponse, user);
    }

    /**
     * @param email Email Of the person
     * @param pass  Password Hash for user username.
     *              Checks if user with email 'email'' and password 'pass' exist or not
     * @return if user exist return details of that user
     */
    public User getUserDataWithEmail(String email, String pass) throws Exception {

        String echoResponse = namePipe("getUserDataWithEmail", "{\n" +
                "  user(where: {usrname: {_ilike: \"" + email + "\"}}) {\n" +
                "    name\n" +
                "    passHash\n" +
                "    email\n" +
                "    usrname\n" +
                "    DOB\n" +
                "    fileLists {\n" +
                "      fileName\n" +
                "      fileHash\n" +
                "    }\n" +
                "  }\n" +
                "}");
        User user = null;

        return getUser(pass, echoResponse, user);
    }

    /**
     * @param email Email Of the person
     * @param pass  Password Hash for user username.
     *              Checks if user with email 'email' and password 'pass' exist or not
     * @return if user exist return true
     * else false     *
     */
    public boolean userAuth(String email, String pass) {

        String echoResponse = namePipe("userAuth", "{\n" +
                "  user(where: {email: {_eq: \"" + email + "\"}, passHash: {_eq: \"" + pass + "\"}}) {\n" +
                "    name\n" +
                "    passHash\n" +
                "    email\n" +
                "    usrname\n" +
                "    DOB\n" +
                "  }\n" +
                "}\n");

        JsonParser parser = new JsonParser();
        JsonElement element = parser.parse(echoResponse);

        return element.isJsonObject();

    }

    /**
     * @param user Details of new user
     *             <p>
     *             Add the details of new user to database
     * @return if added successfully then return true else return false
     */
    public boolean addUser(User user) {

        String echoResponse = namePipe("addUser", "mutation {\n" +
                "  insert_user(objects: {DOB: \"" + user.getDOB() + "\", email: \"" + user.getEmail() + "\", name: \"" + user.getName() + "\", passHash: \"" + user.getPassHash() + "\", usrname: \"" + user.getUsrname() + "\"}) {\n" +
                "    returning {\n" +
                "      email      \n" +
                "    }\n" +
                "  }\n" +
                "}\n");


        return (echoResponse != null);
    }

    public boolean addFile(String email, String fileHash, String fileName) {

        String echoResponse = namePipe("addFile", "mutation {\n" +
                "  insert_fileList(objects: {email: \"" + email + "\", fileName: \"" + fileName + "\", fileHash: \"" + fileHash + "\"}) {\n" +
                "    returning {\n" +
                "      email      \n" +
                "    }\n" +
                "  }\n" +
                "}\n");

        return echoResponse != null;

    }

}

