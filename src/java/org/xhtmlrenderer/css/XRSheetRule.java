/*
 * {{{ header & license
 * XRSheetRule.java
 * Copyright (c) 2004 Patrick Wright
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
package org.xhtmlrenderer.css;

import org.w3c.dom.css.CSSRule;


/**
 * Interface for CSS rules (@ rules, style rules). An XRRule is also a DOM
 * CSSRule. Rules have a sequence which is the order they were found within
 * their stylesheet. A rule belongs to a CSS stylesheet, and is marked important
 * iff it contains only properties marked !important
 *
 * @author Patrick Wright
 */
public interface XRSheetRule extends CSSRule, XRRule {

// --Commented out by Inspection START (2005-01-05 00:31):
//    /**
//     * Returns the stylesheet for this rule...rules always exist in the context
//     * of a sheet.
//     *
//     * @return   The styleSheet value
//     */
//    XRStyleSheet getStyleSheet();
// --Commented out by Inspection STOP (2005-01-05 00:31)


// --Commented out by Inspection START (2005-01-05 00:31):
//    /**
//     * Returns true if this rule is restricted to properties marked
//     * "!important". Normally "!important" properties may be mixed freely with
//     * non-important ones; however, in XR we separate these out to facilitate
//     * the cascade logic, effectively making two rules with the same selector,
//     * one important (higher priority) and one not.
//     *
//     * @return   The important value
//     */
//    boolean isImportant();
// --Commented out by Inspection STOP (2005-01-05 00:31)


// --Commented out by Inspection START (2005-01-05 00:31):
//    /**
//     * The numeric sequence in which this rule was found in the stylesheet.
//     *
//     * @return   Returns
//     */
//    int sequenceInStyleSheet();
// --Commented out by Inspection STOP (2005-01-05 00:31)

}

/*
 * $Id$
 *
 * $Log$
 * Revision 1.3  2005/01/05 01:10:14  tobega
 * Went wild with code analysis tool. removed unused stuff. Lucky we have CVS...
 *
 * Revision 1.2  2004/10/23 13:03:47  pdoubleya
 * Re-formatted using JavaStyle tool.
 * Cleaned imports to resolve wildcards except for common packages (java.io, java.util, etc)
 * Added CVS log comments at bottom.
 *
 *
 */

