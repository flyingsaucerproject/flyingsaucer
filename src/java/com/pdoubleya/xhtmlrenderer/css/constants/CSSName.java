/*
 * {{{ header & license
 * CSSName.java
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
package com.pdoubleya.xhtmlrenderer.css.constants;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import java.util.*;


/**
 * Used for CSS2-related constants
 *
 * @author    Patrick Wright
 *
 */
public final class CSSName {

    /** Constant string for CSS2 property. */
    public final static String COLOR = "color";

    /** Constant string for CSS2 property. */
    public final static String BACKGROUND_SHORTHAND = "background";// TODO: shorthand

    /** Constant string for CSS2 property. */
    public final static String BACKGROUND_COLOR = "background-color";// TODO: just added, handle property

    /** Constant string for CSS2 property. */
    public final static String BACKGROUND_IMAGE = "background-image";// TODO: just added, handle property

    /** Constant string for CSS2 property. */
    public final static String BACKGROUND_REPEAT = "background-repeat";// TODO: just added, handle property

    /** Constant string for CSS2 property. */
    public final static String BACKGROUND_ATTACHMENT = "background-attachment";// TODO: just added, handle property

    /** Constant string for CSS2 property. */
    public final static String BACKGROUND_POSITION = "background-position";// TODO: just added, handle property

    /** Constant string for CSS2 property. */
    public final static String BORDER_COLLAPSE = "border-collapse";// TODO: just added, handle property

    /** Constant string for CSS2 property. */
    public final static String BORDER_SPACING = "border-spacing";// TODO: just added, handle property

    /** Constant string for CSS2 property. */
    public final static String BOTTOM = "bottom";// TODO: just added, handle property

    /** Constant string for CSS2 property. */
    public final static String CAPTION_SIDE = "caption-side";// TODO: just added, handle property

    /** Constant string for CSS2 property. */
    public final static String CLEAR = "clear";// TODO: just added, handle property

    /** Constant string for CSS2 property. */
    public final static String CLIP = "clip";// TODO: just added, handle property

    /** Constant string for CSS2 property. */
    public final static String CONTENT = "content";// TODO: just added, handle property

    /** Constant string for CSS2 property. */
    public final static String COUNTER_INCREMENT = "counter-increment";// TODO: just added, handle property

    /** Constant string for CSS2 property. */
    public final static String COUNTER_RESET = "counter-reset";// TODO: just added, handle property

    /** Constant string for CSS2 property. */
    public final static String CURSOR = "cursor";// TODO: just added, handle property

    /** Constant string for CSS2 property. */
    public final static String DIRECTION = "direction";// TODO: just added, handle property

    /** Constant string for CSS2 property. */
    public final static String DISPLAY = "display";// TODO: just added, handle property

    /** Constant string for CSS2 property. */
    public final static String EMPTY_CELLS = "empty-cells";// TODO: just added, handle property

    /** Constant string for CSS2 property. */
    public final static String FLOAT = "float";// TODO: just added, handle property

    /** Constant string for CSS2 property. */
    public final static String FONT_SHORTHAND = "font";// TODO: shorthand

    /** Constant string for CSS2 property. */
    public final static String FONT_STYLE = "font-style";// TODO: just added, handle property

    /** Constant string for CSS2 property. */
    public final static String FONT_VARIANT = "font-variant";// TODO: just added, handle property

    /** Constant string for CSS2 property. */
    public final static String FONT_WEIGHT = "font-weight";// TODO: just added, handle property

    /** Constant string for CSS2 property. */
    public final static String FONT_SIZE = "font-size";// TODO: just added, handle property

    /** Constant string for CSS2 property. */
    public final static String LINE_HEIGHT = "line-height";// TODO: just added, handle property

    /** Constant string for CSS2 property. */
    public final static String FONT_FAMILY = "font-family";// TODO: just added, handle property

    /** Constant string for CSS2 property. */
    public final static String FONT_SIZE_ADJUST = "font-size-adjust";// TODO: just added, handle property

    /** Constant string for CSS2 property. */
    public final static String FONT_STRETCH = "font-stretch";// TODO: just added, handle property

    /** Constant string for CSS2 property. */
    public final static String HEIGHT = "height";// TODO: just added, handle property

