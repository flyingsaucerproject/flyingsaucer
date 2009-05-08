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

import java.util.*;
import java.util.logging.Level;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.*;
import org.xhtmlrenderer.css.constants.IdentValue;
import org.xhtmlrenderer.css.value.FontSpecification;
import org.xhtmlrenderer.extend.FontResolver;
import org.xhtmlrenderer.layout.SharedContext;
import org.xhtmlrenderer.render.FSFont;
import org.xhtmlrenderer.util.XRLog;

/**
 * Resolve font the SWT way.
 * 
 * @author Vianney le Clément
 * 
 */
public class SWTFontResolver implements FontResolver {
    private final Device _device;

    private final float _pointsPerPixel;

    private Map _instance_hash;

    private Map _default_fonts;

    private SWTFSFont _system_font;

    private static final String[] _defaults_serif = { "serif", "Serif", "times new roman", "times" };

    private static final String[] _defaults_monospace = { "monospace", "monospaced", "courier new",
            "courier" };

    public SWTFontResolver(Device device) {
        _device = device;
        _pointsPerPixel = 72f / device.getDPI().y;
        init();
    }

    private void init() {
        _instance_hash = new HashMap();

        // system fonts
        String system_font_family = _device.getSystemFont().getFontData()[0].getName();
        _default_fonts = new HashMap();

        // system font is likely to be a good default sans serif font
        _default_fonts.put("sans-serif", system_font_family);

        for (int i = 0; i < _defaults_serif.length; i++) {
            if (_device.getFontList(_defaults_serif[i], true).length > 0) {
                _default_fonts.put("serif", _defaults_serif[i]);
                break;
            }
        }
        if (_default_fonts.get("serif") == null) {
            _default_fonts.put("serif", system_font_family);
        }

        for (int i = 0; i < _defaults_monospace.length; i++) {
            if (_device.getFontList(_defaults_monospace[i], true).length > 0) {
                _default_fonts.put("monospace", _defaults_monospace[i]);
                break;
            }
        }
        if (_default_fonts.get("monospace") == null) {
            _default_fonts.put("monospace", system_font_family);
        }

        // last resort font
        Font systemFont = _device.getSystemFont();
        _system_font = new SWTFSFont(systemFont, systemFont.getFontData()[0].getHeight(), true);
    }

    public void flushCache() {
        for (Iterator iter = _instance_hash.values().iterator(); iter.hasNext();) {
            ((SWTFSFont) iter.next()).dispose();
        }
        _instance_hash.clear();
    }

    public FSFont resolveFont(SharedContext renderingContext, FontSpecification spec) {
        if (spec.families != null) {
            for (int i = 0; i < spec.families.length; i++) {
                FSFont font = resolveFont(renderingContext, spec.families[i], spec.size,
                        spec.fontWeight, spec.fontStyle, spec.variant);
                if (font != null) {
                    return font;
                }
            }
        }

        // no font found, fall back to standard sans
        FSFont font = resolveFont(renderingContext, "sans-serif", spec.size, spec.fontWeight,
                spec.fontStyle, spec.variant);
        if (font != null) {
            return font;
        }

        XRLog.cascade(Level.WARNING, "Falling back to default system font. " + spec.toString());

        // last resort: use system font
        return _system_font;
    }

    private SWTFSFont resolveFont(SharedContext ctx, String font, float size, IdentValue weight,
            IdentValue style, IdentValue variant) {
        // strip off the "s if they are there
        if (font.startsWith("\"")) {
            font = font.substring(1);
        }
        if (font.endsWith("\"")) {
            font = font.substring(0, font.length() - 1);
        }

        float size2D = size;

        // convert size from pixel to point and make some changes
        size *= _pointsPerPixel;
        if (variant != null && variant == IdentValue.SMALL_CAPS) {
            size *= 0.6;
        }
        size *= ctx.getTextRenderer().getFontScale();
        int nSize = Math.round(size);

        // normalize the font name
        if (_default_fonts.containsKey(font)) {
            font = (String) _default_fonts.get(font);
        }

        // assemble a font instance hash name
        String font_instance_name = getFontInstanceHashName(font, nSize, weight, style, variant);
        // check if the font instance exists in the hash table
        if (_instance_hash.containsKey(font_instance_name)) {
            // if so then return it
            return (SWTFSFont) _instance_hash.get(font_instance_name);
        }

        // if not then does the font exist
        FontData[] fd = _device.getFontList(font, true);
        if (fd.length > 0) {
            // create the font
            int style_bits = SWT.NORMAL;
            if (weight != null
                    && (weight == IdentValue.BOLD || weight == IdentValue.FONT_WEIGHT_700
                            || weight == IdentValue.FONT_WEIGHT_800 || weight == IdentValue.FONT_WEIGHT_900)) {
                style_bits |= SWT.BOLD;
            }
            if (style != null && (style == IdentValue.ITALIC || style == IdentValue.OBLIQUE)) {
                style_bits |= SWT.ITALIC;
            }

            SWTFSFont fnt = new SWTFSFont(new Font(_device, fd[0].getName(), nSize, style_bits),
                    size2D);

            // add the font to the hash so we don't have to do this again
            _instance_hash.put(font_instance_name, fnt);
            return fnt;
        }

        // we didn't find any possible matching font, so just return null
        return null;
    }

    private static String getFontInstanceHashName(String name, int size, IdentValue weight,
            IdentValue style, IdentValue variant) {
        return name + "-" + size + "-" + weight + "-" + style + "-" + variant;
    }

}
