package org.xhtmlrenderer.layout.breaker;

import org.junit.jupiter.api.Test;

import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for BreakAnywhereLineBreakStrategy.
 *
 * Fix: changed from:
 *   if (position + 1 > currentString.length()) → BreakPoint(position++)
 * to:
 *   if (position >= currentString.length())     → BreakPoint(++position)
 *
 * Correct range: [1..length] — starts at 1 (minimum 1 char per line),
 * ends at length (full string tested to detect overflow).
 */
class BreakAnywhereLineBreakStrategyTest {

    // ────────────────────────────────────────────────────────────────────────
    // Test 1 — Core fix: full string length must be offered as a break point
    // Without position=length, doBreakText never detects the full string overflows avail.
    // ────────────────────────────────────────────────────────────────────────
    @Test
    void shouldOfferBreakPointAtFullStringLength() {
        String str = "ABC"; // length = 3
        BreakAnywhereLineBreakStrategy strategy = new BreakAnywhereLineBreakStrategy(str);

        List<Integer> positions = collectAllPositions(strategy);

        assertThat(positions)
            .as("""
                    Must include break point at position %d (= string length).
                    Without it, full string overflow is never detected → no break → PDF clips.

                    Bug:  position + 1 > length → last offered = %d, skips %d
                    Fix:  position >= length    → last offered = %d ✅
                    """,
                str.length(),
                str.length() - 1, str.length(),
                str.length())
            .contains(str.length());
    }

    // ────────────────────────────────────────────────────────────────────────
    // Test 2 — Range must be [1..length], starting at 1 not 0
    // BreakPoint(0) = 0-char line = no progress = infinite loop risk
    // ────────────────────────────────────────────────────────────────────────
    @Test
    void shouldOfferBreakPointsFromOneToLengthInclusive() {
        String str = "HELLO"; // length = 5
        BreakAnywhereLineBreakStrategy strategy = new BreakAnywhereLineBreakStrategy(str);

        List<Integer> positions = collectAllPositions(strategy);

        assertThat(positions)
            .as("Expected [1,2,3,4,5] for '%s' — starts at 1 (not 0), ends at length", str)
            .containsExactly(1, 2, 3, 4, 5);
    }

    // ────────────────────────────────────────────────────────────────────────
    // Test 3 — Single character string
    // Must offer exactly [1] — the only valid break (after the single char)
    // ────────────────────────────────────────────────────────────────────────
    @Test
    void shouldOfferSingleBreakPointForSingleCharString() {
        BreakAnywhereLineBreakStrategy strategy = new BreakAnywhereLineBreakStrategy("A");

        List<Integer> positions = collectAllPositions(strategy);

        assertThat(positions)
            .as("Single-char string must offer exactly [1]")
            .containsExactly(1);
    }

    // ────────────────────────────────────────────────────────────────────────
    // Test 4 — Empty string
    // No characters → nothing to break → DonePoint immediately → empty list
    // ────────────────────────────────────────────────────────────────────────
    @Test
    void shouldOfferNoBreakPointsForEmptyString() {
        BreakAnywhereLineBreakStrategy strategy = new BreakAnywhereLineBreakStrategy("");

        List<Integer> positions = collectAllPositions(strategy);

        assertThat(positions)
            .as("Empty string has no characters to break → no positions offered")
            .isEmpty();
    }

    // ── Helper ───────────────────────────────────────────────────────────────

    private List<Integer> collectAllPositions(BreakAnywhereLineBreakStrategy strategy) {
        List<Integer> positions = new ArrayList<>();
        BreakPoint bp;
        while ((bp = strategy.next()).getPosition() != BreakIterator.DONE) {
            positions.add(bp.getPosition());
        }
        return positions;
    }
}