    /** Constant string for CSS2 property. */
    public final static String LEFT = "left";// TODO: just added, handle property

    /** Constant string for CSS2 property. */
    public final static String LETTER_SPACING = "letter-spacing";// TODO: just added, handle property

    /** Constant string for CSS2 property. */
    public final static String LIST_STYLE_SHORTHAND = "list-style";// TODO: shorthand

    /** Constant string for CSS2 property. */
    public final static String LIST_STYLE_TYPE = "list-style-type";// TODO: just added, handle property

    /** Constant string for CSS2 property. */
    public final static String LIST_STYLE_POSITION = "list-style-position";// TODO: just added, handle property

    /** Constant string for CSS2 property. */
    public final static String LIST_STYLE_IMAGE = "list-style-image";// TODO: just added, handle property

    /** Constant string for CSS2 property. */
    public final static String MARKER_OFFSET = "marker-offset";// TODO: just added, handle property

    /** Constant string for CSS2 property. */
    public final static String MARKS = "marks";// TODO: just added, handle property

    /** Constant string for CSS2 property. */
    public final static String MAX_HEIGHT = "max-height";// TODO: just added, handle property

    /** Constant string for CSS2 property. */
    public final static String MAX_WIDTH = "max-width";// TODO: just added, handle property

    /** Constant string for CSS2 property. */
    public final static String MIN_HEIGHT = "min-height";// TODO: just added, handle property

    /** Constant string for CSS2 property. */
    public final static String MIN_WIDTH = "min-width";// TODO: just added, handle property

    /** Constant string for CSS2 property. */
    public final static String ORPHANS = "orphans";// TODO: just added, handle property

    /** Constant string for CSS2 property. */
    public final static String OUTLINE_SHORTHAND = "outline";// TODO: shorthand

    /** Constant string for CSS2 property. */
    public final static String OUTLINE_COLOR = "outline-color";// TODO: just added, handle property

    /** Constant string for CSS2 property. */
    public final static String OUTLINE_STYLE = "outline-style";// TODO: just added, handle property

    /** Constant string for CSS2 property. */
    public final static String OUTLINE_WIDTH = "outline-width";// TODO: just added, handle property

    /** Constant string for CSS2 property. */
    public final static String OVERFLOW = "overflow";// TODO: just added, handle property

    /** Constant string for CSS2 property. */
    public final static String PAGE = "page";// TODO: just added, handle property

    /** Constant string for CSS2 property. */
    public final static String PAGE_BREAK_AFTER = "page-break-after";// TODO: just added, handle property

    /** Constant string for CSS2 property. */
    public final static String PAGE_BREAK_BEFORE = "page-break-before";// TODO: just added, handle property

    /** Constant string for CSS2 property. */
    public final static String PAGE_BREAK_INSIDE = "page-break-inside";// TODO: just added, handle property

    /** Constant string for CSS2 property. */
    public final static String POSITION = "position";// TODO: just added, handle property

    /** Constant string for CSS2 property. */
    public final static String QUOTES = "quotes";// TODO: just added, handle property

    /** Constant string for CSS2 property. */
    public final static String RIGHT = "right";// TODO: just added, handle property

    /** Constant string for CSS2 property. */
    public final static String SIZE = "size";// TODO: just added, handle property

    /** Constant string for CSS2 property. */
    public final static String TABLE_LAYOUT = "table-layout";// TODO: just added, handle property

    /** Constant string for CSS2 property. */
    public final static String TEXT_ALIGN = "text-align";// TODO: just added, handle property

    /** Constant string for CSS2 property. */
    public final static String TEXT_DECORATION = "text-decoration";// TODO: just added, handle property

    /** Constant string for CSS2 property. */
    public final static String TEXT_INDENT = "text-indent";// TODO: just added, handle property

    /** Constant string for CSS2 property. */
    public final static String TEXT_SHADOW = "text-shadow";// TODO: just added, handle property

    /** Constant string for CSS2 property. */
    public final static String TEXT_TRANSFORM = "text-transform";// TODO: just added, handle property

    /** Constant string for CSS2 property. */
    public final static String TOP = "top";// TODO: just added, handle property

