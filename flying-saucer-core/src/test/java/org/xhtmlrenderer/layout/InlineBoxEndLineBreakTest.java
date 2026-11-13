package org.xhtmlrenderer.layout;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.w3c.dom.Document;
import org.xhtmlrenderer.render.BlockBox;
import org.xhtmlrenderer.render.Box;
import org.xhtmlrenderer.render.InlineLayoutBox;
import org.xhtmlrenderer.render.InlineText;
import org.xhtmlrenderer.render.LineBox;
import org.xhtmlrenderer.resource.XMLResource;
import org.xhtmlrenderer.simple.Graphics2DRenderer;
import org.xhtmlrenderer.swing.ImageReplacedElement;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.StringReader;
import java.util.List;
import java.util.stream.Stream;

import static java.awt.image.BufferedImage.TYPE_INT_ARGB;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * The right margin, border and padding of an inline box stay on the line where its text ends, also where the text
 * overflows the line, see <a href="https://github.com/flyingsaucerproject/flyingsaucer/issues/756">issue 756</a>:
 * a break after the last character of a box is after its margin edge (CSS Text 3, § 5.5).
 */
class InlineBoxEndLineBreakTest {

    private static final String SPAN = "<span>aaaaaaa</span>";

    /** A white GIF of 1 x 1 pixels. */
    private static final String GIF = "data:image/gif;base64,R0lGODlhAQABAIAAAP///wAAACwAAAAAAQABAAACAkQBADs=";

    @ParameterizedTest
    @ValueSource(strings = {"padding-right: 10px", "margin-right: 10px", "border-right: 10px solid red"})
    void theEndOfABoxStaysOnTheLineOfItsOverflowingText(String edge) {
        String span = "<span style='" + edge + "'>aaaaaaa</span>";

        List<LineBox> lines = lineBoxes(layout("x " + span + " y"));

        assertThat(texts(lines)).containsExactly("x ", "aaaaaaa", "y");
        List<LineBox> plain = lineBoxes(layout("x " + SPAN + " y"));
        assertThat(lines.get(1).getWidth()).isEqualTo(plain.get(1).getWidth() + 10);
        assertThat(lines.get(2).getWidth()).isEqualTo(plain.get(2).getWidth());
    }

    /** With nothing after the box, its end makes no line of its own. */
    @Test
    void theEndOfTheLastBoxMakesNoLineOfItsOwn() {
        List<LineBox> lines = lineBoxes(layout("x <span style='padding-right: 10px'>aaaaaaa</span>"));

        assertThat(texts(lines)).containsExactly("x ", "aaaaaaa");
        assertThat(lines.get(1).getWidth()).isEqualTo(lineBoxes(layout("x " + SPAN)).get(1).getWidth() + 10);
    }

    /** A line break after the box ends the line with the box, and makes no empty line. */
    @Test
    void theEndOfABoxBeforeALineBreakMakesNoLineOfItsOwn() {
        List<LineBox> lines = lineBoxes(layout("x <span style='padding-right: 10px'>aaaaaaa</span><br/>y"));

        assertThat(texts(lines)).containsExactly("x ", "aaaaaaa", "y");
    }

    /** The end of a box which holds another one comes after the overflowing text of the inner box. */
    @Test
    void theEndOfAnOuterBoxComesAfterTheTextOfTheInnerBox() {
        List<LineBox> lines = lineBoxes(layout("x <span style='padding-right: 10px'>aaaaaaa<b> bbbb</b></span>"));

        assertThat(texts(lines)).containsExactly("x ", "aaaaaaa", "bbbb");
        assertThat(lines.get(2).getWidth())
                .isEqualTo(lineBoxes(layout("x <span>aaaaaaa<b> bbbb</b></span>")).get(2).getWidth() + 10);
    }

    /** A justified line which an inline-block ends loses the trailing space of its text. */
    @Test
    void aJustifiedLineWhichAnInlineBlockEndsLosesItsTrailingSpace() {
        List<LineBox> lines = lineBoxes(layout("<div style='text-align: justify'><span>aaaaaaa </span>" +
                "<span style='display: inline-block; width: 10px'>x</span></div>"));

        assertThat(texts(lines)).first().isEqualTo("aaaaaaa");
    }

    /** The same for a line which an image ends. */
    @Test
    void aJustifiedLineWhichAnImageEndsLosesItsTrailingSpace() {
        Box root = layout("<div style='text-align: justify'><span>aaaaaaa </span>" +
                "<img style='width: 10px; height: 10px' src='" + GIF + "'/></div>");

        assertThat(images(root)).hasSize(1);
        assertThat(texts(lineBoxes(root))).first().isEqualTo("aaaaaaa");
    }

