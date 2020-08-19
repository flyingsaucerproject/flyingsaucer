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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
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
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.print.PrinterGraphics;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import javax.swing.JOptionPane;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import org.w3c.dom.Document;
import org.xhtmlrenderer.css.style.CalculatedStyle;
import org.xhtmlrenderer.css.style.derived.RectPropertySet;
import org.xhtmlrenderer.extend.NamespaceHandler;
import org.xhtmlrenderer.extend.UserAgentCallback;
import org.xhtmlrenderer.layout.Layer;
import org.xhtmlrenderer.layout.SharedContext;
import org.xhtmlrenderer.render.Box;
import org.xhtmlrenderer.render.PageBox;
import org.xhtmlrenderer.render.RenderingContext;
import org.xhtmlrenderer.resource.XMLResource;
import org.xhtmlrenderer.simple.NoNamespaceHandler;
import org.xhtmlrenderer.simple.extend.FormSubmissionListener;
import org.xhtmlrenderer.util.Configuration;
import org.xhtmlrenderer.util.Uu;
import org.xhtmlrenderer.util.XRLog;
import org.xml.sax.InputSource;




/**
 * A Swing {@link javax.swing.JPanel} that encloses the Flying Saucer renderer
 * for easy integration into Swing applications.
 *
 * @author Joshua Marinacci
 */
public abstract class BasicPanel extends RootPanel implements FormSubmissionListener {
    private static final int PAGE_PAINTING_CLEARANCE_WIDTH = 10;
    private static final int PAGE_PAINTING_CLEARANCE_HEIGHT = 10;

    private boolean explicitlyOpaque;

    private final MouseTracker mouseTracker;
    private boolean centeredPagedView;
    protected FormSubmissionListener formSubmissionListener;

    public BasicPanel() {
        this(new NaiveUserAgent());
    }

    public BasicPanel(UserAgentCallback uac) {
        sharedContext = new SharedContext(uac);
        mouseTracker = new MouseTracker(this);
        formSubmissionListener = new FormSubmissionListener() {
            public void submit(String query) {
                System.out.println("Form Submitted!");
                System.out.println("Data: " + query);

                JOptionPane.showMessageDialog(
                        null,
                        "Form submit called; check console to see the query string" +
                        " that would have been submitted.",
                        "Form Submission",
                        JOptionPane.INFORMATION_MESSAGE
                );
            }

        };
        sharedContext.setFormSubmissionListener(formSubmissionListener);
        init();
    }

    public void paintComponent(Graphics g) {
        if (doc == null) {
            paintDefaultBackground(g);
            return;
        }

        // if this is the first time painting this document, then calc layout
        Layer root = getRootLayer();
        if (root == null || isNeedRelayout()) {
            Graphics gg = g.create();
            try {
                doDocumentLayout(gg);
            } finally {
                gg.dispose();
            }
            root = getRootLayer();
        }
        setNeedRelayout(false);
        if (root == null) {
            //Uu.p("dispatching an initial resize event");
            //queue.dispatchLayoutEvent(new ReflowEvent(ReflowEvent.CANVAS_RESIZED, this.getSize()));
            XRLog.render(Level.FINE, "skipping the actual painting");
        } else {
            Graphics gg = g.create();
            try {
                RenderingContext c = newRenderingContext((Graphics2D) gg);
                long start = System.currentTimeMillis();
                doRender(c, root);
                long end = System.currentTimeMillis();
                XRLog.render(Level.FINE, "RENDERING TOOK " + (end - start) + " ms");
            } finally {
                gg.dispose();
            }
        }
    }

    protected void doRender(RenderingContext c, Layer root) {
        try {
            // paint the normal swing background first
            // but only if we aren't printing.
            Graphics g = ((Java2DOutputDevice)c.getOutputDevice()).getGraphics();

            paintDefaultBackground(g);

            JScrollPane scrollPane = getEnclosingScrollPane();
            if (scrollPane == null) {
                Insets insets = getInsets();
                g.translate(insets.left, insets.top);
            }

            long start = System.currentTimeMillis();
            if (!c.isPrint()) {
                root.paint(c);
            } else {
                paintPagedView(c, root);
            }
            long after = System.currentTimeMillis();
            if (Configuration.isTrue("xr.incremental.repaint.print-timing", false)) {
                Uu.p("repaint took ms: " + (after - start));
            }
        } catch (ThreadDeath t) {
            throw t;
        } catch (Throwable t) {
            if (hasDocumentListeners()) {
                fireOnRenderException(t);
            } else {
                if (t instanceof Error) {
                    throw (Error)t;
                }
                if (t instanceof RuntimeException) {
                    throw (RuntimeException)t;
                }

                // "Shouldn't" happen
                XRLog.exception(t.getMessage(), t);
            }
        }
    }

