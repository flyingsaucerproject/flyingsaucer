/*
 * Stylesheet.java
 * Copyright (c) 2004 Patrick Wright, Torbjörn Gannholm
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

import java.util.Iterator;


/**
 * A representation of a CSS style sheet. A Stylesheet has the sheet's rules in
 * {@link Ruleset}, and has an origin--either user agent, user, or author. A Stylesheet
 * can only be instantiated from a SAC CSSStyleSheet instance-- this would be
 * the output of a SAC-compliant parser after parsing a CSS stream or source. A
 * Stylesheet is immutable; after instantiation, you can query the origin and
 * the {@link Ruleset}, but not modify either of them.
 *
 * @author   Torbjörn Gannholm
 * @author   Patrick Wright
 */
public class Stylesheet {
    /** Description of the Field */
    private int _origin;

    /** Description of the Field */
    private java.util.List _rulesets;

    /** Origin of stylesheet - user agent */
    public final static int USER_AGENT = 0;

    /** Origin of stylesheet - user */
    public final static int USER = 1;

    /** Origin of stylesheet - author */
    public final static int AUTHOR = 2;

    /**
     * Creates a new instance of Stylesheet
     *
     * @param origin  One of the integer constants USER_AGENT, USER or AUTHOR,
     *      indicates where the sheet originated and thus its precedence in the
     *      cascade.
     */
    public Stylesheet( int origin ) {
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
     * Returns the Rulesets loaded from the source stylesheet.
     *
     * @return   The rulesets value
     */
    public java.util.Iterator getRulesets() {
        return _rulesets.iterator();
    }

    /**
     * Set the Rulesets to this stylesheet. Should usually only be called by StylesheetFactory.
     * TODO: where do we keep track of @media? In Ruleset?
     */
    void addRuleset(Ruleset r) {
        _rulesets.add(r);
    }

    /**
     * Set the imported stylesheet Rulesets to this stylesheet. Should usually only be called by StylesheetFactory.
     * TODO: where do we keep track of @media? In Ruleset?
     */
    void addRulesets(Stylesheet s) {
        for(Iterator i = s.getRulesets(); i.hasNext();) {
            Ruleset r = (Ruleset) i.next();
            _rulesets.add(r);
        }
    }
} // end class

/*
 * $Id$
 *
 * $Log$
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

