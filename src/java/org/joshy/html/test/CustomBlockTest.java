package org.joshy.html.test;

import org.joshy.html.*;
import java.awt.*;
import javax.swing.*;
import org.joshy.u;

public class CustomBlockTest {
    public static void main(String[] args) throws Exception {
        HTMLPanel panel = new HTMLPanel();
        LayoutFactory.addCustomLayout("custom",new XLayout());
        panel.setDocument("demos/customblock.xhtml");
        JFrame frame = new JFrame();
        frame.getContentPane().add(panel);
        frame.pack();
        frame.setSize(200,200);
        frame.show();
    }
}
