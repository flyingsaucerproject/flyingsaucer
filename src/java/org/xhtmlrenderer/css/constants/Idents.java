/*
 * {{{ header & license
 * Copyright (c) 2004, 2005 Patrick Wright
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
package org.xhtmlrenderer.css.constants;

import java.util.*;
import java.util.regex.*;
import org.xhtmlrenderer.util.XRRuntimeException;


/**
 * Booch utility class for working with ident values in CSS.
 *
 * @author   Patrick Wright
 */

// TODO: idents are also defined in Ident, but then need to decide whether lookup is useful or not; here we use strings (PWW 28-01-05)
// TODO: check idents list against CSS 2.1 spec (not 2.0 spec) (PWW 28-01-05)
public final class Idents {
    /*
     * Useful regexes to remember for later, from http://www.javapractices.com/Topic151.cjp
     * text
     * "^(\\S)(.){1,75}(\\S)$";
     * non-negative ints, incl
     * "(\\d){1,9}";
     * ints
     * "(-)?" + <non-negative ints>
     * non-negative floats, incl 0.0
     * "(\\d){1,10}\\.(\\d){1,10}";
     * floats
     * "(-)?" + <non-negative floats>;
     */
    /** Regex pattern, a CSS number--either integer or float  */
    private final static String RCSS_NUMBER = "(-)?((\\d){1,10}((\\.)(\\d){1,10})?)";
    /** Regex pattern, CSS lengths  */
    private final static String RCSS_LENGTH = "(" + RCSS_NUMBER + ")+" + "((em)|(ex)|(px)|(cm)|(mm)|(in)|(pt)|(pc)|(%))?";

    /** Pattern instance, for CSS lengths  */
    private final static Pattern CSS_NUMBER_PATTERN = Pattern.compile( RCSS_NUMBER );
    /** Pattern instance, for CSS lengths  */
    private final static Pattern CSS_LENGTH_PATTERN = Pattern.compile( RCSS_LENGTH );

    /** Pattern instance, for Hex-colors  */
    private final static Pattern COLOR_HEX_PATTERN = Pattern.compile( "#((((\\d)|[a-fA-F]){6})|(((\\d)|[a-fA-F]){3}))" );

    /** Description of the Field  */
    private final static Map COLOR_MAP;
    /** Description of the Field  */
    private final static Map FONT_SIZES;
    /** Description of the Field  */
    private final static Map FONT_WEIGHTS;
    /** Description of the Field  */
    private final static Map BORDER_WIDTHS;
    /** Description of the Field  */
    private final static Map BACKGROUND_POSITIONS;
    /** Description of the Field  */
    private final static List BACKGROUND_REPEATS;
    /** Description of the Field  */
    private final static List BORDER_STYLES;
    /** Description of the Field  */
    private final static List LIST_TYPES;
    /** Description of the Field  */
    private final static List FONT_STYLES;

    /** Description of the Field */
    private final static List BACKGROUND_POSITIONS_IDENTS;

    /**
     * Description of the Method
     *
     * @param cssName  PARAM
     * @param ident    PARAM
     * @return         Returns
     */
    public static String convertIdent( CSSName cssName, String ident ) {
        String val = ident;

        if ( cssName == CSSName.FONT_SIZE ) {
            String size = (String)FONT_SIZES.get( ident );
            val = ( size == null ? ident : size );
        } else if ( cssName == CSSName.FONT_WEIGHT ) {
            String size = (String)FONT_WEIGHTS.get( ident );
            val = ( size == null ? ident : size );
        } else if ( cssName == CSSName.BACKGROUND_POSITION ) {
            String pos = (String)BACKGROUND_POSITIONS.get( ident );
            val = ( pos == null ? ident : pos );
        } else if (
                cssName == CSSName.BORDER_WIDTH_BOTTOM ||
                cssName == CSSName.BORDER_WIDTH_LEFT ||
                cssName == CSSName.BORDER_WIDTH_RIGHT ||
                cssName == CSSName.BORDER_WIDTH_SHORTHAND ||
                cssName == CSSName.BORDER_WIDTH_TOP ) {

            String size = (String)BORDER_WIDTHS.get( ident );
            val = ( size == null ? ident : size );
        } else if (
                cssName == CSSName.BORDER_COLOR_BOTTOM ||
                cssName == CSSName.BORDER_COLOR_LEFT ||
                cssName == CSSName.BORDER_COLOR_RIGHT ||
                cssName == CSSName.BORDER_COLOR_SHORTHAND ||
                cssName == CSSName.BORDER_COLOR_TOP ||
                cssName == CSSName.BACKGROUND_COLOR ||
                cssName == CSSName.COLOR ||
                cssName == CSSName.OUTLINE_COLOR ) {

            val = getColorHex( ident );

        }
        return val;
    }

