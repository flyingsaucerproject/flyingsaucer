/**
 *
 */

import org.xhtmlrenderer.simple.FSScrollPane;
import org.xhtmlrenderer.simple.XHTMLPanel;

import javax.swing.*;
import java.io.File;

/**
 * This example shows the most basic use of Flying Saucer, to
 * display a single page, scrollable, within a JFrame. The input
 * is a file name.
 *
 *
 * @author Patrick Wright
 */
public class SinglePageFrame {
    private String fileName;

    public static void main(String[] args) throws Exception {
        new SinglePageFrame().run(args);
    }

    private void run(String[] args) {
        loadAndCheckArgs(args);

        // Create a JPanel subclass to render the page
        XHTMLPanel panel = new XHTMLPanel();

        // Set the XHTML document to render. We use the simplest form
        // of the API call, which uses a File reference. There
        // are a variety of overloads for setDocument().
        try {
            panel.setDocument(new File(fileName));
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        // Put our panel in a scrolling pane. You can use
        // a regular JScrollPane here, or our FSScrollPane.
        // FSScrollPane is already set up to move the correct
        // amount when scrolling 1 line or 1 page
        FSScrollPane scroll = new FSScrollPane(panel);

        JFrame frame = new JFrame("Flying Saucer: " + panel.getDocumentTitle());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(scroll);
        frame.pack();
        frame.setSize(1024, 768);
        frame.setVisible(true);
    }

    private void loadAndCheckArgs(String[] args) {
        if (args.length == 0) {
            System.out.println("Enter a file or URI.");
            System.exit(-1);
        }
        String name = args[0];
        if (! new File(name).exists()) {
            System.out.println("File " + name + " does not exist.");
            System.exit(-1);
        }
        this.fileName = name;
    }
}