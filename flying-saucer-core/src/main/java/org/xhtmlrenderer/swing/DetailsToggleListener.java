package org.xhtmlrenderer.swing;

import com.google.errorprone.annotations.CheckReturnValue;
import org.jspecify.annotations.Nullable;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xhtmlrenderer.extend.NamespaceHandler;
import org.xhtmlrenderer.layout.SharedContext;
import org.xhtmlrenderer.render.Box;

/**
 * Expands or collapses a {@code <details>} element when the user clicks its {@code <summary>}
 * in a {@link BasicPanel}, by toggling the {@code open} attribute and re-building the box tree.
 * <p>
 * Clicks on a link inside the summary are left to {@link LinkListener}.
 * <p>
 * See <a href="https://github.com/flyingsaucerproject/flyingsaucer/issues/500">issue 500</a>
 */
public class DetailsToggleListener extends DefaultFSMouseListener {
    @Override
    public void onMouseUp(BasicPanel panel, Box box) {
        Element element = box.getElement();
        if (element == null) {
            return;
        }

        Element details = findToggledDetails(panel.getSharedContext().getNamespaceHandler(), element);
        if (details != null) {
            toggle(panel, details);
        }
    }

    /**
     * Toggles the {@code open} attribute of given {@code <details>} element and re-renders the panel
     * (keeping the scroll position).
     */
    public void toggle(BasicPanel panel, Element details) {
        if (details.hasAttribute("open")) {
            details.removeAttribute("open");
        } else {
            details.setAttribute("open", "open");
        }

        // selectors like "details[open] > summary" must be re-matched
        restyle(panel.getSharedContext(), details);
        panel.setRootBox(null);
        panel.repaint();
    }

    private static void restyle(SharedContext sharedContext, Element element) {
        var unused = sharedContext.getStyle(element, true);
        for (Node child = element.getFirstChild(); child != null; child = child.getNextSibling()) {
            if (child instanceof Element childElement) {
                restyle(sharedContext, childElement);
            }
        }
    }

    /**
     * @return the {@code <details>} element whose (first) {@code <summary>} contains the given element,
     * or null if the given element is not inside a summary, or is inside a link.
     */
    @Nullable
    @CheckReturnValue
    static Element findToggledDetails(NamespaceHandler namespaceHandler, Element element) {
        for (Node node = element; node instanceof Element current; node = node.getParentNode()) {
            if (namespaceHandler.getLinkUri(current) != null) {
                return null;
            }
            if (is(current, "summary")) {
                return current.getParentNode() instanceof Element parent && is(parent, "details") && findSummary(parent) == current
                    ? parent : null;
            }
        }
        return null;
    }

    @Nullable
    @CheckReturnValue
    private static Element findSummary(Element details) {
        for (Node node = details.getFirstChild(); node != null; node = node.getNextSibling()) {
            if (node instanceof Element child && is(child, "summary")) {
                return child;
            }
        }
        return null;
    }

    private static boolean is(Element element, String tagName) {
        String localName = element.getLocalName() != null ? element.getLocalName() : element.getTagName();
        return tagName.equalsIgnoreCase(localName);
    }
}
