/*
 * PropertyDeclaration.java
 * Copyright (c) 2004 Torbjörn Gannholm
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
 *
 */
package org.xhtmlrenderer.css.sheet;


/**
 * Represents a single property declared in a CSS rule set. A
 * PropertyDeclaration is created from an {@link
 * org.xhtmlrenderer.css.XRProperty} and is immutable. The declaration knows its
 * origin, importance and specificity, and thus is prepared to be sorted out
 * among properties of the same name, within a matched group, for the CSS
 * cascade, into a {@link org.xhtmlrenderer.css.newmatch.CascadedStyle}.
 *
 * @author Torbjörn Gannholm
 */
public class PropertyDeclaration {
    /**
     * The XRProperty instance we are wrapping.
     */
    private org.xhtmlrenderer.css.XRProperty base;

    /**
     * Whether the property was declared as important! by the user.
     */
    private boolean important;

    /**
     * Origin constant from the list defined in {@link Stylesheet}. See {@link
     * Stylesheet#USER_AGENT}, {@link Stylesheet#USER}, and {@link
     * Stylesheet#AUTHOR}.
     */
    private int origin;

    /**
     * ImportanceAndOrigin of stylesheet - how many different
     */
    public final static int IMPORTANCE_AND_ORIGIN_COUNT = 6;

    /**
     * ImportanceAndOrigin of stylesheet - user agent
     */
    private final static int CSS_DEFAULT = 0;

    /**
     * ImportanceAndOrigin of stylesheet - user agent
     */
    private final static int USER_AGENT = 1;

    /**
     * ImportanceAndOrigin of stylesheet - user normal
     */
    private final static int USER_NORMAL = 2;

    /**
     * ImportanceAndOrigin of stylesheet - author normal
     */
    private final static int AUTHOR_NORMAL = 3;

    /**
     * ImportanceAndOrigin of stylesheet - author important
     */
    private final static int AUTHOR_IMPORTANT = 4;

    /**
     * ImportanceAndOrigin of stylesheet - user important
     */
    private final static int USER_IMPORTANT = 5;

    /**
     * Creates a new instance of PropertyDeclaration from an {@link
     * org.xhtmlrenderer.css.XRProperty} instance.
     *
     * @param p    The XRProperty instance to create the PropertyDeclaration
     *             from.
     * @param imp  True if property was declared important! and false if not.
     * @param orig int constant from {@link Stylesheet} for the origin of the
     *             property declaration, that is, the origin of the style sheet where
     *             it was declared. See {@link Stylesheet#USER_AGENT}, {@link
     *             Stylesheet#USER}, and {@link Stylesheet#AUTHOR}.
     */
    public PropertyDeclaration(org.xhtmlrenderer.css.XRProperty p, boolean imp, int orig) {
        base = p;
        important = imp;
        origin = orig;
    }

    /**
     * Returns an int representing the combined origin and importance of the
     * property as declared. The int is assigned such that default origin and
     * importance is 0, and highest an important! property defined by the user
     * (origin is Stylesheet.USER). The combined value would allow this property
     * to be sequenced in the CSS cascade along with other properties matched to
     * the same element with the same property name. In that sort, the highest
     * sequence number returned from this method would take priority in the
     * cascade, so that a user important! property would override a user
     * non-important! property, and so on. The actual integer value returned by
     * this method is unimportant, but has a lowest value of 0 and increments
     * sequentially by 1 for each increase in origin/importance..
     *
     * @return See method javadoc.
     */
    public int getImportanceAndOrigin() {
        if (origin == StylesheetInfo.USER_AGENT) {
            return PropertyDeclaration.USER_AGENT;
        } else if (origin == StylesheetInfo.USER) {
            if (important) {
                return PropertyDeclaration.USER_IMPORTANT;
            }
            return PropertyDeclaration.USER_NORMAL;
        } else {
            if (important) {
                return PropertyDeclaration.AUTHOR_IMPORTANT;
            }
            return PropertyDeclaration.AUTHOR_NORMAL;
        }
    }

    /**
     * Returns the CSS name of this property, e.g. "font-family".
     *
     * @return See desc.
     */
    public String getName() {
        return base.propertyName();
    }

    /**
     * Returns the specified {@link org.w3c.dom.css.CSSValue} for this property.
     * Specified means the value as entered by the user. Modifying the CSSValue
     * returned here will result in indeterminate behavior--consider it
     * immutable.
     *
     * @return See desc.
     */
    public org.w3c.dom.css.CSSValue getValue() {
        return base.specifiedValue().cssValue();
    }
}// end class

/*
 * $Id$
 *
 * $Log$
 * Revision 1.5  2004/12/11 18:18:07  tobega
 * Still broken, won't even compile at the moment. Working hard to fix it, though. Replace the StyleReference interface with our only concrete implementation, it was a bother changing in two places all the time.
 *
 * Revision 1.4  2004/11/28 23:29:01  tobega
 * Now handles media on Stylesheets, still need to handle at-media-rules. The media-type should be set in Context.media (set by default to "screen") before calling setContext on StyleReference.
 *
 * Revision 1.3  2004/11/16 10:39:34  pdoubleya
 * Made members all private where appropriate.
 * Comments.
 *
 * Revision 1.2  2004/11/15 12:42:23  pdoubleya
 * Across this checkin (all may not apply to this particular file)
 * Changed default/package-access members to private.
 * Changed to use XRRuntimeException where appropriate.
 * Began move from System.err.println to std logging.
 * Standard code reformat.
 * Removed some unnecessary SAC member variables that were only used in initialization.
 * CVS log section.
 *
 *
 */

