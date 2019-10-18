package netmanager;

import client.Client;
import client.DefaultClient;
import com.google.gson.Gson;
import container.DownloadState;
import container.File;
import container.Packet;
import container.call.AddNewFileRequestSearchEngineTemplate;
import lombok.extern.slf4j.Slf4j;
import net.manager.NetManagerService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

/**
 * <code>NetManager</code> class is responsible for handling calls and managing incoming clients.
 */
@Slf4j
public class NetManager extends NetManagerService {

    private static NetManager netManager;

    private NetManager(int port) {
        super(port);
    }

    public static NetManager getNetManager(int port) {
        if (netManager == null) {
            netManager = new NetManager(port);
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
            if (requestPacket.call.equals("Client.chunkExist")) {
                String chunkHashWithParentName = gson.fromJson(requestPacket.data, String.class);
                Boolean chunkExist = Client.getClient().chunkExist(chunkHashWithParentName);
                responsePacket.data = gson.toJson(chunkExist, Boolean.class);
            } else if (requestPacket.call.equals("DefaultClient.download")) {
                AddNewFileRequestSearchEngineTemplate template = gson.fromJson(requestPacket.data, AddNewFileRequestSearchEngineTemplate.class);
                Boolean isDownloading = DefaultClient.getDefaultClient().download(template.torrent, template.ip, template.port);
                responsePacket.data = gson.toJson(isDownloading, Boolean.class);
            }
            httpResponse += gson.toJson(responsePacket);
        } catch (IOException e) {
            log.error("Unable to read request");
            Packet responsePacket = new Packet();
            responsePacket.call = "Return.Error";
            responsePacket.data = "";
            httpResponse += new Gson().toJson(responsePacket);
        } catch (Exception e) {
            log.error("Unable to process request");
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
