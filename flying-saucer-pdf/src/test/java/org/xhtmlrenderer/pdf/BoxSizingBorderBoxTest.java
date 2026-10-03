package org.xhtmlrenderer.pdf;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.xhtmlrenderer.render.Box;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies that box-sizing: border-box in Flying Saucer matches browser (standards mode).
 *
 * KEY API FACTS (verified from Box.java + TableRowBox.java source):
 *
 *  box.getWidth()         = contentWidth + leftMBP + rightMBP   (total rendered width)
 *  box.getContentWidth()  = content area only
 *  box.getHeight()        = contentHeight + padding + border     (total rendered height)
 *                           ↑ set AFTER calcLayoutHeight() adds padding/border,
 *                             then OVERWRITTEN by TableRowBox.setCellHeights() with row height.
 *
 * WHY height assertions expect 80 not 60:
 *   calcDimensions sets contentHeight = 60 (border-box: 80 - 10top - 10bottom)
 *   calcLayoutHeight ADDS padding+border back: 60 + 10 + 10 = 80
 *   setCellHeights then sets cell.setHeight(rowHeight) = 80
 *   So getHeight() = 80 is CORRECT with the patch. The BUG (without patch) gives 60.
 *
 * IMPORTANT — min-width / max-width and table-layout:
 *   CSS 2.1 §17.5.2 leaves the effect of min/max-width on table cells undefined.
 *   Chrome and Firefox IGNORE max-width / min-width on td in table-layout:fixed —
 *   the column-allocated width is authoritative, regardless of box-sizing.
 *   In table-layout:auto (advisory widths) both browsers DO respect max/min-width
 *   for every box-sizing value.
 *   Section 3 tests are therefore split into two sub-groups accordingly.
 *
 * NOTE — applyCSSMinMaxWidth and border-box adjustment:
 *
 *   Both max-width and min-width in border-box mode have paddingBorderWidth
 *   subtracted correctly (BlockBox.getCSSMaxWidth / getCSSMinWidth):
 *     max-width:80px, padding:20px each side →
 *       contentWidth = 80 − 40 = 40   ✅ matches browsers
 *       getWidth()   = 40 + 40  = 80
 *     min-width:120px, padding:20px each side →
 *       contentWidth = 120 − 40 = 80  ✅ matches browsers
 *       getWidth()   = 80 + 40  = 120
 */
@DisplayName("box-sizing: border-box")
class BoxSizingBorderBoxTest {

    private static final String RESET_CSS = """
            * {
                margin: 0;
                padding: 0;
                box-sizing: border-box;
            }
            """;

    private static String xhtml(String css, String body) {
        return """
                <?xml version="1.0" encoding="UTF-8"?>
                <!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"
                        "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
                <html xmlns="http://www.w3.org/1999/xhtml">
                  <head>
                    <style type="text/css">
                """ + RESET_CSS + css + """
                    </style>
                  </head>
                  <body>
                """ + body + """
                  </body>
                </html>
                """;
    }

