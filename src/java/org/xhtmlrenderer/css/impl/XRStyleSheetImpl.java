/*
 * {{{ header & license
 * XRStyleSheetImpl.java
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
import org.w3c.dom.DOMException;

import org.w3c.dom.Node;
import org.w3c.dom.css.CSSRule;
import org.w3c.dom.css.CSSRuleList;
import org.w3c.dom.css.CSSStyleSheet;
import org.w3c.dom.stylesheets.MediaList;
import org.w3c.dom.stylesheets.StyleSheet;

import org.xhtmlrenderer.css.XRSheetRule;
import org.xhtmlrenderer.css.XRStyleRule;
import org.xhtmlrenderer.css.XRStyleSheet;



/**
 * Represents a stylesheet...see interface comments.
 *
 * @author    Patrick Wright
 *
 */
public class XRStyleSheetImpl implements XRStyleSheet {
    /** origin of the sheet */
    private int _origin;

    /**
     * Sequence in which style sheet was read, but also priority for sheet
     * properties relative to other sheets
     */
    private int _sequence;

    /** The DOM representation of the sheet (from SAC parser, for example) */
    private CSSStyleSheet _domSheet;

    /** The XRSheetRules in this sheet. */
    private List _rules;

    /** XRStyleRules, by selector--doesn't include @ rules */
    private Map _rulesBySelector;


    /**
     * Constructor for the XRStyleSheetImpl object. For a description of
     * sequence, see comments on XRStyleSheet interface.
     *
     * @param sheet   PARAM
     * @param origin  PARAM
     * @param seq     PARAM
     */
    public XRStyleSheetImpl( CSSStyleSheet sheet, int origin, int seq ) {
        this();
        _origin = origin;
        _sequence = seq;
        setDOMStyleSheet( sheet );
        pullRulesFromDOMStyleSheet();
    }


    /** Constructor for the XRStyleSheetImpl object */
    private XRStyleSheetImpl() {
        _rules = new ArrayList();
        _rulesBySelector = new TreeMap();
    }


    /**
     * Convenience, creates a new XRStyleSheet instance for a user agent sheet.
     *
     * @param sheet  PARAM
     * @param seq    PARAM
     * @return       Returns
     */
    public static XRStyleSheet newUserAgentStyleSheet( CSSStyleSheet sheet, int seq ) {
        return new XRStyleSheetImpl( sheet, XRStyleSheet.USER_AGENT, seq );
    }


    /**
     * Convenience, creates a new XRStyleSheet instance for an author sheet.
     *
     * @param sheet  PARAM
     * @param seq    PARAM
     * @return       Returns
     */
    public static XRStyleSheet newAuthorStyleSheet( CSSStyleSheet sheet, int seq ) {
        return new XRStyleSheetImpl( sheet, XRStyleSheet.AUTHOR, seq );
    }


    /**
     * Convenience, creates a new XRStyleSheet instance for a user sheet.
     *
     * @param sheet  PARAM
     * @param seq    PARAM
     * @return       Returns
     */
    public static XRStyleSheet newUserStyleSheet( CSSStyleSheet sheet, int seq ) {
        return new XRStyleSheetImpl( sheet, XRStyleSheet.USER, seq );
    }


    /**
     * Iterator over all XRSheetRules in the sheet.
     *
     * @return   Returns
     */
    public Iterator styleRules() {
        List r = new ArrayList();
        Iterator iter = _rulesBySelector.values().iterator();
        while ( iter.hasNext() ) {
            XRSheetRule rule = (XRSheetRule)iter.next();
            if ( rule instanceof XRStyleRule ) {
                r.add( rule );
            }
        }
        return r.iterator();
    }


    /**
     * The origin of the stylesheet.
     *
     * @return   Returns
     */
    public int origin() {
        return _origin;
    }


    /**
     * Simple label for stylesheet origin--USER-AGENT, AUTHOR, USER
     *
     * @return   Returns
     */
    public String orginLabel() {
        switch ( _origin ) {
            case USER_AGENT:
                return "USER_AGENT";
            case AUTHOR:
                return "AUTHOR";
            case USER:
                return "USER";
            default:
                return "UNKNOWN";
        }
    }


    /**
     * The priority sequence for the stylesheet. See class comments.
     *
     * @return   Returns
     */
    public int sequence() {
        return _sequence;
    }


    /**
     * Removes a rule from the stylesheet.
     *
     * @param param             PARAM
     * @exception DOMException  Throws
     * @see                     org.w3c.dom.css.CSSStyleSheet
     */
    public void deleteRule( int param )
        throws DOMException {
        _domSheet.deleteRule( param );
    }


    /**
     * Adds a rule to the stylesheet.
     *
     * @param str               PARAM
     * @param param             PARAM
     * @return                  Returns
     * @exception DOMException  Throws
     * @see                     org.w3c.dom.css.CSSStyleSheet
     */
    public int insertRule( String str, int param )
        throws DOMException {
        return _domSheet.insertRule( str, param );
    }


