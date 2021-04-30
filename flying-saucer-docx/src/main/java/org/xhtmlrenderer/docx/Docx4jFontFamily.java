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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.xhtmlrenderer.css.constants.IdentValue;

public class Docx4jFontFamily {
    
    private String _name;
    private List _fontDescriptions;

    public Docx4jFontFamily() {
    }

    public List getFontDescriptions() {
        return _fontDescriptions;
    }

    public void addFontDescription(Docx4jFontDescription descr) {
        if (_fontDescriptions == null) {
            _fontDescriptions = new ArrayList();
        }
        _fontDescriptions.add(descr);
        Collections.sort(_fontDescriptions,
                new Comparator() {
                    public int compare(Object o1, Object o2) {
                        Docx4jFontDescription f1 = (Docx4jFontDescription)o1;
                        Docx4jFontDescription f2 = (Docx4jFontDescription)o2;
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

    public Docx4jFontDescription match(int desiredWeight, IdentValue style) {
        if (_fontDescriptions == null) {
            throw new RuntimeException("fontDescriptions is null");
        }

        List candidates = new ArrayList();

        for (Iterator i = _fontDescriptions.iterator(); i.hasNext(); ) {
            Docx4jFontDescription description = (Docx4jFontDescription)i.next();

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
                candidates.addAll(_fontDescriptions);
            }
        }

        Docx4jFontDescription[] matches = (Docx4jFontDescription[])
            candidates.toArray(new Docx4jFontDescription[candidates.size()]);
        Docx4jFontDescription result;

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

    private Docx4jFontDescription findByWeight(Docx4jFontDescription[] matches,
            int desiredWeight, int searchMode) {
        if (searchMode == SM_EXACT) {
            for (int i = 0; i < matches.length; i++) {
                Docx4jFontDescription descr = matches[i];
                if (descr.getWeight() == desiredWeight) {
                    return descr;
                }
            }
            return null;
        } else if (searchMode == SM_LIGHTER_OR_DARKER){
            int offset = 0;
            Docx4jFontDescription descr = null;
            for (offset = 0; offset < matches.length; offset++) {
                descr = matches[offset];
                if (descr.getWeight() > desiredWeight) {
                    break;
                }
            }

            if (offset > 0 && descr.getWeight() > desiredWeight) {
                return matches[offset-1];
            } else {
                return descr;
            }

        } else if (searchMode == SM_DARKER_OR_LIGHTER) {
            int offset = 0;
            Docx4jFontDescription descr = null;
            for (offset = matches.length - 1; offset >= 0; offset--) {
                descr = matches[offset];
                if (descr.getWeight() < desiredWeight) {
                    break;
                }
            }

            if (offset != matches.length - 1 && descr.getWeight() < desiredWeight) {
                return matches[offset+1];
            } else {
                return descr;
            }
        }

        return null;
    }
}
