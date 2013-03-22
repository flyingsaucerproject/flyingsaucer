/*
 * {{{ header & license
 * Copyright (c) 2004, 2005 Joshua Marinacci, Torbjoern Gannholm
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
package org.xhtmlrenderer.layout;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.RandomAccess;

import org.xhtmlrenderer.css.constants.CSSName;
import org.xhtmlrenderer.css.constants.IdentValue;
import org.xhtmlrenderer.render.BlockBox;
import org.xhtmlrenderer.render.Box;
import org.xhtmlrenderer.render.LineBox;
import org.xhtmlrenderer.render.PageBox;

/**
 * Utility class for laying block content.  It is called when a block box
 * contains block level content.  {@link BoxBuilder} will have made sure that
 * the block we're working on will either contain only inline or block content.
 * If we're in a paged media environment, the various page break related
 * properties are also handled here.  If a rule is violated, the affected run
 * of boxes will be layed out again.  If the rule still cannot be satisfied,
 * the rule will be dropped.
 */
public class BlockBoxing {
    private static final int NO_PAGE_TRIM = -1;

    private BlockBoxing() {
    }

    public static void layoutContent(LayoutContext c, BlockBox block, int contentStart) {
        int offset = -1;

        List localChildren = block.getChildren();
        if (c.isPrint() && ! (localChildren instanceof RandomAccess)) {
            localChildren = new ArrayList(localChildren);
        }

        int childOffset = block.getHeight() + contentStart;

        RelayoutDataList relayoutDataList = null;
        if (c.isPrint()) {
            relayoutDataList = new RelayoutDataList(localChildren.size());
        }

        int pageCount = NO_PAGE_TRIM;
        BlockBox previousChildBox = null;
        for (Iterator i = localChildren.iterator(); i.hasNext();) {
            BlockBox child = (BlockBox) i.next();
            offset++;

            RelayoutData relayoutData = null;

            boolean mayCheckKeepTogether = false;
            if (c.isPrint()) {
                relayoutData = relayoutDataList.get(offset);
                relayoutData.setLayoutState(c.copyStateForRelayout());
                relayoutData.setChildOffset(childOffset);
                pageCount = c.getRootLayer().getPages().size();

                child.setNeedPageClear(false);

                if ((child.getStyle().isAvoidPageBreakInside() || child.getStyle().isKeepWithInline())
                        && c.isMayCheckKeepTogether()) {
                    mayCheckKeepTogether = true;
                    c.setMayCheckKeepTogether(false);
                }
            }

            layoutBlockChild(
                    c, block, child, false, childOffset, NO_PAGE_TRIM,
                    relayoutData == null ? null : relayoutData.getLayoutState());

            if (c.isPrint()) {
                boolean needPageClear = child.isNeedPageClear();
                if (needPageClear || mayCheckKeepTogether) {
                    c.setMayCheckKeepTogether(mayCheckKeepTogether);
                    boolean tryToAvoidPageBreak = child.getStyle().isAvoidPageBreakInside() && child.crossesPageBreak(c);
                    boolean keepWithInline = child.isNeedsKeepWithInline(c);
                    if (tryToAvoidPageBreak || needPageClear || keepWithInline) {
                        c.restoreStateForRelayout(relayoutData.getLayoutState());
                        child.reset(c);
                        layoutBlockChild(
                                c, block, child, true, childOffset, pageCount, relayoutData.getLayoutState());

                        if (tryToAvoidPageBreak && child.crossesPageBreak(c) && ! keepWithInline) {
                            c.restoreStateForRelayout(relayoutData.getLayoutState());
                            child.reset(c);
                            layoutBlockChild(
                                    c, block, child, false, childOffset, pageCount, relayoutData.getLayoutState());
                        }
                    }
                }
                c.getRootLayer().ensureHasPage(c, child);
            }

            Dimension relativeOffset = child.getRelativeOffset();
            if (relativeOffset == null) {
                childOffset = child.getY() + child.getHeight();
            } else {
                // Box will have been positioned by this point so calculate
                // relative to where it would have been if it hadn't been
                // moved
                childOffset = child.getY() - relativeOffset.height + child.getHeight();
            }

            if (childOffset > block.getHeight()) {
                block.setHeight(childOffset);
            }

            if (c.isPrint()) {
                if (child.getStyle().isForcePageBreakAfter()) {
                    block.forcePageBreakAfter(c, child.getStyle().getIdent(CSSName.PAGE_BREAK_AFTER));
                    childOffset = block.getHeight();
                }

                if (previousChildBox != null) {
                    relayoutDataList.markRun(offset, previousChildBox, child);
                }

                RelayoutRunResult runResult =
                        processPageBreakAvoidRun(
                                c, block, localChildren, offset, relayoutDataList, relayoutData, child);
                if (runResult.isChanged()) {
                    childOffset = runResult.getChildOffset();
                    if (childOffset > block.getHeight()) {
                        block.setHeight(childOffset);
                    }
                }
            }

            previousChildBox = child;
        }
    }