    /** An empty inline box between the text and the inline-block does not keep the trailing space. */
    @Test
    void aJustifiedLineLosesItsTrailingSpaceBeforeAnEmptyBox() {
        List<LineBox> lines = lineBoxes(layout("<div style='text-align: justify'><span>aaaaaaa </span><span></span>" +
                "<span style='display: inline-block; width: 10px'>x</span></div>"));

        assertThat(texts(lines)).first().isEqualTo("aaaaaaa");
    }

    /** An empty box with a border does not keep the trailing space either, as in Chromium. */
    @Test
    void aJustifiedLineLosesItsTrailingSpaceBeforeAnEmptyBoxWithABorder() {
        List<LineBox> lines = lineBoxes(layout("<div style='text-align: justify'><span>aaaaaaa </span>" +
                "<span style='border-left: 5px solid; padding: 0 10px'></span>" +
                "<span style='display: inline-block; width: 10px'>x</span></div>"));

        assertThat(texts(lines)).first().isEqualTo("aaaaaaa");
    }

    /**
     * An image after a text which overflows the line goes to the next line, also where the overflow comes from the
     * right padding of the box around both. The text (67px) fits the block (80px), but not with the padding (30px).
     */
    @Test
    void anImageAfterAnOverflowingTextGoesToTheNextLine() {
        List<LineBox> lines = lineBoxes(layout(80, "<span style='padding-right: 30px'>aaaaaaa" +
                "<img style='width: 5px; height: 5px' src='" + GIF + "'/></span>"));

        assertThat(texts(lines)).containsExactly("aaaaaaa", "");
        assertThat(images(lines.get(1))).hasSize(1);
    }

    /** The same for an inline-block. */
    @Test
    void anInlineBlockAfterAnOverflowingTextGoesToTheNextLine() {
        List<LineBox> lines = lineBoxes(layout(80, "<span style='padding-right: 30px'>aaaaaaa" +
                "<span style='display: inline-block; width: 5px'>x</span></span>"));

        assertThat(texts(lines)).containsExactly("aaaaaaa", "x");
    }

    /** A float after a text which overflows the line goes below the line. */
    @Test
    void aFloatAfterAnOverflowingTextGoesBelowTheLine() {
        List<LineBox> lines = lineBoxes(layout(80, "<span style='padding-right: 30px'>aaaaaaa" +
                "<img style='float: left; width: 5px; height: 5px' src='" + GIF + "'/></span> y"));

        assertThat(texts(lines)).containsExactly("aaaaaaa", "y");
        assertThat(nonFlowContent(lines)).singleElement().extracting(Box::getY).isEqualTo(lines.get(1).getY());
    }

    /** An image which fits the line, but not with the right padding of the box it ends, goes to the next line. */
    @Test
    void anImageWhichEndsABoxFitsTheLineWithTheEndOfTheBox() {
        List<LineBox> lines = lineBoxes(layout(80, "<span style='padding-right: 30px'>aaa" +
                "<img style='width: 40px; height: 5px' src='" + GIF + "'/></span>"));

        assertThat(texts(lines)).containsExactly("aaa", "");
        assertThat(images(lines.get(1))).hasSize(1);
    }

    /**
     * An image with more text of the box after it does not need the room for the end of the box. Chromium keeps it
     * on the line too, and breaks before the next text.
     */
    @Test
    void anImageBeforeMoreTextOfTheBoxStaysOnTheLine() {
        List<LineBox> lines = lineBoxes(layout(80, "<span style='padding-right: 30px'>aaa" +
                "<img style='width: 40px; height: 5px' src='" + GIF + "'/>bbb ccc</span>"));

        assertThat(texts(lines)).containsExactly("aaa", "bbb ", "ccc");
        assertThat(images(lines.get(0))).hasSize(1);
    }

    /**
     * The text (67px) with the right padding of its box (30px) overflows the block (80px), but the padding comes
     * after the rest of the box. An image which fits after the text stays on its line, as in Chromium.
     */
    @Test
    void anImageWhichFitsAfterTheTextStaysOnItsLine() {
        List<LineBox> lines = lineBoxes(layout(80, "<span style='padding-right: 30px'>aaaaaaa" +
                "<img style='width: 5px; height: 5px' src='" + GIF + "'/>bbb</span>"));

        assertThat(texts(lines)).containsExactly("aaaaaaa", "bbb");
        assertThat(images(lines.get(0))).hasSize(1);
    }

    /** The same for an inline-block. */
    @Test
    void anInlineBlockWhichFitsAfterTheTextStaysOnItsLine() {
        List<LineBox> lines = lineBoxes(layout(80, "<span style='padding-right: 30px'>aaaaaaa" +
                "<span style='display: inline-block; width: 5px'>x</span>bbb</span>"));

        assertThat(texts(lines)).containsExactly("aaaaaaax", "bbb");
    }

