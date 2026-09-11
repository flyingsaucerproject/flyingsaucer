package org.xhtmlrenderer.layout.breaker;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.xhtmlrenderer.simple.Graphics2DRenderer;

import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;

import static java.awt.image.BufferedImage.TYPE_INT_ARGB;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests doBreakText() behaviour when word-wrap: break-word is set
 * and a single character is wider than the full available container width.
 *
 * Expected (per CSS overflow-wrap spec):
 *   Force break after each character, allow slight overflow.
 *   Never mark text as unbreakable — causes infinite loop or single-line overflow.
 *
 * Bug:  doBreakText sets unbreakable=true when tryToBreakAnywhere=true and right=-1
 * Fix:  force context.setEnd(start + 1), do NOT set unbreakable
 */
class WordWrapBreakWordSingleCharOverflowTest {

    // 3 identical chars — no whitespace, no digit-letter transitions
    // → natural BreakIterator finds no break points
    // → word-wrap: break-word falls back to BreakAnywhereLineBreakStrategy
    private static final String TEXT = "WWW";

    // font-size: 100pt → single 'W' ≈ 70pt wide
    // width: 10pt → single char (70pt) >> container (10pt)
    // → guaranteed: single char wider than full container avail
    private static final String HTML =
        "<?xml version='1.0' encoding='UTF-8'?>" +
            "<!DOCTYPE html PUBLIC '-//W3C//DTD XHTML 1.0 Strict//EN' " +
            "'http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd'>" +
            "<html xmlns='http://www.w3.org/1999/xhtml'><head><style>" +
            "* { margin: 0; padding: 0; }" +
            "body { font-size: 100pt; }" +
            "div  { width: 10pt; word-wrap: break-word; }" +
            "</style></head>" +
            "<body><div>" + TEXT + "</div></body>" +
            "</html>";

    // Control: measures the height of exactly 1 line
    private static final String HTML_ONE_CHAR =
        "<?xml version='1.0' encoding='UTF-8'?>" +
            "<!DOCTYPE html PUBLIC '-//W3C//DTD XHTML 1.0 Strict//EN' " +
            "'http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd'>" +
            "<html xmlns='http://www.w3.org/1999/xhtml'><head><style>" +
            "* { margin: 0; padding: 0; }" +
            "body { font-size: 100pt; }" +
            "div  { width: 10pt; word-wrap: break-word; }" +
            "</style></head>" +
            "<body><div>W</div></body>" +
            "</html>";

    // ────────────────────────────────────────────────────────────────────────
    // Test 1 — must not hang (infinite loop guard)
    // Bug: unbreakable=true → InlineBoxing retries same position → loop
    // Fix: consume 1 char → forward progress guaranteed
    // ────────────────────────────────────────────────────────────────────────
    @Test
    @Timeout(5)
    void layoutMustCompleteWithinTimeout() throws Exception {
        renderAutoSize(HTML);
    }

    // ────────────────────────────────────────────────────────────────────────
    // Test 2 — must produce multiple lines not one unbreakable overflow
    // Uses auto-sized image height as proxy for line count:
    //   Bug → 1 line  → image height ≈ single char height
    //   Fix → 3 lines → image height > single char height
    // ────────────────────────────────────────────────────────────────────────
    @Test
    @Timeout(5)
    void shouldProduceMultipleLinesWhenSingleCharExceedsContainerWidth() throws Exception {
        int oneLineHeight = renderAutoSize(HTML_ONE_CHAR).getHeight();
        int result        = renderAutoSize(HTML).getHeight();

        assertThat(result)
            .as("""
                    word-wrap: break-word must break '%s' into multiple lines
                    when single char width > full container width.

                    1-char reference height : %dpx

                    Bug:  unbreakable=true  → height ≈ %dpx  (1 line)
                    Fix:  force 1 char/line → height > %dpx  (%d lines)
                    """,
                TEXT, oneLineHeight,
                oneLineHeight, oneLineHeight, TEXT.length())
            .isGreaterThan(oneLineHeight);
    }

    // ── helper ────────────────────────────────────────────────────────────────

    private static BufferedImage renderAutoSize(String html) throws Exception {
        File tmp = File.createTempFile("fs-breaker-test-", ".html");
        tmp.deleteOnExit();
        Files.writeString(tmp.toPath(), html);
        return Graphics2DRenderer.renderToImageAutoSize(
            tmp.toURI().toURL().toExternalForm(), 500, TYPE_INT_ARGB);
    }
}