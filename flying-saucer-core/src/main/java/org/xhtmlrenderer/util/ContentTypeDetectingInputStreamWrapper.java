package org.xhtmlrenderer.util;

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
public class ContentTypeDetectingInputStreamWrapper extends BufferedInputStream {
    protected static final int MAX_MAGIC_BYTES=4;
    private final byte[] firstBytes;

    public ContentTypeDetectingInputStreamWrapper(InputStream source) throws IOException {
        super(source);
        byte[] MAGIC_BYTES=new byte[MAX_MAGIC_BYTES];
        mark(MAX_MAGIC_BYTES);

        try {
            int bytesRead=read(MAGIC_BYTES);
            if (bytesRead<MAX_MAGIC_BYTES) { // Not enough data in stream
                if (bytesRead<=0) MAGIC_BYTES=new byte[0]; // no data
                else MAGIC_BYTES=Arrays.copyOf(MAGIC_BYTES, bytesRead); // fewer bytes
            }
            this.firstBytes = MAGIC_BYTES;
        }
        finally {
            reset();
        }
    }

    private boolean streamStartsWithMagicBytes(byte[] bytes) {
        if (firstBytes.length<bytes.length) return false;
        for (int i = 0; i < bytes.length; i++) {
            if (firstBytes[i]!=bytes[i]) return false;
        }
        return true;
    }

    private static final byte[] MAGIC_BYTES_PDF = "%PDF".getBytes(UTF_8);

    public boolean isPdf() {
        return streamStartsWithMagicBytes(MAGIC_BYTES_PDF);
    }

}
