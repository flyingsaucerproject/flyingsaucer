/*
 * {{{ header & license
 * Copyright (c) 2004, 2005 Joshua Marinacci
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.	See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 * }}}
 */
package org.xhtmlrenderer.swing;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LayoutManager;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.print.PrinterGraphics;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;

import org.w3c.dom.Document;
import org.xhtmlrenderer.css.style.CalculatedStyle;
import org.xhtmlrenderer.event.DocumentListener;
import org.xhtmlrenderer.extend.NamespaceHandler;
import org.xhtmlrenderer.extend.UserAgentCallback;
import org.xhtmlrenderer.layout.Layer;
import org.xhtmlrenderer.layout.SharedContext;
import org.xhtmlrenderer.render.Box;
import org.xhtmlrenderer.render.PageBox;
import org.xhtmlrenderer.render.RenderingContext;
import org.xhtmlrenderer.resource.XMLResource;
import org.xhtmlrenderer.util.Configuration;
import org.xhtmlrenderer.util.Uu;
import org.xhtmlrenderer.util.XRLog;
import org.xml.sax.ErrorHandler;

//hmm, IntelliJ sees references to Xx below as being Xx in Component!

/**
 * A Swing {@link javax.swing.JPanel} that encloses the Flying Saucer renderer
 * for easy integration into Swing applications.
 *
 * @author Joshua Marinacci
 */
public abstract class BasicPanel extends RootPanel {
    /**
     * Description of the Field
     */
     
    //public Element hovered_element = null;

    /**
     * Description of the Field
     */
    //public Element active_element = null;

    /**
     * Description of the Field
     */
    //public Element focus_element = null;

    /**
     * Description of the Field
     */
    protected ErrorHandler error_handler;

    /**
     * Description of the Field
     */
    //protected RenderingContext ctx;

    /**
     * Description of the Field
     */
    //protected LayoutThread layout_thread;

    /**
     * Description of the Field
     */
    //private Map documentListeners;

    /**
     * Description of the Field
     */
    /* DEAD: joshy
   private Dimension intrinsic_size;
*/

    /**
     * Description of the Field
     */
    private boolean anti_aliased = true;

    private boolean explicitlyOpaque;


    /**
     * Message used while layout is in progress and panel is being redrawn.
     */
    private String layoutInProgressMsg = "Layout in progress...";

    /**
     * Returns the string message drawn on the panel while rendering a page. For most pages, this will be barely visible
     * as pages render so quickly.
     *
     * @return See desc.
     */
    public String getLayoutInProgressMsg() {
        return layoutInProgressMsg;
    }

    /**
     * Sets the string message drawn on the panel while rendering a page. For most pages, this will be barely visible
     * as pages render so quickly.
     *
     * @param layoutInProgressMsg See desc..
     */
    public void setLayoutInProgressMsg(String layoutInProgressMsg) {
        this.layoutInProgressMsg = layoutInProgressMsg;
    }


    private boolean interactive = true;

    public BasicPanel() {
        sharedContext = new SharedContext(new NaiveUserAgent());
        init();
    }

    public BasicPanel(UserAgentCallback uac) {
        sharedContext = new SharedContext(uac);
        init();
    }

    public BasicPanel(boolean useThreads) {
        super(useThreads);
        sharedContext = new SharedContext(new NaiveUserAgent());
        init();
    }

    public BasicPanel(boolean useThreads, UserAgentCallback uac) {
        super(useThreads);
        sharedContext = new SharedContext(uac);
        init();
    }

    /**
     * Adds the specified Document listener to receive Document events from this
     * component. If listener l is null, no exception is thrown and no action is
     * performed.
     *
     * @param listener Contains the DocumentListener for DocumentEvent data.
     */
    public void addDocumentListener(DocumentListener listener) {
        this.documentListeners.put(listener, listener);
    }

    /**
     * Description of the Method
     *
     * @param g PARAM
     */
    public void paintComponent(Graphics g) {
        if (anti_aliased) {
            // TODO:
            // ( (Graphics2D)g ).setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );
        }

        if (doc == null) {
            return;
        }
        
        //Uu.p("paint component () called");
        // if this is the first time painting this document, then calc layout
        Layer root = getRootLayer();
        if (root == null && !isUseThreads()) {
            doActualLayout(getGraphics());
            root = getRootLayer();
        }
        if (root == null) {
            //Uu.p("dispatching an initial resize event");
            //queue.dispatchLayoutEvent(new ReflowEvent(ReflowEvent.CANVAS_RESIZED, this.getSize()));
            XRLog.render(Level.FINE, "skipping the actual painting");
        } else {
            RenderingContext c = newRenderingContext((Graphics2D) g);
            long start = System.currentTimeMillis();
            executeRenderThread(c, root);
            long end = System.currentTimeMillis();
            XRLog.render(Level.FINE, "RENDERING TOOK " + (end - start) + " ms");
        }
    }

    /**
     * Description of the Method
     */

