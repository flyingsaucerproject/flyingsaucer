/*
 * {{{ header & license
 * Copyright (c) 2007 Vianney le Clément
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 * }}}
 */
package org.xhtmlrenderer.swt;

import java.awt.Dimension;
import java.awt.Shape;
import java.awt.geom.Area;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.StringReader;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xhtmlrenderer.css.style.CalculatedStyle;
import org.xhtmlrenderer.css.style.derived.RectPropertySet;
import org.xhtmlrenderer.event.DocumentListener;
import org.xhtmlrenderer.extend.*;
import org.xhtmlrenderer.layout.BoxBuilder;
import org.xhtmlrenderer.layout.Layer;
import org.xhtmlrenderer.layout.LayoutContext;
import org.xhtmlrenderer.layout.SharedContext;
import org.xhtmlrenderer.render.BlockBox;
import org.xhtmlrenderer.render.Box;
import org.xhtmlrenderer.render.PageBox;
import org.xhtmlrenderer.render.RenderingContext;
import org.xhtmlrenderer.render.ViewportBox;
import org.xhtmlrenderer.resource.XMLResource;
import org.xhtmlrenderer.simple.NoNamespaceHandler;
import org.xhtmlrenderer.util.Configuration;
import org.xhtmlrenderer.util.Uu;
import org.xhtmlrenderer.util.XRLog;
import org.xml.sax.InputSource;

/**
 * Renders XML+CSS using SWT in a widget (a Composite). Scrollbars are handled
 * automatically.
 * 
 * @author Vianney le Clément
 */
public class BasicRenderer extends Canvas implements PaintListener, UserInterface, FSCanvas {
    private static final int PAGE_PAINTING_CLEARANCE = 10;

    private SharedContext _sharedContext;

    // TODO layout_context should not be stored!
    private LayoutContext _layout_context;

    private Image _layout_image = null; // Image and GC used in layout_context

    private GC _layout_gc = null;

    private float _fontScalingFactor = 1.2F;

    private float _minFontScale = 0.50F;

    private float _maxFontScale = 3.0F;

    private Document _doc = null;

    private BlockBox _rootBox = null;

    private Set _documentListeners = new HashSet();

    private boolean _needRelayout = false;

    private boolean _hasFixedContent = false;

    private boolean _noResize = false; // temp. deactivate resize code

    private Point _origin = new Point(0, 0);

    private Point _drawnSize = new Point(0, 0);

    private Image _offscreen = null;

    private SpecialRedraw _specialRedraw = null;

    private static int checkStyle(int style) {
        final int mask = SWT.BORDER;
        return (style & mask) | SWT.NO_REDRAW_RESIZE | SWT.NO_BACKGROUND | SWT.V_SCROLL
                | SWT.H_SCROLL | SWT.NO_RADIO_GROUP;
    }

    public BasicRenderer(Composite parent, int style) {
        this(parent, style, new NaiveUserAgent(parent.getDisplay()));
    }

