package org.xhtmlrenderer.layout;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.w3c.dom.Document;
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
 * A text which may not break moves to the next line where it does not fit after a space, see
 * <a href="https://github.com/flyingsaucerproject/flyingsaucer/issues/772">issue 772</a>: the box which holds the
 * space decides if the line may break at it (CSS Text 3, § 5.5).
 */
class NowrapLineBreakTest {

    @ParameterizedTest
    @ValueSource(strings = {"nowrap", "pre"})
    void aTextWhichMayNotBreakMovesToTheNextLineAfterASpace(String whiteSpace) {
        assertThat(lines(layout("<div style='width: 40px'>xx <span style='white-space: " + whiteSpace + "'>yyyyy</span></div>")))
                .containsExactly("xx ", "yyyyy");
    }

    @Test
    void aTextWithASpaceWhichMayNotBreakMovesToTheNextLineWhole() {
        assertThat(lines(layout("<div style='width: 80px'>xx yy <span style='white-space: nowrap'>zzz zz</span></div>")))
                .containsExactly("xx yy ", "zzz zz");
    }

    @Test
    void aTextWhichMayNotBreakAndFitsStaysOnTheLine() {
        assertThat(lines(layout("<div style='width: 80px'>xx <span style='white-space: nowrap'>yyy</span></div>")))
                .containsExactly("xx yyy");
    }

    /** A line with nothing before the text holds it, and the text overflows the line. */
    @Test
    void aTextWhichMayNotBreakOverflowsALineItStarts() {
        assertThat(lines(layout("<div style='width: 40px'><span style='white-space: nowrap'>yyyyy</span> xx</div>")))
                .containsExactly("yyyyy", "xx");
    }

    /** With no space before it, the text continues the word before it: the line has no place to break. */
    @Test
    void aTextWhichMayNotBreakStaysWithTheWordBeforeIt() {
        assertThat(lines(layout("<div style='width: 40px'>xx<span style='white-space: nowrap'>yyyyy</span></div>")))
                .containsExactly("xxyyyyy");
    }

    /** The space at the start of the text may not break either: the box which holds it does not wrap. */
    @Test
    void aSpaceAtTheStartOfATextWhichMayNotBreakDoesNotBreak() {
        assertThat(lines(layout("<div style='width: 40px'>xx<span style='white-space: nowrap'> yyyyy</span></div>")))
                .containsExactly("xx yyyyy");
    }

    /** The space at the end of the text may not break: the text after it continues on the line. */
    @Test
    void aSpaceAtTheEndOfATextWhichMayNotBreakDoesNotBreak() {
        assertThat(lines(layout("<div style='width: 40px'><span style='white-space: nowrap'>xx </span>yyyyy</div>")))
                .containsExactly("xx yyyyy");
    }

    /** A preformatted text which does not fit before its first new line moves to the next line as well. */
    @Test
    void aPreformattedTextWithANewLineMovesToTheNextLineAfterASpace() {
        assertThat(lines(layout("<div style='width: 40px'>xx <span style='white-space: pre'>yyyyy\nz</span></div>")))
                .containsExactly("xx ", "yyyyy", "z");
    }

    /** {@code word-wrap: break-word} does not let a line break where {@code white-space} does not. */
    @Test
    void breakWordDoesNotBreakATextWhichMayNotWrap() {
        assertThat(lines(layout("<div style='width: 40px; white-space: nowrap; word-wrap: break-word'>xx<span>yyyyy</span></div>")))
                .containsExactly("xxyyyyy");
    }

    /** After a hyphen, the nearest common ancestor of the two boxes decides if the line may break (CSS Text 3, § 5.5). */
    @Test
    void aLineBreaksAfterAHyphenAtTheEndOfATextWhichMayNotBreak() {
        assertThat(lines(layout("<div style='width: 80px'><span style='white-space: nowrap'>Pseudo-</span>element</div>")))
                .containsExactly("Pseudo-", "element");
    }

    /** Inside a text which may not break, a hyphen does not break the line either. */
    @Test
    void aLineDoesNotBreakAfterAHyphenInsideATextWhichMayNotBreak() {
        assertThat(lines(layout("<div style='width: 80px'><span style='white-space: nowrap'>Pseudo-<b>element</b></span></div>")))
                .containsExactly("Pseudo-element");
    }

    /** A text which does not fit next to a float moves below it, as a word which may not break does. */
    @Test
    void aTextWhichMayNotBreakMovesBelowAFloat() {
        String floated = "<span style='float: left; width: 30px; height: 10px'></span>";

        Box root = layout("<div style='width: 80px'>" + floated + "<span style='white-space: nowrap'>yyyyyyy</span></div>");

        List<LineBox> lines = lineBoxes(root);
        assertThat(lines).hasSize(1);
        assertThat(lines.get(0).getY()).isGreaterThanOrEqualTo(10);
    }

    private static Box layout(String body) {
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
        return renderer.getPanel().getRootBox();
    }

    /** The text of each line with text in the box tree, in document order. */
    private static List<String> lines(Box root) {
        return lineBoxes(root).stream().map(NowrapLineBreakTest::text).toList();
    }

    /** The lines with text in the box tree, in document order. */
    private static List<LineBox> lineBoxes(Box root) {
        List<LineBox> lines = new ArrayList<>();
        collectLines(root, lines);
        return lines;
    }

    private static void collectLines(Box box, List<LineBox> lines) {
        if (box instanceof LineBox line) {
            if (!text(line).isEmpty()) {
                lines.add(line);
            }
            return;
        }
        for (Box child : box.getChildren()) {
            collectLines(child, lines);
        }
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
