package org.xhtmlrenderer.demo.docbook;

import org.xhtmlrenderer.layout.SharedContext;
import org.xhtmlrenderer.render.Java2DTextRenderer;
import org.xhtmlrenderer.simple.FSScrollPane;
import org.xhtmlrenderer.simple.XHTMLPanel;
import org.xhtmlrenderer.util.XRLog;
import org.xhtmlrenderer.extend.TextRenderer;

import javax.swing.*;
import java.awt.*;
import java.net.URL;

/**
 *
 */
public class ShowDocBookPage {

    public static void main(final String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                String uri = "/xml/plugin-implement.xml";
                if (args.length > 0) uri = args[0];

                new ShowDocBookPage().run(uri);
            }
        });
    }

    private static void usage(int i, String reason) {
        String s = "org.xhtmlrenderer.demo.docbook.ShowDocbookPage" +
                "\n" +
                "Simple example to render a single DocBook XML page, " +
                "using only CSS, in a Swing JFrame/JPanel " +
                "using Flying Saucer." +
                "\n\n" +
                "Usage: \n" +
                "      java org.xhtmlrenderer.demo.docbook.ShowDocbookPage [uri]" +
                "\n\n" +
                "Error: " + reason;
        System.out.println(s);
        System.exit(i);
    }

    private void run(final String uri) {
        JFrame frame = new JFrame("Show Sample DocBook XML Rendered with Pure CSS");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        XHTMLPanel introPanel = new XHTMLPanel();
        setAntiAlias(introPanel);
        URL url = ShowDocBookPage.class.getResource("/docbook/xhtml/intro.xhtml");
        introPanel.setDocument(url.toExternalForm());

        introPanel.setPreferredSize(new Dimension(1024, 260));


        JScrollPane comp = new JScrollPane(introPanel);
        comp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        comp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        frame.getContentPane().add(comp, BorderLayout.NORTH);

        final XHTMLPanel panel = new XHTMLPanel();
        setAntiAlias(panel);

        FSScrollPane fsp = new FSScrollPane(panel);
        frame.getContentPane().add(fsp, BorderLayout.CENTER);

        frame.pack();
        frame.setSize(1024, 768);
        frame.setVisible(true);

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                URL url = ShowDocBookPage.class.getResource(uri);
                String urls = url.toExternalForm();
                XRLog.general("Loading URI: " + urls);
                panel.setDocument(urls);
            }
        });
    }

    private void setAntiAlias(XHTMLPanel introPanel) {
        SharedContext sharedContext = introPanel.getSharedContext();
        sharedContext.setTextRenderer(new Java2DTextRenderer());
        sharedContext.getTextRenderer().setSmoothingLevel(TextRenderer.HIGH);
    }
}
