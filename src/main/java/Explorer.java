import com.google.gson.Gson;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.io.*;
import java.net.Socket;
import java.util.HashSet;
import java.util.Properties;

import java.lang.reflect.Type;
import com.google.gson.reflect.TypeToken;

public class Explorer {

    private Socket socket;

    public Explorer() throws IOException {
        FileReader reader = new FileReader("Dukat/bootstrappingNode.properties");
        Properties properties = new Properties();
        properties.load(reader);

        String IP = properties.getProperty("IP");
        String Port = properties.getProperty("Port");
        reader.close();

        this.socket = new Socket("localhost", Integer.parseInt(Port));
        //this.socket = new Socket(IP, Integer.parseInt(Port));
    }

    public Socket getSocket() {
        return socket;
    }

    private void getPeers(Explorer explorer, PrintWriter out, BufferedReader buffer) throws IOException {
        JSONObject message = new JSONObject();
        message.put("type", "getpeers");
        out.println(message);
        out.flush();

        String serverResponse = buffer.readLine();

        System.out.println(serverResponse);
        JSONObject serverResponseJSON = (JSONObject) JSONValue.parse(serverResponse);

        if (serverResponseJSON.keySet().size() != 2) {
            Error.sendError(explorer.socket,"Wrong Protocol: " + serverResponseJSON);
            return;
        }
        for (Object key : serverResponseJSON.keySet()) {
            String keyString = key.toString();
            if (!(keyString.equals("type") || keyString.equals("peers"))) {
                Error.sendError(explorer.socket,"Wrong Protocol: " + serverResponseJSON);
                return;
            }
        }
        if (!serverResponseJSON.get("type").equals("peers")) {
            Error.sendError(explorer.socket,"Unsupported message type received");
            return;
        }

        Type listType = new TypeToken<HashSet<String>>() {}.getType();
        HashSet<String> peerList = new Gson().fromJson(serverResponseJSON.get("peers").toString(), listType);


        BufferedReader alreadyKnownPeersReader = new BufferedReader(new FileReader("Dukat/Peers"));
        HashSet<String> alreadyKnownPeers = new HashSet<>();

        String line = alreadyKnownPeersReader.readLine();
        while (line != null) {
            alreadyKnownPeers.add(line);
            line = alreadyKnownPeersReader.readLine();
        }
        for (String peer : alreadyKnownPeers) {
            System.out.println(peer);
        }

        BufferedWriter peersWriter = new BufferedWriter(new FileWriter("Dukat/Peers", true));
        for (String peer : peerList) {
            if(alreadyKnownPeers.add(peer)) {
                System.out.println(peer);
                peersWriter.write(peer + "\n");
            }
        }
        alreadyKnownPeersReader.close();
        peersWriter.close();
    }

    public static void main(String[] args) throws IOException {
        Explorer explorer = new Explorer();

        PrintWriter out = new PrintWriter(explorer.socket.getOutputStream());
        BufferedReader in = new BufferedReader(new InputStreamReader(explorer.socket.getInputStream()));
        BufferedReader keyboard = new BufferedReader(new InputStreamReader(System.in));

        if(!Handshake.explorerHandshake(explorer, out, in)) return;

        while (true) {
            System.out.println("> ");
            String command = keyboard.readLine();

            if (command.equals("exit")) {
                break;
            } else if (command.equals("getpeers")) {
                explorer.getPeers(explorer, out, in);
            } else {
                System.out.println("Not recognizable command: " + command);
            }
        }
        out.close();
        in.close();
        keyboard.close();
    }
}
