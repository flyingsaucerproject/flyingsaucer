/*
 * {{{ header & license
 * Copyright (c) 2006 Wisconsin Court System
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
package org.xhtmlrenderer.pdf;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.RenderingHints.Key;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xhtmlrenderer.css.style.CalculatedStyle;
import org.xhtmlrenderer.extend.FSImage;
import org.xhtmlrenderer.extend.NamespaceHandler;
import org.xhtmlrenderer.extend.OutputDevice;
import org.xhtmlrenderer.extend.ReplacedElement;
import org.xhtmlrenderer.layout.SharedContext;
import org.xhtmlrenderer.render.AbstractOutputDevice;
import org.xhtmlrenderer.render.BlockBox;
import org.xhtmlrenderer.render.BorderPainter;
import org.xhtmlrenderer.render.Box;
import org.xhtmlrenderer.render.FSFont;
import org.xhtmlrenderer.render.InlineLayoutBox;
import org.xhtmlrenderer.render.PageBox;
import org.xhtmlrenderer.render.RenderingContext;
import org.xhtmlrenderer.util.XRRuntimeException;

import com.lowagie.text.DocumentException;
import com.lowagie.text.Image;
import com.lowagie.text.pdf.PdfAction;
import com.lowagie.text.pdf.PdfAnnotation;
import com.lowagie.text.pdf.PdfBorderDictionary;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfDestination;
import com.lowagie.text.pdf.PdfImportedPage;
import com.lowagie.text.pdf.PdfName;
import com.lowagie.text.pdf.PdfOutline;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfWriter;

/**
 * This class is largely based on {@link com.lowagie.text.pdf.PdfGraphics2D}. See
 * <a href="http://sourceforge.net/projects/itext/">http://sourceforge.net/projects/itext/</a> for license information.
 */
public class ITextOutputDevice extends AbstractOutputDevice implements OutputDevice {
    private static final int FILL = 1;
    private static final int STROKE = 2;
    private static final int CLIP = 3;
    
    private static AffineTransform IDENTITY = new AffineTransform();
    
    private static final BasicStroke STROKE_ONE = new BasicStroke(1);    
    
    private PdfContentByte _currentPage;
    private float _pageHeight;
    
    private ITextFSFont _font;
    
    private AffineTransform _transform = new AffineTransform();
    
    private Color _color = Color.BLACK;
    
    private Color _fillColor;
    private Color _strokeColor;
    
    private Stroke _stroke = null;
    private Stroke _originalStroke = null;
    private Stroke _oldStroke = null;
    
    private Area _clip;
    
    private SharedContext _sharedContext;
    private float _dotsPerPoint;
    
    private PdfWriter _writer;
    
    private Map _readerCache = new HashMap();
    
    private PdfDestination _defaultDestination;
    
    private List _bookmarks = new ArrayList();
    
    private Box _root;
    
    private int _startPageNo;
    
    public ITextOutputDevice(float dotsPerPoint) {
        _dotsPerPoint = dotsPerPoint;
    }
    
    public void setWriter(PdfWriter writer) {
        _writer = writer;
    }
    
    private PdfWriter getWriter() {
        return _writer;
    }

    public void initializePage(PdfContentByte currentPage, float height) {
        _currentPage = currentPage;
        _pageHeight = height;
        
        _transform = new AffineTransform();
        _transform.scale(1.0d / _dotsPerPoint, 1.0d / _dotsPerPoint);
        
        _stroke = transformStroke(STROKE_ONE);
        _originalStroke = _stroke;
        _oldStroke = _stroke;
        
        setStrokeDiff(_stroke, null);
        
        _currentPage.saveState();
        
        if (_defaultDestination == null) {
            _defaultDestination = new PdfDestination(PdfDestination.FITH, height);
            _defaultDestination.addPage(_writer.getPageReference(1));
        }
    }
    
    public void finishPage() {
        _currentPage.restoreState();
    }
    
    public void paintReplacedElement(RenderingContext c, BlockBox box) {
        Rectangle contentBounds = box.getContentAreaEdge(box.getAbsX(), box.getAbsY(), c);
        ReplacedElement element = box.getReplacedElement();
        if (element instanceof ITextImageElement) {
            drawImage(
                    ((ITextImageElement)element).getImage(),
                    contentBounds.x, contentBounds.y);
        }
    }
    
