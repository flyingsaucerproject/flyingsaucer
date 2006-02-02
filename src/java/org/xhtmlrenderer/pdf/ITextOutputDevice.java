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

import org.xhtmlrenderer.extend.FSImage;
import org.xhtmlrenderer.extend.OutputDevice;
import org.xhtmlrenderer.extend.ReplacedElement;
import org.xhtmlrenderer.render.AbstractOutputDevice;
import org.xhtmlrenderer.render.BlockBox;
import org.xhtmlrenderer.render.BorderPainter;
import org.xhtmlrenderer.render.FSFont;
import org.xhtmlrenderer.render.RenderingContext;
import org.xhtmlrenderer.util.XRRuntimeException;

import com.lowagie.text.DocumentException;
import com.lowagie.text.Image;
import com.lowagie.text.pdf.PdfContentByte;

/**
 * This class is largely based on {@link com.lowagie.text.pdf.PdfGraphics2D}. See
 * {@link http://sourceforge.net/projects/itext/} for license information.
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
    
    private float _pixelsPerPoint;
    
    public ITextOutputDevice(float pixelsPerPoint) {
        _pixelsPerPoint = pixelsPerPoint;
    }

    public void initializePage(PdfContentByte currentPage, float height) {
        _currentPage = currentPage;
        _pageHeight = height;
        
        _transform = new AffineTransform();
        _transform.scale(1.0d / _pixelsPerPoint, 1.0d / _pixelsPerPoint);
        
        _stroke = transformStroke(STROKE_ONE);
        _originalStroke = _stroke;
        _oldStroke = _stroke;
        
        setStrokeDiff(_stroke, null);
        
        _currentPage.saveState();
    }
    
    public void finishPage() {
        _currentPage.restoreState();
    }
    
    public void paintReplacedElement(RenderingContext c, BlockBox box) {
        ReplacedElement element = box.getReplacedElement();
        if (element instanceof ITextImageElement) {
            drawImage(
                    ((ITextImageElement)element).getImage(),
                    box.getAbsX(), box.getAbsY());
        }
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
        inverse.scale(_pixelsPerPoint, _pixelsPerPoint);
        double[] mx = new double[6];
        inverse.getMatrix(mx);
        cb.beginText();
        cb.setFontAndSize(_font.getFontDescription().getFont(), _font.getSize2D() / _pixelsPerPoint);
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
        }
        else if (drawType==FILL)
            ensureFillColor();
        PathIterator points;
        if (drawType == CLIP)
            points = s.getPathIterator(IDENTITY);
        else
            points = s.getPathIterator(_transform);
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
        Image image = ((ITextFSImage)fsImage).getImage();
        
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
