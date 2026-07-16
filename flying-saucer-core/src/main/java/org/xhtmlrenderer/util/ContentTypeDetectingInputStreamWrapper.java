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
    private static final byte[] COMMENT_START = "<!--".getBytes(UTF_8);
    private static final byte[] COMMENT_END = "-->".getBytes(UTF_8);
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

    public boolean isSvg() throws IOException {
        return streamStartsWithMagicBytes(MAGIC_BYTES_XML)
            || streamStartsWithMagicBytes(MAGIC_BYTES_SVG)
            || startsWithSvgAfterLeadingComments();
    }

    /**
     * Handles SVGs whose prolog opens with an XML comment (e.g. a real XML
     * editor or export tool commenting out the declaration:
     * {@code <!--<?xml version="1.0" encoding="UTF-8" standalone="no"?>-->})
     * before the {@code <svg>} or {@code <?xml} magic bytes.
     * <p>
     * Comments are scanned for and consumed incrementally rather than into a
     * fixed-size buffer, so there's no cap on how long a leading comment can
     * be. {@link #mark} covers the whole scan so that whatever gets read
     * while looking for the real start of the document is always undone by
     * {@link #reset} before returning — callers that go on to read the full
     * SVG body (e.g. for the Batik parse) still see it from the very start,
     * comments included.
     */
    private boolean startsWithSvgAfterLeadingComments() throws IOException {
        mark(Integer.MAX_VALUE);
        try {
            int b = skipWhitespace(read());
            while (b != -1) {
                byte[] rest = readNBytes(MAX_MAGIC_BYTES - 1);
                if (rest.length < MAX_MAGIC_BYTES - 1) {
                    return false; // fewer than 4 bytes left
                }
                if (matches(b, rest, COMMENT_START)) {
                    if (!skipToCommentEnd()) {
                        return false; // unterminated comment
                    }
                    b = skipWhitespace(read());
                    continue;
                }
                return matches(b, rest, MAGIC_BYTES_XML) || matches(b, rest, MAGIC_BYTES_SVG);
            }
            return false;
        } finally {
            reset();
        }
    }

    /** Compares {@code firstByte} followed by {@code rest} against a 4-byte magic pattern. */
    private static boolean matches(int firstByte, byte[] rest, byte[] pattern) {
        if (firstByte != pattern[0]) {
            return false;
        }
        for (int i = 0; i < rest.length; i++) {
            if (rest[i] != pattern[i + 1]) {
                return false;
            }
        }
        return true;
    }

    private int skipWhitespace(int b) throws IOException {
        while (b != -1 && isXmlWhitespace((byte) b)) {
            b = read();
        }
        return b;
    }

    private boolean skipToCommentEnd() throws IOException {
        int matched = 0;
        int b;
        while ((b = read()) != -1) {
            if (b == COMMENT_END[matched]) {
                matched++;
                if (matched == COMMENT_END.length) {
                    return true;
                }
            } else {
                matched = b == COMMENT_END[0] ? 1 : 0;
            }
        }
        return false;
    }

    private static boolean isXmlWhitespace(byte b) {
        return b == ' ' || b == '\t' || b == '\r' || b == '\n';
    }
}
