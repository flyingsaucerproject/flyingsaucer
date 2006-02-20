package org.xhtmlrenderer.pdf;

import java.net.URL;

import org.xhtmlrenderer.extend.FSImage;

public class PDFAsImage implements FSImage {
    private URL _source;
    
    private float _width;
    private float _height;
    
    private float _unscaledWidth;
    private float _unscaledHeight;
    
    public PDFAsImage(URL source) {
        _source = source;
    }
    
    public int getWidth() {
        return (int)_width;
    }

    public int getHeight() {
        return (int)_height;
    }

    public void scale(int width, int height) {
        int targetWidth = width;
        int targetHeight = height;
        
        if (targetWidth == -1) {
            targetWidth = getWidth() * (targetHeight / getHeight());
        }
        
        if (targetHeight == -1) {
            targetHeight = getHeight() * (targetWidth / getWidth());
        }
        
        _width = targetWidth;
        _height = targetHeight;
    }
    
    public URL getURL() {
        return _source;
    }
    
    public void setInitialWidth(float width) {
        if (_width == 0) {
            _width = width;
            _unscaledWidth = width;
        }
    }
    
    public void setInitialHeight(float height) {
        if (_height == 0) {
            _height = height;
            _unscaledHeight = height;
        }
    }
    
    public float getWidthAsFloat() {
        return _width;
    }
    
    public float getHeightAsFloat() {
        return _height;
    }

    public float getUnscaledHeight() {
        return _unscaledHeight;
    }

    public void setUnscaledHeight(float unscaledHeight) {
        _unscaledHeight = unscaledHeight;
    }

    public float getUnscaledWidth() {
        return _unscaledWidth;
    }

    public void setUnscaledWidth(float unscaledWidth) {
        _unscaledWidth = unscaledWidth;
    }
    
    public float scaleHeight() {
        return _height / _unscaledHeight;
    }
    
    public float scaleWidth() {
        return _width / _unscaledWidth;
    }

}
