package mypack;

import java.io.IOException;
import java.net.ServerSocket;

/**
 * Created by greg on 25.09.15.
 */
public class Main {
    public static boolean isOn = true;
    public static boolean connectionNeeded = false;
    public static boolean closing = false;



    public static void main(String[] args) throws IOException {
        ServerWindow serverWindow = new ServerWindow();
        ServerSocket serverSocket = new ServerSocket(9001);
        serverWindow.chat.append("Server started\n");
        Server server = new Server(serverWindow, serverSocket);
        server.start();
        serverWindow.start();
        while (true) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (!isOn) {
                if (connectionNeeded) {
                    serverWindow.chat.append("Server started\n");
                    server = new Server(serverWindow, serverSocket);
                    server.run();
                    connectionNeeded = false;
                    isOn = true;
                }
            } else {
                if (closing) {
                    server.stopServer();
                    server.interrupt();
                    server = null;
                    closing = false;
                    isOn = false;
                }
            }
        }

    }
}
