package org.xhtmlrenderer.demo.docbook;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.io.InputStream;
import java.net.URL;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.xhtmlrenderer.layout.SharedContext;
import org.xhtmlrenderer.simple.FSScrollPane;
import org.xhtmlrenderer.simple.XHTMLPanel;
import org.xhtmlrenderer.swing.Java2DTextRenderer;
import org.xhtmlrenderer.swing.NaiveUserAgent;
import org.xhtmlrenderer.util.XRLog;

/**
 *
 */
public class ShowDocBookPage {
    public JFrame frame;
    
    private static class ResourceLoadingUserAgent extends NaiveUserAgent {
        protected InputStream resolveAndOpenStream(String uri) {
        	InputStream result = super.resolveAndOpenStream(uri);
        	if (result == null && uri != null && uri.startsWith("file:")) {
        		return ShowDocBookPage.class.getResourceAsStream(uri.substring("file:".length()));
        	} else {
        		return result;
        	}
        }
    }

    public static void main(final String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                String uri = "/docbook/xml/plugin-implement.xml";
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
        frame = new JFrame("Show Sample DocBook XML Rendered with Pure CSS");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        final XHTMLPanel panel = new XHTMLPanel();
        panel.getSharedContext().setUserAgentCallback(new ResourceLoadingUserAgent());
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

                showAboutDialog();
            }
        });
    }

    private void setAntiAlias(XHTMLPanel introPanel) {
        SharedContext sharedContext = introPanel.getSharedContext();
        sharedContext.setTextRenderer(new Java2DTextRenderer());
    }

    private void showAboutDialog() {
        final JDialog aboutDlg = new JDialog(frame);
        aboutDlg.setSize(new Dimension(500, 450));

        XHTMLPanel panel = new XHTMLPanel();
        setAntiAlias(panel);
        panel.setOpaque(false);

        URL url = ShowDocBookPage.class.getResource("/docbook/xhtml/intro.xhtml");
        panel.setDocument(url.toExternalForm());

        JPanel outer = new JPanel(new BorderLayout());
        outer.add(panel, BorderLayout.CENTER);
        final JButton btn = new JButton(new AbstractAction("OK") {
            public void actionPerformed(ActionEvent e) {
                aboutDlg.dispose();
            }
        });
        JPanel control = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        control.add(btn);
        outer.add(control, BorderLayout.SOUTH);

        aboutDlg.getContentPane().setLayout(new BorderLayout());
        aboutDlg.getContentPane().add(outer, BorderLayout.CENTER);

        aboutDlg.setTitle("About the Browser Demo");

        int xx = (frame.getWidth() - aboutDlg.getWidth()) / 2;
        int yy = (frame.getHeight() - aboutDlg.getHeight()) / 2;
        aboutDlg.setLocation(xx, yy);
        aboutDlg.setModal(true);
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                aboutDlg.setVisible(true);
            }
        });
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                btn.requestFocusInWindow();
            }
        });
    }
}