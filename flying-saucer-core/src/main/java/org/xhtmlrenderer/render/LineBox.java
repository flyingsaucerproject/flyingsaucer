/*
 * {{{ header & license
 * Copyright (c) 2004, 2005 Joshua Marinacci
 * Copyright (c) 2006, 2007 Wisconsin Court System
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

import java.awt.Rectangle;
import java.awt.Shape;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.w3c.dom.Element;
import org.xhtmlrenderer.css.constants.CSSName;
import org.xhtmlrenderer.css.constants.IdentValue;
import org.xhtmlrenderer.css.parser.FSRGBColor;
import org.xhtmlrenderer.css.style.CalculatedStyle;
import org.xhtmlrenderer.css.style.CssContext;
import org.xhtmlrenderer.layout.BoxCollector;
import org.xhtmlrenderer.layout.InlineBoxing;
import org.xhtmlrenderer.layout.InlinePaintable;
import org.xhtmlrenderer.layout.Layer;
import org.xhtmlrenderer.layout.LayoutContext;
import org.xhtmlrenderer.layout.PaintingInfo;
import org.xhtmlrenderer.util.XRRuntimeException;

/**
 * A line box contains a single line of text (or other inline content).  It
 * is created during layout.  It also tracks floated and absolute content
 * added while laying out the line.
 */
public class LineBox extends Box implements InlinePaintable {
    private static final float JUSTIFY_NON_SPACE_SHARE = 0.20f;
    private static final float JUSTIFY_SPACE_SHARE = 1 - JUSTIFY_NON_SPACE_SHARE;
    
    private boolean _endsOnNL;
    private boolean _containsContent;
    private boolean _containsBlockLevelContent;
    
    private FloatDistances _floatDistances;
    
    private List _textDecorations;
    
    private int _paintingTop;
    private int _paintingHeight;
    
    private List _nonFlowContent;
    
    private MarkerData _markerData;
    
    private boolean _containsDynamicFunction;
    
    private int _contentStart;
    
    private int _baseline;
    
    private JustificationInfo _justificationInfo;
    
    public LineBox() {
    }
    
    public String dump(LayoutContext c, String indent, int which) {
        if (which != Box.DUMP_RENDER) {
            throw new IllegalArgumentException();
        }

        StringBuffer result = new StringBuffer(indent);
        result.append(this);
        result.append('\n');
        
        dumpBoxes(c, indent, getNonFlowContent(), Box.DUMP_RENDER, result);
        if (getNonFlowContent().size() > 0  ) {
            result.append('\n');
        }
        dumpBoxes(c, indent, getChildren(), Box.DUMP_RENDER, result);
        
        return result.toString();
    }

    public String toString() {
        return "LineBox: (" + getAbsX() + "," + getAbsY() + ")->(" + getWidth() + "," + getHeight() + ")";
    }

    public Rectangle getMarginEdge(CssContext cssCtx, int tx, int ty) {
        Rectangle result = new Rectangle(getX(), getY(), getContentWidth(), getHeight());
        result.translate(tx, ty);
        return result;
    }
    
    public void paintInline(RenderingContext c) {
        if (! getParent().getStyle().isVisible()) {
            return;
        }
        
        if (isContainsDynamicFunction()) {
            lookForDynamicFunctions(c);
            int totalLineWidth = InlineBoxing.positionHorizontally(c, this, 0);
            setContentWidth(totalLineWidth);
            calcChildLocations();
            align(true);
            calcPaintingInfo(c, false);
        }
        
        if (_textDecorations != null) {
            c.getOutputDevice().drawTextDecoration(c, this);
        }
        
        if (c.debugDrawLineBoxes()) {
            c.getOutputDevice().drawDebugOutline(c, this, FSRGBColor.GREEN);
        }
    }
    
