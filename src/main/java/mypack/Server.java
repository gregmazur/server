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

    public void stopServer() {
        synchronized (connectors) {
            for (Connector connector : connectors) {
                serverWindow.writeInChat(connector.getConnectorName() + " disconnected\n");
                connector.disconnect();
            }
        }
        connectors.clear();
        try {
            Thread.sleep(10);
            serverSocket.close();
            Thread.sleep(100);
            Thread.currentThread().interrupt();
            return;
        } catch (IOException e) {
            e.printStackTrace();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            e.printStackTrace();
        }
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
            serverWindow.writeInChat("The server shutdown\n");
        } catch (IOException e) {
            e.printStackTrace();
            serverWindow.writeInChat("The server shutdown unexpectedly\n");
        } finally {
            try {
                listener.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    private static class Connector extends Thread {
        private String connectorName;
        private Socket socket;
        private BufferedReader in;
        private PrintWriter out;

        public Connector(Socket socket) {
            this.socket = socket;
        }

        private boolean getValidName(String name) {
            synchronized (writers) {
                if (!writers.containsKey(name)) {
                    writers.put(name, out);
                    serverWindow.writeInChat(name + " connected" + "\n");
                    return true;
                }
            }
            return false;
        }

        public void disconnect() {
            if (out != null) {
                writers.remove(connectorName);
                out.println("DISCONNECT");
            }

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
                            if (connectorName == null) {
                                out.println("SUBMITNAME");
                                break BASE;
                            }
                            String receiver = line.substring(3, line.indexOf(':'));
                            if (writers.get(receiver) == null) {
                                out.println("NOTAVAILABLE");
                                break BASE;
                            }
                            //receiver
                            writers.get(receiver).println("MESSAGE FROM " + connectorName + " " + line);
                            //sender
                            out.println("MESSAGE FROM " + connectorName + " " + line);
                            serverWindow.writeInChat("FROM " + connectorName + " " + line + "\n");
                        } else if (line.startsWith("NAME")) {
                            String name = line.substring(5);
                            if (getValidName(name)) {
                                this.connectorName = name;
                                out.println("NAMEACCEPTED");
                                break BASE;
                            }
                            out.println("SUBMITNAME");
                        } else if (line.startsWith("DISCONNECT")) {
                            out.println("DISCONNECT");
                            serverWindow.writeInChat(connectorName + " disconnected\n");
                            break;
                        }
                    }
                }
            } catch (IOException e) {
                System.out.println(e);
            } finally {
                if (out != null) {
                    writers.remove(connectorName);
                }
                try {
                    in.close();
                    out.close();
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        public String getConnectorName() {
            return connectorName;
        }
    }
}