    private void paintDefaultBackground(Graphics g) {
        if (!(g instanceof PrinterGraphics) && explicitlyOpaque) {
            g.setColor(getBackground());
            g.fillRect(0, 0, getWidth(), getHeight());
        }
    }

    private void paintPagedView(RenderingContext c, Layer root) {
        if (root.getLastPage() == null) {
            return;
        }

        final int pagePaintingClearanceWidth = isCenteredPagedView() ?
                calcCenteredPageLeftOffset(root.getMaxPageWidth(c, 0)) :
                PAGE_PAINTING_CLEARANCE_WIDTH;
        root.assignPagePaintingPositions(
                c, Layer.PAGED_MODE_SCREEN, PAGE_PAINTING_CLEARANCE_HEIGHT);

        setPreferredSize(new Dimension(
                root.getMaxPageWidth(c, pagePaintingClearanceWidth),
                root.getLastPage().getPaintingBottom() + PAGE_PAINTING_CLEARANCE_HEIGHT));
        revalidate();

        Graphics2D g = ((Java2DOutputDevice)c.getOutputDevice()).getGraphics();
        Shape working = g.getClip();

        List pages = root.getPages();
        c.setPageCount(pages.size());
        for (int i = 0; i < pages.size(); i++) {
            PageBox page = (PageBox)pages.get(i);
            c.setPage(i, page);

            g.setClip(working);

            Rectangle overall = page.getScreenPaintingBounds(c, pagePaintingClearanceWidth);
            overall.x -= 1;
            overall.y -= 1;
            overall.width += 1;
            overall.height += 1;

            Rectangle bounds = new Rectangle(overall);
            bounds.width += 1;
            bounds.height += 1;
            if (working.intersects(bounds)) {
                page.paintBackground(c, pagePaintingClearanceWidth, Layer.PAGED_MODE_SCREEN);
                page.paintMarginAreas(c, pagePaintingClearanceWidth, Layer.PAGED_MODE_SCREEN);
                page.paintBorder(c, pagePaintingClearanceWidth, Layer.PAGED_MODE_SCREEN);

                Color old = g.getColor();

                g.setColor(Color.BLACK);
                g.drawRect(overall.x, overall.y, overall.width, overall.height);
                g.setColor(old);

                Rectangle content = page.getPagedViewClippingBounds(c, pagePaintingClearanceWidth);
                g.clip(content);

                int left = pagePaintingClearanceWidth +
                    page.getMarginBorderPadding(c, CalculatedStyle.LEFT);
                int top = page.getPaintingTop()
                    + page.getMarginBorderPadding(c, CalculatedStyle.TOP)
                    - page.getTop();

                g.translate(left, top);
                root.paint(c);
                g.translate(-left, -top);

                g.setClip(working);
            }
        }

        g.setClip(working);
    }

    private int calcCenteredPageLeftOffset(int maxPageWidth) {
        return (getWidth() - maxPageWidth) / 2;
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
        c.setPageCount(root.getPages().size());
        c.setPage(pageNo, page);

        page.paintBackground(c, 0, Layer.PAGED_MODE_PRINT);
        page.paintMarginAreas(c, 0, Layer.PAGED_MODE_PRINT);
        page.paintBorder(c, 0, Layer.PAGED_MODE_PRINT);

        Shape working = g.getClip();

        Rectangle content = page.getPrintClippingBounds(c);
        g.clip(content);

        int top = -page.getPaintingTop() +
            page.getMarginBorderPadding(c, CalculatedStyle.TOP);

        int left = page.getMarginBorderPadding(c, CalculatedStyle.LEFT);

        g.translate(left, top);
        root.paint(c);
        g.translate(-left, -top);

        g.setClip(working);
    }

    public void assignPagePrintPositions(Graphics2D g) {
        RenderingContext c = newRenderingContext(g);
        getRootLayer().assignPagePaintingPositions(c, Layer.PAGED_MODE_PRINT);
    }