    public void paintBackground(RenderingContext c, Box box) {
        super.paintBackground(c, box);
        
        processLink(c, box);
    }

    private void processLink(RenderingContext c, Box box) {
        Element elem = box.getElement();
        if (elem != null) {
            NamespaceHandler handler = _sharedContext.getNamespaceHandler();
            String uri = handler.getLinkUri(elem);
            if (uri != null && uri.length() > 1 && uri.charAt(0) == '#') {
                String anchor = uri.substring(1);
                Box target = _sharedContext.getBoxById(anchor);
                if (target != null) {
                    PdfDestination dest = createDestination(c, target);
                    
                    PdfAction action = new PdfAction();
                    action.put(PdfName.S, PdfName.GOTO);
                    action.put(PdfName.D, dest);

                    Rectangle bounds = box.getContentAreaEdge(box.getAbsX(), box.getAbsY(), c);
                    
                    Point2D docCorner = new Point2D.Double(bounds.x, bounds.y + bounds.height);
                    Point2D pdfCorner = new Point.Double();
                    _transform.transform(docCorner, pdfCorner);
                    pdfCorner.setLocation(pdfCorner.getX(), normalizeY((float)pdfCorner.getY()));
                    
                    com.lowagie.text.Rectangle targetArea = 
                        new com.lowagie.text.Rectangle(
                                (float)pdfCorner.getX(),
                                (float)pdfCorner.getY(),
                                (float)pdfCorner.getX() + bounds.width / _dotsPerPoint,
                                (float)pdfCorner.getY() + bounds.height / _dotsPerPoint);
                    
                    PdfAnnotation annot = PdfAnnotation.createLink(
                            _writer, targetArea, PdfAnnotation.HIGHLIGHT_INVERT, action);
                    annot.setBorderStyle(new PdfBorderDictionary(0.0f, 0));
                    
                    _writer.addAnnotation(annot);
                }
            }
        }
    }
    
    private PdfDestination createDestination(RenderingContext c, Box box) {
        PdfDestination result;
        
        PageBox page = _root.getLayer().getPage(c, getPageRefY(box));
        int distanceFromTop =
            page.getMarginBorderPadding(c, CalculatedStyle.TOP);
        distanceFromTop += box.getAbsY() + box.getMargin(c).top() - page.getTop();
        result = new PdfDestination(PdfDestination.FITH, 
                normalizeY(distanceFromTop / _dotsPerPoint));
        result.addPage(_writer.getPageReference(_startPageNo + page.getPageNo()+1));
        
        return result;
    }
    
    public void drawBorderLine(
            Rectangle bounds, int side, int lineWidth, boolean solid) {
        float x = bounds.x;
        float y = bounds.y;
        float w = bounds.width;
        float h = bounds.height;
        
        float adj = solid ? (float)lineWidth / 2 : 0;
        float adj2 = lineWidth % 2 == 1 ? 0.5f : 0f;
        
        Line2D.Float line = null;
        
        if (side == BorderPainter.TOP) {
            line = new Line2D.Float(
                    x + adj, y + lineWidth / 2 + adj2, x + w - adj, y + lineWidth / 2 + adj2);
        } else if (side == BorderPainter.LEFT) {
            line = new Line2D.Float(
                    x + lineWidth / 2 + adj2, y + adj, 
                    x + lineWidth / 2 + adj2, y + h - adj);
        } else if (side == BorderPainter.RIGHT) {
            float offset = lineWidth / 2;
            if (lineWidth % 2 == 1) {
                offset += 1;
            }
            line = new Line2D.Float(
                    x + w - offset + adj2, y + adj, 
                    x + w - offset + adj2, y + h - adj);
        } else if (side == BorderPainter.BOTTOM) {
            float offset = lineWidth / 2;
            if (lineWidth % 2 == 1) {
                offset += 1;
            }
            line = new Line2D.Float(x + adj, y + h - offset + adj2, x + w - adj, y + h - offset + adj2);
        }
        
        draw(line);
    }
    
    public void setColor(Color color) {
        _color = color;
    }
    
