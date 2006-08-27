package org.xhtmlrenderer.swing;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JViewport;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xhtmlrenderer.event.DocumentListener;
import org.xhtmlrenderer.extend.NamespaceHandler;
import org.xhtmlrenderer.extend.ReplacedElementFactory;
import org.xhtmlrenderer.extend.UserInterface;
import org.xhtmlrenderer.layout.BoxBuilder;
import org.xhtmlrenderer.layout.Layer;
import org.xhtmlrenderer.layout.LayoutContext;
import org.xhtmlrenderer.layout.SharedContext;
import org.xhtmlrenderer.render.BlockBox;
import org.xhtmlrenderer.render.Box;
import org.xhtmlrenderer.render.Java2DFontContext;
import org.xhtmlrenderer.render.PageBox;
import org.xhtmlrenderer.render.ReflowEvent;
import org.xhtmlrenderer.render.RenderQueue;
import org.xhtmlrenderer.render.RenderingContext;
import org.xhtmlrenderer.util.Configuration;
import org.xhtmlrenderer.util.Uu;
import org.xhtmlrenderer.util.XRLog;


public class RootPanel extends JPanel implements ComponentListener, UserInterface {
    static final long serialVersionUID = 1L;
    
    private boolean useThreads;

    public RootPanel(boolean useThreads) {
        this.useThreads = useThreads;
    }

    public RootPanel() {
        this(Configuration.isTrue("xr.use.threads", true));
    }

    protected Map documentListeners;

    public SharedContext getSharedContext() {
        return sharedContext;
    }

    protected SharedContext sharedContext;

    //TODO: layout_context should not be stored!
    protected volatile LayoutContext layout_context;

    private Box rootBox = null;

    private Thread layoutThread;
    private Thread renderThread;
    
    private boolean pendingResize = false;

    public void setDocument(Document doc, String url, NamespaceHandler nsh) {
        resetScrollPosition();
        setRootBox(null);
        this.doc = doc;

        //have to do this first
        if (Configuration.isTrue("xr.cache.stylesheets", true)) {
            getSharedContext().getCss().flushStyleSheets();
        } else {
            getSharedContext().getCss().flushAllStyleSheets();
        }
        getSharedContext().reset();
        getSharedContext().setBaseURL(url);
        getSharedContext().setNamespaceHandler(nsh);
        getSharedContext().getCss().setDocumentContext(getSharedContext(), getSharedContext().getNamespaceHandler(), doc, this);

        if (isUseThreads()) {
            queue.dispatchLayoutEvent(new ReflowEvent(ReflowEvent.DOCUMENT_SET));
        } else {
            repaint();
        }
    }

    protected JScrollPane enclosingScrollPane;
    public void resetScrollPosition() {
        if (this.enclosingScrollPane != null) {
            this.enclosingScrollPane.getVerticalScrollBar().setValue(0);
        }
    }

    /**
     * The method is invoked by {@link #addNotify} and {@link #removeNotify} to
     * ensure that any enclosing {@link JScrollPane} works correctly with this
     * panel. This method can be safely invoked with a <tt>null</tt> scrollPane.
     *
     * @param scrollPane the enclosing {@link JScrollPane} or <tt>null</tt> if
     *                   the panel is no longer enclosed in a {@link JScrollPane}.
     */
    protected void setEnclosingScrollPane(JScrollPane scrollPane) {
        // if a scrollpane is already installed we remove it.
        if (enclosingScrollPane != null) {
            enclosingScrollPane.removeComponentListener(this);
        }

        enclosingScrollPane = scrollPane;

        if (enclosingScrollPane != null) {
            Uu.p("added root panel as a component listener to the scroll pane");
            enclosingScrollPane.addComponentListener(this);
            default_scroll_mode = enclosingScrollPane.getViewport().getScrollMode();
        }
    }

    private int default_scroll_mode = -1;

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
     * Overrides the default implementation to test for and configure any {@link
     * JScrollPane} parent.
     */
    public void addNotify() {
        super.addNotify();
        XRLog.general(Level.FINE, "add notify called");
        Container p = getParent();
        if (p instanceof JViewport) {
            Container vp = p.getParent();
            if (vp instanceof JScrollPane) {
                setEnclosingScrollPane((JScrollPane) vp);
            }
        }
    }

    /**
     * Overrides the default implementation unconfigure any {@link JScrollPane}
     * parent.
     */
    public void removeNotify() {
        super.removeNotify();
        setEnclosingScrollPane(null);
    }

    protected Document doc = null;

    /**
     * The queue to handle painting and layout events
     */
    RenderQueue queue;

