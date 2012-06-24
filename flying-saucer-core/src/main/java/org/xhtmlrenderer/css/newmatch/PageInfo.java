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
package org.xhtmlrenderer.css.newmatch;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.xhtmlrenderer.css.constants.CSSName;
import org.xhtmlrenderer.css.constants.IdentValue;
import org.xhtmlrenderer.css.constants.MarginBoxName;
import org.xhtmlrenderer.css.parser.PropertyValue;
import org.xhtmlrenderer.css.sheet.PropertyDeclaration;
import org.xhtmlrenderer.css.sheet.StylesheetInfo;

public class PageInfo {
    private final List _properties;
    private final CascadedStyle _pageStyle;
    private final Map _marginBoxes;
    
    private final List _xmpPropertyList;
    
    public PageInfo(List properties, CascadedStyle pageStyle, Map marginBoxes) {
        _properties = properties;
        _pageStyle = pageStyle;
        _marginBoxes = marginBoxes;
        
        _xmpPropertyList = (List)marginBoxes.remove(MarginBoxName.FS_PDF_XMP_METADATA);
    }

    public Map getMarginBoxes() {
        return _marginBoxes;
    }
    
    public CascadedStyle getPageStyle() {
        return _pageStyle;
    }
    
    public List getProperties() {
        return _properties;
    }
    
    public CascadedStyle createMarginBoxStyle(MarginBoxName marginBox, boolean alwaysCreate) {
        List marginProps = (List)_marginBoxes.get(marginBox);
        
        if ((marginProps == null || marginProps.size() == 0) && ! alwaysCreate) {
            return null;
        }
        
        List all;
        if (marginProps != null) {
            all = new ArrayList(marginProps.size() + 3);
            all.addAll(marginProps);    
        } else {
            all = new ArrayList(3);
        }
        
        all.add(CascadedStyle.createLayoutPropertyDeclaration(CSSName.DISPLAY, IdentValue.TABLE_CELL));
        all.add(new PropertyDeclaration(
                    CSSName.VERTICAL_ALIGN, 
                    new PropertyValue(marginBox.getInitialVerticalAlign()), 
                    false,
                    StylesheetInfo.USER_AGENT));
        all.add(new PropertyDeclaration(
                CSSName.TEXT_ALIGN, 
                new PropertyValue(marginBox.getInitialTextAlign()), 
                false,
                StylesheetInfo.USER_AGENT));        
                        
        
        return new CascadedStyle(all.iterator());
    }
    
    public boolean hasAny(MarginBoxName[] marginBoxes) {
        for (int i = 0; i < marginBoxes.length; i++) {
            if (_marginBoxes.containsKey(marginBoxes[i])) {
                return true;
            }
        }
        
        return false;
    }
    
    public List getXMPPropertyList()
    {
        return _xmpPropertyList;
    }
}