    private static RelayoutRunResult processPageBreakAvoidRun(final LayoutContext c, final BlockBox block,
                                                              List localChildren, int offset,
                                                              RelayoutDataList relayoutDataList, RelayoutData relayoutData,
                                                              BlockBox childBox) {
        RelayoutRunResult result = new RelayoutRunResult();
        if (offset > 0) {
            boolean mightNeedRelayout = false;
            int runEnd = -1;
            if (offset == localChildren.size() - 1 && relayoutData.isEndsRun()) {
                mightNeedRelayout = true;
                runEnd = offset;
            } else if (offset > 0) {
                RelayoutData previousRelayoutData = relayoutDataList.get(offset - 1);
                if (previousRelayoutData.isEndsRun()) {
                    mightNeedRelayout = true;
                    runEnd = offset - 1;
                }
            }
            if (mightNeedRelayout) {
                int runStart = relayoutDataList.getRunStart(runEnd);
                if ( isPageBreakBetweenChildBoxes(relayoutDataList, runStart, runEnd, c, block) ) {
                    result.setChanged(true);
                    block.resetChildren(c, runStart, offset);
                    result.setChildOffset(relayoutRun(c, localChildren, block,
                            relayoutDataList, runStart, offset, true));
                    if ( isPageBreakBetweenChildBoxes(relayoutDataList, runStart, runEnd, c, block) ) {
                        block.resetChildren(c, runStart, offset);
                        result.setChildOffset(relayoutRun(c, localChildren, block,
                                relayoutDataList, runStart, offset, false));
                    }
                }
            }
        }
        return result;
    }

    private static boolean isPageBreakBetweenChildBoxes(RelayoutDataList relayoutDataList,
            int runStart, int runEnd, LayoutContext c, BlockBox block) {
        for ( int i = runStart; i < runEnd; i++ ) {
            Box prevChild = block.getChild(i);
            Box nextChild = block.getChild(i+1);
            // if nextChild is made of several lines, then only the first line
            // is relevant for "page-break-before: avoid".
            Box nextLine = getFirstLine(nextChild) == null ? nextChild : getFirstLine(nextChild);
            int prevChildEnd = prevChild.getAbsY() + prevChild.getHeight();
            int nextLineEnd = nextLine.getAbsY() + nextLine.getHeight();
            if ( c.getRootLayer().crossesPageBreak(c, prevChildEnd, nextLineEnd) ) {
                return true;
            }
        }
        return false;
    }

    private static LineBox getFirstLine(Box box) {
        for ( Box child = box; child.getChildCount()>0; child = child.getChild(0) ) {
            if ( child instanceof LineBox ) {
                return (LineBox) child;
            }
        }
        return null;
    }

