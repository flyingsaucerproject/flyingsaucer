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

import org.xhtmlrenderer.css.constants.CSSName;
import org.xhtmlrenderer.css.constants.IdentValue;
import org.xhtmlrenderer.css.style.CssContext;
import org.xhtmlrenderer.layout.BoxCollector;
import org.xhtmlrenderer.layout.InlinePaintable;
import org.xhtmlrenderer.layout.Layer;
import org.xhtmlrenderer.layout.LayoutContext;
import org.xhtmlrenderer.util.XRLog;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Shape;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;


/**
 * Description of the Class
 *
 * @author empty
 */
public class LineBox extends Box implements Renderable, InlinePaintable {

    public int renderIndex;
    
    private boolean containsContent;
    private boolean containsBlockLevelContent;
    
    private FloatDistances floatDistances;
    
    private TextDecoration textDecoration;
    
    private int paintingTop;
    private int paintingHeight;
    
    private List nonFlowContent;

    /**
     * Constructor for the LineBox object
     */
    public LineBox() {
    }

    /**
     * Converts to a String representation of the object.
     *
     * @return A string representation of the object.
     */
    public String toString() {

        return "Line: (" + x + "," + y + ")x(" + getWidth() + "," + height + ")";
    }

    public int getIndex() {
        return renderIndex;
    }

    public double getAbsTop() {
        return getAbsY();
    }

    public double getAbsBottom() {
        return getAbsY() + height;
    }

    public Rectangle getBounds(CssContext cssCtx, int tx, int ty) {
        Rectangle result = new Rectangle(x, y, contentWidth, height);
        result.translate(tx, ty);
        return result;
    }
    
    private void paintTextDecoration(RenderingContext c) {
        Graphics graphics = c.getGraphics();
        
        Color oldColor = graphics.getColor();
        
        graphics.setColor(getStyle().getCalculatedStyle().getColor());
        Box parent = getParent();
        if (parent.getStyle().getCalculatedStyle().isIdent(
                CSSName.FS_TEXT_DECORATION_EXTENT, IdentValue.BLOCK)) {
            c.getGraphics().fillRect(
                    getAbsX(), 
                    getAbsY() + textDecoration.getOffset(),
                    parent.getAbsX() + parent.tx + parent.getContentWidth() - getAbsX(), 
                    textDecoration.getThickness());
        } else {
            c.getGraphics().fillRect(
                    getAbsX(), getAbsY() + textDecoration.getOffset(),
                    getContentWidth(),
                    textDecoration.getThickness());
        }
        
        graphics.setColor(oldColor);
    }
    
    public void paintInline(RenderingContext c) {
        if (! getParent().getStyle().isVisible()) {
            return;
        }
        
        if (textDecoration != null) {
            paintTextDecoration(c);
        }
        
        if (c.debugDrawLineBoxes()) {
            Color oldColor = c.getGraphics().getColor();
            c.getGraphics().setColor(Color.GREEN);
            c.getGraphics().drawRect(getAbsX(), getAbsY(), getWidth(), getHeight());
            c.getGraphics().setColor(oldColor);
        }
    }
    
    public boolean isFirstLine() {
        Box parent = getParent();
        return parent != null && parent.getChildCount() > 0 && parent.getChild(0) == this;
    }
    
    public void prunePendingInlineBoxes() {
        if (getChildCount() > 0) {
            for (int i = getChildCount() - 1; i >= 0; i--) {
                Box b = (Box)getChild(i);
                if (! (b instanceof InlineBox)) {
                    break;
                }
                InlineBox iB = (InlineBox)b;
                iB.prunePending();
                if (iB.isPending()) {
                    removeChild(i);
                }
            }
        }
    }

    public boolean isContainsContent() {
        return containsContent;
    }

    public void setContainsContent(boolean containsContent) {
        this.containsContent = containsContent;
    }
    