    public void printTree() {
        printTree(getRootBox(), "");
    }

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

    public void setSharedContext(SharedContext ctx) {
        this.sharedContext = ctx;
    }

    public void setSize(Dimension d) {
        XRLog.layout(Level.FINEST, "set size called");
        super.setSize(d);
        /* CLEAN: do we need this?
        if (doc != null && body_box != null) {
            if(body_box.width != d.width)
            RenderQueue.getInstance().dispatchLayoutEvent(new ReflowEvent(ReflowEvent.CANVAS_RESIZED, d));
            //don't need the below, surely
            //else if(body_box.height != d.height)
            //    RenderQueue.getInstance().dispatchRepaintEvent(new ReflowEvent(ReflowEvent.CANVAS_RESIZED, d));
    } */
    }

    /*
=========== set document utility methods =============== */

    public void setDocument(InputStream stream, String url, NamespaceHandler nsh) {
        Document dom = XMLResource.load(stream).getDocument();

        setDocument(dom, url, nsh);
    }

    public void setDocumentFromString(String content, String url, NamespaceHandler nsh) {
        InputSource is = new InputSource(new BufferedReader(new StringReader(content)));
        Document dom = XMLResource.load(is).getDocument();

        setDocument(dom, url, nsh);
    }

    public void setDocument(Document doc, String url) {
        setDocument(doc, url, new NoNamespaceHandler());
    }

    public void setDocument(String url) {
        setDocument(loadDocument(url), url, new NoNamespaceHandler());
    }

    public void setDocument(String url, NamespaceHandler nsh) {
        setDocument(loadDocument(url), url, nsh);
    }

