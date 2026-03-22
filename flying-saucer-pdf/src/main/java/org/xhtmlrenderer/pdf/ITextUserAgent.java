/*
 * {{{ header & license
 * Copyright (c) 2004, 2005 Torbjoern Gannholm
 * Copyright (c) 2006 Wisconsin Court System
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 * }}}
 */
package org.xhtmlrenderer.pdf;

import org.apache.batik.anim.dom.SAXSVGDocumentFactory;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.util.XMLResourceDescriptor;
import org.jspecify.annotations.Nullable;
import org.openpdf.text.BadElementException;
import org.openpdf.text.Image;
import org.openpdf.text.Rectangle;
import org.openpdf.text.pdf.PdfReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xhtmlrenderer.extend.FSImage;
import org.xhtmlrenderer.extend.Size;
import org.xhtmlrenderer.resource.ImageResource;
import org.xhtmlrenderer.swing.NaiveUserAgent;
import org.xhtmlrenderer.util.Configuration;
import org.xhtmlrenderer.util.ContentTypeDetectingInputStreamWrapper;
import org.xhtmlrenderer.util.ImageUtil;
import org.xhtmlrenderer.util.XRLog;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.Float.parseFloat;
import static java.lang.Math.round;
import static java.lang.Math.toIntExact;
import static java.util.Objects.requireNonNull;
import static org.xhtmlrenderer.util.ContentTypeDetectingInputStreamWrapper.detectContentType;
import static org.xhtmlrenderer.util.IOUtil.readBytes;
import static org.xhtmlrenderer.util.ImageUtil.isEmbeddedBase64Image;

public class ITextUserAgent extends NaiveUserAgent {
    private static final Logger log = LoggerFactory.getLogger(ITextUserAgent.class);
    private static final int IMAGE_CACHE_CAPACITY = 32;
    private static final Pattern RE_SIZE_WITH_UNITS = Pattern.compile("(.+?)(\\D{1,2})");

    private final ITextOutputDevice _outputDevice;
    private final int dotsPerPixel;

    public ITextUserAgent(ITextOutputDevice outputDevice, int dotsPerPixel) {
        super(Configuration.valueAsInt("xr.image.cache-capacity", IMAGE_CACHE_CAPACITY));
        _outputDevice = outputDevice;
        this.dotsPerPixel = dotsPerPixel;
    }

    int getDotsPerPixel() {
        return dotsPerPixel;
    }

    @Override
    public ImageResource getImageResource(String uriStr) {
        String unresolvedUri = uriStr;
        if (!isEmbeddedBase64Image(uriStr)) {
            uriStr = resolveURI(uriStr);
        }
        ImageResource resource = _imageCache.get(unresolvedUri);

        if (resource == null) {
            resource = loadImageResource(uriStr);
            _imageCache.put(unresolvedUri, resource);
        }
        if (resource != null) {
            FSImage image = makeSafeCopy(resource.getImage());
            return new ImageResource(resource.getImageUri(), image);
        } else {
            return new ImageResource(uriStr, null);
        }
    }

    @Nullable
    private FSImage makeSafeCopy(@Nullable FSImage image) {
        return image instanceof ITextFSImage mutable ? (FSImage) mutable.clone() : image;
    }