    private void draw(Shape s) {
        followPath(s, STROKE);
    }
    
    protected void drawLine(int x1, int y1, int x2, int y2) {
        Line2D line = new Line2D.Double((double)x1, (double)y1, (double)x2, (double)y2);
        draw(line);
    }
    
    public void drawRect(int x, int y, int width, int height) {
        draw(new Rectangle(x, y, width, height));
    }
    
    public void drawOval(int x, int y, int width, int height) {
        Ellipse2D oval = new Ellipse2D.Float((float)x, (float)y, (float)width, (float)height);
        draw(oval);
    }    
    
    public void fill(Shape s) {
        followPath(s, FILL);
    }
    
    public void fillRect(int x, int y, int width, int height) {
        fill(new Rectangle(x,y,width,height));
    }
    
    public void fillOval(int x, int y, int width, int height) {
        Ellipse2D oval = new Ellipse2D.Float((float)x, (float)y, (float)width, (float)height);
        fill(oval);
    }
    
    public void translate(double tx, double ty) {
        _transform.translate(tx, ty);
    }

    public Object getRenderingHint(Key key) {
        return null;
    }

    public void setRenderingHint(Key key, Object value) {
    }
    
    public void setFont(FSFont font) {
        _font = ((ITextFSFont)font);
    }
    
    private AffineTransform normalizeMatrix(AffineTransform current) {
        double[] mx = new double[6];
        AffineTransform result = new AffineTransform();
        result.getMatrix(mx);
        mx[3] = -1;
        mx[5] = _pageHeight;
        result = new AffineTransform(mx);
        result.concatenate(current);
        return result;
    }
    
    public void drawString(String s, float x, float y) {
        if (s.length() == 0)
            return;
        PdfContentByte cb = _currentPage;
        ensureFillColor();
        AffineTransform at = (AffineTransform)getTransform().clone();
        at.translate(x, y);
        AffineTransform inverse = normalizeMatrix(at);
        AffineTransform flipper = AffineTransform.getScaleInstance(1, -1);
        inverse.concatenate(flipper);
        inverse.scale(_dotsPerPoint, _dotsPerPoint);
        double[] mx = new double[6];
        inverse.getMatrix(mx);
        cb.beginText();
        cb.setFontAndSize(_font.getFontDescription().getFont(), _font.getSize2D() / _dotsPerPoint);
        cb.setTextMatrix((float)mx[0], (float)mx[1], (float)mx[2], (float)mx[3], (float)mx[4], (float)mx[5]);
        cb.showText(s);
        cb.endText();
    }

    private AffineTransform getTransform() {
        return _transform;
    }
    
    private void ensureFillColor() {
        if (! (_color.equals(_fillColor))) {
            _fillColor = _color;
            _currentPage.setColorFill(_fillColor);
        }
    }
    
    private void ensureStrokeColor() {
        if (! (_color.equals(_strokeColor))) {
            _strokeColor = _color;
            _currentPage.setColorStroke(_strokeColor);
        }
    }    
    
    public PdfContentByte getCurrentPage() {
        return _currentPage;
    }
    
    private void followPath(Shape s, int drawType) {
        PdfContentByte cb = _currentPage;
        if (s==null) return;
        
        if (drawType==STROKE) {
            if (!(_stroke instanceof BasicStroke)) {
                s = _stroke.createStrokedShape(s);
                followPath(s, FILL);
                return;
            }
        }
        if (drawType==STROKE) {
            setStrokeDiff(_stroke, _oldStroke);
            _oldStroke = _stroke;
            ensureStrokeColor();
        } else if (drawType==FILL) {
            ensureFillColor();
        }
            
        PathIterator points;
        if (drawType == CLIP) {
            points = s.getPathIterator(IDENTITY);
        } else {
            points = s.getPathIterator(_transform);
        }
        float[] coords = new float[6];
        int traces = 0;
        while(!points.isDone()) {
            ++traces;
            int segtype = points.currentSegment(coords);
            normalizeY(coords);
            switch(segtype) {
                case PathIterator.SEG_CLOSE:
                    cb.closePath();
                    break;
                    
                case PathIterator.SEG_CUBICTO:
                    cb.curveTo(coords[0], coords[1], coords[2], coords[3], coords[4], coords[5]);
                    break;
                    
                case PathIterator.SEG_LINETO:
                    cb.lineTo(coords[0], coords[1]);
                    break;
                    
                case PathIterator.SEG_MOVETO:
                    cb.moveTo(coords[0], coords[1]);
                    break;
                    
                case PathIterator.SEG_QUADTO:
                    cb.curveTo(coords[0], coords[1], coords[2], coords[3]);
                    break;
            }
            points.next();
        }
        
        switch (drawType) {
        case FILL:
            if (traces > 0) {
                if (points.getWindingRule() == PathIterator.WIND_EVEN_ODD)
                    cb.eoFill();
                else
                    cb.fill();
            }
            break;
        case STROKE:
            if (traces > 0)
                cb.stroke();
            break;
        default: //drawType==CLIP
            if (traces == 0)
                cb.rectangle(0, 0, 0, 0);
            if (points.getWindingRule() == PathIterator.WIND_EVEN_ODD)
                cb.eoClip();
            else
                cb.clip();
            cb.newPath();
        }
    }
    
