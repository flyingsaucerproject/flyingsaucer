package org.xhtmlrenderer.layout.breaker;

import org.jspecify.annotations.Nullable;

import java.text.BreakIterator;
import java.text.CharacterIterator;

/**
 * BreakIterator implementation that improves line breaking for URLs. Break points are supported
 * before path fragments.
 */
public class UrlAwareLineBreakIterator extends BreakIterator {

    private static final String BREAKING_CHARS = ".,:;!?- \n\r\t/";
    /** The characters no line may break before (UAX #14, LB6 and LB7), which come before the break after U+200B (LB8). */
    private static final String NO_BREAK_BEFORE = "\u000B\u000C\r\n\u0085\u2028\u2029\u200B";
    private static final int NONE = -2;

    private final BreakIterator delegate = BreakIterator.getLineInstance();
    @Nullable
    private String text;
    @Nullable
    private Range currentRange;
    private int lastBreak;
    private int pendingBreak = NONE;
    /** The next zero width space at or after {@code lastBreak}, or -1 for none: the text is searched once. */
    private int nextZeroWidthSpace;

    public UrlAwareLineBreakIterator(String text) {
        setText(text);
    }

    @Override
    public int preceding(int offset) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int last() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int previous() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * The next break point. A break after U+200B ZERO WIDTH SPACE and the spaces after it is added: Unicode requires one
     * there (UAX #14, LB8: {@code ZW SP* ÷}), and the line iterator of the JDK has none.
     */
    @Override
    public int next() {
        int next = pendingBreak == NONE ? nextBreak() : pendingBreak;
        pendingBreak = NONE;
        int zeroWidthSpaceBreak = zeroWidthSpaceBreakBefore(next == BreakIterator.DONE ? text.length() : next);
        if (zeroWidthSpaceBreak != NONE) {
            pendingBreak = next;
            next = zeroWidthSpaceBreak;
        }
        if (next != BreakIterator.DONE) {
            lastBreak = next;
            if (nextZeroWidthSpace > -1 && nextZeroWidthSpace < lastBreak) {
                nextZeroWidthSpace = indexOfZeroWidthSpace(lastBreak);
            }
        }
        return next;
    }

    /** The first break after a zero width space between the last break point and {@code limit}, or {@code NONE}. */
    private int zeroWidthSpaceBreakBefore(int limit) {
        while (nextZeroWidthSpace > -1 && nextZeroWidthSpace < limit) {
            int end = nextZeroWidthSpace + 1;
            while (end < text.length() && text.charAt(end) == ' ') {
                end++;
            }
            if (end > lastBreak && end < limit && NO_BREAK_BEFORE.indexOf(text.charAt(end)) < 0) {
                return end;
            }
            nextZeroWidthSpace = indexOfZeroWidthSpace(end);
        }
        return NONE;
    }

    private int indexOfZeroWidthSpace(int fromIndex) {
        return text.indexOf('\u200B', fromIndex);
    }

    private int nextBreak() {
        checkNotAheadOfDelegate();

        Range searchRange = currentRange; // the range in which we search for slashes

        if (isDelegateInSync()) {
            boolean reachedEnd = advanceDelegate();
            if (reachedEnd) {
                return BreakIterator.DONE;
            }

            if ("://".equals(substring(new Range(currentRange.getStop(), -1, 2)))) {
                searchRange = searchRange.withStart(currentRange.getStop() + 2);
                advanceDelegate(); // no reached-end check needed here, because there are at least two slashes ahead
            }
        }
        searchRange = searchRange.withStop(currentRange.getStop());

        searchRange = trimSearchRange(searchRange);
        int nextSlash = findSlashInRange(searchRange);
        currentRange = currentRange.withStart(nextSlash > -1 ? nextSlash : delegate.current());

        return currentRange.getStart();
    }

    private Range trimSearchRange(Range searchRange) {
        // Exclude leading breaking characters (should really only be a slash).
        while (searchRange.getStart() < currentRange.getStop() && BREAKING_CHARS.indexOf(text.charAt(searchRange.getStart())) > -1) {
            searchRange = searchRange.incrementStart();
        }

        // Exclude trailing breaking characters.
        while (searchRange.getStop() > searchRange.getStart() && BREAKING_CHARS.indexOf(text.charAt(searchRange.getStop() - 1)) > -1) {
            searchRange = searchRange.decrementStop();
        }

        return searchRange;
    }

    private int findSlashInRange(Range searchRange) {
        int nextSlash = text.indexOf('/', searchRange.getStart());
        return nextSlash < searchRange.getStop() ? nextSlash : -1;
    }

    private String substring(Range range) {
        return text.substring(Math.max(0, range.getStart()), Math.min(text.length(), range.getStop()));
    }

    private void checkNotAheadOfDelegate() {
        // This is a sanity check. We should never be in this state.
        if (currentRange.getStart() > delegate.current()) {
            throw new IllegalStateException("Iterator ahead of delegate.");
        }
    }

    private boolean isDelegateInSync() {
        return currentRange.getStart() == delegate.current();
    }

    private boolean advanceDelegate() {
        int next = delegate.next();
        currentRange = currentRange.withStop(next);
        return next == BreakIterator.DONE;
    }

    @Override
    public int next(int n) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public boolean isBoundary(int offset) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int following(int offset) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int first() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void setText(CharacterIterator newText) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int current() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public CharacterIterator getText() {
        return delegate.getText();
    }

    @Override
    public final void setText(String newText) {
        delegate.setText(newText);
        text = newText;
        currentRange = new Range(delegate.current(), delegate.current());
        lastBreak = 0;
        pendingBreak = NONE;
        nextZeroWidthSpace = indexOfZeroWidthSpace(0);
    }

    private static final class Range {
        private final int start;
        private final int stop;

        public Range(int start, int stop) {
            this.start = start;
            this.stop = Math.max(start, stop);
        }

        public Range(int referencePoint, int startOffset, int stopOffset) {
            this(referencePoint + startOffset, referencePoint + stopOffset);
        }

        public Range withStart(int start) {
            return new Range(start, stop);
        }

        public Range withStop(int stop) {
            return new Range(start, stop);
        }

        public Range incrementStart() {
            int newStart = start + 1;
            return new Range(newStart, Math.max(newStart, stop));
        }

        public Range decrementStop() {
            int newStop = stop - 1;
            return new Range(Math.min(start, newStop), newStop);
        }

        public int getStart() {
            return start;
        }

        public int getStop() {
            return stop;
        }

        @Override
        public String toString() {
            return "[" + start + ", " + stop + ")";
        }
    }
}
