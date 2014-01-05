/*
 * {{{ header & license
 * Copyright (c) 2007 Vianney le Clément
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 * }}}
 */
package org.xhtmlrenderer.swt;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import org.w3c.dom.Element;
import org.xhtmlrenderer.extend.FSImage;
import org.xhtmlrenderer.extend.ReplacedElement;
import org.xhtmlrenderer.extend.ReplacedElementFactory;
import org.xhtmlrenderer.extend.UserAgentCallback;
import org.xhtmlrenderer.layout.LayoutContext;
import org.xhtmlrenderer.render.BlockBox;
import org.xhtmlrenderer.util.XRLog;
import org.xhtmlrenderer.simple.extend.FormSubmissionListener;
import org.xhtmlrenderer.simple.extend.DefaultFormSubmissionListener;
import org.xhtmlrenderer.util.ImageUtil;

/**
 * 
 * @author Vianney le Clément
 * 
 */
public class SWTReplacedElementFactory implements ReplacedElementFactory {
    /**
     * Cache of image components (ReplacedElements) for quick lookup, keyed by
     * Element.
     */
    private Map _imageComponents;
    private FormSubmissionListener _formSubmissionListener;

    public SWTReplacedElementFactory() {
        _formSubmissionListener = new DefaultFormSubmissionListener();
    }

    /**
     * Dispose missing image if created.
     */
    public void clean() {
        reset();
    }

    public ReplacedElement createReplacedElement(LayoutContext c, BlockBox box,
            UserAgentCallback uac, int cssWidth, int cssHeight) {
        Element e = box.getElement();
        if (e == null) {
            return null;
        }

        // images
        if (c.getNamespaceHandler().isImageElement(e)) {
            return replaceImage(uac, c, e, cssWidth, cssHeight);
        }
        
        return null;
    }

    /**
     * Handles replacement of image elements in the document. May return the
     * same ReplacedElement for a given image on multiple calls. Image will be
     * automatically scaled to cssWidth and cssHeight assuming these are
     * non-zero positive values. The element is assume to have a src attribute
     * (e.g. it's an &lt;img&gt; element)
     * 
     * @param uac Used to retrieve images on demand from some source.
     * @param context
     * @param elem The element with the image reference
     * @param cssWidth Target width of the image
     * @param cssHeight Target height of the image
     * @return A ReplacedElement for the image; will not be null.
     */
    protected ReplacedElement replaceImage(UserAgentCallback uac,
            LayoutContext context, Element elem, int cssWidth, int cssHeight) {
        ReplacedElement re = null;
        String imageSrc = context.getNamespaceHandler().getImageSourceURI(elem);
        
        if (imageSrc == null || imageSrc.length() == 0) {
            XRLog.layout(Level.WARNING, "No source provided for img element.");
            re = new ImageReplacedElement(new SWTFSImage(), cssWidth, cssHeight);
        } else if (ImageUtil.isEmbeddedBase64Image(imageSrc)) {
            SWTFSImage fsImage = (SWTFSImage) uac.getImageResource(imageSrc).getImage();
            if (fsImage != null) {
                re = new ImageReplacedElement(fsImage, cssWidth, cssHeight);
            }
        } else {
            // lookup in cache, or instantiate
            re = lookupImageReplacedElement(elem);
            if (re == null) {
                FSImage fsImage = uac.getImageResource(imageSrc).getImage();
                if (fsImage != null) {
                    re = new ImageReplacedElement(new SWTFSImage(
                        (SWTFSImage) fsImage), cssWidth, cssHeight);
                } else {
                    // TODO: Should return "broken" image icon, e.g. "not found"
                    re = new ImageReplacedElement(new SWTFSImage(), cssWidth,
                        cssHeight);
                }
                storeImageReplacedElement(elem, re);
            }
        }
        return re;
    }

    /**
     * Adds a ReplacedElement containing an image to a cache of images for quick
     * lookup.
     * 
     * @param e The element under which the image is keyed.
     * @param cc The replaced element containing the image, or another
     *            ReplacedElement to be used in its place (like a placeholder if
     *            the image can't be loaded).
     */
    protected void storeImageReplacedElement(Element e, ReplacedElement cc) {
        if (_imageComponents == null) {
            _imageComponents = new HashMap();
        }
        _imageComponents.put(e, cc);
    }

    /**
     * Retrieves a ReplacedElement for an image from cache, or null if not
     * found.
     * 
     * @param e The element by which the image is keyed
     * @return The ReplacedElement for the image, or null if there is none.
     */
    protected ReplacedElement lookupImageReplacedElement(Element e) {
        if (_imageComponents == null) {
            return null;
        }
        return (ReplacedElement) _imageComponents.get(e);
    }

    public void remove(Element e) {
        if (_imageComponents != null) {
            _imageComponents.remove(e);
        }
    }

    public void setFormSubmissionListener(FormSubmissionListener listener) {
        _formSubmissionListener = listener;
    }

    public void reset() {
        _imageComponents = null;
    }

}
