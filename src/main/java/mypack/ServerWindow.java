package mypack;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by greg on 25.09.15.
 */
public class ServerWindow  {
    private JFrame frame = new JFrame("Server");
    public static JTextArea chat = new JTextArea(8, 40);
    private JButton connectButton = new JButton("Connect/Disconnect");
    private static boolean isOn = false;
    private Server server;
    private ExecutorService executor = Executors.newFixedThreadPool(1);
    private static ServerWindow serverWindow;


    public ServerWindow() {
        super();
        run();
    }


    public void run() {
        chat.setEditable(false);
        frame.getContentPane().add(new JScrollPane(chat), "Center");
        frame.getContentPane().add(connectButton, "South");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
        frame.pack();
        connectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!isOn) {
                    writeInChat("Server started\n");
                    try {
                        server = new Server(serverWindow, new ServerSocket(9001));
                    } catch (IOException e1) {
                        e1.printStackTrace();
                        warning("Server could not start");
                        isOn = false;
                    }
                    executor.execute(server);
                    isOn = true;
                } else {
                    server.stopServer();
                    isOn = false;
                }
            }
        });
    }
    public void writeInChat(String text){
        chat.append(text);
    }
    private void warning(String message) {
        JOptionPane.showMessageDialog(frame, message);
    }
    public static void main(String[] args) throws IOException {
        serverWindow = new ServerWindow();



    }
}




