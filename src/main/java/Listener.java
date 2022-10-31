import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.json.simple.JSONArray;

public class Listener {

    private ServerSocket serverSocket;
    private Socket socket;
    private static ArrayList<ListenerThread> connectedPeers = new ArrayList<>();
    private static ExecutorService pool = Executors.newFixedThreadPool(5);

    public Listener() throws IOException {
        this.serverSocket = new ServerSocket(18018);
        this.socket = new Socket();
    }

    public ServerSocket getServerSocket() {
        return serverSocket;
    }

    public Socket getSocket() {
        return socket;
    }

    public JSONArray peers() throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader("Dukat/Peers"));
        JSONArray peers = new JSONArray();

        String line = reader.readLine();
        while (line != null) {
            peers.add(line);
            line = reader.readLine();
        }
        reader.close();
        return peers;
    }

    public static void main(String[] args) throws IOException {
        Listener listener = new Listener();

        try {
            while (true) {
                listener.socket = listener.serverSocket.accept();

                ListenerThread peerThread = new ListenerThread(listener);
                connectedPeers.add(peerThread);
                pool.execute(peerThread);
            }
        } finally {
            listener.getServerSocket().close();
            listener.getSocket().close();
        }

    }

}