    // TODO: should throw more specific exception (PWW 25/07/2006)
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
            String id = getAnchorId(filename);
            Box box = getSharedContext().getBoxById(id);
            if (box != null) {
                Point pt;
                if (box.getStyle().isInline()) {
                    pt = new Point(box.getAbsX(), box.getAbsY());
                } else {
                    RectPropertySet margin = box.getMargin(getLayoutContext());
                    pt = new Point(
                            box.getAbsX() + (int)margin.left(),
                            box.getAbsY() + (int)margin.top());
                }
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

    public URL getURL() {
        URL base = null;
        try {
            base = new URL(getSharedContext().getUac().getBaseURL());
        } catch (MalformedURLException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return base;
    }

    public Document getDocument() {
        return doc;
    }

    /**
     * Returns the title as reported by the NamespaceHandler assigned to the SharedContext in this panel. For an HTML
     * document, this will be the contents of /html/head/title.
     *
     * @return the document title, or "" if the namespace handler cannot find a title, or if there is no current document
     * in the panel.
     */
    public String getDocumentTitle() {
        return doc == null ? "" : getSharedContext().getNamespaceHandler().getDocumentTitle(doc);
    }

    protected Document loadDocument(final String uri) {
        XMLResource xmlResource = sharedContext.getUac().getXMLResource(uri);
        return xmlResource.getDocument();
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

    public SharedContext getSharedContext() {
        return sharedContext;
    }

    private boolean isAnchorInCurrentDocument(String str) {
        return str.charAt(0) == '#';
    }

    private String getAnchorId(String url) {
        return url.substring(1, url.length());
    }

    /**
     * Scroll the panel to make the specified point be on screen. Typically
     * this will scroll the screen down to the y component of the point.
     */
    public void scrollTo(Point pt) {
        JScrollPane scrollPane = getEnclosingScrollPane();
        if (scrollPane != null) {
            JScrollBar scrollBar = scrollPane.getVerticalScrollBar();
            if(scrollBar != null) {
                scrollBar.setValue(pt.y);
            }
        }
    }


    public boolean isInteractive() {
        return this.getSharedContext().isInteractive();
    }

    public void setInteractive(boolean interactive) {
        this.getSharedContext().setInteractive(interactive);
    }

    public void addMouseTrackingListener(FSMouseListener l) {
        mouseTracker.addListener(l);
    }

    public void removeMouseTrackingListener(FSMouseListener l) {
        mouseTracker.removeListener(l);
    }

    public List getMouseTrackingListeners() {
        return mouseTracker.getListeners();
    }

    protected void resetMouseTracker() {
        mouseTracker.reset();
    }

    public boolean isCenteredPagedView() {
        return centeredPagedView;
    }

    public void setCenteredPagedView(boolean centeredPagedView) {
        this.centeredPagedView = centeredPagedView;
    }
    public void submit(String url) {
        formSubmissionListener.submit(url);
    }
    public void setFormSubmissionListener(FormSubmissionListener fsl) {
        this.formSubmissionListener =fsl;
        sharedContext.setFormSubmissionListener(formSubmissionListener);
    }
}

/*
 *
 * $Log$
 * Revision 1.124  2009/05/15 16:12:54  pdoubleya
 * Field can be local
 *
 * Revision 1.123  2009/05/08 12:22:26  pdoubleya
 * Merge Vianney's SWT branch to trunk. Passes regress.verify and browser still works :).
 *
 * Revision 1.119  2008/03/30 16:35:20  pdoubleya
 * Issue #231: allow for centered page box in preview mode.
 *
 * Revision 1.118  2008/02/28 19:35:30  pdoubleya
 * On printing, not updating the total page count, required because this is not otherwise available in the rendering context we create for each page
 *
 * Revision 1.117  2007/11/21 23:59:12  peterbrant
 * Always paint default background even if there is no document (report from Richard Bair)
 *
 * Revision 1.116  2007/10/13 12:04:09  pdoubleya
 * Applied patch to fix NPE reported by email https://xhtmlrenderer.dev.java.net/servlets/ReadMsg?listName=users&msgNo=724 with patch proposed by Pete in that thread.
 *
 * Revision 1.115  2007/08/24 22:49:15  peterbrant
 * Add method to retrieve all FSMouseListener objects attached to a panel
 *
 * Revision 1.114  2007/08/19 22:22:50  peterbrant
 * Merge R8pbrant changes to HEAD
 *
 * Revision 1.112.2.5  2007/08/13 22:32:10  peterbrant
 * Rename doLayout() to doDocumentLayout() to avoid confusion with AWT's doLayout()
 *
 * Revision 1.112.2.4  2007/08/07 17:57:03  peterbrant
 * Implement page level backgrounds
 *
 * Revision 1.112.2.3  2007/08/07 17:06:31  peterbrant
 * Implement named pages / Implement page-break-before/after: left/right / Experiment with efficient selection
 *
 * Revision 1.112.2.2  2007/07/11 22:48:31  peterbrant
 * Further progress on running headers and footers
 *
 * Revision 1.112.2.1  2007/07/09 22:18:04  peterbrant
 * Begin work on running headers and footers and named pages
 *
 * Revision 1.112  2007/06/11 22:30:15  peterbrant
 * Minor cleanup to LayoutContext / Start adding infrastructure to support better table pagination
 *
 * Revision 1.111  2007/05/24 13:22:38  peterbrant
 * Optimize and clean up hover and link listeners
 *
 * Patch from Sean Bright
 *
 * Revision 1.110  2007/05/21 21:58:48  peterbrant
 * More cleanup (remove experimental threading code)
 *
 * Revision 1.109  2007/04/03 13:12:07  peterbrant
 * Add notification interface for layout and render exceptions / Minor clean up (remove obsolete body expand hack, remove unused API, method name improvements)
 *
 * Revision 1.108  2007/02/22 15:30:43  peterbrant
 * Internal links should be able to target block boxes too (plus other minor cleanup)
 *
 * Revision 1.107  2007/02/07 16:33:29  peterbrant
 * Initial commit of rewritten table support and associated refactorings
 *
 * Revision 1.106  2006/08/29 17:29:14  peterbrant
 * Make Style object a thing of the past
 *
 * Revision 1.105  2006/08/27 00:37:06  peterbrant
 * Initial commit of (initial) R7 work
 *
 * Revision 1.104  2006/08/03 14:12:36  pdoubleya
 * Interactivity flag now belongs to shared context, should not be a private field anymore.
 *
 * Revision 1.103  2006/07/26 18:12:15  pdoubleya
 * Add removeDocListener and setDocFromString.
 *
 * Revision 1.102  2006/04/07 14:48:31  peterbrant
 * Fix page border in print mode (vs. paged screen mode)
 *
 * Revision 1.101  2006/04/07 07:30:56  pdoubleya
 * Store reference to loaded XMLResource. Removed commented code. Javadoc.
 *
 * Revision 1.100  2006/02/22 02:20:19  peterbrant
 * Links and hover work again
 *
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