    public void align() {
    	if (getFloatDistances() == null) {
    		// Shouldn't happen (but currently can with nested tables)
    		XRLog.layout(Level.WARNING, "Float distances not available. Cannot align.");
    		return;
    	}
    	
        IdentValue align = getParent().getStyle().getCalculatedStyle().getIdent(CSSName.TEXT_ALIGN);
        
        // TODO implement text-align: justify
        
        int current = this.x;
        
        if (align == IdentValue.LEFT || align == IdentValue.JUSTIFY) {
            int floatDistance = getFloatDistances().getLeftFloatDistance();
            this.x += floatDistance;
        } else if (align == IdentValue.CENTER) {
            int leftFloatDistance = getFloatDistances().getLeftFloatDistance();
            int rightFloatDistance = getFloatDistances().getRightFloatDistance();
            
            int midpoint = leftFloatDistance +
                (getParent().getContentWidth() - leftFloatDistance - rightFloatDistance) / 2;
            
            this.x += midpoint - getContentWidth() / 2;
        } else if (align == IdentValue.RIGHT) {
            int floatDistance = getFloatDistances().getRightFloatDistance();
            this.x += getParent().getContentWidth() - floatDistance - getContentWidth();
        }
        
        if (current != this.x) {
            calcCanvasLocation();
            calcChildLocations();
        }
    }
    
	public FloatDistances getFloatDistances() {
		return floatDistances;
	}

	public void setFloatDistances(FloatDistances floatDistances) {
		this.floatDistances = floatDistances;
	}

    public boolean isContainsBlockLevelContent() {
        return containsBlockLevelContent;
    }

    public void setContainsBlockLevelContent(boolean containsBlockLevelContent) {
        this.containsBlockLevelContent = containsBlockLevelContent;
    }
    
    public boolean intersects(CssContext cssCtx, Shape clip) {
        return intersectsLine(cssCtx, clip) || 
            (isContainsBlockLevelContent() && intersectsInlineBlocks(cssCtx, clip));
    }
    
    private boolean intersectsLine(CssContext cssCtx, Shape clip) {
        Rectangle result = new Rectangle(
                getAbsX(), getAbsY() + paintingTop, contentWidth, paintingHeight);
        return clip.intersects(result);
    }
    
    private boolean intersectsInlineBlocks(CssContext cssCtx, Shape clip) {
        for (int i = 0; i < getChildCount(); i++) {
            Box child = (Box)getChild(i);
            if (child instanceof InlineBox) {
                boolean possibleResult = ((InlineBox)child).intersectsInlineBlocks(
                        cssCtx, clip);
                if (possibleResult) {
                    return true;
                }
            } else {
                BoxCollector collector = new BoxCollector();
                if (collector.intersectsAny(cssCtx, clip, child)) {
                    return true;
                }
            }
        }
        
        return false;
    }

    public TextDecoration getTextDecoration() {
        return textDecoration;
    }

    public void setTextDecoration(TextDecoration textDecoration) {
        this.textDecoration = textDecoration;
    }

    public int getPaintingHeight() {
        return paintingHeight;
    }

    public void setPaintingHeight(int paintingHeight) {
        this.paintingHeight = paintingHeight;
    }

    public int getPaintingTop() {
        return paintingTop;
    }

    public void setPaintingTop(int paintingTop) {
        this.paintingTop = paintingTop;
    }
    
    
    public void addAllChildren(List list, Layer layer) {
        for (int i = 0; i < getChildCount(); i++) {
            Box child = getChild(i);
            if (getContainingLayer() == layer) {
                list.add(child);
                if (child instanceof InlineBox) {
                    ((InlineBox)child).addAllChildren(list, layer);
                }
            }
        }
    }
    
    public List getNonFlowContent() {
        return nonFlowContent == null ? Collections.EMPTY_LIST : nonFlowContent;
    }
    
    public void addNonFlowContent(BlockBox box) {
        if (nonFlowContent == null) {
            nonFlowContent = new ArrayList();
        }
        
        nonFlowContent.add(box);
    }
    