    private void lookForDynamicFunctions(RenderingContext c) {
        if (getChildCount() > 0) {
            for (int i = 0; i < getChildCount(); i++) {
                Box b = (Box)getChild(i);
                if (b instanceof InlineLayoutBox) {
                    ((InlineLayoutBox)b).lookForDynamicFunctions(c);
                }
            }
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
                if (! (b instanceof InlineLayoutBox)) {
                    break;
                }
                InlineLayoutBox iB = (InlineLayoutBox)b;
                iB.prunePending();
                if (iB.isPending()) {
                    removeChild(i);
                }
            }
        }
    }

    public boolean isContainsContent() {
        return _containsContent;
    }

    public void setContainsContent(boolean containsContent) {
        _containsContent = containsContent;
    }
    
    public boolean isEndsOnNL() {
        return _endsOnNL;
    }

    public void setEndsOnNL(boolean endsOnNL) {
        _endsOnNL = endsOnNL;
    }

    public void align(boolean dynamic) {
        IdentValue align = getParent().getStyle().getIdent(CSSName.TEXT_ALIGN);
        
        int calcX = 0;
        
        if (align == IdentValue.LEFT || align == IdentValue.JUSTIFY) {
            int floatDistance = getFloatDistances().getLeftFloatDistance();
            calcX = getContentStart() + floatDistance;
            if (align == IdentValue.JUSTIFY && dynamic) {
                justify();
            }
        } else if (align == IdentValue.CENTER) {
            int leftFloatDistance = getFloatDistances().getLeftFloatDistance();
            int rightFloatDistance = getFloatDistances().getRightFloatDistance();
            
            int midpoint = leftFloatDistance +
                (getParent().getContentWidth() - leftFloatDistance - rightFloatDistance) / 2;
            
            calcX = midpoint - (getContentWidth() + getContentStart()) / 2;
        } else if (align == IdentValue.RIGHT) {
            int floatDistance = getFloatDistances().getRightFloatDistance();
            calcX = getParent().getContentWidth() - floatDistance - getContentWidth();
        }
        
        if (calcX != getX()) {
            setX(calcX);
            calcCanvasLocation();
            calcChildLocations();
        }
    }
    
    public void justify() {
        if (! isLastLineWithContent()) {
            int leftFloatDistance = getFloatDistances().getLeftFloatDistance();
            int rightFloatDistance = getFloatDistances().getRightFloatDistance();
            
            int available = getParent().getContentWidth() - 
                leftFloatDistance - rightFloatDistance - getContentStart(); 
            
            if (available > getContentWidth()) {
                int toAdd = available - getContentWidth();
                
                CharCounts counts = countJustifiableChars();
                
                JustificationInfo info = new JustificationInfo();
                if (! getParent().getStyle().isIdent(CSSName.LETTER_SPACING, IdentValue.NORMAL)) {
                    info.setNonSpaceAdjust(0.0f);
                    info.setSpaceAdjust((float)toAdd / counts.getSpaceCount());
                } else {
                    if (counts.getNonSpaceCount() > 1) {
                        info.setNonSpaceAdjust((float)toAdd * JUSTIFY_NON_SPACE_SHARE / (counts.getNonSpaceCount()-1));
                    } else {
                        info.setNonSpaceAdjust(0.0f);
                    }
                    
                    if (counts.getSpaceCount() > 0) {
                        info.setSpaceAdjust((float)toAdd * JUSTIFY_SPACE_SHARE / counts.getSpaceCount());
                    } else {
                        info.setSpaceAdjust(0.0f);
                    }
                }
                
                adjustChildren(info);
                
                setJustificationInfo(info);
            }
        }
    }
    
    private void adjustChildren(JustificationInfo info) {
        float adjust = 0.0f;
        for (Iterator i = getChildIterator(); i.hasNext(); ) {
            Box b = (Box)i.next();
            b.setX(b.getX() + Math.round(adjust));
            
            if (b instanceof InlineLayoutBox) {
                adjust += ((InlineLayoutBox)b).adjustHorizontalPosition(info, adjust);
            }
        }
        
        calcChildLocations();
    }
    
    private boolean isLastLineWithContent() {
        LineBox current = (LineBox)getNextSibling();
        if (!_endsOnNL) {
            while (current != null) {
                if (current.isContainsContent()) {
                    return false;
                } else {
                    current = (LineBox)current.getNextSibling();
                }
            }
        }
        return true;
    }
    
    private CharCounts countJustifiableChars() {
        CharCounts result = new CharCounts();
        
        for (Iterator i = getChildIterator(); i.hasNext(); ) {
            Box b = (Box)i.next();
            if (b instanceof InlineLayoutBox) {
                ((InlineLayoutBox)b).countJustifiableChars(result);
            }
        }
        
        return result;
    }
    
	public FloatDistances getFloatDistances() {
		return _floatDistances;
	}

	public void setFloatDistances(FloatDistances floatDistances) {
		_floatDistances = floatDistances;
	}

    public boolean isContainsBlockLevelContent() {
        return _containsBlockLevelContent;
    }

    public void setContainsBlockLevelContent(boolean containsBlockLevelContent) {
        _containsBlockLevelContent = containsBlockLevelContent;
    }
    
    public boolean intersects(CssContext cssCtx, Shape clip) {
        return clip == null || (intersectsLine(cssCtx, clip) || 
            (isContainsBlockLevelContent() && intersectsInlineBlocks(cssCtx, clip)));
    }
    
    private boolean intersectsLine(CssContext cssCtx, Shape clip) {
        Rectangle result = getPaintingClipEdge(cssCtx);
        return clip.intersects(result);
    }

    public Rectangle getPaintingClipEdge(CssContext cssCtx) {
        Box parent = getParent();
        Rectangle result = null;
        if (parent.getStyle().isIdent(
                CSSName.FS_TEXT_DECORATION_EXTENT, IdentValue.BLOCK) || 
                    getJustificationInfo() != null) {
            result = new Rectangle(
                    getAbsX(), getAbsY() + _paintingTop, 
                    parent.getAbsX() + parent.getTx() + parent.getContentWidth() - getAbsX(), 
                    _paintingHeight);
        } else {
            result = new Rectangle(
                    getAbsX(), getAbsY() + _paintingTop, getContentWidth(), _paintingHeight);
        }
        return result;
    }
    
    private boolean intersectsInlineBlocks(CssContext cssCtx, Shape clip) {
        for (int i = 0; i < getChildCount(); i++) {
            Box child = (Box)getChild(i);
            if (child instanceof InlineLayoutBox) {
                boolean possibleResult = ((InlineLayoutBox)child).intersectsInlineBlocks(
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

    public List getTextDecorations() {
        return _textDecorations;
    }

    public void setTextDecorations(List textDecorations) {
        _textDecorations = textDecorations;
    }

    public int getPaintingHeight() {
        return _paintingHeight;
    }

    public void setPaintingHeight(int paintingHeight) {
        _paintingHeight = paintingHeight;
    }

    public int getPaintingTop() {
        return _paintingTop;
    }

    public void setPaintingTop(int paintingTop) {
        _paintingTop = paintingTop;
    }
    
    
    public void addAllChildren(List list, Layer layer) {
        for (int i = 0; i < getChildCount(); i++) {
            Box child = getChild(i);
            if (getContainingLayer() == layer) {
                list.add(child);
                if (child instanceof InlineLayoutBox) {
                    ((InlineLayoutBox)child).addAllChildren(list, layer);
                }
            }
        }
    }
    
    public List getNonFlowContent() {
        return _nonFlowContent == null ? Collections.EMPTY_LIST : _nonFlowContent;
    }
    
    public void addNonFlowContent(BlockBox box) {
        if (_nonFlowContent == null) {
            _nonFlowContent = new ArrayList();
        }
        
        _nonFlowContent.add(box);
    }
    
    public void reset(LayoutContext c) {
        for (int i = 0; i < getNonFlowContent().size(); i++) {
            Box content = (Box)getNonFlowContent().get(i);
            content.reset(c);
        }
        if (_markerData != null) {
            _markerData.restorePreviousReferenceLine(this);
        }
        super.reset(c);
    }
    
    public void calcCanvasLocation() {
        Box parent = getParent();
        if (parent == null) {
            throw new XRRuntimeException("calcCanvasLocation() called with no parent");
        }
        setAbsX(parent.getAbsX() + parent.getTx() + getX());
        setAbsY(parent.getAbsY() + parent.getTy() + getY());        
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

    public MarkerData getMarkerData() {
        return _markerData;
    }

    public void setMarkerData(MarkerData markerData) {
        _markerData = markerData;
    }

    public boolean isContainsDynamicFunction() {
        return _containsDynamicFunction;
    }

    public void setContainsDynamicFunction(boolean containsPageCounter) {
        _containsDynamicFunction |= containsPageCounter;
    }

    public int getContentStart() {
        return _contentStart;
    }

    public void setContentStart(int contentOffset) {
        _contentStart = contentOffset;
    }
    
    public InlineText findTrailingText() {
        if (getChildCount() == 0) {
            return null;
        }
        
        for (int offset = getChildCount() - 1; offset >= 0; offset--) {
            Box child = getChild(offset);
            if (child instanceof InlineLayoutBox) {
                InlineText result = ((InlineLayoutBox)child).findTrailingText();
                if (result != null && result.isEmpty()) {
                    continue;
                }
                return result;
            } else {
                return null;
            }
        }
        
        return null;
    }
    
    public void trimTrailingSpace(LayoutContext c) {
        InlineText text = findTrailingText();
        
        if (text != null) {
            InlineLayoutBox iB = text.getParent();
            IdentValue whitespace = iB.getStyle().getWhitespace();
            if (whitespace == IdentValue.NORMAL || whitespace == IdentValue.NOWRAP) {
                text.trimTrailingSpace(c);
            }
        }
    }    
    
    public Box find(CssContext cssCtx, int absX, int absY, boolean findAnonymous) {
        PaintingInfo pI = getPaintingInfo();
        if (pI !=null && ! pI.getAggregateBounds().contains(absX, absY)) {
            return null;
        }
        
        Box result = null;
        for (int i = 0; i < getChildCount(); i++) {
            Box child = getChild(i);
            result = child.find(cssCtx, absX, absY, findAnonymous);
            if (result != null) {
                return result;
            }
        }
        
        return null;
    }

    public int getBaseline() {
        return _baseline;
    }

    public void setBaseline(int baseline) {
        _baseline = baseline;
    }
    
    public boolean isContainsOnlyBlockLevelContent() {
        if (! isContainsBlockLevelContent()) {
            return false;
        }
        
        for (int i = 0; i < getChildCount(); i++) {
            Box b = (Box)getChild(i);
            if (! (b instanceof BlockBox)) {
                return false;
            }
        }
        
        return true;
    }
    
    public Box getRestyleTarget() {
        return getParent();
    }
    
    public void restyle(LayoutContext c) {
        Box parent = getParent();
        Element e = parent.getElement();
        if (e != null) {
            CalculatedStyle style = c.getSharedContext().getStyle(e, true);
            setStyle(style.createAnonymousStyle(IdentValue.BLOCK));
        }
        
        restyleChildren(c);
    }    
    
    public boolean isContainsVisibleContent() {
        for (int i = 0; i < getChildCount(); i++) {
            Box b = getChild(i);
            if (b instanceof BlockBox) {
                if (b.getWidth() > 0 || b.getHeight() > 0) {
                    return true;
                }
            } else {
                boolean maybeResult = ((InlineLayoutBox)b).isContainsVisibleContent();
                if (maybeResult) {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    public void clearSelection(List modified) {
        for (Iterator i = getNonFlowContent().iterator(); i.hasNext(); ) {
            Box b = (Box)i.next();
            b.clearSelection(modified);
        }
        
        super.clearSelection(modified);
    }
    
    public void selectAll() {
        for (Iterator i = getNonFlowContent().iterator(); i.hasNext(); ) {
            BlockBox box = (BlockBox)i.next();
            box.selectAll();
        }
        
        super.selectAll();
    }
    
    public void collectText(RenderingContext c, StringBuffer buffer) throws IOException {
        for (Iterator i = getNonFlowContent().iterator(); i.hasNext(); ) {
            Box b = (Box)i.next();
            b.collectText(c, buffer);
        }
        if (isContainsDynamicFunction()) {
            lookForDynamicFunctions(c);
        }
        super.collectText(c, buffer);
    }
    
    public void exportText(RenderingContext c, Writer writer) throws IOException {
        int baselinePos = getAbsY() + getBaseline();
        if (baselinePos >= c.getPage().getBottom() && isInDocumentFlow()) {
            exportPageBoxText(c, writer, baselinePos);
        }
        
        for (Iterator i = getNonFlowContent().iterator(); i.hasNext(); ) {
            Box b = (Box)i.next();
            b.exportText(c, writer);
        }
        
        if (isContainsContent()) {
            StringBuffer result = new StringBuffer();
            collectText(c, result);
            writer.write(result.toString().trim());
            writer.write(LINE_SEPARATOR);
        }
    }
    
    public void analyzePageBreaks(LayoutContext c, ContentLimitContainer container) {
        container.updateTop(c, getAbsY());
        container.updateBottom(c, getAbsY() + getHeight());
    }
    
    public void checkPagePosition(LayoutContext c, boolean alwaysBreak) {
        if (! c.isPageBreaksAllowed()) {
            return;
        }
        
        PageBox pageBox = c.getRootLayer().getFirstPage(c, this);
        if (pageBox != null) {
            boolean needsPageBreak = 
                alwaysBreak || getAbsY() + getHeight() >= pageBox.getBottom() - c.getExtraSpaceBottom();
                
           if (needsPageBreak) {
               forcePageBreakBefore(c, IdentValue.ALWAYS, false);
               calcCanvasLocation();
           } else if (pageBox.getTop() + c.getExtraSpaceTop() > getAbsY()) {
               int diff = pageBox.getTop() + c.getExtraSpaceTop() - getAbsY();
               
               setY(getY() + diff);
               calcCanvasLocation();
           }
        }
    }

    public JustificationInfo getJustificationInfo() {
        return _justificationInfo;
    }

    private void setJustificationInfo(JustificationInfo justificationInfo) {
        _justificationInfo = justificationInfo;
    }
}

/*
 * $Id$
 *
 * $Log$
 * Revision 1.73  2009/05/09 14:18:24  pdoubleya
 * FindBugs: use of ref that is provably null
 *
 * Revision 1.72  2008/07/27 00:21:47  peterbrant
 * Implement CMYK color support for PDF output, starting with patch from Mykola Gurov / Banish java.awt.Color from FS core layout classes
 *
 * Revision 1.71  2008/02/11 17:52:25  peterbrant
 * Fix divide by zero error in justification algorithm
 *
 * Revision 1.70  2008/01/26 01:53:44  peterbrant
 * Implement partial support for leader and target-counter (patch from Karl Tauber)
 *
 * Revision 1.69  2007/08/30 23:14:28  peterbrant
 * Implement text-align: justify
 *
 * Revision 1.68  2007/08/29 22:18:18  peterbrant
 * Experiment with text justification
 *
 * Revision 1.67  2007/08/28 22:31:26  peterbrant
 * Implement widows and orphans properties
 *
 * Revision 1.66  2007/08/19 22:22:49  peterbrant
 * Merge R8pbrant changes to HEAD
 *
 * Revision 1.65.2.6  2007/08/16 22:38:47  peterbrant
 * Further progress on table pagination
 *
 * Revision 1.65.2.5  2007/08/15 21:29:30  peterbrant
 * Initial draft of support for running headers and footers on tables
 *
 * Revision 1.65.2.4  2007/08/14 16:10:30  peterbrant
 * Remove obsolete code
 *
 * Revision 1.65.2.3  2007/08/13 22:41:13  peterbrant
 * First pass at exporting the render tree as text
 *
 * Revision 1.65.2.2  2007/08/07 17:06:32  peterbrant
 * Implement named pages / Implement page-break-before/after: left/right / Experiment with efficient selection
 *
 * Revision 1.65.2.1  2007/07/30 00:43:15  peterbrant
 * Start implementing text selection and copying
 *
 * Revision 1.65  2007/05/16 15:44:37  peterbrant
 * inline-block/inline-table elements that take up no space should not be considered visible content
 *
 * Revision 1.64  2007/05/10 00:43:55  peterbrant
 * Empty inline elements should generate boxes and participate in line height calculations
 *
 * Revision 1.63  2007/03/12 21:11:20  peterbrant
 * Documentation update
 *
 * Revision 1.62  2007/03/07 17:15:16  peterbrant
 * Minor fixes to dump() method
 *
 * Revision 1.61  2007/02/28 18:16:29  peterbrant
 * Support multiple values for text-decoration (per spec)
 *
 * Revision 1.60  2007/02/22 15:52:46  peterbrant
 * Restyle generated content correctly (although the CSS matcher needs more
 * work before restyle with generated content and dynamic pseudo classes will work)
 *
 * Revision 1.59  2007/02/21 23:11:02  peterbrant
 * Correct margin edge calculation (as it turns out the straightforward approach is also the correct one)
 *
 * Revision 1.58  2007/02/19 14:53:36  peterbrant
 * Integrate new CSS parser
 *
 * Revision 1.57  2007/02/07 16:33:22  peterbrant
 * Initial commit of rewritten table support and associated refactorings
 *
 * Revision 1.56  2006/08/29 17:29:12  peterbrant
 * Make Style object a thing of the past
 *
 * Revision 1.55  2006/08/27 00:36:36  peterbrant
 * Initial commit of (initial) R7 work
 *
 * Revision 1.54  2006/04/02 22:22:34  peterbrant
 * Add function interface for generated content / Implement page counters in terms of this, removing previous hack / Add custom page numbering functions
 *
 * Revision 1.53  2006/03/01 00:45:02  peterbrant
 * Provide LayoutContext when calling detach() and friends
 *
 * Revision 1.52  2006/02/22 02:20:19  peterbrant
 * Links and hover work again
 *
 * Revision 1.51  2006/02/05 01:57:23  peterbrant
 * Fix bug where final space on the final line of a block was not being collapsed away
 *
 * Revision 1.50  2006/02/03 23:57:53  peterbrant
 * Implement counter(page) and counter(pages) / Bug fixes to alignment calculation
 *
 * Revision 1.49  2006/01/27 01:15:33  peterbrant
 * Start on better support for different output devices
 *
 * Revision 1.48  2006/01/11 22:16:05  peterbrant
 * Fix NPE when clip region is null
 *
 * Revision 1.47  2006/01/03 17:04:50  peterbrant
 * Many pagination bug fixes / Add ability to position absolute boxes in margin area
 *
 * Revision 1.46  2006/01/01 03:14:24  peterbrant
 * Implement page-break-inside: avoid
 *
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

