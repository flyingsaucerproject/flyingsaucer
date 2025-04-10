package org.xhtmlrenderer.demo.browser;

public record ScaleFactor(double factor, String zoomLabel) {
    public static final double PAGE_WIDTH = -2.0d;
    public static final double PAGE_HEIGHT = -3.0d;
    public static final double PAGE_WHOLE = -4.0d;

    @Override
    public String toString() {
        return zoomLabel();
    }

    public boolean isNotZoomed() {
        return factor() == 1d;
    }
}
