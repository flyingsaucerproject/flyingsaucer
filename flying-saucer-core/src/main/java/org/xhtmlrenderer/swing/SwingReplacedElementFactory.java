/*
 * {{{ header & license
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
package org.xhtmlrenderer.swing;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xhtmlrenderer.extend.ReplacedElement;
import org.xhtmlrenderer.extend.ReplacedElementFactory;
import org.xhtmlrenderer.extend.UserAgentCallback;
import org.xhtmlrenderer.layout.LayoutContext;
import org.xhtmlrenderer.render.BlockBox;
import org.xhtmlrenderer.simple.extend.DefaultFormSubmissionListener;
import org.xhtmlrenderer.simple.extend.FormSubmissionListener;
import org.xhtmlrenderer.simple.extend.XhtmlForm;
import org.xhtmlrenderer.simple.extend.form.FormField;
import org.xhtmlrenderer.util.ImageUtil;
import org.xhtmlrenderer.util.XRLog;
import org.xhtmlrenderer.resource.ImageResource;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;

/**
 * A ReplacedElementFactory where Elements are replaced by Swing components.
 */
public class SwingReplacedElementFactory implements ReplacedElementFactory {
    /**
     * Cache of image components (ReplacedElements) for quick lookup, keyed by Element.
     */
    protected Map imageComponents;
    /**
     * Cache of XhtmlForms keyed by Element.
     */
    protected LinkedHashMap forms;

    private FormSubmissionListener formSubmissionListener;

    protected final RepaintListener repaintListener;

    private ImageResourceLoader imageResourceLoader;


    public SwingReplacedElementFactory() {
        this(ImageResourceLoader.NO_OP_REPAINT_LISTENER);
    }

    public SwingReplacedElementFactory(RepaintListener repaintListener) {
        this(repaintListener, new ImageResourceLoader());
    }

    public SwingReplacedElementFactory(final RepaintListener listener, final ImageResourceLoader irl) {
        this.repaintListener = listener;
        this.imageResourceLoader = irl;
        this.formSubmissionListener = new DefaultFormSubmissionListener();
    }

    /**
     * {@inheritDoc}
     */
    public ReplacedElement createReplacedElement(
            LayoutContext context,
            BlockBox box,
            UserAgentCallback uac,
            int cssWidth,
            int cssHeight
    ) {
        Element e = box.getElement();

        if (e == null) {
            return null;
        }

        if (context.getNamespaceHandler().isImageElement(e)) {
            return replaceImage(uac, context, e, cssWidth, cssHeight);
        } else {
            //form components
            Element parentForm = getParentForm(e, context);
            //parentForm may be null! No problem! Assume action is this document and method is get.
            XhtmlForm form = getForm(parentForm);
            if (form == null) {
                form = new XhtmlForm(uac, parentForm, formSubmissionListener);
                addForm(parentForm, form);
            }

            FormField formField = form.addComponent(e, context, box);
            if (formField == null) {
                return null;
            }

            JComponent cc = formField.getComponent();

            if (cc == null) {
                return new EmptyReplacedElement(0, 0);
            }

            SwingReplacedElement result = new SwingReplacedElement(cc);
            result.setIntrinsicSize(formField.getIntrinsicSize());

            if (context.isInteractive()) {
                ((Container) context.getCanvas()).add(cc);
            }
            return result;
        }
    }

    /**
     * Handles replacement of image elements in the document. May return the same ReplacedElement for a given image
     * on multiple calls. Image will be automatically scaled to cssWidth and cssHeight assuming these are non-zero
     * positive values. The element is assume to have a src attribute (e.g. it's an <img> element)
     *
     * @param uac       Used to retrieve images on demand from some source.
     * @param context
     * @param elem      The element with the image reference
     * @param cssWidth  Target width of the image
     * @param cssHeight Target height of the image @return A ReplacedElement for the image; will not be null.
     * @return
     */
    protected ReplacedElement replaceImage(UserAgentCallback uac, LayoutContext context, Element elem, int cssWidth, int cssHeight) {
        ReplacedElement re = null;
        String imageSrc = context.getNamespaceHandler().getImageSourceURI(elem);
        
        if (imageSrc == null || imageSrc.length() == 0) {
            XRLog.layout(Level.WARNING, "No source provided for img element.");
            re = newIrreplaceableImageElement(cssWidth, cssHeight);
        } else if (ImageUtil.isEmbeddedBase64Image(imageSrc)) {
            BufferedImage image = ImageUtil.loadEmbeddedBase64Image(imageSrc);
            if (image != null) {
                re = new ImageReplacedElement(image, cssWidth, cssHeight);
            }
        } else {
            // lookup in cache, or instantiate
            String ruri = uac.resolveURI(imageSrc);
            re = lookupImageReplacedElement(elem, ruri, cssWidth, cssHeight);
            if (re == null) {
                XRLog.load(Level.FINE, "Swing: Image " + ruri + " requested at "+ " to " + cssWidth + ", " + cssHeight);
                ImageResource imageResource = imageResourceLoader.get(ruri, cssWidth, cssHeight);
                if (imageResource.isLoaded()) {
                    re = new ImageReplacedElement(((AWTFSImage) imageResource.getImage()).getImage(), cssWidth, cssHeight);
                } else {
                    re = new DeferredImageReplacedElement(imageResource, repaintListener, cssWidth, cssHeight);
                }
                storeImageReplacedElement(elem, re, ruri, cssWidth, cssHeight);
            }
        }
        return re;
    }