    protected void init() {


        documentListeners = new HashMap();
        setBackground(Color.white);
        super.setLayout(null);

        if (isUseThreads()) {
            queue = new RenderQueue();

            layoutThread = new Thread(new LayoutLoop(this), "FlyingSaucer-Layout");
            renderThread = new Thread(new RenderLoop(this), "FlyingSaucer-Render");

            layoutThread.start();
            renderThread.start();
        }
    }

    public synchronized void shutdown() {
        try {
            if (layoutThread != null) {
                layoutThread.interrupt();
                layoutThread.join();
                layoutThread = null;
            }

            if (renderThread != null) {
                renderThread.interrupt();
                renderThread.join();
                renderThread = null;
            }

        } catch (InterruptedException e) {
            // ignore
        }
    }

    int rendered_width = 0;

    protected int getRenderWidth() {
        return rendered_width;
    }

    protected void setRenderWidth(int renderWidth) {
        this.rendered_width = renderWidth;
    }


    boolean layoutInProgress = false;


    public ReflowEvent last_event = null;

    protected RenderingContext newRenderingContext(Graphics2D g) {
        XRLog.layout(Level.FINEST, "new context begin");

        getSharedContext().setCanvas(this);

        XRLog.layout(Level.FINEST, "new context end");
        
        RenderingContext result = getSharedContext().newRenderingContextInstance();
        result.setFontContext(new Java2DFontContext(g));
        result.setOutputDevice(new Java2DOutputDevice(g));
        
        getSharedContext().getTextRenderer().setup(result.getFontContext());

        return result;
    }

    protected LayoutContext newLayoutContext(Graphics2D g) {
        XRLog.layout(Level.FINEST, "new context begin");

        getSharedContext().setCanvas(this);

        Rectangle extents = getScreenExtents();
        
        // HACK avoid bogus warning
        if (extents.width == 0 && extents.height == 0) {
            extents = new Rectangle(0, 0, 1, 1);
        }

        XRLog.layout(Level.FINEST, "new context end");

        LayoutContext result = getSharedContext().newLayoutContextInstance(extents);
        Graphics2D layoutGraphics = 
            g.getDeviceConfiguration().createCompatibleImage(1, 1).createGraphics();
        result.setFontContext(new Java2DFontContext(layoutGraphics));
        result.setReplacedElementFactory(newReplacedElementFactory());
        
        getSharedContext().getTextRenderer().setup(result.getFontContext());
        
        if (result.isPrint()) {
            PageBox first = Layer.createPageBox(result, "first");
            extents = new Rectangle(0, 0, 
                    first.getContentWidth(result), first.getContentHeight(result));
            result.setExtents(extents);
        }
        
        return result;
    }

    public ReplacedElementFactory newReplacedElementFactory() {
        return new SwingReplacedElementFactory();
    }

    public Rectangle getScreenExtents() {
        Rectangle extents;
        if (enclosingScrollPane != null) {
            Rectangle bnds = enclosingScrollPane.getViewportBorderBounds();
            extents = new Rectangle(0, 0, bnds.width, bnds.height);
            //Uu.p("bnds = " + bnds);
        } else {
            extents = new Rectangle(getWidth(), getHeight());//200, 200 ) );
        }
        return extents;
    }

    public void doActualLayout(Graphics g) {
        //Uu.p("doActualLayout called");
        this.removeAll();
        if (g == null) {
            return;
        }
        if (doc == null) {
            return;
        }
        
        LayoutContext c = newLayoutContext((Graphics2D) g);
        synchronized (this) {
            if (this.layout_context != null) this.layout_context.stopRendering();
            this.layout_context = c;
        }
        c.setRenderQueue(queue);
        setRenderWidth((int) c.getExtents().getWidth());
        
        long start = System.currentTimeMillis();
        
        BlockBox root = (BlockBox)getRootBox(); 
        if (root != null && isPendingResize()) {
            root.reset(c);
        } else {
            root = BoxBuilder.createRootBox(c, doc);
            setRootBox(root);            
        }
        
        root.layout(c);
        
        long end = System.currentTimeMillis();
        
        XRLog.layout(Level.INFO, "Layout took " + (end - start) + "ms");
        
        if (!c.isStylesAllPopped()) {
            XRLog.layout(Level.SEVERE, "mismatch in style popping and pushing");
        }
        
        if (c.shouldStop()) {//interrupted layout
            return;
        }
// if there is a fixed child then we need to set opaque to false
// so that the entire viewport will be repainted. this is slower
// but that's the hit you get from using fixed layout
        if (root.getLayer().containsFixedContent()) {
            super.setOpaque(false);
        } else {
            super.setOpaque(true);
        }
        
        XRLog.layout(Level.FINEST, "after layout: " + root);
        
        Dimension intrinsic_size = root.getLayer().getPaintingDimension(c);
        
        if (c.isPrint()) {
            root.getLayer().trimEmptyPages(c, intrinsic_size.height);
        }
        
        setPreferredSize(intrinsic_size);
        revalidate();
        
        // if doc is shorter than viewport
        // then stretch canvas to fill viewport exactly
        // then adjust the body element accordingly
        if (enclosingScrollPane != null) {
            if (intrinsic_size.height < enclosingScrollPane.getViewport().getHeight()) {
                //Uu.p("int height is less than viewport height");
                // XXX Not threadsafe
                if (enclosingScrollPane.getViewport().getHeight() != this.getHeight()) {
                    this.setPreferredSize(new Dimension(
                            intrinsic_size.width, enclosingScrollPane.getViewport().getHeight()));
                    this.revalidate();
                }
                //Uu.p("need to do the body hack");
                if (root != null && ! c.isPrint()) {
                    root.height = enclosingScrollPane.getViewport().getHeight();
                    bodyExpandHack(root, root.height);
                    intrinsic_size.height = root.height;
                }
            } 
            
            // turn on simple scrolling mode if there's any fixed elements
            if (root.getLayer().containsFixedContent()) {
                // Uu.p("is fixed");
                enclosingScrollPane.getViewport().setScrollMode(JViewport.SIMPLE_SCROLL_MODE);
            } else {
                // Uu.p("is not fixed");
                enclosingScrollPane.getViewport().setScrollMode(default_scroll_mode);
            }
        } 

        if (isUseThreads()) {
            queue.dispatchRepaintEvent(new ReflowEvent(ReflowEvent.LAYOUT_COMPLETE));
        }
        this.fireDocumentLoaded();
    }