    private static int relayoutRun(
            LayoutContext c, List localChildren, BlockBox block,
            RelayoutDataList relayoutDataList, int start, int end, boolean onNewPage) {
        int childOffset = relayoutDataList.get(start).getChildOffset();

        if (onNewPage) {
            Box startBox = (Box) localChildren.get(start);
            PageBox startPageBox = c.getRootLayer().getFirstPage(c, startBox);
            childOffset += startPageBox.getBottom() - startBox.getAbsY();
        }

        // reset height of parent as it is used for Y-setting of children
        block.setHeight(childOffset);


        for (int i = start; i <= end; i++) {
            BlockBox child = (BlockBox) localChildren.get(i);

            RelayoutData relayoutData = relayoutDataList.get(i);

            int pageCount = c.getRootLayer().getPages().size();

            //TODO:handle run-ins. For now, treat them as blocks

            c.restoreStateForRelayout(relayoutData.getLayoutState());
            relayoutData.setChildOffset(childOffset);
            boolean mayCheckKeepTogether = false;
            if ((child.getStyle().isAvoidPageBreakInside() || child.getStyle().isKeepWithInline())
                    && c.isMayCheckKeepTogether()) {
                mayCheckKeepTogether = true;
                c.setMayCheckKeepTogether(false);
            }
            layoutBlockChild(
                    c, block, child, false, childOffset, NO_PAGE_TRIM, relayoutData.getLayoutState());

            if (mayCheckKeepTogether) {
                c.setMayCheckKeepTogether(true);
                boolean tryToAvoidPageBreak =
                    child.getStyle().isAvoidPageBreakInside() && child.crossesPageBreak(c);
                boolean needPageClear = child.isNeedPageClear();
                boolean keepWithInline = child.isNeedsKeepWithInline(c);
                if (tryToAvoidPageBreak || needPageClear || keepWithInline) {
                    c.restoreStateForRelayout(relayoutData.getLayoutState());
                    child.reset(c);
                    layoutBlockChild(
                            c, block, child, true, childOffset, pageCount, relayoutData.getLayoutState());

                    if (tryToAvoidPageBreak && child.crossesPageBreak(c) && ! keepWithInline) {
                        c.restoreStateForRelayout(relayoutData.getLayoutState());
                        child.reset(c);
                        layoutBlockChild(
                                c, block, child, false, childOffset, pageCount, relayoutData.getLayoutState());
                    }
                }
            }

            c.getRootLayer().ensureHasPage(c, child);

            Dimension relativeOffset = child.getRelativeOffset();
            if (relativeOffset == null) {
                childOffset = child.getY() + child.getHeight();
            } else {
                childOffset = child.getY() - relativeOffset.height + child.getHeight();
            }

            if (childOffset > block.getHeight()) {
                block.setHeight(childOffset);
            }

            if (child.getStyle().isForcePageBreakAfter()) {
                block.forcePageBreakAfter(c, child.getStyle().getIdent(CSSName.PAGE_BREAK_AFTER));
                childOffset = block.getHeight();
            }
        }

        return childOffset;
    }

    private static void layoutBlockChild(
            LayoutContext c, BlockBox parent, BlockBox child,
            boolean needPageClear, int childOffset, int trimmedPageCount, LayoutState layoutState) {
        layoutBlockChild0(c, parent, child, needPageClear, childOffset, trimmedPageCount);
        BreakAtLineContext bContext = child.calcBreakAtLineContext(c);
        if (bContext != null) {
            c.setBreakAtLineContext(bContext);
            c.restoreStateForRelayout(layoutState);
            child.reset(c);
            layoutBlockChild0(c, parent, child, needPageClear, childOffset, trimmedPageCount);
            c.setBreakAtLineContext(null);
        }
    }

    private static void layoutBlockChild0(LayoutContext c, BlockBox parent, BlockBox child,
            boolean needPageClear, int childOffset, int trimmedPageCount) {
        child.setNeedPageClear(needPageClear);

        child.initStaticPos(c, parent, childOffset);

        child.initContainingLayer(c);
        child.calcCanvasLocation();

        c.translate(0, childOffset);
        repositionBox(c, child, trimmedPageCount);
        child.layout(c);
        c.translate(-child.getX(), -child.getY());
    }

    private static void repositionBox(LayoutContext c, BlockBox child, int trimmedPageCount) {
        boolean moved = false;
        if (child.getStyle().isRelative()) {
            Dimension delta = child.positionRelative(c);
            c.translate(delta.width, delta.height);
            moved = true;
        }
        if (c.isPrint()) {
            boolean pageClear = child.isNeedPageClear() ||
                                    child.getStyle().isForcePageBreakBefore();
            boolean needNewPageContext = child.checkPageContext(c);

            if (needNewPageContext && trimmedPageCount != NO_PAGE_TRIM) {
                c.getRootLayer().trimPageCount(trimmedPageCount);
            }

            if (pageClear || needNewPageContext) {
                int delta = child.forcePageBreakBefore(
                        c,
                        child.getStyle().getIdent(CSSName.PAGE_BREAK_BEFORE),
                        needNewPageContext);
                c.translate(0, delta);
                moved = true;
                child.setNeedPageClear(false);
            }
        }
        if (moved) {
            child.calcCanvasLocation();
        }
    }

    private static class RelayoutDataList {
        private List _hints;

        public RelayoutDataList(int size) {
            _hints = new ArrayList(size);
            for (int i = 0; i < size; i++) {
                _hints.add(new RelayoutData());
            }
        }

        public RelayoutData get(int index) {
            return (RelayoutData) _hints.get(index);
        }

