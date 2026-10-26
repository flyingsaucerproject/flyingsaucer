package org.xhtmlrenderer.layout;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.xhtmlrenderer.render.BlockBox;
import org.xhtmlrenderer.render.InlineLayoutBox;
import org.xhtmlrenderer.render.InlineText;
import org.xhtmlrenderer.resource.XMLResource;
import org.xhtmlrenderer.simple.Graphics2DRenderer;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import static java.awt.image.BufferedImage.TYPE_INT_ARGB;
import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

/**
 * A line may break after a zero width space, see
 * <a href="https://github.com/flyingsaucerproject/flyingsaucer/issues/753">issue 753</a>.
 */
class ZeroWidthSpaceLineBreakTest {
    private static final String TABLE_STYLE = "table { border-collapse: collapse; width: 1px; } td { padding: 0; }";

    /** The table is as narrow as it goes, so a cell has as many lines as its text has words. */
    @ParameterizedTest
    @ValueSource(strings = {
            "aaa&#8203;bbb",
            "aaa&#8203; bbb",
            "aaa&#8203;<span>bbb</span>",
            "<span>aaa&#8203;</span>bbb",
    })
    void aCellBreaksAfterZeroWidthSpace(String cell) {
        assertThat(box(TABLE_STYLE, "<table><tr><td id='box'>" + cell + "</td></tr></table>").getChildCount())
                .as("the lines of %s", cell)
                .isEqualTo(2);
    }

    @Test
    void aCellWithZeroWidthSpaceIsAsNarrowAsItsWiderWord() {
        int widerWord = Math.max(cellWidth("aaa"), cellWidth("www"));

        assertThat(cellWidth("aaa&#8203;www")).isCloseTo(widerWord, within(1));
        assertThat(cellWidth("www&#8203;aaa")).isCloseTo(widerWord, within(1));
    }

    private static int cellWidth(String cell) {
        return box(TABLE_STYLE, "<table><tr><td id='box'>" + cell + "</td></tr></table>").getContentWidth();
    }

    @Test
    void aZeroWidthSpaceInSpanDoesNotEndTheWordBeforeIt() {
        assertThat(box(TABLE_STYLE, "<table><tr><td id='box'>aaa<span>&#8203;bbb</span></td></tr></table>").getChildCount())
                .isEqualTo(2);
        assertThat(box(TABLE_STYLE, "<table><tr><td id='box'>aaa<span>bbb&#8203;</span>ccc</td></tr></table>").getChildCount())
                .isEqualTo(2);
    }

    @Test
    void aBlockWrapsAfterZeroWidthSpace() {
        BlockBox div = box("", "<div id='box' style='width: 60px'>xx aaa&#8203;bbb</div>");

        assertThat(div.getChildCount()).isEqualTo(2);
        assertThat(text(div, 0)).isEqualTo("xx aaa\u200B");
        assertThat(text(div, 1)).isEqualTo("bbb");
    }

    private static String text(BlockBox block, int line) {
        InlineLayoutBox inline = (InlineLayoutBox) block.getChild(line).getChild(0);
        return inline.getInlineChildren().stream()
                .map(child -> ((InlineText) child).getSubstring())
                .reduce("", String::concat);
    }

    private static BlockBox box(String style, String body) {
        String html = """
                <html xmlns="http://www.w3.org/1999/xhtml">
                  <head>
                    <style>body { margin: 0; font: 16px sans-serif; } %s</style>
                  </head>
                  <body>%s</body>
                </html>
                """.formatted(style, body);
        Graphics2DRenderer renderer = new Graphics2DRenderer(XMLResource.load(html).getDocument(), "");
        BufferedImage image = new BufferedImage(500, 500, TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        try {
            renderer.layout(g, new Dimension(500, 500));
        } finally {
            g.dispose();
        }
        return (BlockBox) requireNonNull(renderer.getSharedContext().getBoxById("box"));
    }
}
