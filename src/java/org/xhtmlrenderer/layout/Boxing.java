/*
 * {{{ header & license
 * Copyright (c) 2004, 2005 Joshua Marinacci, Torbj�rn Gannholm
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 * }}}
 */
package org.xhtmlrenderer.layout;

import java.awt.Rectangle;
import java.util.List;

import org.w3c.dom.Element;
import org.xhtmlrenderer.css.constants.CSSName;
import org.xhtmlrenderer.css.style.CalculatedStyle;
import org.xhtmlrenderer.css.style.derived.BorderPropertySet;
import org.xhtmlrenderer.css.style.derived.RectPropertySet;
import org.xhtmlrenderer.extend.ReplacedElement;
import org.xhtmlrenderer.layout.content.AnonymousBlockContent;
import org.xhtmlrenderer.layout.content.Content;
import org.xhtmlrenderer.layout.content.FirstLetterStyle;
import org.xhtmlrenderer.layout.content.FirstLineStyle;
import org.xhtmlrenderer.layout.content.TableContent;
import org.xhtmlrenderer.render.AnonymousBlockBox;
import org.xhtmlrenderer.render.BlockBox;
import org.xhtmlrenderer.render.Box;
import org.xhtmlrenderer.render.StrutMetrics;
import org.xhtmlrenderer.table.TableBoxing;
import org.xhtmlrenderer.util.XRRuntimeException;


public class Boxing {
    private Boxing() {
    }
    
    /**
     * @deprecated
     */
    public static BlockBox constructBox(LayoutContext c, Content content) {
        BlockBox block = null;
        if (content instanceof AnonymousBlockContent) {
            throw new XRRuntimeException("API isn't there any more! Check me!");
        } else if (content instanceof TableContent) {
            //tables have an anonymous outer box that nevertheless represents the table
            block = TableBoxing.createBox(c, content);
        } else {
            block = new BlockBox();
            block.element = content.getElement();
            //block.width = (int) c.getExtents().getWidth();
        }
        Element element = content.getElement();
        if (element != null) {
            String id = c.getNamespaceHandler().getID(element);
            if (id != null && !id.equals("")) {
                c.addIDBox(id, block);
            }
        }
        return block;
    }
    
    /**
     * @deprecated
     */
    public static Box layout(LayoutContext c, BlockBox block, Content content) {
        return layout(c, block, content, null);
    }

    /**
     * @deprecated
     */
    public static Box layout(LayoutContext c, BlockBox block, Content content, 
            StyleSetListener listener) {
        if (content instanceof AnonymousBlockContent) {
            return AnonymousBoxing.layout(c, block);
        } else if (content instanceof TableContent) {
            return TableBoxing.layout(c, (BlockBox) block, content, listener);
        } else
            return layoutBlock(c, block);
    }
    
    public static void layout(LayoutContext c, BlockBox block) {
        layoutBlock(c, block);
    }
    
