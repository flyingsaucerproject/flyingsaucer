package org.xhtmlrenderer.render;

import java.awt.Font;
import java.awt.font.LineMetrics;

public class ScaledLineMetrics extends LineMetrics {
    private LineMetrics delegate;
    
    private float scalingFactor;
    
    public ScaledLineMetrics(Font f, LineMetrics metrics) {
        float height = metrics.getAscent() + metrics.getDescent();
        
        this.scalingFactor = f.getSize2D() / height;
        this.delegate = metrics;
    }

    public float getAscent() {
        return scalingFactor * delegate.getAscent();
    }

    public int getBaselineIndex() {
        return delegate.getBaselineIndex();
    }

    public float[] getBaselineOffsets() {
        float[] raw = delegate.getBaselineOffsets();
        float[] result = new float[raw.length];
        for (int i = 0; i < raw.length; i++) {
            result[i] = raw[i] * scalingFactor;
        }
        return result;
    }

    public float getDescent() {
        return scalingFactor * delegate.getDescent();
    }

    public float getHeight() {
        return scalingFactor * delegate.getHeight();
    }

    public float getLeading() {
        return scalingFactor * delegate.getLeading();
    }

    public int getNumChars() {
        return delegate.getNumChars();
    }

    public float getStrikethroughOffset() {
        return scalingFactor * delegate.getStrikethroughOffset();
    }

    public float getStrikethroughThickness() {
        return delegate.getStrikethroughThickness();
    }

    public float getUnderlineOffset() {
        return scalingFactor * delegate.getUnderlineOffset();
    }

    public float getUnderlineThickness() {
        return delegate.getUnderlineThickness();
    }
    
    
}
