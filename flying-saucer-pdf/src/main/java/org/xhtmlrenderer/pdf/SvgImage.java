package org.xhtmlrenderer.pdf;

import com.google.errorprone.annotations.CheckReturnValue;
import org.apache.batik.transcoder.Transcoder;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.jspecify.annotations.NonNull;
import org.openpdf.text.Image;
import org.xhtmlrenderer.extend.Size;
import org.xhtmlrenderer.util.XRRuntimeException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class SvgImage extends ITextFSImage {
    protected final byte[] image;
    protected final Size size;
    protected final String uri;

    @SuppressWarnings("DataFlowIssue")
    public SvgImage(byte[] image, Size size, String uri) {
        super(null); // bad design - just to keep backward compatibility (the original ITextFSImage API)
        this.image = image;
        this.size = size;
        this.uri = uri;
    }

    @Override
    public int getWidth() {
        return size.width();
    }

    @Override
    public int getHeight() {
        return size.height();
    }

    @NonNull
    @CheckReturnValue
    @Override
    public SvgImage scale(int width, int height) {
        return new SvgImage(image, size.scale(width, height), uri);
    }

    @Override
    public Image getImage() {
        Transcoder transcoder = new PNGTranscoder();
        transcoder.addTranscodingHint(PNGTranscoder.KEY_WIDTH, 1.0f * getWidth());
        transcoder.addTranscodingHint(PNGTranscoder.KEY_HEIGHT, 1.0f * getHeight());

        try (ByteArrayInputStream in = new ByteArrayInputStream(image)) {
            try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                transcoder.transcode(new TranscoderInput(in), new TranscoderOutput(out));
                out.flush();
                return createItextImage(out.toByteArray());
            }
        } catch (IOException | TranscoderException e) {
            throw new RuntimeException("Failed to convert SVG to PNG (%s)".formatted(uri), e);
        }
    }

    private static Image createItextImage(byte[] imageBytes) {
        try {
            return Image.getInstance(imageBytes);
        } catch (IOException e) {
            throw new XRRuntimeException(e.getMessage(), e);
        }
    }

    @Override
    @SuppressWarnings("MethodDoesntCallSuperMethod")
    public Object clone() {
        return this;
    }
}
