package org.xhtmlrenderer.simple;

import org.w3c.dom.Document;
import org.xhtmlrenderer.extend.RenderingContext;
import org.xhtmlrenderer.swing.BasicPanel;

import java.io.File;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * <p>XHTMLPanel is a simple Swing component that renders valid
 * XHTML content in a Java program.  It is scrolling aware so you can
 * safely drop it into a JScrollPane.  The most common usage is to stuff a URL
 * into it and then add it to your JFrame. Ex:</p>
 * <pre>
 * import org.xhtmlrenderer.simple.*;
 * .....
 * <p/>
 * public static void main(String[] args) {
 * <p/>
 *   // set up the xhtml panel
 *   XHTMLPanel xhtml = new XHTMLPanel();
 *   xhtml.setDocument(new URL("http://myserver.com/page.xhtml"));
 * <p/>
 *   JScrollPane scroll = new JScrollPane(xhtml);
 *   JFrame frame = new JFrame("Demo");
 *   frame.getContentPane().add(scroll);
 *   frame.pack();
 *   frame.setSize(500,600);
 *   frame.show();
 * }
 * </pre>
 * <p/>
 * <p>XHTMLPanel also lets you make simple changes with simple methods
 * like setFontScale() and setMediaType(). If you want to make other changes
 * you will need to get the rendering context (getRenderingContext()) and
 * call methods on that.  Ex:
 * </p>
 * <p/>
 * <pre>
 * XHTMLPanel xhtml = new XHTMLPanel();
 * RenderingContext ctx = xhtml.getRenderingContext();
 * ctx.setLogging(true); // turn on logging
 * ctx.setValidating(true); // turn on doctype validation
 * ctx.addFont(fnt,"Arial"); // redefine a font
 * ctx.setDomImplementation("com.cooldom.DomImpl");
 * </pre>
 *
 * @author Joshua Marinacci (joshy@joshy.net)
 * @webpage http://xhtmlrenderer.dev.java.net/
 * @see RenderingContext
 *      </p>
 */

public class XHTMLPanel extends BasicPanel {

    public XHTMLPanel() {
        ctx = new RenderingContext();
        //ctx.setTextRenderer(new MiniumTextRenderer());
        //ctx.getTextRenderer().setSmoothingLevel(TextRenderer.HIGH);
    }

    public XHTMLPanel(URL url) throws Exception {
        this();
        setDocument(url);
    }

    /* various forms of setDocument() */
    public void setDocument(String filename) throws Exception {
        URL url = new File(filename).toURL();
        setDocument(url);
    }

    public void setDocument(Document doc) throws MalformedURLException {
        setDocument(doc, new File(".").toURL());
    }

    public void setDocument(URL url) throws Exception {
        setDocument(loadDocument(url), url);
    }

    public void setDocument(Document doc, URL url) {
        super.setDocument(doc, url);
    }

    public void setDocument(InputStream stream, URL url) throws Exception {
        super.setDocument(stream, url);
    }
    
    /*
    public void setDocument(File file) {
    }
    */
    
    /* saves document and all resources relative to it. if it can */
    /*
    public void saveDocument(File file) throws IOException {
    }
    public void saveDocumentAndResources(File file)  throws IOException {
    }
    */
    
    /* accessor for rendering context */
    public RenderingContext getRenderingContext() {
        return super.getRenderingContext();
    }

    public void setRenderingContext(RenderingContext ctx) {
        super.setRenderingContext(ctx);
    }

    public String getDocumentTitle() {
        return super.getDocumentTitle();
    }

    /* browser functions? */
    public void relayout() {
        super.calcLayout();
        ctx.getContext().flushFonts();
    }

    public void repaint() {
        super.repaint();
    }

}