    /** The same for a float. The text after it is glued to the text before it, and stays on the line too. */
    @Test
    void aFloatWhichFitsAfterTheTextStaysOnItsLine() {
        List<LineBox> lines = lineBoxes(layout(80, "<span style='padding-right: 30px'>aaaaaaa" +
                "<img style='float: left; width: 5px; height: 5px' src='" + GIF + "'/>bbb</span>"));

        assertThat(texts(lines)).containsExactly("aaaaaaabbb");
        assertThat(nonFlowContent(lines)).singleElement().extracting(Box::getY).isEqualTo(lines.get(0).getY());
    }

    /** A float after a box which overflows the line goes below the line. */
    @Test
    void aFloatAfterAnOverflowingBoxGoesBelowTheLine() {
        List<LineBox> lines = lineBoxes(layout("x " + SPAN +
                "<img style='float: left; width: 10px; height: 10px' src='" + GIF + "'/> y"));

        assertThat(texts(lines)).containsExactly("x ", "aaaaaaa", "y");
        assertThat(nonFlowContent(lines)).singleElement().extracting(Box::getY).isEqualTo(lines.get(2).getY());
    }

    /**
     * An absolutely positioned box after content which overflows the line takes its static position at the start of
     * the next line, as in Chromium.
     */
    @ParameterizedTest
    @ValueSource(strings = {SPAN, "<span style='display: inline-block; width: 50px'>i</span>"})
    void anAbsoluteBoxAfterOverflowingContentStartsTheNextLine(String overflowing) {
        List<LineBox> lines = lineBoxes(layout("x " + overflowing + "<span style='position: absolute'>Z</span> y"));

        assertThat(texts(lines)).last().isEqualTo("y");
        assertThat(nonFlowContent(lines)).singleElement().extracting(Box::getAbsY).isEqualTo(lines.getLast().getAbsY());
    }

    /**
     * The {@code ::first-line} style ends with the first line also where an image ends it: the text after the image
     * takes the style of the block.
     */
    @Test
    void theFirstLineStyleEndsWhereAnImageEndsTheLine() {
        List<LineBox> lines = lineBoxes(layout("div::first-line { text-transform: uppercase; }", 40,
                "aaaaaaa<img style='width: 10px; height: 10px' src='" + GIF + "'/> bbb"));

        assertThat(texts(lines)).containsExactly("AAAAAAA", " ", "bbb");
        assertThat(images(lines.get(1))).hasSize(1);
    }

    /** An absolutely positioned box after content which fits stays on its line. */
    @Test
    void anAbsoluteBoxAfterContentWhichFitsStaysOnItsLine() {
        List<LineBox> lines = lineBoxes(layout("x<span style='position: absolute'>Z</span> y"));

        assertThat(texts(lines)).containsExactly("x y");
        assertThat(nonFlowContent(lines)).singleElement().extracting(Box::getAbsY).isEqualTo(lines.getFirst().getAbsY());
    }

    /** The floats and the absolutely positioned boxes which the lines hold. */
    private static List<Box> nonFlowContent(List<LineBox> lines) {
        return lines.stream().flatMap(line -> line.getNonFlowContent().stream()).toList();
    }

    /** The content in a block of 40px. */
    private static Box layout(String content) {
        return layout(40, content);
    }

    /** The content in a block of the width in pixels. */
    private static Box layout(int width, String content) {
        return layout("", width, content);
    }

    /** The content in a block of the width in pixels, with the style sheet. */
    private static Box layout(String css, int width, String content) {
        String html = "<html xmlns='http://www.w3.org/1999/xhtml'><head><style>" +
                "body { margin: 0; font: 16px monospace; }" + css +
                "</style></head><body><div style='width: " + width + "px'>" + content + "</div></body></html>";
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

    /** All line boxes in the box tree, in document order, also the ones with no text. */
    private static List<LineBox> lineBoxes(Box root) {
        return lines(root).toList();
    }

    private static Stream<LineBox> lines(Box box) {
        return box instanceof LineBox line ?
                Stream.of(line) : box.getChildren().stream().flatMap(InlineBoxEndLineBreakTest::lines);
    }

    /** The images in the box tree which replace their elements. */
    private static List<ImageReplacedElement> images(Box root) {
        return boxes(root)
                .filter(BlockBox.class::isInstance)
                .map(box -> ((BlockBox) box).getReplacedElement())
                .filter(ImageReplacedElement.class::isInstance)
                .map(ImageReplacedElement.class::cast)
                .toList();
    }

    /** The box and all boxes in it, also the ones which are inline children of inline boxes. */
    private static Stream<Box> boxes(Box box) {
        Stream<Box> inlineChildren = box instanceof InlineLayoutBox inline ?
                inline.getInlineChildren().stream().filter(Box.class::isInstance).map(Box.class::cast) : Stream.empty();
        return Stream.concat(Stream.of(box),
                Stream.concat(inlineChildren, box.getChildren().stream()).flatMap(InlineBoxEndLineBreakTest::boxes));
    }

    private static List<String> texts(List<LineBox> lines) {
        return lines.stream().map(line -> {
            StringBuilder text = new StringBuilder();
            collectText(line, text);
            return text.toString();
        }).toList();
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