        public void markRun(int offset, BlockBox previous, BlockBox current) {
            RelayoutData previousData = get(offset - 1);
            RelayoutData currentData = get(offset);

            IdentValue previousAfter =
                    previous.getStyle().getIdent(CSSName.PAGE_BREAK_AFTER);
            IdentValue currentBefore =
                    current.getStyle().getIdent(CSSName.PAGE_BREAK_BEFORE);

            if ((previousAfter == IdentValue.AVOID && currentBefore == IdentValue.AUTO) ||
                    (previousAfter == IdentValue.AUTO && currentBefore == IdentValue.AVOID) ||
                    (previousAfter == IdentValue.AVOID && currentBefore == IdentValue.AVOID)) {
                if (! previousData.isInRun()) {
                    previousData.setStartsRun(true);
                }
                previousData.setInRun(true);
                currentData.setInRun(true);

                if (offset == _hints.size() - 1) {
                    currentData.setEndsRun(true);
                }
            } else {
                if (previousData.isInRun()) {
                    previousData.setEndsRun(true);
                }
            }
        }

        public int getRunStart(int runEnd) {
            int offset = runEnd;
            RelayoutData current = get(offset);
            if (! current.isEndsRun()) {
                throw new RuntimeException("Not the end of a run");
            }
            while (! current.isStartsRun()) {
                current = get(--offset);
            }
            return offset;
        }
    }

    private static class RelayoutRunResult {
        private boolean _changed;
        private int _childOffset;

        public boolean isChanged() {
            return _changed;
        }

        public void setChanged(boolean changed) {
            _changed = changed;
        }

        public int getChildOffset() {
            return _childOffset;
        }

        public void setChildOffset(int childOffset) {
            _childOffset = childOffset;
        }
    }

    private static class RelayoutData {
        private LayoutState _layoutState;
        private int _listIndex;

        private boolean _startsRun;
        private boolean _endsRun;
        private boolean _inRun;

        private int _childOffset;

        public RelayoutData() {
        }

        public boolean isEndsRun() {
            return _endsRun;
        }

        public void setEndsRun(boolean endsRun) {
            _endsRun = endsRun;
        }

        public boolean isInRun() {
            return _inRun;
        }

        public void setInRun(boolean inRun) {
            _inRun = inRun;
        }

        public LayoutState getLayoutState() {
            return _layoutState;
        }

        public void setLayoutState(LayoutState layoutState) {
            _layoutState = layoutState;
        }

        public boolean isStartsRun() {
            return _startsRun;
        }

        public void setStartsRun(boolean startsRun) {
            _startsRun = startsRun;
        }

        public int getChildOffset() {
            return _childOffset;
        }

        public void setChildOffset(int childOffset) {
            _childOffset = childOffset;
        }

        public int getListIndex() {
            return _listIndex;
        }

        public void setListIndex(int listIndex) {
            _listIndex = listIndex;
        }
    }
}

