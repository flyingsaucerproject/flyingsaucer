package org.xhtmlrenderer.layout;

import org.jspecify.annotations.Nullable;
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
import java.util.List;
import java.util.stream.Stream;

import static java.awt.image.BufferedImage.TYPE_INT_ARGB;
import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * A line which wraps loses its trailing collapsible space whatever its alignment, see
 * <a href="https://github.com/flyingsaucerproject/flyingsaucer/issues/779">issue 779</a>: a collapsible space at the
 * end of a line is removed (CSS Text 3, § 4.1.3).
 */
class TrailingSpaceLineBreakTest {

    private static final String SPAN = "<span>aaaa bbbb</span>";

    @Test
    void aRightAlignedLineEndsAtTheRightEdge() {
        BlockBox block = layout("<div style='width: 50px; text-align: right'>" + SPAN + "</div>");

        List<LineBox> lines = lineBoxes(block);
        assertThat(lines).extracting(TrailingSpaceLineBreakTest::text).containsExactly("aaaa", "bbbb");
        assertThat(lines).allSatisfy(line ->
                assertThat(line.getX() + line.getWidth()).isEqualTo(block.getContentWidth()));
    }

    @Test
    void aCenteredLineHasEqualSpaceOnBothSides() {
        BlockBox block = layout("<div style='width: 50px; text-align: center'>" + SPAN + "</div>");

        List<LineBox> lines = lineBoxes(block);
        assertThat(lines).extracting(TrailingSpaceLineBreakTest::text).containsExactly("aaaa", "bbbb");
        assertThat(lines.get(0).getX()).isEqualTo(lines.get(1).getX());
        assertThat(lines.get(0).getWidth()).isEqualTo(lines.get(1).getWidth());
    }

    @Test
    void aLeftAlignedLineLosesItsTrailingSpace() {
        BlockBox block = layout("<div style='width: 50px'>" + SPAN + "</div>");

        assertThat(lineBoxes(block)).extracting(TrailingSpaceLineBreakTest::text).containsExactly("aaaa", "bbbb");
    }

    /** {@code pre-line} collapses spaces as {@code normal} does: the trailing space is removed too. */
    @Test
    void aPreLineLineEndsAtTheRightEdge() {
        BlockBox block = layout("<div style='width: 50px; text-align: right; white-space: pre-line'>" + SPAN + "</div>");

        List<LineBox> lines = lineBoxes(block);
        assertThat(lines).extracting(TrailingSpaceLineBreakTest::text).containsExactly("aaaa", "bbbb");
        assertThat(lines).allSatisfy(line ->
                assertThat(line.getX() + line.getWidth()).isEqualTo(block.getContentWidth()));
    }

    /** A preserved space hangs at the end of the line: it stays. */
    @Test
    void aPreservedSpaceStaysAtTheEndOfTheLine() {
        BlockBox block = layout("<div style='width: 50px; text-align: right; white-space: pre-wrap'>" + SPAN + "</div>");

        assertThat(lineBoxes(block)).extracting(TrailingSpaceLineBreakTest::text).containsExactly("aaaa ", "bbbb");
    }

    /** The first {@code div} in the body, laid out. */
    private static BlockBox layout(String body) {
        String html = "<html xmlns='http://www.w3.org/1999/xhtml'><head><style>" +
                "body { margin: 0; font: 16px monospace; }" +
                "</style></head><body>" + body + "</body></html>";
        Document document = XMLResource.load(new StringReader(html)).getDocument();
        Graphics2DRenderer renderer = new Graphics2DRenderer(document, "file:/");
        Graphics2D graphics = new BufferedImage(500, 500, TYPE_INT_ARGB).createGraphics();
        try {
            renderer.layout(graphics, new Dimension(500, 500));
        } finally {
            graphics.dispose();
        }
        return requireNonNull(firstDiv(renderer.getPanel().getRootBox()));
    }

    @Nullable
    private static BlockBox firstDiv(Box box) {
        if (box instanceof BlockBox block && box.getElement() != null && "div".equals(box.getElement().getNodeName())) {
            return block;
        }
        for (Box child : box.getChildren()) {
            BlockBox div = firstDiv(child);
            if (div != null) {
                return div;
            }
        }
        return null;
    }

    private static List<LineBox> lineBoxes(Box root) {
        return lines(root).filter(line -> !text(line).isEmpty()).toList();
    }

    private static Stream<LineBox> lines(Box box) {
        return box instanceof LineBox line ? Stream.of(line) : box.getChildren().stream().flatMap(TrailingSpaceLineBreakTest::lines);
    }

    private static String text(Box box) {
        StringBuilder text = new StringBuilder();
        collectText(box, text);
        return text.toString();
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
