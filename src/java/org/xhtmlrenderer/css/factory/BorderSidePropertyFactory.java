/*
 * {{{ header & license
 * BorderSidePropertyFactory.java
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
package org.xhtmlrenderer.css.factory;

import java.util.*;
import org.w3c.dom.css.CSSPrimitiveValue;
import org.w3c.dom.css.CSSStyleDeclaration;
import org.w3c.dom.css.CSSValue;
import org.w3c.dom.css.CSSValueList;

import org.xhtmlrenderer.css.RuleNormalizer;

import org.xhtmlrenderer.css.constants.CSSName;
import org.xhtmlrenderer.css.impl.XRValueImpl;


/**
 * A PropertyFactory for CSS 2 "border" shorthand properties that are border side specific (top, bottom, left, right), instantiating XRProperties.
 *
 * @author    Patrick Wright
 *
 */
public class BorderSidePropertyFactory extends AbstractPropertyFactory {
    /** Singleton instance. */
    private static BorderSidePropertyFactory _instance;

    /** TODO--used in property explosion */
    private static final Map PROP_EXPLODE;

    private static final int WIDTH_IDX;
    private static final int STYLE_IDX;
    private static final int COLOR_IDX;
    
    /** Constructor for the BorderSidePropertyFactory object */
    private BorderSidePropertyFactory() { }


    /**
     * Returns the singleton instance.
     *
     * @return   Returns
     */
    public static synchronized PropertyFactory instance() {
        if ( _instance == null ) {
            _instance = new BorderSidePropertyFactory();
        }
        return _instance;
    }

    // thread-safe
    /**
     * If <code>propName</code> describes a shorthand property, explodes it into
     * the specific properties it is a shorthand for, and returns those as an
     * Iterator of {@link org.xhtmlrenderer.css.XRProperty} instances;
     * or just instantiates a single <code>XRProperty</code> for non-shorthand
     * props.
     *
     * @param style     The CSSStyleDeclaration from the SAC parser.
     * @param propName  The String property name for the property to explode.
     * @param sequence  Sequence in which the declaration was found in the
     *      containing stylesheet.
     * @return          Iterator of one or more XRProperty instances representing the exploded values.
     */
    public Iterator explodeProperties( CSSStyleDeclaration style, String propName, int sequence ) {
        List list = new ArrayList();
        CSSValue cssValue = style.getPropertyCSSValue( propName );
        String priority = style.getPropertyPriority( propName );

        List explodes = (List)PROP_EXPLODE.get( propName );
        if ( explodes == null ) {
            // TODO: error
            return list.iterator();
        }

        // CAREFUL: note that with steadyState parser impl, their value class impl
        // both primitive and value list interfaces! use getCssValueType(), not instanceof!!
        if ( cssValue.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE ) {
            addPrimitive(style, (CSSPrimitiveValue)cssValue, priority, sequence, explodes, list);
        } else {
            // is a value list
            // border explodes differently based on number of supplied values
            // but values for color, style, width can be given in any order
            // loop over the ones given and sniff them out to see if they 
            // look like color, style, width, then apply to all four sides 

            CSSValueList vList = (CSSValueList)cssValue;
            CSSPrimitiveValue primitive = null;

            for ( int i = 0, len = vList.getLength(); i < len; i++ ) {
                primitive = (CSSPrimitiveValue)vList.item( i );
                addPrimitive(style, primitive, priority, sequence, explodes, list);
            }            
        }
        return list.iterator();
    }

    private void addPrimitive(
        CSSStyleDeclaration style,
        CSSPrimitiveValue primitive, 
        String priority,
        int sequence,
        List explodeTo,
        List list) {
            
        String val = primitive.getCssText().trim();
        String propName = "";
        if ( RuleNormalizer.looksLikeAColor( val ) ) {
            propName = (String)explodeTo.get( COLOR_IDX );
        } else if ( RuleNormalizer.looksLikeABorderStyle( val ) ) {
            propName = (String)explodeTo.get( STYLE_IDX );
        } else {
            // HACK: as with background, we should go ahead and check this is a valid CSS length value (PWW 24-08-04)
            propName = (String)explodeTo.get( WIDTH_IDX );
        }
        list.add( 
            newProperty( 
                propName, 
                primitive, 
                priority, 
                style, 
                sequence ) 
            );
    }    

    static {
        WIDTH_IDX = 0;
        STYLE_IDX = 1;
        COLOR_IDX = 2;

        PROP_EXPLODE = new HashMap();
        List l = new ArrayList();
        l.add( CSSName.BORDER_WIDTH_TOP );
        l.add( CSSName.BORDER_STYLE_TOP );
        l.add( CSSName.BORDER_COLOR_TOP );
        PROP_EXPLODE.put( CSSName.BORDER_TOP_SHORTHAND, l );

        l = new ArrayList();
        l.add( CSSName.BORDER_WIDTH_RIGHT );
        l.add( CSSName.BORDER_STYLE_RIGHT );
        l.add( CSSName.BORDER_COLOR_RIGHT );
        PROP_EXPLODE.put( CSSName.BORDER_RIGHT_SHORTHAND, l );

        l = new ArrayList();
        l.add( CSSName.BORDER_WIDTH_BOTTOM );
        l.add( CSSName.BORDER_STYLE_BOTTOM );
        l.add( CSSName.BORDER_COLOR_BOTTOM );
        PROP_EXPLODE.put( CSSName.BORDER_BOTTOM_SHORTHAND, l );

        l = new ArrayList();
        l.add( CSSName.BORDER_WIDTH_LEFT );
        l.add( CSSName.BORDER_STYLE_LEFT );
        l.add( CSSName.BORDER_COLOR_LEFT );
        PROP_EXPLODE.put( CSSName.BORDER_LEFT_SHORTHAND, l );
    }
}

