/*
 * {{{ header & license
 * Copyright (c) 2007 Wisconsin Court System
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
package org.xhtmlrenderer.docx;

import java.awt.Rectangle;
import java.awt.RenderingHints.Key;
import java.awt.Shape;
import java.awt.Stroke;

import org.xhtmlrenderer.css.parser.FSColor;
import org.xhtmlrenderer.extend.FSImage;
import org.xhtmlrenderer.render.AbstractOutputDevice;
import org.xhtmlrenderer.render.BlockBox;
import org.xhtmlrenderer.render.FSFont;
import org.xhtmlrenderer.render.InlineText;
import org.xhtmlrenderer.render.RenderingContext;

public class Docx4jDocxOutputDevice extends AbstractOutputDevice {

   @Override
   public void draw(Shape s) {
      // TODO Auto-generated method stub
      
   }

   @Override
   public void drawBorderLine(Shape bounds, int side, int width, boolean solid) {
      // TODO Auto-generated method stub
      
   }

    public void drawSelection(RenderingContext c, InlineText inlineText) {
        // TODO Auto-generated method stub
        
    }

    public void paintReplacedElement(RenderingContext c, BlockBox box) {
        // TODO Auto-generated method stub
        
    }

    public void setFont(FSFont font) {
        // TODO Auto-generated method stub
        
    }

    public void setColor(FSColor color) {
        // TODO Auto-generated method stub
        
    }

    public void drawRect(int x, int y, int width, int height) {
        // TODO Auto-generated method stub
        
    }

    public void drawOval(int x, int y, int width, int height) {
        // TODO Auto-generated method stub
        
    }

    public void drawBorderLine(Rectangle bounds, int side, int width, boolean solid) {
        // TODO Auto-generated method stub
        
    }

    public void drawImage(FSImage image, int x, int y) {
        // TODO Auto-generated method stub
        
    }

    public void fill(Shape s) {
        // TODO Auto-generated method stub
        
    }

    public void fillRect(int x, int y, int width, int height) {
        // TODO Auto-generated method stub
        
    }

    public void fillOval(int x, int y, int width, int height) {
        // TODO Auto-generated method stub
        
    }

    public void clip(Shape s) {
        // TODO Auto-generated method stub
        
    }

    public Shape getClip() {
        // TODO Auto-generated method stub
        return null;
    }

    public void setClip(Shape s) {
        // TODO Auto-generated method stub
        
    }

    public void translate(double tx, double ty) {
        // TODO Auto-generated method stub
        
    }

    public void setStroke(Stroke s) {
        // TODO Auto-generated method stub
        
    }

    public Stroke getStroke() {
        // TODO Auto-generated method stub
        return null;
    }

    public Object getRenderingHint(Key key) {
        // TODO Auto-generated method stub
        return null;
    }

    public void setRenderingHint(Key key, Object value) {
        // TODO Auto-generated method stub
        
    }

    public boolean isSupportsSelection() {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean isSupportsCMYKColors() {
        // TODO Auto-generated method stub
        return false;
    }

    protected void drawLine(int x1, int y1, int x2, int y2) {
        // TODO Auto-generated method stub
        
    }

    public void drawString(String string, float x, float y, Object object) {
        // TODO Auto-generated method stub
        //System.out.println(string);
        
    }

}
