/*
 * {{{ header & license
 * XRStyleSheet.java
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

import java.util.*;

import org.w3c.dom.css.CSSStyleSheet;


/**
 * <p>
 *
 * Interface to a CSS StyleSheet in XR; more generally, a collection of style
 * declarations/rules Note "stylesheet" is treated liberally, so that it can
 * include:</p>
 * <ul>
 *   <li> .css text-format files included with <code>
 *
 *
 *
 *
 *<link> </code> tags</li>
 *   <li> inline <code><style></code> declarations</li>
 *   <li> element style="" attributes</li>
 *   <li> element HTML styling attributes (converted to CSS2)</li>
 *   <li> "user agent" CSS</li>
 *   <li> "user" CSS</li>
 * </ul>
 * <p>
 *
 * An XRStyleSheet is said to have a "sequence", which in the simple case would
 * be the "sequence" in which it was loaded. This sequence is used to determine
 * priority within the CSS cascade--so, for example, the user-agent stylesheet
 * would have sequence 0 (lowest priority), user 1, author 2. For imported
 * stylesheets, you can use the same sequence as the owner sheet, as long as the
 * rules are loaded such that imported rules *precede* the rules in the owner
 * sheet, so that the owner sheet rules override matching selectors (see CSS2
 * spec).</p>
 *
 * @author    Patrick Wright
 *
 */
public interface XRStyleSheet extends CSSStyleSheet {
    /** Origin of stylesheet - user agent */
    public final static int USER_AGENT = 0;

    /** Origin of stylesheet - author */
    public final static int AUTHOR = 1;

    /** Origin of stylesheet - user */
    public final static int USER = 2;


    /**
     * The sequence in which the sheet was found and loaded. Sheets get priority
     * in terms of loading order--user agent sheets, inline styles, HTML attrs
     * as styles would have lower sequence that user and author sheets. This
     * number used in cascade logic.
     *
     * @return   Returns
     */
    int sequence();


    /**
     * Appends a representation of the stylesheet to the StringBuffer.
     *
     * @param sb  PARAM
     */
    void dump( StringBuffer sb );


    /**
     * An Iterator of all XRStyleRules found in this sheet. Rule ordering is not
     * guarranteed by this method, but should be the order in which rules were
     * read from the source stylesheet.
     *
     * @return   Returns
     */
    Iterator styleRules();


    /**
     * The origin of the stylesheet.
     *
     * @return   Returns
     */
    // ASK: this could be replaced with three isAuthor/isUserAgent/isUser methods...
    // no reason to force API users to mess with int constants if they don't have to (PWW 15/08/04)
    int origin();


    /**
     * Simple label for stylesheet origin--USER-AGENT, AUTHOR, USER
     *
     * @return   Returns
     */
    String orginLabel();
}// end interface


