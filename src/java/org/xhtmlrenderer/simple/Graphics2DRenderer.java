package org.xhtmlrenderer.simple;
import org.w3c.dom.*;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Dimension;
import java.io.*;
import java.net.*;
import org.xhtmlrenderer.extend.*;

import java.awt.*;
import java.awt.image.*;

/**

  Graphics2DRenderer supports headless rendering of XHTML documents.

*/
public class Graphics2DRenderer {
    protected XHTMLPanel panel;
    protected Dimension dim;
    
    public Graphics2DRenderer() {
        panel = new XHTMLPanel();
        panel.setThreadedLayout(false);
    }
    
    
    /* set the document to be rendered. */
    public void setDocument(URL url) throws Exception {
        panel.setDocument(url);
    }
    public void setDocument(Document doc, URL base_url) {
        panel.setDocument(doc,base_url);
    }
    
    /* lay out the document with the desired size. don't paint though. */
    /* maybe we could change the graphics2d to be a font rendering context? */
    public void layout(Graphics2D g2, Dimension dim) {
        this.dim = dim;
        panel.setSize(dim);
        panel.startLayout(g2);
    }
    /* return the size image needed to render the document without anything
    going off the side. *could be different than the dimensions passed into layout
    because of text that couldn't break or a table that's set to be too big */
    
    public Rectangle getMinimumSize() {
        return new Rectangle(0, 0,
            (int)panel.getIntrinsicSize().getWidth(),
            (int)panel.getIntrinsicSize().getHeight());
    }
    
    
    /* actually draw the */
    public void render(Graphics2D g2) {
        if(g2.getClip() == null) {
            g2.setClip(getMinimumSize());
        }
        panel.paintComponent(g2);
    }
    
    /* reset the internal variables so you can reuse this class */
    /*
    public void reset() {
    }
    */
    
    
    public RenderingContext getRenderingContext() {
        return panel.getRenderingContext();
    }
    public void setRenderingContext(RenderingContext ctx) {
        panel.setRenderingContext(ctx);
    }
    
    /* a static utility method to automatically create an image
    the right size, then layout and render the document */
    
    public static BufferedImage renderToImage(URL url, int width, int height) throws Exception {
        Graphics2DRenderer g2r = new Graphics2DRenderer();
        g2r.setDocument(url);
        Dimension dim = new Dimension(width,height);
        BufferedImage buff = new BufferedImage((int)dim.getWidth(), (int)dim.getHeight(),BufferedImage.TYPE_4BYTE_ABGR);
        Graphics2D g = (Graphics2D)buff.getGraphics();
        g2r.layout(g,dim);
        g2r.render(g);
        g.dispose();
        return buff;
    }
        
    public static BufferedImage renderToImage(URL url, int width) throws Exception {
        Graphics2DRenderer g2r = new Graphics2DRenderer();
        g2r.setDocument(url);
        Dimension dim = new Dimension(width,1000);

        // do layout with temp buffer
        BufferedImage buff = new BufferedImage((int)dim.getWidth(), (int)dim.getHeight(),BufferedImage.TYPE_4BYTE_ABGR);
        Graphics2D g = (Graphics2D)buff.getGraphics();
        g2r.layout(g,new Dimension(width,1000));
        g.dispose();
        
        // get size
        Rectangle rect = g2r.getMinimumSize();
        
        // render into real buffer
        buff = new BufferedImage((int)rect.getWidth(), (int)rect.getHeight(),BufferedImage.TYPE_4BYTE_ABGR);
        g = (Graphics2D)buff.getGraphics();
        g2r.render(g);
        g.dispose();
        
        // return real buffer
        return buff;
    }
    

}
