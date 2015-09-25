package mypack;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
        ExecutorService executor = Executors.newFixedThreadPool(1);
        executor.execute(server);
        serverWindow.start();
        while (true) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (!isOn) {
                if (connectionNeeded) {
                    serverWindow.writeInChat("Server started\n");
                    server = new Server(serverWindow, new ServerSocket(9001));
                    executor.execute(server);
                    connectionNeeded = false;
                    isOn = true;
                }
            } else {
                if (closing) {
                    server.stopServer();
                    closing = false;
                    isOn = false;
                }
            }
        }

    }
}