    private float normalizeY(float y) {
        return _pageHeight - y;
    }
    
    private void normalizeY(float[] coords) {
        coords[1] = normalizeY(coords[1]);
        coords[3] = normalizeY(coords[3]);
        coords[5] = normalizeY(coords[5]);
    }
    
    private void setStrokeDiff(Stroke newStroke, Stroke oldStroke) {
        PdfContentByte cb = _currentPage;
        if (newStroke == oldStroke)
            return;
        if (!(newStroke instanceof BasicStroke))
            return;
        BasicStroke nStroke = (BasicStroke)newStroke;
        boolean oldOk = (oldStroke instanceof BasicStroke);
        BasicStroke oStroke = null;
        if (oldOk)
            oStroke = (BasicStroke)oldStroke;
        if (!oldOk || nStroke.getLineWidth() != oStroke.getLineWidth())
            cb.setLineWidth(nStroke.getLineWidth());
        if (!oldOk || nStroke.getEndCap() != oStroke.getEndCap()) {
            switch (nStroke.getEndCap()) {
            case BasicStroke.CAP_BUTT:
                cb.setLineCap(0);
                break;
            case BasicStroke.CAP_SQUARE:
                cb.setLineCap(2);
                break;
            default:
                cb.setLineCap(1);
            }
        }
        if (!oldOk || nStroke.getLineJoin() != oStroke.getLineJoin()) {
            switch (nStroke.getLineJoin()) {
            case BasicStroke.JOIN_MITER:
                cb.setLineJoin(0);
                break;
            case BasicStroke.JOIN_BEVEL:
                cb.setLineJoin(2);
                break;
            default:
                cb.setLineJoin(1);
            }
        }
        if (!oldOk || nStroke.getMiterLimit() != oStroke.getMiterLimit())
            cb.setMiterLimit(nStroke.getMiterLimit());
        boolean makeDash;
        if (oldOk) {
            if (nStroke.getDashArray() != null) {
                if (nStroke.getDashPhase() != oStroke.getDashPhase()) {
                    makeDash = true;
                }
                else if (!java.util.Arrays.equals(nStroke.getDashArray(), oStroke.getDashArray())) {
                    makeDash = true;
                }
                else
                    makeDash = false;
            }
            else if (oStroke.getDashArray() != null) {
                makeDash = true;
            }
            else
                makeDash = false;
        }
        else {
            makeDash = true;
        }
        if (makeDash) {
            float dash[] = nStroke.getDashArray();
            if (dash == null)
                cb.setLiteral("[]0 d\n");
            else {
                cb.setLiteral('[');
                int lim = dash.length;
                for (int k = 0; k < lim; ++k) {
                    cb.setLiteral(dash[k]);
                    cb.setLiteral(' ');
                }
                cb.setLiteral(']');
                cb.setLiteral(nStroke.getDashPhase());
                cb.setLiteral(" d\n");
            }
        }
    }

    public void setStroke(Stroke s) {
        _originalStroke = s;
        this._stroke = transformStroke(s);
    }
    
