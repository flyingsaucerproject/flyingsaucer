/*
 * Stylesheet.java
 * Copyright (c) 2004, 2005 Patrick Wright, Torbjörn Gannholm
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
 * A representation of a CSS style sheet. A Stylesheet has the sheet's rules in
 * {@link Ruleset}, and has an origin--either user agent, user, or author. A
 * Stylesheet can only be instantiated from a SAC CSSStyleSheet instance-- this
 * would be the output of a SAC-compliant parser after parsing a CSS stream or
 * source. A Stylesheet is immutable; after instantiation, you can query the
 * origin and the {@link Ruleset}, but not modify either of them.
 *
 * @author   Torbjörn Gannholm
 * @author   Patrick Wright
 */
public class Stylesheet {
    /** The info for this stylesheet  */
    private String _uri;
    /** Description of the Field */
    private int _origin;

    /** Description of the Field  */
    private java.util.List _rulesets;

    /**
     * Creates a new instance of Stylesheet
     *
     * @param uri
     * @param origin
     */
    public Stylesheet( String uri, int origin ) {
        _uri = uri;
        _origin = origin;
        _rulesets = new java.util.LinkedList();
    }

    /**
     * Gets the origin attribute of the Stylesheet object
     *
     * @return   The origin value
     */
    public int getOrigin() {
        return _origin;
    }

    /**
     * Gets the URI of the Stylesheet object
     *
     * @return   The URI
     */
    public String getURI() {
        return _uri;
    }

    /**
     * Returns an iterator over the Rulesets and embedded Stylesheets loaded
     * from the source stylesheet.
     *
     * @return   The rulesets value
     */
    public java.util.Iterator getRulesets() {
        return _rulesets.iterator();
    }

    /**
     * Set the Rulesets to this stylesheet. Should usually only be called by
     * StylesheetFactory.
     *
     * @param r  The feature to be added to the Ruleset attribute
     */
    void addRuleset( Ruleset r ) {
        _rulesets.add( r );
    }

    /**
     * Set the imported stylesheet Rulesets to this stylesheet. Should usually
     * only be called by StylesheetFactory.
     *
     * @param s  The feature to be added to the Stylesheet attribute
     */
    void addStylesheet( StylesheetInfo s ) {
        _rulesets.add( s );
    }
}// end class

/*
 * $Id$
 *
 * $Log$
 * Revision 1.10  2005/01/29 20:19:21  pdoubleya
 * Clean/reformat code. Removed commented blocks, checked copyright.
 *
 * Revision 1.9  2004/12/11 18:18:07  tobega
 * Still broken, won't even compile at the moment. Working hard to fix it, though. Replace the StyleReference interface with our only concrete implementation, it was a bother changing in two places all the time.
 *
 * Revision 1.8  2004/12/02 19:46:36  tobega
 * Refactored handling of inline styles to fit with StylesheetInfo and media handling (is also now correct if there should be more than one style element)
 *
 * Revision 1.7  2004/11/29 23:25:39  tobega
 * Had to redo thinking about Stylesheets and StylesheetInfos. Now StylesheetInfos are passed around instead of Stylesheets because any Stylesheet should only be linked to its URI. Bonus: the external sheets get lazy-loaded only if needed for the medium.
 *
 * Revision 1.6  2004/11/28 23:29:02  tobega
 * Now handles media on Stylesheets, still need to handle at-media-rules. The media-type should be set in Context.media (set by default to "screen") before calling setContext on StyleReference.
 *
 * Revision 1.5  2004/11/15 22:22:08  tobega
 * Now handles @import stylesheets
 *
 * Revision 1.4  2004/11/15 20:06:31  tobega
 * Should now handle @import stylesheets, at least those with absolute urls
 *
 * Revision 1.3  2004/11/15 19:46:14  tobega
 * Refactoring in preparation for handling @import stylesheets
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

