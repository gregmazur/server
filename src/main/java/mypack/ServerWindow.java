package mypack;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by greg on 25.09.15.
 */
public class ServerWindow extends Thread {
    private JFrame frame = new JFrame("Server");
    public static JTextArea chat = new JTextArea(8, 40);
    private JButton connectButton = new JButton("Connect/Disconnect");


    public ServerWindow() {
        super();
    }

    @Override
    public void run() {
        super.run();
        chat.setEditable(false);
        frame.getContentPane().add(new JScrollPane(chat), "Center");
        frame.getContentPane().add(connectButton, "South");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
        frame.pack();
        connectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (Main.isOn) {
                    Main.closing = true;
                } else {
                    Main.connectionNeeded = true;
                }
            }
        });
    }
    public void writeInChat(String text){
        chat.append(text);
    }
}


//    private void warning(String message) {
//        JOptionPane.showMessageDialog(frame, message);
//    }

