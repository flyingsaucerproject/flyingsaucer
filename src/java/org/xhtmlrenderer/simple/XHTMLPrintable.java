package org.xhtmlrenderer.simple;

import java.awt.print.*;
import java.awt.*;

import org.xhtmlrenderer.util.u;
import org.xhtmlrenderer.extend.*;

public class XHTMLPrintable implements Printable {
    XHTMLPanel panel;
    public XHTMLPrintable(XHTMLPanel panel) {
        this.panel = panel;
    }
    public Graphics2DRenderer g2r;
    public int height = 0;
    public int print(Graphics g, PageFormat pf, int page) {
        try {
            Graphics2D g2 = (Graphics2D)g;
            double x = pf.getImageableX();
            double y = pf.getImageableY();
            double w = pf.getImageableWidth();
            double h = pf.getImageableHeight();
            g2.translate(x,y);
            
            /*u.p("size = " + pf.getImageableX() + ","
            + pf.getImageableY() + 
            " -> (" + pf.getImageableWidth() +
            " x " + pf.getImageableHeight() +
            ")");*/
            u.p("printing: page = " + page + " clip = " + g.getClipRect());
            
            //quit if this page would put us over the length of the document
            if(h*page > height) {
                u.p("quitting: " + h*page);
                u.p("height = " + height);
                return Printable.NO_SUCH_PAGE;
            }
            int trans = (int)(-h*page);
            //u.p("translating by " + trans);
            g2.translate(0,trans);

            
            // lay out the document if it's not already
            if(g2r == null) {
                u.p("building new Graphics2DRenderer");
                g2r = new Graphics2DRenderer();
                g2r.getRenderingContext().setDPI(72f);
                //g2r.getRenderingContext().setPrinting(true);
                g2r.getRenderingContext().getTextRenderer().setSmoothingThreshold(0);
                g2r.getRenderingContext().getTextRenderer().setSmoothingLevel(TextRenderer.HIGH);
                g2r.setDocument(panel.getURL());
                Dimension dim = new Dimension((int)w,(int)h);
                g2r.layout(g2,dim);
                Rectangle rect = g2r.getMinimumSize();
                u.p("laid out height = " + rect.getHeight());
                height = (int)rect.getHeight();
            }
            
            // render the document
            //u.p("render start");
            g2r.render(g2);
            //u.p("render end");
            return Printable.PAGE_EXISTS;
        } catch (Exception ex) {
            u.p(ex);
            return Printable.NO_SUCH_PAGE;
        }
    }
}

