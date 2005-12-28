/*
 * {{{ header & license
 * Copyright (c) 2005 Wisconsin Court System
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
package org.xhtmlrenderer.css.sheet.factory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.w3c.dom.css.CSSPrimitiveValue;
import org.xhtmlrenderer.css.constants.CSSName;
import org.xhtmlrenderer.css.value.FSCssValue;
import org.xhtmlrenderer.css.value.PageSize;

public class SizePropertyDeclarationFactory extends AbstractPropertyDeclarationFactory {
    private static SizePropertyDeclarationFactory _instance;
    
    private SizePropertyDeclarationFactory() {
    }
    
    protected Iterator doBuildDeclarations(CSSPrimitiveValue[] primVals, boolean important, CSSName cssName, int origin) {
        List declarations = new ArrayList();
        CSSPrimitiveValue primitive = null;
        CSSPrimitiveValue primitives[] = new CSSPrimitiveValue[1];
        CSSName names[] = new CSSName[1];
        
        if (primVals.length == 1) {
            primitive = primVals[0];
            String val = primitive.getCssText().trim();
            if (val.equals("landscape") || val.equals("portrait")) {
                names[0] = CSSName.FS_PAGE_WIDTH;
                primitives[0] = FSCssValue.getNewIdentValue("auto");
                addProperties(declarations, primitives, names, origin, important);
                names[0] = CSSName.FS_PAGE_HEIGHT;
                addProperties(declarations, primitives, names, origin, important);
                names[0] = CSSName.FS_PAGE_ORIENTATION;
                addProperties(declarations, primVals, names, origin, important);
            } else if (PageSize.resolvePageSize(val) != null) {
                PageSize pageSize = PageSize.resolvePageSize(val);
                names[0] = CSSName.FS_PAGE_WIDTH;
                primitives[0] = pageSize.getPageWidth();
                addProperties(declarations, primitives, names, origin, important);
                names[0] = CSSName.FS_PAGE_HEIGHT;
                primitives[0] = pageSize.getPageHeight();
                addProperties(declarations, primitives, names, origin, important);
                
                names[0] = CSSName.FS_PAGE_ORIENTATION;
                primitives[0] = FSCssValue.getNewIdentValue("auto");
                addProperties(declarations, primitives, names, origin, important);
            } else {
                // <length>{1} or auto
                names[0] = CSSName.FS_PAGE_WIDTH;
                addProperties(declarations, primVals, names, origin, important);
                names[0] = CSSName.FS_PAGE_HEIGHT;
                addProperties(declarations, primVals, names, origin, important);
                
                names[0] = CSSName.FS_PAGE_ORIENTATION;
                primitives[0] = FSCssValue.getNewIdentValue("auto");
                addProperties(declarations, primitives, names, origin, important);
            }
        } else if (primVals.length == 2) {
            primitive = primVals[1];
            String val = primitive.getCssText().trim();
            
            if (val.equals("landscape") || val.equals("portrait")) {
                PageSize pageSize = PageSize.resolvePageSize(
                        primVals[0].getCssText().trim());
                names[0] = CSSName.FS_PAGE_WIDTH;
                primitives[0] = pageSize == null ? 
                        FSCssValue.getNewIdentValue("auto") : pageSize.getPageWidth();
                addProperties(declarations, primitives, names, origin, important);
                names[0] = CSSName.FS_PAGE_HEIGHT;
                primitives[0] = pageSize == null ?
                        FSCssValue.getNewIdentValue("auto") : pageSize.getPageHeight();
                addProperties(declarations, primitives, names, origin, important);  
                
                names[0] = CSSName.FS_PAGE_ORIENTATION;
                primitives[0] = primVals[1];
                addProperties(declarations, primitives, names, origin, important);
            } else {
                // <length>{2}
                names[0] = CSSName.FS_PAGE_WIDTH;
                primitives[0] = primVals[0];
                addProperties(declarations, primitives, names, origin, important);
                
                names[0] = CSSName.FS_PAGE_HEIGHT;
                primitives[0] = primVals[1];
                addProperties(declarations, primitives, names, origin, important);
                
                names[0] = CSSName.FS_PAGE_ORIENTATION;
                primitives[0] = FSCssValue.getNewIdentValue("auto");
                addProperties(declarations, primitives, names, origin, important);
            }
        }

        return declarations.iterator();
    }
    
    public static synchronized PropertyDeclarationFactory instance() {
        if (_instance == null) {
            _instance = new SizePropertyDeclarationFactory();
        }
        return _instance;
    }

}
