/*
 * {{{ header & license
 * Copyright (c) Nick Reddel
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
package org.xhtmlrenderer.swing;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.logging.Level;

import javax.swing.AbstractAction;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;
import org.w3c.dom.ranges.DocumentRange;
import org.w3c.dom.ranges.Range;
import org.w3c.dom.traversal.DocumentTraversal;
import org.w3c.dom.traversal.NodeFilter;
import org.w3c.dom.traversal.NodeIterator;
import org.xhtmlrenderer.css.style.CalculatedStyle;
import org.xhtmlrenderer.layout.LayoutContext;
import org.xhtmlrenderer.render.BlockBox;
import org.xhtmlrenderer.render.Box;
import org.xhtmlrenderer.render.InlineLayoutBox;
import org.xhtmlrenderer.render.InlineText;
import org.xhtmlrenderer.simple.XHTMLPanel;
import org.xhtmlrenderer.util.Util;
import org.xhtmlrenderer.util.XRLog;

/**
 * <p>
 * A simple Selection and Highlighter class for
 * {@link org.xhtmlrenderer.simple.XHTMLPanel}.
 * </p>
 * <p>
 * The current selection is available as a DOM Range via <a
 * href="#getSelectionRange()">getSelectionRange</a>. There is also a Swing
 * action to copy the selection contents to the clipboard:
 * {@link org.xhtmlrenderer.swing.SelectionHighlighter.CopyAction}, which
 * should be installed on the SelectionHighlighter
 * </p>
 * <p>
 * Usage: create the XHTMLPanel, create an instance
 * of this class then call <a
 * href="#install(org.xhtmlrenderer.simple.XHTMLPanel)">install</a>. See also:
 * /demos/samples/src/SelectionHighlighterTest.java
 * </p>
 * 
 * With thanks to Swing's <code>DefaultCaret</code>
 * 
 * @author Nick Reddel
 */
public class SelectionHighlighter implements MouseMotionListener, MouseListener {

    private static final String PARA_EQUIV = "&!<p2equiv!";

    private XHTMLPanel panel;

    private ViewModelInfo dotInfo;

    private ViewModelInfo markInfo;

    protected EventListenerList listenerList = new EventListenerList();

    protected transient ChangeEvent changeEvent = null;

    private DocumentRange docRange;

    // private List lastModified = new ArrayList();

    private Range lastSelectionRange = null;

    private DocumentTraversal docTraversal;

    private Map elementBoxMap;

    private Map textInlineMap;

    private String lastHighlightedString = "";

    private TransferHandler handler;

    private Document document;

    public void addChangeListener(ChangeListener l) {
        listenerList.add(ChangeListener.class, l);
    }

    public void removeChangeListener(ChangeListener l) {
        listenerList.remove(ChangeListener.class, l);
    }

