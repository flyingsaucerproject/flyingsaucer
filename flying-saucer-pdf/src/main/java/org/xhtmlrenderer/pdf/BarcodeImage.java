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
import static java.nio.charset.StandardCharsets.ISO_8859_1;
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
 * (making bars taller doesn't make the barcode wider); 2D barcodes keep their aspect ratio when only one dimension is set.
 * <p>
 * Values that cannot be encoded exactly (e.g. letters in EAN-13, wrong number of digits,
 * non-ISO-8859-1 characters in PDF417 or Data Matrix) are rejected with a warning: no image is generated.
 */
public final class BarcodeImage {
    private static final Logger log = LoggerFactory.getLogger(BarcodeImage.class);

    public enum Type {
        CODE128(true, code -> linear(new Barcode128(), code)),
        CODE128_UCC(true, code -> linear(new Barcode128(), Barcode.CODE128_UCC, code)),
        CODE39(true, code -> linear(new Barcode39(), code)),
        CODE39_EXTENDED(true, code -> linear(extended(new Barcode39()), code)),
        CODABAR(true, code -> linear(new BarcodeCodabar(), code)),
        EAN13(true, code -> linear(new BarcodeEAN(), Barcode.EAN13, digits(code, 13))),
        EAN8(true, code -> linear(new BarcodeEAN(), Barcode.EAN8, digits(code, 8))),
        UPCA(true, code -> linear(new BarcodeEAN(), Barcode.UPCA, digits(code, 12))),
        UPCE(true, code -> linear(new BarcodeEAN(), Barcode.UPCE, digits(code, 8))),
        INTER25(true, code -> linear(new BarcodeInter25(), digits(code))),
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
            barcode.setText(latin1(code));
            return barcode.createAwtImage(BLACK, WHITE);
        }

        private static java.awt.@Nullable Image datamatrix(String code) {
            BarcodeDatamatrix barcode = new BarcodeDatamatrix();
            byte[] bytes = latin1(code);
            int status = barcode.generate(bytes, 0, bytes.length);
            if (status != DM_NO_ERROR) {
                log.warn("Failed to generate barcode DATAMATRIX for \"{}\": error code {}", code, status);
                return null;
            }
            return barcode.createAwtImage(BLACK, WHITE);
        }

        /**
         * OpenPDF silently ignores non-digit characters and extra digits, thus encoding a different value.
         */
        private static String digits(String code, int length) {
            if (code.length() != length) {
                throw new IllegalArgumentException("Expected %s digits, but got %s characters".formatted(length, code.length()));
            }
            return digits(code);
        }

        private static String digits(String code) {
            if (!code.chars().allMatch(c -> c >= '0' && c <= '9')) {
                throw new IllegalArgumentException("Expected only digits");
            }
            return code;
        }

        /**
         * PDF417 and Data Matrix are decoded as ISO-8859-1 by default, and OpenPDF cannot declare another encoding.
         * Java silently replaces unmappable characters, thus encoding a different value.
         */
        private static byte[] latin1(String code) {
            if (!ISO_8859_1.newEncoder().canEncode(code)) {
                throw new IllegalArgumentException("Only ISO-8859-1 characters are supported");
            }
            return code.getBytes(ISO_8859_1);
        }
    }

    @FunctionalInterface
    private interface Generator {
        java.awt.@Nullable Image generate(String code);
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
