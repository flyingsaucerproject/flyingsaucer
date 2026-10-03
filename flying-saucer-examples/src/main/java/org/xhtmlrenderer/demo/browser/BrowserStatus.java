package org.xhtmlrenderer.demo.browser;

import javax.swing.*;
import java.awt.*;

public final class BrowserStatus extends JPanel {
    public final JLabel text;
    public final JLabel memory;

    public BrowserStatus() {
        text = new JLabel("Status");
        memory = new JLabel("? MB / ? MB");
        createLayout();
        createEvents();
    }

    private void createLayout() {
        setLayout(new BorderLayout(5, 5));
        add("Center", text);
        add("East", memory);
    }

    @Override
    public Insets getInsets() {
        return new Insets(3, 4, 3, 4);
    }

    private void createEvents() {

        new Thread(() -> {
            while (true) {
                try {
                    Runtime rt = Runtime.getRuntime();
                    long used = rt.totalMemory() - rt.freeMemory();
                    long total = rt.totalMemory();

                    used = used / (1024 * 1024);
                    total = total / (1024 * 1024);

                    final String text = used + "M / " + total + "M";
                    SwingUtilities.invokeLater(() -> memory.setText(text));
                    Thread.sleep(5000);
                } catch (InterruptedException ignore) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }).start();
    }

}