    /*
   public void startLayout(Graphics g) {
       Uu.p("shouldn't be calling this method");
       doActualLayout(g);

       this.removeAll();
       if (g == null) {
           return;
       }
       if (doc == null) {
           return;
       }

       // set up CSS
       Context c = newContext((Graphics2D) g);
       //getSharedContext().setMaxWidth(0);
       this.layout_context = c;
       getRenderingContext().getTextRenderer().setupGraphics(c.getGraphics());
       //TODO: maybe temporary hack
       if (c.getPersistentBFC() != null) c.popBFC();//we set one for the top level before
       // do the actual layout
       body_box = Boxing.layout(c, new DomToplevelNode(doc));
       if (!c.isStylesAllPopped()) {
           XRLog.layout(Level.SEVERE, "mismatch in style popping and pushing");
       }

       XRLog.layout(Level.FINEST, "is a fixed child: " + body_box.isChildrenExceedBounds());

       // if there is a fixed child then we need to set opaque to false
       // so that the entire viewport will be repainted. this is slower
       // but that's the hit you get from using fixed layout
       if (body_box.isChildrenExceedBounds()) {
           super.setOpaque(false);
       } else {
           super.setOpaque(true);
       }

       getRenderingContext().setRootBox(body_box);

       XRLog.layout(Level.FINEST, "after layout: " + body_box);

       intrinsic_size = new Dimension(getSharedContext().getMaxWidth(), body_box.height);
       //Uu.p("intrinsic size = " + intrinsic_size);
       if (enclosingScrollPane != null) {
           XRLog.layout(Level.FINEST, "enclosing scroll pane = " + this.enclosingScrollPane);
           int view_height = this.enclosingScrollPane.getViewport().getHeight();
           // resize the outter most box incase it is too small for the viewport
           if (intrinsic_size.getHeight() < view_height) {
               if (body_box != null) {
                   body_box.height = view_height;
                   bodyExpandHack(body_box, view_height);
                   intrinsic_size.height = view_height;
               }
           }
       }

       if (!intrinsic_size.equals(this.getSize())) {
           this.setPreferredSize(intrinsic_size);
           this.revalidate();
       }

       this.fireDocumentLoaded();

   }
   */


    protected void executeRenderThread(RenderingContext c, Layer root) {
        //Uu.p("do render called");
        //Uu.p("last render event = " + last_event);
		
        // paint the normal swing background first
        // but only if we aren't printing.
        Graphics g = ((Java2DOutputDevice)c.getOutputDevice()).getGraphics();
        if (!(g instanceof PrinterGraphics) && explicitlyOpaque) {
            g.setColor(getBackground());
            g.fillRect(0, 0, getWidth(), getHeight());
        }

        long start = System.currentTimeMillis();
        if (!c.isPrint()) {
            root.paint(c, 0, 0);
        } else {
            paintPagedView(c, root);
        }
        long after = System.currentTimeMillis();
        if (Configuration.isTrue("xr.incremental.repaint.print-timing", false)) {
            Uu.p("repaint took ms: " + (after - start));
        }

        last_event = null;

    }
    
    private static final int PAGE_PAINTING_CLEARANCE = 10;

    private void paintPagedView(RenderingContext c, Layer root) {
        if (root.getLastPage() == null) {
            return;
        }
        
        root.assignPagePaintingPositions(
                c, Layer.PAGED_MODE_SCREEN, PAGE_PAINTING_CLEARANCE);

        setPreferredSize(new Dimension(
                root.getMaxPageWidth(c, PAGE_PAINTING_CLEARANCE),
                root.getLastPage().getPaintingBottom() + PAGE_PAINTING_CLEARANCE));
        revalidate();

        Graphics2D g = ((Java2DOutputDevice)c.getOutputDevice()).getGraphics();
        Shape working = g.getClip();

        List pages = root.getPages();
        c.setPageCount(pages.size());
        for (int i = 0; i < pages.size(); i++) {
            PageBox page = (PageBox)pages.get(i);
            c.setPage(i, page);

            g.setClip(working);
            
            Rectangle overall = page.getOverallPaintingBounds(c, PAGE_PAINTING_CLEARANCE);
            overall.x -= 1;
            overall.y -= 1;
            overall.width += 1;
            overall.height += 1;
            
            Rectangle bounds = new Rectangle(overall);
            bounds.width += 1;
            bounds.height += 1;
            if (working.intersects(bounds)) {
                Color old = g.getColor();
                
                g.setColor(Color.BLACK);
                g.drawRect(overall.x, overall.y, overall.width, overall.height);
                g.setColor(old);
                
                Rectangle content = page.getPagedViewClippingBounds(c, PAGE_PAINTING_CLEARANCE);
                g.clip(content);
                
                int left = PAGE_PAINTING_CLEARANCE +
                    page.getStyle().getMarginBorderPadding(c, CalculatedStyle.LEFT);
                int top = page.getPaintingTop() 
                    + page.getStyle().getMarginBorderPadding(c, CalculatedStyle.TOP)
                    - page.getTop();
                
                g.translate(left, top);
                root.paint(c, 0, 0);
                g.translate(-left, -top);
                
                g.setClip(working);
                page.paintAlternateFlows(c, root, 
                        Layer.PAGED_MODE_SCREEN, PAGE_PAINTING_CLEARANCE);
                
                page.paintBorder(c, PAGE_PAINTING_CLEARANCE);
            } 
        }
        
        g.setClip(working);
    }
    

    public void paintPage(Graphics2D g, int pageNo) {
        Layer root = getRootLayer();

        if (root == null) {
            throw new RuntimeException("Document needs layout");
        }
        
        if (pageNo < 0 || pageNo >= root.getPages().size()) {
            throw new IllegalArgumentException("Page " + pageNo + " is not between 0 " +
                    "and " + root.getPages().size());
        }

        RenderingContext c = newRenderingContext(g);

        PageBox page = (PageBox)root.getPages().get(pageNo);
        
        Shape working = g.getClip();
        
        Rectangle content = page.getPrintingClippingBounds(c);
        g.clip(content);
        
        int top = -page.getPaintingTop() + 
            page.getStyle().getMarginBorderPadding(c, CalculatedStyle.TOP);
        
        int left = page.getStyle().getMarginBorderPadding(c, CalculatedStyle.LEFT);
        
        g.translate(left, top);
        root.paint(c, 0, 0);
        g.translate(-left, -top);
        
        g.setClip(working);
        page.paintAlternateFlows(c, root, Layer.PAGED_MODE_PRINT, 0);
        
        page.paintBorder(c, 0);

        g.setClip(working);
    }
    
    public void assignPagePrintPositions(Graphics2D g) {
        RenderingContext c = newRenderingContext(g);
        getRootLayer().assignPagePaintingPositions(c, Layer.PAGED_MODE_PRINT);
    }

