package mypack;

/**
 * Created by greg on 22.09.15.
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;

public class Server extends Thread {

    private static final int PORT = 9001;
    private static HashMap<String, PrintWriter> writers = new HashMap<>();
    private static ServerWindow serverWindow = null;


    private static ArrayList<Connector> connectors = new ArrayList<>();
    private ServerSocket serverSocket;

    public Server(ServerWindow serverWindow, ServerSocket serverSocket) {
        this.serverWindow = serverWindow;
        this.serverSocket = serverSocket;

    }

    @Override
    public void run() {
        super.run();
        run(serverSocket);
    }


    public void run(ServerSocket serverSocket) {
        ServerSocket listener = serverSocket;
        try {
            while (true) {
                Connector connector = new Connector(serverSocket.accept());
                connectors.add(connector);
                connector.start();
            }
        } catch (SocketException e) {
            serverWindow.chat.append("The server shutdown unexpectedly\n");

        } catch (IOException e) {
            e.printStackTrace();
            serverWindow.chat.append("The server shutdown unexpectedly\n");
        } finally {
            try {
                listener.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    private static class Connector extends Thread {
        private String name;
        private Socket socket;
        private BufferedReader in;
        private PrintWriter out;
        private boolean disconnect = false;

        public Connector(Socket socket) {
            this.socket = socket;
        }

        private boolean getValidName(String name) {
            synchronized (writers) {
                if (!writers.containsKey(name)) {
                    writers.put(name, out);
                    serverWindow.chat.append(name + " connected" + "\n");
                    return true;
                }
            }
            return false;
        }

        public void run() {
            try {
                // Create character streams for the socket.
                in = new BufferedReader(new InputStreamReader(
                        socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);
                out.println("SUBMITNAME");
                while (true) {
                    BASE:
                    {
                        String line = in.readLine();
                        if (line == null || line.isEmpty()) {
                            break BASE;
                        }
                        if (line.startsWith("TO")) {
                            if (name == null) {
                                out.println("SUBMITNAME");
                                break BASE;
                            }
                            String receiver = line.substring(3, line.indexOf(':'));
                            if (writers.get(receiver) == null) {
                                out.println("NOTAVAILABLE");
                                break BASE;
                            }
                            //receiver
                            writers.get(receiver).println("MESSAGE FROM " + name + " " + line);
                            //sender
                            out.println("MESSAGE FROM " + name + " " + line);
                            serverWindow.chat.append("FROM " + name + " " + line + "\n");
                        } else if (line.startsWith("NAME")) {
                            String name = line.substring(5);
                            if (getValidName(name)) {
                                this.name = name;
                                out.println("NAMEACCEPTED");
                                break BASE;
                            }
                            out.println("SUBMITNAME");
                        } else if (line.startsWith("DISCONNECT")) {
                            out.println("DISCONNECT");
                            serverWindow.chat.append(name + " disconnected\n");
                            break;
                        } else if (disconnect) {
                            out.println("DISCONNECT");
                            break;
                        }
                    }
                }
            } catch (IOException e) {
                System.out.println(e);
            } finally {
                if (out != null) {
                    writers.remove(name);
                }
                try {
                    in.close();
                    out.close();
                    socket.close();
                } catch (IOException e) {
                }
            }
        }
    }
}