    /**
     * Description of the Method
     *
     * @param val  PARAM
     * @return     Returns
     */
    public static boolean looksLikeABorderStyle( String val ) {
        return BORDER_STYLES.contains( val );
    }


    /**
     * Description of the Method
     *
     * @param val  PARAM
     * @return     Returns
     */
    public static boolean looksLikeAColor( String val ) {
        return COLOR_MAP.get( val ) != null || ( val.startsWith( "#" ) && ( val.length() == 7 || val.length() == 4 ) ) || val.startsWith( "rgb" );
    }

    /**
     * Description of the Method
     *
     * @param val  PARAM
     * @return     Returns
     */
    public static boolean looksLikeALength( String val ) {
        return CSS_LENGTH_PATTERN.matcher( val ).matches();
    }

    /**
     * Description of the Method
     *
     * @param val  PARAM
     * @return     Returns
     */
    public static boolean looksLikeAURI( String val ) {
        return val.startsWith( "url(" ) && val.endsWith( ")" );
    }

    /**
     * Description of the Method
     *
     * @param val  PARAM
     * @return     Returns
     */
    public static boolean looksLikeABGRepeat( String val ) {
        return BACKGROUND_REPEATS.indexOf( val ) >= 0;
    }

    /**
     * Description of the Method
     *
     * @param val  PARAM
     * @return     Returns
     */
    public static boolean looksLikeABGAttachment( String val ) {
        return "scroll".equals( val ) || "fixed".equals( val );
    }

    /**
     * Description of the Method
     *
     * @param val  PARAM
     * @return     Returns
     */
    public static boolean looksLikeABGPosition( String val ) {
        return BACKGROUND_POSITIONS_IDENTS.contains( val ) || looksLikeALength( val );
    }

    /**
     * Description of the Method
     *
     * @param val  PARAM
     * @return     Returns
     */
    public static boolean looksLikeAListStyleType( String val ) {
        return LIST_TYPES.indexOf( val ) >= 0;
    }

    /**
     * Description of the Method
     *
     * @param val  PARAM
     * @return     Returns
     */
    public static boolean looksLikeAListStyleImage( String val ) {
        return "none".equals( val ) || looksLikeAURI( val );
    }

    /**
     * Description of the Method
     *
     * @param val  PARAM
     * @return     Returns
     */
    public static boolean looksLikeAListStylePosition( String val ) {
        return "inside".equals( val ) || "outside".equals( val );
    }

    /**
     * Description of the Method
     *
     * @param val  PARAM
     * @return     Returns
     */
    public static boolean looksLikeAFontStyle( String val ) {
        return FONT_STYLES.indexOf( val ) >= 0;
    }

    /**
     * Description of the Method
     *
     * @param val  PARAM
     * @return     Returns
     */
    public static boolean looksLikeAFontVariant( String val ) {
        return "normal".equals( val ) || "small-caps".equals( val );
    }

    /**
     * Description of the Method
     *
     * @param val  PARAM
     * @return     Returns
     */
    public static boolean looksLikeAFontWeight( String val ) {
        return FONT_WEIGHTS.get( val ) != null;
    }

    /**
     * Description of the Method
     *
     * @param val  PARAM
     * @return     Returns
     */
    public static boolean looksLikeAFontSize( String val ) {
        // TODO
        return FONT_SIZES.get( val ) != null ||
                looksLikeALength( val ) ||
                "larger".equals( val ) || "smaller".equals( val );
    }

    /**
     * Description of the Method
     *
     * @param val  PARAM
     * @return     Returns
     */
    public static boolean looksLikeALineHeight( String val ) {
        return "normal".equals( val ) || looksLikeALength( val );
    }