    /** Constant string for CSS2 property. */
    public final static String UNICODE_BIDI = "unicode-bidi";// TODO: just added, handle property

    /** Constant string for CSS2 property. */
    public final static String VERTICAL_ALIGN = "vertical-align";// TODO: just added, handle property

    /** Constant string for CSS2 property. */
    public final static String VISIBILITY = "visibility";// TODO: just added, handle property

    /** Constant string for CSS2 property. */
    public final static String WHITE_SPACE = "white-space";// TODO: just added, handle property

    /** Constant string for CSS2 property. */
    public final static String WIDOWS = "widows";// TODO: just added, handle property

    /** Constant string for CSS2 property. */
    public final static String WIDTH = "width";// TODO: just added, handle property

    /** Constant string for CSS2 property. */
    public final static String WORD_SPACING = "word-spacing";// TODO: just added, handle property

    /** Constant string for CSS2 property. */
    public final static String Z_INDEX = "z-index";// TODO: just added, handle property

    /** Constant string for CSS2 property. */
    public final static String BORDER_SHORTHAND = "border";
    /** Constant string for CSS2 property. */
    public final static String BORDER_TOP_SHORTHAND = "border-top";
    /** Constant string for CSS2 property. */
    public final static String BORDER_RIGHT_SHORTHAND = "border-right";
    /** Constant string for CSS2 property. */
    public final static String BORDER_BOTTOM_SHORTHAND = "border-bottom";
    /** Constant string for CSS2 property. */
    public final static String BORDER_LEFT_SHORTHAND = "border-left";

    /** Constant string for CSS2 property. */
    public final static String BORDER_COLOR_SHORTHAND = "border-color";
    /** Constant string for CSS2 property. */
    public final static String BORDER_COLOR_TOP = "border-top-color";
    /** Constant string for CSS2 property. */
    public final static String BORDER_COLOR_RIGHT = "border-right-color";
    /** Constant string for CSS2 property. */
    public final static String BORDER_COLOR_BOTTOM = "border-bottom-color";
    /** Constant string for CSS2 property. */
    public final static String BORDER_COLOR_LEFT = "border-left-color";

    /** Constant string for CSS2 property. */
    public final static String BORDER_STYLE_SHORTHAND = "border-style";
    /** Constant string for CSS2 property. */
    public final static String BORDER_STYLE_TOP = "border-top-style";
    /** Constant string for CSS2 property. */
    public final static String BORDER_STYLE_RIGHT = "border-right-style";
    /** Constant string for CSS2 property. */
    public final static String BORDER_STYLE_BOTTOM = "border-bottom-style";
    /** Constant string for CSS2 property. */
    public final static String BORDER_STYLE_LEFT = "border-left-style";

    /** Constant string for CSS2 property. */
    public final static String BORDER_WIDTH_SHORTHAND = "border-width";

    /** Constant string for CSS2 property. */
    public final static String BORDER_WIDTH_TOP = "border-top-width";
    /** Constant string for CSS2 property. */
    public final static String BORDER_WIDTH_RIGHT = "border-right-width";
    /** Constant string for CSS2 property. */
    public final static String BORDER_WIDTH_BOTTOM = "border-bottom-width";
    /** Constant string for CSS2 property. */
    public final static String BORDER_WIDTH_LEFT = "border-left-width";

    /** Constant string for CSS2 property. */
    public final static String MARGIN_SHORTHAND = "margin";
    /** Constant string for CSS2 property. */
    public final static String MARGIN_TOP = "margin-top";
    /** Constant string for CSS2 property. */
    public final static String MARGIN_RIGHT = "margin-right";
    /** Constant string for CSS2 property. */
    public final static String MARGIN_BOTTOM = "margin-bottom";
    /** Constant string for CSS2 property. */
    public final static String MARGIN_LEFT = "margin-left";

