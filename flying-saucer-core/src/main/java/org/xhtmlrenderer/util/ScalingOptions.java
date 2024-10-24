/*
 * {{{ header & license
 * Copyright (c) 2007 Patrick Wright
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
package org.xhtmlrenderer.util;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Map;

import static java.util.Collections.singletonMap;


/**
 * POJO used when calling
 * {@link ImageUtil#getScaledInstance(ScalingOptions, BufferedImage)}.
 * Encapsulates a set of parameters related to scaling quality and output. Values are final once constructed, except
 * for target width and height, which can be change and the options instance reused.
 * There is a default constructor for average quality and performance.
 */
public class ScalingOptions {
    private final DownscaleQuality downscalingHint;
    private final Object renderingHint;
    private int targetWidth;
    private int targetHeight;

    /**
     * Constructor with all options.
     *
     * @param downscalingHint   Directs downscaling quality. One of the enumerated types of
     *                          {@link org.xhtmlrenderer.util.DownscaleQuality} such as
     *                          {@link DownscaleQuality#FAST}.
     * @param interpolationHint Hint for interpolation to AWT image renderer, one of the Object constants from
     *                          {@link java.awt.RenderingHints} using {@link java.awt.RenderingHints#KEY_INTERPOLATION}
     */
    public ScalingOptions(DownscaleQuality downscalingHint, Object interpolationHint) {
        this.downscalingHint = downscalingHint;
        this.renderingHint = interpolationHint;
    }

    /**
     * Default scaling options, nearest neighbor interpolation, and fast downscaling. This is fast, but not great
     * quality.
     */
    public ScalingOptions() {
        this(DownscaleQuality.FAST, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
    }

    /**
     * Constructor with all options.
     *
     * @param targetWidth  Target width in pixels of image once scaled
     * @param targetHeight Target height in pixels of image once scaled
     * @param downscalingHint   Directs downscaling quality. One of the enumerated types of
     *                          {@link org.xhtmlrenderer.util.DownscaleQuality} such as
     *                          {@link DownscaleQuality#FAST}.
     * @param hint		 Hint for interpolation to AWT image renderer, one of the Object constants from
     *                     {@link java.awt.RenderingHints} using {@link java.awt.RenderingHints#KEY_INTERPOLATION}
     */
    public ScalingOptions(int targetWidth, int targetHeight, DownscaleQuality downscalingHint, Object hint) {
        this(downscalingHint, hint);
        this.setTargetHeight(Math.max(1, targetHeight));
        this.setTargetWidth(Math.max(1, targetWidth));
    }

    public DownscaleQuality getDownscalingHint() {
        return downscalingHint;
    }

    public Object getRenderingHint() {
        return renderingHint;
    }

    /**
     * Applies any rendering hints configured for these ScalingOptions to a Graphics2D instance before image
     * operations are called on it. These might be
     *
     * @param g2 A Graphics2D instance on which scaled images will be rendered.
     */
    public void applyRenderingHints(Graphics2D g2) {
        g2.setRenderingHints(getRenderingHints());
    }

    /**
     * Returns a Map of image rendering hints applicable to {@link Graphics2D#setRenderingHints(java.util.Map)}.
     * By default, this will only include the interpolation hint specified for this ScalingOptions. Other hints
     * could be added in an overridden version in a subclass.
     *
     * @return Map of rendering hints.
     */
    protected Map<RenderingHints.Key, Object> getRenderingHints() {
        return singletonMap(RenderingHints.KEY_INTERPOLATION, getRenderingHint());
    }

    /**
     * Returns true if the target size specified by these options matches the size provided (e.g. image is
     * already at target size).
     *
     * @param w an image width
     * @param h an image height
     * @return true if image dimensions already match target size
     */
    public boolean sizeMatches(int w, int h) {
        return (w == getTargetWidth() && h == getTargetHeight());
    }

    /**
     * Returns true if the target size specified by these options matches the size provided (e.g. image is
     * already at target size).
     *
     * @return true if image dimensions already match target size
     */
    public boolean sizeMatches(Image img) {
        return sizeMatches(img.getWidth(null), img.getHeight(null));
    }

    public int getTargetWidth() {
        return targetWidth;
    }

    public int getTargetHeight() {
        return targetHeight;
    }

    public void setTargetWidth(int targetWidth) {
        this.targetWidth = targetWidth;
    }

    public void setTargetHeight(int targetHeight) {
        this.targetHeight = targetHeight;
    }
}
