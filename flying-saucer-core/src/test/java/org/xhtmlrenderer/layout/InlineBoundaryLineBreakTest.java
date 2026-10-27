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
 * A line does not break where an inline box starts or ends inside a word, see
 * <a href="https://github.com/flyingsaucerproject/flyingsaucer/issues/752">issue 752</a>: line breaking ignores the
 * boundaries of inline boxes (CSS Text 3, § 1.5), so {@code aaa<span>bbb</span>} is the one word {@code aaabbb}.
 */
class InlineBoundaryLineBreakTest {

    @Test
    void aWordWithASpanInsideStaysOnOneLineInTheNarrowestCell() {
        assertThat(lines(layout(narrowestCell("aaa<span>bbb</span>")))).containsExactly("aaabbb");
    }

    @Test
    void aWordWithABoldPartStaysOnOneLineInTheNarrowestCell() {
        assertThat(lines(layout(narrowestCell("aaa<b>bbb</b>")))).containsExactly("aaabbb");
    }

    @Test
    void aWordWithAColoredPartStaysOnOneLineInTheNarrowestCell() {
        assertThat(lines(layout(narrowestCell("aaa<span style='color: #06c'>bbb</span>")))).containsExactly("aaabbb");
    }

    @Test
    void aNoBreakSpaceBeforeASpanDoesNotBreakInTheNarrowestCell() {
        assertThat(lines(layout(narrowestCell("aaa&#160;<span>bbb</span>")))).containsExactly("aaa bbb");
    }

    @Test
    void aWordOfManyInlineBoxesStaysOnOneLineInTheNarrowestCell() {
        assertThat(lines(layout(narrowestCell("a<i>b</i>c<i>d</i>e")))).containsExactly("abcde");
    }

    @Test
    void aSpaceBeforeASpanStillBreaksInTheNarrowestCell() {
        assertThat(lines(layout(narrowestCell("aaa <span>bbb</span>")))).containsExactly("aaa ", "bbb");
    }

    /** The space breaks the line as it does without the span. */
    @Test
    void aSpaceAtTheStartOfASpanStillBreaksInTheNarrowestCell() {
        assertThat(lines(layout(narrowestCell("aaa<span> bbb</span>"))))
                .isEqualTo(lines(layout(narrowestCell("aaa bbb"))))
                .containsExactly("aaa ", "bbb");
    }

    /** The narrowest cell is as wide as the parts of the word together: each box measures its own part. */
    @Test
    void theNarrowestCellIsAsWideAsThePartsOfTheWord() {
        Box root = layout("<table style='width: 1px'><tr>" +
                "<td>aaa<span>bbb</span></td><td>aaa</td><td>bbb</td></tr></table>");

        List<BlockBox> cells = cells(root);
        assertThat(cells).hasSize(3);
        assertThat(cells.get(0).getContentWidth())
                .isEqualTo(cells.get(1).getContentWidth() + cells.get(2).getContentWidth());
    }

    /** A line that the word does not fit on ends before the word, not inside it. */
    @Test
    void aWordWhichDoesNotFitMovesToTheNextLineWhole() {
        String word = "<div style='width: 62px'>xx aaa<span>bbb</span></div>";
        String plain = "<div style='width: 62px'>xx aaabbb</div>";

        assertThat(lines(layout(plain))).containsExactly("xx ", "aaabbb");
        assertThat(lines(layout(word))).containsExactly("xx ", "aaabbb");
    }

    /** A word wider than the line overflows it, and the line ends after the word, not inside it. */
    @Test
    void aWordWiderThanTheLineOverflowsItWhole() {
        assertThat(lines(layout("<div style='width: 20px'>aaaaaaa<span>bb cc</span></div>")))
                .containsExactly("aaaaaaabb ", "cc");
    }

    /** A float between the parts of a word does not break it. */
    @Test
    void aFloatInsideAWordDoesNotBreakIt() {
        assertThat(lines(layout("<div style='width: 62px'>xx aaa<span style='float: right'>F</span>bbb</div>")))
                .startsWith("xx ", "aaabbb");
    }

    /** An inline-block ends the word before it: the line may break before it. */
    @Test
    void anInlineBlockEndsTheWordBeforeIt() {
        assertThat(lines(layout("<div style='width: 62px'>xx aaa<span style='display: inline-block'>IB</span>bbb</div>")))
                .containsExactly("xx aaa", "IBbbb");
    }

    /** A text which may not break continues the word before it, and the line overflows with both. */
    @Test
    void aWordRunsOnIntoATextWhichMayNotBreak() {
        assertThat(lines(layout("<div style='width: 62px'>xx aaa<span style='white-space: nowrap'>bbb ccc</span></div>")))
                .containsExactly("xx ", "aaabbb ccc");
    }