    /**
     * Construct the BasicRenderer
     * 
     * @param parent
     * @param uac
     */
    public BasicRenderer(Composite parent, int style, UserAgentCallback uac) {
        super(parent, checkStyle(style));
        super.setLayout(null);
        setBackground(getDisplay().getSystemColor(SWT.COLOR_WHITE));
        setBackgroundMode(SWT.INHERIT_FORCE);

        _sharedContext = new SharedContext(uac, new SWTFontResolver(parent.getDisplay()),
                new SWTReplacedElementFactory(), new SWTTextRenderer(), getDisplay().getDPI().y);
        _sharedContext.setCanvas(this);

        getHorizontalBar().addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
                ScrollBar bar = (ScrollBar) event.widget;
                int hSelection = bar.getSelection();
                scrollTo(new Point(hSelection, _origin.y));
            }
        });
        getVerticalBar().addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
                ScrollBar bar = (ScrollBar) event.widget;
                int vSelection = bar.getSelection();
                scrollTo(new Point(_origin.x, vSelection));
            }
        });

        addPaintListener(this);
        addDisposeListener(new DisposeListener() {
            public void widgetDisposed(DisposeEvent e) {
                // dispose used fonts
                _sharedContext.flushFonts();
                // clean ReplacedElementFactory
                ReplacedElementFactory ref = _sharedContext.getReplacedElementFactory();
                if (ref instanceof SWTReplacedElementFactory) {
                    ((SWTReplacedElementFactory) ref).clean();
                }
                // dispose images when using NaiveUserAgent
                UserAgentCallback uac = _sharedContext.getUac();
                if (uac instanceof NaiveUserAgent) {
                    ((NaiveUserAgent) uac).disposeCache();
                }
                // dispose offscreen image
                if (_offscreen != null) {
                    _offscreen.dispose();
                }
                // dispose temp Image/GC
                if (_layout_image != null) {
                    _layout_gc.dispose();
                    _layout_image.dispose();
                }
            }
        });
        addListener(SWT.Resize, new Listener() {
            private Point _previousSize = null;

            public void handleEvent(Event event) {
                Point size = getScreenSize();
                if (getRootLayer() != null && !_noResize) {
                    if (!isPrint() && (_previousSize == null || size.x != _previousSize.x)) {
                        // Ask for relayout if the width has changed
                        relayout();
                    } else {
                        // Else, don't relayout, but update scrollbars
                        if (updateScrollBars()) {
                            relayout();
                        } else if (_offscreen != null) {
                            redrawSpecial(new RedrawNewSize(_previousSize));
                        }
                    }
                }
                _previousSize = size;
            }
        });
        addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                Point pt = new Point(_origin.x, _origin.y);
                switch (e.keyCode) {
                    case SWT.ARROW_UP:
                        pt.y -= getVerticalBar().getIncrement();
                        break;
                    case SWT.ARROW_DOWN:
                        pt.y += getVerticalBar().getIncrement();
                        break;
                    case SWT.ARROW_LEFT:
                        pt.x -= getHorizontalBar().getIncrement();
                        break;
                    case SWT.ARROW_RIGHT:
                        pt.x += getHorizontalBar().getIncrement();
                        break;
                    case SWT.PAGE_UP:
                        pt.y -= getVerticalBar().getPageIncrement();
                        break;
                    case SWT.PAGE_DOWN:
                        pt.y += getVerticalBar().getPageIncrement();
                        break;
                    case SWT.HOME:
                        pt.x = 0;
                        pt.y = 0;
                        break;
                    case SWT.END:
                        pt.x = 0;
                        pt.y = _drawnSize.y; // will be fixed by setOrigin
                        break;
                }
                setOrigin(pt);
            }
        });

        updateScrollBars();
    }

    public void addDocumentListener(DocumentListener listener) {
        _documentListeners.add(listener);
    }

    public void removeDocumentListener(DocumentListener listener) {
        _documentListeners.remove(listener);
    }

    protected void fireDocumentLoaded() {
        Iterator it = _documentListeners.iterator();
        while (it.hasNext()) {
            ((DocumentListener) it.next()).documentLoaded();
        }
    }

    protected void fireOnLayoutException(Throwable t) {
        Iterator it = _documentListeners.iterator();
        while (it.hasNext()) {
            ((DocumentListener) it.next()).onLayoutException(t);
        }
    }

    protected void fireOnRenderException(Throwable t) {
        Iterator it = _documentListeners.iterator();
        while (it.hasNext()) {
            ((DocumentListener) it.next()).onRenderException(t);
        }
    }

    /**
     * A Renderer has no layout!
     */
    public void setLayout(Layout layout) {
    }

    /**
     * Do a full relayout and redraw
     */
    public void relayout() {
        _needRelayout = true;
        redraw();
    }

    /**
     * Invalidate the whole view. Redraw everything.
     */
    public void invalidate() {
        if (_offscreen != null) {
            _offscreen.dispose();
            _offscreen = null;
        }
        redraw();
    }

    private void redrawSpecial(SpecialRedraw type) {
        if (_hasFixedContent && !type.isForFixedContent()) {
            invalidate();
        } else if (_specialRedraw == null) {
            _specialRedraw = type;
            _specialRedraw.redraw();
        } else if (_specialRedraw.getClass().equals(type.getClass())
                && _specialRedraw.ignoreFurther()) {
            _specialRedraw.redraw();
        } else {
            invalidate();
        }
    }

    /**
     * Redraw only rect.
     * 
     * @param rect
     *            the rectangle
     */
    public void invalidate(Rectangle rect) {
        Rectangle r = getClientArea();
        r.intersect(rect);
        redrawSpecial(new RedrawTarget(r));
    }

    /**
     * @return a new {@link LayoutContext}
     */
    protected LayoutContext newLayoutcontext() {
        LayoutContext result = _sharedContext.newLayoutContextInstance();

        if (_layout_gc == null) {
            _layout_image = new Image(getDisplay(), 1, 1);
            _layout_gc = new GC(_layout_image);
        }

        result.setFontContext(new SWTFontContext(_layout_gc));
        _sharedContext.getTextRenderer().setup(result.getFontContext());

        return result;
    }

    /**
     * @param gc
     * @return a new {@link RenderingContext}
     */
    protected RenderingContext newRenderingContext(GC gc) {
        RenderingContext result = _sharedContext.newRenderingContextInstance();

        result.setFontContext(new SWTFontContext(gc));
        result.setOutputDevice(new SWTOutputDevice(gc));

        _sharedContext.getTextRenderer().setup(result.getFontContext());

        return result;
    }

    protected java.awt.Rectangle getInitialExtents(LayoutContext c) {
        if (!c.isPrint()) {
            Point size = getScreenSize();
            if (size.x == 0 && size.y == 0) {
                size.x = 1;
                size.y = 1;
            }
            return new java.awt.Rectangle(size.x, size.y);
        } else {
            PageBox first = Layer.createPageBox(c, "first");
            return new java.awt.Rectangle(0, 0, first.getContentWidth(c), first.getContentHeight(c));
        }
    }

    /**
     * @return the size of the drawable screen
     */
    public Point getScreenSize() {
        org.eclipse.swt.graphics.Rectangle rect = getClientArea();
        return new Point(rect.width, rect.height);
    }

    public Point computeSize(int wHint, int hHint, boolean changed) {
        return _drawnSize;
    }

    public java.awt.Rectangle getFixedRectangle() {
        Point size = getScreenSize();
        return new java.awt.Rectangle(0, 0, size.x, size.y);
    }

    public int getX() {
        return -_origin.x;
    }

    public int getY() {
        return -_origin.y;
    }

    /**
     * Check (and correct) the point to be within origin bounds.
     */
    private Point checkOrigin(Point pt) {
        Point size = getScreenSize();
        Point p = new Point(pt.x, pt.y);

        if (p.x > _drawnSize.x - size.x) {
            p.x = _drawnSize.x - size.x;
        }
        if (p.x < 0) {
            p.x = 0;
        }

        if (p.y > _drawnSize.y - size.y) {
            p.y = _drawnSize.y - size.y;
        }
        if (p.y < 0) {
            p.y = 0;
        }

        return p;
    }

    /**
     * @return the origin of the view
     */
    public Point getOrigin() {
        return _origin;
    }

    /**
     * Set the origin of the view. NOTE: this won't be done immediately.
     * 
     * @param pt
     */
    public void setOrigin(Point pt) {
        Point p = checkOrigin(pt);
        if (p.equals(_origin))
            return;
        getHorizontalBar().setSelection(p.x);
        getVerticalBar().setSelection(p.y);
        scrollTo(p);
    }

    protected void scrollTo(Point pt) {
        if (_origin.equals(pt)) {
            return;
        }

        if (_offscreen != null) {
            redrawSpecial(new RedrawNewOrigin(_origin));
        }

        Control[] children = getChildren();
        for (int i = 0; i < children.length; i++) {
            Point loc = children[i].getLocation();
            loc.x += _origin.x - pt.x;
            loc.y += _origin.y - pt.y;
            children[i].setLocation(loc);
        }

        _origin = pt;
        redraw();
    }

    /**
     * Update the scrollbars
     * 
     * @return true if we need to relayout the whole thing
     */
    protected boolean updateScrollBars() {
        Point size = getScreenSize();
        ScrollBar hBar = getHorizontalBar(), vBar = getVerticalBar();
        boolean needRelayout = false;

        hBar.setMaximum(_drawnSize.x);
        hBar.setThumb(Math.min(_drawnSize.x, size.x));
        hBar.setIncrement(15); // TODO something meaningful ?
        hBar.setPageIncrement(size.x);
        boolean visible = !(_origin.x == 0 && _drawnSize.x <= size.x);
        hBar.setVisible(visible);

        size = getScreenSize();

        vBar.setMaximum(_drawnSize.y);
        vBar.setThumb(Math.min(_drawnSize.y, size.y));
        vBar.setIncrement(15); // TODO line height here
        vBar.setPageIncrement(size.y);
        visible = !(_origin.y == 0 && _drawnSize.y <= size.y);
        if (!isPrint() && vBar.isVisible() != visible) {
            needRelayout = true;
        }
        vBar.setVisible(visible);

        return needRelayout;
    }

    /**
     * Convert an SWT rectangle into an AWT rectangle.
     * 
     * @param rect
     * @return
     */
    private static java.awt.Rectangle convertRectangle(Rectangle rect) {
        return new java.awt.Rectangle(rect.x, rect.y, rect.width, rect.height);
    }

    public void paintControl(PaintEvent e) {
        if (_doc == null) {
            // just draw background
            e.gc.fillRectangle(getClientArea());
        }

        // if this is the first time painting this document, then calc layout
        Layer root = getRootLayer();
        if (root == null || _needRelayout) {
            doLayout();
            root = getRootLayer();
            if (_offscreen != null) {
                // invalidate offscreen image
                _offscreen.dispose();
                _offscreen = null;
            }
        }
        _needRelayout = false;
        if (root == null) {
            XRLog.render(Level.FINE, "skipping the actual painting");
        } else {
            Point size = getScreenSize();
            // make sure origin is within the bounds
            Point origin = checkOrigin(_origin);
            if (!origin.equals(_origin)) {
                // the origin has been corrected
                if (_offscreen != null) {
                    if (_hasFixedContent
                            || (_specialRedraw != null && !(_specialRedraw instanceof RedrawNewOrigin))) {
                        _offscreen.dispose();
                        _offscreen = null;
                    } else if (_specialRedraw == null) {
                        _specialRedraw = new RedrawNewOrigin(_origin);
                    }
                }
            }
            _origin = origin;
            // redraw offscreen if needed
            if (_offscreen == null) { // full redraw
                _offscreen = new Image(getDisplay(), size.x, size.y);
                GC gc = new GC(_offscreen);
                RenderingContext c = newRenderingContext(gc);
                c.getOutputDevice().setClip(new java.awt.Rectangle(0, 0, size.x, size.y));
                doRender(c);
                gc.dispose();
            } else if (_specialRedraw instanceof RedrawTarget) { // targetted
                Rectangle target = ((RedrawTarget) _specialRedraw)._target;
                GC gc = new GC(_offscreen);
                RenderingContext c = newRenderingContext(gc);
                c.getOutputDevice().setClip(convertRectangle(target));
                doRender(c);
                gc.dispose();
            } else if (_specialRedraw instanceof RedrawNewOrigin) { // scroll
                Point previousOrigin = ((RedrawNewOrigin) _specialRedraw)._previousOrigin;
                Image img = new Image(getDisplay(), size.x, size.y);
                GC gc = new GC(img);
                gc.drawImage(_offscreen, previousOrigin.x - _origin.x, previousOrigin.y
                                - _origin.y);
                Area a = new Area();
                if (_origin.x < previousOrigin.x) {
                    int width = Math.min(size.x, previousOrigin.x - _origin.x);
                    a.add(new Area(new java.awt.Rectangle(0, 0, width, size.y)));
                } else if (_origin.x > previousOrigin.x) {
                    int width = Math.min(size.x, _origin.x - previousOrigin.x);
                    a.add(new Area(new java.awt.Rectangle(size.x - width, 0, width, size.y)));
                }
                if (_origin.y < previousOrigin.y) {
                    int height = Math.min(size.y, previousOrigin.y - _origin.y);
                    a.add(new Area(new java.awt.Rectangle(0, 0, size.x, height)));
                } else if (_origin.y > previousOrigin.y) {
                    int height = Math.min(size.y, _origin.y - previousOrigin.y);
                    a.add(new Area(new java.awt.Rectangle(0, size.y - height, size.x, height)));
                }
                RenderingContext c = newRenderingContext(gc);
                c.getOutputDevice().setClip(a);
                doRender(c);
                gc.dispose();
                _offscreen.dispose();
                _offscreen = img;
            } else if (_specialRedraw instanceof RedrawNewSize) { // adjust
                // size
                Point previousSize = ((RedrawNewSize) _specialRedraw)._previousSize;
                Image img = new Image(getDisplay(), size.x, size.y);
                GC gc = new GC(img);
                gc.drawImage(_offscreen, 0, 0);
                if (size.x > previousSize.x || size.y > previousSize.y) {
                    Area a = new Area();
                    if (size.x > previousSize.x) {
                        a.add(new Area(new java.awt.Rectangle(previousSize.x, 0, size.x
                                - previousSize.x, size.y)));
                    }
                    if (size.y > previousSize.y) {
                        a.add(new Area(new java.awt.Rectangle(0, previousSize.y, size.x, size.y
                                - previousSize.y)));
                    }
                    RenderingContext c = newRenderingContext(gc);
                    c.getOutputDevice().setClip(a);
                    doRender(c);
                    gc.setClipping((Rectangle) null);
                }
                gc.dispose();
                _offscreen.dispose();
                _offscreen = img;
            }
            // draw on screen
            e.gc.drawImage(_offscreen, 0, 0);
        }
        _specialRedraw = null;
    }

    protected void doLayout() {
        if (_doc == null) {
            return;
        }

        _layout_context = newLayoutcontext();

        try {
            long start = System.currentTimeMillis();

            if (_rootBox != null && _needRelayout) {
                _rootBox.reset(_layout_context);
            } else {
                _rootBox = BoxBuilder.createRootBox(_layout_context, _doc);
            }

            _rootBox.setContainingBlock(new ViewportBox(getInitialExtents(_layout_context)));
            _rootBox.layout(_layout_context);

            long end = System.currentTimeMillis();
            XRLog.layout(Level.INFO, "Layout took " + (end - start) + "ms");
        } catch (Throwable e) {
            XRLog.exception(e.getMessage(), e);
            e.printStackTrace();
        }

        Layer rootLayer = _rootBox.getLayer();
        _hasFixedContent = rootLayer.containsFixedContent();

        XRLog.layout(Level.FINEST, "after layout: " + _rootBox);

        // update scrollbars
        Dimension intrinsic_size = rootLayer.getPaintingDimension(_layout_context);
        if (_layout_context.isPrint()) {
            rootLayer.trimEmptyPages(_layout_context, intrinsic_size.height);
            if (rootLayer.getLastPage() != null) {
                rootLayer.assignPagePaintingPositions(_layout_context, Layer.PAGED_MODE_SCREEN,
                        PAGE_PAINTING_CLEARANCE);
                _drawnSize = new Point(rootLayer.getMaxPageWidth(_layout_context,
                        PAGE_PAINTING_CLEARANCE), rootLayer.getLastPage().getPaintingBottom()
                        + PAGE_PAINTING_CLEARANCE);
            } else {
                _drawnSize = new Point(0, 0);
            }
        } else {
            _drawnSize = new Point(intrinsic_size.width, intrinsic_size.height);
        }

        _noResize = true;
        if (updateScrollBars()) {
            doLayout();
        }
        _noResize = false;

        // TODO call only once? in display.asyncExec?
        fireDocumentLoaded();
    }

    protected void doRender(RenderingContext c) {
        try {
            c.getOutputDevice().translate(-_origin.x, -_origin.y);
            long start = System.currentTimeMillis();
            if (c.isPrint()) {
                paintPagedView(c, _rootBox.getLayer());
            } else {
                _rootBox.getLayer().paint(c);
            }
            long after = System.currentTimeMillis();
            if (Configuration.isTrue("xr.incremental.repaint.print-timing", false)) {
                Uu.p("repaint took ms: " + (after - start));
            }
        } catch (Throwable e) {
            XRLog.exception(e.getMessage(), e);
        }
        ((SWTOutputDevice) c.getOutputDevice()).clean();
    }

    private void paintPagedView(RenderingContext c, Layer root) {
        if (root.getLastPage() == null) {
            return;
        }

        SWTOutputDevice out = (SWTOutputDevice) c.getOutputDevice();
        GC gc = out.getGC();
        Shape working = out.getClip();

        List pages = root.getPages();
        c.setPageCount(pages.size());
        for (int i = 0; i < pages.size(); i++) {
            PageBox page = (PageBox) pages.get(i);
            c.setPage(i, page);

            java.awt.Rectangle overall = page.getScreenPaintingBounds(c, PAGE_PAINTING_CLEARANCE);
            overall.x -= 1;
            overall.y -= 1;
            overall.width += 1;
            overall.height += 1;

            java.awt.Rectangle bounds = new java.awt.Rectangle(overall);
            bounds.width += 1;
            bounds.height += 1;
            if (working.intersects(bounds)) {
                page.paintBackground(c, PAGE_PAINTING_CLEARANCE, Layer.PAGED_MODE_SCREEN);
                page.paintMarginAreas(c, PAGE_PAINTING_CLEARANCE, Layer.PAGED_MODE_SCREEN);
                page.paintBorder(c, PAGE_PAINTING_CLEARANCE, Layer.PAGED_MODE_SCREEN);

                Color old = gc.getForeground();
                gc.setForeground(gc.getDevice().getSystemColor(SWT.COLOR_BLACK));
                gc.drawRectangle(overall.x, overall.y, overall.width, overall.height);
                gc.setForeground(old);

                java.awt.Rectangle content = page.getPagedViewClippingBounds(c,
                        PAGE_PAINTING_CLEARANCE);
                out.clip(content);

                int left = PAGE_PAINTING_CLEARANCE
                        + page.getMarginBorderPadding(c, CalculatedStyle.LEFT);
                int top = page.getPaintingTop()
                        + page.getMarginBorderPadding(c, CalculatedStyle.TOP) - page.getTop();

                out.translate(left, top);
                root.paint(c);
                out.translate(-left, -top);

                out.setClip(working);
            }
        }

        out.setClip(working);
    }

    public Document getDocument() {
        return _doc;
    }

    /**
     * Reload the current document.
     */
    public void reload() {
        if (_doc == null) {
            return;
        }
        _rootBox = null;
        _active_element = null;
        _hovered_element = null;
        _focus_element = null;
        if (Configuration.isTrue("xr.cache.stylesheets", true)) {
            _sharedContext.getCss().flushStyleSheets();
        } else {
            _sharedContext.getCss().flushAllStyleSheets();
        }

        setCursor(null);
        _sharedContext.reset();
        if (_offscreen != null) {
            _offscreen.dispose();
            _offscreen = null;
        }
        _origin = new Point(0, 0);
        getHorizontalBar().setSelection(0);
        getVerticalBar().setSelection(0);
        redraw();
    }

    public void setDocument(Document doc, String url, NamespaceHandler nsh) {
        _rootBox = null;
        _doc = doc;

        _active_element = null;
        _hovered_element = null;
        _focus_element = null;

        // have to do this first
        if (Configuration.isTrue("xr.cache.stylesheets", true)) {
            _sharedContext.getCss().flushStyleSheets();
        } else {
            _sharedContext.getCss().flushAllStyleSheets();
        }

        setCursor(null);
        _sharedContext.reset();
        if (_offscreen != null) {
            _offscreen.dispose();
            _offscreen = null;
        }
        _origin = new Point(0, 0);
        getHorizontalBar().setSelection(0);
        getVerticalBar().setSelection(0);

        if (doc == null) {
            _drawnSize = new Point(1, 1);
            updateScrollBars();
        } else {
            _sharedContext.setBaseURL(url);
            _sharedContext.setNamespaceHandler(nsh);
            _sharedContext.getCss().setDocumentContext(_sharedContext,
                    _sharedContext.getNamespaceHandler(), doc, this);
        }

        redraw();
    }

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

    public void setDocument(InputStream stream, String url) {
        setDocument(stream, url, new NoNamespaceHandler());
    }

    private boolean isAnchorInCurrentDocument(String str) {
        return str.charAt(0) == '#';
    }

    private String getAnchorId(String url) {
        return url.substring(1, url.length());
    }

    /**
     * Sets the new current document, where the new document is located
     * relative, e.g using a relative URL.
     * 
     * @param filename
     *            The new document to load
     */
    protected void setDocumentRelative(String filename) {
        String url = _sharedContext.getUac().resolveURI(filename);
        if (isAnchorInCurrentDocument(filename)) {
            String id = getAnchorId(filename);
            Box box = _sharedContext.getBoxById(id);
            if (box != null) {
                Point pt;
                if (box.getStyle().isInline()) {
                    pt = new Point(0 /* box.getAbsX() */, box.getAbsY());
                } else {
                    RectPropertySet margin = box.getMargin(_layout_context);
                    pt = new Point(0 /* box.getAbsX() + (int) margin.left() */, box.getAbsY()
                            + (int) margin.top());
                }
                setOrigin(pt);
                return;
            }
        }
        Document dom = loadDocument(url);
        setDocument(dom, url);
    }

    protected Document loadDocument(final String uri) {
        XMLResource xmlResource = _sharedContext.getUac().getXMLResource(uri);
        if (xmlResource == null) {
            return null;
        }
        return xmlResource.getDocument();
    }

    public String getDocumentTitle() {
        if (_doc == null) {
            return null;
        }
        NamespaceHandler nsh = getSharedContext().getNamespaceHandler();
        if (nsh == null) {
            return null;
        }
        return nsh.getDocumentTitle(_doc);
    }

    public Box getRootBox() {
        return _rootBox;
    }

    public Layer getRootLayer() {
        return getRootBox() == null ? null : getRootBox().getLayer();
    }

    public SharedContext getSharedContext() {
        return _sharedContext;
    }

    public LayoutContext getLayoutContext() {
        return _layout_context;
    }

    public Box find(int x, int y) {
        Layer l = getRootLayer();
        if (l != null) {
            return l.find(_layout_context, x + _origin.x, y + _origin.y, false);
        }
        return null;
    }

    private Element _hovered_element = null;

    private Element _active_element = null;

    private Element _focus_element = null;

    public boolean isHover(org.w3c.dom.Element e) {
        if (e == _hovered_element) {
            return true;
        }
        return false;
    }

    public Element getHovered_element() {
        return _hovered_element;
    }

    public void setHovered_element(Element hovered_element) {
        _hovered_element = hovered_element;
    }

    public boolean isActive(org.w3c.dom.Element e) {
        if (e == _active_element) {
            return true;
        }
        return false;
    }

    public Element getActive_element() {
        return _active_element;
    }

    public void setActive_element(Element active_element) {
        _active_element = active_element;
    }

    public boolean isFocus(org.w3c.dom.Element e) {
        if (e == _focus_element) {
            return true;
        }
        return false;
    }

    public Element getFocus_element() {
        return _focus_element;
    }

    public void setFocus_element(Element focus_element) {
        _focus_element = focus_element;
    }

    public boolean isPrint() {
        return _sharedContext.isPrint();
    }

    public void setPrint(boolean print) {
        _sharedContext.setPrint(print);
        _sharedContext.setInteractive(!print);
        _sharedContext.getReplacedElementFactory().reset();
        reload();
    }

    /**
     * Sets the scaling factor used by {@link #incrementFontSize()} and
     * {@link #decrementFontSize()}--both scale the font up or down by this
     * scaling factor. The scaling roughly modifies the font size as a
     * multiplier or divisor. A scaling factor of 1.2 applied against a font
     * size of 10pt results in a scaled font of 12pt. The default scaling factor
     * is 1.2F.
     */
    public void setFontScalingFactor(float scaling) {
        _fontScalingFactor = scaling;
    }

    /**
     * Increments all rendered fonts on the current document by the current
     * scaling factor for the panel. Scaling applies culmulatively, which means
     * that multiple calls to this method scale fonts larger and larger by
     * applying the current scaling factor against itself. You can modify the
     * scaling factor by {@link #setFontScalingFactor(float)}, and reset to the
     * document's specified font size with {@link #resetFontSize()}.
     */
    public void incrementFontSize() {
        scaleFont(_fontScalingFactor);
    }

    /**
     * Resets all rendered fonts on the current document to the font size
     * specified in the document's styling instructions.
     */
    public void resetFontSize() {
        getSharedContext().getTextRenderer().setFontScale(1f);
        reload();
    }

    /**
     * Decrements all rendered fonts on the current document by the current
     * scaling factor for the panel. Scaling applies culmulatively, which means
     * that multiple calls to this method scale fonts smaller and smaller by
     * applying the current scaling factor against itself. You can modify the
     * scaling factor by {@link #setFontScalingFactor(float)}, and reset to the
     * document's specified font size with {@link #resetFontSize()}.
     */
    public void decrementFontSize() {
        scaleFont(1 / _fontScalingFactor);
    }

    /**
     * Applies a change in scale for fonts using the rendering context's text
     * renderer.
     */
    private void scaleFont(float scaleBy) {
        TextRenderer tr = getSharedContext().getTextRenderer();
        float fs = tr.getFontScale() * scaleBy;
        if (fs < _minFontScale || fs > _maxFontScale) {
            return;
        }
        tr.setFontScale(fs);
        reload();
    }

    /**
     * Returns the maximum font scaling that may be applied, e.g. 3 times
     * assigned font size.
     */
    public float getMaxFontScale() {
        return _maxFontScale;
    }

    /**
     * Returns the minimum font scaling that may be applied, e.g. 0.5 times
     * assigned font size.
     */
    public float getMinFontScale() {
        return _minFontScale;
    }

    /**
     * Sets the maximum font scaling that may be applied, e.g. 3 times assigned
     * font size. Calling incrementFontSize() after this scale has been reached
     * doesn't have an effect.
     */
    public void setMaxFontScale(float f) {
        _maxFontScale = f;
    }

    /**
     * Sets the minimum font scaling that may be applied, e.g. 3 times assigned
     * font size. Calling decrementFontSize() after this scale has been reached
     * doesn't have an effect.
     */
    public void setMinFontScale(float f) {
        _minFontScale = f;
    }

    /**
     * Information about a special way of redrawing.
     */
    private abstract class SpecialRedraw {
        /**
         * @return <code>true</code> if this special redraw method can also be
         *         applied when there is fixed content
         */
        abstract boolean isForFixedContent();

        /**
         * @return <code>true</code> if special redraws of the same kind (but
         *         with other parameters) should be ignored
         */
        abstract boolean ignoreFurther();

        /**
         * Trigger redraw
         */
        void redraw() {
            BasicRenderer.this.redraw();
        }
    }

    private class RedrawNewSize extends SpecialRedraw {
        final Point _previousSize;

        RedrawNewSize(Point previousSize) {
            _previousSize = previousSize;
        }

        boolean isForFixedContent() {
            return false;
        }

        boolean ignoreFurther() {
            return true;
        }
    }

    private class RedrawNewOrigin extends SpecialRedraw {
        final Point _previousOrigin;

        RedrawNewOrigin(Point previousOrigin) {
            _previousOrigin = previousOrigin;
        }

        boolean isForFixedContent() {
            return false;
        }

        boolean ignoreFurther() {
            return true;
        }
    }

    private class RedrawTarget extends SpecialRedraw {
        final Rectangle _target;

        RedrawTarget(Rectangle target) {
            _target = target;
        }

        boolean isForFixedContent() {
            return true;
        }

        boolean ignoreFurther() {
            return false;
        }

        void redraw() {
            BasicRenderer.this.redraw(_target.x, _target.y, _target.width, _target.height, true);
        }
    }

}
