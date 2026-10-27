package org.xhtmlrenderer.layout.breaker;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.text.BreakIterator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class UrlAwareLineBreakIteratorTest {

    @Test
    public void breakAtSpace() {
        assertBreaksCorrectly("Hello World! World foo",
                new String[] {"Hello ", "World! ", "World ", "foo"});
    }

    @Test
    public void breakAtPunctuation() {
        assertBreaksCorrectly("The.quick,brown:fox;jumps!over?the(lazy)[dog]",
                new String[] {"The.", "quick,", "brown:", "fox;", "jumps!", "over?", "the", "(lazy)", "[dog]"});
    }

    @Test
    public void breakAtHyphen() {
        assertBreaksCorrectly("Pseudo-element",
                new String[] {"Pseudo-", "element"});
    }

    /** Unicode requires a break after a zero width space (UAX #14, LB8: {@code ZW SP* ÷}). */
    @Test
    public void breakAfterZeroWidthSpace() {
        assertBreaksCorrectly("aaa\u200Bbbb\u200Bccc",
                new String[] {"aaa\u200B", "bbb\u200B", "ccc"});
    }

    /** The break comes after the spaces which follow the zero width space, not before them. */
    @Test
    public void breakAfterSpacesWhichFollowZeroWidthSpace() {
        assertBreaksCorrectly("aaa\u200B  bbb",
                new String[] {"aaa\u200B  ", "bbb"});
    }

    @Test
    public void breakAfterZeroWidthSpaceAmongOtherBreaks() {
        assertBreaksCorrectly("xx aaa\u200Bbbb yy",
                new String[] {"xx ", "aaa\u200B", "bbb ", "yy"});
    }

    /** No break before a hard line break (UAX #14, LB6, which comes before LB8): the line ends after it. */
    @Test
    public void noBreakAfterZeroWidthSpaceBeforeHardLineBreak() {
        assertBreaksCorrectly("aaa\u200B\nbbb",
                new String[] {"aaa\u200B\n", "bbb"});
    }

    /** No break before a zero width space (UAX #14, LB7, which comes before LB8): the break follows the last one. */
    @Test
    public void noBreakBetweenTwoZeroWidthSpaces() {
        assertBreaksCorrectly("aaa\u200B\u200Bbbb",
                new String[] {"aaa\u200B\u200B", "bbb"});
    }

    /** A zero width space at the end of the text adds no break: the end of the text is one already. */
    @Test
    public void zeroWidthSpaceAtEndAddsNoBreak() {
        assertBreaksCorrectly("aaa\u200B",
                new String[] {"aaa\u200B"});
    }

    /**
     * A long text without a zero width space is not searched for one again at every break: that would cost time
     * quadratic in its length. Each word holds a Cyrillic letter, as a text of Latin-1 letters only finds no U+200B
     * without searching.
     */
    @Test
    @Timeout(5)
    public void longTextWithoutZeroWidthSpaceBreaksInLinearTime() {
        String text = "\u044Fb ".repeat(250_000);
        BreakIterator iterator = new UrlAwareLineBreakIterator(text);

        int last = 0;
        int next;
        while ((next = iterator.next()) != BreakIterator.DONE) {
            last = next;
        }

        assertThat(last).isEqualTo(text.length());
    }

    /**
     * A long text without a slash is not searched for one up to its end at every break: that would cost time quadratic
     * in its length.
     */
    @Test
    @Timeout(5)
    public void longTextWithoutSlashBreaksInLinearTime() {
        String text = "word ".repeat(500_000);
        BreakIterator iterator = new UrlAwareLineBreakIterator(text);

        int last = 0;
        int next;
        while ((next = iterator.next()) != BreakIterator.DONE) {
            last = next;
        }

        assertThat(last).isEqualTo(text.length());
    }

    @Test
    public void breakAfterZeroWidthSpaceInUrl() {
        assertBreaksCorrectly("http://example.com/a\u200Bb",
                new String[] {"http://example.", "com", "/a\u200B", "b"});
    }

    @Test
    public void breakAtSlash() {
        assertBreaksCorrectly("Justice/Law",
                new String[] {"Justice", "/Law"});
    }

    @Test
    public void wordBeginsWithSlash() {
        assertBreaksCorrectly("Justice /Law",
                new String[] {"Justice ", "/Law"});
    }

    @Test
    public void wordEndsWithSlash() {
        assertBreaksCorrectly("Justice/ Law",
                new String[] {"Justice/ ", "Law"});
    }

    @Test
    public void wordEndsWithSlashMultipleWhitespace() {
        assertBreaksCorrectly("Justice/    Law",
                new String[] {"Justice/    ", "Law"});
    }

    @Test
    public void slashSeparatedSequence() {
        assertBreaksCorrectly("/this/is/a/long/path/name/",
                new String[] {"/this", "/is", "/a", "/long", "/path", "/name/"});
    }

    @Test
    public void urlInside() {
        assertBreaksCorrectly("Sentence with url https://github.com/flyingsaucerproject/flyingsaucer?test=true&param2=false inside.",
                new String[] {"Sentence ", "with ", "url ", "https://github.", "com", "/flyingsaucerproject", "/flyingsaucer?",
                        "test=true&param2=false ", "inside."});
    }

    @Test
    public void multipleSlashesInWord() {
        assertBreaksCorrectly("word/////word",
                new String[] {"word", "/////word"});
    }

    @Test
    public void multipleSlashesBeforeWord() {
        assertBreaksCorrectly("hello /////world",
                new String[] {"hello ", "/////world"});
    }

    @Test
    public void multipleSlashesAfterWord() {
        assertBreaksCorrectly("hello world/////",
                new String[] {"hello ", "world/////"});
    }

    @Test
    public void multipleSlashesAroundWord() {
        assertBreaksCorrectly("hello /////world/////",
                new String[] {"hello ", "/////world/////"});
    }

    @Test
    public void whitespaceAfterTrailingSlashes() {
        assertBreaksCorrectly("hello world///    ",
                new String[] {"hello ", "world///    "});
    }

    @Test
    public void shortUrl() {
        assertBreaksCorrectly("http://localhost",
                new String[] {"http://localhost"});
    }

    @Test
    public void incompleteUrl() {
        assertBreaksCorrectly("http://", 
                new String[] {"http://"});
    }


    private void assertBreaksCorrectly(String input, String[] segments) {
        BreakIterator iterator = new UrlAwareLineBreakIterator(input);

        int segmentIndex = 0;
        int lastBreakPoint = 0;
        int breakpoint;
        while ((breakpoint = iterator.next()) != BreakIterator.DONE) {
            if (segmentIndex < segments.length) {
                String segment = segments[segmentIndex++];
                assertThat(input.substring(lastBreakPoint, breakpoint))
                        .as("Segment #" + segmentIndex + " does not match.")
                        .isEqualTo(segment);
                lastBreakPoint = breakpoint;
            } else {
                fail("Too few segments.");
            }
        }
        assertThat(lastBreakPoint).as("Last breakpoint is wrong.").isEqualTo(input.length());
        if (segmentIndex != segments.length) {
            fail("Too many segments.");
        }
    }

}