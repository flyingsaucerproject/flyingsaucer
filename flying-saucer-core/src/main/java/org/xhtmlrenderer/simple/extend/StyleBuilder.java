package org.xhtmlrenderer.simple.extend;

import org.w3c.dom.Element;

import java.util.regex.Pattern;

import static java.util.Locale.ROOT;

class StyleBuilder {
    private static final Pattern RE_INTEGER = Pattern.compile("\\d+");

    private final StringBuilder style = new StringBuilder();

    void append(Element e, String attributeName, String styleName) {
        String attributeValue = e.getAttribute(attributeName).trim();
        if (!attributeValue.isEmpty()) {
            appendStyle(styleName, attributeValue);
        }
    }

    void appendUrl(Element e, String attributeName, String styleName) {
        String attributeValue = e.getAttribute(attributeName).trim();
        if (!attributeValue.isEmpty()) {
            appendStyle(styleName, "url(" + attributeValue + ")");
        }
    }

    void appendLength(Element e, String attributeName, String styleName) {
        appendLength(e, attributeName, styleName, ";");
    }

    void appendLength(Element e, String attributeName, String styleName, String suffix) {
        String attributeValue = e.getAttribute(attributeName).trim();
        if (!attributeValue.isEmpty()) {
            appendStyle(styleName, convertToLength(attributeValue), suffix);
        }
    }

    void appendStyle(String styleName, String attributeValue) {
        appendStyle(styleName, attributeValue, ";");
    }

    void appendStyle(String styleName, String attributeValue, String suffix) {
        style.append(styleName);
        style.append(attributeValue.toLowerCase(ROOT));
        style.append(suffix);
    }

    void appendWidth(Element e) {
        appendLength(e, "width", "width: ");
    }

    void appendHeight(Element e) {
        appendLength(e, "height", "height: ");
    }

    String convertToLength(String value) {
        return isInteger(value) ? value + "px" : value;
    }

    boolean isInteger(String value) {
        return RE_INTEGER.matcher(value).matches();
    }

    void appendRawStyle(String cssStyle) {
        style.append(cssStyle);
    }

    void applyTableContentAlign(Element e) {
        append(e, "align", "text-align: ");
        append(e, "valign", "vertical-align: ");
    }

    void applyFloatingAlign(Element e) {
        String s = e.getAttribute("align").trim().toLowerCase(ROOT);

        switch (s) {
            case "" -> {}
            case "left", "right" ->
                appendStyle("float: ", s);
            case "center" ->
                appendRawStyle("margin-left: auto; margin-right: auto;");
            default ->
                throw new IllegalArgumentException("Unknown align attribute: '%s'".formatted(s));
        }
    }

    @Override
    public String toString() {
        return style.toString();
    }
}
