package org.joshy.html.app.aboutbox;

import java.awt.event.*;
import javax.swing.*;
import java.awt.*;

import org.joshy.html.*;
import org.joshy.u;

public class AboutBox extends JDialog implements Runnable {
    JScrollPane scroll;
    JButton close_button;
    boolean go = false;
    public AboutBox(String text, String url) {
        super();
        setTitle(text);
        HTMLPanel panel = new HTMLPanel();
        int w = 400;
        int h = 500;
        panel.setPreferredSize(new Dimension(w,h));

        
        scroll = new JScrollPane(panel);
        scroll.setVerticalScrollBarPolicy(scroll.VERTICAL_SCROLLBAR_ALWAYS);
        scroll.setHorizontalScrollBarPolicy(scroll.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setPreferredSize(new Dimension(w,h));
        panel.setViewportComponent(scroll);
        panel.setJScrollPane(scroll);
        getContentPane().add(scroll,"Center");
        close_button = new JButton("Close");
        getContentPane().add(close_button,"South");
        close_button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                setVisible(false);
                go = false;
            }
        });
        
        try {
            panel.setDocument(url);
        } catch (Exception ex) {
            u.p(ex);
        }
        pack();
        setSize(w,h);
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation((screen.width-w)/2,(screen.height-h)/2);
    }
    
    public void setVisible(boolean vis) {
        super.setVisible(vis);
        if(vis == true) {
            startScrolling();
        }
    }
    
    Thread thread;
    public void startScrolling() {
        go = true;
        thread = new Thread(this);
        thread.start();
    }
    
    public void run() {
        while(go) {
            try {
                Thread.currentThread().sleep(100);
            } catch (Exception ex) {
                u.p(ex);
            }
            JScrollBar sb = scroll.getVerticalScrollBar();
            sb.setValue(sb.getValue()+1);
        }
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("About Box Test");
        JButton launch = new JButton("Show About Box");
        frame.getContentPane().add(launch);
        frame.pack();
        frame.setVisible(true);
        
        launch.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                AboutBox ab = new AboutBox("About Flying Saucer","demos/about/index.xhtml");
                ab.setVisible(true);
            }
        });
    }
}
