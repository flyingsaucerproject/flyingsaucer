/*
 * {{{ header & license
 * XRRule.java
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


/**
 * Interface for CSS rules (@ rules, style rules). An XRRule is also a DOM
 * CSSRule. Rules have a sequence which is the order they were found within
 * their stylesheet.
 *
 * @author Patrick Wright
 */
public interface XRRule {
// --Commented out by Inspection START (2005-01-05 00:30):
//    /**
//     * An iterator of all XRProperties in this rule.
//     *
//     * @return Returns
//     */
//    Iterator listXRProperties();
// --Commented out by Inspection STOP (2005-01-05 00:30)


// --Commented out by Inspection START (2005-01-05 00:31):
//    /**
//     * Value of a single XRProperty, by name.
//     *
//     * @param context  PARAM
//     * @param propName PARAM
//     * @return Returns
//     */
//    XRProperty propertyByName(Context context, String propName);
// --Commented out by Inspection STOP (2005-01-05 00:31)


// --Commented out by Inspection START (2005-01-05 00:30):
//    /**
//     * Returns true if the named property was defined and has a value in this
//     * rule set.
//     *
//     * @param propName PARAM
//     * @return Returns
//     */
//    boolean hasProperty(String propName);
// --Commented out by Inspection STOP (2005-01-05 00:30)


// --Commented out by Inspection START (2005-01-05 00:30):
//    /**
//     * Merges two XRRules, combining all properties. This is not used for
//     * cascading, rather for two rules defined separately in the same sheet with
//     * the same selector. Any properties with the same name in fromRuleSet will
//     * replace existing properties with that name in this XRRule.
//     *
//     * @param fromRuleSet PARAM
//     */
//    void mergeProperties(XRRule fromRuleSet);
// --Commented out by Inspection STOP (2005-01-05 00:30)
}

/*
 * $Id$
 *
 * $Log$
 * Revision 1.4  2005/01/05 01:10:13  tobega
 * Went wild with code analysis tool. removed unused stuff. Lucky we have CVS...
 *
 * Revision 1.3  2004/12/29 10:39:27  tobega
 * Separated current state Context into ContextImpl and the rest into SharedContext.
 *
 * Revision 1.2  2004/10/23 13:03:46  pdoubleya
 * Re-formatted using JavaStyle tool.
 * Cleaned imports to resolve wildcards except for common packages (java.io, java.util, etc)
 * Added CVS log comments at bottom.
 *
 *
 */

