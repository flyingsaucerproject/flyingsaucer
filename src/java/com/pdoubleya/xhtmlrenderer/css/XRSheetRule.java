/*
 * {{{ header & license
 * XRSheetRule.java
 * Copyright (c) 2004 Patrick Wright
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.	See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 * }}}
 */
package com.pdoubleya.xhtmlrenderer.css;

import org.w3c.dom.css.CSSRule;


/**
 * Interface for CSS rules (@ rules, style rules). An XRRule is also a DOM
 * CSSRule. Rules have a sequence which is the order they were found within
 * their stylesheet. A rule belongs to a CSS stylesheet, and is marked important
 * iff it contains only properties marked !important
 *
 * @author    Patrick Wright
 *
 */
public interface XRSheetRule extends XRRule, CSSRule {

    /**
     * Returns the stylesheet for this rule...rules always exist in the context
     * of a sheet.
     *
     * @return   The styleSheet value
     */
    XRStyleSheet getStyleSheet();


    /**
     * Returns true if this rule is restricted to properties marked
     * "!important". Normally "!important" properties may be mixed freely with
     * non-important ones; however, in XR we separate these out to facilitate
     * the cascade logic, effectively making two rules with the same selector,
     * one important (higher priority) and one not.
     *
     * @return   The important value
     */
    boolean isImportant();


    /**
     * The numeric sequence in which this rule was found in the stylesheet.
     *
     * @return   Returns
     */
    int sequenceInStyleSheet();

}