    private static Box layoutBlock(LayoutContext c, BlockBox block) {
        Rectangle oe = c.getExtents();

        CalculatedStyle style = block.getStyle();
        
        block.setContainingBlockWidth((int)oe.getWidth());
        
        if (style.isFixedBackground()) {
            c.getRootLayer().setFixedBackground(true);
        }
        
        boolean pushedLayer = false;
        if (block.isRoot() || block.getStyle().requiresLayer()) {
            pushedLayer = true;
            if (block.getLayer() == null) {
                c.pushLayer(block);
                if (c.isPrint() && block.isRoot()) {
                    c.getLayer().addPage(c);
                }
            } else {
                c.pushLayer(block.getLayer());
            }
        }

        if (block.isRoot() || block.getStyle().establishesBFC()) {
            BlockFormattingContext bfc = new BlockFormattingContext(block, c);
            c.pushBFC(bfc);
        }
        
        // copy the extents
        c.setExtents(new Rectangle(oe));

        /*
        VerticalMarginCollapser.collapseVerticalMargins(c, block, content, (float) oe.getWidth());
        */
        
        if (block.isResetMargins()) {
            block.resetCollapsedMargin();
            block.setResetMargins(false);
        }

        BorderPropertySet border = style.getBorder(c);
        //note: percentages here refer to width of containing block
        RectPropertySet margin = block.getMarginWidth(c);
        RectPropertySet padding = style.getPaddingRect((float) oe.getWidth(), (float) oe.getWidth(), c);
        // CLEAN: cast to int
        block.leftMBP = (int) margin.left() + (int) border.left() + (int) padding.left();
        block.rightMBP = (int) padding.right() + (int) border.right() + (int) margin.right();
        block.contentWidth = (int) (c.getExtents().getWidth() - block.leftMBP - block.rightMBP);

        // calculate the width and height as much as possible
        if (!(block instanceof AnonymousBlockBox)) {
            int setHeight = -1;//means height is not set by css
            int setWidth = -1;//means width is not set by css
            if (!block.getStyle().isAutoWidth()) {
                setWidth = (int) style.getFloatPropertyProportionalWidth(CSSName.WIDTH, c.getExtents().width, c);
                block.contentWidth = setWidth;
                c.getExtents().width = block.getWidth();
            }
            if (!block.getStyle().isAutoHeight()) {
                setHeight = (int) style.getFloatPropertyProportionalHeight(CSSName.HEIGHT, c.getExtents().height, c);
                // CLEAN: cast to int
                c.getExtents().height = (int) margin.top() + (int) border.top() + (int) padding.top() +
                        setHeight + (int) padding.bottom() + (int) border.bottom() + (int) margin.bottom();
                block.height = setHeight;
            }
            //check if replaced
            ReplacedElement re = c.getReplacedElementFactory().createReplacedElement(
                    c, block, c.getUac(), setWidth, setHeight);
            if (re != null) {
                block.contentWidth = re.getIntrinsicWidth();
                block.height = re.getIntrinsicHeight();
                block.setReplacedElement(re);
            }
        }
        
        // save height incase fixed height
        int original_height = block.height;

        if (! block.isReplaced()) {
            block.height = 0;
        }
        
        boolean didSetMarkerData = false;
        if (block instanceof BlockBox && block.getStyle().isListItem()) {
            StrutMetrics strutMetrics = InlineBoxing.createDefaultStrutMetrics(c, block);
            ((BlockBox)block).createMarkerData(c, strutMetrics);
            c.setCurrentMarkerData(((BlockBox)block).getMarkerData());
            didSetMarkerData = true;
        }        

        // do children's layout

        // CLEAN: cast to int
        int tx = (int) margin.left() + (int) border.left() + (int) padding.left();
        int ty = (int) margin.top() + (int) border.top() + (int) padding.top();
        block.tx = tx;
        block.ty = ty;
        c.translate(tx, ty);
        // CLEAN: cast to int
        Rectangle extents = c.shrinkExtents(
                tx + (int) margin.right() + (int) border.right() + (int) padding.right(), ty + (int) margin.bottom() + (int) border.bottom() + (int) padding.bottom());
        if (! block.isReplaced())
            block.layoutChildren(c);
        else {
            block.setState(Box.DONE);
        }
        c.setExtents(extents);
        c.translate(-tx, -ty);

        // restore height incase fixed height
        if (!block.getStyle().isAutoHeight()) {
            // Uu.p("restoring original height");
            block.height = original_height;
        }

        if (block.isRoot() || block.getStyle().establishesBFC()) {
            if (block.getStyle().isAutoHeight()) {
                int delta = 
                    c.getBlockFormattingContext().getFloatManager().getClearDelta(
                            c, (int) margin.top() + (int) border.top() + (int) padding.top() + block.height);
                if (delta > 0) {
                    block.height += delta ;
                }
            }
            c.popBFC();
        }
        
        if (didSetMarkerData) {
            c.setCurrentMarkerData(null);
        }

        // calculate the total outer width
        //block.contentWidth = block.getWidth();
        //block.width = margin.left + border.left + padding.left + block.contentWidth + padding.right + border.right + margin.right;
        // CLEAN: cast to int
        block.height = (int) margin.top() + (int) border.top() + (int) padding.top() + block.height + (int) padding.bottom() + (int) border.bottom() + (int) margin.bottom();
        
        if (pushedLayer) {
            c.popLayer();
        }

        //restore the extents
        c.setExtents(oe);
        
        // Uu.p("BoxLayout: finished with block: " + block);
        return block;
    }

