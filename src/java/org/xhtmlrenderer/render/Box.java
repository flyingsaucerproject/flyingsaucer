/*
 * {{{ header & license
 * Copyright (c) 2004, 2005 Joshua Marinacci
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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;

import org.w3c.dom.Element;
import org.xhtmlrenderer.css.constants.CSSName;
import org.xhtmlrenderer.css.constants.IdentValue;
import org.xhtmlrenderer.css.newmatch.CascadedStyle;
import org.xhtmlrenderer.css.style.CalculatedStyle;
import org.xhtmlrenderer.css.style.CssContext;
import org.xhtmlrenderer.css.style.derived.RectPropertySet;
import org.xhtmlrenderer.layout.Layer;
import org.xhtmlrenderer.layout.LayoutContext;
import org.xhtmlrenderer.util.Configuration;
import org.xhtmlrenderer.util.Uu;
import org.xhtmlrenderer.util.XRLog;

public abstract class Box {

    public Element element;

    // dimensions stuff
    /**
     * Box x-pos.
     */
    public int x;

    /**
     * Box y-pos.
     */
    public int y;

    private int absY;
    private int absX;

    /**
     * Box width.
     */
    //public int width;
    public int contentWidth;
    public int rightMBP = 0;
    public int leftMBP = 0;

    public int getWidth() {
        return contentWidth + leftMBP + rightMBP;
    }

    /**
     * Box height.
     */
    public int height;
    
    private Layer layer = null;
    private Layer containingLayer;
    
    private Box parent;

    // children stuff
    private List boxes;

    /**
     * Keep track of the start of childrens containing block.
     * Needed for hover.
     */
    public int tx;
    public int ty;

    private Style style;
    private Box containingBlock;

    public Box() {
    }

    /**
     * Return true if the target coordinates are inside of this box. The target
     * coordinates are already translated to be relative to the origin of this
     * box. ie x=0 & y=0. Thus the point 100,100 in a box with coordinates 20,20
     * x 90x90 would have the target coordinates passed in as 80,80 and the
     * function would return true.
     *
     * @param x PARAM
     * @param y PARAM
     * @return Returns
     */

    public boolean contains(int x, int y) {
        if ((x >= 0) && (x <= 0 + this.getWidth())) {
            if ((y >= 0) && (y <= 0 + this.height)) {
                return true;
            }
        }
        return false;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("Box: ");
        sb.append(" (" + x + "," + y + ")->(" + getWidth() + " x " + height + ")");
        return sb.toString();
    }

    public void addChild(LayoutContext c, Box child) {
        if (boxes == null) {
            boxes = new ArrayList();
        }
        if (child == null) {
            throw new NullPointerException("trying to add null child");
        }
        child.setParent(this);
        boxes.add(child);
        
        child.initContainingLayer(c);
    }

    public void removeAllChildren() {
        if (boxes != null) {
            boxes.clear();
        }
    }

    public void removeChild(Box child) {
        if (boxes != null) {
            boxes.remove(child);
        }
    }
    
    public void removeChild(int i) {
        if (boxes != null) {
            boxes.remove(i);
        }
    }

    public void setParent(Box box) {
        this.parent = box;
    }

    public int getHeight() {
        return height;
    }

    public Box getParent() {
        return parent;
    }

    public int getChildCount() {
        return boxes == null ? 0 : boxes.size();
    }

    public Box getChild(int i) {
        if (boxes == null) {
            throw new IndexOutOfBoundsException();
        } else {
            return (Box) boxes.get(i);
        }
    }

    public Iterator getChildIterator() {
        if (boxes == null) {
            return Collections.EMPTY_LIST.iterator();
        } else {
            return boxes.iterator();
        }
    }

    /**
     * This generates a string which fully represents every facet of the
     * rendered box (or at least as much as possible without actually drawing
     * it). This includes dimensions, location, color, backgrounds, images,
     * text, and pretty much everything else. The test string is used by the
     * regression tests.
     *
     * @return The testString value
     */
    /*
     * display_none
     * relative
     * fixed
     * top
     * right
     * bottom
     * left
     * floated
     * border_color
     * padding
     * border
     * margin
     * border_style
     * color
     * background_color
     * background_image
     * repeat
     * attachment
     * back_pos_vert
     * back_pos_horiz
     */
    public String getTestString() {
        StringBuffer sb = new StringBuffer();
        // type
        sb.append(" " + this.hashCode() + " ");
        if (this instanceof LineBox) {
            sb.append("line:");
        } else if (this instanceof InlineBox) {
            sb.append("inline:");
        } else {
            sb.append("box:");
        }

        sb.append("element:");
        if (this.element != null) {
            sb.append(this.element.getNodeName());
        } else {
            sb.append("null");
        }

        // dimensions and location
        sb.append("-box(" + x + "," + y + ")-(" + getWidth() + "x" + height + ")");

        if (style.isFixed()) {
            sb.append("-fixed");
        }
        if (style.isAbsolute()) {
            sb.append("-absolute");
        }
        if (style.isFloated()) {
            sb.append("-floated");
        }

        // no color support yet. wait for later

        // insets
        /*sb.append("insets(");
        sb.append("mar(" + this.margin.top + "," + this.margin.left + "," + this.margin.bottom + "," + this.margin.right + ")");
        sb.append("-bor(" + this.border.top + "," + this.border.left + "," + this.border.bottom + "," + this.border.right + ")");
        sb.append("-pad(" + this.padding.top + "," + this.padding.left + "," + this.padding.bottom + "," + this.padding.right + ")");
        sb.append(")"); */
		
        sb.append("-value:");
        if (element != null) {
            sb.append(element.getNodeValue());
        } else {
            sb.append("null");
        }

        return sb.toString();
    }


    public static final int NOTHING = 0;
    public static final int FLUX = 1;
    public static final int CHILDREN_FLUX = 2;
    public static final int DONE = 3;

    private static final Color TRANSPARENT = new Color(0, 0, 0, 0);
    private int state = NOTHING;

    public synchronized int getState() {
        return this.state;
    }

    public synchronized void setState(int state) {
        this.state = state;
    }

    public static String stateToString(int state) {
        switch (state) {
            case NOTHING:
                return "NOTHING";
            case FLUX:
                return "FLUX";
            case CHILDREN_FLUX:
                return "CHILDREN_FLUX";
            case DONE:
                return "DONE";
            default:
                return "unknown";
        }
    }

    private static void tileFill(Graphics g, Image img, Rectangle rect, int xoff, int yoff, boolean horiz, boolean vert) {
        int iwidth = img.getWidth(null);
        int iheight = img.getHeight(null);
        int rwidth = rect.width;
        int rheight = rect.height;
    
        if (horiz) {
            xoff = xoff % iwidth - iwidth;
            rwidth += iwidth;
        } else {
            rwidth = iwidth;
        }
    
        if (vert) {
            yoff = yoff % iheight - iheight;
            rheight += iheight;
        } else {
            rheight = iheight;
        }
    
        for (int i = 0; i < rwidth; i += iwidth) {
            for (int j = 0; j < rheight; j += iheight) {
                g.drawImage(img, i + rect.x + xoff, j + rect.y + yoff, null);
            }
        }
    
    }

    public final Style getStyle() {
        return this.style;
    }

    public void setStyle(Style style) {
        this.style = style;
    }

    public Box getContainingBlock() {
        return containingBlock == null ? getParent() : containingBlock;
    }

    public void setContainingBlock(Box containingBlock) {
        this.containingBlock = containingBlock;
    }
    
    public Rectangle getBounds(int left, int top, CssContext cssCtx, int tx, int ty) {
        // Looks unnecessarily convoluted, but necessary to get negative
        // margins right
        Rectangle result = getBorderEdge(left, top, cssCtx);
        addBackMargins(cssCtx, result);
        result.translate(tx, ty);
        return result;
    }

    public Rectangle getBounds(CssContext cssCtx, int tx, int ty) {
        return getBounds(this.x, this.y, cssCtx, tx, ty);
    }
    
    public Rectangle getPaintingBorderEdge(CssContext cssCtx) {
        return getBorderEdge(getAbsX(), getAbsY(), cssCtx);
    }

    /**
     * <B>NOTE</B>: This method does not consider any children of this box
     */
    public boolean intersects(CssContext cssCtx, Shape clip) {
        return clip == null || clip.intersects(getBorderEdge(getAbsX(), getAbsY(), cssCtx));
    }

    private void addBackMargins(CssContext cssCtx, Rectangle bounds) {
        RectPropertySet margin = getStyle().getMarginWidth(cssCtx);
        if (margin.top() > 0) {
            bounds.y -= margin.top();
            bounds.height += margin.top();
        }
        if (margin.right() > 0) {
            bounds.width += margin.right();
        }
        if (margin.bottom() > 0) {
            bounds.height += margin.bottom();
        }
        if (margin.left() > 0) {
            bounds.x -= margin.left();
            bounds.width += margin.left();
        }
    }

    protected Rectangle getBorderEdge(int left, int top, CssContext cssCtx) {
        RectPropertySet margin = getStyle().getMarginWidth(cssCtx);
        Rectangle result = new Rectangle(left + (int) margin.left(),
                top + (int) margin.top(),
                getWidth() - (int) margin.left() - (int) margin.right(),
                getHeight() - (int) margin.top() - (int) margin.bottom());
        return result;
    }

    public Rectangle getPaddingEdge(int left, int top, CssContext cssCtx) {
        RectPropertySet margin = getStyle().getMarginWidth(cssCtx);
        RectPropertySet border = getStyle().getCalculatedStyle().getBorder(cssCtx);
        Rectangle result = new Rectangle(left + (int) margin.left() + (int) border.left(),
                top + (int) margin.top() + (int) border.top(),
                getWidth() - (int) margin.width() - (int) border.width(),
                getHeight() - (int) margin.height() - (int) border.height());
        return result;
    }
    
    protected Rectangle getContentAreaEdge(int left, int top, CssContext cssCtx) {
        RectPropertySet margin = getStyle().getMarginWidth(cssCtx);
        RectPropertySet border = getStyle().getCalculatedStyle().getBorder(cssCtx);
        RectPropertySet padding = getStyle().getPaddingWidth(cssCtx);
        
        Rectangle result = new Rectangle(
                left + (int)margin.left() + (int)border.left() + (int)padding.left(),
                top + (int)margin.top() + (int)border.top() + (int)padding.top(),
                getWidth() - (int)margin.width() - (int)border.width() - (int)padding.width(),
                getHeight() - (int) margin.height() - (int) border.height() - (int) padding.height());
        return result;
    }

    public Layer getLayer() {
        return layer;
    }

    public void setLayer(Layer layer) {
        this.layer = layer;
    }

    public int getContentWidth() {
        return contentWidth;
    }
    
    public Dimension positionRelative(CssContext cssCtx) {
        int initialX = this.x;
        int initialY = this.y;
        
        CalculatedStyle style = getStyle().getCalculatedStyle();
        if (! style.isIdent(CSSName.LEFT, IdentValue.AUTO)) {
            this.x += style.getFloatPropertyProportionalWidth(
                    CSSName.LEFT, getContainingBlock().getContentWidth(), cssCtx);
        } else if (! style.isIdent(CSSName.RIGHT, IdentValue.AUTO)) {
            this.x += style.getFloatPropertyProportionalWidth(
                    CSSName.LEFT, getContainingBlock().getContentWidth(), cssCtx);
        }
        
        int cbContentHeight = 0;
        if (! getContainingBlock().getStyle().isAutoHeight()) {
            CalculatedStyle cbStyle = getContainingBlock().getStyle().getCalculatedStyle();
            cbContentHeight = (int)cbStyle.getFloatPropertyProportionalHeight(
                    CSSName.HEIGHT, 0, cssCtx);
        }
        
        if (!style.isIdent(CSSName.TOP, IdentValue.AUTO)) {
            this.y += style.getFloatPropertyProportionalHeight(
                    CSSName.TOP, cbContentHeight, cssCtx);
        } else if (!style.isIdent(CSSName.BOTTOM, IdentValue.AUTO)) {
            this.y += style.getFloatPropertyProportionalHeight(
                    CSSName.TOP, cbContentHeight, cssCtx);
        }
        
        return new Dimension(this.x - initialX, this.y - initialY);
    }
    
    // HACK If a box doesn't have a Style object, NPEs are the likely result
    // However, it begs the question if a Style object is being used in places
    // it doesn't make sense (e.g. line boxes)
    public void createDefaultStyle(LayoutContext c) {
        c.pushStyle(CascadedStyle.emptyCascadedStyle);
        setStyle(new Style(c.getCurrentStyle(), 0));
        c.popStyle();
    }

    public void setAbsY(int absY) {
        this.absY = absY;
    }

    public int getAbsY() {
        return absY;
    }

    public void setAbsX(int absX) {
        this.absX = absX;
    }

    public int getAbsX() {
        return absX;
    }

    public boolean isStyled() {
        return style != null;
    }
    
    protected int getBorderSides() {
        return BorderPainter.ALL;
    }
    
    public void paintBorder(RenderingContext c) {
        if (! getStyle().isVisible()) {
            return;
        }
        
        Rectangle borderBounds = getPaintingBorderEdge(c);
        if (getState() != Box.DONE) {
            borderBounds.height += c.getCanvas().getHeight();
        }
    
        BorderPainter.paint(borderBounds, getBorderSides(),
                getStyle().getCalculatedStyle(), c.getGraphics(), c, 0);
    }

    private Image getBackgroundImage(RenderingContext c) {
        String uri = getStyle().getCalculatedStyle().getStringProperty(CSSName.BACKGROUND_IMAGE);
        if (! uri.equals("none")) {
            try {
                return c.getUac().getImageResource(uri).getImage();
            } catch (Exception ex) {
                ex.printStackTrace();
                Uu.p(ex);
            }
        }
        return null;
    }

    public void paintBackground(RenderingContext c) {
        if (!Configuration.isTrue("xr.renderer.draw.backgrounds", true)) {
            return;
        }
        
        if (! getStyle().isVisible()) {
            return;
        }
        
        Color backgroundColor = getStyle().getCalculatedStyle().getBackgroundColor();
        Image backgroundImage = getBackgroundImage(c);
        
        if ( (backgroundColor == null || backgroundColor.equals(TRANSPARENT)) &&
                backgroundImage == null) {
            return;
        }
    
        Rectangle backgroundBounds = getPaintingBorderEdge(c);
        if (getState() != Box.DONE) {
            backgroundBounds.height += c.getCanvas().getHeight();
        }
        
        if (backgroundColor != null && ! backgroundColor.equals(TRANSPARENT)) {
            c.getGraphics().setColor(backgroundColor);
            c.getGraphics().fillRect(backgroundBounds.x, backgroundBounds.y, backgroundBounds.width, backgroundBounds.height);
        }
    
        int xoff = 0;
        int yoff = 0;
        
        if (backgroundImage != null) {
            Shape oldclip = (Shape) c.getGraphics().getClip();
    
            if (getStyle().isFixedBackground()) {
                yoff = c.getCanvas().getLocation().y;
                c.getGraphics().setClip(c.getCanvas().getVisibleRect());
            }
    
            c.getGraphics().clip(backgroundBounds);
    
            int imageWidth = backgroundImage.getWidth(null);
            int imageHeight = backgroundImage.getHeight(null);
    
            Point bgOffset = getStyle().getCalculatedStyle().getBackgroundPosition(backgroundBounds.width - imageWidth,
                    backgroundBounds.height - imageHeight, c);
            xoff += bgOffset.x;
            yoff -= bgOffset.y;
    
            tileFill(c.getGraphics(), backgroundImage,
                    backgroundBounds,
                    xoff, -yoff,
                    getStyle().isHorizontalBackgroundRepeat(),
                    getStyle().isVerticalBackgroundRepeat());
            c.getGraphics().setClip(oldclip);
        }
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public Layer getContainingLayer() {
        return containingLayer;
    }

    public void setContainingLayer(Layer containingLayer) {
        this.containingLayer = containingLayer;
    }
    
    public void initContainingLayer(LayoutContext c) {
        if (getLayer() != null) {
            setContainingLayer(getLayer());
        } else if (getContainingLayer() == null) {
            if (getParent() == null || getParent().getContainingLayer() == null) {
                throw new RuntimeException("internal error");
            }
            setContainingLayer(getParent().getContainingLayer());
            
            // FIXME Will be glacially slow for large inline relative layers.  Could 
            // be much more efficient.  We're just looking for block boxes which are
            // directly wrapped by an inline relative layer (i.e. block boxes sandwiched
            // between anonymous block boxes)
            if (c.getLayer().isInline()) {
                List content = 
                    ((InlineBox)c.getLayer().getMaster()).getElementWithContent();
                if (content.contains(this)) {
                    setContainingLayer(c.getLayer());
                }
            }
        }
    }
    
    public void connectChildrenToCurrentLayer(LayoutContext c) {
        
        for (int i = 0; i < getChildCount(); i++) {
            Box box = getChild(i);
            box.setContainingLayer(c.getLayer());
            box.connectChildrenToCurrentLayer(c);
        }
    }
    
    public List getElementBoxes(Element elem) {
        List result = new ArrayList();
        for (int i = 0; i < getChildCount(); i++) {
            Box child = getChild(i);
            if (child.element == elem) {
                result.add(child);
            }
            result.addAll(child.getElementBoxes(elem));
        }
        return result;
    }
    
    protected void paintDebugOutline(RenderingContext c, Color color) {
        Color oldColor = c.getGraphics().getColor();
        
        c.getGraphics().setColor(color);
        Rectangle rect = getBounds(getAbsX(), getAbsY(), c, 0, 0);
        rect.height -= 1;
        rect.width -= 1;
        c.getGraphics().drawRect(rect.x, rect.y, rect.width, rect.height);
        c.getGraphics().setColor(oldColor);
    }
    
    public void detach() {
        detachChildren();
        if (this.layer != null) {
            this.layer.detach();
            this.layer = null;
        }
        if (getParent() != null) {
            getParent().removeChild(this);
        }
        setParent(null);
    }
    
    public void detachChildren(int start, int end) {
        for (int i = start; i <= end; i++) {
            Box box = getChild(start);
            box.detach();
        }
    }
    
    protected void detachChildren() {
        int remaining = getChildCount();
        while (remaining-- > 0) {
            Box box = getChild(0);
            box.detach();
        }
    }
    
    public abstract void calcCanvasLocation();
    
    public void calcChildLocations() {
        for (int i = 0; i < getChildCount(); i++) {
            Box child = getChild(i);
            child.calcCanvasLocation();
            child.calcChildLocations();
        }
    }
    
    public int moveToNextPage(LayoutContext c) {
        if (c.isLayingOutTable()) {
            return 0;
        }
        
        PageBox page = c.getRootLayer().getFirstPage(c, this);
        if (page == null) {
            XRLog.layout(Level.WARNING, "Box has no page");
            return 0;
        } else {
            if (page.getTop() == getAbsY()) {
                return 0;
            } else {
                int delta = page.getBottom() - getAbsY();
                this.y += delta;
                if (page == c.getRootLayer().getLastPage()) {
                    c.getRootLayer().addPage(c);
                }
                return delta;
            }
        }
    }

    public void expandToPageBottom(LayoutContext c) {
        if (c.isLayingOutTable()) {
            return;
        }
        
        PageBox page = c.getRootLayer().getLastPage(c, this);
        int delta = page.getBottom() - (getAbsY() + 
                getStyle().getMarginBorderPadding(c, CalculatedStyle.TOP) + this.height);
        this.height += delta;
        if (page == c.getRootLayer().getLastPage()) {   
            c.getRootLayer().addPage(c);
        }
    }
    
    public boolean crossesPageBreak(LayoutContext c) {
        if (c.isLayingOutTable()) {
            return false;
        }
        
        PageBox pageBox = c.getRootLayer().getFirstPage(c, this);
        if (pageBox == null) {
            return false;
        } else {
            return getAbsY() + getHeight() >= pageBox.getBottom();
        }
    }
}