    /**
     * Description of the Method
     *
     * @param box PARAM
     * @param x   PARAM
     * @param y   PARAM
     * @return Returns
     */
    /*
   public Box findBox(Box box, int x, int y, BlockFormattingContext bfc) {

       if (box == null) {
           return null;
       }
       Iterator it = box.getChildIterator();
       while (it.hasNext()) {
           Box bx = (Box) it.next();
           int tx = x;
           int ty = y;
           tx -= bx.x;
           //is this needed?
           tx -= bx.tx;
           ty -= bx.y;
           //is this needed?
           ty -= bx.ty;

           // test the contents
           Box retbox = null;
           retbox = findBox(bx, tx, ty, bfc);
           if (retbox != null) {
               return retbox;
           }

           // test the box itself
           int tty = y;
           if (bx instanceof InlineBox) {
               InlineBox ibx = (InlineBox) bx;
               LineBox lbx = (LineBox) box;
               int off = lbx.getBaseline() + ibx.y - ibx.height;//this is not really correct, what about vertical align?
               tty -= off;
           }

           if (bx.contains(x - bx.x, tty - bx.y)) {
               return bx;
           }
       }

       return null;
   }
   */

    /**
     * Description of the Method
     *
     * @param box PARAM
     * @param x   PARAM
     * @param y   PARAM
     * @return Returns
     */
    /*
   public Box findElementBox(Box box, int x, int y) {
       return findElementBox(box, x, y, null);
   }
   */
/*
    public Box findElementBox(Box box, int x, int y,
                              BlockFormattingContext bfc) {//TODO: why is this used? A better way? should be in a render util?
        
        if (box == null) {
            return null;
        }

        //Uu.p("bfc blah = " + box.getPersistentBFC());

        // go down to the next bfc
        if (box.getPersistentBFC() != null) {
            bfc = box.getPersistentBFC();
        }

        // loop through the children first
        Iterator it = box.getChildIterator();
        while (it.hasNext()) {
            Box bx = (Box) it.next();
            int tx = x;
            int ty = y;
            tx -= bx.x;
            tx -= bx.tx;
            ty -= bx.y;
            ty -= bx.ty;
*/
    /*
    if(bx.getPersistentBFC() != null) {
        Uu.p("current bfc = " + bfc);
        bfc = bx.getPersistentBFC();
        Uu.p("setting bfc" + bfc);
    }

    */
/*            //Uu.p("bfc = " + box.getPersistentBFC());
            if (bx.absolute) {
                int[] adj = adjustForAbsolute(bx, tx, ty, bfc);
                tx = adj[0];
                ty = adj[1];
            }


            // test the contents
            Box retbox = null;
            retbox = findElementBox(bx, tx, ty, bfc);
            if (retbox != null) {
                return retbox;
            }
            
            // test the box itself
            
            // skip if it's text only so that we can
            // hit the parent instead
            // skip line boxes
            if (bx instanceof LineBox) {
                continue;
            }

            int tty = y;
            if (bx instanceof InlineBox) {
                InlineBox ibx = (InlineBox) bx;
                LineBox lbx = (LineBox) box;
                int off = lbx.getBaseline() + ibx.y - ibx.height;//not really correct
                tty -= off;
            }
            
            //Uu.p("bx = " + bx);
            //Uu.p("tx = " + tx + " ty = " + ty);
            if (bx.contains(x - bx.x, tty - bx.y)) {
                //TODO: if this is InlineBox, we need to find the first previous sibling with a pushStyle
                //Uu.p("matches box: " + bx);
                return bx;
            }
        }

        return null;
    }
    */

    /*
    private int[] adjustForAbsolute(Box bx, int tx, int ty, BlockFormattingContext bfc) {
        //Uu.p("testing: " + bx);
        //Uu.p("is abs");
        //Uu.p("bfc = " + bfc);
        BlockFormattingContext obfc = bx.getPersistentBFC();
        //Uu.p("own bfc = " + obfc);
        if (bfc != null) {
            //int adj_x = tx += bfc.getX();
            //Uu.p("x = " + adj_x);
            //Uu.p("x = " + tx);
            if (bx.left_set) {
                tx -= bx.left;
            }
            if (bx.right_set) {
                int off = (bfc.getWidth() - bx.width - bx.right);
                //Uu.p("offset = " + off);
                tx -= off;
            }
            //Uu.p("final x = " + tx);

            //Uu.p("y = " + ty);
            if (bx.top_set) {
                ty -= bx.top;
            }
            if (bx.bottom_set) {
                int off = (bfc.getHeight() - bx.height - bx.bottom);
                //Uu.p("offset = " + off);
                ty -= off;
            }
            //Uu.p("final y = " + ty);
      
        }

        int[] adjs = new int[2];
        adjs[0] = tx;
        adjs[1] = ty;
        return adjs;
    }
    */



    /**
     * Description of the Method
     *
     * @param box PARAM
     * @param x   PARAM
     * @param y   PARAM
     * @return Returns
     */
    /*
   public int findBoxX(Box box, int x, int y) {
       XRLog.layout(Level.FINEST, "findBox(" + box + " at (" + x + "," + y + ")");
       Iterator it = box.getChildIterator();

       while (it.hasNext()) {
           Box bx = (Box) it.next();
           int tx = x;
           int ty = y;
           tx -= bx.x;
           //is this needed? tx -= bx.totalLeftPadding(c.getCurrentStyle());
           ty -= bx.y;
           //is this needed? ty -= bx.totalTopPadding(c.getCurrentStyle());

           // test the contents
           int retbox = findBoxX(bx, tx, ty);
           if (retbox != -1) {
               return retbox;
           }

           int tty = y;
           if (bx instanceof InlineBox) {
               InlineBox ibx = (InlineBox) bx;
               LineBox lbx = (LineBox) box;
               XRLog.layout(Level.FINEST, "inline = " + ibx);
               XRLog.layout(Level.FINEST, "inline y = " + ibx.y);
               XRLog.layout(Level.FINEST, "inline height = " + ibx.height);
               XRLog.layout(Level.FINEST, "line = " + lbx);
               int off = lbx.getBaseline() + ibx.y - ibx.height;//not really correct
               XRLog.layout(Level.FINEST, "off = " + off);
               tty -= off;
           }

           // test the box itself
           XRLog.layout(Level.FINEST, "bx test = " + bx + " " + x + "," + y);
           if (bx.contains(x - bx.x, tty - bx.y)) {
               return x - bx.x;
           }
       }

       return -1;
   }
   */





