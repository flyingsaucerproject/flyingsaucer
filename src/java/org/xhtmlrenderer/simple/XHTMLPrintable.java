package org.xhtmlrenderer.simple;

import java.awt.print.*;
import java.awt.*;

import org.xhtmlrenderer.util.u;
import org.xhtmlrenderer.extend.*;

/**
    <p>XHTMLPrintable allows you to print XHTML content to a printer instead of
    rendering it to screen.  It is an implementation of @see java.awt.print.Printable
    so you can use it any where you would use any other Printable object. The constructor
    requires an XHTMLPanel, so it's easiest to prepare an XHTMLPanel as normal, and then
    wrap a printable around it.ex:
    </p>
    
    <pre>
        import org.xhtmlrenderer.simple.*;
        import java.awt.print.*;
        // . . . .
        // xhtml_panel created earlier
        
        PrinterJob printJob = PrinterJob.getPrinterJob();
        printJob.setPrintable(new XHTMLPrintable(xhtml_panel));
        
        if(printJob.printDialog()) {
            printJob.print();
        }
    </pre>
    
    
    
*/

public class XHTMLPrintable implements Printable {
    
    protected XHTMLPanel panel;
    protected Graphics2DRenderer g2r = null;
    protected int height = 0;
    
    
    /**
    * Creates a new XHTMLPrintable that will print
    * the current contents of the passed in XHTMLPanel.
    * @param panel the XHTMLPanel to print
    */
    public XHTMLPrintable(XHTMLPanel panel) {
        this.panel = panel;
    }
    
    
    /**
    <p>The implementation of the <i>print</i> method
    from the @see java.awt.print.Printable interface.    
    */
    public int print(Graphics g, PageFormat pf, int page) {
        try {
            
            Graphics2D g2 = (Graphics2D)g;
            double x = pf.getImageableX();
            double y = pf.getImageableY();
            double w = pf.getImageableWidth();
            double h = pf.getImageableHeight();
            g2.translate(x,y);
            
            //u.p("printing: page = " + page + " clip = " + g.getClipRect());
            
            //quit if this page would put us over the length of the document
            if(h*page > height) {
                //u.p("quitting: " + h*page);
                //u.p("height = " + height);
                return Printable.NO_SUCH_PAGE;
            }
            int trans = (int)(-h*page);
            //u.p("translating by " + trans);
            g2.translate(0,trans);

            
            // lay out the document if it's not already
            if(g2r == null) {
                //u.p("building new Graphics2DRenderer");
                g2r = new Graphics2DRenderer();
                g2r.getRenderingContext().setMedia("print");
                g2r.getRenderingContext().setDPI(72f);
                g2r.getRenderingContext().getTextRenderer().setSmoothingThreshold(0);
                g2r.getRenderingContext().getTextRenderer().setSmoothingLevel(TextRenderer.HIGH);
                g2r.setDocument(panel.getURL());
                Dimension dim = new Dimension((int)w,(int)h);
                g2r.layout(g2,dim);
                Rectangle rect = g2r.getMinimumSize();
                //u.p("laid out height = " + rect.getHeight());
                height = (int)rect.getHeight();
            }
            
            // render the document
            g2r.render(g2);
            return Printable.PAGE_EXISTS;
        } catch (Exception ex) {
            u.p(ex);
            return Printable.NO_SUCH_PAGE;
        }
    }
}

