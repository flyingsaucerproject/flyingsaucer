package org.xhtmlrenderer.pdf;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A margin at the end of the document is not painted: it does not add a page
 * (<a href="https://github.com/flyingsaucerproject/flyingsaucer/issues/322">#322</a>).
 */
class TrailingMarginPageTest {
    private static final String STYLE = "@page { size: A4; margin: 36pt; } body { margin: 0; font: 10pt/1 serif; }";

    /** The page holds 769.89pt: the paragraph ends at 710pt, its margin at 810pt. */
    @Test
    void aBottomMarginWhichDoesNotFitAddsNoPage() {
        assertThat(pages("<div style='height: 700pt'></div><p style='margin: 0 0 100pt'>The last line</p>")).isEqualTo(1);
    }

    /** The bottom margin of the body collapses with the margin of its last paragraph. */
    @Test
    void aBottomMarginOfTheBodyWhichDoesNotFitAddsNoPage() {
        assertThat(pages("<div style='height: 700pt'></div><p style='margin: 0'>The last line</p>",
                "body { margin-bottom: 100pt; }")).isEqualTo(1);
    }

    /** The case of the issue: on A4, the margins after the last paragraph reach the next page. */
    @Test
    void theMarginsAfterTheLastParagraphOfTheIssueAddNoPage() {
        String filler = "<p style='line-height: 1'><span style='font-family: times; font-size: 10pt'>E</span></p>";
        String content = "<p style='line-height: 1'><span style='font-family: times; font-size: 10pt'>" +
                "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore " +
                "magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo " +
                "consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla " +
                "pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est " +
                "laborum.</span></p>";
        String html = "<html><head><style>@page { size: A4 }</style></head><body>" +
                "<span style='font-size: 2pt'>2 <br/></span>" + filler.repeat(17) + content.repeat(2) + "</body></html>";

        ITextRenderer renderer = new ITextRenderer(20f, 20);
        renderer.setDocumentFromString(html);
        renderer.layout();

        assertThat(renderer.getRootBox().getLayer().getPages()).hasSize(1);
    }

    /** A bottom padding is painted: the page it reaches stays. */
    @Test
    void aBottomPaddingWhichDoesNotFitAddsAPage() {
        assertThat(pages("<div style='height: 700pt'></div>" +
                "<p style='margin: 0; padding-bottom: 100pt; background: #ccc'>The last line</p>")).isEqualTo(2);
    }

    /** An empty block is laid out, not only painted: the pages its height reaches stay. */
    @Test
    void anEmptyBlockWithAHeightAddsPages() {
        assertThat(pages("<p style='margin: 0'>The first line</p><div style='height: 1000pt'></div>")).isEqualTo(2);
    }

    /** A box positioned below the content keeps its page. */
    @Test
    void anAbsoluteBoxOnTheNextPageKeepsItsPage() {
        assertThat(pages("<p>The first line</p><div style='position: absolute; top: 900pt'>Below</div>")).isEqualTo(2);
    }

    /** An empty absolute box has no content but its own box: the page of that box stays. */
    @Test
    void anEmptyAbsoluteBoxOnTheNextPageKeepsItsPage() {
        assertThat(pages("<p>The first line</p>" +
                "<div style='position: absolute; top: 900pt; width: 40pt; height: 40pt; background: #ccc'></div>")).isEqualTo(2);
    }

    /** The minimum height of the root is its own extent: the pages it reaches stay. */
    @Test
    void theMinimumHeightOfTheRootKeepsItsPages() {
        assertThat(pages("<p style='margin: 0'>The first line</p>",
                "html { min-height: 900pt; } body { margin-bottom: 1000pt; }")).isEqualTo(2);
    }

    /** A float in an inline-block overflows the line: the pages it reaches stay. */
    @Test
    void aFloatInAnInlineBlockKeepsItsPages() {
        assertThat(pages("<p style='margin: 0'><span style='display: inline-block; height: 10pt'>" +
                "<span style='float: left; width: 40pt; height: 1000pt; background: #ccc'></span></span></p>")).isEqualTo(2);
    }

    private static int pages(String body) {
        return pages(body, "");
    }

    private static int pages(String body, String css) {
        ITextRenderer renderer = new ITextRenderer();
        renderer.setDocumentFromString("<html><head><style>" + STYLE + css + "</style></head><body>" + body + "</body></html>");
        renderer.layout();
        return renderer.getRootBox().getLayer().getPages().size();
    }
}
