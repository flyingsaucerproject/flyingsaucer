package org.xhtmlrenderer.util;

import com.google.errorprone.annotations.CheckReturnValue;
import org.jspecify.annotations.Nullable;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * This class wraps an input stream and detects if it contains certain content using "magic numbers".
 *
 * <a href="http://en.wikipedia.org/wiki/Magic_number_(programming)">...</a>
 * <p>
 * currently only pdf detection is implemented
 *
 * @author mwyraz
 */
public final class ContentTypeDetectingInputStreamWrapper extends BufferedInputStream {
    private static final byte[] MAGIC_BYTES_PDF = "%PDF".getBytes(UTF_8);
    private static final byte[] MAGIC_BYTES_XML = "<?xm".getBytes(UTF_8);
    private static final byte[] MAGIC_BYTES_SVG = "<svg".getBytes(UTF_8);
    private static final int MAX_MAGIC_BYTES = 4;
    private static final byte[] NO_DATA = new byte[0];

    @Nullable
    @CheckReturnValue
    public static ContentTypeDetectingInputStreamWrapper detectContentType(@Nullable InputStream is) throws IOException {
        return is == null ? null : new ContentTypeDetectingInputStreamWrapper(is);
    }

    private final byte[] firstBytes;

    private static final byte[] UTF_8_BOM = { (byte) 0xEF, (byte) 0xBB, (byte) 0xBF };

    private ContentTypeDetectingInputStreamWrapper(InputStream source) throws IOException {
        super(source);
        skipUtf8BomIfPresent(this);
        this.firstBytes = readFirstBytes(this, MAX_MAGIC_BYTES);
    }

    /**
     * If the wrapped stream begins with a UTF-8 BOM (EF BB BF), consume it
     * so {@link #firstBytes} captures the real first glyph. Otherwise the
     * magic-byte comparison fails for BOM-prefixed XML / SVG written by
     * text editors that auto-prepend a BOM (e.g. Notepad on Windows), and
     * {@link #isSvg()} returns {@code false} for what is clearly an SVG.
     */
    private static void skipUtf8BomIfPresent(InputStream in) throws IOException {
        in.mark(UTF_8_BOM.length);
        byte[] head = new byte[UTF_8_BOM.length];
        int bytesRead = in.read(head);
        if (bytesRead == UTF_8_BOM.length && Arrays.equals(head, UTF_8_BOM)) {
            return; // BOM consumed; subsequent reads see "<svg…" / "<?xml…"
        }
        in.reset();
    }

    private static byte[] readFirstBytes(InputStream in, int count) throws IOException {
        in.mark(count);

        try {
            byte[] buffer = new byte[count];
            int bytesRead = in.read(buffer);
            return bytesRead >= count ?
                buffer :
                bytesRead <= 0 ? NO_DATA : Arrays.copyOf(buffer, bytesRead); // Not enough data in stream
        } finally {
            in.reset();
        }
    }

    private boolean streamStartsWithMagicBytes(byte[] bytes) {
        return Arrays.equals(firstBytes, bytes);
    }

    public boolean isPdf() {
        return streamStartsWithMagicBytes(MAGIC_BYTES_PDF);
    }

    public boolean isSvg() {
        // TODO Ignore leading comments in file, e.g.
        // <!--<?xml version="1.0" encoding="UTF-8" standalone="no"?>-->
        return streamStartsWithMagicBytes(MAGIC_BYTES_XML) || streamStartsWithMagicBytes(MAGIC_BYTES_SVG);
    }
}
