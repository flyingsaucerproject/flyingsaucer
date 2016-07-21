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
package org.xhtmlrenderer.css.sheet;

import java.util.Iterator;

import org.xhtmlrenderer.css.newmatch.CascadedStyle;
import org.xhtmlrenderer.css.style.CalculatedStyle;
import org.xhtmlrenderer.css.style.EmptyStyle;
import org.xhtmlrenderer.util.XRRuntimeException;

public class FontFaceRule implements RulesetContainer {
    private int _origin;
    private Ruleset _ruleset;
    private CalculatedStyle _calculatedStyle;

    public FontFaceRule(int origin) {
        _origin = origin;
    }

    public void addContent(Ruleset ruleset) {
        if (_ruleset != null) {
            throw new XRRuntimeException("Ruleset can only be set once");
        }
        _ruleset = ruleset;
    }

    public int getOrigin() {
        return _origin;
    }

    public void setOrigin(int origin) {
        _origin = origin;
    }

    public CalculatedStyle getCalculatedStyle() {
        if (_calculatedStyle == null) {
            _calculatedStyle = new EmptyStyle().deriveStyle(
                    CascadedStyle.createLayoutStyle(_ruleset.getPropertyDeclarations()));
        }

        return _calculatedStyle;
    }

    public boolean hasFontFamily() {
        for (Iterator i = _ruleset.getPropertyDeclarations().iterator(); i.hasNext(); ) {
            PropertyDeclaration decl = (PropertyDeclaration)i.next();
            if (decl.getPropertyName().equals("font-family")) {
                return true;
            }
        }

        return false;
    }

    public boolean hasFontWeight() {
        for (Iterator i = _ruleset.getPropertyDeclarations().iterator(); i.hasNext(); ) {
            PropertyDeclaration decl = (PropertyDeclaration)i.next();
            if (decl.getPropertyName().equals("font-weight")) {
                return true;
            }
        }

        return false;
    }

    public boolean hasFontStyle() {
        for (Iterator i = _ruleset.getPropertyDeclarations().iterator(); i.hasNext(); ) {
            PropertyDeclaration decl = (PropertyDeclaration)i.next();
            if (decl.getPropertyName().equals("font-style")) {
                return true;
            }
        }

        return false;
    }
}
