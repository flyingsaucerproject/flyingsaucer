package org.xhtmlrenderer.pdf;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.traversal.DocumentTraversal;
import org.w3c.dom.traversal.NodeFilter;
import org.w3c.dom.traversal.NodeIterator;

import org.xhtmlrenderer.pdf.ITextOutputDevice.Bookmark;
import org.xhtmlrenderer.render.Box;

class HTMLOutline {

    private static final Pattern HEADING =
            Pattern.compile("h([1-6])", Pattern.CASE_INSENSITIVE);

    private static final Pattern WS = Pattern.compile("\\s+");

    private final HTMLOutline parent;

    private final int rank;

    private final Bookmark bookmark;

    private HTMLOutline() {
        this(0, "root", null);
    }

    private HTMLOutline(int rank, String title, HTMLOutline parent) {
        this.rank = rank;
        this.bookmark = new Bookmark(title, "");
        this.parent = parent;
        if (parent != null) {
            parent.bookmark.addChild(bookmark);
        }
    }

    /**
     * Include non-heading element as bookmark:
     * <pre>
     * &lt;strong data-pdf-bookmark="4">...&lt;/strong></pre>
     * <p>
     * Specify bookmark name:</p>
     * <pre>
     * &lt;tr data-pdf-bookmark="4" data-pdf-bookmark-name="Bar baz">...&lt;/tr></pre>
     * <p>
     * Exclude individual heading from bookmarks:</p>
     * <pre>
     * &lt;h3 data-pdf-bookmark="none">Baz qux&lt;/h3></pre>
     * <p>
     * Prevent automatic bookmarks for the whole of the document:</p>
     * <pre>
     * &lt;html data-pdf-bookmark="none">...&lt;/html></pre>
     */
    public static List<Bookmark> generate(Element context, Box box) {
        if (context.getAttribute("data-pdf-bookmark").trim().equalsIgnoreCase("none")) {
            return new ArrayList<Bookmark>(0);
        }

        NodeIterator iterator = ((DocumentTraversal) context.getOwnerDocument())
                .createNodeIterator(context, NodeFilter.SHOW_ELEMENT,
                                    NestedSectioningFilter.INSTANCE, true);

        HTMLOutline root = new HTMLOutline();
        HTMLOutline current = root;
        Map<Element,Bookmark> map = new IdentityHashMap();

        for (Element element = (Element) iterator.nextNode();
                element != null; element = (Element) iterator.nextNode()) {
            String bookmark = element.getAttribute("data-pdf-bookmark").trim();
            Matcher matcher = HEADING.matcher(element.getTagName());
            if (bookmark.isEmpty()) {
                bookmark = matcher.matches() ? matcher.group(1) : "none";
            }
            if (bookmark.equalsIgnoreCase("none")) {
                continue;
            }

            int rank;
            try {
                rank = Integer.parseInt(bookmark);
                if (rank < 1) {
                    continue; // Illegal value
                }
            } catch (NumberFormatException e) {
                continue; // Invalid value
            }

            String name = element.getAttribute("data-pdf-bookmark-name").trim();
            if (name.isEmpty()) {
                name = element.getTextContent();
            }
            name = WS.matcher(name.trim()).replaceAll(" ");

            while (current.rank >= rank) {
                current = current.parent;
            }
            current = new HTMLOutline(rank, name, current);
            map.put(element, current.bookmark);
        }
        initBoxPositions(map, box);
        return root.bookmark.getChildren();
    }

    private static void initBoxPositions(Map<Element,Bookmark> map, Box box) {
        Bookmark bookmark = map.get(box.getElement());
        if (bookmark != null) {
            bookmark.setBox(box);
        }
        for (int i = 0, len = box.getChildCount(); i < len; i++) {
            initBoxPositions(map, box.getChild(i));
        }
    }


    private static class NestedSectioningFilter implements NodeFilter {

        static final NestedSectioningFilter INSTANCE = new NestedSectioningFilter();

        // https://www.w3.org/TR/html51/sections.html#sectioning-roots
        private static final Pattern ROOTS = Pattern
                .compile("blockquote|details|fieldset|figure|td",
                         Pattern.CASE_INSENSITIVE);

        @Override
        public short acceptNode(Node n) {
            if (((Element) n).getAttribute("data-pdf-bookmark").equalsIgnoreCase("none")) {
                return FILTER_REJECT;
            }
            // REVISIT: May be use another control "data-pdf-bookmark" value
            // to indicate force traversing into "blockquote" and similar.
            return ROOTS.matcher(n.getNodeName()).matches() ? FILTER_REJECT
                                                            : FILTER_ACCEPT;
        }

    } // class NestedSectioningFilter

}
