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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 * }}}
 */
package org.xhtmlrenderer.render;

import org.xhtmlrenderer.extend.FSImage;

/**
 * A bean containing information necessary to draw a list marker.  This includes
 * font information from the block (for selecting the correct font when drawing
 * a text marker) or the data necessary to draw other types of markers.  It
 * also includes a reference to the first line box in the block box (which in
 * turn may be nested inside of other block boxes).  All markers are drawn 
 * relative to the baseline of this line box.
 */
public class MarkerData {
    private StrutMetrics _structMetrics;
    
    private TextMarker _textMarker;
    private GlyphMarker _glyphMarker;
    private ImageMarker _imageMarker;
    
    private LineBox _referenceLine;
    private LineBox _previousReferenceLine;

    public TextMarker getTextMarker() {
        return _textMarker;
    }

    public void setTextMarker(TextMarker markerText) {
        _textMarker = markerText;
    }

    public GlyphMarker getGlyphMarker() {
        return _glyphMarker;
    }

    public void setGlyphMarker(GlyphMarker glyphMarker) {
        _glyphMarker = glyphMarker;
    }

    public ImageMarker getImageMarker() {
        return _imageMarker;
    }

    public void setImageMarker(ImageMarker imageMarker) {
        _imageMarker = imageMarker;
    }

    public StrutMetrics getStructMetrics() {
        return _structMetrics;
    }

    public void setStructMetrics(StrutMetrics structMetrics) {
        _structMetrics = structMetrics;
    }
    
    public int getLayoutWidth() {
        if (_textMarker != null) {
            return _textMarker.getLayoutWidth();
        } else if (_glyphMarker != null) {
            return _glyphMarker.getLayoutWidth();
        } else if (_imageMarker != null) {
            return _imageMarker.getLayoutWidth();
        } else {
            return 0;
        }
    }

    public LineBox getReferenceLine() {
        return _referenceLine;
    }

    public void setReferenceLine(LineBox referenceLine) {
        _previousReferenceLine = _referenceLine;
        _referenceLine = referenceLine;
    }
    
    public void restorePreviousReferenceLine(LineBox current) {
        if (current == _referenceLine) {
            _referenceLine = _previousReferenceLine;
        }
    }
    
    public static class ImageMarker {
        private int _layoutWidth;
        private FSImage _image;
        
        public FSImage getImage() {
            return _image;
        }
        public void setImage(FSImage image) {
            _image = image;
        }
        public int getLayoutWidth() {
            return _layoutWidth;
        }
        public void setLayoutWidth(int layoutWidth) {
            _layoutWidth = layoutWidth;
        }
    }
    
    public static class GlyphMarker {
        private int _diameter;
        private int _layoutWidth;
        
        public int getDiameter() {
            return _diameter;
        }
        
        public void setDiameter(int diameter) {
            _diameter = diameter;
        }
        
        public int getLayoutWidth() {
            return _layoutWidth;
        }
        
        public void setLayoutWidth(int layoutWidth) {
            _layoutWidth = layoutWidth;
        }
    }
    
    public static class TextMarker {
        private String _text;
        private int _layoutWidth;
        
        public TextMarker() {
        }

        public String getText() {
            return _text;
        }

        public void setText(String text) {
            _text = text;
        }

        public int getLayoutWidth() {
            return _layoutWidth;
        }

        public void setLayoutWidth(int width) {
            _layoutWidth = width;
        }
    }    
}
