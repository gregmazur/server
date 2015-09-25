package mypack;

import java.io.IOException;
import java.net.ServerSocket;

/**
 * Created by greg on 25.09.15.
 */
public class Main {
    public static boolean isOn = false;
    public static boolean connectionNeeded = true;
    public static boolean closing = false;

    public static void main(String[] args) throws IOException {
        ServerWindow serverWindow = new ServerWindow();
        ServerSocket serverSocket = new ServerSocket(9001);
        Server server;
        serverWindow.start();
        while (true) {
            if (!isOn) {
                if (connectionNeeded) {
                    serverWindow.chat.append("Server started\n");
                    server = new Server(serverWindow, serverSocket);
                    server.start();
                    connectionNeeded = false;
                    isOn = true;
                }
            } else {
                if (closing) {
                    serverWindow.chat.append("Server shutdown\n");
                    server = null;
                    closing = false;
                    isOn = false;
                }
            }
        }

    }
}