    private ReplacedElement lookupImageReplacedElement(final Element elem, final String ruri, final int cssWidth, final int cssHeight) {
        if (imageComponents == null) {
            return null;
        }
        CacheKey key = new CacheKey(elem, ruri, cssWidth, cssHeight);
        return (ReplacedElement) imageComponents.get(key);
    }



    /**
     * Returns a ReplacedElement for some element in the stream which should be replaceable, but is not. This might
     * be the case for an element like img, where the source isn't provided.
     *
     * @param cssWidth  Target width for the element.
     * @param cssHeight Target height for the element
     * @return A ReplacedElement to substitute for one that can't be generated.
     */
    protected ReplacedElement newIrreplaceableImageElement(int cssWidth, int cssHeight) {
        BufferedImage missingImage;
        ReplacedElement mre;
        try {
            // TODO: we can come up with something better; not sure if we should use Alt text, how text should size, etc.
            missingImage = ImageUtil.createCompatibleBufferedImage(cssWidth, cssHeight, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = missingImage.createGraphics();
            g.setColor(Color.BLACK);
            g.setBackground(Color.WHITE);
            g.setFont(new Font("Serif", Font.PLAIN, 12));
            g.drawString("Missing", 0, 12);
            g.dispose();
            mre = new ImageReplacedElement(missingImage, cssWidth, cssHeight);
        } catch (Exception e) {
            mre = new EmptyReplacedElement(
                    cssWidth < 0 ? 0 : cssWidth,
                    cssHeight < 0 ? 0 : cssHeight);
        }
        return mre;
    }

    /**
     * Adds a ReplacedElement containing an image to a cache of images for quick lookup.
     *
     * @param e   The element under which the image is keyed.
     * @param cc  The replaced element containing the image, or another ReplacedElement to be used in its place
     * @param uri
     * @param cssWidth
     * @param cssHeight
     */
    protected void storeImageReplacedElement(Element e, ReplacedElement cc, String uri, final int cssWidth, final int cssHeight) {
        if (imageComponents == null) {
            imageComponents = new HashMap();
        }
        CacheKey key = new CacheKey(e, uri, cssWidth, cssHeight);
        imageComponents.put(key, cc);
    }

    /**
     * Retrieves a ReplacedElement for an image from cache, or null if not found.
     *
     * @param e   The element by which the image is keyed
     * @param uri
     * @return The ReplacedElement for the image, or null if there is none.
     */
    protected ReplacedElement lookupImageReplacedElement(Element e, String uri) {
        return lookupImageReplacedElement(e, uri, -1, -1);
    }

    /**
     * Adds a form to a local cache for quick lookup.
     *
     * @param e The element under which the form is keyed (e.g. "<form>" in HTML)
     * @param f The form element being stored.
     */
    protected void addForm(Element e, XhtmlForm f) {
        if (forms == null) {
            forms = new LinkedHashMap();
        }
        forms.put(e, f);
    }

    /**
     * Returns the XhtmlForm associated with an Element in cache, or null if not found.
     *
     * @param e The Element to which the form is keyed
     * @return The form, or null if not found.
     */
    protected XhtmlForm getForm(Element e) {
        if (forms == null) {
            return null;
        }
        return (XhtmlForm) forms.get(e);
    }

    /**
     * @param e
     */
    protected Element getParentForm(Element e, LayoutContext context) {
        Node node = e;

        do {
            node = node.getParentNode();
        } while (node.getNodeType() == Node.ELEMENT_NODE &&
                !context.getNamespaceHandler().isFormElement((Element) node));

        if (node.getNodeType() != Node.ELEMENT_NODE) {
            return null;
        }

        return (Element) node;
    }

    /**
     * Clears out any references to elements or items created by this factory so far.
     */
    public void reset() {
        forms = null;
        //imageComponents = null;
    }

    public void remove(Element e) {
        if (forms != null) {
            forms.remove(e);
        }

        if (imageComponents != null) {
            imageComponents.remove(e);
        }
    }

    public void setFormSubmissionListener(FormSubmissionListener fsl) {
        this.formSubmissionListener = fsl;
    }

    private static class CacheKey {
        final Element elem;
        final String uri;
        final int width;
        final int height;

        public CacheKey(final Element elem, final String uri, final int width, final int height) {
            this.uri = uri;
            this.width = width;
            this.height = height;
            this.elem = elem;
        }

        public boolean equals(final Object o) {
            if (this == o) return true;
            if (!(o instanceof CacheKey)) return false;

            final CacheKey cacheKey = (CacheKey) o;

            if (height != cacheKey.height) return false;
            if (width != cacheKey.width) return false;
            if (!elem.equals(cacheKey.elem)) return false;
            if (!uri.equals(cacheKey.uri)) return false;

            return true;
        }

        public int hashCode() {
            int result = elem.hashCode();
            result = 31 * result + uri.hashCode();
            result = 31 * result + width;
            result = 31 * result + height;
            return result;
        }
    }

}
