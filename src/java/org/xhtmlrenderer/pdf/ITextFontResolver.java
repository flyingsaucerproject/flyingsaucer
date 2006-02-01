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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.	See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 * }}}
 */
package org.xhtmlrenderer.pdf;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.xhtmlrenderer.css.constants.IdentValue;
import org.xhtmlrenderer.css.value.FontSpecification;
import org.xhtmlrenderer.extend.FontResolver;
import org.xhtmlrenderer.layout.SharedContext;
import org.xhtmlrenderer.render.FSFont;

import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.BaseFont;

/**
 * This class is mix of {@link org.xhtmlrenderer.context.FontResolver} and
 * {@link com.lowagie.text.pdf.DefaultFontMapper} 
 */
public class ITextFontResolver implements FontResolver {
    private Map _fontFamilies = createInitialFontMap();
    
    private Map _fontCache = new HashMap();

    public FSFont resolveFont(SharedContext renderingContext, FontSpecification spec) {
        return resolveFont(renderingContext, spec.families, spec.size, spec.fontWeight, spec.fontStyle, spec.variant);
    }
    
    public void flushCache() {
        _fontFamilies = createInitialFontMap();
        _fontCache = new HashMap();
    }
    
    private FSFont resolveFont(SharedContext ctx, String[] families, float size, IdentValue weight, IdentValue style, IdentValue variant) {
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
    
    private String normalizeFontFamily(String fontFamily) {
        String result = fontFamily;
        // strip off the "s if they are there
        if (result.startsWith("\"")) {
            result = result.substring(1);
        }
        if (result.endsWith("\"")) {
            result = result.substring(0, fontFamily.length() - 1);
        }

        // normalize the font name
        if (result.equals("serif")) {
            result = "Serif";
        }
        else if (result.equals("sans-serif")) {
            result = "SansSerif";
        }
        else if (result.equals("monospace")) {
            result = "Monospaced";
        }
        
        return result;
    }
    
    private FSFont resolveFont(SharedContext ctx, String fontFamily, float size, IdentValue weight, IdentValue style, IdentValue variant) {
        String normalizedFontFamily = normalizeFontFamily(fontFamily);

        String cacheKey = getHashName(normalizedFontFamily, weight, style);
        BaseFont result = (BaseFont)_fontCache.get(cacheKey);
        if (result != null) {
            return new ITextFSFont(result, size);
        }
        
        FontFamily family = (FontFamily)_fontFamilies.get(normalizedFontFamily);
        if (family != null) {
            result = family.match(convertWeightToInt(weight), style);
            if (result != null) {
                _fontCache.put(cacheKey, result);
                return new ITextFSFont(result, size);
            }
        }
        
        return null;
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
    
    protected static String getHashName(
            String name, IdentValue weight, IdentValue style) {
        return name + "-" + weight + "-" + style;
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
        FontFamily courier = new FontFamily();
        courier.setName("Courier");
        
        courier.addFontDescription(new FontDescription(
                createFont(BaseFont.COURIER_BOLDOBLIQUE), IdentValue.OBLIQUE, 700));
        courier.addFontDescription(new FontDescription(
                createFont(BaseFont.COURIER_OBLIQUE), IdentValue.OBLIQUE, 400));
        courier.addFontDescription(new FontDescription(
                createFont(BaseFont.COURIER_BOLD), IdentValue.NORMAL, 700));
        courier.addFontDescription(new FontDescription(
                createFont(BaseFont.COURIER), IdentValue.NORMAL, 400));        
        
        result.put("DialogInput", courier);
        result.put("Monospaced", courier);
        result.put("Courier", courier);
    }
    
    private static void addTimes(HashMap result) throws DocumentException, IOException {
        FontFamily times = new FontFamily();
        times.setName("Times");
        
        times.addFontDescription(new FontDescription(
                createFont(BaseFont.TIMES_BOLDITALIC), IdentValue.ITALIC, 700));
        times.addFontDescription(new FontDescription(
                createFont(BaseFont.TIMES_ITALIC), IdentValue.ITALIC, 400));
        times.addFontDescription(new FontDescription(
                createFont(BaseFont.TIMES_BOLD), IdentValue.NORMAL, 700));
        times.addFontDescription(new FontDescription(
                createFont(BaseFont.TIMES_ROMAN), IdentValue.NORMAL, 400));  
        
        result.put("Serif", times);
        result.put("TimesRoman", times);
    }
    
    private static void addHelvetica(HashMap result) throws DocumentException, IOException {
        FontFamily helvetica = new FontFamily();
        helvetica.setName("Helvetica");
        
        helvetica.addFontDescription(new FontDescription(
                createFont(BaseFont.HELVETICA_BOLDOBLIQUE), IdentValue.OBLIQUE, 700));
        helvetica.addFontDescription(new FontDescription(
                createFont(BaseFont.HELVETICA_OBLIQUE), IdentValue.OBLIQUE, 400));
        helvetica.addFontDescription(new FontDescription(
                createFont(BaseFont.HELVETICA_BOLD), IdentValue.NORMAL, 700));
        helvetica.addFontDescription(new FontDescription(
                createFont(BaseFont.HELVETICA), IdentValue.NORMAL, 400));  
        
        result.put("Dialog", helvetica);
        result.put("SansSerif", helvetica);
    } 
    
    private static class FontFamily {
        private String _name;
        private List _fontDescriptions;
        
        public FontFamily() {
        }

        public List getFontDescriptions() {
            return _fontDescriptions;
        }

        public void addFontDescription(FontDescription descr) {
            if (_fontDescriptions == null) {
                _fontDescriptions = new ArrayList();
            }
            _fontDescriptions.add(descr);
            Collections.sort(_fontDescriptions,
                    new Comparator() {
                        public int compare(Object o1, Object o2) {
                            FontDescription f1 = (FontDescription)o1;
                            FontDescription f2 = (FontDescription)o2;
                            return f1.getWeight() - f2.getWeight();
                        }
            });
        }

        public String getName() {
            return _name;
        }

        public void setName(String name) {
            _name = name;
        }
        
        public BaseFont match(int desiredWeight, IdentValue style) {
            if (_fontDescriptions == null) {
                throw new RuntimeException("fontDescriptions is null");
            }
            
            List candidates = new ArrayList();
            
            for (Iterator i = _fontDescriptions.iterator(); i.hasNext(); ) {
                FontDescription description = (FontDescription)i.next();
                
                if (description.getStyle() == style) {
                    candidates.add(description);
                }
            }
            
            if (candidates.size() == 0) {
                if (style == IdentValue.ITALIC) {
                    return match(desiredWeight, IdentValue.OBLIQUE);
                } else if (style == IdentValue.OBLIQUE) {
                    return match(desiredWeight, IdentValue.NORMAL);
                } else {
                    return null;
                }
            }
            
            FontDescription[] matches = (FontDescription[]) 
                candidates.toArray(new FontDescription[candidates.size()]);
            BaseFont result;
            
            result = findByWeight(matches, desiredWeight, SM_EXACT);
            
            if (result != null) {
                return result;
            } else {
                if (desiredWeight <= 500) {
                    return findByWeight(matches, desiredWeight, SM_LIGHTER_OR_DARKER);
                } else {
                    return findByWeight(matches, desiredWeight, SM_DARKER_OR_LIGHTER);
                }
            }
        }
        
        private static final int SM_EXACT = 1;
        private static final int SM_LIGHTER_OR_DARKER = 2;
        private static final int SM_DARKER_OR_LIGHTER = 3;
        
        private BaseFont findByWeight(FontDescription[] matches, 
                int desiredWeight, int searchMode) {
            if (searchMode == SM_EXACT) {
                for (int i = 0; i < matches.length; i++) {
                    FontDescription descr = matches[i];
                    if (descr.getWeight() == desiredWeight) {
                        return descr.getFont();
                    } 
                }
                return null;
            } else if (searchMode == SM_LIGHTER_OR_DARKER){
                int offset = 0;
                FontDescription descr = null;
                for (offset = 0; offset < matches.length; offset++) {
                    descr = matches[offset];
                    if (descr.getWeight() > desiredWeight) {
                        break;
                    }
                }
                
                if (offset > 0 && descr.getWeight() > desiredWeight) {
                    return matches[offset-1].getFont();
                } else {
                    return descr.getFont();
                }
                
            } else if (searchMode == SM_DARKER_OR_LIGHTER) {
                int offset = 0;
                FontDescription descr = null;
                for (offset = matches.length - 1; offset >= 0; offset--) {
                    descr = matches[offset];
                    if (descr.getWeight() < desiredWeight) {
                        break;
                    }
                }
                
                if (offset != matches.length - 1 && descr.getWeight() < desiredWeight) {
                    return matches[offset+1].getFont();
                } else {
                    return descr.getFont();
                }
            }
            
            return null;
        }
    }
    
    private static class FontDescription {
        private IdentValue _style;
        private int _weight;
        
        private BaseFont _font;
        
        public FontDescription() {
        }
        
        public FontDescription(BaseFont font, IdentValue style, int weight) {
            _font = font;
            _style = style;
            _weight = weight;
        }

        public BaseFont getFont() {
            return _font;
        }

        public void setFont(BaseFont font) {
            _font = font;
        }

        public int getWeight() {
            return _weight;
        }

        public void setWeight(int weight) {
            _weight = weight;
        }

        public IdentValue getStyle() {
            return _style;
        }

        public void setStyle(IdentValue style) {
            _style = style;
        }
    }    
}
