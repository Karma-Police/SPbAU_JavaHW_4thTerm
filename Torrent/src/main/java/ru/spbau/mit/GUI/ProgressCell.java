package ru.spbau.mit.GUI;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

/**
 * Created by michael on 24.05.16.
 */

public class ProgressCell extends DefaultTableCellRenderer {
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        JProgressBar bar = new JProgressBar();
        bar.setForeground(Color.blue);
        bar.setValue((Integer) table.getModel().getValueAt(row, column));
        bar.setString((Integer.toString(bar.getValue())));
        return bar;
    }
}
