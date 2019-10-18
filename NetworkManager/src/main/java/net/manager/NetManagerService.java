package net.manager;

import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.net.ConnectException;
import java.net.ServerSocket;
import java.net.Socket;

@Slf4j
public abstract class NetManagerService implements INetManager, Runnable {
    ServerSocket serverSocket;
    int port;

    protected NetManagerService(int port) {
        this.port = port;
        try {
            log.info("Starting server at {}", port);
            serverSocket = new ServerSocket(port);
            log.info("Server started successfully");
        } catch (IOException e) {
            log.error("Unable to start server socket at port " + port);
            e.printStackTrace();
            System.exit(4);
        }
        log.info("Server Initialized");
        new Thread(this).start();
    }

    public boolean ping(String ip, int port) {
        //ping and return
        return true;
        // TODO: 10-09-2019 try to use system ping instead, and remove port from parameter
    }

//    public void recieve(Socket clientSocket) {
//        new Thread(this).start();
//        String httpResponse = null;
//        httpResponse = "";
//
//        InputStreamReader isr = null;
//        BufferedReader reader = null;
////        int c = 0;        //c is the parsed object
//        //use above reader to parse json and put it in object
//        try {
//            isr = new InputStreamReader(clientSocket.getInputStream());
//            reader = new BufferedReader(isr);
//            String request = "";
//            while (true) {
//                int temp = reader.read();
//                if (temp == (int) '\0') {
//                    break;
//                }
//                request += (char) temp;
//            }
////            c = request.length();
//            log.info("received request:{}", request);
//            Gson gson = new Gson();
//            Packet requestPacket = gson.fromJson(request, Packet.class);
//            Packet responsePacket = new Packet();
//            if (requestPacket.call.equals("AdminServer.authUser")) {
//                UserCreds userCreds = gson.fromJson(requestPacket.data, UserCreds.class);
//                Boolean isAuth = AdminServer.getAdminServer().authUser(userCreds);
//                responsePacket.data = gson.toJson(isAuth, Boolean.class);
//            } else if (requestPacket.call.equals("AdminServer.createNewAccount")) {
//                User user = gson.fromJson(requestPacket.data, User.class);
//                Boolean isCreated = AdminServer.getAdminServer().createNewAccount(user);
//                responsePacket.data = gson.toJson(isCreated, Boolean.class);
//            } else if (requestPacket.call.equals("AdminServer.getUser")) {
//                UserCreds userCreds = gson.fromJson(requestPacket.data, UserCreds.class);
//                User user = AdminServer.getAdminServer().getUser(userCreds);
//                responsePacket.data = gson.toJson(user, User.class);
//            } else if (requestPacket.call.equals("AdminServer.insertMyFile")) {
//                InsertMyFileRequestTemplate requestTemplate = gson.fromJson(requestPacket.data, InsertMyFileRequestTemplate.class);
//                Boolean isInserted = AdminServer.getAdminServer().insertMyFile(requestTemplate.emailId, requestTemplate.fileHash, requestTemplate.fileName);
//                responsePacket.data = gson.toJson(isInserted, Boolean.class);
//            } else if (requestPacket.call.equals("AdminServer.getMyFileChunks")) {
//                UserCreds userCreds = gson.fromJson(requestPacket.data, UserCreds.class);
//                String[] myFileChunks = AdminServer.getAdminServer().getMyFileChunks(userCreds);
//                responsePacket.data = gson.toJson(myFileChunks, GetStrings.class);
//            } else if (requestPacket.call.equals("AdminServer.getSearchEngines")) {
//                String[] myFileChunks = AdminServer.getAdminServer().getSearchEngines();
//                responsePacket.data = gson.toJson(myFileChunks, GetStrings.class);
//            } else if (requestPacket.call.equals("AdminServer.addNewFile")) {
//                AddNewFileRequestTemplate requestTemplate = gson.fromJson(requestPacket.data, AddNewFileRequestTemplate.class);
//                Boolean isAdded = AdminServer.getAdminServer().addNewFile(requestTemplate.torrentFile, requestTemplate.sourceIP, requestTemplate.sourcePort, requestTemplate.searchEngines);
//                responsePacket.data = gson.toJson(isAdded, Boolean.class);
//            } else if (requestPacket.call.equals("SearchEngine.fileExist")) {
//                String fileHash = gson.fromJson(requestPacket.data, String.class);
//                Boolean isExist = SearchEngine.getSearchEngine().fileExist(fileHash);
//                responsePacket.data = gson.toJson(isExist, Boolean.class);
//            } else if (requestPacket.call.equals("SearchEngine.addNewFile")) {
//                AddNewFileRequestSearchEngineTemplate template = gson.fromJson(requestPacket.data, AddNewFileRequestSearchEngineTemplate.class);
//                Boolean isAdded = SearchEngine.getSearchEngine().addNewFile(template.torrent, template.ip, template.port);
//                responsePacket.data = gson.toJson(isAdded, Boolean.class);
//            } else if (requestPacket.call.equals("SearchEngine.searchByFileName")) {
//                String fileName = gson.fromJson(requestPacket.data, String.class);
//                Torrent[] torrents = SearchEngine.getSearchEngine().searchByFileName(fileName);
//                responsePacket.data = gson.toJson(torrents, TorrentArrayTemplate.class);
//            } else if (requestPacket.call.equals("Owl.getDownloadLinks")) {
//                String chunkHash = gson.fromJson(requestPacket.data, String.class);
//                String[] downloadLinks = Owl.getOwl().getDownloadLinks(chunkHash);
//                responsePacket.data = gson.toJson(downloadLinks, GetStrings.class);
//            } else if (requestPacket.call.equals("Owl.addClient")) {
//                AddClientRequestTemplate requestTemplate = gson.fromJson(requestPacket.data, AddClientRequestTemplate.class);
//                Boolean isAdded = Owl.getOwl().addClient(requestTemplate.seeder, requestTemplate.chunkHash);
//                responsePacket.data = gson.toJson(isAdded, Boolean.class);
//            } else if (requestPacket.call.equals("Client.chunkExist")) {
//                String chunkHashWithParentName = gson.fromJson(requestPacket.data, String.class);
//                Boolean chunkExist = Client.getClient().chunkExist(chunkHashWithParentName);
//                responsePacket.data = gson.toJson(chunkExist, Boolean.class);
//            }
//            httpResponse += gson.toJson(responsePacket);
//        } catch (IOException e) {
//            log.error("Unable to read request");
//            Packet responsePacket = new Packet();
//            responsePacket.call = "Return.Error";
//            responsePacket.data = "";
//            httpResponse += new Gson().toJson(responsePacket);
//        } catch (Exception e) {
//            log.error("Unable to process request");
//            Packet responsePacket = new Packet();
//            responsePacket.call = "Return.Error";
//            responsePacket.data = "";
//            httpResponse += new Gson().toJson(responsePacket);
//        }
//        try {
//            log.info("Sending response " + httpResponse + "\r\n\r\n");
////            new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream())).write(httpResponse);
//            httpResponse += '\0';
//            clientSocket.getOutputStream().write(httpResponse.getBytes("UTF-8"));
//        } catch (IOException e) {
//            log.error("Unable to send response");
//        }
//    }

