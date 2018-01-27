/*
 * {{{ header & license
 * Copyright (c) 2007 Wisconsin Court System
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
package org.xhtmlrenderer.docx;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.xhtmlrenderer.css.constants.IdentValue;
import org.xhtmlrenderer.css.value.FontSpecification;
import org.xhtmlrenderer.extend.FontResolver;
import org.xhtmlrenderer.layout.SharedContext;
import org.xhtmlrenderer.render.FSFont;

import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.BaseFont;

public class Docx4jFontResolver implements FontResolver {
    
    private Map _fontFamilies = createInitialFontMap();
    
    private Map _fontCache = new HashMap();
    
    private final SharedContext _sharedContext;

    public Docx4jFontResolver(SharedContext sharedContext) {
        _sharedContext = sharedContext;
    }
    
    public void flushCache() {
        // TODO Auto-generated method stub

    }    
    
    public FSFont resolveFont(SharedContext renderingContext, FontSpecification spec) {
        // TODO Auto-generated method stub
        return null;
    }
    
    private FSFont resolveFont(SharedContext ctx, String[] families, float size, 
            IdentValue weight, IdentValue style, IdentValue variant) {
        
        if (! (style == IdentValue.NORMAL || style == IdentValue.OBLIQUE
                || style == IdentValue.ITALIC)) {
            style = IdentValue.NORMAL;
        }
        if (families != null) {
            for (int i = 0; i < families.length; i++) {
                FSFont font = resolveFont(ctx, families[i], size, weight, style, variant);
                if (font != null) {
                    return font;
                }
            }
        }

        return resolveFont(ctx, "Serif", size, weight, style, variant);
    }
    
    private FSFont resolveFont(SharedContext ctx, String fontFamily, float size, IdentValue weight, IdentValue style, IdentValue variant) {
        
        String normalizedFontFamily = normalizeFontFamily(fontFamily);

        String cacheKey = getHashName(normalizedFontFamily, weight, style);
        Docx4jFontDescription result = (Docx4jFontDescription)_fontCache.get(cacheKey);
        if (result != null) {
            return new Docx4jFSFont(result, size);
        }

        Docx4jFontFamily family = (Docx4jFontFamily)_fontFamilies.get(normalizedFontFamily);
        if (family != null) {
            result = family.match(convertWeightToInt(weight), style);
            if (result != null) {
                _fontCache.put(cacheKey, result);
                return new Docx4jFSFont(result, size);
            }
        }

        return null;
    }
    
    protected static String getHashName(
            String name, IdentValue weight, IdentValue style) {
        return name + "-" + weight + "-" + style;
    }

    private String normalizeFontFamily(String fontFamily) {
        String result = fontFamily;
        // strip off the "s if they are there
        if (result.startsWith("\"")) {
            result = result.substring(1);
        }
        if (result.endsWith("\"")) {
            result = result.substring(0, result.length() - 1);
        }

        // normalize the font name
        if (result.equalsIgnoreCase("serif")) {
            result = "Serif";
        }
        else if (result.equalsIgnoreCase("sans-serif")) {
            result = "SansSerif";
        }
        else if (result.equalsIgnoreCase("monospace")) {
            result = "Monospaced";
        }

        return result;
    }

    private int convertWeightToInt(IdentValue weight) {
        if (weight == IdentValue.NORMAL) {
            return 400;
        } else if (weight == IdentValue.BOLD) {
            return 700;
        } else if (weight == IdentValue.FONT_WEIGHT_100) {
            return 100;
        } else if (weight == IdentValue.FONT_WEIGHT_200) {
            return 200;
        } else if (weight == IdentValue.FONT_WEIGHT_300) {
            return 300;
        } else if (weight == IdentValue.FONT_WEIGHT_400) {
            return 400;
        } else if (weight == IdentValue.FONT_WEIGHT_500) {
            return 500;
        } else if (weight == IdentValue.FONT_WEIGHT_600) {
            return 600;
        } else if (weight == IdentValue.FONT_WEIGHT_700) {
            return 700;
        } else if (weight == IdentValue.FONT_WEIGHT_800) {
            return 800;
        } else if (weight == IdentValue.FONT_WEIGHT_900) {
            return 900;
        } else if (weight == IdentValue.LIGHTER) {
            // FIXME
            return 400;
        } else if (weight == IdentValue.BOLDER) {
            // FIXME
            return 700;
        }
        throw new IllegalArgumentException();
    }
    

    private static Map createInitialFontMap() {
        HashMap result = new HashMap();

        try {
            addCourier(result);
            addTimes(result);
            addHelvetica(result);
        } catch (DocumentException e) {
            throw new RuntimeException(e.getMessage(), e);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }

        return result;
    }

    private static BaseFont createFont(String name) throws DocumentException, IOException {
        return BaseFont.createFont(name, "winansi", true);
    }

    private static void addCourier(HashMap result) throws DocumentException, IOException {
        Docx4jFontFamily courier = new Docx4jFontFamily();
        courier.setName("Courier");

        courier.addFontDescription(new Docx4jFontDescription(
                createFont(BaseFont.COURIER_BOLDOBLIQUE), IdentValue.OBLIQUE, 700));
        courier.addFontDescription(new Docx4jFontDescription(
                createFont(BaseFont.COURIER_OBLIQUE), IdentValue.OBLIQUE, 400));
        courier.addFontDescription(new Docx4jFontDescription(
                createFont(BaseFont.COURIER_BOLD), IdentValue.NORMAL, 700));
        courier.addFontDescription(new Docx4jFontDescription(
                createFont(BaseFont.COURIER), IdentValue.NORMAL, 400));

        result.put("DialogInput", courier);
        result.put("Monospaced", courier);
        result.put("Courier", courier);
    }

    private static void addTimes(HashMap result) throws DocumentException, IOException {
        Docx4jFontFamily times = new Docx4jFontFamily();
        times.setName("Times");

        times.addFontDescription(new Docx4jFontDescription(
                createFont(BaseFont.TIMES_BOLDITALIC), IdentValue.ITALIC, 700));
        times.addFontDescription(new Docx4jFontDescription(
                createFont(BaseFont.TIMES_ITALIC), IdentValue.ITALIC, 400));
        times.addFontDescription(new Docx4jFontDescription(
                createFont(BaseFont.TIMES_BOLD), IdentValue.NORMAL, 700));
        times.addFontDescription(new Docx4jFontDescription(
                createFont(BaseFont.TIMES_ROMAN), IdentValue.NORMAL, 400));

        result.put("Serif", times);
        result.put("TimesRoman", times);
    }

    private static void addHelvetica(HashMap result) throws DocumentException, IOException {
        Docx4jFontFamily helvetica = new Docx4jFontFamily();
        helvetica.setName("Helvetica");

        helvetica.addFontDescription(new Docx4jFontDescription(
                createFont(BaseFont.HELVETICA_BOLDOBLIQUE), IdentValue.OBLIQUE, 700));
        helvetica.addFontDescription(new Docx4jFontDescription(
                createFont(BaseFont.HELVETICA_OBLIQUE), IdentValue.OBLIQUE, 400));
        helvetica.addFontDescription(new Docx4jFontDescription(
                createFont(BaseFont.HELVETICA_BOLD), IdentValue.NORMAL, 700));
        helvetica.addFontDescription(new Docx4jFontDescription(
                createFont(BaseFont.HELVETICA), IdentValue.NORMAL, 400));

        result.put("Dialog", helvetica);
        result.put("SansSerif", helvetica);
    }
    
}
