package org.xhtmlrenderer.layout;

import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.xhtmlrenderer.layout.breaker.BreakPoint;
import org.xhtmlrenderer.layout.breaker.LineBreakingStrategy;
import org.xhtmlrenderer.render.Box;
import org.xhtmlrenderer.render.InlineLayoutBox;
import org.xhtmlrenderer.render.InlineText;
import org.xhtmlrenderer.render.LineBox;
import org.xhtmlrenderer.resource.XMLResource;
import org.xhtmlrenderer.simple.Graphics2DRenderer;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static java.awt.image.BufferedImage.TYPE_INT_ARGB;
import static org.assertj.core.api.Assertions.assertThat;

/** A line on which no break point of a text fits ends after the first word of the text, with its hyphen. */
class FirstWordLineBreakTest {

    /** Breaks after each space, inside "aaabbbbbb" after "aaa" with a hyphen, and at the end of the text. */
    private static final LineBreakingStrategy SYLLABLES = (text, lang, style) -> {
        List<BreakPoint> points = new ArrayList<>();
        for (int i = 0; i < text.length(); i++) {
            if (text.startsWith("aaabbbbbb", i)) {
                points.add(new BreakPoint(i + 3, "-"));
            } else if (text.charAt(i) == ' ' && i + 1 < text.length()) {
                points.add(new BreakPoint(i + 1));
            }
        }
        points.add(new BreakPoint(text.length()));
        Iterator<BreakPoint> iterator = points.iterator();
        return () -> iterator.hasNext() ? iterator.next() : BreakPoint.DONE;
    };

    /** The first syllable does not fit after "xxxxxxx", and the word does on the next line: it gets no hyphen. */
    @Test
    void aWordWhichMovesToTheNextLineWholeGetsNoHyphen() {
        assertThat(lines(layout("<div style='width: 100px'>xxxxxxx <span>aaabbbbbb</span></div>")))
                .containsExactly("xxxxxxx ", "aaabbbbbb");
    }

    private static Box layout(String body) {
        String html = "<html xmlns='http://www.w3.org/1999/xhtml'><head><style>" +
                "body { margin: 0; font: 16px monospace; }" +
                "</style></head><body>" + body + "</body></html>";
        Document document = XMLResource.load(new StringReader(html)).getDocument();
        Graphics2DRenderer renderer = new Graphics2DRenderer(document, "file:/");
        renderer.getSharedContext().setLineBreakingStrategy(SYLLABLES);
        Graphics2D graphics = new BufferedImage(500, 500, TYPE_INT_ARGB).createGraphics();
        try {
            renderer.layout(graphics, new Dimension(500, 500));
        } finally {
            graphics.dispose();
        }
        return renderer.getPanel().getRootBox();
    }

    /** The text of each line with text in the box tree, in document order. */
    private static List<String> lines(Box root) {
        List<String> lines = new ArrayList<>();
        collectLines(root, lines);
        return lines;
    }

    private static void collectLines(Box box, List<String> lines) {
        if (box instanceof LineBox) {
            StringBuilder text = new StringBuilder();
            collectText(box, text);
            if (!text.isEmpty()) {
                lines.add(text.toString());
            }
            return;
        }
        for (Box child : box.getChildren()) {
            collectLines(child, lines);
        }
    }

    private static void collectText(Box box, StringBuilder text) {
        if (box instanceof InlineLayoutBox inline) {
            for (Object child : inline.getInlineChildren()) {
                if (child instanceof InlineText inlineText) {
                    text.append(inlineText.getSubstring());
                } else if (child instanceof Box childBox) {
                    collectText(childBox, text);
                }
            }
        }
        for (Box child : box.getChildren()) {
            collectText(child, text);
        }
    }
}
