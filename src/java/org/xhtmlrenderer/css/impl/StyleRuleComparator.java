/*
 * {{{ header & license
 * StyleRuleComparator.java
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
package org.xhtmlrenderer.css.impl;

import java.util.*;
import org.xhtmlrenderer.css.XRStyleRule;
import org.xhtmlrenderer.css.XRStyleSheet;


/**
 * The idea, and first implementation of this, is from Scott Sauyet, adapted for
 * this class by Patrick Wright.</p>
 *
 * @author   Patrick Wright
 * @author   Scott Sauyet
 */
public class StyleRuleComparator implements Comparator {
    /**
     * <p>
     *
     * Implementation of Comparator.compare(), for the purpose of sorting rules
     * in determining cascade/inherit precedence. Specifically, this comparison
     * will result in instance o1 falling before o2 if</p>
     * <ul>
     *   <li> o1 derives from a stylesheet with lower precedence--e.g. user
     *   agent sheet has lower precedence than user sheet</li>
     *   <li> o1 came from the same sheet as o2, but preceded it in that sheet
     *   (sequentially)</li>
     *   <li> o1 and o2 were originally the same rule, but o2 includes one or
     *   more properties marked "important" in the rule</li> <p>
     *
     *
     *
     * @param o1  PARAM
     * @param o2  PARAM
     * @return    a negative integer, zero, or a positive integer as the first
     *      argument is less than, equal to, or greater than the second.
     */
    public int compare( Object o1, Object o2 ) {
        if ( o1 == o2 ) {
            return 0;
        }
        if ( !( o1 instanceof XRStyleRule && o2 instanceof XRStyleRule ) ) {
            throw new IllegalArgumentException(
                    "Compares only XRStyleRule" );
        }
        XRStyleRule rule1 = (XRStyleRule)o1;
        XRStyleRule rule2 = (XRStyleRule)o2;

        int level1 = getLevel( rule1 );
        int level2 = getLevel( rule2 );

        /*
         * ASK: what do we do now with sheet loading sequence?
         * int sseq1 = rule1.getStyleSheet().sequence();
         * int sseq2 = rule2.getStyleSheet().sequence();
         */
        if ( level1 != level2 ) {
            return level1 - level2;
        }

        int specificity1 = rule1.selectorSpecificity();
        int specificity2 = rule2.selectorSpecificity();

        int difference = specificity1 - specificity2;

        if ( difference != 0 ) {
            return difference;
        }

        return rule1.sequenceInStyleSheet() - rule2.sequenceInStyleSheet();
    }


    /*
     * Scott's original proposal is that we track, explicitly, which stylesheets are user/agent/author.
     * However, we have to track sequence in which stylesheets are loaded in any case (for determining precedence of
     * imported sheets/rules), so current proposal is to use this sequence to also indicate user/agent/author origin.
     *
     * Original: (Scott, from XR mail list)
     */
    /**
     * Gets the level attribute of the XRStyleRuleImpl class
     *
     * @param rule  PARAM
     * @return      The level value
     */
    private int getLevel( XRStyleRule rule ) {

        int origin = rule.getStyleSheet().origin();
        if ( origin == XRStyleSheet.USER_AGENT ) {
            return 100;
        }
        if ( origin == XRStyleSheet.USER ) {
            if ( rule.isImportant() ) {
                return 500;
            } else {
                return 200;
            }
        }
        if ( origin == XRStyleSheet.AUTHOR ) {
            if ( rule.isImportant() ) {
                return 400;
            } else {
                return 300;
            }
        }
        return -1;// n/a if the rule is properly initialized.
    }
}

/*
 * $Id$
 *
 * $Log$
 * Revision 1.2  2004/10/23 13:21:14  pdoubleya
 * Re-formatted using JavaStyle tool.
 * Cleaned imports to resolve wildcards except for common packages (java.io, java.util, etc).
 * Added CVS log comments at bottom.
 *
 *
 */

