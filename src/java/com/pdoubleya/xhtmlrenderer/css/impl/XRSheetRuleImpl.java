/*
 * {{{ header & license
 * XRSheetRuleImpl.java
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
package com.pdoubleya.xhtmlrenderer.css.impl;

import java.util.*;
import org.w3c.dom.DOMException;

import org.w3c.dom.css.CSSFontFaceRule;
import org.w3c.dom.css.CSSPageRule;
import org.w3c.dom.css.CSSRule;
import org.w3c.dom.css.CSSRuleList;
import org.w3c.dom.css.CSSStyleDeclaration;
import org.w3c.dom.css.CSSStyleRule;
import org.w3c.dom.css.CSSStyleSheet;

import org.joshy.html.Context;

import com.pdoubleya.xhtmlrenderer.css.XRProperty;
import com.pdoubleya.xhtmlrenderer.css.XRRule;
import com.pdoubleya.xhtmlrenderer.css.XRSheetRule;
import com.pdoubleya.xhtmlrenderer.css.XRStyleSheet;


/**
 * An rule in CSS, either @ rule or style rule. Much of the basic rule-handling
 * (like property handling) is in here for use in subclasses. The class wraps a
 * DOM CSSRule, for example from a SAC parser.
 *
 * @author    Patrick Wright
 *
 */
public abstract class XRSheetRuleImpl implements XRSheetRule {

    /** The XRStyleSheet where the rule was found. */
    protected XRStyleSheet _sheet;

    /** The DOM representation of the rule. */
    protected CSSRule _domCSSRule;

    /** Map of XRProperties by property name */
    protected Map _xrPropertiesByName;

    /** Flag true if all the properties in this rule are !important */
    protected boolean _isImportant;

    /** Sequence in which the rule was found in stylesheet. */
    private int _sequence;


    /**
     * Constructor for the XRSheetRuleImpl object
     *
     * @param sheet        PARAM
     * @param domCSSRule   PARAM
     * @param sequence     PARAM
     * @param propNames    PARAM
     * @param isImportant  PARAM
     */
    public XRSheetRuleImpl( XRStyleSheet sheet, CSSRule domCSSRule, List propNames, int sequence, boolean isImportant ) {
        _sheet = sheet;
        _domCSSRule = domCSSRule;
        _sequence = sequence;
        _isImportant = isImportant;
        _xrPropertiesByName = new TreeMap();
        pullPropertiesFromDOMRule( propNames );
    }


    /**
     * Pulls CSSRules out of an XRStyleSheet and returns an Iterator of the
     * corresponding XRSheetRule for each
     *
     * @param styleSheet  PARAM
     * @return            Returns
     */
    public static Iterator fromCSSStyleSheet( XRStyleSheet styleSheet ) {
        ArrayList xrRules = new ArrayList();
        List props[];
        List reg;
        List imp = null;
        CSSRuleList rules = styleSheet.getCssRules();
        for ( int seq = 0, len = rules.getLength(); seq < len; seq++ ) {
            CSSRule rule = rules.item( seq );
            switch ( rule.getType() ) {
                case CSSRule.CHARSET_RULE:// fall thru
                case CSSRule.FONT_FACE_RULE:// fall thru
                case CSSRule.IMPORT_RULE:// fall thru
                case CSSRule.MEDIA_RULE:// fall thru
                case CSSRule.PAGE_RULE:
                    props = splitImportant( rule );
                    reg = (List)props[0];
                    imp = (List)props[1];
                    if ( reg.size() > 0 ) {
                        xrRules.add( new XRAtRuleImpl( styleSheet, rule, props[0], seq, false ) );
                    }

                    if ( imp.size() > 0 ) {
                        xrRules.add( new XRAtRuleImpl( styleSheet, rule, props[1], seq, true ) );
                    }
                    break;
                case CSSRule.STYLE_RULE:
                    CSSStyleRule styleRule = (CSSStyleRule)rule;
                    props = splitImportant( rule );
                    reg = (List)props[0];
                    imp = (List)props[1];

                    String selector = styleRule.getSelectorText();
                    String selectors[] = selector.split( "," );
                    for ( int i = 0, slen = selectors.length; i < slen; i++ ) {
                        if ( reg.size() > 0 ) {
                            xrRules.add( new XRStyleRuleImpl( styleSheet, rule, selectors[i].trim(), reg, seq, false ) );
                        }
                        if ( imp.size() > 0 ) {
                            xrRules.add( new XRStyleRuleImpl( styleSheet, rule, selectors[i].trim(), imp, seq, true ) );
                        }
                    }
                    break;
            }
        }
        return xrRules.iterator();
    }


    /**
     * Breaks the rule into two lists of properties, one regular List[0], one
     * important List[1]. Will be empty list if none found
     *
     * @param rule  PARAM
     * @return      Returns
     */
    private static List[] splitImportant( CSSRule rule ) {
        CSSStyleDeclaration decl = getStyleDeclaration( rule );
        List reg = new ArrayList();
        List imp = new ArrayList();
        for ( int seq = 0, len = decl.getLength(); seq < len; seq++ ) {
            String propName = decl.item( seq );
            boolean isimp = decl.getPropertyPriority( propName ).equals( "important" );
            if ( isimp ) {
                imp.add( propName );
            } else {
                reg.add( propName );
            }
        }
        return new List[]{reg, imp};
    }


