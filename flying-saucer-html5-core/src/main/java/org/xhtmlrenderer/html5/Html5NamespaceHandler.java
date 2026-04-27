package org.xhtmlrenderer.html5;

import com.google.errorprone.annotations.CheckReturnValue;
import org.jspecify.annotations.Nullable;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xhtmlrenderer.css.sheet.StylesheetInfo;
import org.xhtmlrenderer.simple.extend.XhtmlNamespaceHandler;
import org.xhtmlrenderer.util.Configuration;

import java.net.URL;
import java.util.Locale;
import java.util.Optional;

import static java.util.Objects.requireNonNull;
import static org.xhtmlrenderer.css.sheet.StylesheetInfo.Origin.USER_AGENT;
import static org.xhtmlrenderer.css.sheet.StylesheetInfo.mediaTypes;

/**
 * Namespace handler for HTML5 documents parsed via {@link Html5Parser}.
 *
 * <p>Differences from {@link XhtmlNamespaceHandler}:
 * <ul>
 *   <li>Loads {@code Html5NamespaceHandler.css} as the user-agent stylesheet, which adds
 *       {@code display:block} for HTML5 semantic elements (article, aside, footer, header,
 *       main, nav, section, figure, figcaption, details, summary, dialog) and styles for
 *       mark, time, video, audio, canvas, meter, progress, and output.</li>
 *   <li>Handles the {@code hidden} boolean attribute by applying {@code display:none}.</li>
 *   <li>Handles {@code align} on HTML5 block elements (article, aside, header, footer,
 *       main, nav, section, figure).</li>
 *   <li>Recognises HTML5's {@code id} attribute on {@code <a>} as an anchor name
 *       (HTML5 dropped the {@code name} attribute).</li>
 *   <li>Recognises {@code <picture>} as an image container and resolves its first
 *       {@code <img>} child as the image source URI.</li>
 * </ul>
 */
public class Html5NamespaceHandler extends XhtmlNamespaceHandler {

    @Nullable
    private static volatile StylesheetInfo defaultStylesheet;

    @Override
    @CheckReturnValue
    public Optional<StylesheetInfo> getDefaultStylesheet() {
        if (defaultStylesheet == null) {
            synchronized (Html5NamespaceHandler.class) {
                if (defaultStylesheet == null) {
                    String path = Configuration.valueFor("xr.css.user-agent-default-css") + "Html5NamespaceHandler.css";
                    URL url = requireNonNull(
                            getClass().getResource(path),
                            () -> "Html5NamespaceHandler.css not found on classpath at: " + path);
                    defaultStylesheet = new StylesheetInfo(USER_AGENT, url.toString(), mediaTypes(""), null);
                }
            }
        }
        return Optional.of(defaultStylesheet);
    }

    @Override
    @CheckReturnValue
    public String getNonCssStyling(Element e) {
        if (e.hasAttribute("hidden")) {
            return "display: none;";
        }
        String tag = e.getNodeName().toLowerCase(Locale.ROOT);
        return switch (tag) {
            case "article", "aside", "footer", "header", "main", "nav", "section", "figure" ->
                    blockAlign(e);
            default -> super.getNonCssStyling(e);
        };
    }

    /** HTML5 dropped the {@code name} attribute on anchors; recognise {@code id} as well. */
    @Override
    @Nullable
    @CheckReturnValue
    public String getAnchorName(@Nullable Element e) {
        if (e == null) return null;
        String name = super.getAnchorName(e);
        if (name != null) return name;
        if ("a".equalsIgnoreCase(e.getNodeName()) && e.hasAttribute("id")) {
            return e.getAttribute("id");
        }
        return null;
    }

    /** {@code <picture>} is treated as an image container; its first {@code <img>} child supplies the URI. */
    @Override
    @CheckReturnValue
    public boolean isImageElement(Element e) {
        return "picture".equalsIgnoreCase(e.getNodeName()) || super.isImageElement(e);
    }

    @Override
    @Nullable
    @CheckReturnValue
    public String getImageSourceURI(Element e) {
        if ("picture".equalsIgnoreCase(e.getNodeName())) {
            NodeList children = e.getChildNodes();
            for (int i = 0; i < children.getLength(); i++) {
                Node child = children.item(i);
                if (child.getNodeType() == Node.ELEMENT_NODE
                        && "img".equalsIgnoreCase(child.getNodeName())) {
                    return ((Element) child).getAttribute("src");
                }
            }
            return null;
        }
        return super.getImageSourceURI(e);
    }

    private static String blockAlign(Element e) {
        String align = e.getAttribute("align").trim().toLowerCase(Locale.ROOT);
        return switch (align) {
            case "left", "right", "center", "justify" -> "text-align: " + align + ";";
            default -> "";
        };
    }
}