    public void detach() {
        for (int i = 0; i < getNonFlowContent().size(); i++) {
            Box content = (Box)getNonFlowContent().get(i);
            content.detach();
        }
        super.detach();
    }
    
    public void calcCanvasLocation() {
        Box parent = getParent();
        if (parent == null) {
            XRLog.layout(Level.WARNING, "calcCanvasLocation() called with no parent");
        }
        setAbsX(parent.getAbsX() + parent.tx + this.x);
        setAbsY(parent.getAbsY() + parent.ty + this.y);        
    }
    
    public void calcChildLocations() {
        super.calcChildLocations();
        
        // Update absolute boxes too.  Not necessary most of the time, but
        // it doesn't hurt (revisit this)
        for (int i = 0; i < getNonFlowContent().size(); i++) {
            Box content = (Box)getNonFlowContent().get(i);
            if (content.getStyle().isAbsolute()) {
                content.calcCanvasLocation();
                content.calcChildLocations();
            }
        }
    }
    
    public boolean crossesPageBreak(LayoutContext c) {
        PageBox pageBox = c.getRootLayer().getFirstPage(c, this);
        return pageBox == null || getAbsY() + getHeight() >= pageBox.getBottom();
    }
}

/*
 * $Id$
 *
 * $Log$
 * Revision 1.45  2006/01/01 02:38:18  peterbrant
 * Merge more pagination work / Various minor cleanups
 *
 * Revision 1.44  2005/12/21 02:36:28  peterbrant
 * - Calculate absolute positions incrementally (prep work for pagination)
 * - Light cleanup
 * - Fix bug where floats nested in floats could cause the outer float to be positioned in the wrong place
 *
 * Revision 1.43  2005/12/15 20:04:47  peterbrant
 * Implement visibility: hidden
 *
 * Revision 1.42  2005/12/14 15:03:12  peterbrant
 * Revert ill-advised text-decoration change
 *
 * Revision 1.41  2005/12/13 20:46:04  peterbrant
 * Improve list support (implement list-style-position: inside, marker "sticks" to first line box even if there are other block boxes in between, plus other minor fixes) / Experimental support for optionally extending text decorations to box edge vs line edge
 *
 * Revision 1.40  2005/12/13 02:41:32  peterbrant
 * Initial implementation of vertical-align: top/bottom (not done yet) / Minor cleanup and optimization
 *
 * Revision 1.39  2005/12/09 21:41:18  peterbrant
 * Finish support for relative inline layers
 *
 * Revision 1.38  2005/12/09 01:24:55  peterbrant
 * Initial commit of relative inline layers
 *
 * Revision 1.37  2005/12/07 20:34:45  peterbrant
 * Remove unused fields/methods from RenderingContext / Paint line content using absolute coords (preparation for relative inline layers)
 *
 * Revision 1.36  2005/11/29 16:39:04  peterbrant
 * Complete line box clip region checking
 *
 * Revision 1.35  2005/11/29 15:26:16  peterbrant
 * Implement text-decoration
 *
 * Revision 1.34  2005/11/29 03:12:25  peterbrant
 * Fix clip region checking when a line contains an inline-block
 *
 * Revision 1.33  2005/11/29 02:37:23  peterbrant
 * Make clear work again / Rip out old pagination code
 *
 * Revision 1.32  2005/11/25 22:42:05  peterbrant
 * Wait until table has completed layout before doing line alignment
 *
 * Revision 1.31  2005/11/25 16:57:17  peterbrant
 * Initial commit of inline content refactoring
 *
 * Revision 1.30  2005/11/12 21:55:27  tobega
 * Inline enhancements: block box text decorations, correct line-height when it is a number, better first-letter handling
 *
 * Revision 1.29  2005/11/11 16:45:29  tobega
 * Fixed vertical align calculations to use line-height properly
 *
 * Revision 1.28  2005/11/09 22:33:18  tobega
 * fixed handling of first-line-style
 *
 * Revision 1.27  2005/11/08 20:03:56  peterbrant
 * Further progress on painting order / improved positioning implementation
 *
 * Revision 1.26  2005/11/07 00:07:35  tobega
 * Got text-decoration and relative inlines kind-of working
 *
 * Revision 1.25  2005/11/04 02:43:11  tobega
 * Inline borders and backgrounds are back!
 *
 * Revision 1.24  2005/11/03 17:58:40  peterbrant
 * Float rewrite (still stomping bugs, but demos work)
 *
 * Revision 1.23  2005/10/29 22:31:02  tobega
 * House-cleaning
 *
 * Revision 1.22  2005/10/27 00:09:04  tobega
 * Sorted out Context into RenderingContext and LayoutContext
 *
 * Revision 1.21  2005/10/16 23:57:17  tobega
 * Starting experiment with flat representation of render tree
 *
 * Revision 1.20  2005/10/12 21:17:14  tobega
 * patch from Peter Brant
 *
 * Revision 1.19  2005/10/08 17:40:21  tobega
 * Patch from Peter Brant
 *
 * Revision 1.18  2005/10/06 03:20:23  tobega
 * Prettier incremental rendering. Ran into more trouble than expected and some creepy crawlies and a few pages don't look right (forms.xhtml, splash.xhtml)
 *
 * Revision 1.17  2005/08/06 22:12:24  tobega
 * Fixed issue 110
 *
 * Revision 1.16  2005/07/14 22:25:17  joshy
 * major updates to float code. should fix *most* issues.
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.15  2005/05/09 23:47:15  tobega
 * Cleaned up some getting of LineMetrics and optimized InlineRendering
 *
 * Revision 1.14  2005/05/08 13:02:41  tobega
 * Fixed a bug whereby styles could get lost for inline elements, notably if root element was inline. Did a few other things which probably has no importance at this moment, e.g. refactored out some unused stuff.
 *
 * Revision 1.13  2005/01/29 20:21:04  pdoubleya
 * Clean/reformat code. Removed commented blocks, checked copyright.
 *
 * Revision 1.12  2005/01/24 22:46:42  pdoubleya
 * Added support for ident-checks using IdentValue instead of string comparisons.
 *
 * Revision 1.11  2005/01/10 01:58:37  tobega
 * Simplified (and hopefully improved) handling of vertical-align. Added support for line-height. As always, provoked a few bugs in the process.
 *
 * Revision 1.10  2005/01/09 13:32:35  tobega
 * Caching image components. Also fixed two bugs that were introduced fixing the last one. Code still too brittle...
 *
 * Revision 1.9  2005/01/09 00:29:28  tobega
 * Removed XPath usages from core classes. Also happened to find and fix a layout-bug that I introduced a while ago.
 *
 * Revision 1.8  2005/01/06 09:49:38  tobega
 * More cleanup, aiming to remove Content reference in box
 *
 * Revision 1.7  2005/01/05 01:10:16  tobega
 * Went wild with code analysis tool. removed unused stuff. Lucky we have CVS...
 *
 * Revision 1.6  2004/12/29 10:39:35  tobega
 * Separated current state Context into LayoutContext and the rest into SharedContext.
 *
 * Revision 1.5  2004/12/15 00:53:40  tobega
 * Started playing a bit with inline box, provoked a few nasties, probably created some, seems to work now
 *
 * Revision 1.4  2004/12/12 03:33:01  tobega
 * Renamed x and u to avoid confusing IDE. But that got cvs in a twist. See if this does it
 *
 * Revision 1.3  2004/12/10 06:51:05  tobega
 * Shamefully, I must now check in painfully broken code. Good news is that Layout is much nicer, and we also handle :before and :after, and do :first-line better than before. Table stuff must be brought into line, but most needed is to fix Render. IMO Render should work with Boxes and Content. If Render goes for a node, that is wrong.
 *
 * Revision 1.2  2004/10/23 13:50:27  pdoubleya
 * Re-formatted using JavaStyle tool.
 * Cleaned imports to resolve wildcards except for common packages (java.io, java.util, etc).
 * Added CVS log comments at bottom.
 *
 *
 */