    /**
     * Gets the styleDeclaration attribute of the XRSheetRuleImpl object
     *
     * @param rule  PARAM
     * @return      The styleDeclaration value
     */
    private static CSSStyleDeclaration getStyleDeclaration( CSSRule rule ) {
        CSSStyleDeclaration decl = null;
        switch ( rule.getType() ) {
            case CSSRule.CHARSET_RULE:// fall thru
            case CSSRule.IMPORT_RULE:// fall thru
            case CSSRule.MEDIA_RULE:// fall thru
                // these have no style declaration, nothing to do
                decl = null;
                break;
            case CSSRule.FONT_FACE_RULE:// fall thru
                decl = ( (CSSFontFaceRule)rule ).getStyle();
                break;
            case CSSRule.PAGE_RULE:// fall thru
                decl = ( (CSSPageRule)rule ).getStyle();
                break;
            case CSSRule.STYLE_RULE:// fall thru
                decl = ( (CSSStyleRule)rule ).getStyle();
                break;
        }
        return decl;
    }


    /**
     * Iterator over all XRProperties for this rule.
     *
     * @return   Returns
     */
    public Iterator listXRProperties() {
        return _xrPropertiesByName.values().iterator();
    }


    /**
     * Merges two XRRules, combining all properties. This is not used for
     * cascading, rather for two rules defined separately in the same sheet with
     * the same selector. Any properties with the same name in fromRuleSet will
     * replace existing properties with that name in this XRRule.
     *
     * @param fromRule  PARAM
     */
    public void mergeProperties( XRRule fromRule ) {
        Iterator iter = fromRule.listXRProperties();
        while ( iter.hasNext() ) {
            XRProperty prop = (XRProperty)iter.next();
            _xrPropertiesByName.put( prop.propertyName(), prop );
        }
    }


    /**
     * Sequence in which this rule was found in the stylesheet.
     *
     * @return   Returns
     */
    public int sequenceInStyleSheet() {
        return _sequence;
    }


    /**
     * Returns the XRProperty given its propertyname. Note this is independent
     * of cascade, inherit, etc--it is the property as specified.
     *
     * @param propName  PARAM
     * @param context   PARAM
     * @return          Returns
     */
    public XRProperty propertyByName( Context context, String propName ) {
        return (XRProperty)_xrPropertiesByName.get( propName );
    }


    /**
     * Returns true if the named property was defined and has a value in this
     * rule set.
     *
     * @param propName  PARAM
     * @return          Returns
     */
    public boolean hasProperty( String propName ) {
        return _xrPropertiesByName.get( propName ) != null;
    }


    /**
     * @param str               The new cssText value
     * @exception DOMException  Throws
     * @see                     org.w3c.dom.css.CSSRule
     */
    public void setCssText( String str )
        throws DOMException {
        _domCSSRule.setCssText( str );
    }


    /**
     * * Returns true if this rule is restricted to properties marked
     * "!important". Normally "!important" properties may be mixed freely with
     * non-important ones; however, in XR we separate these out to facilitate
     * the cascade logic, effectively making two rules with the same selector,
     * one important (higher priority) and one not.
     *
     * @return   The important value
     */
    public boolean isImportant() {
        return _isImportant;
    }


    /**
     * Gets the XRStyleSheet the rule belongs to
     *
     * @return   The styleSheet value
     */
    public XRStyleSheet getStyleSheet() {
        return _sheet;
    }


    /**
     * @return   The cssText value
     * @see      org.w3c.dom.css.CSSRule
     */
    public String getCssText() {
        return _domCSSRule.getCssText();
    }


    /**
     * @return   The parentRule value
     * @see      org.w3c.dom.css.CSSRule
     */
    public CSSRule getParentRule() {
        return _domCSSRule.getParentRule();
    }


    /**
     * @return   The parentStyleSheet value
     * @see      org.w3c.dom.css.CSSRule
     */
    public CSSStyleSheet getParentStyleSheet() {
        return _domCSSRule.getParentStyleSheet();
    }


    /**
     * @return   The type value
     * @see      org.w3c.dom.css.CSSRule
     */
    public short getType() {
        return _domCSSRule.getType();
    }


    /**
     * Loads XPProperties from the DOM CSSRule we are wrapping
     *
     * @param propNames  PARAM
     */
    private void pullPropertiesFromDOMRule( List propNames ) {
        CSSStyleDeclaration decl = null;
        switch ( _domCSSRule.getType() ) {
            case CSSRule.CHARSET_RULE:// fall thru
            case CSSRule.IMPORT_RULE:// fall thru
            case CSSRule.MEDIA_RULE:// fall thru
                // these have no style declaration, nothing to do
                return;
            case CSSRule.FONT_FACE_RULE:// fall thru
                decl = ( (CSSFontFaceRule)_domCSSRule ).getStyle();
                break;
            case CSSRule.PAGE_RULE:// fall thru
                decl = ( (CSSPageRule)_domCSSRule ).getStyle();
                break;
            case CSSRule.STYLE_RULE:// fall thru
                decl = ( (CSSStyleRule)_domCSSRule ).getStyle();
                break;
        }
        Iterator names = propNames.iterator();

        int seq = 0;
        while ( names.hasNext() ) {

            String propName = (String)names.next();
            Iterator iter = XRPropertyImpl.fromCSSPropertyDecl( _domCSSRule, decl, propName, seq++ );
            while ( iter.hasNext() ) {
                XRProperty xrProp = (XRPropertyImpl)iter.next();
                _xrPropertiesByName.put( xrProp.propertyName(), xrProp );
            }
        }
    }
}

