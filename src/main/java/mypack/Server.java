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
    private static ServerSocket listener;


    private JFrame frame = new JFrame("Server");
    private static JTextArea chat = new JTextArea(8, 40);
    private JButton connectButton = new JButton("Connect/Disconnect");
    private static boolean isOn;
    private static ArrayList<Connector> connectors = new ArrayList<>();


    Server() throws IOException {
        chat.setEditable(false);
        frame.getContentPane().add(new JScrollPane(chat), "Center");
        frame.getContentPane().add(connectButton, "South");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
        frame.pack();
        isOn = true;
        chat.append("The server has started\n");
        connectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (isOn) {
                    try {
                        isOn = false;
                        for (Connector connector : connectors) {
                            connector.disconnect = true;
                        }
                        connectors.clear();
                        listener.close();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                } else {
                    isOn = true;
                    chat.append("The server has started\n");

                }
            }
        });

    }

    private void warning(String message) {
        JOptionPane.showMessageDialog(frame, message);
    }

    public static void main(String[] args) throws Exception {
        Server server = new Server();
        listener = new ServerSocket(PORT);
        try {
            while (true) {
                if (isOn) {
                    try {
                        if (listener.isClosed()) {
                            listener = new ServerSocket(PORT);
                        }
                        Connector connector = new Connector(listener.accept());
                        connectors.add(connector);
                        connector.start();
                    } catch (SocketException e) {
                        chat.append("The server shutdown\n");
                    }
                }
            }
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

        public void run() {
            try {
                // Create character streams for the socket.
                in = new BufferedReader(new InputStreamReader(
                        socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);


                while (true) {
                    out.println("SUBMITNAME");
                    name = in.readLine();
                    if (name == null || name.isEmpty()) {
                        return;
                    }
                    synchronized (writers) {
                        if (!writers.containsKey(name)) {
                            writers.put(name, out);
                            chat.append(name + " connected" + "\n");
                            break;
                        }
                    }
                }
                out.println("NAMEACCEPTED");
                while (true) {
                    String input = in.readLine();
                    if (input == null) {
                        return;
                    }
                    if (input.startsWith("DISCONNECT")) {
                        out.println("DISCONNECT");
                        chat.append(name + " disconnected\n");
                        break;
                    } else if (disconnect) {
                        out.println("DISCONNECT");
                        break;
                    }
                    System.out.println(input);
                    String receiver = input.substring(3, input.indexOf(':'));
                    if (writers.get(receiver) == null) {
                        out.println("NOTAVAILABLE");
                    } else {
                        //receiver
                        writers.get(receiver).println("MESSAGE FROM " + name + " " + input);
                        //sender
                        out.println("MESSAGE FROM " + name + " " + input);
                        chat.append("FROM " + name + " " + input + "\n");
                    }
                }
            } catch (IOException e) {
                System.out.println(e);
            } finally {
                if (out != null) {
                    writers.remove(name);
                }
                try {
                    socket.close();
                } catch (IOException e) {
                }
            }
        }
    }
}