    /**
     * Description of the Method
     */
    public void printTree() {
        printTree(getRootBox(), "");
    }

    /**
     * Description of the Method
     *
     * @param box PARAM
     * @param tab PARAM
     */
    private void printTree(Box box, String tab) {
        XRLog.layout(Level.FINEST, tab + "Box = " + box);
        Iterator it = box.getChildIterator();
        while (it.hasNext()) {
            Box bx = (Box) it.next();
            printTree(bx, tab + " ");
        }
    }


    /**
     * Sets the layout attribute of the BasicPanel object
     * Overrides the method to do nothing, since you shouldn't have a
     * LayoutManager on an FS panel.
     *
     * @param l The new layout value
     */
    public void setLayout(LayoutManager l) {
    }

    /**
     * Sets the renderingContext attribute of the BasicPanel object
     *
     * @param ctx The new renderingContext value
     */
    public void setSharedContext(SharedContext ctx) {
        this.sharedContext = ctx;
    }

    /**
     * Sets the errorHandler attribute of the BasicPanel object
     *
     * @param error_handler The new errorHandler value
     */
    public void setErrorHandler(ErrorHandler error_handler) {
        this.error_handler = error_handler;
    }


    /**
     * Sets the antiAliased attribute of the BasicPanel object
     *
     * @param anti_aliased The new antiAliased value
     */
    public void setAntiAliased(boolean anti_aliased) {
        this.anti_aliased = anti_aliased;
    }


    /**
     * Sets the size attribute of the BasicPanel object
     *
     * @param d The new size value
     */
    public void setSize(Dimension d) {
        XRLog.layout(Level.FINEST, "set size called");
        super.setSize(d);
        /* do we need this?
        if (doc != null && body_box != null) {
            if(body_box.width != d.width)
            RenderQueue.getInstance().dispatchLayoutEvent(new ReflowEvent(ReflowEvent.CANVAS_RESIZED, d));
            //don't need the below, surely
            //else if(body_box.height != d.height)
            //    RenderQueue.getInstance().dispatchRepaintEvent(new ReflowEvent(ReflowEvent.CANVAS_RESIZED, d));
    } */
    }






    /* =========== set document utility methods =============== */
    
    
    
    /**
     * Sets the document attribute of the BasicPanel object
     *
     * @param stream The new document value
     * @param url    The new document value
     * @param nsh    The new document value
     */
    public void setDocument(InputStream stream, String url, NamespaceHandler nsh) {
        Document dom = XMLResource.load(stream).getDocument();

        setDocument(dom, url, nsh);
    }


    /**
     * Sets the document attribute of the BasicPanel object
     *
     * @param doc The new document value
     * @param url The new document value
     */
    public void setDocument(Document doc, String url) {
        setDocument(doc, url, new NoNamespaceHandler());
    }

    /**
     * Sets the document attribute of the BasicPanel object
     *
     * @param url The new document value
     */
    public void setDocument(String url) {
        setDocument(loadDocument(url), url, new NoNamespaceHandler());
    }

    /**
     * Sets the document attribute of the BasicPanel object
     *
     * @param url The new document value
     */
    public void setDocument(String url, NamespaceHandler nsh) {
        setDocument(loadDocument(url), url, nsh);
    }

    /**
     * Sets the document attribute of the BasicPanel object
     *
     * @param stream The new document value
     * @param url    The new document value
     * @throws Exception Throws
     */
    protected void setDocument(InputStream stream, String url)
            throws Exception {
        setDocument(stream, url, new NoNamespaceHandler());
    }

    /**
     * Sets the new current document, where the new document
     * is located relative, e.g using a relative URL.
     *
     * @param filename The new document to load
     */
    protected void setDocumentRelative(String filename) {
        String url = getSharedContext().getUac().resolveURI(filename);
        if (isAnchorInCurrentDocument(filename)) {
            String id = getAnchorID(filename);
            Box bxx = getSharedContext().getIDBox(id);
            if (bxx != null) {
                Point pt = BoxFinder.findCoordsByBox(bxx);
                scrollTo(pt);
                return;
            }
        }
        Document dom = loadDocument(url);
        setDocument(dom, url);
    }


    /**
     * Reloads the document using the same base URL and namespace handler. Reloading will pick up changes to styles
     * within the document.
     *
     * @param URI A URI for the Document to load, for example, file.toURL().toExternalForm().
     */
    public void reloadDocument(String URI) {
        reloadDocument(loadDocument(URI));
    }

    /**
     * Reloads the document using the same base URL and namespace handler. Reloading will pick up changes to styles
     * within the document.
     *
     * @param doc The document to reload.
     */
    public void reloadDocument(Document doc) {
        if (this.doc == null) {
            XRLog.render("Reload called on BasicPanel, but there is no document set on the panel yet.");
            return;
        }
        ;
        this.doc = doc;
        setDocument(this.doc, getSharedContext().getBaseURL(), getSharedContext().getNamespaceHandler());
    }