    /**
     * Given a String, returns either the rgb declaration for the color, or the
     * hex declaration; used to cleanup assignments like "red" or "green".
     *
     * @param value  A String which contains a Color identifier, an rgb
     *      assignment or a Color hex value.
     * @return       The colorHex value
     */
    public static String getColorHex( String value ) {
        if ( value == null ) {
            throw new XRRuntimeException( "value is null on getColorHex()" );
        }
        String retval = (String)COLOR_MAP.get( value.toLowerCase() );
        if ( retval == null ) {
            if ( value.indexOf( "rgb" ) >= 0 ) {
                retval = value;
            } else {
                Matcher m = COLOR_HEX_PATTERN.matcher( value );
                if ( m.matches() ) {
                    retval = value;
                }
            }
        }
        if ( retval == null ) {
            System.out.println( "!!! can't identify specified color:" + value );
        }
        return retval;
    }

    static {
        COLOR_MAP = new HashMap();
        COLOR_MAP.put( "black", "#000000" );
        COLOR_MAP.put( "white", "#FFFFFF" );
        COLOR_MAP.put( "red", "#FF0000" );
        COLOR_MAP.put( "yellow", "#FFFF00" );
        COLOR_MAP.put( "lime", "#00ff00" );
        COLOR_MAP.put( "aqua", "#00ffff" );
        COLOR_MAP.put( "blue", "#0000ff" );
        COLOR_MAP.put( "fuchsia", "#ff00ff" );
        COLOR_MAP.put( "gray", "#808080" );
        COLOR_MAP.put( "silver", "#c0c0c0" );
        COLOR_MAP.put( "maroon", "#800000" );
        COLOR_MAP.put( "olive", "#808000" );
        COLOR_MAP.put( "green", "#008000" );
        COLOR_MAP.put( "teal", "#008080" );
        COLOR_MAP.put( "navy", "#000080" );
        COLOR_MAP.put( "purple", "#800080" );
        COLOR_MAP.put( "transparent", "transparent" );

        FONT_SIZES = new HashMap();
        FONT_SIZES.put( "xx-small", "6.9pt" );
        FONT_SIZES.put( "x-small", "8.3pt" );
        FONT_SIZES.put( "small", "10pt" );
        FONT_SIZES.put( "medium", "12pt" );
        FONT_SIZES.put( "large", "14.4pt" );
        FONT_SIZES.put( "x-large", "17.3pt" );
        FONT_SIZES.put( "xx-large", "20.7pt" );

        FONT_WEIGHTS = new HashMap();
        FONT_WEIGHTS.put( "normal", "400" );
        FONT_WEIGHTS.put( "bold", "700" );
        FONT_WEIGHTS.put( "100", "100" );
        FONT_WEIGHTS.put( "200", "200" );
        FONT_WEIGHTS.put( "300", "300" );
        FONT_WEIGHTS.put( "400", "400" );
        FONT_WEIGHTS.put( "500", "500" );
        FONT_WEIGHTS.put( "600", "600" );
        FONT_WEIGHTS.put( "700", "700" );
        FONT_WEIGHTS.put( "800", "800" );
        FONT_WEIGHTS.put( "900", "900" );
        FONT_WEIGHTS.put( "bolder", "bolder" );
        FONT_WEIGHTS.put( "lighter", "lighter" );
        // NOTE: 'bolder' and 'lighter' need to be handled programmatically

        BORDER_WIDTHS = new HashMap();
        BORDER_WIDTHS.put( "thin", "1px" );
        BORDER_WIDTHS.put( "medium", "2px" );
        BORDER_WIDTHS.put( "thick", "3px" );

        BACKGROUND_POSITIONS_IDENTS = new ArrayList();
        BACKGROUND_POSITIONS_IDENTS.add( "top" );
        BACKGROUND_POSITIONS_IDENTS.add( "center" );
        BACKGROUND_POSITIONS_IDENTS.add( "bottom" );
        BACKGROUND_POSITIONS_IDENTS.add( "right" );
        BACKGROUND_POSITIONS_IDENTS.add( "left" );
        BACKGROUND_POSITIONS = new HashMap();

        // NOTE: combinations of idents for background-positions, are specified in the CSS
        // spec; some are disallowed, for example, there is no "top" all by itself. Check
        // the CSS spec for background (shorthand) or background-position for a complete list.
        // The percentages specified here are from that section of the spec.
        BACKGROUND_POSITIONS.put( "top left", "0% 0%" );
        BACKGROUND_POSITIONS.put( "left top", "0% 0%" );

        BACKGROUND_POSITIONS.put( "top center", "50% 0%" );
        BACKGROUND_POSITIONS.put( "center top", "50% 0%" );

        BACKGROUND_POSITIONS.put( "right top", "100% 0%" );
        BACKGROUND_POSITIONS.put( "top right", "100% 0%" );

        BACKGROUND_POSITIONS.put( "left center", "0% 50%" );
        BACKGROUND_POSITIONS.put( "center left", "0% 50%" );

        BACKGROUND_POSITIONS.put( "center", "50% 50%" );
        BACKGROUND_POSITIONS.put( "center center", "50% 50%" );

        BACKGROUND_POSITIONS.put( "right center", "100% 50%" );
        BACKGROUND_POSITIONS.put( "center right", "100% 50%" );

        BACKGROUND_POSITIONS.put( "bottom left", "0% 100%" );
        BACKGROUND_POSITIONS.put( "left bottom", "0% 100%" );

        BACKGROUND_POSITIONS.put( "bottom center", "50% 100%" );
        BACKGROUND_POSITIONS.put( "center bottom", "50% 100%" );

        BACKGROUND_POSITIONS.put( "bottom right", "100% 100%" );
        BACKGROUND_POSITIONS.put( "right bottom", "100% 100%" );

        BACKGROUND_REPEATS = new ArrayList();
        BACKGROUND_REPEATS.add( "repeat" );
        BACKGROUND_REPEATS.add( "repeat-x" );
        BACKGROUND_REPEATS.add( "repeat-y" );
        BACKGROUND_REPEATS.add( "no-repeat" );

        BORDER_STYLES = new ArrayList();
        BORDER_STYLES.add( "none" );
        BORDER_STYLES.add( "hidden" );
        BORDER_STYLES.add( "dotted" );
        BORDER_STYLES.add( "dashed" );
        BORDER_STYLES.add( "solid" );
        BORDER_STYLES.add( "double" );
        BORDER_STYLES.add( "groove" );
        BORDER_STYLES.add( "ridge" );
        BORDER_STYLES.add( "inset" );
        BORDER_STYLES.add( "outset" );

        LIST_TYPES = new ArrayList();
        LIST_TYPES.add( "disc" );
        LIST_TYPES.add( "circle" );
        LIST_TYPES.add( "square" );
        LIST_TYPES.add( "decimal" );
        LIST_TYPES.add( "decimal-leading-zero" );
        LIST_TYPES.add( "lower-roman" );
        LIST_TYPES.add( "upper-roman" );
        LIST_TYPES.add( "lower-greek" );
        LIST_TYPES.add( "lower-alpha" );
        LIST_TYPES.add( "lower-latin" );
        LIST_TYPES.add( "upper-alpha" );
        LIST_TYPES.add( "upper-latin" );
        LIST_TYPES.add( "hebrew" );
        LIST_TYPES.add( "armenian" );
        LIST_TYPES.add( "georgian" );
        LIST_TYPES.add( "cjk-ideographic" );
        LIST_TYPES.add( "hiragana" );
        LIST_TYPES.add( "katakana" );
        LIST_TYPES.add( "hiragana-iroha" );
        LIST_TYPES.add( "katakana-iroha" );
        LIST_TYPES.add( "none" );

        FONT_STYLES = new ArrayList();
        FONT_STYLES.add( "normal" );
        FONT_STYLES.add( "italic" );
        FONT_STYLES.add( "oblique" );

    }// end static
}// end class

/*
 * $Id$
 *
 * $Log$
 * Revision 1.4  2005/01/29 20:21:09  pdoubleya
 * Clean/reformat code. Removed commented blocks, checked copyright.
 *
 * Revision 1.3  2005/01/29 12:17:18  pdoubleya
 * .
 *
 * Revision 1.2  2005/01/24 19:01:07  pdoubleya
 * Mass checkin. Changed to use references to CSSName, which now has a Singleton instance for each property, everywhere property names were being used before. Removed commented code. Cascaded and Calculated style now store properties in arrays rather than maps, for optimization.
 *
 * Revision 1.1  2005/01/24 14:27:51  pdoubleya
 * Added to CVS.
 *
 *
 */

