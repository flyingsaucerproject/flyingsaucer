package org.xhtmlrenderer.layout.breaker;

import junit.framework.TestCase;

import java.text.BreakIterator;


public class UrlAwareLineBreakIteratorTest extends TestCase {

    public void testNext_BreakAtSpace() {
        assertBreaksCorrectly("Hello World! World foo",
                new String[] {"Hello ", "World! ", "World ", "foo"});
    }


    public void testNext_BreakAtPunctuation() {
        assertBreaksCorrectly("The.quick,brown:fox;jumps!over?the(lazy)[dog]",
                new String[] {"The.", "quick,", "brown:", "fox;", "jumps!", "over?", "the", "(lazy)", "[dog]"});
    }


    public void testNext_BreakAtHyphen() {
        assertBreaksCorrectly("Pseudo-element",
                new String[] {"Pseudo-", "element"});
    }


    public void testNext_BreakAtSlash() {
        assertBreaksCorrectly("Justice/Law",
                new String[] {"Justice", "/Law"});
    }


    public void testNext_WordBeginsWithSlash() {
        assertBreaksCorrectly("Justice /Law",
                new String[] {"Justice ", "/Law"});
    }


    public void testNext_WordEndsWithSlash() {
        assertBreaksCorrectly("Justice/ Law",
                new String[] {"Justice/ ", "Law"});
    }


    public void testNext_WordEndsWithSlashMultipleWhitespace() {
        assertBreaksCorrectly("Justice/    Law",
                new String[] {"Justice/    ", "Law"});
    }


    public void testNext_SlashSeparatedSequence() {
        assertBreaksCorrectly("/this/is/a/long/path/name/",
                new String[] {"/this", "/is", "/a", "/long", "/path", "/name/"});
    }


    public void testNext_UrlInside() {
        assertBreaksCorrectly("Sentence with url https://github.com/flyingsaucerproject/flyingsaucer?test=true&param2=false inside.",
                new String[] {"Sentence ", "with ", "url ", "https://github.", "com", "/flyingsaucerproject", "/flyingsaucer?",
                        "test=true&param2=false ", "inside."});
    }


    public void testNext_MultipleSlashesInWord() {
        assertBreaksCorrectly("word/////word",
                new String[] {"word", "/////word"});
    }


    public void testNext_MultipleSlashesBeforeWord() {
        assertBreaksCorrectly("hello /////world",
                new String[] {"hello ", "/////world"});
    }


    public void testNext_MultipleSlashesAfterWord() {
        assertBreaksCorrectly("hello world/////",
                new String[] {"hello ", "world/////"});
    }


    public void testNext_MultipleSlashesAroundWord() {
        assertBreaksCorrectly("hello /////world/////",
                new String[] {"hello ", "/////world/////"});
    }


    public void testNext_WhitespaceAfterTrailingSlashes() {
        assertBreaksCorrectly("hello world///    ",
                new String[] {"hello ", "world///    "});
    }


    public void testNext_ShortUrl() {
        assertBreaksCorrectly("http://localhost",
                new String[] {"http://localhost"});
    }


    public void testNext_IncompleteUrl() {
        assertBreaksCorrectly("http://",
                new String[] {"http://"});
    }


    private void assertBreaksCorrectly(String input, String[] segments) {
        BreakIterator iterator = new UrlAwareLineBreakIterator();
        iterator.setText(input);

        int segmentIndex = 0;
        int lastBreakPoint = 0;
        int breakpoint;
        while ((breakpoint = iterator.next()) != BreakIterator.DONE) {
            if (segmentIndex < segments.length) {
                String segment = segments[segmentIndex++];
                assertEquals("Segment #" + segmentIndex + " does not match.", segment, input.substring(lastBreakPoint, breakpoint));
                lastBreakPoint = breakpoint;
            } else {
                fail("Too few segments.");
            }
        }
        assertEquals("Last breakpoint is wrong.", input.length(), lastBreakPoint);
        if (segmentIndex != segments.length) {
            fail("Too many segments.");
        }
    }

}