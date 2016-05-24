package ru.spbau.mit.GUI;

import ru.spbau.mit.ClientHandler;
import ru.spbau.mit.Logging;
import ru.spbau.mit.Server;
import ru.spbau.mit.Torrent;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.logging.Level;

/**
 * Created by michael on 24.05.16.
 */

public class ServerGUI {
    public static void main(String[] args) {
        Logging.setLevel(Level.FINE);
        Server server = new Server(Torrent.SERVER_PORT);
        server.run(ClientHandler::new);
        JFrame frame = new JFrame("Torrent server");
        frame.add(new JLabel("SERVER"));
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                server.stop();
                super.windowClosing(e);
            }
        });
        frame.setMinimumSize(new Dimension(400, 400));
        frame.setVisible(true);
    }
}