    public static PseudoClassPushStatus pushPseudoClasses(LayoutContext c, List contentList) {
        PseudoClassPushStatus status = new PseudoClassPushStatus();
        
        Object first = contentList.get(0);
        Object second = contentList.size() > 1 ? contentList.get(1) : null;

        if (first instanceof FirstLineStyle) {
            c.getFirstLinesTracker().addStyle(((FirstLineStyle) first).getStyle());
            status.setPushedFirstLine(true);
            if (second instanceof FirstLetterStyle) {
                c.getFirstLettersTracker().addStyle(((FirstLetterStyle) second).getStyle());
                status.setPushedFirstLetter(true);
            }
        } else if (first instanceof FirstLetterStyle) {
            c.getFirstLettersTracker().addStyle(((FirstLetterStyle) first).getStyle());
            status.setPushedFirstLetter(true);
        }

        return status;
    }    
    
    private static class PseudoClassPushStatus {
        private boolean pushedFirstLine;
        private boolean pushedFirstLetter;
        
        public boolean isPushedFirstLetter() {
            return pushedFirstLetter;
        }
        public void setPushedFirstLetter(boolean pushedFirstLetter) {
            this.pushedFirstLetter = pushedFirstLetter;
        }
        public boolean isPushedFirstLine() {
            return pushedFirstLine;
        }
        public void setPushedFirstLine(boolean pushedFirstLine) {
            this.pushedFirstLine = pushedFirstLine;
        }
    }
    
    /**
     * @deprecated
     */
    public interface StyleSetListener {
        public void onStyleSet(BlockBox box);
    }
}

