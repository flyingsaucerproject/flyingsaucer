/*
 * {{{ header & license
 * Copyright (c) 2005 Wisconsin Court System
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
package org.xhtmlrenderer.render;

import java.awt.Image;

import org.xhtmlrenderer.extend.FSImage;

public class MarkerData {
    private StrutMetrics structMetrics;
    
    private TextMarker textMarker;
    private GlyphMarker glyphMarker;
    private ImageMarker imageMarker;
    
    private LineBox referenceLine;
    private LineBox previousReferenceLine;

    public TextMarker getTextMarker() {
        return textMarker;
    }

    public void setTextMarker(TextMarker markerText) {
        this.textMarker = markerText;
    }

    public GlyphMarker getGlyphMarker() {
        return glyphMarker;
    }

    public void setGlyphMarker(GlyphMarker glyphMarker) {
        this.glyphMarker = glyphMarker;
    }

    public ImageMarker getImageMarker() {
        return imageMarker;
    }

    public void setImageMarker(ImageMarker imageMarker) {
        this.imageMarker = imageMarker;
    }

    public StrutMetrics getStructMetrics() {
        return structMetrics;
    }

    public void setStructMetrics(StrutMetrics structMetrics) {
        this.structMetrics = structMetrics;
    }
    
    public int getLayoutWidth() {
        if (textMarker != null) {
            return textMarker.getLayoutWidth();
        } else if (glyphMarker != null) {
            return glyphMarker.getLayoutWidth();
        } else if (imageMarker != null) {
            return imageMarker.getLayoutWidth();
        } else {
            return 0;
        }
    }

    public LineBox getReferenceLine() {
        return referenceLine;
    }

    public void setReferenceLine(LineBox referenceLine) {
        this.previousReferenceLine = this.referenceLine;
        this.referenceLine = referenceLine;
    }
    
    public void restorePreviousReferenceLine(LineBox current) {
        if (current == this.referenceLine) {
            this.referenceLine = this.previousReferenceLine;
        }
    }
    
    public static class ImageMarker {
        private int layoutWidth;
        private FSImage image;
        
        public FSImage getImage() {
            return image;
        }
        public void setImage(FSImage image) {
            this.image = image;
        }
        public int getLayoutWidth() {
            return layoutWidth;
        }
        public void setLayoutWidth(int layoutWidth) {
            this.layoutWidth = layoutWidth;
        }
    }
    
    public static class GlyphMarker {
        private int diameter;
        private int layoutWidth;
        
        public int getDiameter() {
            return diameter;
        }
        
        public void setDiameter(int diameter) {
            this.diameter = diameter;
        }
        
        public int getLayoutWidth() {
            return layoutWidth;
        }
        
        public void setLayoutWidth(int layoutWidth) {
            this.layoutWidth = layoutWidth;
        }
    }
    
    public static class TextMarker {
        private String text;
        private int layoutWidth;
        
        public TextMarker() {
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public int getLayoutWidth() {
            return layoutWidth;
        }

        public void setLayoutWidth(int width) {
            this.layoutWidth = width;
        }
    }    
}