    protected void fireStateChanged() {
        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == ChangeListener.class) {
                // Lazily create the event:
                if (changeEvent == null)
                    changeEvent = new ChangeEvent(this);
                ((ChangeListener) listeners[i + 1]).stateChanged(changeEvent);
            }
        }
    }

    public void install(XHTMLPanel panel) {
        this.panel = panel;
        if (!checkDocument()) {
            return;
        }

        panel.setTransferHandler(handler);
        panel.addMouseListener(this);
        panel.addMouseMotionListener(this);
    }

    public void deinstall(XHTMLPanel panel) {

        if (panel.getTransferHandler() == handler) {
            panel.setTransferHandler(null);
        }
        panel.removeMouseListener(this);
        panel.removeMouseMotionListener(this);

    }

    private boolean checkDocument() {
        while (true) {
            if (this.document != panel.getDocument() || textInlineMap == null) {
                this.document = panel.getDocument();
                textInlineMap = null;
                this.dotInfo = null;
                this.markInfo = null;
                this.lastSelectionRange = null;
                try {
                    this.docRange = (DocumentRange) panel.getDocument();
                    this.docTraversal = (DocumentTraversal) panel.getDocument();
                    if (this.document != null && this.createMaps()) {
                        return true;
                    }
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        return false;
                    }
                } catch (ClassCastException cce) {
                    XRLog.layout(Level.WARNING,
                            "Document instance cannot create ranges: no selection possible");
                    return false;
                }
            }
            return true;
        }
    }

    public void setDot(ViewModelInfo pos) {
        this.dotInfo = pos;
        this.markInfo = pos;
        fireStateChanged();
        updateHighlights();
        updateSystemSelection();

    }

    public void mouseDragged(MouseEvent e) {
        if ((!e.isConsumed()) && SwingUtilities.isLeftMouseButton(e)) {
            moveCaret(convertMouseEventToScale(e));
        }

    }

    public void mouseMoved(MouseEvent e) {
    }

    public void mouseClicked(MouseEvent e) {
        // TODO: double-triple click handler
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    public void mousePressed(MouseEvent e) {
        int nclicks = e.getClickCount();

        if (SwingUtilities.isLeftMouseButton(e)) {
            if (e.isConsumed()) {
            } else {
                adjustCaretAndFocus(e);
                MouseEvent newE = convertMouseEventToScale(e);
                adjustCaretAndFocus(newE);
                if (nclicks == 2) {
                    selectWord(newE);
                }
            }
        }

    }

    void adjustCaretAndFocus(MouseEvent e) {
        adjustCaret(e);
        adjustFocus(false);
    }

    /**
     * Adjusts the caret location based on the MouseEvent.
     */
    private void adjustCaret(MouseEvent e) {
        if ((e.getModifiers() & ActionEvent.SHIFT_MASK) != 0 && this.dotInfo != null) {
            moveCaret(e);
        } else {
            positionCaret(e);
        }
    }

    private void positionCaret(MouseEvent e) {
        ViewModelInfo pos = infoFromPoint(e);
        if (pos != null) {
            setDot(pos);
        }
    }

    /**
     * Adjusts the focus, if necessary.
     * 
     * @param inWindow
     *            if true indicates requestFocusInWindow should be used
     */
    private void adjustFocus(boolean inWindow) {
        if ((panel != null) && panel.isEnabled() && panel.isRequestFocusEnabled()) {
            if (inWindow) {
                panel.requestFocusInWindow();
            } else {
                panel.requestFocus();
            }
        }
    }

    private void selectWord(MouseEvent e) {
        // TODO Auto-generated method stub

    }

    public void mouseReleased(MouseEvent e) {
        // TODO Auto-generated method stub

    }

    public XHTMLPanel getComponent() {
        return this.panel;
    }

    protected void moveCaret(MouseEvent e) {
        ViewModelInfo pos = infoFromPoint(e);
        if (pos != null) {
            moveDot(pos);
        }
    }

    public void selectAll() {
        if (this.getComponent() == null || this.getComponent().getWidth() == 0
                || this.getComponent().getHeight() == 0) {
            return;
        }
        checkDocument();
        NodeIterator nodeIterator = this.docTraversal.createNodeIterator(this.document
                .getDocumentElement(), NodeFilter.SHOW_TEXT, null, false);
        Text firstText = null;
        Text lastText = null;
        while (true) {
            Node n = nodeIterator.nextNode();
            if (n == null) {
                break;
            }
            if (!textInlineMap.containsKey(n)) {
                continue;
            }
            lastText = (Text) n;
            if (firstText == null) {
                firstText = lastText;
            }
        }
        if (firstText == null) {
            return;
        }
        Range r = docRange.createRange();
        r.setStart(firstText, 0);
        ViewModelInfo firstPoint = new ViewModelInfo(r, (InlineText) ((List) textInlineMap
                .get(firstText)).get(0));
        r = docRange.createRange();
        try {
            // possibly some dom impls don't handle this?
            r.setStart(lastText, lastText.getLength());
        } catch (Exception e) {
            r.setStart(lastText, Math.max(0, lastText.getLength() - 1));
        }
        List l = (List) textInlineMap.get(firstText);

        ViewModelInfo lastPoint = new ViewModelInfo(r, (InlineText) l.get(l.size() - 1));
        setDot(firstPoint);
        moveDot(lastPoint);

    }

    public void moveDot(ViewModelInfo pos) {
        this.dotInfo = pos;
        if (this.markInfo == null) {
            this.markInfo = pos;
        }
        fireStateChanged();
        updateHighlights();
        updateSystemSelection();
        InlineText iT = this.dotInfo.text;
        InlineLayoutBox iB = iT.getParent();
        adjustVisibility(new Rectangle(iB.getAbsX() + iT.getX(), iB.getAbsY(), 1, iB.getBaseline()));

    }

    private void updateHighlights() {

        List modified = new ArrayList();
        StringBuffer hlText = new StringBuffer();
        if (this.dotInfo == null) {
            getComponent().getRootBox().clearSelection(modified);
            getComponent().repaint();
            lastHighlightedString = "";
            return;
        }
        Range range = getSelectionRange();

        if (lastSelectionRange != null
                && range.compareBoundaryPoints(Range.START_TO_START, lastSelectionRange) == 0
                && range.compareBoundaryPoints(Range.END_TO_END, lastSelectionRange) == 0) {
            return;
        }
        lastHighlightedString = "";
        lastSelectionRange = range.cloneRange();

        if (range.compareBoundaryPoints(Range.START_TO_END, range) == 0) {
            getComponent().getRootBox().clearSelection(modified);
        } else {
            boolean endBeforeStart = (this.markInfo.range.compareBoundaryPoints(
                    Range.START_TO_START, this.dotInfo.range) >= 0);
            // TODO: track modifications
            getComponent().getRootBox().clearSelection(modified);
            InlineText t1 = (endBeforeStart) ? this.dotInfo.text : this.markInfo.text;
            InlineText t2 = (!endBeforeStart) ? this.dotInfo.text : this.markInfo.text;
            if (t1 == null || t2 == null) {
                // TODO: need general debug here (never print to system.err; use XRLog instead)
                // TODO: is this just a warning, or should we bail out
                XRLog.general(Level.FINE, "null text node");
            }

            final Range acceptRange = docRange.createRange();
            final Range tr = range;
            NodeFilter f = new NodeFilter() {
                public short acceptNode(Node n) {
                    acceptRange.setStart(n, 0);
                    if (tr.getStartContainer() == n) {
                        return FILTER_ACCEPT;
                    }
                    if ((acceptRange.compareBoundaryPoints(Range.START_TO_START, tr) < 0 || acceptRange
                            .compareBoundaryPoints(Range.END_TO_START, tr) > 0)
                            && n != tr.getStartContainer() && n != tr.getEndContainer()) {
                        return NodeFilter.FILTER_SKIP;
                    }

                    return NodeFilter.FILTER_ACCEPT;
                }

            };
            NodeIterator nodeIterator = this.docTraversal.createNodeIterator(range
                    .getCommonAncestorContainer(), NodeFilter.SHOW_ELEMENT | NodeFilter.SHOW_TEXT
                    | NodeFilter.SHOW_CDATA_SECTION, f, false);
            Box box;
            boolean lastNodeWasBox = false;
            for (Node n = nodeIterator.nextNode(); n != null; n = nodeIterator.nextNode()) {
                if (n.getNodeType() == Node.ELEMENT_NODE) {
                    box = getBoxForElement((Element) n);
                    if (box instanceof BlockBox && !lastNodeWasBox) {
                        hlText.append(PARA_EQUIV);
                        lastNodeWasBox = true;
                    } else {
                        lastNodeWasBox = false;
                    }
                } else {
                    lastNodeWasBox = false;
                    Text t = (Text) n;
                    List iTs = getInlineTextsForText(t);
                    if (iTs == null) {
                        // shouldn't happen
                        continue;
                    }
                    int selTxtSt = (t == range.getStartContainer()) ? range.getStartOffset() : 0;
                    int selTxtEnd = (t == range.getEndContainer()) ? range.getEndOffset() : t
                            .getNodeValue().length();

                    hlText.append(t.getNodeValue().substring(selTxtSt, selTxtEnd));
                    for (Iterator itr = iTs.iterator(); itr.hasNext();) {
                        InlineText iT = (InlineText) itr.next();

                        iT.setSelectionStart((short) Math.max(0, Math.min(selTxtSt, iT.getEnd())
                                - iT.getStart()));
                        iT.setSelectionEnd((short) Math.max(0, Math.min(iT.getEnd(), selTxtEnd)
                                - iT.getStart()));

                    }
                }
            }
        }
        String s = normalizeSpaces(hlText.toString());
        getComponent().repaint();
        lastHighlightedString = Util.replace(s, PARA_EQUIV, "\n\n");
        // lastModified = modified;
    }

    public String normalizeSpaces(String s) {
        if (s == null)
            return null;
        StringBuffer buf = new StringBuffer();
        CharacterIterator iter = new StringCharacterIterator(s);
        boolean inWhitespace = false; // Flag set if we're in a second
        // consecutive whitespace
        for (char c = iter.first(); c != CharacterIterator.DONE; c = iter.next()) {
            if (Character.isWhitespace(c)) {
                if (!inWhitespace) {
                    buf.append(' ');
                    inWhitespace = true;
                }
            } else {
                inWhitespace = false;
                buf.append(c);
            }
        }
        return buf.toString();
    }

    private Box getElementContainerBox(InlineText t) {
        Box b = t.getParent();
        while (b.getElement() == null) {
            b = b.getParent();
        }
        return b;
    }

    private boolean createMaps() {
        if (panel.getRootBox() == null) {
            return false;
        }
        textInlineMap = new LinkedHashMap();
        elementBoxMap = new HashMap();
        Stack s = new Stack();
        s.push(panel.getRootBox());
        while (!s.empty()) {
            Box b = (Box) s.pop();
            Element element = b.getElement();
            if (element != null && !elementBoxMap.containsKey(element)) {
                elementBoxMap.put(element, b);
            }
            if (b instanceof InlineLayoutBox) {
                InlineLayoutBox ilb = (InlineLayoutBox) b;
                for (Iterator it = ilb.getInlineChildren().iterator(); it.hasNext();) {
                    Object o = it.next();
                    if (o instanceof InlineText) {
                        InlineText t = (InlineText) o;
                        Text txt = t.getTextNode();
                        if (!textInlineMap.containsKey(txt)) {
                            textInlineMap.put(txt, new ArrayList());
                        }
                        ((List) textInlineMap.get(txt)).add(t);
                    } else {
                        s.push((Box) o);
                    }
                }
            } else {
                Iterator childIterator = b.getChildIterator();
                while (childIterator.hasNext()) {
                    s.push(childIterator.next());
                }
            }
        }
        return true;

    }

    private List getInlineTextsForText(Text t) {
        return (List) textInlineMap.get(t);
    }

    private Box getBoxForElement(Element elt) {
        return (Box) elementBoxMap.get(elt);
    }

    private void updateSystemSelection() {
        if (this.dotInfo != this.markInfo && panel != null) {
            Clipboard clip = panel.getToolkit().getSystemSelection();
            if (clip != null) {
                String selectedText = lastHighlightedString;
                try {
                    clip.setContents(new StringSelection(selectedText), null);

                } catch (IllegalStateException ise) {
                    // clipboard was unavailable
                    // no need to provide error feedback to user since updating
                    // the system selection is not a user invoked action
                }
            }
        }

    }

    void copy() {
        if (this.dotInfo != this.markInfo && panel != null) {
            Clipboard clip = panel.getToolkit().getSystemClipboard();
            if (clip != null) {
                String selectedText = lastHighlightedString;
                try {
                    clip.setContents(new StringSelection(selectedText), null);

                } catch (IllegalStateException ise) {
                    // clipboard was unavailable
                    // no need to provide error feedback to user since updating
                    // the system selection is not a user invoked action
                }
            }
        }
    }

    List getInlineLayoutBoxes(Box b, boolean ignoreChildElements) {
        Stack boxes = new Stack();
        List ilbs = new ArrayList();
        boxes.push(b);
        while (!boxes.empty()) {
            b = (Box) boxes.pop();
            if (b instanceof InlineLayoutBox) {
                ilbs.add((InlineLayoutBox) b);
            } else {
                for (Iterator it = b.getChildIterator(); it.hasNext();) {
                    Box child = (Box) it.next();
                    if (!ignoreChildElements || child.getElement() == null) {
                        boxes.push(child);
                    }
                }
            }
        }
        return ilbs;
    }

    ViewModelInfo infoFromPoint(MouseEvent e) {
        checkDocument();
        Range r = docRange.createRange();
        InlineText fndTxt = null;
        Box box = panel.getRootLayer().find(panel.getLayoutContext(), e.getX(), e.getY(), true);
        if (box == null) {
            return null;
        }
        Element elt = null;
        int offset = 0;
        InlineLayoutBox ilb = null;
        boolean containsWholeIlb = false;
        if (box instanceof InlineLayoutBox) {
            ilb = (InlineLayoutBox) box;
        } else {
            for (; ilb == null;) {
                List ilbs = getInlineLayoutBoxes(box, false);
                for (int i = ilbs.size() - 1; i >= 0; i--) {
                    InlineLayoutBox ilbt = (InlineLayoutBox) ilbs.get(i);
                    if (ilbt.getAbsY() <= e.getY() && ilbt.getAbsX() <= e.getX()) {
                        if (ilb == null || (ilbt.getAbsY() > ilb.getAbsY())
                                || (ilbt.getAbsY() == ilb.getAbsY() && ilbt.getX() > ilb.getX())) {

                            if (ilbt.isContainsVisibleContent()) {
                                boolean hasDecentTextNode = false;
                                int x = ilbt.getAbsX();

                                for (Iterator it = ilbt.getInlineChildren().iterator(); it
                                        .hasNext();) {
                                    Object o = it.next();
                                    if (o instanceof InlineText) {
                                        InlineText txt = (InlineText) o;
                                        if (txt.getTextNode() != null) {
                                            hasDecentTextNode = true;
                                            break;
                                        }
                                    }
                                }
                                if (hasDecentTextNode) {
                                    ilb = ilbt;
                                }
                            }
                        }
                        containsWholeIlb = true;
                    }
                }
                if (ilb == null) {
                    if (box.getParent() == null) {
                        return null;
                    }
                    box = box.getParent();
                }
            }
        }
        int x = ilb.getAbsX();
        InlineText lastItxt = null;
        for (Iterator it = ilb.getInlineChildren().iterator(); it.hasNext();) {
            Object o = it.next();
            if (o instanceof InlineText) {
                InlineText txt = (InlineText) o;
                if (txt.getTextNode() != null) {
                    if ((e.getX() >= x + txt.getX() && e.getX() < x + txt.getX() + txt.getWidth())
                            || containsWholeIlb) {
                        fndTxt = txt;
                        break;
                    } else {
                        if (e.getX() < x + txt.getX()) {
                            // assume inline image or somesuch
                            if (lastItxt != null) {
                                fndTxt = lastItxt;
                                break;
                            }
                        }
                    }
                }
                lastItxt = txt;
            }
        }

        LayoutContext lc = panel.getLayoutContext();
        if (fndTxt == null) {
            // TODO: need general debug flag here; not sure if this is an error condition and if the logging is necessary
            if (false) {
                XRLog.general(Level.FINE, ilb.dump(lc, "", Box.DUMP_RENDER));
                XRLog.general(Level.FINE, ilb.getParent().dump(lc, "", Box.DUMP_RENDER));
                XRLog.general(Level.FINE, ilb.getParent().getParent().dump(lc, "", Box.DUMP_RENDER));
            }
            return null;
        }

        String txt = fndTxt.getMasterText();

        CalculatedStyle style = ilb.getStyle();
        if (containsWholeIlb) {
            offset = fndTxt.getEnd();
        } else {
            for (offset = fndTxt.getStart(); offset < fndTxt.getEnd(); offset++) {
                int w = getTextWidth(lc, style, txt.substring(fndTxt.getStart(), offset + 1));
                if (w + x + fndTxt.getX() > e.getX()) {
                    break;
                }

            }
        }

        Node node = fndTxt.getTextNode();
        try {
            r.setStart(node, offset);
        } catch (Exception ex) {
            // maybe differs for dom impl? anyway, fix for issue 216
            r.setStart(node, ((Text) node).getLength() - 1);
        }
        return new ViewModelInfo(r, fndTxt);

    }

    private int getTextWidth(LayoutContext c, CalculatedStyle cs, String s) {
        return c.getTextRenderer().getWidth(c.getFontContext(), c.getFont(cs.getFont(c)), s);
    }

    public Range getSelectionRange() {
        if (this.dotInfo == null || this.dotInfo.range == null) {
            return null;
        }
        Range r = docRange.createRange();
        // some xml parsers don't allow end<start in the same text node. So,
        // handle dot<mark here

        if (this.markInfo.range.compareBoundaryPoints(Range.START_TO_START, this.dotInfo.range) <= 0) {
            r.setStart(this.markInfo.range.getStartContainer(), this.markInfo.range
                    .getStartOffset());
            r.setEnd(this.dotInfo.range.getStartContainer(), this.dotInfo.range.getStartOffset());
        } else {
            r.setStart(this.dotInfo.range.getStartContainer(), this.dotInfo.range.getStartOffset());
            r.setEnd(this.markInfo.range.getStartContainer(), this.markInfo.range.getStartOffset());
        }
        return r;
    }

    protected void adjustVisibility(Rectangle nloc) {
        if (panel == null) {
            return;
        }
        if (SwingUtilities.isEventDispatchThread()) {
            panel.scrollRectToVisible(nloc);
        } else {
            SwingUtilities.invokeLater(new SafeScroller(nloc));
        }
    }

    protected MouseEvent convertMouseEventToScale(MouseEvent e) {
        if (!e.isConsumed() && panel instanceof ScalableXHTMLPanel) {
            Point newP = ((ScalableXHTMLPanel) panel).convertFromScaled(e.getX(), e.getY());
            MouseEvent newE = new MouseEvent(
                    e.getComponent(),
                    e.getID(),
                    e.getWhen(),
                    e.getModifiersEx(),
                    (int) newP.getX(),
                    (int) newP.getY(),
                    e.getClickCount(),
                    e.isPopupTrigger(),
                    e.getButton()
            );
            return newE;
        }
        return e;
    }

    public void setHandler(TransferHandler handler) {
        this.handler = handler;
    }

    public class ViewModelInfo {
        Range range;

        InlineText text;

        ViewModelInfo(Range range, InlineText text) {
            this.range = range;
            this.text = text;

        }

        public String toString() {
            return range.getStartContainer() + ":" + range.getStartOffset();
        }

        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ViewModelInfo)) return false;

            ViewModelInfo that = (ViewModelInfo) o;

            if (!range.equals(that.range)) return false;
            if (!text.equals(that.text)) return false;

            return true;
        }

        public int hashCode() {
            int result = range.hashCode();
            result = 31 * result + text.hashCode();
            return result;
        }

        public boolean canCopy() {
            return lastHighlightedString.length() != 0;
        }

    }

    public static final String copyAction = "Copy";

    public static class CopyAction extends AbstractAction {

        private SelectionHighlighter caret;

        /** Create this object with the appropriate identifier. */
        public CopyAction() {
            super(copyAction);
        }

        public void install(SelectionHighlighter caret) {
            this.caret = caret;

        }

        /**
         * The operation to perform when this action is triggered.
         * 
         * @param e
         *            the action event
         */
        public void actionPerformed(ActionEvent e) {
            if (caret != null) {
                caret.copy();
            }
        }
    }

    class SafeScroller implements Runnable {

        SafeScroller(Rectangle r) {
            this.r = r;
        }

        public void run() {
            if (panel != null) {
                panel.scrollRectToVisible(r);
            }
        }

        Rectangle r;
    }
}
