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

import java.awt.*;
import java.awt.RenderingHints.Key;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.PathIterator;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.Color;
import org.xhtmlrenderer.extend.FSImage;
import org.xhtmlrenderer.extend.OutputDevice;
import org.xhtmlrenderer.extend.ReplacedElement;
import org.xhtmlrenderer.render.*;
import org.xhtmlrenderer.simple.xhtml.swt.SWTFormControl;
import org.xhtmlrenderer.css.parser.FSColor;
import org.xhtmlrenderer.css.parser.FSRGBColor;

/**
 * Implementation of {@link OutputDevice} for SWT.
 * 
 * @author Vianney le Clément
 * 
 */
public class SWTOutputDevice extends AbstractOutputDevice {

    private final GC _gc;
    private Path _clippingPath = null;
    private Area _clippingArea = null;
    private Color _color = null;
    private java.awt.Color _awt_color = null;
    private Transform _transform = null;
    private Stroke _stroke = null;

    public SWTOutputDevice(GC gc) {
        _gc = gc;
    }

    /**
     * @return the Graphical Context associated with this OutputDevice
     */
    public GC getGC() {
        return _gc;
    }

    /**
     * Clean used resources.
     */
    public void clean() {
        if (_clippingPath != null) {
            _gc.setClipping((Rectangle) null);
            _clippingPath.dispose();
            _clippingPath = null;
            _clippingArea = null;
        }
        if (_color != null) {
            _color.dispose();
            _color = null;
        }
        if (_transform != null) {
            _gc.setTransform(null);
            _transform.dispose();
        }
    }

    public void clip(Shape s) {
        if (s == null) {
            return;
        }
        if (_clippingArea == null) {
            setClip(s);
        } else {
            Area a = new Area(_clippingArea);
            a.intersect(new Area(s));
            setClip(a);
        }
    }

    public void setClip(Shape s) {
        Path path = convertToPath(s);
        if (path == null) {
            _gc.setClipping((Rectangle) null);
        } else {
            _gc.setClipping(path);
        }
        if (_clippingPath != null) {
            _clippingPath.dispose();
        }
        _clippingPath = path;
        _clippingArea = (s == null ? null : new Area(s));
    }

    public Shape getClip() {
        return _clippingArea;
    }

    protected void drawLine(int x1, int y1, int x2, int y2) {
        _gc.drawLine(x1, y1, x2, y2);
    }

    public void drawBorderLine(Shape bounds, int side,
            int lineWidth, boolean solid) {
        /*int x = bounds.x;
        int y = bounds.y;
        int w = bounds.width;
        int h = bounds.height;

        int adj = solid ? 1 : 0;

        if (side == BorderPainter.TOP) {
            drawLine(x, y + (lineWidth / 2), x + w - adj, y + (lineWidth / 2));
        } else if (side == BorderPainter.LEFT) {
            drawLine(x + (lineWidth / 2), y, x + (lineWidth / 2), y + h - adj);
        } else if (side == BorderPainter.RIGHT) {
            int offset = (lineWidth / 2);
            if (lineWidth % 2 != 0) {
                offset += 1;
            }
            drawLine(x + w - offset, y, x + w - offset, y + h - adj);
        } else if (side == BorderPainter.BOTTOM) {
            int offset = (lineWidth / 2);
            if (lineWidth % 2 != 0) {
                offset += 1;
            }
            drawLine(x, y + h - offset, x + w - adj, y + h - offset);
        }*/
    	draw(bounds);
    }

    public void drawImage(FSImage image, int x, int y) {
        Image img = ((SWTFSImage) image).getImage();
        if (img == null) {
            int width = image.getWidth();
            int height = image.getHeight();
            Color oldBG = _gc.getBackground();
            Color oldFG = _gc.getForeground();
            _gc.setBackground(_gc.getDevice().getSystemColor(SWT.COLOR_WHITE));
            _gc.setForeground(_gc.getDevice().getSystemColor(SWT.COLOR_BLACK));
            _gc.fillRectangle(x, y, width, height);
            _gc.drawRectangle(x, y, width, height);
            _gc.drawLine(x, y, x + width - 1, y + height - 1);
            _gc.drawLine(x, y + height - 1, x + width - 1, y);
            _gc.setBackground(oldBG);
            _gc.setForeground(oldFG);
        } else {
            Rectangle bounds = img.getBounds();
            _gc.drawImage(img, 0, 0, bounds.width, bounds.height, x, y, image
                .getWidth(), image.getHeight());
        }
    }

    public void drawOval(int x, int y, int width, int height) {
        _gc.drawOval(x, y, width, height);
    }

    public void drawRect(int x, int y, int width, int height) {
        _gc.drawRectangle(x, y, width, height);
    }



	public void draw(Shape s) {
        Path p = convertToPath(s);
        _gc.drawPath(p);
        p.dispose();
	}
	
    public void fill(Shape s) {
        Path p = convertToPath(s);
        _gc.fillPath(p);
        p.dispose();
    }

    public void fillOval(int x, int y, int width, int height) {
        _gc.fillOval(x, y, width, height);
    }

    public void fillRect(int x, int y, int width, int height) {
        _gc.fillRectangle(x, y, width, height);
    }

    public void paintReplacedElement(RenderingContext c, BlockBox box) {
        ReplacedElement replaced = box.getReplacedElement();
        java.awt.Point location = replaced.getLocation();
        if (replaced instanceof ImageReplacedElement) {
            drawImage(((ImageReplacedElement) replaced).getImage(), location.x,
                location.y);
        } else if (replaced instanceof FormControlReplacementElement) {
            SWTFormControl swtControl = ((FormControlReplacementElement) replaced)
                .getControl();
            swtControl.getSWTControl().setVisible(true);
        }
    }

