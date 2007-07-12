package org.xhtmlrenderer.demo.svg;

import org.xhtmlrenderer.extend.ReplacedElementFactory;
import org.xhtmlrenderer.simple.FSScrollPane;
import org.xhtmlrenderer.simple.XHTMLPanel;
import org.xhtmlrenderer.swing.SwingReplacedElementFactory;
import org.xhtmlrenderer.util.XRLog;

import javax.swing.*;
import java.awt.*;
import java.net.URL;

/**
 *
 */
public class ShowSVGPage {

    public static void main(final String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                String uri = "/svg/svg.xhtml";
                if ( args.length > 0 ) uri = args[0];

                new ShowSVGPage().run(uri);
            }
        });
    }

    private static void usage(int i, String reason) {
        String s = "svg.ShowSVGPage" +
                "\n" +
                "Simple example to render a single XML/CSS page, " +
                "which contains embedded SVG, in a Swing JFrame/JPanel " +
                "using Flying Saucer." +
                "\n\n" +
                "Usage: \n" +
                "      java svg.ShowSVGPage [uri]" +
                "\n\n" +
                "Error: " + reason;
        System.out.println(s);
        System.exit(i);
    }

    private void run(final String uri) {
        JFrame frame = new JFrame("Show Sample XML with Embedded SVG");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // RootPanel holds the ReplacedElementFactories. Currently, this factory
        // is created on each call to layout, so we override the RootPanel method
        // and return our own--the chained factory delegates first for Swing
        // replaced element, then for SVG elements.
        ChainedReplacedElementFactory cef = new ChainedReplacedElementFactory();
        cef.addFactory(new SwingReplacedElementFactory());
        cef.addFactory(new SVGSalamanderReplacedElementFactory());

        final XHTMLPanel panel = new XHTMLPanel();
        panel.getSharedContext().setReplacedElementFactory(cef);

        FSScrollPane fsp = new FSScrollPane(panel);
        frame.getContentPane().add(fsp, BorderLayout.CENTER);

        frame.setSize(1024, 768);
        frame.setVisible(true);

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                XRLog.general("URI is: " + uri);
                URL url = ShowSVGPage.class.getResource(uri);
                String urls = url.toExternalForm();
                XRLog.general("Loading URI: " + urls);
                panel.setDocument(urls);
            }
        });
    }
}
