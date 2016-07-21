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

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.RenderingHints.Key;
import java.awt.font.GlyphVector;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;

import javax.swing.*;

import org.xhtmlrenderer.css.parser.FSColor;
import org.xhtmlrenderer.css.parser.FSRGBColor;
import org.xhtmlrenderer.extend.FSGlyphVector;
import org.xhtmlrenderer.extend.FSImage;
import org.xhtmlrenderer.extend.OutputDevice;
import org.xhtmlrenderer.extend.ReplacedElement;
import org.xhtmlrenderer.render.AbstractOutputDevice;
import org.xhtmlrenderer.render.BlockBox;
import org.xhtmlrenderer.render.BorderPainter;
import org.xhtmlrenderer.render.FSFont;
import org.xhtmlrenderer.render.InlineLayoutBox;
import org.xhtmlrenderer.render.InlineText;
import org.xhtmlrenderer.render.JustificationInfo;
import org.xhtmlrenderer.render.RenderingContext;

public class Java2DOutputDevice extends AbstractOutputDevice implements OutputDevice {
    private Graphics2D _graphics;

    public Java2DOutputDevice(Graphics2D graphics) {
        _graphics = graphics;
    }

    public Java2DOutputDevice(BufferedImage outputImage) {
        this(outputImage.createGraphics());
    }
    
    
    public void drawSelection(RenderingContext c, InlineText inlineText) {
        if (inlineText.isSelected()) {
            InlineLayoutBox iB = inlineText.getParent();
            String text = inlineText.getSubstring();
            if (text != null && text.length() > 0) {
                FSFont font = iB.getStyle().getFSFont(c);
                FSGlyphVector glyphVector = c.getTextRenderer().getGlyphVector(
                        c.getOutputDevice(),
                        font,
                        inlineText.getSubstring());
                
                Rectangle start = c.getTextRenderer().getGlyphBounds(
                        c.getOutputDevice(),
                        font,
                        glyphVector,
                        inlineText.getSelectionStart(),
                        iB.getAbsX() + inlineText.getX(),
                        iB.getAbsY() + iB.getBaseline());
                
                Rectangle end = c.getTextRenderer().getGlyphBounds(
                        c.getOutputDevice(),
                        font,
                        glyphVector,
                        inlineText.getSelectionEnd() - 1,
                        iB.getAbsX() + inlineText.getX(),
                        iB.getAbsY() + iB.getBaseline());
                Graphics2D graphics = getGraphics();
                double scaleX = graphics.getTransform().getScaleX();
                boolean allSelected = (text.length() == inlineText.getSelectionEnd()-inlineText.getSelectionStart());
                int startX = (inlineText.getSelectionStart() == inlineText.getStart())?iB.getAbsX() + inlineText.getX():(int)Math.round(start.x/scaleX);
                int endX = (allSelected)?startX+inlineText.getWidth():(int)Math.round((end.x + end.width)/scaleX);
                _graphics.setColor(UIManager.getColor("TextArea.selectionBackground"));  // FIXME
                fillRect(
                        startX,
                        iB.getAbsY(),
                        endX - startX,
                        iB.getHeight());
                
                _graphics.setColor(Color.WHITE); // FIXME
                setFont(iB.getStyle().getFSFont(c));                
                
                drawSelectedText(c, inlineText, iB, glyphVector);
            }
        }
    }

    private void drawSelectedText(RenderingContext c, InlineText inlineText, InlineLayoutBox iB, FSGlyphVector glyphVector) {
        GlyphVector vector = ((AWTFSGlyphVector)glyphVector).getGlyphVector();
        
        // We'd like to draw only the characters that are actually selected, but 
        // unfortunately vector.getGlyphPixelBounds() doesn't give us accurate
        // results with the result that text can appear to jump around as it's
        // selected.  To work around this, we draw the whole string, but move
        // non-selected characters offscreen.
        for (int i = 0; i < inlineText.getSelectionStart(); i++) {
            vector.setGlyphPosition(i, new Point2D.Float(-100000, -100000));
        }
        for (int i = inlineText.getSelectionEnd(); i < inlineText.getSubstring().length(); i++) {
            vector.setGlyphPosition(i, new Point2D.Float(-100000, -100000));
        }
        if(inlineText.getParent().getStyle().isTextJustify()) {
            JustificationInfo info = inlineText.getParent().getLineBox().getJustificationInfo();
            if(info!=null) {
                String string = inlineText.getSubstring();
                float adjust = 0.0f;
                for (int i = inlineText.getSelectionStart(); i < inlineText.getSelectionEnd(); i++) {
                    char ch = string.charAt(i);
                    if (i != 0) {
                        Point2D point = vector.getGlyphPosition(i);
                        vector.setGlyphPosition(
                                i, new Point2D.Double(point.getX() + adjust, point.getY()));
                    }
                    if (ch == ' ' || ch == '\u00a0' || ch == '\u3000') {
                        adjust += info.getSpaceAdjust();
                    } else {
                        adjust += info.getNonSpaceAdjust();
                    }
                }

            }
        }
        c.getTextRenderer().drawGlyphVector(
                c.getOutputDevice(),
                glyphVector,
                iB.getAbsX() + inlineText.getX(),
                iB.getAbsY() + iB.getBaseline());
    }    

    public void drawBorderLine(
            Shape bounds, int side, int lineWidth, boolean solid) {
       /* int x = bounds.x;
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
            if (lineWidth % 2 != 0) {
                offset += 1;
            }
            drawLine(x + w - offset, y, x + w - offset, y + h - adj);
        } else if (side == BorderPainter.BOTTOM) {
            int offset = (int)(lineWidth / 2);
            if (lineWidth % 2 != 0) {
                offset += 1;
            }
            drawLine(x, y + h - offset, x + w - adj, y + h - offset);
        }*/
    	draw(bounds);
    }

    public void paintReplacedElement(RenderingContext c, BlockBox box) {
        ReplacedElement replaced = box.getReplacedElement();
        if (replaced instanceof SwingReplacedElement) {
            Rectangle contentBounds = box.getContentAreaEdge(box.getAbsX(), box.getAbsY(), c);
            JComponent component = ((SwingReplacedElement)box.getReplacedElement()).getJComponent();
            RootPanel canvas = (RootPanel)c.getCanvas();
            CellRendererPane pane = canvas.getCellRendererPane();
            pane.paintComponent(_graphics, component, canvas, contentBounds.x,  contentBounds.y, contentBounds.width, contentBounds.height,true);
        } else if (replaced instanceof ImageReplacedElement) {
            Image image = ((ImageReplacedElement)replaced).getImage();
            
            Point location = replaced.getLocation();
            _graphics.drawImage(
                    image, (int)location.getX(), (int)location.getY(), null);
        }
    }
    
    public void setColor(FSColor color) {
        if (color instanceof FSRGBColor) {
            FSRGBColor rgb = (FSRGBColor)color;
            _graphics.setColor(new Color(rgb.getRed(), rgb.getGreen(), rgb.getBlue()));
        } else {
            throw new RuntimeException("internal error: unsupported color class " + color.getClass().getName());
        }
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
    
    public void draw(Shape s) {
        _graphics.draw(s);
    }
    
    public void drawImage(FSImage image, int x, int y) {
        _graphics.drawImage(((AWTFSImage)image).getImage(), x, y, null);
    }
    
    public boolean isSupportsSelection() {
        return true;
    }
    
    public boolean isSupportsCMYKColors() {
        return true;
    }
}