    private Stroke transformStroke(Stroke stroke) {
        if (!(stroke instanceof BasicStroke))
            return stroke;
        BasicStroke st = (BasicStroke)stroke;
        float scale = (float)Math.sqrt(Math.abs(_transform.getDeterminant()));
        float dash[] = st.getDashArray();
        if (dash != null) {
            for (int k = 0; k < dash.length; ++k)
                dash[k] *= scale;
        }
        return new BasicStroke(st.getLineWidth() * scale, st.getEndCap(), st.getLineJoin(), st.getMiterLimit(), dash, st.getDashPhase() * scale);
    } 
    
    public void clip(Shape s) {
        if (s != null)
            s = _transform.createTransformedShape(s);
        if (_clip == null)
            _clip = new Area(s);
        else
            _clip.intersect(new Area(s));
        followPath(s, CLIP);
    }
    
    public Shape getClip() {
        try {
            return _transform.createInverse().createTransformedShape(_clip);
        }
        catch (NoninvertibleTransformException e) {
            return null;
        }
    }
    
    public void setClip(Shape s) {
        PdfContentByte cb = _currentPage;
        cb.restoreState();
        cb.saveState();
        if (s != null)
            s = _transform.createTransformedShape(s);
        if (s == null) {
            _clip = null;
        }
        else {
            _clip = new Area(s);
            followPath(s, CLIP);
        }
        _fillColor = null;
        _strokeColor = null;
        _oldStroke = STROKE_ONE;
    }

    public Stroke getStroke() {
        return _originalStroke;
    }
    
    public void drawImage(FSImage fsImage, int x, int y) {
        if (fsImage instanceof PDFAsImage) {
            drawPDFAsImage((PDFAsImage)fsImage, x, y);
        } else {
            Image image = ((ITextFSImage)fsImage).getImage();
            
            if (fsImage.getHeight() <= 0 || fsImage.getWidth() <= 0) {
                return;
            }
            
            AffineTransform at = AffineTransform.getTranslateInstance(x,y);
            at.translate(0, fsImage.getHeight());
            at.scale(fsImage.getWidth(), fsImage.getHeight());
            
            AffineTransform inverse = normalizeMatrix(_transform);
            AffineTransform flipper = AffineTransform.getScaleInstance(1,-1);
            inverse.concatenate(at);
            inverse.concatenate(flipper);
            
            double[] mx = new double[6];
            inverse.getMatrix(mx);
            
            try {
                _currentPage.addImage(image, 
                        (float)mx[0], (float)mx[1], (float)mx[2], 
                        (float)mx[3], (float)mx[4], (float)mx[5]);
            } catch (DocumentException e) {
                throw new XRRuntimeException(e.getMessage(), e);
            }
        }    
    }
    
    private void drawPDFAsImage(PDFAsImage image, int x, int y) {
        URL url = image.getURL();
        PdfReader reader = null;
        
        try {
            reader = getReader(url);
        } catch (IOException e) {
            throw new XRRuntimeException("Could not load " + url + ": " + 
                    e.getMessage(), e);
        }
        
        PdfImportedPage page = getWriter().getImportedPage(reader, 1);
        
        AffineTransform at = AffineTransform.getTranslateInstance(x,y);
        at.translate(0, image.getHeightAsFloat());
        at.scale(image.getWidthAsFloat(), image.getHeightAsFloat());
        
        AffineTransform inverse = normalizeMatrix(_transform);
        AffineTransform flipper = AffineTransform.getScaleInstance(1,-1);
        inverse.concatenate(at);
        inverse.concatenate(flipper);
        
        double[] mx = new double[6];
        inverse.getMatrix(mx);
        
        mx[0] = image.scaleWidth();
        mx[3] = image.scaleHeight();
        
        _currentPage.addTemplate(page,
                (float)mx[0], (float)mx[1], (float)mx[2],
                (float)mx[3], (float)mx[4], (float)mx[5]);
    }
    
    public PdfReader getReader(URL url) throws IOException {
        PdfReader result = (PdfReader) _readerCache.get(url);
        if (result ==  null) {
            result = new PdfReader(url);
            _readerCache.put(url, result);
        }
        return result;
    }
    
    public float getDotsPerPoint() {
        return _dotsPerPoint;
    }
    
    public void start(Document doc) {
        loadBookmarks(doc);
    }
    
