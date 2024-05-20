package org.xhtmlrenderer.demo.browser;

import javax.swing.*;
import java.awt.*;

public class BrowserStatus extends JPanel {
    private static final long serialVersionUID = 1L;

    public JLabel text, memory;

    public void init() {
        createComponents();
        createLayout();
        createEvents();
    }

    public void createComponents() {
        text = new JLabel("Status");
        memory = new JLabel("? MB / ? MB");
    }

    public void createLayout() {
        setLayout(new BorderLayout(5, 5));
        add("Center", text);
        add("East", memory);
    }

    public Insets getInsets() {
        return new Insets(3, 4, 3, 4);
    }

    public void createEvents() {

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