    /** Constant string for CSS2 property. */
    public final static String PADDING_SHORTHAND = "padding";
    /** Constant string for CSS2 property. */
    public final static String PADDING_TOP = "padding-top";
    /** Constant string for CSS2 property. */
    public final static String PADDING_RIGHT = "padding-right";
    /** Constant string for CSS2 property. */
    public final static String PADDING_BOTTOM = "padding-bottom";
    /** Constant string for CSS2 property. */
    public final static String PADDING_LEFT = "padding-left";
    /** Constant string for CSS2 property. */
    private final static List ALL_PROPERTY_NAMES;// static block at bottom of class
    /** Constant string for CSS2 property. */
    private final static List DEFAULT_INHERITABLE;// static block at bottom of class

    /** Map of property names to initial values, per property, as defined by CSS Spec. */
    private final static Map  INITIAL_VALUE_MAP;

    /**
     * Iterator of ALL CSS 2 visual property names.
     *
     * @return   Returns
     */
    public final static Iterator allCSS2PropertyNames() {
        return ALL_PROPERTY_NAMES.iterator();
    }

    /**
     * Returns true if the named property inherits by default, according to the CSS2 spec.
     *
     * @param propName  PARAM
     * @return          Returns
     */
    public final static boolean propertyInherits( String propName ) {
        return DEFAULT_INHERITABLE.contains( propName );
    }

