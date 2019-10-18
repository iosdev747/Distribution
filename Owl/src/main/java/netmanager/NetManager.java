package netmanager;

import com.google.gson.Gson;
import container.Packet;
import container.Seeder;
import container.call.AddClientRequestTemplate;
import container.call.GetStrings;
import lombok.extern.slf4j.Slf4j;
import net.manager.NetManagerService;
import owl.Owl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

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
            if (requestPacket.call.equals("Owl.getDownloadLink")) {
                String chunkHash = gson.fromJson(requestPacket.data, String.class);
                String[] downloadLinks = Owl.getOwl().getDownloadLinks(chunkHash);
                GetStrings strings = new GetStrings();
                strings.setStrings(downloadLinks);
                responsePacket.data = gson.toJson(strings, GetStrings.class);
            } else if (requestPacket.call.equals("Owl.addClient")) {
                Seeder seeder = gson.fromJson(requestPacket.data, Seeder.class);
                Boolean isAdded = Owl.getOwl().addClient(seeder);
                responsePacket.data = gson.toJson(isAdded, Boolean.class);
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
