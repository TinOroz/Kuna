import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

public final class Handshake {

    private static boolean messageChecker(Socket socket, String message) throws IOException {
        JSONObject messageJSON = (JSONObject) JSONValue.parse(message);

        if (messageJSON.keySet().size() != 3 && messageJSON.keySet().size() != 2) {
            Error.sendError(socket,"Unsupported protocol message received: " + messageJSON);
            return false;
        }
        for (Object key : messageJSON.keySet()) {
            String keyString = key.toString();
            if (!(keyString.equals("type") || keyString.equals("version") || keyString.equals("agent"))) {
                Error.sendError(socket,"Unsupported protocol message received: " + keyString);
                return false;
            }
        }
        if (!messageJSON.get("type").equals("hello")) {
            Error.sendError(socket, "Received message: " + messageJSON + " prior to \\hello\\");
            return false;
        } else if (!messageJSON.get("version").toString().startsWith("0.8")) {
            Error.sendError(socket, "Unsupported message version received");
            return false;
        }
        return true;
    }

    private static JSONObject helloMessage() {
        JSONObject helloMessage = new JSONObject();
        helloMessage.put("type", "hello");
        helloMessage.put("version", "0.8.0");
        helloMessage.put("agent", "Kerma−Core Client 0.8");
        return helloMessage;
    }

    public static boolean listenerHandshake(Listener listener, String message, PrintWriter out) throws IOException {
        if (message == null) {
            Error.sendError(listener.getSocket(), "Wrong Protocol. Message is: " + message);
            return false;
        }
        if (!messageChecker(listener.getSocket(), message)) return false;

        out.println(helloMessage());
        out.flush();
        return true;
    }

    public static boolean explorerHandshake(Explorer explorer, PrintWriter out, BufferedReader in) throws IOException {
        out.println(helloMessage());
        out.flush();

        String message = in.readLine();
        System.out.println(message);
        for (int i = 0; i < 3; i++) {
            System.out.println(in.readLine());
        }

        if (message == null) {
            Error.sendError(explorer.getSocket(),"Wrong Protocol. Message is: " + message);
            return false;
        }
        if (!messageChecker(explorer.getSocket(), message)) return false;

        return true;
    }
}
