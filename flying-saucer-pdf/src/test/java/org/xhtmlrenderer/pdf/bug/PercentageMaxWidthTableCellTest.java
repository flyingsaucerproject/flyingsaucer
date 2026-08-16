package org.xhtmlrenderer.pdf.bug;

import org.junit.jupiter.api.Test;
import org.xhtmlrenderer.newtable.TableCellBox;
import org.xhtmlrenderer.pdf.ITextRenderer;
import org.xhtmlrenderer.render.BlockBox;
import org.xhtmlrenderer.render.Box;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.Percentage.withPercentage;

/**
 * Reproducible example for <a href="https://github.com/flyingsaucerproject/flyingsaucer/issues/697">issue 697</a>:
 * a percentage {@code max-width} on a table cell used to resolve against a containing block
 * (the table row) whose content width had not been established yet, so it was always clamped to zero.
 */
class PercentageMaxWidthTableCellTest {
    @Test
    void percentageMaxWidthResolvesAgainstTheTableWidth() {
        String page = """
            <html><head><style>
              table { width: 600pt; }
            </style></head>
            <body>
            <table><tr><td style="max-width: 60pt">fixed length</td></tr></table>
            <table><tr><td style="max-width: 10%">percentage</td></tr></table>
            </body></html>""";

        ITextRenderer renderer = new ITextRenderer();
        renderer.setDocumentFromString(page);
        renderer.layout();

        BlockBox root = renderer.getRootBox();
        TableCellBox fixedLengthCell = (TableCellBox) findFirst(root, TableCellBox.class);
        TableCellBox percentageCell = (TableCellBox) findLast(root, TableCellBox.class);

        // Not byte-exact: the table layout algorithm allocates border-spacing per column, so a
        // percentage of the table's overall width is not pixel-identical to an equivalent fixed
        // length. It must, however, be in the same ballpark, not resolve to (near) zero.
        assertThat(percentageCell.getContentWidth()).isCloseTo(fixedLengthCell.getContentWidth(), withPercentage(5));
    }

    private static Box findFirst(Box box, Class<?> type) {
        if (type.isInstance(box)) return box;
        for (int i = 0; i < box.getChildCount(); i++) {
            Box found = findFirst(box.getChild(i), type);
            if (found != null) return found;
        }
        return null;
    }

    private static Box findLast(Box box, Class<?> type) {
        Box last = null;
        if (type.isInstance(box)) last = box;
        for (int i = 0; i < box.getChildCount(); i++) {
            Box found = findLast(box.getChild(i), type);
            if (found != null) last = found;
        }
        return last;
    }
}
