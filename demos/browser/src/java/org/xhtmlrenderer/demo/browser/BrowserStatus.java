package org.xhtmlrenderer.demo.browser;

import org.xhtmlrenderer.util.Uu;

import javax.swing.*;
import java.awt.BorderLayout;

public class BrowserStatus extends JPanel {
    public JLabel text, memory;

    public void init() {
        createComponents();
        createLayout();
        createEvents();
    }

    public void createComponents() {
        text = new JLabel("Status");
        memory = new JLabel("XXX/XXX");
    }

    public void createLayout() {
        setLayout(new BorderLayout());
        add("Center", text);
        add("East", memory);
    }

    public void createEvents() {

        new Thread(new Runnable() {
            public void run() {
                while (true) {
                    try {
                        Runtime rt = Runtime.getRuntime();
                        long used = rt.totalMemory() - rt.freeMemory();
                        long total = rt.totalMemory();

                        used = used / (1024 * 1024);
                        total = total / (1024 * 1024);

                        final String text = used + "Mb / " + total + "Mb";
                        SwingUtilities.invokeLater(new Runnable() {
                            public void run() {
                                memory.setText(text);
                            }
                        });
                        Thread.currentThread().sleep(5000);
                    } catch (Exception ex) {
                        Uu.p(ex);
                    }
                }
            }
        }).start();
    }

}
