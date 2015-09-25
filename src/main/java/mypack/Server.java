package mypack;

/**
 * Created by greg on 22.09.15.
 */

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;

public class Server {

    private static final int PORT = 9001;
    private static HashMap<String, PrintWriter> writers = new HashMap<>();


    private JFrame frame = new JFrame("Server");
    private static JTextArea chat = new JTextArea(8, 40);
    private JButton connectButton = new JButton("Connect/Disconnect");
    private static int status;
    private static final int DISCONNECTED = 0;
    private static final int WORKING = 1;
    private static final int CONNECTING = 2;
    private static final int DISCONNECTING = 3;
    private static ArrayList<Connector> connectors = new ArrayList<>();


    Server() throws IOException {
        status = CONNECTING;
        chat.setEditable(false);
        frame.getContentPane().add(new JScrollPane(chat), "Center");
        frame.getContentPane().add(connectButton, "South");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
        frame.pack();
        chat.append("The server has started\n");
        connectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                switch (status) {
                    case DISCONNECTED: {
                        status = CONNECTING;
                        chat.append("The server has started\n");
                        break;
                    }
                    case WORKING: {
                        status = DISCONNECTING;
                        break;
                    }
                }

            }
        });
    }

//    private void warning(String message) {
//        JOptionPane.showMessageDialog(frame, message);
//    }

    public static void main(String[] args) throws Exception {
        Server server = new Server();
        ServerSocket listener = null;
        try {
            while (true) {
                if (WORKING == status) {
                    Connector connector = new Connector(listener.accept());
                    connectors.add(connector);
                    connector.start();
                } else if (CONNECTING == status) {
                    listener = new ServerSocket(PORT);
                    status = WORKING;
                } else if (DISCONNECTING == status) {
                    for (Connector connector : connectors) {
                        connector.disconnect = true;
                    }
                    status = DISCONNECTED;
                    connectors.clear();
                    listener.close();
                    chat.append("The server shutdown\n");
                }
            }

        } catch (SocketException e) {
            chat.append("The server shutdown unexpectedly\n");
            status = DISCONNECTED;

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
                    chat.append(name + " connected" + "\n");
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
                            chat.append("FROM " + name + " " + line + "\n");
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
                            chat.append(name + " disconnected\n");
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
