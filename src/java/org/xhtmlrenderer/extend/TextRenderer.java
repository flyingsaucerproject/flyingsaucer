package org.xhtmlrenderer.extend;

import java.awt.Graphics2D;
import java.awt.Font;
import java.awt.font.*;
import java.awt.geom.*;

public interface TextRenderer {
    
    public void setupGraphics(Graphics2D graphics);
    public void drawString(Graphics2D graphics, String string, float x, float y);
    public LineMetrics getLineMetrics(Graphics2D graphics, Font font, String string);
    public Rectangle2D getLogicalBounds(Graphics2D graphics, Font font, String string);
    public void setFontScale(float scale);
    public float getFontScale();

    /* set to -1 for no antialiasing. set to 0 for all antialising.
    else, set to the threshold font size. does not take font scaling
    into account. */

    public void setSmoothingThreshold(float fontsize);
    public int getSmoothingLevel();
    public void setSmoothingLevel(int level);
    
    public final int NONE = 0;
    public final int LOW = 1;
    public final int MEDIUM = 2;
    public final int HIGH = 3;
}
