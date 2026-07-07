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
    //
    //  getHeight() lifecycle for table cells:
    //    1. calcDimensions:   setHeight(contentHeight)           e.g. 60
    //    2. calcLayoutHeight: setHeight(h + paddingTop + paddingBottom + ...)  → 80
    //    3. setCellHeights:   cell.setHeight(rowHeight)          → 80
    //
    //  So getHeight() == 80 is CORRECT after the patch (was 60 due to double-subtraction bug)
    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("display: table-cell — height")
    class TableCellHeight {

        /**
         * CSS:  height: 80px;  padding-top: 10px;  padding-bottom: 10px;
         *       box-sizing: border-box;
         *
         * Expected post-layout getHeight():
         *   calcDimensions:   contentHeight = 80 - 10 - 10 = 60        (patch subtracts ONCE)
         *   calcLayoutHeight: 60 + 10(top) + 10(bottom) = 80           (adds padding back)
         *   setCellHeights:   cell.setHeight(80)  ← row height
         *   getHeight() = 80  ✅
         *
         * WITHOUT the patch (double-subtract bug):
         *   calcDimensions:   contentHeight = (80-20) - 20 = 40        (subtracts TWICE)
         *   calcLayoutHeight: 40 + 20 = 60
         *   getHeight() = 60  ❌  (proves the bug existed)
         */
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

            // With the patch: 80px (correct — no double subtraction)
            // Without the patch: 60px (bug — getCSSHeight and calcDimensions both subtracted)
            assertThat(px(target.getHeight(), dpp))
                    .as("border-box height: getHeight() must equal CSS height (80px). "
                            + "If 60px, the double-subtraction bug is present.")
                    .isEqualTo(80);
        }

        /**
         * XHTML 1.0 legacy quirk for content-box table-cell height is preserved.
         *
         * CSS:  height: 80px;  padding: 10px;  box-sizing: content-box;
         *
         * Flying Saucer (XHTML 1.0 quirk): getCSSHeight always subtracts border+padding,
         * treating height as border-box even for content-box.
         *
         * calcDimensions:   contentHeight = 80 - 10 - 10 = 60   (quirk subtracts)
         * calcLayoutHeight: 60 + 10 + 10 = 80
         * getHeight() = 80  (quirk preserved by patch)
         */
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

            // Legacy XHTML 1.0 quirk: getCSSHeight subtracts padding even for content-box.
            // Our patch preserves this so existing templates don't break.
            assertThat(px(target.getHeight(), dpp))
                    .as("content-box table-cell (XHTML 1.0 quirk): getHeight() == 80")
                    .isEqualTo(80);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  3. MIN-WIDTH / MAX-WIDTH with border-box
    //
    //  Requires Patch D (applyCSSMinMaxWidth → protected) +
    //            Patch E (setLayoutWidth re-applies applyCSSMinMaxWidth)
    //  Without these patches, setLayoutWidth overwrites the clamped content width.
    // ─────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("min-width / max-width with border-box")
    class MinMaxWidth {

        /**
         * CSS:  width: 200px;  max-width: 80px;  padding: 20px;
         *       box-sizing: border-box;
         *
         * Browser: max-width is border-box → total ≤ 80px, content = 80-40 = 40px.
         *
         * WITHOUT Patch E: setLayoutWidth overwrites applyCSSMinMaxWidth → total = 200px ❌
         * WITH    Patch E: applyCSSMinMaxWidth re-applied after overwrite  → total = 80px  ✅
         */
        @Test
        @DisplayName("border-box: max-width constrains TOTAL width (requires Patch D+E)")
        void borderBox_maxWidth() throws Exception {
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
                    .as("max-width: 80px (border-box) → total width must be ≤ 80px")
                    .isLessThanOrEqualTo(80);

            assertThat(px(target.getContentWidth(), dpp))
                    .as("content = max-width (80) - left pad (20) - right pad (20) = 40px")
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
        @DisplayName("border-box: min-width enforces TOTAL width (requires Patch D+E)")
        void borderBox_minWidth() throws Exception {
            String html = xhtml(
                    """
                    .table  { display: table; table-layout: fixed; width: 500px; }
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

            assertThat(px(target.getWidth(), dpp))
                    .as("min-width: 120px (border-box) → total width must be ≥ 120px")
                    .isGreaterThanOrEqualTo(120);

            assertThat(px(target.getContentWidth(), dpp))
                    .as("content = min-width (120) - left pad (20) - right pad (20) = 80px")
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