    /**
     * Gets the uRL attribute of the BasicPanel object
     *
     * @return The uRL value
     */
    public URL getURL() {
        URL base = null;
        try {
            base = new URL(getSharedContext().getUac().getBaseURL());
        } catch (MalformedURLException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return base;
    }

    /**
     * Gets the document attribute of the BasicPanel object
     *
     * @return The document value
     */
    public Document getDocument() {
        return doc;
    }

    /**
     * Gets the documentTitle attribute of the BasicPanel object
     *
     * @return The documentTitle value
     */
    public String getDocumentTitle() {
        return getSharedContext().getNamespaceHandler().getDocumentTitle(doc);
    }


    /**
     * Description of the Method
     *
     * @param uri PARAM
     * @return Returns
     */
    protected Document loadDocument(final String uri) {
        return sharedContext.getUac().getXMLResource(uri).getDocument();
    }






    
    
    
    /* ====== hover and active utility methods ========= */
    
    

    /**
     * Gets the hover attribute of the BasicPanel object
     *
     * @param e PARAM
     * @return The hover value
     */
    public boolean isHover(org.w3c.dom.Element e) {
        if (e == hovered_element) {
            return true;
        }
        return false;
    }

    /**
     * Gets the active attribute of the BasicPanel object
     *
     * @param e PARAM
     * @return The active value
     */
    public boolean isActive(org.w3c.dom.Element e) {
        if (e == active_element) {
            return true;
        }
        return false;
    }

    /**
     * Gets the focus attribute of the BasicPanel object
     *
     * @param e PARAM
     * @return The focus value
     */
    public boolean isFocus(org.w3c.dom.Element e) {
        if (e == focus_element) {
            return true;
        }
        return false;
    }


    /**
     * Returns whether the background of this <code>BasicPanel</code> will
     * be painted when it is rendered.
     *
     * @return <code>true</code> if the background of this
     *         <code>BasicPanel</code> will be painted, <code>false</code> if it
     *         will not.
     */
    public boolean isOpaque() {
        checkOpacityMethodClient();
        return explicitlyOpaque;
    }

    /**
     * Specifies whether the background of this <code>BasicPanel</code> will
     * be painted when it is rendered.
     *
     * @param opaque <code>true</code> if the background of this
     *               <code>BasicPanel</code> should be painted, <code>false</code> if it
     *               should not.
     */
    public void setOpaque(boolean opaque) {
        checkOpacityMethodClient();
        explicitlyOpaque = opaque;
    }

    /**
     * Checks that the calling method of the method that calls this method is not in this class
     * and throws a RuntimeException if it was. This is used to ensure that parts of this class that
     * use the opacity to indicate something other than whether the background is painted do not
     * interfere with the user's intentions regarding the background painting.
     *
     * @throws IllegalStateException if the method that called this method was itself called by a
     *                               method in this same class.
     */
    private void checkOpacityMethodClient() {
        StackTraceElement[] stackTrace = new Throwable().getStackTrace();
        if (stackTrace.length > 2) {
            String callingClassName = stackTrace[2].getClassName();
            if (BasicPanel.class.getName().equals(callingClassName))
                throw new IllegalStateException("BasicPanel should not use its own opacity methods. Use " +
                        "super.isOpaque()/setOpaque() instead.");
        }
    }

    /**
     * Gets the context attribute of the BasicPanel object
     *
     * @return The context value
     */
    public SharedContext getSharedContext() {
        return sharedContext;
    }


    /**
     * Gets the fixedRectangle attribute of the BasicPanel object
     *
     * @return The fixedRectangle value
     */
    public Rectangle getFixedRectangle() {
        if (enclosingScrollPane != null) {
            return enclosingScrollPane.getViewportBorderBounds();
        } else {
            Dimension dim = getSize();
            return new Rectangle(0, 0, dim.width, dim.height);
        }
    }

    /**
     * Recalculate the layout of the panel. Only called by paintComponent(). Use
     * calcLayout() instead.
     *
     * @param g PARAM
     */
    /*
   protected void calcLayout(Graphics g, Dimension d) {
       //Uu.p("calc layout with graphics called: " + d);
       layout_thread.startLayout(g, d);
       explicitlyOpaque = super.isOpaque();
   }
   */

    /**
     * Description of the Method
     *
     * @param c
     */
    /*
   protected void doRender(Context c) {
       // paint the normal swing background first
       // but only if we aren't printing.
       Graphics g = c.getGraphics();
       if (!(g instanceof PrinterGraphics) && explicitlyOpaque) {
           g.setColor(getBackground());
           g.fillRect(0, 0, getWidth(), getHeight());
       }
       // start painting the box tree
       if (body_box != null) {
           BoxRendering.paint(c, body_box, false, false);//no restyle demanded on top level
       }
       if (!c.isStylesAllPopped()) {
           XRLog.render(Level.SEVERE, "mismatch in style popping and pushing");
       }
   }
   */

    /**
     * Description of the Method
     */
    protected void fireDocumentLoaded() {
        Iterator it = this.documentListeners.keySet().iterator();
        while (it.hasNext()) {
            DocumentListener list = (DocumentListener) it.next();
            list.documentLoaded();
        }
    }

    private boolean isAnchorInCurrentDocument(String str) {
        if (str.startsWith("#")) {
            return true;
        }
        return false;
    }

    private String getAnchorID(String url) {
        return url.substring(1, url.length());
    }

    /**
     * Scroll the panel to make the specified point be on screen. Typically
     * this will scroll the screen down to the y component of the point.
     */
    public void scrollTo(Point pt) {
        if (this.enclosingScrollPane != null) {
            this.enclosingScrollPane.getVerticalScrollBar().setValue(pt.y);
        }
    }


    public boolean isInteractive() {
        return interactive;
    }

    public void setInteractive(boolean interactive) {
        this.interactive = interactive;
    }
}

/*
 * $Id$
 *
 * $Log$
 * Revision 1.99  2006/02/05 00:33:59  peterbrant
 * Draw fixed layers on every page
 *
 * Revision 1.98  2006/02/03 23:57:55  peterbrant
 * Implement counter(page) and counter(pages) / Bug fixes to alignment calculation
 *
 * Revision 1.97  2006/02/01 01:30:14  peterbrant
 * Initial commit of PDF work
 *
 * Revision 1.96  2006/01/11 22:21:20  peterbrant
 * Fixes to print vs. print preview displays
 *
 * Revision 1.95  2006/01/04 19:50:17  peterbrant
 * More pagination bug fixes / Implement simple pagination for tables
 *
 * Revision 1.94  2006/01/03 23:54:30  peterbrant
 * Fix page clip region checking
 *
 * Revision 1.93  2006/01/03 17:04:52  peterbrant
 * Many pagination bug fixes / Add ability to position absolute boxes in margin area
 *
 * Revision 1.92  2006/01/02 20:58:22  peterbrant
 * Fix NPE
 *
 * Revision 1.91  2006/01/01 03:15:13  peterbrant
 * Fix position of rectangle around page in paged view
 *
 * Revision 1.90  2006/01/01 02:38:21  peterbrant
 * Merge more pagination work / Various minor cleanups
 *
 * Revision 1.89  2005/12/28 00:50:54  peterbrant
 * Continue ripping out first try at pagination / Minor method name refactoring
 *
 * Revision 1.88  2005/12/21 02:36:30  peterbrant
 * - Calculate absolute positions incrementally (prep work for pagination)
 * - Light cleanup
 * - Fix bug where floats nested in floats could cause the outer float to be positioned in the wrong place
 *
 * Revision 1.87  2005/12/07 20:34:47  peterbrant
 * Remove unused fields/methods from RenderingContext / Paint line content using absolute coords (preparation for relative inline layers)
 *
 * Revision 1.86  2005/11/25 16:57:23  peterbrant
 * Initial commit of inline content refactoring
 *
 * Revision 1.85  2005/11/09 18:41:28  peterbrant
 * Fixes to vertical margin collapsing in the presence of floats / Paint floats as
 * layers
 *
 * Revision 1.84  2005/11/08 20:03:59  peterbrant
 * Further progress on painting order / improved positioning implementation
 *
 * Revision 1.83  2005/11/05 03:30:03  peterbrant
 * Start work on painting order and improved positioning implementation
 *
 * Revision 1.82  2005/11/03 18:02:02  peterbrant
 * Flush stylesheets on a RootPanel.setDocument()
 *
 * Revision 1.81  2005/10/27 00:09:07  tobega
 * Sorted out Context into RenderingContext and LayoutContext
 *
 * Revision 1.80  2005/10/26 17:01:44  peterbrant
 * Allow the "use threads" config property to be set on individual instances of
 * XHTMLPanel.
 *
 * Revision 1.79  2005/10/23 22:16:44  tobega
 * Preparation for StackingContext rendering
 *
 * Revision 1.78  2005/10/21 19:54:20  pdoubleya
 * changed logging statements to use XRLog and FINE.
 *
 * Revision 1.77  2005/10/21 19:36:42  peterbrant
 * Paint first time through if sync mode
 *
 * Revision 1.76  2005/10/20 22:51:39  peterbrant
 * Add non-threaded rendering mode
 *
 * Revision 1.75  2005/10/18 20:57:07  tobega
 * Patch from Peter Brant
 *
 * Revision 1.74  2005/10/16 23:57:20  tobega
 * Starting experiment with flat representation of render tree
 *
 * Revision 1.73  2005/10/15 23:39:18  tobega
 * patch from Peter Brant
 *
 * Revision 1.72  2005/10/08 17:40:22  tobega
 * Patch from Peter Brant
 *
 * Revision 1.71  2005/10/02 21:30:00  tobega
 * Fixed a lot of concurrency (and other) issues from incremental rendering. Also some house-cleaning.
 *
 * Revision 1.70  2005/09/29 21:34:05  joshy
 * minor updates to a lot of files. pulling in more incremental rendering code.
 * fixed another resize bug
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.69  2005/09/28 20:13:26  joshy
 * re-enabled body height hack
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.68  2005/09/28 05:17:09  tobega
 * don't layout on resize if doc is null
 *
 * Revision 1.67  2005/09/28 00:33:31  joshy
 * more minor cleanups
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.66  2005/09/28 00:25:16  joshy
 * a bit more cleanup
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.65  2005/09/28 00:03:29  joshy
 * removed cruft from BasicPanel
 * turned of incremental layout and lazy images by default
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.64  2005/09/27 23:48:40  joshy
 * first merge of basicpanel reworking and incremental layout. more to come.
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.63  2005/09/26 22:40:22  tobega
 * Applied patch from Peter Brant concerning margin collapsing
 *
 * Revision 1.62  2005/07/21 16:10:42  joshy
 * added hack to expand the body. fix for bug 96
 *
 * Revision 1.61  2005/07/18 21:21:20  joshy
 * fix for #82
 *
 * Revision 1.60  2005/07/15 23:39:49  joshy
 * updates to try to fix the resize issue
 *
 * Revision 1.59  2005/07/07 22:13:52  tobega
 * cleanup
 *
 * Revision 1.58  2005/07/07 21:56:45  tobega
 * Added patches from Changshin Lee
 *
 * Revision 1.57  2005/07/02 07:26:59  joshy
 * better support for jumping to anchor tags
 * also some testing for the resize issue
 * need to investigate making the history remember document position.
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.56  2005/06/25 19:27:47  tobega
 * UAC now supplies Resources
 *
 * Revision 1.55  2005/06/22 23:48:46  tobega
 * Refactored the css package to allow a clean separation from the core.
 *
 * Revision 1.54  2005/06/19 23:31:33  joshy
 * stop layout support
 * clear bug fixes
 * mouse busy cursor support
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.53  2005/06/16 12:59:24  pdoubleya
 * Cleaned up support for reloading documents.
 *
 * Revision 1.52  2005/06/16 11:29:13  pdoubleya
 * First cut support for reload page, flushes inline stylesheets.
 *
 * Revision 1.51  2005/06/16 07:24:52  tobega
 * Fixed background image bug.
 * Caching images in browser.
 * Enhanced LinkListener.
 * Some house-cleaning, playing with Idea's code inspection utility.
 *
 * Revision 1.50  2005/06/15 11:59:17  pdoubleya
 * Added dynamic layout message.
 *
 * Revision 1.49  2005/06/15 11:53:46  tobega
 * Changed UserAgentCallback to getInputStream instead of getReader. Fixed up some consequences of previous change.
 *
 * Revision 1.48  2005/06/15 10:56:15  tobega
 * cleaned up a bit of URL mess, centralizing URI-resolution and loading to UserAgentCallback
 *
 * Revision 1.47  2005/06/09 22:34:57  joshy
 * This makes the hover listener be added to the xhtml panel by default.
 * Also improves the box searching code by testing if the parent of the deepest
 * box is hoverable in the case where the deepest box is not.
 *
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.46  2005/06/01 21:36:44  tobega
 * Got image scaling working, and did some refactoring along the way
 *
 * Revision 1.45  2005/05/13 15:23:56  tobega
 * Done refactoring box borders, margin and padding. Hover is working again.
 *
 * Revision 1.44  2005/05/09 20:11:30  tobega
 * Improved the bfc hack for top level document
 *
 * Revision 1.43  2005/05/08 13:02:41  tobega
 * Fixed a bug whereby styles could get lost for inline elements, notably if root element was inline. Did a few other things which probably has no importance at this moment, e.g. refactored out some unused stuff.
 *
 * Revision 1.42  2005/04/22 17:12:50  joshy
 * fixed hover breaking with absolute blocks
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.41  2005/04/21 22:34:57  tobega
 * Fixed an instability in rendering arbitrary xml (added default style to start off with)
 *
 * Revision 1.40  2005/02/05 17:20:10  pdoubleya
 * Use XMLResource for loading XML.
 *
 * Revision 1.39  2005/02/05 11:33:49  pdoubleya
 * Load pages directly from XMLResource.
 *
 * Revision 1.38  2005/02/03 23:02:31  pdoubleya
 * Uses ResourceProvider for loading files.
 *
 * Revision 1.37  2005/01/31 22:54:14  pdoubleya
 * Adjusted calcs for findBox and findElementBox.
 *
 * Revision 1.36  2005/01/29 20:17:42  pdoubleya
 * Updated panels to support page up/down properly, and formatted/cleaned.
 *
 * Revision 1.35  2005/01/25 14:45:54  pdoubleya
 * Added support for IdentValue mapping on property declarations. On both CascadedStyle and PropertyDeclaration you can now request the value as an IdentValue, for object-object comparisons. Updated 99% of references that used to get the string value of PD to return the IdentValue instead; remaining cases are for pseudo-elements where the PD content needs to be manipulated as a String.
 *
 * Revision 1.34  2005/01/16 18:50:06  tobega
 * Re-introduced caching of styles, which make hamlet and alice scroll nicely again. Background painting still slow though.
 *
 * Revision 1.33  2005/01/10 01:58:37  tobega
 * Simplified (and hopefully improved) handling of vertical-align. Added support for line-height. As always, provoked a few bugs in the process.
 *
 * Revision 1.32  2005/01/08 15:56:55  tobega
 * Further work on extensibility interfaces. Documented it - see website.
 *
 * Revision 1.31  2005/01/08 11:55:18  tobega
 * Started massaging the extension interfaces
 *
 * Revision 1.30  2005/01/07 12:42:08  tobega
 * Hacked improved support for custom components (read forms). Creates trouble with the image demo. Anyway, components work and are usually in the right place.
 *
 * Revision 1.29  2005/01/06 21:54:33  tobega
 * Text decoration now handled in rendering only
 *
 * Revision 1.28  2005/01/05 01:10:16  tobega
 * Went wild with code analysis tool. removed unused stuff. Lucky we have CVS...
 *
 * Revision 1.27  2005/01/03 00:35:03  tobega
 * Cleaned out old forms code
 *
 * Revision 1.26  2005/01/02 12:22:20  tobega
 * Cleaned out old layout code
 *
 * Revision 1.25  2005/01/02 09:32:41  tobega
 * Now using mostly static methods for layout
 *
 * Revision 1.24  2005/01/02 01:00:09  tobega
 * Started sketching in code for handling replaced elements in the NamespaceHandler
 *
 * Revision 1.23  2005/01/01 08:09:21  tobega
 * Now using entirely static methods for render. Need to implement table. Need to clean.
 *
 * Revision 1.22  2004/12/29 10:39:35  tobega
 * Separated current state Context into LayoutContext and the rest into SharedContext.
 *
 * Revision 1.21  2004/12/29 07:35:39  tobega
 * Prepared for cloned Context instances by encapsulating fields
 *
 * Revision 1.20  2004/12/28 01:48:24  tobega
 * More cleaning. Magically, the financial report demo is starting to look reasonable, without any effort being put on it.
 *
 * Revision 1.19  2004/12/27 07:43:33  tobega
 * Cleaned out border from box, it can be gotten from current style. Is it maybe needed for dynamic stuff?
 *
 * Revision 1.18  2004/12/22 05:32:57  tobega
 * Hover is working
 *
 * Revision 1.17  2004/12/14 01:56:23  joshy
 * fixed layout width bugs
 * fixed extra border on document bug
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.16  2004/12/14 00:32:21  tobega
 * Cleaned and fixed line breaking. Renamed BodyContent to DomToplevelNode
 *
 * Revision 1.15  2004/12/12 03:33:02  tobega
 * Renamed x and u to avoid confusing IDE. But that got cvs in a twist. See if this does it
 *
 * Revision 1.14  2004/12/11 18:18:11  tobega
 * Still broken, won't even compile at the moment. Working hard to fix it, though. Replace the StyleReference interface with our only concrete implementation, it was a bother changing in two places all the time.
 *
 * Revision 1.13  2004/12/09 18:00:06  joshy
 * fixed hover bugs
 * fixed li's not being blocks bug
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.12  2004/12/09 00:11:52  tobega
 * Almost ready for Content-based inline generation.
 *
 * Revision 1.11  2004/12/06 02:52:22  tobega
 * re-inserted reference to Class Xx, which IntelliJ thought was unused
 *
 * Revision 1.10  2004/12/06 00:19:15  tobega
 * Worked on handling :before and :after. Got sidetracked by BasicPanel causing layout to be done twice: solved. If solution causes problems, check BasicPanel.setSize
 *
 * Revision 1.9  2004/11/30 20:28:28  joshy
 * support for multiple floats on a single line.
 *
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.8  2004/11/29 23:28:11  joshy
 * updated the javadocs
 * added media methods to RenderingContext
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.7  2004/11/23 21:19:22  joshy
 * added support for loading a document from an InputStream
 * (for cases where you don't have a Document or URL)
 *
 * - j
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.6  2004/11/23 18:38:48  joshy
 * removed isPrinting() method from rendering context because it's
 * not needed. the panel can detect printing by checking for
 * instanceof PrinterGraphics
 *
 * -j
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.5  2004/11/22 21:34:04  joshy
 * created new whitespace handler.
 * new whitespace routines only work if you set a special property. it's
 * off by default.
 *
 * turned off fractional font metrics
 *
 * fixed some bugs in Uu and Xx
 *
 * - j
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.4  2004/11/16 15:38:44  joshy
 * removed background printing which speeds it up considerably
 * added boolean in conf to turn off backgrounds for testing
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.3  2004/11/16 10:30:20  pdoubleya
 * Changed to use XRLog for logging.
 * Ran code formatter.
 * Marked blocks needed to CLEAN.
 *
 * Revision 1.2  2004/11/16 10:14:11  pdoubleya
 * Was not importing util.Xx, so was assuming that Xx was a member variable in the superclass. Added import.
 *
 * Revision 1.1  2004/11/16 07:25:13  tobega
 * Renamed HTMLPanel to BasicPanel
 *
 * Revision 1.33  2004/11/14 21:33:49  joshy
 * new font rendering interface support
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.32  2004/11/12 20:54:08  joshy
 * fixed bug where setOpaque would become false for a page with fixed content and then never
 * become true again on another normal page
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.31  2004/11/12 18:51:01  joshy
 * fixed repainting issue for background-attachment: fixed
 * added static util methods and get minimum size to graphics 2d renderer
 * added test for graphics 2d renderer
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.30  2004/11/12 17:05:25  joshy
 * support for fixed positioning
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.29  2004/11/12 02:54:38  joshy
 * removed more dead code
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.28  2004/11/12 02:50:59  joshy
 * finished moving base url
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.27  2004/11/12 02:23:59  joshy
 * added new APIs for rendering context, xhtmlpanel, and graphics2drenderer.
 * initial support for font mapping additions
 *
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.26  2004/11/12 01:42:26  tobega
 * oops
 *
 * Revision 1.25  2004/11/12 01:39:07  tobega
 * no message
 *
 * Revision 1.24  2004/11/12 00:20:31  tobega
 * Set up the HoverListener to work properly. Colors are changing!
 *
 * Revision 1.23  2004/11/10 14:34:21  joshy
 * more hover support
 *
 * Revision 1.22  2004/11/10 04:46:12  tobega
 * no message
 *
 * Revision 1.21  2004/11/07 23:24:19  joshy
 * added menu item to generate diffs
 * added diffs for multi-colored borders and inline borders
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.20  2004/11/05 23:59:10  tobega
 * "New" matching and styling
 *
 * Revision 1.19  2004/11/04 21:51:17  tobega
 * Preparation for new matching/styling code
 *
 * Revision 1.18  2004/11/03 23:54:34  joshy
 * added hamlet and tables to the browser
 * more support for absolute layout
 * added absolute layout unit tests
 * removed more dead code and moved code into layout factory
 *
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.17  2004/11/01 14:24:19  joshy
 * added a boolean for turning off threading
 * fixed the diff tests
 * removed some dead code
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.16  2004/10/28 14:18:23  joshy
 * cleaned up the htmlpanel and made more of the variables protected
 * fixed the bug where the body is too small for the viewport
 * fixed the bug where the screen isn't re-laid out when the window is resized
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.15  2004/10/28 13:46:33  joshy
 * removed dead code
 * moved code about specific elements to the layout factory (link and br)
 * fixed form rendering bug
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.14  2004/10/27 13:39:57  joshy
 * moved more rendering code out of the layouts
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.13  2004/10/27 13:17:02  joshy
 * beginning to split out rendering code
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.12  2004/10/27 04:08:44  joshy
 * removed debugging code
 *
 * Revision 1.11  2004/10/26 00:13:14  joshy
 * added threaded layout support to the BasicPanel
 *
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.10  2004/10/23 13:51:54  pdoubleya
 * Re-formatted using JavaStyle tool.
 * Cleaned imports to resolve wildcards except for common packages (java.io, java.util, etc).
 * Added CVS log comments at bottom.
 *
 * Revision 1.9  2004/10/18 12:12:26  pdoubleya
 * Changed to use XRLog for logging.
 *
 * Revision 1.8  2004/10/14 15:45:22  pdoubleya
 * Reformatted.
 *
 * Revision 1.7  2004/10/14 15:43:32  pdoubleya
 * Reads location of default.CSS from configuration file.
 *
 *
 */