    /** A text which may not break, glued to the text after it, moves to the next line with it where the two do not fit. */
    @Test
    void aTextWhichMayNotBreakMovesToTheNextLineWithTheWordItRunsOnInto() {
        assertThat(lines(layout("<div style='width: 62px'>xx <span style='white-space: nowrap'>aaa</span>bbb</div>")))
                .containsExactly("xx ", "aaabbb");
        assertThat(lines(layout("<div style='width: 62px'>xx <span style='white-space: pre'>aaa</span>bbb</div>")))
                .containsExactly("xx ", "aaabbb");
    }

    /** {@code word-wrap: break-word} breaks a word only where it does not fit, so a word which fits stays whole. */
    @Test
    void aWordWithBreakWordWhichFitsMovesToTheNextLineWhole() {
        assertThat(lines(layout("<div style='width: 62px; word-wrap: break-word'>xx aaa<span>bbb</span></div>")))
                .containsExactly("xx ", "aaabbb");
    }

    /** A box which fits whole after the text before it, but not with the text glued to it, moves to the next line too. */
    @Test
    void aBoxWithBreakWordWhichFitsWithoutTheWordItRunsOnIntoMovesToTheNextLine() {
        assertThat(lines(layout("<div style='width: 62px; word-wrap: break-word'>xx <span>aaa</span>bbb</div>")))
                .containsExactly("xx ", "aaabbb");
    }

    /** {@code word-wrap: break-word} breaks a word only where the line has no other place to break: it moves to the next line first. */
    @Test
    void aWordWithBreakWordInABoxAfterOtherContentMovesToTheNextLineBeforeItBreaks() {
        assertThat(lines(layout("<div style='width: 62px; word-wrap: break-word'>xx <span>aaaaaaa</span></div>")))
                .isEqualTo(lines(layout("<div style='width: 62px; word-wrap: break-word'>xx aaaaaaa</div>")))
                .containsExactly("xx ", "aaaaaa", "a");
    }

    @Test
    void aWordWithBreakWordOfTwoBoxesAfterOtherContentMovesToTheNextLineBeforeItBreaks() {
        assertThat(lines(layout("<div style='width: 62px; word-wrap: break-word'>xx <span>aaaa</span>bbbbbbbb</div>")))
                .isEqualTo(lines(layout("<div style='width: 62px; word-wrap: break-word'>xx aaaabbbbbbbb</div>")))
                .containsExactly("xx ", "aaaabb", "bbbbbb");
    }

    /** The part of a word in a box after the start of the word breaks where the line ends: the line has no other place to break. */
    @Test
    void aWordWithBreakWordBreaksInsideTheBoxWhichContinuesIt() {
        assertThat(lines(layout("<div style='width: 62px; word-wrap: break-word'>xx aaa<span>aaaaaaa</span></div>")))
                .isEqualTo(lines(layout("<div style='width: 62px; word-wrap: break-word'>xx aaaaaaaaaa</div>")))
                .containsExactly("xx ", "aaaaaa", "aaaa");
    }

    /** A word which starts the line breaks on it, even where the line is shorter than the next one. */
    @Test
    void aWordWithBreakWordWhichStartsAnIndentedLineBreaksOnIt() {
        assertThat(lines(layout("<div style='width: 62px; word-wrap: break-word; text-indent: 10px'><span>aaaaaaaa</span></div>")))
                .isEqualTo(lines(layout("<div style='width: 62px; word-wrap: break-word; text-indent: 10px'>aaaaaaaa</div>")))
                .containsExactly("aaaaa", "aaa");
    }

    /** {@code word-break: break-all} may break between any two letters, so between two boxes too. */
    @Test
    void aWordWithBreakAllBreaksBetweenItsBoxes() {
        assertThat(lines(layout("<div style='width: 62px; word-break: break-all'>xx aaa<span>bbb</span></div>")))
                .containsExactly("xx aaa", "bbb");
    }

    /** A new line in a text which keeps them ends the word before it. */
    @Test
    void aNewLineEndsTheWordBeforeIt() {
        assertThat(lines(layout("<div style='width: 62px; white-space: pre-wrap'>xx aaa<span>\nbbb</span></div>")))
                .containsExactly("xx aaa", "bbb");
    }

    private static String narrowestCell(String content) {
        return "<table style='width: 1px'><tr><td>" + content + "</td></tr></table>";
    }

    private static Box layout(String body) {
        String html = "<html xmlns='http://www.w3.org/1999/xhtml'><head><style>" +
                "body { margin: 0; font: 16px monospace; } table { border-collapse: collapse; } td { padding: 0; }" +
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
