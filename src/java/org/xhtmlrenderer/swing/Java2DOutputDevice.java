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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 * }}}
 */
package org.xhtmlrenderer.swing;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.RenderingHints.Key;

import javax.swing.JComponent;

import org.xhtmlrenderer.extend.FSImage;
import org.xhtmlrenderer.extend.OutputDevice;
import org.xhtmlrenderer.extend.ReplacedElement;
import org.xhtmlrenderer.render.AbstractOutputDevice;
import org.xhtmlrenderer.render.BlockBox;
import org.xhtmlrenderer.render.BorderPainter;
import org.xhtmlrenderer.render.FSFont;
import org.xhtmlrenderer.render.RenderingContext;

public class Java2DOutputDevice extends AbstractOutputDevice implements OutputDevice {
    private Graphics2D _graphics;

    public Java2DOutputDevice(Graphics2D graphics) {
        _graphics = graphics;
    }

    public Java2DOutputDevice(BufferedImage outputImage) {
        this(outputImage.createGraphics());
    }

    public void drawBorderLine(
            Rectangle bounds, int side, int lineWidth, boolean solid) {
        int x = bounds.x;
        int y = bounds.y;
        int w = bounds.width;
        int h = bounds.height;
        
        int adj = solid ? 1 : 0;
        
        if (side == BorderPainter.TOP) {
            drawLine(x, y + (int) (lineWidth / 2), x + w - adj, y + (int) (lineWidth / 2));
        } else if (side == BorderPainter.LEFT) {
            drawLine(x + (int) (lineWidth / 2), y, x + (int) (lineWidth / 2), y + h - adj);
        } else if (side == BorderPainter.RIGHT) {
            int offset = (int)(lineWidth / 2);
            if (lineWidth % 2 == 1) {
                offset += 1;
            }
            drawLine(x + w - offset, y, x + w - offset, y + h - adj);
        } else if (side == BorderPainter.BOTTOM) {
            int offset = (int)(lineWidth / 2);
            if (lineWidth % 2 == 1) {
                offset += 1;
            }
            drawLine(x, y + h - offset, x + w - adj, y + h - offset);
        }
    }

    public void paintReplacedElement(RenderingContext c, BlockBox box) {
        ReplacedElement replaced = box.getReplacedElement();
        if (replaced instanceof SwingReplacedElement) {
            Rectangle contentBounds = box.getContentAreaEdge(box.getAbsX(), box.getAbsY(), c);
            translate(contentBounds.x, contentBounds.y);
            JComponent component = ((SwingReplacedElement)box.getReplacedElement()).getJComponent();
            component.paint(_graphics);
            translate(-contentBounds.x, -contentBounds.y);
        } else if (replaced instanceof ImageReplacedElement) {
            Image image = ((ImageReplacedElement)replaced).getImage();
            
            Point location = replaced.getLocation();
            _graphics.drawImage(
                    image, (int)location.getX(), (int)location.getY(), null);
        }
    }
    
    public void setColor(Color color) {
        _graphics.setColor(color);
    }
    
    protected void drawLine(int x1, int y1, int x2, int y2) {
        _graphics.drawLine(x1, y1, x2, y2);
    }
    
    public void drawRect(int x, int y, int width, int height) {
        _graphics.drawRect(x, y, width, height);
    }
    
    public void fillRect(int x, int y, int width, int height) {
        _graphics.fillRect(x, y, width, height);
    }
    
    public void setClip(Shape s) {
        _graphics.setClip(s);
    }
    
    public Shape getClip() {
        return _graphics.getClip();
    }
    
    public void clip(Shape s) {
        _graphics.clip(s);
    }
    
    public void translate(double tx, double ty) {
        _graphics.translate(tx, ty);
    }
    
    public Graphics2D getGraphics() {
        return _graphics;
    }

    public void drawOval(int x, int y, int width, int height) {
        _graphics.drawOval(x, y, width, height);
    }

    public void fillOval(int x, int y, int width, int height) {
        _graphics.fillOval(x, y, width, height);
    }

    public Object getRenderingHint(Key key) {
        return _graphics.getRenderingHint(key);
    }

    public void setRenderingHint(Key key, Object value) {
        _graphics.setRenderingHint(key, value);
    }
    
    public void setFont(FSFont font) {
        _graphics.setFont(((AWTFSFont)font).getAWTFont());
    }

    public void setStroke(Stroke s) {
        _graphics.setStroke(s);
    }

    public Stroke getStroke() {
        return _graphics.getStroke();
    }

    public void fill(Shape s) {
        _graphics.fill(s);
    }
    
    public void drawImage(FSImage image, int x, int y) {
        _graphics.drawImage(((AWTFSImage)image).getImage(), x, y, null);
    }
}