/*
 * $Id$
 *
 * $Log$
 * Revision 1.79  2006/08/29 17:29:10  peterbrant
 * Make Style object a thing of the past
 *
 * Revision 1.78  2006/08/27 00:35:56  peterbrant
 * Initial commit of (initial) R7 work
 *
 * Revision 1.77  2006/03/01 00:40:54  peterbrant
 * Remove reference to SwingReplacedElement / Adjust to detach() API change
 *
 * Revision 1.76  2006/02/22 02:20:18  peterbrant
 * Links and hover work again
 *
 * Revision 1.75  2006/01/27 01:15:30  peterbrant
 * Start on better support for different output devices
 *
 * Revision 1.74  2006/01/09 23:23:16  peterbrant
 * Fix layers nested inside an "alternate" layer
 *
 * Revision 1.73  2006/01/03 23:55:56  peterbrant
 * Add support for proper page breaking of floats / More bug fixes to pagination support
 *
 * Revision 1.72  2006/01/02 20:59:08  peterbrant
 * Implement page-break-before/after: avoid
 *
 * Revision 1.71  2006/01/01 02:38:15  peterbrant
 * Merge more pagination work / Various minor cleanups
 *
 * Revision 1.70  2005/12/30 01:32:36  peterbrant
 * First merge of parts of pagination work
 *
 * Revision 1.69  2005/12/28 00:50:51  peterbrant
 * Continue ripping out first try at pagination / Minor method name refactoring
 *
 * Revision 1.68  2005/12/21 02:36:27  peterbrant
 * - Calculate absolute positions incrementally (prep work for pagination)
 * - Light cleanup
 * - Fix bug where floats nested in floats could cause the outer float to be positioned in the wrong place
 *
 * Revision 1.67  2005/12/17 02:24:10  peterbrant
 * Remove last pieces of old (now non-working) clip region checking / Push down handful of fields from Box to BlockBox
 *
 * Revision 1.66  2005/12/13 20:46:08  peterbrant
 * Improve list support (implement list-style-position: inside, marker "sticks" to first line box even if there are other block boxes in between, plus other minor fixes) / Experimental support for optionally extending text decorations to box edge vs line edge
 *
 * Revision 1.65  2005/12/11 02:50:24  peterbrant
 * Minor cleanup / Don't pop layer before Box.height is set
 *
 * Revision 1.64  2005/12/07 03:14:21  peterbrant
 * Fixes to final float position when float BFC is not contained in the layer being positioned / Implement 10.6.7 of the spec
 *
 * Revision 1.63  2005/12/07 00:33:12  peterbrant
 * :first-letter and :first-line works again
 *
 * Revision 1.62  2005/12/05 00:13:54  peterbrant
 * Improve list-item support (marker positioning is now correct) / Start support for relative inline layers
 *
 * Revision 1.61  2005/11/29 02:37:25  peterbrant
 * Make clear work again / Rip out old pagination code
 *
 * Revision 1.60  2005/11/28 22:26:04  peterbrant
 * Comment out noisy debug logging
 *
 * Revision 1.59  2005/11/25 16:57:14  peterbrant
 * Initial commit of inline content refactoring
 *
 * Revision 1.58  2005/11/10 01:55:18  peterbrant
 * Further progress on layer work
 *
 * Revision 1.57  2005/11/05 23:19:08  peterbrant
 * Always add fixed layers to root layer / If element has fixed background just
 * note this on the root layer instead of property in Box
 *
 * Revision 1.56  2005/11/05 18:45:05  peterbrant
 * General cleanup / Remove obsolete code
 *
 * Revision 1.55  2005/11/05 03:29:31  peterbrant
 * Start work on painting order and improved positioning implementation
 *
 * Revision 1.54  2005/11/04 02:43:07  tobega
 * Inline borders and backgrounds are back!
 *
 * Revision 1.53  2005/11/03 17:58:16  peterbrant
 * Float rewrite (still stomping bugs, but demos work)
 *
 * Revision 1.52  2005/11/01 23:49:23  tobega
 * Pulled floats and absolutes out of the "normal" rendering
 *
 * Revision 1.51  2005/10/30 00:02:35  peterbrant
 * - Minor cleanup to get rid of unused CssContext in Style constructor
 * - Switch to ArrayList from LinkedList in a few places (saves several MBs of memory on Hamlet)
 * - Introduce ScaledLineMetrics to work around apparent Java bug
 *
 * Revision 1.50  2005/10/27 00:08:58  tobega
 * Sorted out Context into RenderingContext and LayoutContext
 *
 * Revision 1.49  2005/10/23 22:16:41  tobega
 * Preparation for StackingContext rendering
 *
 * Revision 1.48  2005/10/22 23:00:30  peterbrant
 * Fix memory leak (all box trees ever built remained in memory)
 *
 * Revision 1.47  2005/10/21 18:10:51  pdoubleya
 * Support for cachable borders. Still buggy on some pages, but getting there.
 *
 * Revision 1.46  2005/10/21 13:17:12  pdoubleya
 * Rename some methods in RectPropertySet, cleanup.
 *
 * Revision 1.45  2005/10/21 13:02:22  pdoubleya
 * Changed to cache padding in RectPropertySet.
 *
 * Revision 1.44  2005/10/21 12:01:16  pdoubleya
 * Added cachable rect property for margin, cleanup minor in styling.
 *
 * Revision 1.43  2005/10/21 05:52:10  tobega
 * A little more experimenting with flattened render tree
 *
 * Revision 1.42  2005/10/20 20:47:59  pdoubleya
 * Updates for refactoring to style classes. CalculatedStyle now has lookup methods to cover all general cases, so propertyByName() is private, which means the backing classes for styling were able to be replaced.
 *
 * Revision 1.41  2005/10/18 20:57:02  tobega
 * Patch from Peter Brant
 *
 * Revision 1.40  2005/10/15 23:39:17  tobega
 * patch from Peter Brant
 *
 * Revision 1.39  2005/10/12 21:17:12  tobega
 * patch from Peter Brant
 *
 * Revision 1.38  2005/10/08 17:40:20  tobega
 * Patch from Peter Brant
 *
 * Revision 1.37  2005/10/06 03:20:21  tobega
 * Prettier incremental rendering. Ran into more trouble than expected and some creepy crawlies and a few pages don't look right (forms.xhtml, splash.xhtml)
 *
 * Revision 1.36  2005/10/02 21:29:58  tobega
 * Fixed a lot of concurrency (and other) issues from incremental rendering. Also some house-cleaning.
 *
 * Revision 1.35  2005/09/30 04:58:04  joshy
 * fixed garbage when showing a document with a fixed positioned block
 *
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.34  2005/09/29 21:34:02  joshy
 * minor updates to a lot of files. pulling in more incremental rendering code.
 * fixed another resize bug
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.33  2005/09/29 06:15:05  tobega
 * Patch from Peter Brant:
 * List of changes:
 *  - Fix extents height calculation
 *  - Small refactoring to Boxing to combine a method
 *  - Make render and layout threads interruptible and add
 * RootPanel.shutdown() method to shut them down in an orderly manner
 *  - Fix NPE in Graphics2DRenderer.  It looks like
 * BasicPanel.intrinsic_size will always be null anyway?
 *  - Fix NPE in RootPanel when enclosingScrollPane is null.
 *  - Both RenderLoop.collapseRepaintEvents and
 * LayoutLoop.collapseLayoutEvents will go into an infinite loop if the
 * next event isn't collapsible.  I added a common implementation to
 * RenderQueue which doesn't have this problem.
 *
 * Revision 1.32  2005/09/27 23:48:39  joshy
 * first merge of basicpanel reworking and incremental layout. more to come.
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.31  2005/09/27 06:03:00  tobega
 * Patch from Peter Brant for specified width
 *
 * Revision 1.30  2005/09/26 22:40:19  tobega
 * Applied patch from Peter Brant concerning margin collapsing
 *
 * Revision 1.29  2005/09/20 11:28:12  tobega
 * Knowledge of how to extract IDs belongs in the NamespaceHandler
 *
 * Revision 1.28  2005/07/04 01:58:30  joshy
 * removed debuggintg
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.27  2005/07/04 00:12:12  tobega
 * text-align now works for table-cells too (is done in render, not in layout)
 *
 * Revision 1.26  2005/07/02 07:26:59  joshy
 * better support for jumping to anchor tags
 * also some testing for the resize issue
 * need to investigate making the history remember document position.
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.25  2005/06/22 23:48:44  tobega
 * Refactored the css package to allow a clean separation from the core.
 *
 * Revision 1.24  2005/06/19 23:31:32  joshy
 * stop layout support
 * clear bug fixes
 * mouse busy cursor support
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.23  2005/06/16 18:34:10  joshy
 * support for clear:right
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.22  2005/06/16 07:24:50  tobega
 * Fixed background image bug.
 * Caching images in browser.
 * Enhanced LinkListener.
 * Some house-cleaning, playing with Idea's code inspection utility.
 *
 * Revision 1.21  2005/06/16 04:38:15  joshy
 * finished support for clear
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.20  2005/06/04 13:28:33  tobega
 * better??
 *
 * Revision 1.19  2005/06/01 21:36:39  tobega
 * Got image scaling working, and did some refactoring along the way
 *
 * Revision 1.18  2005/06/01 00:47:04  tobega
 * Partly confused hack trying to get width and height working properly for replaced elements.
 *
 * Revision 1.17  2005/05/31 01:40:05  tobega
 * Replaced elements can now be display: block;
 * display: inline-block; should be working even for non-replaced elements.
 *
 * Revision 1.16  2005/05/13 15:23:54  tobega
 * Done refactoring box borders, margin and padding. Hover is working again.
 *
 * Revision 1.15  2005/05/13 11:49:58  tobega
 * Started to fix up borders on inlines. Got caught up in refactoring.
 * Boxes shouldn't cache borders and stuff unless necessary. Started to remove unnecessary references.
 * Hover is not working completely well now, might get better when I'm done.
 *
 * Revision 1.14  2005/05/09 20:11:29  tobega
 * Improved the bfc hack for top level document
 *
 * Revision 1.13  2005/05/09 19:35:13  tobega
 * Fixed a logic error with ems and exs
 *
 * Revision 1.12  2005/05/08 14:36:57  tobega
 * Refactored away the need for having a context in a CalculatedStyle
 *
 * Revision 1.11  2005/04/21 18:16:06  tobega
 * Improved handling of inline padding. Also fixed first-line handling according to spec.
 *
 * Revision 1.10  2005/01/29 20:22:23  pdoubleya
 * Clean/reformat code. Removed commented blocks, checked copyright.
 *
 * Revision 1.9  2005/01/24 22:46:43  pdoubleya
 * Added support for ident-checks using IdentValue instead of string comparisons.
 *
 * Revision 1.8  2005/01/24 19:01:04  pdoubleya
 * Mass checkin. Changed to use references to CSSName, which now has a Singleton instance for each property, everywhere property names were being used before. Removed commented code. Cascaded and Calculated style now store properties in arrays rather than maps, for optimization.
 *
 * Revision 1.7  2005/01/24 14:36:32  pdoubleya
 * Mass commit, includes: updated for changes to property declaration instantiation, and new use of DerivedValue. Removed any references to older XR... classes (e.g. XRProperty). Cleaned imports.
 *
 * Revision 1.6  2005/01/09 15:22:48  tobega
 * Prepared improved handling of margins, borders and padding.
 *
 * Revision 1.5  2005/01/07 00:29:28  tobega
 * Removed Content reference from Box (mainly to reduce memory footprint). In the process stumbled over and cleaned up some messy stuff.
 *
 * Revision 1.4  2005/01/06 00:58:41  tobega
 * Cleanup of code. Aiming to get rid of references to Content in boxes
 *
 * Revision 1.3  2005/01/05 01:10:14  tobega
 * Went wild with code analysis tool. removed unused stuff. Lucky we have CVS...
 *
 * Revision 1.2  2005/01/02 12:22:16  tobega
 * Cleaned out old layout code
 *
 * Revision 1.1  2005/01/02 09:32:41  tobega
 * Now using mostly static methods for layout
 *
 * Revision 1.61  2005/01/02 01:00:08  tobega
 * Started sketching in code for handling replaced elements in the NamespaceHandler
 *
 * Revision 1.60  2005/01/01 23:38:37  tobega
 * Cleaned out old rendering code
 *
 * Revision 1.59  2005/01/01 22:37:43  tobega
 * Started adding in the table support.
 *
 * Revision 1.58  2004/12/29 10:39:32  tobega
 * Separated current state Context into LayoutContext and the rest into SharedContext.
 *
 * Revision 1.57  2004/12/28 01:48:23  tobega
 * More cleaning. Magically, the financial report demo is starting to look reasonable, without any effort being put on it.
 *
 * Revision 1.56  2004/12/27 09:40:47  tobega
 * Moved more styling to render stage. Now inlines have backgrounds and borders again.
 *
 * Revision 1.55  2004/12/27 07:43:30  tobega
 * Cleaned out border from box, it can be gotten from current style. Is it maybe needed for dynamic stuff?
 *
 * Revision 1.54  2004/12/24 08:46:49  tobega
 * Starting to get some semblance of order concerning floats. Still needs more work.
 *
 * Revision 1.53  2004/12/20 23:25:31  tobega
 * Cleaned up handling of absolute boxes and went back to correct use of anonymous boxes in ContentUtil
 *
 * Revision 1.52  2004/12/16 17:22:25  joshy
 * minor code cleanup
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.51  2004/12/16 17:10:41  joshy
 * fixed box bug
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.50  2004/12/16 15:53:08  joshy
 * fixes for absolute layout
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.49  2004/12/14 02:28:47  joshy
 * removed some comments
 * some bugs with the backgrounds still
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.48  2004/12/14 01:56:22  joshy
 * fixed layout width bugs
 * fixed extra border on document bug
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.47  2004/12/13 15:15:56  joshy
 * fixed bug where inlines would pick up parent styles when they aren't supposed to
 * fixed extra Xx's in printed text
 * added conf boolean to turn on box outlines
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.46  2004/12/13 01:29:40  tobega
 * Got the scrollbars back (by accident), and now we should be able to display DocumentFragments as well as Documents, if someone finds that useful.
 *
 * Revision 1.45  2004/12/12 18:06:51  tobega
 * Made simple layout (inline and box) a bit easier to understand
 *
 * Revision 1.44  2004/12/12 05:51:48  tobega
 * Now things run. But there is a lot to do before it looks as nice as it did. At least we now have :before and :after content and handling of breaks by css.
 *
 * Revision 1.43  2004/12/12 04:18:56  tobega
 * Now the core compiles at least. Now we must make it work right. Table layout is one point that really needs to be looked over
 *
 * Revision 1.42  2004/12/12 03:32:58  tobega
 * Renamed x and u to avoid confusing IDE. But that got cvs in a twist. See if this does it
 *
 * Revision 1.41  2004/12/12 03:04:31  tobega
 * Making progress
 *
 * Revision 1.40  2004/12/11 23:36:48  tobega
 * Progressing on cleaning up layout and boxes. Still broken, won't even compile at the moment. Working hard to fix it, though.
 *
 * Revision 1.39  2004/12/11 21:14:48  tobega
 * Prepared for handling run-in content (OK, I know, a side-track). Still broken, won't even compile at the moment. Working hard to fix it, though.
 *
 * Revision 1.38  2004/12/11 18:18:10  tobega
 * Still broken, won't even compile at the moment. Working hard to fix it, though. Replace the StyleReference interface with our only concrete implementation, it was a bother changing in two places all the time.
 *
 * Revision 1.37  2004/12/10 06:51:02  tobega
 * Shamefully, I must now check in painfully broken code. Good news is that Layout is much nicer, and we also handle :before and :after, and do :first-line better than before. Table stuff must be brought into line, but most needed is to fix Render. IMO Render should work with Boxes and Content. If Render goes for a node, that is wrong.
 *
 * Revision 1.36  2004/12/09 21:18:52  tobega
 * precaution: code still works
 *
 * Revision 1.35  2004/12/09 00:11:51  tobega
 * Almost ready for Content-based inline generation.
 *
 * Revision 1.34  2004/12/08 00:42:34  tobega
 * More cleaning of use of Node, more preparation for Content-based inline generation. Also fixed 2 irritating bugs!
 *
 * Revision 1.33  2004/12/06 23:41:14  tobega
 * More cleaning of use of Node, more preparation for Content-based inline generation.
 *
 * Revision 1.32  2004/12/05 05:00:39  joshy
 * fixed bug that prevented explict box heights from working.
 *
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.31  2004/12/05 00:48:57  tobega
 * Cleaned up so that now all property-lookups use the CalculatedStyle. Also added support for relative values of top, left, width, etc.
 *
 * Revision 1.30  2004/12/01 01:57:00  joshy
 * more updates for float support.
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.29  2004/11/30 20:38:49  joshy
 * cleaned up the float and absolute interfaces a bit
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.28  2004/11/30 20:28:27  joshy
 * support for multiple floats on a single line.
 *
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.27  2004/11/27 15:46:38  joshy
 * lots of cleanup to make the code clearer
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.26  2004/11/18 19:10:04  joshy
 * added bottom support to absolute positioning
 *
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.25  2004/11/18 18:49:48  joshy
 * fixed the float issue.
 * commented out more dead code
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.24  2004/11/18 14:26:22  joshy
 * more code cleanup
 *
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.23  2004/11/18 02:51:14  joshy
 * moved more code out of the box into custom classes
 * added more preload logic to the default layout's preparebox method
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.21  2004/11/16 07:25:09  tobega
 * Renamed HTMLPanel to BasicPanel
 *
 * Revision 1.20  2004/11/15 15:20:38  joshy
 * fixes for absolute layout
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.19  2004/11/14 16:40:58  joshy
 * refactored layout factory
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.18  2004/11/12 18:51:00  joshy
 * fixed repainting issue for background-attachment: fixed
 * added static util methods and get minimum size to graphics 2d renderer
 * added test for graphics 2d renderer
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.17  2004/11/12 17:05:24  joshy
 * support for fixed positioning
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.16  2004/11/09 15:53:48  joshy
 * initial support for hover (currently disabled)
 * moved justification code into it's own class in a new subpackage for inline
 * layout (because it's so blooming complicated)
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.15  2004/11/08 20:50:58  joshy
 * improved float support
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.14  2004/11/08 15:10:10  joshy
 * added support for styling :first-letter inline boxes
 * updated the absolute positioning tests
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.13  2004/11/07 13:39:17  joshy
 * fixed missing borders on the table
 * changed td and th to display:table-cell
 * updated isBlockLayout() code to fix double border problem with tables
 *
 * -j
 *
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.12  2004/11/06 22:49:51  joshy
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
 * Revision 1.11  2004/11/05 18:45:14  joshy
 * support for floated blocks (not just inline blocks)
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.10  2004/11/04 15:35:44  joshy
 * initial float support
 * includes right and left float
 * cannot have more than one float per line per side
 * floats do not extend beyond enclosing block
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.9  2004/11/03 23:54:33  joshy
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
 * Revision 1.8  2004/11/03 15:17:04  joshy
 * added intial support for absolute positioning
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.7  2004/10/28 02:13:40  joshy
 * finished moving the painting code into the renderers
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.6  2004/10/28 01:34:23  joshy
 * moved more painting code into the renderers
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.5  2004/10/26 00:13:14  joshy
 * added threaded layout support to the BasicPanel
 *
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.4  2004/10/23 13:46:46  pdoubleya
 * Re-formatted using JavaStyle tool.
 * Cleaned imports to resolve wildcards except for common packages (java.io, java.util, etc).
 * Added CVS log comments at bottom.
 *
 *
 */


