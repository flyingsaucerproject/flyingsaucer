package org.xhtmlrenderer.simple;

import org.xhtmlrenderer.extend.TextRenderer;
import org.xhtmlrenderer.util.Uu;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.print.PageFormat;
import java.awt.print.Printable;

/**
 * <p>XHTMLPrintable allows you to print XHTML content to a printer instead of
 * rendering it to screen.  It is an implementation of @see java.awt.print.Printable
 * so you can use it any where you would use any other Printable object. The constructor
 * requires an XHTMLPanel, so it's easiest to prepare an XHTMLPanel as normal, and then
 * wrap a printable around it.ex:
 * </p>
 * <p/>
 * <pre>
 * import org.xhtmlrenderer.simple.*;
 * import java.awt.print.*;
 * // . . . .
 * // xhtml_panel created earlier
 * <p/>
 * PrinterJob printJob = PrinterJob.getPrinterJob();
 * printJob.setPrintable(new XHTMLPrintable(xhtml_panel));
 * <p/>
 * if(printJob.printDialog()) {
 * printJob.print();
 * }
 * </pre>
 */

public class XHTMLPrintable implements Printable {

    protected XHTMLPanel panel;
    protected Graphics2DRenderer g2r = null;


    /**
     * Creates a new XHTMLPrintable that will print
     * the current contents of the passed in XHTMLPanel.
     *
     * @param panel the XHTMLPanel to print
     */
    public XHTMLPrintable(XHTMLPanel panel) {
        this.panel = panel;
    }


    /**
     * <p>The implementation of the <i>print</i> method
     * from the @see java.awt.print.Printable interface.
     */
    public int print(Graphics g, PageFormat pf, int page) {
        try {

            Graphics2D g2 = (Graphics2D) g;
            
            if (g2r == null) {
                g2r = new Graphics2DRenderer();
                g2r.getSharedContext().setPrint(true);
                g2r.getSharedContext().setInteractive(false);
                g2r.getSharedContext().setDPI(72f);
                g2r.getSharedContext().getTextRenderer().setSmoothingThreshold(0);
                g2r.getSharedContext().setUserAgentCallback(panel.getSharedContext().getUserAgentCallback());
                g2r.setDocument(panel.getDocument(), panel.getSharedContext().getUac().getBaseURL());
                g2r.getSharedContext().setReplacedElementFactory(panel.getSharedContext().getReplacedElementFactory());
                g2r.layout(g2, null);
                g2r.getPanel().assignPagePrintPositions(g2);
            }
            
            if (page >= g2r.getPanel().getRootLayer().getPages().size()) {
                return Printable.NO_SUCH_PAGE;
            }
            
            // render the document
            g2r.getPanel().paintPage(g2, page);
            
            return Printable.PAGE_EXISTS;
        } catch (Exception ex) {
            Uu.p(ex);
            return Printable.NO_SUCH_PAGE;
        }
    }
}

