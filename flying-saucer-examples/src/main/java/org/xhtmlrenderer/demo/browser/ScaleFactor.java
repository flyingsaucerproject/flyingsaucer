package org.xhtmlrenderer.demo.browser;

/**
 * Date: Jul 13, 2007
* Time: 12:38:39 PM
*
* @author pwright
*/
public class ScaleFactor {
    public static final double PAGE_WIDTH = -2.0d;
    public static final double PAGE_HEIGHT = -3.0d;
    public static final double PAGE_WHOLE = -4.0d;
    private final double factor;
    private final String zoomLabel;

    public ScaleFactor(double factor, String zoomLabel) {
        super();
        this.factor = factor;
        this.zoomLabel = zoomLabel;
    }

    public double getFactor() {
        return factor;
    }

    public String getZoomLabel() {
        return zoomLabel;
    }

    public String toString() {
        return getZoomLabel();
    }

    public boolean isNotZoomed() {
        return getFactor() == 1d;
    }
}
