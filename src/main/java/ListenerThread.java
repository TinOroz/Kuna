import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.regex.Pattern;

public class ListenerThread implements Runnable {

    private Listener listener;
    private BufferedReader in;
    private PrintWriter out;

    public ListenerThread(Listener listener) throws IOException {
        this.listener = listener;
        this.out = new PrintWriter(listener.getSocket().getOutputStream());
        this.in = new BufferedReader(new InputStreamReader(listener.getSocket().getInputStream()));
    }

    @Override
    public void run() {
        try {
            String receivedMessage = in.readLine();
            System.out.println(receivedMessage);

            if (!Handshake.listenerHandshake(listener, receivedMessage, out)) return;

            try {
                while (true) {
                    receivedMessage = in.readLine();
                    if (receivedMessage == null) break;

                    System.out.println(receivedMessage);
                    JSONObject receivedMessageJSON = (JSONObject) JSONValue.parse(receivedMessage);

                    if (receivedMessageJSON.get("type").equals("getpeers")) {
                        JSONObject message = new JSONObject();

                        message.put("type", "peers");
                        message.put("peers", listener.peers());

                        out.println(message);
                        out.flush();
                        break;
                    } else {
                        Error.sendError(listener.getSocket(),"Unsupported message type received");
                    }
                }
            } finally {
                out.close();
                in.close();
            }
        } catch (IOException e) {
            System.err.println(e.getStackTrace());
        }

    }
}
