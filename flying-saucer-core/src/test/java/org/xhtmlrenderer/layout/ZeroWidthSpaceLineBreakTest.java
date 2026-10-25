package org.xhtmlrenderer.layout;

import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.xhtmlrenderer.render.BlockBox;
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
import java.util.List;

import static java.awt.image.BufferedImage.TYPE_INT_ARGB;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * A line breaks after U+200B ZERO WIDTH SPACE, see
 * <a href="https://github.com/flyingsaucerproject/flyingsaucer/issues/753">issue 753</a>: Unicode requires a break
 * there (UAX #14, LB8), and CSS Text 3 names it as a way to mark a place to wrap, as {@code <wbr>} does.
 */
class ZeroWidthSpaceLineBreakTest {

    @Test
    void theNarrowestCellBreaksAfterAZeroWidthSpace() {
        assertThat(lines(layout("<table style='width: 1px'><tr><td>aaa&#8203;bbb</td></tr></table>")))
                .containsExactly("aaa\u200B", "bbb");
    }

    /** The narrowest cell is as wide as the wider part, as with {@code <wbr>}, not as the whole word. */
    @Test
    void theNarrowestCellIsAsWideAsThePartOfTheWord() {
        Box root = layout("<table style='width: 1px'><tr>" +
                "<td>aaabbb</td><td>aaa&#8203;bbb</td><td>aaa<wbr/>bbb</td></tr></table>");

        List<BlockBox> cells = cells(root);
        assertThat(cells).hasSize(3);
        assertThat(cells.get(1).getContentWidth())
                .isLessThan(cells.get(0).getContentWidth())
                .isEqualTo(cells.get(2).getContentWidth());
    }

    @Test
    void aLineBreaksAfterAZeroWidthSpace() {
        assertThat(lines(layout("<div style='width: 60px'>xx aaa&#8203;bbb</div>")))
                .containsExactly("xx aaa\u200B", "bbb");
    }

    private static Box layout(String body) {
        String html = "<html xmlns='http://www.w3.org/1999/xhtml'><head><style>" +
                "body { margin: 0; font: 16px sans-serif; } table { border-collapse: collapse; } td { padding: 0; }" +
                "</style></head><body>" + body + "</body></html>";
        Document document = XMLResource.load(new StringReader(html)).getDocument();
        Graphics2DRenderer renderer = new Graphics2DRenderer(document, "file:/");
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

    private static List<BlockBox> cells(Box root) {
        List<BlockBox> cells = new ArrayList<>();
        collectCells(root, cells);
        return cells;
    }

    private static void collectCells(Box box, List<BlockBox> cells) {
        if (box instanceof BlockBox block && box.getElement() != null && "td".equals(box.getElement().getNodeName())) {
            cells.add(block);
            return;
        }
        for (Box child : box.getChildren()) {
            collectCells(child, cells);
        }
    }
}
