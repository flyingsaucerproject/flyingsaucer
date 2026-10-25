package org.xhtmlrenderer.render;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
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
 * The boundary of an inline box is ignored in finding where a line may break, so a table cell is as narrow as the
 * widest word of its text, not of each of its inline boxes, see
 * <a href="https://github.com/flyingsaucerproject/flyingsaucer/issues/752">issue 752</a>.
 * <p>
 * Each table is as narrow as it goes, so a cell has as many lines as its text has words.
 */
class InlineBoundaryMinWidthTest {
    @ParameterizedTest
    @ValueSource(strings = {
            "aaabbb",
            "aaa<span>bbb</span>",
            "aaa<b>bbb</b>",
            "aaa<span style='color: #06c'>bbb</span>",
            "aaa&#160;bbb",
            "aaa&#160;<span>bbb</span>",
            "<span>aaa</span>bbb",
            "<span>aaa</span><span>bbb</span>",
            "aaa<span><b>bbb</b></span>",
            "aaa<span style='padding-left: 20px'><b>bbb</b></span>",
            "aaa<span>b</span>bb",
    })
    void aWordAcrossInlineBoxesDoesNotBreak(String cell) {
        assertThat(lines(cell, "")).as("the lines of %s", cell).isEqualTo(1);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "aaa <span>bbb</span>",
            "aaa<span> bbb</span>",
            "<span>aaa </span>bbb",
            "<span>aaa</span> <span>bbb</span>",
            "aaa-<span>bbb</span>",
    })
    void aLineBreaksBetweenWordsInDifferentInlineBoxes(String cell) {
        assertThat(lines(cell, "")).as("the lines of %s", cell).isEqualTo(2);
    }

    /** With {@code word-wrap: break-word}, a cell is as narrow as its widest letter, as before. */
    @Test
    void aCellWithWordWrapBreakWordIsAsNarrowAsBefore() {
        assertThat(lines("aaa<span>bbb</span>", "word-wrap: break-word;")).isGreaterThan(1);
    }

    /** The width of the cell is the width of the word: the padding of the span counts once. */
    @ParameterizedTest
    @ValueSource(strings = {
            "aaa<span style='padding-left: 20px'>bbb</span>",
            "<span style='padding-right: 20px'>aaa</span>bbb",
    })
    void theMarginBorderAndPaddingOfAnInlineBoxCountInTheWord(String cell) {
        int plain = cellWidth("aaabbb");
        assertThat(cellWidth(cell)).as("the width of %s", cell).isCloseTo(plain + 20, within(1));
    }

    private static int lines(String cell, String cellStyle) {
        return cellBox(cell, cellStyle).getChildCount();
    }

    private static int cellWidth(String cell) {
        return cellBox(cell, "").getContentWidth();
    }

    private static BlockBox cellBox(String cell, String cellStyle) {
        String html = """
                <html xmlns="http://www.w3.org/1999/xhtml">
                  <head>
                    <style>
                      body { margin: 0; font: 16px sans-serif; }
                      table { border-collapse: collapse; width: 1px; }
                      td { padding: 0; %s }
                    </style>
                  </head>
                  <body><table><tr><td id="cell">%s</td></tr></table></body>
                </html>
                """.formatted(cellStyle, cell);
        Graphics2DRenderer renderer = new Graphics2DRenderer(XMLResource.load(html).getDocument(), "");
        BufferedImage image = new BufferedImage(500, 500, TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        try {
            renderer.layout(g, new Dimension(500, 500));
        } finally {
            g.dispose();
        }
        return (BlockBox) requireNonNull(renderer.getSharedContext().getBoxById("cell"));
    }
}