/*
 * $Id$
 *
 * $Log$
 * Revision 1.69  2009/05/12 17:38:30  peterbrant
 * Patch from Stefan Hoffmann
 *
 * Revision 1.68  2009/01/23 22:32:07  peterbrant
 * Fix NPE reported by Christophe M (thanks Patrick)
 *
 * Revision 1.67  2008/12/14 13:53:30  peterbrant
 * Implement -fs-keep-with-inline: keep property that instructs FS to try to avoid breaking a box so that only borders and padding appear on a page
 *
 * Revision 1.66  2008/04/12 01:04:13  peterbrant
 * Optimize implementation of page-break-inside: avoid
 *
 * Revision 1.65  2008/02/03 12:35:06  peterbrant
 * Need to update child offset when laying out a block again.  We'll need it later if two "keep together" runs adjoin.
 *
 * Revision 1.64  2007/08/19 22:22:52  peterbrant
 * Merge R8pbrant changes to HEAD
 *
 * Revision 1.63.2.3  2007/08/17 19:11:30  peterbrant
 * When paginating a table, move table past page break if header would overlap the page break
 *
 * Revision 1.63.2.2  2007/08/17 17:11:01  peterbrant
 * Try to avoid awkward row splits when paginating a table by making sure there is at least one line box on the same page as the start of the row
 *
 * Revision 1.63.2.1  2007/08/07 17:06:30  peterbrant
 * Implement named pages / Implement page-break-before/after: left/right / Experiment with efficient selection
 *
 * Revision 1.63  2007/06/16 22:51:24  tobega
 * We now support counters!
 *
 * Revision 1.62  2007/06/11 22:24:53  peterbrant
 * Fix bug when calculating new initial position of a run of blocks tied together by page-break-before/after
 *
 * Revision 1.61  2007/06/07 16:56:30  peterbrant
 * When vertically aligning table cell content, call layout again on cells as necessary to make sure pagination properties are respected at the cell's final position (and to make sure line boxes can't straddle page breaks).
 *
 * Revision 1.60  2007/05/22 15:55:15  peterbrant
 * Minor cleanup
 *
 * Revision 1.59  2007/05/21 21:58:48  peterbrant
 * More cleanup (remove experimental threading code)
 *
 * Revision 1.58  2007/04/25 18:09:41  peterbrant
 * Always reset block box margin if it is the first thing on a page
 *
 * Revision 1.57  2007/03/12 21:11:21  peterbrant
 * Documentation update
 *
 * Revision 1.56  2007/03/08 19:47:04  peterbrant
 * Comment change
 *
 * Revision 1.55  2007/03/08 18:02:51  peterbrant
 * Fix regression in page-break-before/after: always
 *
 * Revision 1.54  2007/02/21 23:49:41  peterbrant
 * Can't calculate clearance until margins have been collapsed / Clearance must be calculated relative to the box's border edge, not margin edge
 *
 * Revision 1.53  2007/02/21 17:16:49  peterbrant
 * Calculate position of next child and block height independently.  They may not
 * move in lockstep in the face of negative vertical margins.
 *
 * Revision 1.52  2007/02/07 16:33:35  peterbrant
 * Initial commit of rewritten table support and associated refactorings
 *
 * Revision 1.51  2006/09/01 23:49:35  peterbrant
 * Implement basic margin collapsing / Various refactorings in preparation for shrink-to-fit / Add hack to treat auto margins as zero
 *
 * Revision 1.50  2006/08/29 17:29:11  peterbrant
 * Make Style object a thing of the past
 *
 * Revision 1.49  2006/08/27 00:35:42  peterbrant
 * Initial commit of (initial) R7 work
 *
 * Revision 1.48  2006/03/01 00:45:03  peterbrant
 * Provide LayoutContext when calling detach() and friends
 *
 * Revision 1.47  2006/02/07 00:02:51  peterbrant
 * If "keep together" cannot be satisified, drop rule vs. pushing to next page / Fix bug with incorrect positioning of content following relative block layers
 *
 * Revision 1.46  2006/01/27 01:15:31  peterbrant
 * Start on better support for different output devices
 *
 * Revision 1.45  2006/01/11 22:08:53  peterbrant
 * Only increment list "counter" when display: list-item
 *
 * Revision 1.44  2006/01/10 20:11:31  peterbrant
 * Fix bug in page-break-before/avoid: avoid
 *
 * Revision 1.43  2006/01/10 19:55:59  peterbrant
 * Fix inappropriate box resizing when width: auto
 *
 * Revision 1.42  2006/01/04 19:50:15  peterbrant
 * More pagination bug fixes / Implement simple pagination for tables
 *
 * Revision 1.41  2006/01/03 23:55:54  peterbrant
 * Add support for proper page breaking of floats / More bug fixes to pagination support
 *
 * Revision 1.40  2006/01/03 17:04:46  peterbrant
 * Many pagination bug fixes / Add ability to position absolute boxes in margin area
 *
 * Revision 1.39  2006/01/02 20:59:07  peterbrant
 * Implement page-break-before/after: avoid
 *
 * Revision 1.38  2006/01/01 03:35:37  peterbrant
 * In print mode, make sure block has a page on which to be drawn
 *
 * Revision 1.37  2006/01/01 03:14:27  peterbrant
 * Implement page-break-inside: avoid
 *
 * Revision 1.36  2006/01/01 02:38:16  peterbrant
 * Merge more pagination work / Various minor cleanups
 *
 * Revision 1.35  2005/12/30 01:32:35  peterbrant
 * First merge of parts of pagination work
 *
 * Revision 1.34  2005/12/28 00:50:50  peterbrant
 * Continue ripping out first try at pagination / Minor method name refactoring
 *
 * Revision 1.33  2005/12/21 02:36:26  peterbrant
 * - Calculate absolute positions incrementally (prep work for pagination)
 * - Light cleanup
 * - Fix bug where floats nested in floats could cause the outer float to be positioned in the wrong place
 *
 * Revision 1.32  2005/12/17 02:24:07  peterbrant
 * Remove last pieces of old (now non-working) clip region checking / Push down handful of fields from Box to BlockBox
 *
 * Revision 1.31  2005/12/07 00:33:12  peterbrant
 * :first-letter and :first-line works again
 *
 * Revision 1.30  2005/12/05 00:13:54  peterbrant
 * Improve list-item support (marker positioning is now correct) / Start support for relative inline layers
 *
 * Revision 1.29  2005/11/29 02:37:25  peterbrant
 * Make clear work again / Rip out old pagination code
 *
 * Revision 1.28  2005/11/25 16:57:14  peterbrant
 * Initial commit of inline content refactoring
 *
 * Revision 1.27  2005/11/05 18:45:05  peterbrant
 * General cleanup / Remove obsolete code
 *
 * Revision 1.26  2005/11/05 03:29:30  peterbrant
 * Start work on painting order and improved positioning implementation
 *
 * Revision 1.25  2005/11/03 20:58:41  peterbrant
 * Bug fixes to rewritten float code.  Floated block positioning should be very solid now.
 *
 * Revision 1.24  2005/11/03 17:58:16  peterbrant
 * Float rewrite (still stomping bugs, but demos work)
 *
 * Revision 1.23  2005/11/02 18:15:25  peterbrant
 * First merge of Tobe's and my stacking context work / Rework float code (not done yet)
 *
 * Revision 1.22  2005/10/27 00:08:58  tobega
 * Sorted out Context into RenderingContext and LayoutContext
 *
 * Revision 1.21  2005/10/18 20:57:01  tobega
 * Patch from Peter Brant
 *
 * Revision 1.20  2005/10/16 23:57:14  tobega
 * Starting experiment with flat representation of render tree
 *
 * Revision 1.19  2005/10/12 21:17:12  tobega
 * patch from Peter Brant
 *
 * Revision 1.18  2005/10/08 17:40:20  tobega
 * Patch from Peter Brant
 *
 * Revision 1.17  2005/10/06 03:20:20  tobega
 * Prettier incremental rendering. Ran into more trouble than expected and some creepy crawlies and a few pages don't look right (forms.xhtml, splash.xhtml)
 *
 * Revision 1.16  2005/10/02 21:42:53  tobega
 * Only do incremental rendering if we are in an interactive context
 *
 * Revision 1.15  2005/10/02 21:29:58  tobega
 * Fixed a lot of concurrency (and other) issues from incremental rendering. Also some house-cleaning.
 *
 * Revision 1.14  2005/09/29 21:34:02  joshy
 * minor updates to a lot of files. pulling in more incremental rendering code.
 * fixed another resize bug
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.13  2005/09/27 23:48:39  joshy
 * first merge of basicpanel reworking and incremental layout. more to come.
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.12  2005/07/20 18:11:41  joshy
 * bug fixes to absolute pos layout and box finding within abs layout
 *
 * Revision 1.11  2005/06/19 23:31:32  joshy
 * stop layout support
 * clear bug fixes
 * mouse busy cursor support
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.10  2005/06/16 18:34:09  joshy
 * support for clear:right
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.9  2005/06/16 04:38:15  joshy
 * finished support for clear
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.8  2005/06/15 16:49:48  joshy
 * inital clear support
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.7  2005/06/05 01:02:34  tobega
 * Very simple and not completely functional table layout
 *
 * Revision 1.6  2005/06/03 19:56:42  tobega
 * Now uses first-line styles from all block-level ancestors
 *
 * Revision 1.5  2005/05/13 11:49:57  tobega
 * Started to fix up borders on inlines. Got caught up in refactoring.
 * Boxes shouldn't cache borders and stuff unless necessary. Started to remove unnecessary references.
 * Hover is not working completely well now, might get better when I'm done.
 *
 * Revision 1.4  2005/01/31 22:50:17  pdoubleya
 * .
 *
 * Revision 1.3  2005/01/29 20:24:27  pdoubleya
 * Clean/reformat code. Removed commented blocks, checked copyright.
 *
 * Revision 1.2  2005/01/07 12:42:07  tobega
 * Hacked improved support for custom components (read forms). Creates trouble with the image demo. Anyway, components work and are usually in the right place.
 *
 * Revision 1.1  2005/01/02 12:22:16  tobega
 * Cleaned out old layout code
 *
 * Revision 1.62  2005/01/02 09:32:40  tobega
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