    public void finish(RenderingContext c, Box root) {
        writeOutline(c, root);
    }
    
    private void writeOutline(RenderingContext c, Box root) {
        if (_bookmarks.size() > 0) {
            _writer.setViewerPreferences(PdfWriter.PageModeUseOutlines);
            writeBookmarks(c, root, _writer.getRootOutline(), _bookmarks);
        }
    }
    
    private void writeBookmarks(RenderingContext c, 
            Box root, PdfOutline parent, List bookmarks) {
        for (Iterator i = bookmarks.iterator(); i.hasNext(); ) {
            Bookmark bookmark = (Bookmark)i.next();
            writeBookmark(c, root, parent, bookmark);
        }
    }
    
    private int getPageRefY(Box box) {
        if (box instanceof InlineLayoutBox) {
            InlineLayoutBox iB = (InlineLayoutBox)box;
            return iB.getAbsY() + iB.getBaseline();
        } else {
            return box.getAbsY();
        }
    }
    
    private void writeBookmark(RenderingContext c, Box root, PdfOutline parent, Bookmark bookmark) {
        String href = bookmark.getHRef();
        PdfDestination target = null;
        if (href.length() > 0 && href.charAt(0) == '#') {
            Box box = _sharedContext.getBoxById(href.substring(1));
            if (box != null) {
                PageBox page = root.getLayer().getPage(c, getPageRefY(box));
                int distanceFromTop =
                    page.getMarginBorderPadding(c, CalculatedStyle.TOP);
                distanceFromTop += box.getAbsY() - page.getTop();
                target = new PdfDestination(PdfDestination.FITH, 
                        normalizeY(distanceFromTop / _dotsPerPoint));
                target.addPage(_writer.getPageReference(_startPageNo + page.getPageNo()+1));
            }
        }
        if (target == null) {
            target = _defaultDestination;
        }
        PdfOutline outline = new PdfOutline(parent, target, bookmark.getName());
        writeBookmarks(c, root, outline, bookmark.getChildren());
    }
    
    private void loadBookmarks(Document doc) {
        Element head = DOMUtil.getChild(doc.getDocumentElement(), "head");
        if (head != null) {
            Element bookmarks = DOMUtil.getChild(head, "bookmarks");
            if (bookmarks != null) {
                List l = DOMUtil.getChildren(bookmarks, "bookmark");
                if (l != null) {
                    for (Iterator i = l.iterator(); i.hasNext(); ) {
                        Element e = (Element)i.next();
                        loadBookmark(null, e);
                    }
                }
            }
        }
    }
    
    private void loadBookmark(Bookmark parent, Element bookmark) {
        Bookmark us = new Bookmark(bookmark.getAttribute("name"),
                bookmark.getAttribute("href"));
        if (parent == null) {
            _bookmarks.add(us);
        } else {
            parent.addChild(us);
        }
        List l = DOMUtil.getChildren(bookmark, "bookmark");
        if (l != null) {
            for (Iterator i = l.iterator(); i.hasNext(); ) {
                Element e = (Element)i.next();
                loadBookmark(us, e);
            }
        }
    }
    
    private static class Bookmark {
        private String _name;
        private String _HRef;
        
        private List _children;
        
        public Bookmark() {
        }
        
        public Bookmark(String name, String href) {
            _name = name;
            _HRef = href;
        }

        public String getHRef() {
            return _HRef;
        }

        public void setHRef(String href) {
            _HRef = href;
        }

        public String getName() {
            return _name;
        }

        public void setName(String name) {
            _name = name;
        }
        
        public void addChild(Bookmark child) {
            if (_children == null) {
                _children = new ArrayList();
            }
            _children.add(child);
        }
        
        public List getChildren() {
            return _children == null ? Collections.EMPTY_LIST : _children;
        }
    }

    public SharedContext getSharedContext() {
        return _sharedContext;
    }

    public void setSharedContext(SharedContext sharedContext) {
        _sharedContext = sharedContext;
    }
    
    public void setRoot(Box root) {
        _root = root;
    }

    public int getStartPageNo() {
        return _startPageNo;
    }

    public void setStartPageNo(int startPageNo) {
        _startPageNo = startPageNo;
    }
}