    /**
     * Appends a description of the style sheet to a StringBuffer
     *
     * @param sb  PARAM
     */
    public void dump( StringBuffer sb ) {
        sb.append( "Sheet: " + getHref() + " (" + sequence() + ") \n" );
        Iterator iter = _rulesBySelector.values().iterator();
        while ( iter.hasNext() ) {
            sb.append( iter.next().toString() );
        }
    }


    /**
     * Disabled a sheet...?
     *
     * @param param  The new disabled value
     * @see          org.w3c.dom.css.CSSStyleSheet
     */
    public void setDisabled( boolean param ) {
        _domSheet.setDisabled( param );
    }


    /**
     * Returns the CSSRuleList associated with the sheet--these are DOM rules,
     * not XRSheetRules!
     *
     * @return   The cssRules value
     * @see      org.w3c.dom.css.CSSStyleSheet
     */
    public CSSRuleList getCssRules() {
        return _domSheet.getCssRules();
    }


    /**
     * @return   The disabled value
     * @see      org.w3c.dom.css.CSSStyleSheet
     */
    public boolean getDisabled() {
        return _domSheet.getDisabled();
    }


    /**
     * The URL identifying this sheet
     *
     * @return   The href value
     * @see      org.w3c.dom.css.CSSStyleSheet
     */
    public String getHref() {
        return _domSheet.getHref();
    }


    /**
     * The MediaList for the media to which this sheet applies
     *
     * @return   The media value
     * @see      org.w3c.dom.css.CSSStyleSheet
     */
    public MediaList getMedia() {
        return _domSheet.getMedia();
    }


    /**
     * @return   The ownerNode value
     * @see      org.w3c.dom.css.CSSStyleSheet
     */
    public Node getOwnerNode() {
        return _domSheet.getOwnerNode();
    }


    /**
     * @return   The ownerRule value
     * @see      org.w3c.dom.css.CSSStyleSheet
     */
    public CSSRule getOwnerRule() {
        return _domSheet.getOwnerRule();
    }


    /**
     * @return   The parentStyleSheet value
     * @see      org.w3c.dom.css.CSSStyleSheet
     */
    public StyleSheet getParentStyleSheet() {
        return _domSheet.getParentStyleSheet();
    }


    /**
     * @return   The title value
     * @see      org.w3c.dom.css.CSSStyleSheet
     */
    public String getTitle() {
        return _domSheet.getTitle();
    }


    /**
     * @return   The type value
     * @see      org.w3c.dom.css.CSSStyleSheet
     */
    public String getType() {
        return _domSheet.getType();
    }


    /**
     * Returns an XRStyleRule by its selector text. Note that multiple-value
     * selectors ("H1, H2") are split into individual rules with their own
     * single-value selector.
     *
     * @param selector   PARAM
     * @param important  PARAM
     * @return           Returns
     */
    // CLEAN: this was a public method for awhile...seemed convenient during testing, but have removed it
    // from the interface as it was no longer used...(PWW 15/08/04)
    private XRStyleRule ruleBySelector( String selector, boolean important ) {
        return (XRStyleRule)_rulesBySelector.get( selectorImportantKey( selector, important ) );
    }


    /**
     * Returns a string key including both the selector and its important
     * property, usable for keying into a Map
     *
     * @param selector   PARAM
     * @param important  PARAM
     * @return           Returns
     */
    private String selectorImportantKey( String selector, boolean important ) {
        return selector + "--(important: " + important + ")";
    }


    /**
     * Extracts the CSSRuleList from the stylesheet and converts (wraps) them in
     * XRSheetRules, adding them to our internal lists and maps.
     */
    private void pullRulesFromDOMStyleSheet() {
        Iterator iter = XRSheetRuleImpl.fromCSSStyleSheet( this );
        while ( iter.hasNext() ) {
            XRSheetRule rule = (XRSheetRule)iter.next();
            if ( rule instanceof XRStyleRule ) {
                XRStyleRule srule = (XRStyleRule)rule;
                String key = selectorImportantKey( srule.cssSelectorText(), srule.isImportant() );
                XRStyleRule prevRule =
                        (XRStyleRule)_rulesBySelector.get( key );
                if ( prevRule == null ||
                        ( prevRule != null && prevRule.isImportant() != srule.isImportant() ) ) {
                    _rulesBySelector.put( key, srule );
                } else {
                    prevRule.mergeProperties( srule );
                }
            }
        }
    }


    /**
     * @param sheet  The new dOMStyleSheet value
     * @see          org.w3c.dom.css.CSSStyleSheet
     */
    private void setDOMStyleSheet( CSSStyleSheet sheet ) {
        _domSheet = sheet;
    }
}