    @Nullable
    private ImageResource loadImageResource(String uriStr) {
        if (isEmbeddedBase64Image(uriStr)) {
            return loadEmbeddedBase64ImageResource(uriStr);
        }
        try (ContentTypeDetectingInputStreamWrapper cis = detectContentType(resolveAndOpenStream(uriStr))) {
            if (cis != null) {
                if (cis.isPdf()) {
                    URI uri = new URI(uriStr);
                    PdfReader reader = _outputDevice.getReader(uri);
                    Rectangle rect = reader.getPageSizeWithRotation(1);
                    float initialWidth = rect.getWidth() * _outputDevice.getDotsPerPoint();
                    float initialHeight = rect.getHeight() * _outputDevice.getDotsPerPoint();
                    PDFAsImage image = new PDFAsImage(uri, initialWidth, initialHeight);
                    return new ImageResource(uriStr, image);
                } else if (cis.isSvg()) {
                    SvgImage image = readCsv(uriStr, cis);
                    return new ImageResource(uriStr, image);
                } else {
                    Image image = Image.getInstance(readBytes(cis));
                    scaleToOutputResolution(image);
                    return new ImageResource(uriStr, new ITextFSImage(image));
                }
            }
        } catch (TranscoderException e) {
            log.error("Could not load SVG image from '{}'", uriStr, e);
            XRLog.exception("Could not load image from '%s'".formatted(uriStr), e);
        } catch (BadElementException | IOException | URISyntaxException e) {
            log.warn("Could not load image from '{}'", uriStr, e);
            XRLog.exception("Could not load image from '%s'".formatted(uriStr), e);
        }
        return null;
    }

    private ImageResource loadEmbeddedBase64ImageResource(final String uri) {
        try {
            byte[] bytes = requireNonNull(ImageUtil.getEmbeddedBase64Image(uri));
            Image image = Image.getInstance(bytes);
            scaleToOutputResolution(image);
            return new ImageResource(uri, new ITextFSImage(image));
        } catch (BadElementException | IOException e) {
            XRLog.exception("Can't read embedded base64 image from " + uri, e);
        }
        return new ImageResource(null, null);
    }

    private SvgImage readCsv(String uri, InputStream in) throws IOException, TranscoderException {
        byte[] svgBytes = readBytes(in);
        return new SvgImage(svgBytes, getOriginalSvgSize(uri, svgBytes), uri);
    }

    Size getOriginalSvgSize(String uri, byte[] svgImage) throws IOException {
        SAXSVGDocumentFactory factory = new SAXSVGDocumentFactory(XMLResourceDescriptor.getXMLParserClassName());
        try (ByteArrayInputStream in = new ByteArrayInputStream(svgImage)) {
            Document document = factory.createDocument(uri, in);
            String width = document.getDocumentElement().getAttribute("width");
            String height = document.getDocumentElement().getAttribute("height");
            if (!width.isEmpty() && !height.isEmpty()) {
                return new Size(parseSize(width), parseSize(height));
            }
            String[] viewBox = document.getDocumentElement().getAttribute("viewBox").split(" ", 4);
            if (viewBox.length >= 4) {
                return new Size(parseSize(viewBox[2]), parseSize(viewBox[3]));
            }

            return new Size(300, 150); // default size in most browsers
        }
    }

    private void scaleToOutputResolution(Image image) {
        float factor = dotsPerPixel;
        if (factor != 1.0f) {
            image.scaleAbsolute(image.getPlainWidth() * factor, image.getPlainHeight() * factor);
        }
    }

    /**
     * Taken from <a href="https://developer.mozilla.org/en-US/docs/Learn_web_development/Core/Styling_basics/Values_and_units#absolute_length_units">spec</a>
     *
     * @param cssValue e.g. "100px", "200pt", "300mm", "400cm", "500Q", "600pc", "700in"
     * @return the size in pixels
     */
    static int parseSize(String cssValue) {
        CssSize value = parseCssValue(cssValue);

        return toIntExact(round(switch (value.units()) {
            case "cm" -> 37.8 * value.value();
            case "mm" -> 96.0 / 25.4 * value.value();
            case "Q" -> 96.0 / 25.4 / 4 * value.value();
            case "in" -> 96.0 * value.value();
            case "pt" -> 96.0 / 72 * value.value();
            case "pc" -> 96.0 / 72 * 12 * value.value();
            default -> value.value();
        }));
    }

    private static CssSize parseCssValue(String cssValue) {
        Matcher matcher = RE_SIZE_WITH_UNITS.matcher(cssValue);
        String value = matcher.matches() ? matcher.group(1) : cssValue;
        String units = matcher.matches() ? matcher.group(2) : "px";
        return new CssSize(parseFloat(value), units);
    }

    private record CssSize(float value, String units) {}
}
