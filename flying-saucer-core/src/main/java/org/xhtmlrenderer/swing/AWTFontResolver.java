/*
 * {{{ header & license
 * Copyright (c) 2004, 2005 Joshua Marinacci
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.	See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 * }}}
 */
package org.xhtmlrenderer.swing;

import org.xhtmlrenderer.css.constants.IdentValue;
import org.xhtmlrenderer.css.value.FontSpecification;
import org.xhtmlrenderer.extend.FontResolver;
import org.xhtmlrenderer.layout.SharedContext;
import org.xhtmlrenderer.render.FSFont;

import java.awt.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static java.util.Arrays.asList;


/**
 * @author Joshua Marinacci
 */
public class AWTFontResolver implements FontResolver {
    private final Set<String> availableFontNames = new HashSet<>();
    private final Map<String, Font> instance_hash = new HashMap<>();
    private final Map<String, Font> available_fonts_hash = new HashMap<>();

    public AWTFontResolver() {
        init();
    }

    private void init() {
        instance_hash.clear();
        availableFontNames.clear();

        // preload the font map with the font names as keys
        // don't add the actual font objects because that would be a waste of memory
        // we will only add them once we need to use them
        GraphicsEnvironment gfx = GraphicsEnvironment.getLocalGraphicsEnvironment();
        availableFontNames.addAll(asList(gfx.getAvailableFontFamilyNames()));

        available_fonts_hash.clear();
        
        // preload sans, serif, and monospace into the available font hash
        available_fonts_hash.put("Serif", new Font("Serif", Font.PLAIN, 1));
        available_fonts_hash.put("SansSerif", new Font("SansSerif", Font.PLAIN, 1));
        available_fonts_hash.put("Monospaced", new Font("Monospaced", Font.PLAIN, 1));
    }

    @Override
    public void flushCache() {
        init();
    }

    public FSFont resolveFont(SharedContext ctx, String[] families, float size, IdentValue weight, IdentValue style, IdentValue variant) {
        if (families != null) {
            for (String family : families) {
                Font font = resolveFont(ctx, family, size, weight, style, variant);
                if (font != null) {
                    return new AWTFSFont(font);
                }
            }
        }

        // if we get here then no font worked, so just return default sans
        //Uu.p("pulling out: -" + available_fonts_hash.get("SansSerif") + "-");
        String family = "SansSerif";
        if (style == IdentValue.ITALIC) {
            family = "Serif";
        }

        Font fnt = createFont(ctx, available_fonts_hash.get(family), size, weight, style, variant);
        instance_hash.put(getFontInstanceHashName(ctx, family, size, weight, style, variant), fnt);
        //Uu.p("subbing in base sans : " + fnt);
        return new AWTFSFont(fnt);
    }

    /**
     * Sets the fontMapping attribute of the FontResolver object
     *
     * @param name The new fontMapping value
     * @param font The new fontMapping value
     */
    public void setFontMapping(String name, Font font) {
        available_fonts_hash.put(name, font.deriveFont(1.0f));
    }

    protected static Font createFont(SharedContext ctx, Font root_font, float size, IdentValue weight, IdentValue style, IdentValue variant) {
        int font_const = Font.PLAIN;
        if (weight != null &&
                (weight == IdentValue.BOLD ||
                weight == IdentValue.FONT_WEIGHT_700 ||
                weight == IdentValue.FONT_WEIGHT_800 ||
                weight == IdentValue.FONT_WEIGHT_900)) {

            font_const = font_const | Font.BOLD;
        }
        if (style != null && (style == IdentValue.ITALIC || style == IdentValue.OBLIQUE)) {
            font_const = font_const | Font.ITALIC;
        }

        // scale vs font scale value too
        size *= ctx.getTextRenderer().getFontScale();

        Font fnt = root_font.deriveFont(font_const, size);
        if (variant != null) {
            if (variant == IdentValue.SMALL_CAPS) {
                fnt = fnt.deriveFont((float) (((float) fnt.getSize()) * 0.6));
            }
        }

        return fnt;
    }

    protected Font resolveFont(SharedContext ctx, String font, float size, IdentValue weight, IdentValue style, IdentValue variant) {
        //Uu.p("here");
        // strip off the "s if they are there
        if (font.startsWith("\"")) {
            font = font.substring(1);
        }
        if (font.endsWith("\"")) {
            font = font.substring(0, font.length() - 1);
        }

        //Uu.p("final font = " + font);
        // normalize the font name
        if (font.equals("serif")) {
            font = "Serif";
        }
        if (font.equals("sans-serif")) {
            font = "SansSerif";
        }
        if (font.equals("monospace")) {
            font = "Monospaced";
        }

        if (font.equals("Serif") && style == IdentValue.OBLIQUE) font = "SansSerif";
        if (font.equals("SansSerif") && style == IdentValue.ITALIC) font = "Serif";

        // assemble a font instance hash name
        String font_instance_name = getFontInstanceHashName(ctx, font, size, weight, style, variant);
        //Uu.p("looking for font: " + font_instance_name);
        // check if the font instance exists in the hash table
        if (instance_hash.containsKey(font_instance_name)) {
            // if so then return it
            return instance_hash.get(font_instance_name);
        }

        //Uu.p("font lookup failed for: " + font_instance_name);
        //Uu.p("searching for : " + font + " " + size + " " + weight + " " + style + " " + variant);


        // if not then
        //  does the font exist
        if (available_fonts_hash.containsKey(font)) {
            Font root_font = available_fonts_hash.get(font);

            if (root_font == null && availableFontNames.contains(font)) {
                root_font = new Font(font, Font.PLAIN, 1);
                available_fonts_hash.put(font, root_font);
            }

            // now that we have a root font, we need to create the correct version of it
            Font fnt = createFont(ctx, root_font, size, weight, style, variant);

            // add the font to the hash, so we don't have to do this again
            instance_hash.put(font_instance_name, fnt);
            return fnt;
        }

        // we didn't find any possible matching font, so just return null
        return null;
    }

    /**
     * Gets the fontInstanceHashName attribute of the FontResolverTest object
     */
    protected static String getFontInstanceHashName(SharedContext ctx, String name, float size, IdentValue weight, IdentValue style, IdentValue variant) {
        return name + "-" + (size * ctx.getTextRenderer().getFontScale()) + "-" + weight + "-" + style + "-" + variant;
    }

    @Override
    public FSFont resolveFont(SharedContext renderingContext, FontSpecification spec) {
        return resolveFont(renderingContext, spec.families, spec.size, spec.fontWeight, spec.fontStyle, spec.variant);
    }
}