    public abstract void recieve(Socket clientSocket);

    // TODO: 02-09-2019 append '\0' at last in requestJson whoever calls it
    public String send(String requestJson, String ip, int port) throws IOException {
        log.info("sending {}", requestJson);
        log.info("ip:{} and port:{}", ip, port);
        // TODO: 15-09-2019 handle "java.net.ConnectException: Connection refused: connect" on below line
        Socket socket = new Socket(ip, port);
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        writer.write(requestJson+'\0');
        writer.flush();
        BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        String responseJson = "";
        while (true) {
            int temp = reader.read();
            if (temp == (int) '\0') {
                break;
            }
            responseJson += (char) temp;
        }
        log.info("received response Json: {}", responseJson);
        return responseJson;
    }

    public void run() {
        Socket clientSocket = null;
        try {
            log.info("Waiting for client");
            clientSocket = serverSocket.accept();
            log.info("Client connected " + clientSocket.getInetAddress());
            recieve(clientSocket);
        } catch (IOException e) {
            log.error("Unable to accept socket");
            clientSocket = null;
        }
    }

    public void close() {
        try {
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public abstract void start();
}

/*

String httpResponse = null;
            try {
                httpResponse = "HTTP/1.1 200 OK\r\n\r\n" + recieve(clientSocket);
            } catch (IOException e) {
                httpResponse = "HTTP/1.1 500 Internal Server Error\r\n\r\n";
                log.error("Unable to read request");
//                e.printStackTrace();
            }
            try {
                clientSocket.getOutputStream().write(httpResponse.getBytes("UTF-8"));
            } catch (IOException e) {
                log.error("Unable to send response");
//                e.printStackTrace();
            }


 */
