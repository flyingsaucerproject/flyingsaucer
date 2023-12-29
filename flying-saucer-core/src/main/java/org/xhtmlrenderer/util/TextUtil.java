package org.xhtmlrenderer.util;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class TextUtil {
    @Nullable
    @CheckReturnValue
    public static String readTextContentOrNull(Element element) {
        String text = readTextContent(element);
        return text.isEmpty() ? null : text;
    }

    @Nonnull
    @CheckReturnValue
    public static String readTextContent(Element element) {
        StringBuilder result = new StringBuilder();
        Node current = element.getFirstChild();
        while (current != null) {
            short nodeType = current.getNodeType();
            if (nodeType == Node.TEXT_NODE || nodeType == Node.CDATA_SECTION_NODE) {
                Text t = (Text) current;
                result.append(t.getData());
            }
            current = current.getNextSibling();
        }
        return result.toString();
    }
}
