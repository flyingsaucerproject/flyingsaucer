package org.xhtmlrenderer.pdf;

import org.jspecify.annotations.Nullable;
import org.openpdf.text.Image;
import org.openpdf.text.pdf.Barcode;
import org.openpdf.text.pdf.Barcode128;
import org.openpdf.text.pdf.Barcode39;
import org.openpdf.text.pdf.BarcodeCodabar;
import org.openpdf.text.pdf.BarcodeDatamatrix;
import org.openpdf.text.pdf.BarcodeEAN;
import org.openpdf.text.pdf.BarcodeInter25;
import org.openpdf.text.pdf.BarcodePDF417;
import org.openpdf.text.pdf.BarcodePostnet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xhtmlrenderer.extend.FSImage;

import java.io.IOException;
import java.util.Map;

import static java.awt.Color.BLACK;
import static java.awt.Color.WHITE;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Arrays.stream;
import static java.util.Locale.ROOT;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toUnmodifiableMap;
import static org.openpdf.text.pdf.BarcodeDatamatrix.DM_NO_ERROR;

/**
 * Generates barcode images on the fly, using the barcode generators bundled with OpenPDF.
 * <p>
 * Used by {@link ITextReplacedElementFactory} to replace an image like
 * <pre>{@code <img data-barcode="code128" src="PR12345678" style="height: 100px"/>}</pre>
 * by a generated barcode. The {@code src} attribute contains the value to be encoded,
 * the {@code data-barcode} attribute contains the barcode type (see {@link Type}).
 * <p>
 * Each barcode module (the narrowest bar or the smallest square) is 1 CSS pixel wide.
 * Use CSS {@code width} / {@code height} to resize it.
 * For linear (1D) barcodes, setting only one dimension doesn't change the other one
 * (making bars taller doesn't make the barcode wider); 2D barcodes keep their aspect ratio.
 */
public final class BarcodeImage {
    private static final Logger log = LoggerFactory.getLogger(BarcodeImage.class);

    public enum Type {
        CODE128(true, code -> linear(new Barcode128(), code)),
        CODE128_UCC(true, code -> linear(new Barcode128(), Barcode.CODE128_UCC, code)),
        CODE39(true, code -> linear(new Barcode39(), code)),
        CODE39_EXTENDED(true, code -> linear(extended(new Barcode39()), code)),
        CODABAR(true, code -> linear(new BarcodeCodabar(), code)),
        EAN13(true, code -> linear(new BarcodeEAN(), Barcode.EAN13, code)),
        EAN8(true, code -> linear(new BarcodeEAN(), Barcode.EAN8, code)),
        UPCA(true, code -> linear(new BarcodeEAN(), Barcode.UPCA, code)),
        UPCE(true, code -> linear(new BarcodeEAN(), Barcode.UPCE, code)),
        INTER25(true, code -> linear(new BarcodeInter25(), code)),
        POSTNET(true, code -> linear(new BarcodePostnet(), code)),
        PLANET(true, code -> linear(new BarcodePostnet(), Barcode.PLANET, code)),
        PDF417(false, Type::pdf417),
        DATAMATRIX(false, Type::datamatrix);

        private static final Map<String, Type> BY_NAME = stream(values()).collect(toUnmodifiableMap(Type::name, identity()));

        private final boolean linear;
        private final Generator generator;

        Type(boolean linear, Generator generator) {
            this.linear = linear;
            this.generator = generator;
        }

        /**
         * @return true for linear (1D) barcodes, false for 2D barcodes
         */
        public boolean linear() {
            return linear;
        }

        /**
         * @param name case-insensitive barcode type, e.g. "code128", "ean13", "datamatrix"
         * @return the barcode type, or {@code null} if {@code name} is not a supported barcode type
         */
        public static @Nullable Type of(@Nullable String name) {
            if (name == null || name.isBlank()) return null;
            return BY_NAME.get(name.trim().toUpperCase(ROOT).replace('-', '_'));
        }

        private static java.awt.Image linear(Barcode barcode, String code) {
            barcode.setCode(code);
            return barcode.createAwtImage(BLACK, WHITE);
        }

        private static java.awt.Image linear(Barcode barcode, int codeType, String code) {
            barcode.setCodeType(codeType);
            return linear(barcode, code);
        }

        private static Barcode extended(Barcode barcode) {
            barcode.setExtended(true);
            return barcode;
        }

        private static java.awt.Image pdf417(String code) {
            BarcodePDF417 barcode = new BarcodePDF417();
            barcode.setText(code.getBytes(UTF_8));
            return barcode.createAwtImage(BLACK, WHITE);
        }

        private static java.awt.@Nullable Image datamatrix(String code) throws IOException {
            BarcodeDatamatrix barcode = new BarcodeDatamatrix();
            int status = barcode.generate(code);
            if (status != DM_NO_ERROR) {
                log.warn("Failed to generate barcode DATAMATRIX for \"{}\": error code {}", code, status);
                return null;
            }
            return barcode.createAwtImage(BLACK, WHITE);
        }
    }

    @FunctionalInterface
    private interface Generator {
        java.awt.@Nullable Image generate(String code) throws IOException;
    }

    private BarcodeImage() {
    }

    /**
     * @param type barcode type
     * @param code the value to be encoded
     * @param dotsPerPixel output resolution (see {@link org.xhtmlrenderer.layout.SharedContext#getDotsPerPixel()})
     * @return the generated barcode image, or {@code null} if {@code code} cannot be encoded with given barcode type
     */
    public static @Nullable FSImage create(Type type, String code, int dotsPerPixel) {
        try {
            java.awt.Image awtImage = type.generator.generate(code);
            if (awtImage == null) return null;
            Image image = Image.getInstance(awtImage, null, true);
            image.scaleAbsolute(image.getPlainWidth() * dotsPerPixel, image.getPlainHeight() * dotsPerPixel);
            return new ITextFSImage(image);
        } catch (RuntimeException | IOException e) {
            log.warn("Failed to generate barcode {} for \"{}\": {}", type, code, e.toString());
            return null;
        }
    }
}
