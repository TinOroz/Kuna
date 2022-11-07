import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Test {

    // make handshake instance
    private static Handshake handshake = new Handshake();


    public static void main(String[] args) throws IOException {
        String serverIP = "0.0.0.0";
        int serverPort = 18018;

        Socket socket = new Socket(serverIP, serverPort);

        System.out.println("Connected to " + serverIP + " on port " + serverPort);

        // make buffered reader and print writer for socket
        BufferedReader receiver = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        PrintWriter sender = new PrintWriter(socket.getOutputStream(), true);

        // send handshake
        sender.println(handshake.getErrorMessage());
        sender.println(handshake.getPeersMessage());
        sender.flush();

        while(true) {
            String receivedMessage = receiver.readLine();
            if(receivedMessage != null) {
                System.out.println(receivedMessage);
            }
        }

    }
}
