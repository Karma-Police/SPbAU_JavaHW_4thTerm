package ru.spbau.mit.GUI;

import ru.spbau.mit.*;
import ru.spbau.mit.P2P.FileStatus;
import ru.spbau.mit.P2P.P2PServer;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;

/**
 * Created by michael on 24.05.16.
 */

public class GUI {
    private static Client client;
    private static JFrame frame;
    private static JTable myFilesTable;
    private static JTable serverFilesTable;
    private static JButton uploadButton;
    private static JButton refreshButton;
    private static JButton downloadButton;
    private static Timer timer;
    private static TimerTask updateTask;

    public static void main(String[] args) {
        Logging.setLevel(Level.FINE);
        runClient();
        createAndShowGUI();
        timer = new Timer();
        updateTask = new TimerTask() {
            @Override
            public void run() {
                myFilesTable.updateUI();
            }
        };
        timer.schedule(updateTask, 0, 1000);
    }


    private static void runClient() {
        client = new Client(Torrent.HOST_NAME, Torrent.SERVER_PORT);
        client.run();
    }

    private static void createAndShowGUI() {
        frame = new JFrame("Torrent");

        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                client.stop();
                super.windowClosing(e);
            }
        });

        //Display the window.
        frame.setSize(600, 600);
        frame.setMinimumSize(new Dimension(600, 600));

        JTabbedPane jTabbedPane = new JTabbedPane();
        jTabbedPane.addTab("My files", buildMyFiles());
        jTabbedPane.addTab("Server files", buildServerFiles());

        frame.add(jTabbedPane);
        frame.setVisible(true);
    }

    private static JPanel buildMyFiles() {
        myFilesTable = buildMyFilesTable();
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());

        JToolBar toolBar = new JToolBar(JToolBar.HORIZONTAL);
        toolBar.setFloatable(false);
        toolBar.setLayout(new FlowLayout(FlowLayout.RIGHT));

        uploadButton = new JButton("Upload new file");
        uploadButton.setMargin(new Insets(2, 2, 2, 2));
        uploadButton.addActionListener((event) -> {
            JFileChooser fileChooser = new JFileChooser();
            int ok = fileChooser.showOpenDialog(frame);
            if (ok == JFileChooser.APPROVE_OPTION) {
                client.upload(fileChooser.getSelectedFile().toPath());
                myFilesTable.updateUI();
            }
        });

        toolBar.add(uploadButton);
        panel.add(toolBar, BorderLayout.NORTH);
        panel.add(new JScrollPane(myFilesTable), BorderLayout.CENTER);
        return panel;
    }

    private static JPanel buildServerFiles() {
        serverFilesTable = buildServerFilesTable();
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());

        JToolBar toolBar = new JToolBar(JToolBar.HORIZONTAL);
        toolBar.setFloatable(false);
        toolBar.setLayout(new FlowLayout(FlowLayout.RIGHT));

        JLabel hint = new JLabel();
        hint.setText("Nice to see you again!");

        refreshButton = new JButton("Refresh");
        refreshButton.setMargin(new Insets(2, 2, 2, 2));
        refreshButton.addActionListener((event) -> {
            client.list();
            serverFilesTable.updateUI();
            hint.setText("Refreshed!");
            hint.updateUI();
        });


        downloadButton = new JButton("Download");
        downloadButton.setMargin(new Insets(2, 2, 2, 2));
        downloadButton.addActionListener((event) -> {
            refreshButton.setEnabled(false);
            int row = serverFilesTable.getSelectedRow();
            if (row != -1) {
                Client.FileEntry file = client.serverFiles.get(row);
                client.downloadFile(file);
                myFilesTable.updateUI();
            } else {
                hint.setText("You need to select some file first..");
                hint.updateUI();
            }
            refreshButton.setEnabled(true);
        });

        toolBar.add(refreshButton);
        toolBar.add(downloadButton);

        panel.add(toolBar, BorderLayout.NORTH);
        panel.add(new JScrollPane(serverFilesTable), BorderLayout.CENTER);
        panel.add(hint, BorderLayout.SOUTH);
        return panel;
    }

    private static JTable buildMyFilesTable() {
        JTable table = new JTable(new AbstractTableModel() {
            private final String[] columnNames = new String[]{"File", "Size", "Progress"};
            @Override
            public int getRowCount() {
                return P2PServer.myFiles.size();
            }

            @Override
            public int getColumnCount() {
                return columnNames.length;
            }

            @Override
            public String getColumnName(int index) {
                return columnNames[index];
            }

            @Override
            public Object getValueAt(int rowIndex, int columnIndex) {
                HashMap<Integer, FileStatus> files = new HashMap<>(P2PServer.myFiles);
                FileStatus fileStatus = files.get(files.keySet().toArray()[rowIndex]);
                if (fileStatus != null) {
                    switch (columnIndex) {
                        case 0:
                            return fileStatus.name;
                        case 1:
                            return fileStatus.size;
                        case 2:
                            return fileStatus.calculateProgress();
                    }
                }
                return null;
            }
        });
        table.getColumnModel().getColumn(2).setCellRenderer(new ProgressCell());

        table.getColumnModel().getColumn(1).setMinWidth(80);
        table.getColumnModel().getColumn(1).setMaxWidth(100);

        table.getColumnModel().getColumn(2).setMinWidth(100);
        table.getColumnModel().getColumn(2).setMaxWidth(150);

        return table;
    }

    private static JTable buildServerFilesTable() {
        JTable table = new JTable(new AbstractTableModel() {
            private final String[] columnNames = new String[]{"File", "Size"};

            public int getRowCount() {
                return client.serverFiles.size();
            }

            @Override
            public int getColumnCount() {
                return columnNames.length;
            }

            @Override
            public String getColumnName(int index) {
                return columnNames[index];
            }

            @Override
            public Object getValueAt(int rowIndex, int columnIndex) {
                switch (columnIndex) {
                    case 0:
                        return client.serverFiles.get(rowIndex).name;
                    case 1:
                        return client.serverFiles.get(rowIndex).size;
                }
                return null;
            }
        });

        table.getColumnModel().getColumn(1).setMinWidth(80);
        table.getColumnModel().getColumn(1).setMaxWidth(100);
        return table;
    }

}