    private static int px(int dots, int dotsPerPixel) {
        return dots / dotsPerPixel;
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  1. TABLE-CELL WIDTH
    //  NOTE: must use table-layout: fixed — auto layout uses calcMinMaxWidth()
    //  which bypasses getOuterStyleWidth() and ignores the border-box patch.
    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("display: table-cell — width")
    class TableCellWidth {

        /**
         * THE ORIGINAL BUG: border-box was ignored so getWidth() returned 110.
         *
         * CSS:  width: 100px;  padding-right: 10px;  box-sizing: border-box;
         *
         * Browser (standards mode):
         *   total width   = 100 px  (padding IS inside the width)
         *   content width =  90 px
         *
         * Flying Saucer BEFORE patch: total = 110 px ❌
         * Flying Saucer AFTER patch:  total = 100 px ✅
         */
        @Test
        @DisplayName("border-box: padding-right only — total width == CSS width")
        void borderBox_rightPaddingOnly() throws Exception {
            String html = xhtml(
                """
                .table  { display: table; table-layout: fixed; width: 300px; }
                .cell   { display: table-cell; }
                #target { width: 100px; padding-right: 10px; }
                """,
                """
                <div class="table">
                  <div class="cell" id="target">Label</div>
                  <div class="cell" id="filler">Value</div>
                </div>
                """
            );

            ITextRenderer renderer = new ITextRenderer();
            renderer.setDocumentFromString(html);
            renderer.layout();

            int dpp = renderer.getSharedContext().getDotsPerPixel();
            Box target = renderer.getSharedContext().getBoxById("target");

            assertThat(px(target.getWidth(), dpp))
                .as("total width (border-box) must equal CSS width of 100px")
                .isEqualTo(100);

            assertThat(px(target.getContentWidth(), dpp))
                .as("content = CSS width (100) - right padding (10) = 90px")
                .isEqualTo(90);
        }

        /**
         * CSS:  width: 100px;  padding: 10px (all sides);  box-sizing: border-box;
         *
         * Browser:
         *   total width   = 100 px
         *   content width =  80 px  (100 - 10left - 10right)
         *
         * IMPORTANT: table-layout: fixed required. Without it, auto table layout
         * uses calcMinMaxWidth() which ignores getOuterStyleWidth() for initial
         * column sizing, causing the cell to render at 120px instead of 100px.
         */
        @Test
        @DisplayName("border-box: padding all sides — total width == CSS width")
        void borderBox_allSidesPadding() throws Exception {
            String html = xhtml(
                """
                .table  { display: table; table-layout: fixed; width: 300px; }
                .cell   { display: table-cell; }
                #target { width: 100px; padding: 10px; }
                """,
                """
                <div class="table">
                  <div class="cell" id="target">Label</div>
                  <div class="cell" id="filler">Value</div>
                </div>
                """
            );

            ITextRenderer renderer = new ITextRenderer();
            renderer.setDocumentFromString(html);
            renderer.layout();

            int dpp = renderer.getSharedContext().getDotsPerPixel();
            Box target = renderer.getSharedContext().getBoxById("target");

            assertThat(px(target.getWidth(), dpp))
                .as("total width must equal CSS width (border-box): 100px")
                .isEqualTo(100);

            assertThat(px(target.getContentWidth(), dpp))
                .as("content = 100 - 10 (left) - 10 (right) = 80px")
                .isEqualTo(80);
        }

        /**
         * Sanity: content-box must NOT be broken by the patch.
         *
         * CSS:  width: 100px;  padding-right: 10px;  box-sizing: content-box;
         *
         * Browser: total = 110 px, content = 100 px
         */
        @Test
        @DisplayName("content-box: padding added OUTSIDE CSS width (no regression)")
        void contentBox_noRegression() throws Exception {
            String html = xhtml(
                """
                .table  { display: table; table-layout: fixed; width: 300px; }
                #target {
                    display: table-cell;
                    box-sizing: content-box;
                    width: 100px;
                    padding-right: 10px;
                }
                #filler { display: table-cell; }
                """,
                """
                <div class="table">
                  <div id="target">Label</div>
                  <div id="filler">Value</div>
                </div>
                """
            );

            ITextRenderer renderer = new ITextRenderer();
            renderer.setDocumentFromString(html);
            renderer.layout();

            int dpp = renderer.getSharedContext().getDotsPerPixel();
            Box target = renderer.getSharedContext().getBoxById("target");

            assertThat(px(target.getWidth(), dpp))
                .as("content-box: total = CSS width (100) + padding (10) = 110px")
                .isEqualTo(110);

            assertThat(px(target.getContentWidth(), dpp))
                .as("content-box: content == CSS width exactly")
                .isEqualTo(100);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  2. TABLE-CELL HEIGHT
    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("display: table-cell — height")
    class TableCellHeight {

        @Test
        @DisplayName("border-box: getHeight() == CSS height (no double-subtraction)")
        void borderBox_height_equalsCSS() throws Exception {
            String html = xhtml(
                """
                .table  { display: table; table-layout: fixed; width: 300px; }
                .cell   { display: table-cell; }
                #target { height: 80px; padding-top: 10px; padding-bottom: 10px; }
                """,
                """
                <div class="table">
                  <div class="cell" id="target">&#160;</div>
                  <div class="cell" id="filler">&#160;</div>
                </div>
                """
            );

            ITextRenderer renderer = new ITextRenderer();
            renderer.setDocumentFromString(html);
            renderer.layout();

            int dpp = renderer.getSharedContext().getDotsPerPixel();
            Box target = renderer.getSharedContext().getBoxById("target");

            assertThat(px(target.getHeight(), dpp))
                .as("border-box height: getHeight() must equal CSS height (80px). "
                    + "If 60px, the double-subtraction bug is present.")
                .isEqualTo(80);
        }

        @Test
        @DisplayName("content-box: XHTML 1.0 quirk preserved — getHeight() still == 80")
        void contentBox_legacyQuirkPreserved() throws Exception {
            String html = xhtml(
                """
                .table  { display: table; table-layout: fixed; width: 300px; }
                .cell   { display: table-cell; }
                #target { box-sizing: content-box; height: 80px; padding-top: 10px; padding-bottom: 10px; }
                """,
                """
                <div class="table">
                  <div class="cell" id="target">&#160;</div>
                  <div class="cell" id="filler">&#160;</div>
                </div>
                """
            );

            ITextRenderer renderer = new ITextRenderer();
            renderer.setDocumentFromString(html);
            renderer.layout();

            int dpp = renderer.getSharedContext().getDotsPerPixel();
            Box target = renderer.getSharedContext().getBoxById("target");

            assertThat(px(target.getHeight(), dpp))
                .as("content-box table-cell (XHTML 1.0 quirk): getHeight() == 80")
                .isEqualTo(80);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  3. MIN-WIDTH / MAX-WIDTH with border-box
    //
    //  A. table-layout: auto  — applyCSSMinMaxWidth IS called.
    //  B. table-layout: fixed — applyCSSMinMaxWidth NOT called; col width wins.
    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("min-width / max-width with border-box")
    class MinMaxWidth {

        // ── A. table-layout: auto ─────────────────────────────────────────────

        /**
         * CSS:  width: 200px;  max-width: 80px;  padding: 20px each side;
         *       box-sizing: border-box;  table-layout: auto
         *
         * applyCSSMinMaxWidth correctly applies border-box adjustment for max-width:
         *   contentWidth = max-width − paddingBorderWidth = 80 − 40 = 40px  ✅
         *   getWidth()   = 40 (content) + 40 (padding)                      = 80px
         *
         * Regression guard: if applyCSSMinMaxWidth is NOT called (isBorderBox()
         * guard regression), the cell expands to its full auto-layout allocation
         * (≫ 80px) because max-width is silently ignored for content-box cells.
         */
        @Test
        @DisplayName("auto-layout, border-box: max-width constrains TOTAL width")
        void borderBox_maxWidth_autoLayout() throws Exception {
            String html = xhtml(
                """
                .table  { display: table; table-layout: auto; width: 500px; }
                .cell   { display: table-cell; }
                #target { width: 200px; max-width: 80px; padding-left: 20px; padding-right: 20px; }
                """,
                """
                <div class="table">
                  <div class="cell" id="target">x</div>
                  <div class="cell" id="filler">&#160;</div>
                </div>
                """
            );

            ITextRenderer renderer = new ITextRenderer();
            renderer.setDocumentFromString(html);
            renderer.layout();

            int dpp = renderer.getSharedContext().getDotsPerPixel();
            Box target = renderer.getSharedContext().getBoxById("target");

            // applyCSSMinMaxWidth is called (auto-layout) and correctly applies
            // border-box adjustment: total = max-width = 80px.
            // Regression: if skipped (isBorderBox() guard), total ≫ 80px.
            assertThat(px(target.getWidth(), dpp))
                .as("auto-layout, max-width:80px (border-box) → total must be 80px. "
                    + "A value much larger than 80 means applyCSSMinMaxWidth was "
                    + "not called (isBorderBox() guard regression).")
                .isEqualTo(80);

            // applyCSSMinMaxWidth correctly subtracts paddingBorderWidth for max-width:
            //   content = max-width (80) − left pad (20) − right pad (20) = 40px  ✅
            assertThat(px(target.getContentWidth(), dpp))
                .as("content = max-width (80) − left pad (20) − right pad (20) = 40px. "
                    + "applyCSSMinMaxWidth correctly applies border-box adjustment "
                    + "for max-width.")
                .isEqualTo(40);
        }

        /**
         * CSS:  width: 30px;  min-width: 120px;  padding: 20px;
         *       box-sizing: border-box;
         *
         * Browser: min-width is border-box → total ≥ 120px, content = 120-40 = 80px.
         *
         * WITHOUT Patch E: total = 30px (overwritten), content = -10px (clamped to 0) ❌
         * WITH    Patch E: total = 120px, content = 80px ✅
         */
        @Test
        @DisplayName("auto-layout, border-box: min-width enforces TOTAL width")
        void borderBox_minWidth_autoLayout() throws Exception {
            String html = xhtml(
                """
                .table  { display: table; table-layout: auto; width: 500px; }
                .cell   { display: table-cell; }
                #target { width: 30px; min-width: 120px; padding-left: 20px; padding-right: 20px; }
                """,
                """
                <div class="table">
                  <div class="cell" id="target">x</div>
                  <div class="cell" id="filler">&#160;</div>
                </div>
                """
            );

            ITextRenderer renderer = new ITextRenderer();
            renderer.setDocumentFromString(html);
            renderer.layout();

            int dpp = renderer.getSharedContext().getDotsPerPixel();
            Box target = renderer.getSharedContext().getBoxById("target");

            // getCSSMinWidth now correctly subtracts paddingBorderWidth (40) for border-box.
            // content floor = 120 − 40 = 80; total = 80 + 40 = 120. Matches Chrome + Firefox.
            assertThat(px(target.getWidth(), dpp))
                .as("auto-layout, min-width:120px (border-box) → total must be 120px. "
                    + "If 160px, getCSSMinWidth is not subtracting paddingBorderWidth "
                    + "for border-box (bug). If < 120px, applyCSSMinMaxWidth was not "
                    + "called at all (isFixedWidthAdvisoryOnly() guard regression).")
                .isEqualTo(120);

            assertThat(px(target.getContentWidth(), dpp))
                .as("content = min-width (120) − left pad (20) − right pad (20) = 80px. "
                    + "getCSSMinMaxWidth correctly converts border-box min-width to "
                    + "a content-width floor by subtracting paddingBorderWidth.")
                .isEqualTo(80);
        }

        // ── B. table-layout: fixed ────────────────────────────────────────────

        /**
         * table-layout:fixed, border-box: col-allocated width (200px) must win
         * over max-width:80px.
         *
         * With isFixedWidthAdvisoryOnly() guard: applyCSSMinMaxWidth skipped → 200px. ✅
         * With isBorderBox() guard (regression): applyCSSMinMaxWidth called  → 80px.  ❌
         */
        @Test
        @DisplayName("fixed-layout, border-box: col-allocated width wins over max-width")
        void borderBox_maxWidth_fixedLayout_colWidthWins() throws Exception {
            String html = xhtml(
                """
                .table  { display: table; table-layout: fixed; width: 500px; }
                .cell   { display: table-cell; }
                #target { width: 200px; max-width: 80px; padding-left: 20px; padding-right: 20px; }
                """,
                """
                <div class="table">
                  <div class="cell" id="target">x</div>
                  <div class="cell" id="filler">&#160;</div>
                </div>
                """
            );

            ITextRenderer renderer = new ITextRenderer();
            renderer.setDocumentFromString(html);
            renderer.layout();

            int dpp = renderer.getSharedContext().getDotsPerPixel();
            Box target = renderer.getSharedContext().getBoxById("target");

            assertThat(px(target.getWidth(), dpp))
                .as("fixed-layout, border-box: col-allocated width (200px) must win "
                    + "over max-width:80px. "
                    + "If ≤ 80px, applyCSSMinMaxWidth was incorrectly called "
                    + "(isBorderBox() guard regression).")
                .isEqualTo(200);
        }

        /**
         * table-layout:fixed, border-box: col-allocated width (80px) must win
         * over min-width:200px.
         *
         * With isFixedWidthAdvisoryOnly() guard: applyCSSMinMaxWidth skipped → 80px.  ✅
         * With isBorderBox() guard (regression): applyCSSMinMaxWidth called  → 200px. ❌
         */
        @Test
        @DisplayName("fixed-layout, border-box: col-allocated width wins over min-width")
        void borderBox_minWidth_fixedLayout_colWidthWins() throws Exception {
            String html = xhtml(
                """
                .table  { display: table; table-layout: fixed; width: 500px; }
                .cell   { display: table-cell; }
                #target { width: 80px; min-width: 200px; padding-left: 10px; padding-right: 10px; }
                """,
                """
                <div class="table">
                  <div class="cell" id="target">x</div>
                  <div class="cell" id="filler">&#160;</div>
                </div>
                """
            );

            ITextRenderer renderer = new ITextRenderer();
            renderer.setDocumentFromString(html);
            renderer.layout();

            int dpp = renderer.getSharedContext().getDotsPerPixel();
            Box target = renderer.getSharedContext().getBoxById("target");

            assertThat(px(target.getWidth(), dpp))
                .as("fixed-layout, border-box: col-allocated width (80px) must win "
                    + "over min-width:200px. "
                    + "If ≥ 200px, applyCSSMinMaxWidth was incorrectly called "
                    + "(isBorderBox() guard regression).")
                .isEqualTo(80);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  4. DISPLAY: BLOCK — no regression
    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("display: block — no regression")
    class RegularBlock {

        @Test
        @DisplayName("border-box on display:block was already supported — must remain correct")
        void blockBorderBox_unaffected() throws Exception {
            String html = xhtml(
                """
                #target { display: block; width: 100px; padding-left: 10px; padding-right: 10px; }
                """,
                """
                <div id="target">x</div>
                """
            );

            ITextRenderer renderer = new ITextRenderer();
            renderer.setDocumentFromString(html);
            renderer.layout();

            int dpp = renderer.getSharedContext().getDotsPerPixel();
            Box target = renderer.getSharedContext().getBoxById("target");

            assertThat(px(target.getWidth(), dpp)).isEqualTo(100);
            assertThat(px(target.getContentWidth(), dpp)).isEqualTo(80);
        }
    }
}