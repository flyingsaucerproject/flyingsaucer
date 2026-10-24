package org.xhtmlrenderer.pdf;

import com.google.errorprone.annotations.CheckReturnValue;
import org.apache.batik.anim.dom.SAXSVGDocumentFactory;
import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.bridge.DocumentLoader;
import org.apache.batik.bridge.GVTBuilder;
import org.apache.batik.bridge.UserAgentAdapter;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.batik.transcoder.Transcoder;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.apache.batik.util.XMLResourceDescriptor;
import org.openpdf.text.Image;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.svg.SVGDocument;
import org.xhtmlrenderer.extend.Size;
import org.xhtmlrenderer.util.XRRuntimeException;

import java.awt.Graphics2D;
import java.awt.geom.Dimension2D;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SvgImage extends ITextFSImage {
    private static final Logger log = LoggerFactory.getLogger(SvgImage.class);

    /**
     * SVG elements that {@link org.openpdf.text.pdf.PdfGraphics2D} cannot render as vector graphics:
     * it would rasterize them in low resolution.
     */
    private static final Set<String> RASTER_ONLY_ELEMENTS = Set.of("linearGradient", "radialGradient", "pattern", "filter", "mask");
    private static final Pattern RE_OPACITY = Pattern.compile("(?<![\\w-])opacity\\s*:\\s*([\\d.]+)(%?)");

    protected final byte[] image;
    protected final Size size;
    protected final String uri;
    private final boolean drawableAsVector;

    public SvgImage(byte[] image, Size size, String uri) {
        this(image, size, uri, isDrawableAsVector(image, uri));
    }

    @SuppressWarnings("DataFlowIssue")
    private SvgImage(byte[] image, Size size, String uri, boolean drawableAsVector) {
        super(null); // bad design - just to keep backward compatibility (the original ITextFSImage API)
        this.image = image;
        this.size = size;
        this.uri = uri;
        this.drawableAsVector = drawableAsVector;
    }

    @Override
    public int getWidth() {
        return size.width();
    }

    @Override
    public int getHeight() {
        return size.height();
    }

    @CheckReturnValue
    @Override
    public SvgImage scale(int width, int height) {
        return new SvgImage(image, size.scale(width, height), uri, drawableAsVector);
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

    /**
     * @return true if this SVG can be painted with {@link #draw(Graphics2D, float, float)} without losing quality.
     * Otherwise, it should be rasterized with {@link #getImage()}.
     */
    boolean isDrawableAsVector() {
        return drawableAsVector;
    }

    private static boolean isDrawableAsVector(byte[] image, String uri) {
        try {
            return isDrawableAsVector(parseSvg(image, uri));
        } catch (RuntimeException e) {
            log.debug("Failed to parse SVG ({}), will rasterize it", uri, e);
            return false;
        }
    }

    private static boolean isDrawableAsVector(SVGDocument document) {
        NodeList elements = document.getElementsByTagNameNS("*", "*");
        for (int i = 0; i < elements.getLength(); i++) {
            Element element = (Element) elements.item(i);
            if (RASTER_ONLY_ELEMENTS.contains(element.getLocalName())
                || isTranslucent("opacity:" + element.getAttribute("opacity"))
                || isTranslucent(element.getAttribute("style"))
                || ("style".equals(element.getLocalName()) && isTranslucent(element.getTextContent()))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Group opacity is not supported by {@link org.openpdf.text.pdf.PdfGraphics2D}
     * (unlike {@code fill-opacity} and {@code stroke-opacity} which work fine).
     */
    private static boolean isTranslucent(String css) {
        Matcher matcher = RE_OPACITY.matcher(css);
        while (matcher.find()) {
            try {
                double opacity = Double.parseDouble(matcher.group(1));
                if (opacity < ("%".equals(matcher.group(2)) ? 100 : 1)) {
                    return true;
                }
            } catch (NumberFormatException e) {
                return true;
            }
        }
        return false;
    }

    /**
     * Paints this SVG as vector graphics, scaled to fit the given box.
     * <p>
     * Unlike {@link #getImage()}, it doesn't rasterize the SVG,
     * which is much faster and gives sharp output at any zoom level.
     *
     * @param g      target graphics, with origin at the top-left corner of the image
     * @param width  target width (in units of {@code g})
     * @param height target height (in units of {@code g})
     */
    void draw(Graphics2D g, float width, float height) {
        SvgUserAgent userAgent = new SvgUserAgent(width, height);
        BridgeContext ctx = new BridgeContext(userAgent, new DocumentLoader(userAgent));
        try {
            GraphicsNode root = new GVTBuilder().build(ctx, parseSvg(image, uri));
            Dimension2D documentSize = ctx.getDocumentSize();
            if (documentSize.getWidth() > 0 && documentSize.getHeight() > 0) {
                g.scale(width / documentSize.getWidth(), height / documentSize.getHeight());
            }
            root.paint(g);
        } finally {
            ctx.dispose();
        }
    }

    private static SVGDocument parseSvg(byte[] image, String uri) {
        SAXSVGDocumentFactory factory = new SAXSVGDocumentFactory(XMLResourceDescriptor.getXMLParserClassName());
        try (ByteArrayInputStream in = new ByteArrayInputStream(image)) {
            return factory.createSVGDocument(null, in);
        } catch (IOException e) {
            throw new XRRuntimeException("Failed to parse SVG (%s)".formatted(uri), e);
        }
    }

    /**
     * SVG without explicit {@code width}/{@code height} takes the viewport size,
     * so we make the viewport equal to the target box.
     */
    private final class SvgUserAgent extends UserAgentAdapter {
        private final Dimension2D viewportSize;

        private SvgUserAgent(float width, float height) {
            viewportSize = new Dimension2D() {
                @Override
                public double getWidth() {
                    return width;
                }

                @Override
                public double getHeight() {
                    return height;
                }

                @Override
                public void setSize(double width, double height) {
                    throw new UnsupportedOperationException();
                }
            };
        }

        @Override
        public Dimension2D getViewportSize() {
            return viewportSize;
        }

        @Override
        public void displayError(Exception e) {
            log.warn("Error while rendering SVG ({})", uri, e);
        }

        @Override
        public void displayMessage(String message) {
            log.debug("SVG ({}): {}", uri, message);
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
