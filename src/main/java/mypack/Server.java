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
import java.util.ArrayList;
import java.util.HashMap;

public class Server {

    private static final int PORT = 9001;
    private static HashMap<String, PrintWriter> writers = new HashMap<>();

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
        connectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (isOn) {
                    isOn = false;
                    for (Connector connector : connectors) {
                        connector.disconecting();
                        connector.stop();
                        writers.remove(connector.name);
                    }
                } else {
                    isOn = true;
                }
            }
        });
        ServerSocket listener = new ServerSocket(PORT);
        try {
            while (true) {
                while (isOn) {
                    Connector connector = new Connector(listener.accept());
                    connectors.add(connector);
                    connector.start();
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

    public static void main(String[] args) throws Exception {
        Server server = new Server();

    }

    private static class Connector extends Thread {
        private String name;
        private Socket socket;
        private BufferedReader in;
        private PrintWriter out;

        public Connector(Socket socket) {
            this.socket = socket;
        }

        private void disconecting() {
            writers.get(name).println("DISCONNECT");
        }

        public void run() {
            try {
                // Create character streams for the socket.
                in = new BufferedReader(new InputStreamReader(
                        socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);
                chat.append(name+ " connected");

                while (true) {
                    out.println("SUBMITNAME");
                    name = in.readLine();
                    if (name == null) {
                        return;
                    }
                    synchronized (writers) {
                        if (!writers.containsKey(name)) {
                            writers.put(name, out);
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
                    if (!input.startsWith("DISCONNECT")) {
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

                    } else {
                        writers.get(name).println("DISCONNECT");
                        chat.append(name+ " disconnected");
                        break;
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
