package org.xhtmlrenderer.simple;

import org.xhtmlrenderer.extend.TextRenderer;
import org.xhtmlrenderer.util.Uu;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
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
    protected int height = 0;


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
            double x = pf.getImageableX();
            double y = pf.getImageableY();
            double w = pf.getImageableWidth();
            double h = pf.getImageableHeight();
            g2.translate(x, y);
            
            //Uu.p("printing: page = " + page + " clip = " + g.getClipRect());
            
            //quit if this page would put us over the length of the document
            if (h * page > height) {
                //Uu.p("quitting: " + h*page);
                //Uu.p("height = " + height);
                return Printable.NO_SUCH_PAGE;
            }
            int trans = (int) (-h * page);
            //Uu.p("translating by " + trans);
            g2.translate(0, trans);

            
            // lay out the document if it's not already
            if (g2r == null) {
                //Uu.p("building new Graphics2DRenderer");
                g2r = new Graphics2DRenderer();
                g2r.getRenderingContext().setMedia("print");
                g2r.getRenderingContext().setDPI(72f);
                g2r.getRenderingContext().getTextRenderer().setSmoothingThreshold(0);
                g2r.getRenderingContext().getTextRenderer().setSmoothingLevel(TextRenderer.HIGH);
                g2r.setDocument(panel.getURL());
                Dimension dim = new Dimension((int) w, (int) h);
                g2r.layout(g2, dim);
                Rectangle rect = g2r.getMinimumSize();
                //Uu.p("laid out height = " + rect.getHeight());
                height = (int) rect.getHeight();
            }
            
            // render the document
            g2r.render(g2);
            return Printable.PAGE_EXISTS;
        } catch (Exception ex) {
            Uu.p(ex);
            return Printable.NO_SUCH_PAGE;
        }
    }
}