    private static void bodyExpandHack(Box root, int view_height) {
        for (int i = 0; i < root.getChildCount(); i++) {
            // set the html box to the max
            Box html = root.getChild(i);
            if (html.element != null && html.element.getNodeName().equals("html")) {
                html.height = view_height;
                // set the body box to the max
                for (int j = 0; j < html.getChildCount(); j++) {
                    Box body = html.getChild(j);
                    if (body.element != null && body.element.getNodeName().equals("body")) {
                        body.height = view_height;
                    }
                }
            }
        }
    }

    protected void fireDocumentLoaded() {
        Iterator it = this.documentListeners.keySet().iterator();
        while (it.hasNext()) {
            DocumentListener list = (DocumentListener) it.next();
            list.documentLoaded();
        }
    }


    /*
    * ========= UserInterface implementation ===============
    */
    public Element hovered_element = null;

    public Element active_element = null;

    public Element focus_element = null;

    public boolean isHover(org.w3c.dom.Element e) {
        if (e == hovered_element) {
            return true;
        }
        return false;
    }

    public boolean isActive(org.w3c.dom.Element e) {
        if (e == active_element) {
            return true;
        }
        return false;
    }

    public boolean isFocus(org.w3c.dom.Element e) {
        if (e == focus_element) {
            return true;
        }
        return false;
    }

    public void componentHidden(ComponentEvent e) {
    }

    public void componentMoved(ComponentEvent e) {
    }

    public void componentResized(ComponentEvent e) {
        Uu.p("componentResized() " + this.getSize());
        Uu.p("viewport = " + enclosingScrollPane.getViewport().getSize());
        if (! getSharedContext().isPrint()) {
            relayout(enclosingScrollPane.getViewport().getSize());
        }
    }
    
    protected void relayout(Dimension viewportSize) {
        if (doc != null) {
            if (isUseThreads()) {
                queue.dispatchLayoutEvent(new ReflowEvent(ReflowEvent.CANVAS_RESIZED,
                        viewportSize));
            } else {
                setPendingResize(true);
                repaint();
            }
        }
    }

    public void componentShown(ComponentEvent e) {
    }

    public double getLayoutWidth() {
        if (enclosingScrollPane != null) {
            return enclosingScrollPane.getViewportBorderBounds().width;
        } else {
            return getSize().width;
        }
    }

    public boolean isPrintView() {
        return false;
    }

    public boolean isUseThreads() {
        return useThreads;
    }

    public void setUseThreads(boolean useThreads) {
        this.useThreads = useThreads;
    }
    
    public synchronized Box getRootBox() {
        return rootBox;
    }
    
    public synchronized void setRootBox(Box rootBox) {
        this.rootBox = rootBox;
    }

    public synchronized Layer getRootLayer() {
        return getRootBox() == null ? null : getRootBox().getLayer();
    }
    
    public Box find(MouseEvent e) {
        return find(e.getX(), e.getY());
    }
    
    public Box find(int x, int y) {
        Layer l = getRootLayer();
        if (l != null) {
            return l.find(layout_context, x, y);
        }
        return null;
    }

    protected synchronized boolean isPendingResize() {
        return pendingResize;
    }

    protected synchronized void setPendingResize(boolean pendingResize) {
        this.pendingResize = pendingResize;
    }
}