    public void setColor(java.awt.Color color) {
        if (color.equals(_awt_color)) {
            return;
        }

        Color col = new Color(_gc.getDevice(), color.getRed(),
            color.getGreen(), color.getBlue());
        _gc.setForeground(col);
        _gc.setBackground(col);
        _gc.setAlpha(color.getAlpha());
        if (_color != null) {
            _color.dispose();
        }
        _color = col;
        _awt_color = color;
    }

    public void setFont(FSFont font) {
        _gc.setFont(((SWTFSFont) font).getSWTFont());
    }

    public void setColor(FSColor color) {
        if (color instanceof FSRGBColor) {
            FSRGBColor rgb = (FSRGBColor)color;
            setColor(new java.awt.Color(rgb.getRed(), rgb.getGreen(), rgb.getBlue()));
        } else {
            throw new RuntimeException("internal error: unsupported color class " + color.getClass().getName());
        }

    }

    public Stroke getStroke() {
        return _stroke;
    }

    public void setStroke(Stroke s) {
        _stroke = s;

        /*
         * Code borrowed from SwingWT
         */
        if (s == null) {
            _gc.setLineWidth(1);
            _gc.setLineCap(SWT.CAP_SQUARE);
            _gc.setLineJoin(SWT.JOIN_MITER);
            _gc.setLineDash(null);
            return;
        }

        if (!(s instanceof BasicStroke)) {
            return;
        }

        BasicStroke bs = (BasicStroke) s;

        // Setup the line width
        _gc.setLineWidth((int) bs.getLineWidth());

        // Setup the line cap
        int gcCap = SWT.CAP_SQUARE;
        switch (bs.getEndCap()) {
        case BasicStroke.CAP_BUTT:
            gcCap = SWT.CAP_FLAT;
            break;
        case BasicStroke.CAP_ROUND:
            gcCap = SWT.CAP_ROUND;
            break;
        case BasicStroke.CAP_SQUARE:
            gcCap = SWT.CAP_SQUARE;
            break;
        }
        _gc.setLineCap(gcCap);

        // Setup the line Join
        int gcJoin = SWT.JOIN_MITER;
        switch (bs.getLineJoin()) {
        case BasicStroke.JOIN_BEVEL:
            gcJoin = SWT.JOIN_BEVEL;
            break;
        case BasicStroke.JOIN_MITER:
            gcJoin = SWT.JOIN_MITER;
            break;
        case BasicStroke.JOIN_ROUND:
            gcJoin = SWT.JOIN_ROUND;
        }
        _gc.setLineJoin(gcJoin);

        float d[] = bs.getDashArray();
        int[] dashes = null;
        if (d != null) {
            dashes = new int[d.length];
            for (int i = 0; i < d.length; i++) {
                dashes[i] = (int) d[i];
            }
        }
        _gc.setLineDash(dashes);
    }

    public void translate(double tx, double ty) {
        if (_transform == null) {
            _transform = new Transform(_gc.getDevice());
        }
        _transform.translate((int) tx, (int) ty);
        _gc.setTransform(_transform);
        if (_clippingArea != null) {
            AffineTransform t = new AffineTransform();
            t.translate(-tx, -ty);
            _clippingArea.transform(t);
        }
    }

    public Object getRenderingHint(Key key) {
        if (RenderingHints.KEY_ANTIALIASING.equals(key)) {
            switch (_gc.getAntialias()) {
            case SWT.DEFAULT:
                return RenderingHints.VALUE_ANTIALIAS_DEFAULT;
            case SWT.OFF:
                return RenderingHints.VALUE_ANTIALIAS_OFF;
            case SWT.ON:
                return RenderingHints.VALUE_ANTIALIAS_ON;
            }
        }
        return null;
    }

    public void setRenderingHint(Key key, Object value) {
        if (RenderingHints.KEY_ANTIALIASING.equals(key)) {
            int antialias = SWT.DEFAULT;
            if (RenderingHints.VALUE_ANTIALIAS_OFF.equals(value)) {
                antialias = SWT.OFF;
            } else if (RenderingHints.VALUE_ANTIALIAS_ON.equals(value)) {
                antialias = SWT.ON;
            }
            _gc.setAntialias(antialias);
        }
    }

    /**
     * Convert an AWT Shape to an SWT Path.
     * 
     * @param shape
     * @return the SWT Path or <code>null</code> if <code>shape == null</code>
     */
    private Path convertToPath(Shape shape) {
        if (shape == null) {
            return null;
        }
        Path path = new Path(_gc.getDevice());
        PathIterator iter = shape.getPathIterator(null);
        float[] coords = new float[6];
        while (!iter.isDone()) {
            int op = iter.currentSegment(coords);
            switch (op) {
            case PathIterator.SEG_MOVETO:
                path.moveTo(coords[0], coords[1]);
                break;
            case PathIterator.SEG_LINETO:
                path.lineTo(coords[0], coords[1]);
                break;
            case PathIterator.SEG_QUADTO:
                path.quadTo(coords[0], coords[1], coords[2], coords[3]);
                break;
            case PathIterator.SEG_CUBICTO:
                path.cubicTo(coords[0], coords[1], coords[2], coords[3],
                    coords[4], coords[5]);
                break;
            case PathIterator.SEG_CLOSE:
                path.close();
                break;
            }
            iter.next();
        }
        return path;
    }

    public void drawSelection(RenderingContext c, InlineText inlineText) {
        // TODO support selection drawing
    }

    public boolean isSupportsSelection() {
        // TODO support selection drawing
        return false;
    }

    public boolean isSupportsCMYKColors() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

}
