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
                .containsExactly("xx", "yyyyy");
    }

    @Test
    void aTextWithASpaceWhichMayNotBreakMovesToTheNextLineWhole() {
        assertThat(lines(layout("<div style='width: 80px'>xx yy <span style='white-space: nowrap'>zzz zz</span></div>")))
                .containsExactly("xx yy", "zzz zz");
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

    /** A preserved space does not disappear at the break: the block which holds both texts decides. */
    @Test
    void aLineBreaksAfterAPreservedSpaceAtTheEndOfAPreformattedText() {
        assertThat(lines(layout("<div style='width: 40px'><span style='white-space: pre'>xx </span>yyyyy</div>")))
                .containsExactly("xx ", "yyyyy");
    }

    /** A preformatted text which does not fit before its first new line moves to the next line as well. */
    @Test
    void aPreformattedTextWithANewLineMovesToTheNextLineAfterASpace() {
        assertThat(lines(layout("<div style='width: 40px'>xx <span style='white-space: pre'>yyyyy\nz</span></div>")))
                .containsExactly("xx", "yyyyy", "z");
    }

    /** The right padding of a preformatted text is on its last line: the text before its first new line fits without it. */
    @Test
    void aPreformattedTextFitsBeforeItsNewLineWithoutItsRightPadding() {
        assertThat(lines(layout("<div style='width: 40px'>xx <span style='white-space: pre; padding-right: 20px'>y\nz</span></div>")))
                .containsExactly("xx y", "z");
    }

    /** The line before the preformatted text does not end on its new line: a justified line stays justified. */
    @Test
    void theLineBeforeAPreformattedTextWhichMovesDoesNotEndOnANewLine() {
        List<LineBox> lines = lineBoxes(layout("<div style='width: 80px; text-align: justify'>aa bb " +
                "<span style='white-space: pre'>yyyyy\nz</span></div>"));

        assertThat(lines).extracting(NowrapLineBreakTest::text).containsExactly("aa bb", "yyyyy", "z");
        assertThat(lines.get(0).isEndsOnNL()).isFalse();
        assertThat(lines.get(1).isEndsOnNL()).isTrue();
    }

    /** {@code word-wrap: break-word} does not let a line break where {@code white-space} does not. */
    @Test
    void breakWordDoesNotBreakATextWhichMayNotWrap() {
        assertThat(lines(layout("<div style='width: 40px; white-space: nowrap; word-wrap: break-word'>xx<span>yyyyy</span></div>")))
                .containsExactly("xxyyyyy");
    }

    /** {@code word-break: break-all} does not let a line break where {@code white-space} does not. */
    @Test
    void breakAllDoesNotBreakATextWhichMayNotWrap() {
        assertThat(lines(layout("<div style='width: 40px; white-space: nowrap; word-break: break-all'>xx<span>yyyyy</span></div>")))
                .containsExactly("xxyyyyy");
    }

    /** After a hyphen, the nearest common ancestor of the two boxes decides if the line may break (CSS Text 3, § 5.5). */
    @Test
    void aLineBreaksAfterAHyphenAtTheEndOfATextWhichMayNotBreak() {
        assertThat(lines(layout("<div style='width: 80px'><span style='white-space: nowrap'>Pseudo-</span>element</div>")))
                .containsExactly("Pseudo-", "element");
    }

    /** Both parts of the content of a pseudo-element are in the pseudo-element: its style decides, not the style of its element. */
    @Test
    void aLineBreaksAfterAHyphenInsideAPseudoElementWhichMayBreak() {
        String css = "div::before { white-space: normal; content: 'Pseudo-' attr(title); }";
        assertThat(lines(layout(css, "<div title='element' style='width: 80px; white-space: nowrap'></div>")))
                .containsExactly("Pseudo-", "element");
    }

    /** The space at the end of a text which may not break disappears at the end of the line: it does not count. */
    @Test
    void aTrailingSpaceOfATextWhichMayNotBreakDoesNotMoveIt() {
        assertThat(lines(layout("<div style='width: 80px'>xx <span style='white-space: nowrap'>yyyyy </span></div>")))
                .containsExactly("xx yyyyy");
    }

    /** Other space separators at the end of a text which may not break hang at the end of the line (CSS Text 3, § 4.1.3). */
    @ParameterizedTest
    @ValueSource(strings = {"\u2003", "\u2007", "\u202F"})
    void aTrailingSpaceSeparatorOfATextWhichMayNotBreakDoesNotMoveIt(String space) {
        assertThat(lines(layout("<div style='width: 80px'>xx <span style='white-space: nowrap'>yyyyy" + space + "</span></div>")))
                .containsExactly("xx yyyyy" + space);
    }

    /** A right padding or border after a space separator keeps it from hanging (CSS Text 3, § 8.2): the space counts (#790). */
    @ParameterizedTest
    @ValueSource(strings = {"padding-right: 20px", "border-right: 20px solid"})
    void aTrailingSpaceSeparatorBeforeARightEdgeMovesATextWhichMayNotBreak(String edge) {
        assertThat(lines(layout("<div style='width: 100px'>xx <span style='white-space: nowrap; " + edge + "'>yyyyy&#x2003;</span></div>")))
                .containsExactly("xx", "yyyyy\u2003");
    }

    /** The right padding of a box which ends with the text keeps the space from hanging too. */
    @Test
    void aTrailingSpaceSeparatorBeforeTheRightPaddingOfAnOuterBoxMovesATextWhichMayNotBreak() {
        assertThat(lines(layout("<div style='width: 100px'>xx <span style='padding-right: 20px'>" +
                "<span style='white-space: nowrap'>yyyyy&#x2003;</span></span></div>")))
                .containsExactly("xx", "yyyyy\u2003");
    }

    /** An empty inline box after the space leaves the right padding of the box between the space and the end of the line. */
    @Test
    void aTrailingSpaceSeparatorBeforeAnEmptyBoxAndARightPaddingMovesATextWhichMayNotBreak() {
        assertThat(lines(layout("<div style='width: 100px'>xx <span style='white-space: nowrap; padding-right: 20px'>yyyyy&#x2003;<i></i></span></div>")))
                .containsExactly("xx", "yyyyy\u2003");
    }

    /** A collapsible space between the boxes disappears at the end of the line: the outer padding still follows the space. */
    @Test
    void aTrailingSpaceSeparatorBeforeASpaceAndTheRightPaddingOfAnOuterBoxMovesATextWhichMayNotBreak() {
        assertThat(lines(layout("<div style='width: 100px'>xx <span style='padding-right: 20px'>" +
                "<span style='white-space: nowrap'>yyyyy&#x2003;</span> </span></div>")))
                .containsExactly("xx", "yyyyy\u2003");
    }

    /** A space separator in a padded box of its own is glued to the text which may not break: the two move together. */
    @Test
    void aTrailingSpaceSeparatorInAPaddedInnerBoxMovesTheTextWhichMayNotBreakWhole() {
        assertThat(lines(layout("<div style='width: 100px'>xx <span style='white-space: nowrap'>yyyyy" +
                "<span style='padding-right: 20px'>&#x2003;</span></span></div>")))
                .containsExactly("xx", "yyyyy\u2003");
    }

    /** The rule holds where the text may wrap too: the space before the right padding counts. */
    @Test
    void aTrailingSpaceSeparatorBeforeARightPaddingMovesAWordWhichMayWrap() {
        assertThat(lines(layout("<div style='width: 100px'>xx <span style='padding-right: 20px'>yyyyy&#x2003;</span></div>")))
                .containsExactly("xx", "yyyyy\u2003");
    }

    /** The left padding of an empty box after the space is between the space and the end of the line too. */
    @Test
    void aTrailingSpaceSeparatorBeforeAnEmptyBoxWithALeftPaddingMovesATextWhichMayNotBreak() {
        assertThat(lines(layout("<div style='width: 85px'>xx <span style='white-space: nowrap'>yyyyy&#x2003;<i style='padding-left: 20px'></i></span></div>")))
                .containsExactly("xx", "yyyyy\u2003");
    }

    /** A trailing ogham space mark is removed at the end of the line (CSS Text 3, § 4.1.2), even before a right padding. */
    @Test
    void aTrailingOghamSpaceMarkBeforeARightPaddingDoesNotMoveATextWhichMayNotBreak() {
        assertThat(lines(layout("<div style='width: 100px'>xx <span style='white-space: nowrap; padding-right: 20px'>yyyyy&#x1680;</span></div>")))
                .containsExactly("xx yyyyy");
    }

    /** An absolute box after the space is out of the flow: the right padding of the outer box still follows the space. */
    @Test
    void aTrailingSpaceSeparatorBeforeAnAbsoluteBoxAndARightPaddingMovesATextWhichMayNotBreak() {
        assertThat(lines(layout("<div style='width: 100px'>xx <span style='padding-right: 20px'>" +
                "<span style='white-space: nowrap'>yyyyy&#x2003;</span><b style='position: absolute'></b></span></div>")))
                .containsExactly("xx", "yyyyy\u2003");
    }

    /** A trailing ogham space mark disappears at the end of the line: the right padding of the outer box still follows the space. */
    @Test
    void aTrailingSpaceSeparatorBeforeAnOghamSpaceMarkAndARightPaddingMovesATextWhichMayNotBreak() {
        assertThat(lines(layout("<div style='width: 100px'>xx <span style='padding-right: 20px'>" +
                "<span style='white-space: nowrap'>yyyyy&#x2003;</span>&#x1680;</span></div>")))
                .containsExactly("xx", "yyyyy\u2003");
    }

    /** A space at the end of a line disappears where the text may wrap too: it does not count (CSS Text 3, § 4.1.2). */
    @ParameterizedTest
    @ValueSource(strings = {"normal", "pre-line"})
    void aSpaceAtABreakDoesNotCountWhereTheTextMayWrap(String whiteSpace) {
        assertThat(lines(layout("<div style='width: 70px; white-space: " + whiteSpace + "'>xx aaaa bbbb</div>")))
                .containsExactly("xx aaaa", "bbbb");
    }

    /** A preserved space at the end of a line hangs (CSS Text 3, § 4.1.3): it does not count either. */
    @Test
    void aPreservedSpaceAtABreakDoesNotCount() {
        assertThat(lines(layout("<div style='width: 70px; white-space: pre-wrap'>xx aaaa bbbb</div>")))
                .containsExactly("xx aaaa ", "bbbb");
    }

    /** Other space separators at the end of a line hang where the text may wrap too (CSS Text 3, § 4.1.3). */
    @ParameterizedTest
    @ValueSource(strings = {"normal", "pre-wrap", "pre-line"})
    void aSpaceSeparatorAtABreakDoesNotCountWhereTheTextMayWrap(String whiteSpace) {
        assertThat(lines(layout("<div style='width: 70px; white-space: " + whiteSpace + "'>xx aaaa&#x2003;bbbb</div>")))
                .containsExactly("xx aaaa\u2003", "bbbb");
    }

    /** A space separator at the end of a text which may wrap hangs unless a right padding follows it. */
    @Test
    void aTrailingSpaceSeparatorWithoutAnEdgeDoesNotMoveAWordWhichMayWrap() {
        assertThat(lines(layout("<div style='width: 80px'>xx <span>yyyyy&#x2003;</span></div>")))
                .containsExactly("xx yyyyy\u2003");
    }

    /** A margin is not between the space and the end of the line: the space still hangs. */
    @Test
    void aTrailingSpaceSeparatorBeforeARightMarginDoesNotMoveATextWhichMayNotBreak() {
        assertThat(lines(layout("<div style='width: 100px'>xx <span style='white-space: nowrap; margin-right: 20px'>yyyyy&#x2003;</span></div>")))
                .containsExactly("xx yyyyy\u2003");
    }

    /** A collapsible space at the end of the line disappears, even before a right padding. */
    @Test
    void aTrailingSpaceBeforeARightPaddingDoesNotMoveATextWhichMayNotBreak() {
        assertThat(lines(layout("<div style='width: 100px'>xx <span style='white-space: nowrap; padding-right: 20px'>yyyyy </span></div>")))
                .containsExactly("xx yyyyy");
    }

    /** The block which may not wrap decides the boundary after a text which breaks anywhere. */
    @Test
    void aTextStaysAfterATextWhichBreaksAnywhereWhereTheBlockMayNotBreak() {
        assertThat(lines(layout("<div style='width: 40px; white-space: nowrap'>" +
                "<span style='white-space: normal; word-break: break-all'>x</span><span>yyyyy</span></div>")))
                .containsExactly("xyyyyy");
    }

    /** Inside a text which may not break, a hyphen does not break the line either. */
    @Test
    void aLineDoesNotBreakAfterAHyphenInsideATextWhichMayNotBreak() {
        assertThat(lines(layout("<div style='width: 80px'><span style='white-space: nowrap'>Pseudo-<b>element</b></span></div>")))
                .containsExactly("Pseudo-element");
    }

    /** Two boxes with the same style are still two boxes: the block they are in decides. */
    @Test
    void aLineBreaksAfterAHyphenBetweenTwoTextsWhichMayNotBreak() {
        assertThat(lines(layout("<div style='width: 80px'><span style='white-space: nowrap'>Pseudo-</span>" +
                "<span style='white-space: nowrap'><span style='white-space: normal'>element</span></span></div>")))
                .containsExactly("Pseudo-", "element");
    }

    /** A space which stays at the break, such as an em space, is decided as a hyphen is. */
    @Test
    void aLineBreaksAfterAnEmSpaceAtTheEndOfATextWhichMayNotBreak() {
        assertThat(lines(layout("<div style='width: 40px'><span style='white-space: nowrap'>xx&#x2003;</span>yyyyy</div>")))
                .containsExactly("xx\u2003", "yyyyy");
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

    /** The block which holds an inline-block and the text after it decides if the line may break between them. */
    @ParameterizedTest
    @ValueSource(strings = {"nowrap", "pre"})
    void aTextStaysAfterAnInlineBlockWhereTheBlockMayNotBreak(String whiteSpace) {
        assertThat(lines(layout("<div style='width: 40px; white-space: " + whiteSpace + "'>" +
                "<span style='display: inline-block'>x</span>yyyyyyy</div>")))
                .containsExactly("xyyyyyyy");
    }

    /** Preserved spaces do not disappear: they stay glued to the inline-block before them, as the text after them. */
    @Test
    void preservedSpacesStayAfterAnInlineBlockWhereTheBlockMayNotBreak() {
        assertThat(lines(layout("<div style='width: 40px; white-space: nowrap'>" +
                "<span style='display: inline-block; width: 30px'>x</span><span style='white-space: pre'>  </span>y</div>")))
                .containsExactly("x  y");
    }

    @Test
    void aTextWhichMayNotBreakMovesAfterAnInlineBlockWhereTheBlockMayBreak() {
        assertThat(lines(layout("<div style='width: 40px'><span style='display: inline-block'>x</span>" +
                "<span style='white-space: nowrap'>yyyyyyy</span></div>")))
                .containsExactly("x", "yyyyyyy");
    }

    private static Box layout(String body) {
        return layout("", body);
    }

    private static Box layout(String css, String body) {
        String html = "<html xmlns='http://www.w3.org/1999/xhtml'><head><style>" +
                "body { margin: 0; font: 16px monospace; }" + css +
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