    /**
     * Returns the initial value of the named property, 
     * according to the CSS2 spec, as a String. Casting
     * must be taken care of by the caller, as there is too much
     * variation in value-types.
     *
     * @param propName  PARAM
     * @return          Returns
     */
    public final static String initialValue( String propName ) {
        return (String)INITIAL_VALUE_MAP.get( propName );
    }

    
    static {
        ALL_PROPERTY_NAMES = new ArrayList();
        try {
            Field fields[] = CSSName.class.getFields();
            for ( int i = 0; i < fields.length; i++ ) {
                Field f = fields[i];
                int mod = f.getModifiers();
                if ( Modifier.isFinal( mod ) && Modifier.isStatic( mod ) && Modifier.isPublic( mod ) && f.getType() == String.class ) {
                    ALL_PROPERTY_NAMES.add( (String)f.get( null ) );
                }
            }
        } catch ( Exception ex ) {
            ex.printStackTrace();
        }

        DEFAULT_INHERITABLE = new ArrayList();
        DEFAULT_INHERITABLE.add(BORDER_COLLAPSE);
        DEFAULT_INHERITABLE.add(BORDER_SPACING);
        DEFAULT_INHERITABLE.add(CAPTION_SIDE);
        DEFAULT_INHERITABLE.add(COLOR);
        DEFAULT_INHERITABLE.add(CURSOR);
        DEFAULT_INHERITABLE.add(DIRECTION);
        DEFAULT_INHERITABLE.add(EMPTY_CELLS);
        DEFAULT_INHERITABLE.add(FONT_SHORTHAND);
        DEFAULT_INHERITABLE.add(FONT_FAMILY);
        DEFAULT_INHERITABLE.add(FONT_SIZE);
        DEFAULT_INHERITABLE.add(FONT_SIZE_ADJUST);
        DEFAULT_INHERITABLE.add(FONT_STRETCH);
        DEFAULT_INHERITABLE.add(FONT_STYLE);
        DEFAULT_INHERITABLE.add(FONT_VARIANT);
        DEFAULT_INHERITABLE.add(FONT_WEIGHT);
        DEFAULT_INHERITABLE.add(LETTER_SPACING);
        DEFAULT_INHERITABLE.add(LINE_HEIGHT);
        DEFAULT_INHERITABLE.add(LIST_STYLE_SHORTHAND);
        DEFAULT_INHERITABLE.add(LIST_STYLE_IMAGE);
        DEFAULT_INHERITABLE.add(LIST_STYLE_POSITION);
        DEFAULT_INHERITABLE.add(LIST_STYLE_TYPE);
        DEFAULT_INHERITABLE.add(ORPHANS);
        DEFAULT_INHERITABLE.add(PAGE);
        DEFAULT_INHERITABLE.add(PAGE_BREAK_INSIDE);
        DEFAULT_INHERITABLE.add(POSITION);
        DEFAULT_INHERITABLE.add(QUOTES);
        DEFAULT_INHERITABLE.add(PAGE_BREAK_INSIDE);
        DEFAULT_INHERITABLE.add(TEXT_ALIGN);
        DEFAULT_INHERITABLE.add(TEXT_INDENT);
        DEFAULT_INHERITABLE.add(TEXT_TRANSFORM);
        DEFAULT_INHERITABLE.add(WHITE_SPACE);
        DEFAULT_INHERITABLE.add(WIDOWS);
        DEFAULT_INHERITABLE.add(WORD_SPACING);

        INITIAL_VALUE_MAP = new HashMap();
        INITIAL_VALUE_MAP.put(BACKGROUND_ATTACHMENT,   "scroll");
        INITIAL_VALUE_MAP.put(BACKGROUND_COLOR,        "transparent");
        INITIAL_VALUE_MAP.put(BACKGROUND_IMAGE,        "none");
        INITIAL_VALUE_MAP.put(BACKGROUND_POSITION,     "0% 0%");
        INITIAL_VALUE_MAP.put(BACKGROUND_REPEAT,       "repeat");

        INITIAL_VALUE_MAP.put(BORDER_COLLAPSE,    "collapse");

        INITIAL_VALUE_MAP.put(BORDER_COLOR_TOP,    "black");
        INITIAL_VALUE_MAP.put(BORDER_COLOR_RIGHT,  "black");
        INITIAL_VALUE_MAP.put(BORDER_COLOR_BOTTOM, "black");
        INITIAL_VALUE_MAP.put(BORDER_COLOR_LEFT,   "black");

        INITIAL_VALUE_MAP.put(BORDER_SPACING,      "0px");
        
        INITIAL_VALUE_MAP.put(BORDER_STYLE_SHORTHAND,   "none"); // CLEAN, normally don't assign value for shorthand but code for B.S. is not side-specific yet (PWW 24-08-04)

        INITIAL_VALUE_MAP.put(BORDER_STYLE_TOP,    "none");
        INITIAL_VALUE_MAP.put(BORDER_STYLE_RIGHT,  "none");
        INITIAL_VALUE_MAP.put(BORDER_STYLE_BOTTOM, "none");
        INITIAL_VALUE_MAP.put(BORDER_STYLE_LEFT,   "none");
        
        INITIAL_VALUE_MAP.put(BORDER_WIDTH_TOP,    "thin");
        INITIAL_VALUE_MAP.put(BORDER_WIDTH_RIGHT,  "thin");
        INITIAL_VALUE_MAP.put(BORDER_WIDTH_BOTTOM, "thin");
        INITIAL_VALUE_MAP.put(BORDER_WIDTH_LEFT,   "thin");

        INITIAL_VALUE_MAP.put(BOTTOM,    "auto");

        INITIAL_VALUE_MAP.put(CAPTION_SIDE,    "top");
        INITIAL_VALUE_MAP.put(CLEAR,    "none");
        INITIAL_VALUE_MAP.put(CLIP,    "auto");

        INITIAL_VALUE_MAP.put(COLOR,   "black"); // CLEAN: UA dependent

        INITIAL_VALUE_MAP.put(CONTENT,   ""); 
        
        INITIAL_VALUE_MAP.put(COUNTER_INCREMENT,   "none");
        INITIAL_VALUE_MAP.put(COUNTER_RESET,       "none");
        
        INITIAL_VALUE_MAP.put(CURSOR,       "auto");
        
        INITIAL_VALUE_MAP.put(DIRECTION,     "ltr");
        
        INITIAL_VALUE_MAP.put(DISPLAY,     "inline");
        
        INITIAL_VALUE_MAP.put(EMPTY_CELLS,     "show");
        
        INITIAL_VALUE_MAP.put(FLOAT,     "none");
        
        INITIAL_VALUE_MAP.put(FONT_FAMILY, "\"Times New Roman\"");
        INITIAL_VALUE_MAP.put(FONT_SIZE, "medium");
        INITIAL_VALUE_MAP.put(FONT_SIZE_ADJUST, "none");
        INITIAL_VALUE_MAP.put(FONT_STRETCH, "normal");
        INITIAL_VALUE_MAP.put(FONT_STYLE, "normal");
        INITIAL_VALUE_MAP.put(FONT_VARIANT, "normal");
        INITIAL_VALUE_MAP.put(FONT_WEIGHT, "normal");

        INITIAL_VALUE_MAP.put(HEIGHT, "auto");

        INITIAL_VALUE_MAP.put(LEFT, "auto");

        INITIAL_VALUE_MAP.put(LETTER_SPACING, "normal");
        INITIAL_VALUE_MAP.put(LINE_HEIGHT, "normal");

        INITIAL_VALUE_MAP.put(LIST_STYLE_IMAGE, "none");
        INITIAL_VALUE_MAP.put(LIST_STYLE_POSITION, "outside");
        INITIAL_VALUE_MAP.put(LIST_STYLE_TYPE, "disc");

        INITIAL_VALUE_MAP.put(MARGIN_TOP,    "0px");
        INITIAL_VALUE_MAP.put(MARGIN_RIGHT,  "0px");
        INITIAL_VALUE_MAP.put(MARGIN_BOTTOM, "0px");
        INITIAL_VALUE_MAP.put(MARGIN_LEFT,   "0px");

        INITIAL_VALUE_MAP.put(MARKER_OFFSET,   "auto");
        INITIAL_VALUE_MAP.put(MARKS,   "none");

        INITIAL_VALUE_MAP.put(MAX_HEIGHT,   "none");
        INITIAL_VALUE_MAP.put(MIN_HEIGHT,   "0");
        INITIAL_VALUE_MAP.put(MAX_WIDTH,   "none");
        INITIAL_VALUE_MAP.put(MIN_WIDTH,   "0"); // CLEAN: UA dependent

        INITIAL_VALUE_MAP.put(ORPHANS,   "2"); 
        
        INITIAL_VALUE_MAP.put(OUTLINE_COLOR,   "invert"); 
        INITIAL_VALUE_MAP.put(OUTLINE_STYLE,   "none"); 
        INITIAL_VALUE_MAP.put(OUTLINE_WIDTH,   "medium"); 
        
        INITIAL_VALUE_MAP.put(OVERFLOW,   "visible"); 

        INITIAL_VALUE_MAP.put(PADDING_TOP,    "0px");
        INITIAL_VALUE_MAP.put(PADDING_RIGHT,  "0px");
        INITIAL_VALUE_MAP.put(PADDING_BOTTOM, "0px");
        INITIAL_VALUE_MAP.put(PADDING_LEFT,   "0px");

        INITIAL_VALUE_MAP.put(PAGE,                "auto");
        INITIAL_VALUE_MAP.put(PAGE_BREAK_AFTER,    "auto");
        INITIAL_VALUE_MAP.put(PAGE_BREAK_BEFORE,   "auto");
        INITIAL_VALUE_MAP.put(PAGE_BREAK_INSIDE,   "auto");
        
        INITIAL_VALUE_MAP.put(POSITION,   "static");
        INITIAL_VALUE_MAP.put(QUOTES,     "none"); // CLEAN: depends on UA

        INITIAL_VALUE_MAP.put(RIGHT,   "auto");
        INITIAL_VALUE_MAP.put(SIZE,    "auto");

        INITIAL_VALUE_MAP.put(TABLE_LAYOUT,    "auto");
        
        INITIAL_VALUE_MAP.put(TEXT_ALIGN,       "left"); // CLEAN: depends on UA
        INITIAL_VALUE_MAP.put(TEXT_DECORATION,  "none");
        INITIAL_VALUE_MAP.put(TEXT_INDENT,      "0px");
        INITIAL_VALUE_MAP.put(TEXT_SHADOW,      "none");
        INITIAL_VALUE_MAP.put(TEXT_TRANSFORM,   "none");
        
        INITIAL_VALUE_MAP.put(UNICODE_BIDI,   "normal");
        INITIAL_VALUE_MAP.put(VERTICAL_ALIGN,   "baseline");
        
        INITIAL_VALUE_MAP.put(VISIBILITY,   "inherit");

        INITIAL_VALUE_MAP.put(WHITE_SPACE,   "normal");
        INITIAL_VALUE_MAP.put(WIDOWS,   "2");
        INITIAL_VALUE_MAP.put(WIDTH,   "auto");
        INITIAL_VALUE_MAP.put(WORD_SPACING,   "normal");
        INITIAL_VALUE_MAP.put(Z_INDEX,   "auto");
    }
}

