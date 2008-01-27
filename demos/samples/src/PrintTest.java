import org.xhtmlrenderer.simple.XHTMLPanel;
import org.xhtmlrenderer.simple.XHTMLPrintable;

import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.File;

/**
 * @author patrick
 */
public class PrintTest {
    public static void main(String[] args) {
        if (args.length == 0) {
            System.err.println("Need file path");
            System.exit(-1);
        }

        if (args[0].startsWith("http:")) {
            new PrintTest().printURL(args[0]);
        } else {
            new PrintTest().printFile(new File(args[0]));
        }
    }

    private void printURL(String url) {
        XHTMLPanel panel = new XHTMLPanel();

        try {
            panel.setDocument(url);
        } catch (Exception e) {
            e.printStackTrace();
        }

        printPanel(panel);
    }

    public void printFile(File file) {
        XHTMLPanel panel = new XHTMLPanel();

        try {
            panel.setDocument(file.toURI().toString());
        } catch (Exception e) {
            e.printStackTrace();
        }

        printPanel(panel);
    }

    private void printPanel(XHTMLPanel panel) {
        final PrinterJob printJob = PrinterJob.getPrinterJob();
        printJob.setPrintable(new XHTMLPrintable(panel));
        if (printJob.printDialog()) {
            try {

                printJob.print();

            } catch (PrinterException ex) {
                ex.printStackTrace();
            }
        }
    }

}
