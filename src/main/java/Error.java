import org.json.simple.JSONObject;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

public final class Error {
    public static void sendError(Socket socket, String cause) throws IOException {
        JSONObject message = new JSONObject();
        message.put("type", "error");
        message.put("error", cause);

        PrintWriter out = new PrintWriter(socket.getOutputStream());
        out.println(message);
        out.flush();
        System.err.println(message);
    }
}
