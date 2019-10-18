package netmanager;

import admin.AdminServer;
import com.google.gson.Gson;
import container.Packet;
import container.User;
import container.UserCreds;
import container.call.AddNewFileRequestTemplate;
import container.call.GetStrings;
import container.call.InsertMyFileRequestTemplate;
import lombok.extern.slf4j.Slf4j;
import net.manager.NetManagerService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

@Slf4j
public class NetManager extends NetManagerService {

    private static NetManager netManager;

    private NetManager() {
        super(1111);
    }

    public static NetManager getNetManager() {
        if (netManager == null) {
            netManager = new NetManager();
        }
        return netManager;
    }

    @Override
    public void recieve(Socket clientSocket) {
        new Thread(this).start();
        String httpResponse = null;
        httpResponse = "";

        InputStreamReader isr = null;
        BufferedReader reader = null;
//        int c = 0;        //c is the parsed object
        //use above reader to parse json and put it in object
        try {
            isr = new InputStreamReader(clientSocket.getInputStream());
            reader = new BufferedReader(isr);
            String request = "";
            while (true) {
                int temp = reader.read();
                if (temp == (int) '\0') {
                    break;
                }
                request += (char) temp;
            }
//            c = request.length();
            log.info("received request:{}", request);
            Gson gson = new Gson();
            Packet requestPacket = gson.fromJson(request, Packet.class);
            Packet responsePacket = new Packet();
            if (requestPacket.call.equals("AdminServer.authUser")) {
                UserCreds userCreds = gson.fromJson(requestPacket.data, UserCreds.class);
                Boolean isAuth = AdminServer.getAdminServer().authUser(userCreds);
                responsePacket.data = gson.toJson(isAuth, Boolean.class);
            } else if (requestPacket.call.equals("AdminServer.createNewAccount")) {
                User user = gson.fromJson(requestPacket.data, User.class);
                Boolean isCreated = AdminServer.getAdminServer().createNewAccount(user);
                responsePacket.data = gson.toJson(isCreated, Boolean.class);
            } else if (requestPacket.call.equals("AdminServer.getUser")) {
                UserCreds userCreds = gson.fromJson(requestPacket.data, UserCreds.class);
                User user = AdminServer.getAdminServer().getUser(userCreds);
                responsePacket.data = gson.toJson(user, User.class);
            } else if (requestPacket.call.equals("AdminServer.insertMyFile")) {
                InsertMyFileRequestTemplate requestTemplate = gson.fromJson(requestPacket.data, InsertMyFileRequestTemplate.class);
                Boolean isInserted = AdminServer.getAdminServer().insertMyFile(requestTemplate.emailId, requestTemplate.fileHash, requestTemplate.fileName);
                responsePacket.data = gson.toJson(isInserted, Boolean.class);
            } else if (requestPacket.call.equals("AdminServer.getMyFileChunks")) {
                UserCreds userCreds = gson.fromJson(requestPacket.data, UserCreds.class);
                String[] myFileChunks = AdminServer.getAdminServer().getMyFileChunks(userCreds);
                GetStrings getStrings = new GetStrings();
                getStrings.setStrings(myFileChunks);
                responsePacket.data = gson.toJson(getStrings, GetStrings.class);
            } else if (requestPacket.call.equals("AdminServer.getSearchEngines")) {
                String[] searchEngines = AdminServer.getAdminServer().getSearchEngines();
                GetStrings getStrings = new GetStrings();
                getStrings.setStrings(searchEngines);
                responsePacket.data = gson.toJson(getStrings, GetStrings.class);
            } else if (requestPacket.call.equals("AdminServer.addNewFile")) {
                AddNewFileRequestTemplate requestTemplate = gson.fromJson(requestPacket.data, AddNewFileRequestTemplate.class);
                Boolean isAdded = AdminServer.getAdminServer().addNewFile(requestTemplate.torrentFile, requestTemplate.sourceIP, requestTemplate.sourcePort, requestTemplate.searchEngines);
                responsePacket.data = gson.toJson(isAdded, Boolean.class);
            }
            httpResponse += gson.toJson(responsePacket);
        } catch (IOException e) {
            log.error("Unable to read request");
            e.printStackTrace();
            Packet responsePacket = new Packet();
            responsePacket.call = "Return.Error";
            responsePacket.data = "";
            httpResponse += new Gson().toJson(responsePacket);
        } catch (Exception e) {
            log.error("Unable to process request, might be issue with JSON parsing");
            e.printStackTrace();
            Packet responsePacket = new Packet();
            responsePacket.call = "Return.Error";
            responsePacket.data = "";
            httpResponse += new Gson().toJson(responsePacket);
        }
        try {
            log.info("Sending response " + httpResponse + "\r\n\r\n");
//            new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream())).write(httpResponse);
            httpResponse += '\0';
            clientSocket.getOutputStream().write(httpResponse.getBytes("UTF-8"));
        } catch (IOException e) {
            log.error("Unable to send response");
        }
    }

    @Override
    public void start() {

    }
}