/*
 * $Id$
 *
 * $Log$
 * Revision 1.103  2006/01/10 19:56:00  peterbrant
 * Fix inappropriate box resizing when width: auto
 *
 * Revision 1.102  2006/01/09 23:25:22  peterbrant
 * Correct (?) position of debug outline
 *
 * Revision 1.101  2006/01/04 19:50:14  peterbrant
 * More pagination bug fixes / Implement simple pagination for tables
 *
 * Revision 1.100  2006/01/03 23:55:57  peterbrant
 * Add support for proper page breaking of floats / More bug fixes to pagination support
 *
 * Revision 1.99  2006/01/03 02:12:20  peterbrant
 * Various pagination fixes / Fix fixed positioning
 *
 * Revision 1.98  2006/01/02 20:59:09  peterbrant
 * Implement page-break-before/after: avoid
 *
 * Revision 1.97  2006/01/01 03:14:25  peterbrant
 * Implement page-break-inside: avoid
 *
 * Revision 1.96  2006/01/01 02:38:19  peterbrant
 * Merge more pagination work / Various minor cleanups
 *
 * Revision 1.95  2005/12/30 01:32:39  peterbrant
 * First merge of parts of pagination work
 *
 * Revision 1.94  2005/12/28 00:50:52  peterbrant
 * Continue ripping out first try at pagination / Minor method name refactoring
 *
 * Revision 1.93  2005/12/21 02:36:29  peterbrant
 * - Calculate absolute positions incrementally (prep work for pagination)
 * - Light cleanup
 * - Fix bug where floats nested in floats could cause the outer float to be positioned in the wrong place
 *
 * Revision 1.92  2005/12/17 02:24:14  peterbrant
 * Remove last pieces of old (now non-working) clip region checking / Push down handful of fields from Box to BlockBox
 *
 * Revision 1.91  2005/12/15 20:04:47  peterbrant
 * Implement visibility: hidden
 *
 * Revision 1.90  2005/12/14 22:06:47  peterbrant
 * Fix NPE
 *
 * Revision 1.89  2005/12/13 02:41:33  peterbrant
 * Initial implementation of vertical-align: top/bottom (not done yet) / Minor cleanup and optimization
 *
 * Revision 1.88  2005/12/11 02:51:18  peterbrant
 * Minor tweak (misread spec)
 *
 * Revision 1.87  2005/12/10 03:11:43  peterbrant
 * Use margin edge not content edge
 *
 * Revision 1.86  2005/12/09 21:41:19  peterbrant
 * Finish support for relative inline layers
 *
 * Revision 1.85  2005/12/09 01:24:56  peterbrant
 * Initial commit of relative inline layers
 *
 * Revision 1.84  2005/12/08 02:21:26  peterbrant
 * Fix positioning bug when CB of absolute block is a relative block
 *
 * Revision 1.83  2005/12/07 03:14:20  peterbrant
 * Fixes to final float position when float BFC is not contained in the layer being positioned / Implement 10.6.7 of the spec
 *
 * Revision 1.82  2005/12/07 00:33:12  peterbrant
 * :first-letter and :first-line works again
 *
 * Revision 1.81  2005/12/05 00:13:53  peterbrant
 * Improve list-item support (marker positioning is now correct) / Start support for relative inline layers
 *
 * Revision 1.80  2005/11/29 02:37:24  peterbrant
 * Make clear work again / Rip out old pagination code
 *
 * Revision 1.79  2005/11/25 16:57:20  peterbrant
 * Initial commit of inline content refactoring
 *
 * Revision 1.78  2005/11/13 01:14:16  tobega
 * Take into account the height of a first-letter. Also attempt to line-break better with inline padding.
 *
 * Revision 1.77  2005/11/10 18:27:28  peterbrant
 * Position absolute box correctly when top: auto and bottom: auto.
 *
 * Revision 1.76  2005/11/10 01:55:16  peterbrant
 * Further progress on layer work
 *
 * Revision 1.75  2005/11/09 18:41:28  peterbrant
 * Fixes to vertical margin collapsing in the presence of floats / Paint floats as
 * layers
 *
 * Revision 1.74  2005/11/08 20:03:57  peterbrant
 * Further progress on painting order / improved positioning implementation
 *
 * Revision 1.73  2005/11/05 23:19:07  peterbrant
 * Always add fixed layers to root layer / If element has fixed background just
 * note this on the root layer instead of property in Box
 *
 * Revision 1.72  2005/11/05 18:45:06  peterbrant
 * General cleanup / Remove obsolete code
 *
 * Revision 1.71  2005/11/05 03:30:01  peterbrant
 * Start work on painting order and improved positioning implementation
 *
 * Revision 1.70  2005/11/03 17:58:41  peterbrant
 * Float rewrite (still stomping bugs, but demos work)
 *
 * Revision 1.69  2005/11/02 18:15:29  peterbrant
 * First merge of Tobe's and my stacking context work / Rework float code (not done yet)
 *
 * Revision 1.68  2005/10/30 22:06:15  peterbrant
 * Only create child List if necessary
 *
 * Revision 1.67  2005/10/29 22:31:01  tobega
 * House-cleaning
 *
 * Revision 1.66  2005/10/27 00:09:02  tobega
 * Sorted out Context into RenderingContext and LayoutContext
 *
 * Revision 1.65  2005/10/18 20:57:05  tobega
 * Patch from Peter Brant
 *
 * Revision 1.64  2005/10/15 23:39:18  tobega
 * patch from Peter Brant
 *
 * Revision 1.63  2005/10/12 21:17:13  tobega
 * patch from Peter Brant
 *
 * Revision 1.62  2005/10/08 17:40:21  tobega
 * Patch from Peter Brant
 *
 * Revision 1.61  2005/10/06 03:20:22  tobega
 * Prettier incremental rendering. Ran into more trouble than expected and some creepy crawlies and a few pages don't look right (forms.xhtml, splash.xhtml)
 *
 * Revision 1.60  2005/10/02 21:30:00  tobega
 * Fixed a lot of concurrency (and other) issues from incremental rendering. Also some house-cleaning.
 *
 * Revision 1.59  2005/09/30 04:58:05  joshy
 * fixed garbage when showing a document with a fixed positioned block
 *
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.58  2005/09/29 21:34:04  joshy
 * minor updates to a lot of files. pulling in more incremental rendering code.
 * fixed another resize bug
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.57  2005/09/26 22:40:21  tobega
 * Applied patch from Peter Brant concerning margin collapsing
 *
 * Revision 1.56  2005/07/04 00:12:12  tobega
 * text-align now works for table-cells too (is done in render, not in layout)
 *
 * Revision 1.55  2005/06/16 07:24:51  tobega
 * Fixed background image bug.
 * Caching images in browser.
 * Enhanced LinkListener.
 * Some house-cleaning, playing with Idea's code inspection utility.
 *
 * Revision 1.54  2005/06/16 04:31:30  joshy
 * added clear support to the box
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.53  2005/05/13 15:23:55  tobega
 * Done refactoring box borders, margin and padding. Hover is working again.
 *
 * Revision 1.52  2005/05/13 11:49:59  tobega
 * Started to fix up borders on inlines. Got caught up in refactoring.
 * Boxes shouldn't cache borders and stuff unless necessary. Started to remove unnecessary references.
 * Hover is not working completely well now, might get better when I'm done.
 *
 * Revision 1.51  2005/05/08 14:36:58  tobega
 * Refactored away the need for having a context in a CalculatedStyle
 *
 * Revision 1.50  2005/04/22 17:19:19  joshy
 * resovled conflicts in Box
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.49  2005/04/21 18:16:08  tobega
 * Improved handling of inline padding. Also fixed first-line handling according to spec.
 *
 * Revision 1.48  2005/04/19 17:51:18  joshy
 * fixed absolute positioning bug
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.47  2005/04/19 13:59:48  pdoubleya
 * Added defaults for margin, padding, border.
 *
 * Revision 1.46  2005/02/03 23:16:16  pdoubleya
 * .
 *
 * Revision 1.45  2005/01/31 22:51:35  pdoubleya
 * Added caching for padding/margin/border calcs, plus alternate calls to get their totals (with and without style available). Reformatted.
 *
 * Revision 1.44  2005/01/29 20:24:23  pdoubleya
 * Clean/reformat code. Removed commented blocks, checked copyright.
 *
 * Revision 1.43  2005/01/24 22:46:42  pdoubleya
 * Added support for ident-checks using IdentValue instead of string comparisons.
 *
 * Revision 1.42  2005/01/24 19:01:03  pdoubleya
 * Mass checkin. Changed to use references to CSSName, which now has a Singleton instance for each property, everywhere property names were being used before. Removed commented code. Cascaded and Calculated style now store properties in arrays rather than maps, for optimization.
 *
 * Revision 1.41  2005/01/24 14:36:35  pdoubleya
 * Mass commit, includes: updated for changes to property declaration instantiation, and new use of DerivedValue. Removed any references to older XR... classes (e.g. XRProperty). Cleaned imports.
 *
 * Revision 1.40  2005/01/16 18:50:05  tobega
 * Re-introduced caching of styles, which make hamlet and alice scroll nicely again. Background painting still slow though.
 *
 * Revision 1.39  2005/01/09 15:22:50  tobega
 * Prepared improved handling of margins, borders and padding.
 *
 * Revision 1.38  2005/01/07 00:29:29  tobega
 * Removed Content reference from Box (mainly to reduce memory footprint). In the process stumbled over and cleaned up some messy stuff.
 *
 * Revision 1.37  2005/01/05 23:15:09  tobega
 * Got rid of some redundant code for hover-styling
 *
 * Revision 1.36  2005/01/05 01:10:15  tobega
 * Went wild with code analysis tool. removed unused stuff. Lucky we have CVS...
 *
 * Revision 1.35  2005/01/02 01:00:09  tobega
 * Started sketching in code for handling replaced elements in the NamespaceHandler
 *
 * Revision 1.34  2004/12/29 12:57:27  tobega
 * Trying to handle BFC:s right
 *
 * Revision 1.33  2004/12/28 02:15:19  tobega
 * More cleaning.
 *
 * Revision 1.32  2004/12/28 01:48:24  tobega
 * More cleaning. Magically, the financial report demo is starting to look reasonable, without any effort being put on it.
 *
 * Revision 1.31  2004/12/27 09:40:48  tobega
 * Moved more styling to render stage. Now inlines have backgrounds and borders again.
 *
 * Revision 1.30  2004/12/27 07:43:32  tobega
 * Cleaned out border from box, it can be gotten from current style. Is it maybe needed for dynamic stuff?
 *
 * Revision 1.29  2004/12/16 15:53:10  joshy
 * fixes for absolute layout
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.28  2004/12/13 15:15:57  joshy
 * fixed bug where inlines would pick up parent styles when they aren't supposed to
 * fixed extra Xx's in printed text
 * added conf boolean to turn on box outlines
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.27  2004/12/12 23:19:26  tobega
 * Tried to get hover working. Something happens, but not all that's supposed to happen.
 *
 * Revision 1.26  2004/12/12 03:33:00  tobega
 * Renamed x and u to avoid confusing IDE. But that got cvs in a twist. See if this does it
 *
 * Revision 1.25  2004/12/11 23:36:49  tobega
 * Progressing on cleaning up layout and boxes. Still broken, won't even compile at the moment. Working hard to fix it, though.
 *
 * Revision 1.24  2004/12/11 18:18:11  tobega
 * Still broken, won't even compile at the moment. Working hard to fix it, though. Replace the StyleReference interface with our only concrete implementation, it was a bother changing in two places all the time.
 *
 * Revision 1.23  2004/12/10 06:51:04  tobega
 * Shamefully, I must now check in painfully broken code. Good news is that Layout is much nicer, and we also handle :before and :after, and do :first-line better than before. Table stuff must be brought into line, but most needed is to fix Render. IMO Render should work with Boxes and Content. If Render goes for a node, that is wrong.
 *
 * Revision 1.22  2004/12/09 21:18:53  tobega
 * precaution: code still works
 *
 * Revision 1.21  2004/12/09 18:00:05  joshy
 * fixed hover bugs
 * fixed li's not being blocks bug
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.20  2004/12/05 05:22:36  joshy
 * fixed NPEs in selection listener
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.19  2004/12/05 05:18:02  joshy
 * made bullets be anti-aliased
 * fixed bug in link listener that caused NPEs
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.18  2004/12/05 00:48:59  tobega
 * Cleaned up so that now all property-lookups use the CalculatedStyle. Also added support for relative values of top, left, width, etc.
 *
 * Revision 1.17  2004/12/01 01:57:02  joshy
 * more updates for float support.
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.16  2004/11/18 16:45:13  joshy
 * improved the float code a bit.
 * now floats are automatically forced to be blocks
 *
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.15  2004/11/17 00:44:54  joshy
 * fixed bug in the history manager
 * added cursor support to the link listener
 *
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.14  2004/11/15 15:20:39  joshy
 * fixes for absolute layout
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.13  2004/11/12 20:25:18  joshy
 * added hover support to the browser
 * created hover demo
 * fixed bug with inline borders
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.12  2004/11/12 17:05:25  joshy
 * support for fixed positioning
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.11  2004/11/09 02:04:23  joshy
 * support for text-align: justify
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.10  2004/11/08 15:10:10  joshy
 * added support for styling :first-letter inline boxes
 * updated the absolute positioning tests
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.9  2004/11/07 16:23:18  joshy
 * added support for lighten and darken to bordercolor
 * added support for different colored sides
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.8  2004/11/06 22:49:52  joshy
 * cleaned up alice
 * initial support for inline borders and backgrounds
 * moved all of inlinepainter back into inlinerenderer, where it belongs.
 *
 *
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.7  2004/11/03 23:54:34  joshy
 * added hamlet and tables to the browser
 * more support for absolute layout
 * added absolute layout unit tests
 * removed more dead code and moved code into layout factory
 *
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.6  2004/11/03 15:17:05  joshy
 * added intial support for absolute positioning
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.5  2004/10/23 13:50:26  pdoubleya
 * Re-formatted using JavaStyle tool.
 * Cleaned imports to resolve wildcards except for common packages (java.io, java.util, etc).
 * Added CVS log comments at bottom.
 *
 *
 */

