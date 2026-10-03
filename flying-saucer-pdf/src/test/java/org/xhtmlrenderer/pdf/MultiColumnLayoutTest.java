package org.xhtmlrenderer.pdf;

import com.codeborne.pdftest.PDF;
import org.apache.pdfbox.io.RandomAccessRead;
import org.apache.pdfbox.io.RandomAccessReadBuffer;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.assertj.core.data.Offset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.xhtmlrenderer.pdf.TestUtils.printFile;

public class MultiColumnLayoutTest {
    private static final Logger log = LoggerFactory.getLogger(MultiColumnLayoutTest.class);

    private static final List<String> LEFT_COLUMN_MARKERS = List.of(
            "COL-ALPHA", "COL-BETA", "COL-GAMMA", "COL-DELTA", "COL-EPSILON", "COL-ZETA");
    private static final List<String> RIGHT_COLUMN_MARKERS = List.of(
            "COL-ETA", "COL-THETA", "COL-IOTA", "COL-KAPPA", "COL-LAMBDA", "COL-MU");

    @Test
    void multiColumnCssParsesAndRendersIntoPdf() throws IOException {
        byte[] bytes = Html2Pdf.fromClasspathResource("page-with-columns.html");
        PDF pdf = printFile(log, bytes, "page-with-columns.pdf");
        com.codeborne.pdftest.assertj.Assertions.assertThat(pdf)
                .containsText("COL-ALPHA", "COL-BETA", "COL-GAMMA", "COL-DELTA");

        List<String> allMarkers = new ArrayList<>(LEFT_COLUMN_MARKERS.size() + RIGHT_COLUMN_MARKERS.size());
        allMarkers.addAll(LEFT_COLUMN_MARKERS);
        allMarkers.addAll(RIGHT_COLUMN_MARKERS);
        Map<String, Float> xByMarker = extractMarkerXCoordinates(bytes, allMarkers);

        assertThat(xByMarker).hasSize(12);

        float leftBand = averageX(xByMarker, LEFT_COLUMN_MARKERS);
        float rightBand = averageX(xByMarker, RIGHT_COLUMN_MARKERS);
        float maxLeft = maxX(xByMarker, LEFT_COLUMN_MARKERS);
        float minRight = minX(xByMarker, RIGHT_COLUMN_MARKERS);
        assertThat(maxLeft).isLessThan(minRight - 20f);
        assertThat(leftBand).isLessThan(rightBand - 20f);

        for (String m : LEFT_COLUMN_MARKERS) {
            assertThat(xByMarker.get(m)).isCloseTo(leftBand, Offset.offset(8f));
        }
        for (String m : RIGHT_COLUMN_MARKERS) {
            assertThat(xByMarker.get(m)).isCloseTo(rightBand, Offset.offset(8f));
        }
    }

    private static float averageX(Map<String, Float> xByMarker, List<String> markers) {
        double sum = 0;
        for (String m : markers) {
            sum += xByMarker.get(m);
        }
        return (float) (sum / markers.size());
    }

    private static float maxX(Map<String, Float> xByMarker, List<String> markers) {
        float max = Float.NEGATIVE_INFINITY;
        for (String m : markers) {
            max = Math.max(max, xByMarker.get(m));
        }
        return max;
    }

    private static float minX(Map<String, Float> xByMarker, List<String> markers) {
        float min = Float.POSITIVE_INFINITY;
        for (String m : markers) {
            min = Math.min(min, xByMarker.get(m));
        }
        return min;
    }

    private static Map<String, Float> extractMarkerXCoordinates(byte[] pdfBytes, List<String> markers) throws IOException {
        try (RandomAccessRead source = new RandomAccessReadBuffer(pdfBytes);
             PDDocument document = new PDFParser(source).parse()) {
            MarkerPositionStripper stripper = new MarkerPositionStripper(markers);
            stripper.getText(document);
            return stripper.positions();
        }
    }

    private static final class MarkerPositionStripper extends PDFTextStripper {
        private final List<String> markers;
        private final Map<String, Float> positions = new HashMap<>();

        private MarkerPositionStripper(List<String> markers) throws IOException {
            this.markers = markers;
            setSortByPosition(true);
        }

        @Override
        protected void writeString(String text, List<TextPosition> textPositions) throws IOException {
            super.writeString(text, textPositions);
            for (String marker : markers) {
                if (positions.containsKey(marker)) {
                    continue;
                }
                int idx = text.indexOf(marker);
                if (idx >= 0 && idx + marker.length() <= textPositions.size()) {
                    float minX = Float.POSITIVE_INFINITY;
                    for (int i = 0; i < marker.length(); i++) {
                        minX = Math.min(minX, textPositions.get(idx + i).getXDirAdj());
                    }
                    positions.put(marker, minX);
                }
            }
        }

        private Map<String, Float> positions() {
            return positions;
        }
    }
}
