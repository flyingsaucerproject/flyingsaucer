/*
 * {{{ header & license
 * Copyright (c) 2009 Patrick Wright
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

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.logging.Level;

import org.xhtmlrenderer.extend.ReplacedElement;
import org.xhtmlrenderer.layout.LayoutContext;
import org.xhtmlrenderer.util.ImageUtil;
import org.xhtmlrenderer.util.Configuration;
import org.xhtmlrenderer.util.XRLog;
import org.xhtmlrenderer.resource.ImageResource;

import javax.swing.*;


/**
 * An DeferredImageReplacedElement is a {@link org.xhtmlrenderer.extend.ReplacedElement} that contains a {@link java.awt.Image}
 * which by default is simply a transparent image scaled to the size provided to the constructor. The DeferredImageReplacedElement
 * also has a reference to an ImageResource which points to the image which will be returned for this replaced element.
 * That Image may be loaded some time after this DeferredImageReplacedElement is created. Calling getImage() on
 * instances of DeferredImageReplacedElement will return either the original dummy image, or the actual image
 * loaded into the ImageResource.
 */
public class DeferredImageReplacedElement extends ImageReplacedElement {
    private Point _location = new Point(0, 0);

    private final RepaintListener repaintListener;
    private final int _targetHeight;
    private final int _targetWidth;

    private boolean _doScaleImage;
    private boolean _loaded;
    private final ImageResource _imageResource;


    /**
     * Creates a new ImageReplacedElement and scales it to the size specified if either width or height has a valid
     * value (values are > -1), otherwise original size is preserved. The idea is that the image was loaded at
     * a certain size (that's the Image instance here) and that at the time we create the ImageReplacedElement
     * we have a target W/H we want to use.
     *
     * @param imageResource
     * @param repaintListener
     */
    public DeferredImageReplacedElement(ImageResource imageResource, RepaintListener repaintListener, int w, int h) {
        this._imageResource = imageResource;
        _loaded = false;
        this.repaintListener = repaintListener;
        if (w == -1 && h == -1) {
            _doScaleImage = false;
            _targetHeight = 1;
            _targetWidth = 1;
        } else {
            _doScaleImage = true;
            _targetHeight = Math.max(1, h);
            _targetWidth = Math.max(1, w);
        }
        _image = ImageUtil.createCompatibleBufferedImage(_targetWidth, _targetHeight);
    }

    /** {@inheritDoc} */
    public void detach(LayoutContext c) {
        // nothing to do in this case
    }

    /** {@inheritDoc} */
    public int getIntrinsicHeight() {
        return  _loaded ? _image.getHeight(null) : _targetHeight;
    }

    /** {@inheritDoc} */
    public int getIntrinsicWidth() {
        return _loaded ? _image.getWidth(null) : _targetWidth;
    }

    /** {@inheritDoc} */
    public Point getLocation() {
        return _location;
    }

    /** {@inheritDoc} */
    public boolean isRequiresInteractivePaint() {
        return true;
    }

    /** {@inheritDoc} */
    public void setLocation(int x, int y) {
        _location = new Point(x, y);
    }

    /**
     * The image we're replacing.
     * @return see desc
     */
    public Image getImage() {
        if (!_loaded && _imageResource.isLoaded()) {
            Image image = ((AWTFSImage) _imageResource.getImage()).getImage();
            if (_doScaleImage && (_targetWidth > 0 || _targetHeight > 0)) {
                int w = image.getWidth(null);
                int h = image.getHeight(null);
                int newW = _targetWidth;
                int newH = _targetHeight;

                if (newW == -1) {
                    newW = (int) (w * ((double) newH / h));
                }

                if (newH == -1) {
                    newH = (int) (h * ((double) newW / w));
                }

                if (w != newW || h != newH) {
                    if (image instanceof BufferedImage) {
                        image = ImageUtil.getScaledInstance((BufferedImage) image, newW, newH);
                    } else {
                        if (true) {
                            throw new RuntimeException("image is not a buffered image! " + _imageResource.getImageUri());
                        }
                        String scalingType = Configuration.valueFor("xr.image.scale", "HIGH").trim();

                        if (scalingType.equalsIgnoreCase("HIGH") || scalingType.equalsIgnoreCase("MID")) {
                            image = image.getScaledInstance(newW, newH, Image.SCALE_SMOOTH);
                        } else {
                            image = image.getScaledInstance(newW, newH, Image.SCALE_FAST);
                        }
                    }
                }
                _image = image;
            } else {
                _image = image;
            }
            _loaded = true;
            XRLog.load(Level.FINE, "Icon: replaced image " + _imageResource.getImageUri() + ", repaint requested");
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    repaintListener.repaintRequested(_doScaleImage);
                }
            });

        }

        return _image;
    }

	public int getBaseline() {
		return 0;
	}

    public boolean hasBaseline() {
		return false;
	}
}