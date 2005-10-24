/*
 * {{{ header & license
 * CSSName.java
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


/**
 * A CSSName is a Singleton representing a single CSS property name, like
 * border-width. The class declares a Singleton static instance for each CSS
 * Level 2 property. A CSSName instance has the property name available from the
 * {@link #toString()} method, as well as a unique (among all CSSName instances)
 * integer id ranging from 0...n instances, incremented by 1, available using
 * the {@link #getAssignedID()} method.
 *
 * @author Patrick Wright
 */
public final class CSSName {
    /** marker var, used for initialization */
    private static final boolean IS_PRIMITIVE = true;
    
    /** marker var, used for initialization */
    private static final boolean NOT_PRIMITIVE = false;
    
    /**
     * Description of the Field
     */
    private static int maxAssigned;
    private static int maxNonPrimitiveAssigned;
    
    /**
     * Description of the Field
     */
    private final String propName;
    
    /**
     * Unique integer id for a CSSName.
     */
    public final int FS_ID;
    
    private final boolean isPrimitive;
    
    
    private static final Comparator COMPARATOR = new Comparator() {
        public int compare(Object o1, Object o2) {
            CSSName n1 = (CSSName)o1;
            CSSName n2 = (CSSName)o2;
            return n1.FS_ID - n2.FS_ID;  //To change body of implemented methods use File | Settings | File Templates.
        }
    };
    
    /**
     * Map of all CSS properties
     */
    private static CSSName[] ALL_PROPERTIES;
    
    /**
     * Map of all CSS properties
     */
    private static Map ALL_PROPERTY_NAMES = new TreeMap();
    
    /**
     * Map of all non-shorthand CSS properties
     */
    private static Map ALL_PRIMITIVE_PROPERTY_NAMES = new TreeMap();
    
    /**
     * Unique CSSName instance for CSS2 property.
     */
    private final static List DEFAULT_INHERITABLE;// static block at bottom of class
    
    /**
     * Map of property names to initial values, per property, as defined by CSS
     * Spec.
     */
    private final static Map INITIAL_VALUE_MAP;
    
    /**
     * Array of custom properties which user has declared; may include typos, however.
     */
    private static Map CSS_UNKNOWN_PROPERTIES = new HashMap();
    
    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName COLOR = addProperty("color", IS_PRIMITIVE);
    
    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName BACKGROUND_COLOR = addProperty("background-color", IS_PRIMITIVE);
    
    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName BACKGROUND_IMAGE = addProperty("background-image", IS_PRIMITIVE);
    
    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName BACKGROUND_REPEAT = addProperty("background-repeat", IS_PRIMITIVE);
    
    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName BACKGROUND_ATTACHMENT = addProperty("background-attachment", IS_PRIMITIVE);
    
    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName BACKGROUND_POSITION = addProperty("background-position", IS_PRIMITIVE);
    
    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName BORDER_COLLAPSE = addProperty("border-collapse", IS_PRIMITIVE);
    
    /**
     * Unique CSSName instance for fictitious property.
     */
    public final static CSSName FS_BORDER_SPACING_HORIZONTAL = addProperty("-fs-border-spacing-horizontal", IS_PRIMITIVE);
    
    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName FS_BORDER_SPACING_VERTICAL = addProperty("-fs-border-spacing-vertical", IS_PRIMITIVE);
    
    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName BOTTOM = addProperty("bottom", IS_PRIMITIVE);
    
    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName CAPTION_SIDE = addProperty("caption-side", IS_PRIMITIVE);
    
    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName CLEAR = addProperty("clear", IS_PRIMITIVE);
    
    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName CLIP = addProperty("clip", IS_PRIMITIVE);
    
    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName CONTENT = addProperty("content", IS_PRIMITIVE);
    
    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName COUNTER_INCREMENT = addProperty("counter-increment", IS_PRIMITIVE);
    
    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName COUNTER_RESET = addProperty("counter-reset", IS_PRIMITIVE);
    
    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName CURSOR = addProperty("cursor", IS_PRIMITIVE);
    
    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName DIRECTION = addProperty("direction", IS_PRIMITIVE);
    
    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName DISPLAY = addProperty("display", IS_PRIMITIVE);
    
    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName EMPTY_CELLS = addProperty("empty-cells", IS_PRIMITIVE);
    
    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName FLOAT = addProperty("float", IS_PRIMITIVE);
    
    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName FONT_STYLE = addProperty("font-style", IS_PRIMITIVE);
    
    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName FONT_VARIANT = addProperty("font-variant", IS_PRIMITIVE);
    
    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName FONT_WEIGHT = addProperty("font-weight", IS_PRIMITIVE);
    
    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName FONT_SIZE = addProperty("font-size", IS_PRIMITIVE);
    
    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName LINE_HEIGHT = addProperty("line-height", IS_PRIMITIVE);
    
    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName FONT_FAMILY = addProperty("font-family", IS_PRIMITIVE);
    
    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName FONT_SIZE_ADJUST = addProperty("font-size-adjust", IS_PRIMITIVE);
    
    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName FONT_STRETCH = addProperty("font-stretch", IS_PRIMITIVE);
    
    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName FS_COLSPAN = addProperty("-fs-table-cell-colspan", IS_PRIMITIVE);
    
    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName FS_ROWSPAN = addProperty("-fs-table-cell-rowspan", IS_PRIMITIVE);
    
    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName HEIGHT = addProperty("height", IS_PRIMITIVE);
    
    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName LEFT = addProperty("left", IS_PRIMITIVE);
    
    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName LETTER_SPACING = addProperty("letter-spacing", IS_PRIMITIVE);
    
    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName LIST_STYLE_TYPE = addProperty("list-style-type", IS_PRIMITIVE);
    
    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName LIST_STYLE_POSITION = addProperty("list-style-position", IS_PRIMITIVE);
    
    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName LIST_STYLE_IMAGE = addProperty("list-style-image", IS_PRIMITIVE);
    
    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName MARKER_OFFSET = addProperty("marker-offset", IS_PRIMITIVE);
    
    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName MARKS = addProperty("marks", IS_PRIMITIVE);
    
    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName MAX_HEIGHT = addProperty("max-height", IS_PRIMITIVE);
    
    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName MAX_WIDTH = addProperty("max-width", IS_PRIMITIVE);
    
    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName MIN_HEIGHT = addProperty("min-height", IS_PRIMITIVE);
    
    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName MIN_WIDTH = addProperty("min-width", IS_PRIMITIVE);
    
    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName ORPHANS = addProperty("orphans", IS_PRIMITIVE);
    
    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName OUTLINE_COLOR = addProperty("outline-color", IS_PRIMITIVE);
    
    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName OUTLINE_STYLE = addProperty("outline-style", IS_PRIMITIVE);
    
    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName OUTLINE_WIDTH = addProperty("outline-width", IS_PRIMITIVE);
    
    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName OVERFLOW = addProperty("overflow", IS_PRIMITIVE);
    
    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName PAGE = addProperty("page", IS_PRIMITIVE);
    
    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName PAGE_BREAK_AFTER = addProperty("page-break-after", IS_PRIMITIVE);
    
    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName PAGE_BREAK_BEFORE = addProperty("page-break-before", IS_PRIMITIVE);
    
    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName PAGE_BREAK_INSIDE = addProperty("page-break-inside", IS_PRIMITIVE);
    
    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName POSITION = addProperty("position", IS_PRIMITIVE);
    
    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName QUOTES = addProperty("quotes", IS_PRIMITIVE);
    
    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName RIGHT = addProperty("right", IS_PRIMITIVE);
    
    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName SIZE = addProperty("size", IS_PRIMITIVE);
    
    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName TABLE_LAYOUT = addProperty("table-layout", IS_PRIMITIVE);
    
    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName TEXT_ALIGN = addProperty("text-align", IS_PRIMITIVE);
    
    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName TEXT_DECORATION = addProperty("text-decoration", IS_PRIMITIVE);
    
    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName TEXT_INDENT = addProperty("text-indent", IS_PRIMITIVE);
    
    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName TEXT_SHADOW = addProperty("text-shadow", IS_PRIMITIVE);
    
    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName TEXT_TRANSFORM = addProperty("text-transform", IS_PRIMITIVE);
    
    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName TOP = addProperty("top", IS_PRIMITIVE);
    
    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName UNICODE_BIDI = addProperty("unicode-bidi", IS_PRIMITIVE);
    
    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName VERTICAL_ALIGN = addProperty("vertical-align", IS_PRIMITIVE);
    
    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName VISIBILITY = addProperty("visibility", IS_PRIMITIVE);
    
    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName WHITE_SPACE = addProperty("white-space", IS_PRIMITIVE);
    
    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName WIDOWS = addProperty("widows", IS_PRIMITIVE);
    
    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName WIDTH = addProperty("width", IS_PRIMITIVE);
    
    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName WORD_SPACING = addProperty("word-spacing", IS_PRIMITIVE);
    
    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName Z_INDEX = addProperty("z-index", IS_PRIMITIVE);
    
    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName BORDER_COLOR_TOP = addProperty("border-top-color", IS_PRIMITIVE);
    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName BORDER_COLOR_RIGHT = addProperty("border-right-color", IS_PRIMITIVE);
    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName BORDER_COLOR_BOTTOM = addProperty("border-bottom-color", IS_PRIMITIVE);
    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName BORDER_COLOR_LEFT = addProperty("border-left-color", IS_PRIMITIVE);
    
    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName BORDER_STYLE_TOP = addProperty("border-top-style", IS_PRIMITIVE);
    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName BORDER_STYLE_RIGHT = addProperty("border-right-style", IS_PRIMITIVE);
    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName BORDER_STYLE_BOTTOM = addProperty("border-bottom-style", IS_PRIMITIVE);
    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName BORDER_STYLE_LEFT = addProperty("border-left-style", IS_PRIMITIVE);
        
    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName BORDER_WIDTH_TOP = addProperty("border-top-width", IS_PRIMITIVE);
    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName BORDER_WIDTH_RIGHT = addProperty("border-right-width", IS_PRIMITIVE);
    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName BORDER_WIDTH_BOTTOM = addProperty("border-bottom-width", IS_PRIMITIVE);
    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName BORDER_WIDTH_LEFT = addProperty("border-left-width", IS_PRIMITIVE);
    
    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName MARGIN_TOP = addProperty("margin-top", IS_PRIMITIVE);
    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName MARGIN_RIGHT = addProperty("margin-right", IS_PRIMITIVE);
    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName MARGIN_BOTTOM = addProperty("margin-bottom", IS_PRIMITIVE);
    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName MARGIN_LEFT = addProperty("margin-left", IS_PRIMITIVE);
    
    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName PADDING_TOP = addProperty("padding-top", IS_PRIMITIVE);
    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName PADDING_RIGHT = addProperty("padding-right", IS_PRIMITIVE);
    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName PADDING_BOTTOM = addProperty("padding-bottom", IS_PRIMITIVE);
    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName PADDING_LEFT = addProperty("padding-left", IS_PRIMITIVE);
    
    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName BACKGROUND_SHORTHAND = addProperty("background", NOT_PRIMITIVE);
    
    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName BORDER_WIDTH_SHORTHAND = addProperty("border-width", NOT_PRIMITIVE);
    
    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName BORDER_STYLE_SHORTHAND = addProperty("border-style", NOT_PRIMITIVE);

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName BORDER_SHORTHAND = addProperty("border", NOT_PRIMITIVE);
    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName BORDER_TOP_SHORTHAND = addProperty("border-top", NOT_PRIMITIVE);
    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName BORDER_RIGHT_SHORTHAND = addProperty("border-right", NOT_PRIMITIVE);
    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName BORDER_BOTTOM_SHORTHAND = addProperty("border-bottom", NOT_PRIMITIVE);
    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName BORDER_LEFT_SHORTHAND = addProperty("border-left", NOT_PRIMITIVE);
    
    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName BORDER_COLOR_SHORTHAND = addProperty("border-color", NOT_PRIMITIVE);

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName BORDER_SPACING = addProperty("border-spacing", NOT_PRIMITIVE);
    
    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName FONT_SHORTHAND = addProperty("font", NOT_PRIMITIVE);
    
    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName LIST_STYLE_SHORTHAND = addProperty("list-style", NOT_PRIMITIVE);
    
    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName MARGIN_SHORTHAND = addProperty("margin", NOT_PRIMITIVE);

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName OUTLINE_SHORTHAND = addProperty("outline", NOT_PRIMITIVE);
    
    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName PADDING_SHORTHAND = addProperty("padding", NOT_PRIMITIVE);

    public final static CSSName[] MARGIN_SIDE_PROPERTIES =
            new CSSName[] {
        CSSName.MARGIN_TOP,
                CSSName.MARGIN_RIGHT,
                CSSName.MARGIN_BOTTOM,
                CSSName.MARGIN_LEFT
    };
    
    public final static CSSName[] PADDING_SIDE_PROPERTIES =
            new CSSName[] {
        CSSName.PADDING_TOP,
                CSSName.PADDING_RIGHT,
                CSSName.PADDING_BOTTOM,
                CSSName.PADDING_LEFT
    };
    
    public final static CSSName[] BORDER_SIDE_PROPERTIES =
            new CSSName[] {
        CSSName.BORDER_WIDTH_TOP,
                CSSName.BORDER_WIDTH_RIGHT,
                CSSName.BORDER_WIDTH_BOTTOM,
                CSSName.BORDER_WIDTH_LEFT
    };
    
    public final static CSSName[] BORDER_STYLE_PROPERTIES =
            new CSSName[] {
        CSSName.BORDER_STYLE_TOP,
                CSSName.BORDER_STYLE_RIGHT,
                CSSName.BORDER_STYLE_BOTTOM,
                CSSName.BORDER_STYLE_LEFT
    };
    
    public final static CSSName[] BORDER_COLOR_PROPERTIES =
            new CSSName[] {
        CSSName.BORDER_COLOR_TOP,
                CSSName.BORDER_COLOR_RIGHT,
                CSSName.BORDER_COLOR_BOTTOM,
                CSSName.BORDER_COLOR_LEFT
    };

    /**
     * Constructor for the CSSName object
     *
     * @param propName    PARAM
     * @param isPrimitive
     */
    private CSSName(String propName, boolean isPrimitive) {
        this.propName = propName;
        this.FS_ID = CSSName.maxAssigned++;
        this.isPrimitive = isPrimitive;
    }
    
    /**
     * Returns a string representation of the object, in this case, always the
     * full CSS property name in lowercase.
     *
     * @return a string representation of the object.
     */
    public String toString() {
        return this.propName;
    }
    
    /**
     * Returns a count of all CSS properties known to this class, shorthand and primitive.
     *
     * @return Returns
     */
    public final static int countCSSNames() {
        return CSSName.maxAssigned;
    }
    
    /**
     * Returns a count of all CSS primitive (non-shorthand) properties known to this class.
     *
     * @return Returns
     */
    public final static int countCSSPrimitiveNames() {
        return CSSName.maxAssigned;
    }
    
    /**
     * Iterator of ALL CSS 2 visual property names.
     *
     * @return Returns
     */
    public final static Iterator allCSS2PropertyNames() {
        return ALL_PROPERTY_NAMES.keySet().iterator();
    }
    
    /**
     * Iterator of ALL primitive (non-shorthand) CSS 2 visual property names.
     *
     * @return Returns
     */
    public final static Iterator allCSS2PrimitivePropertyNames() {
        return ALL_PRIMITIVE_PROPERTY_NAMES.keySet().iterator();
    }
    
    /**
     * Returns true if the named property inherits by default, according to the
     * CSS2 spec.
     *
     * @param cssName PARAM
     * @return Returns
     */
    public final static boolean propertyInherits(CSSName cssName) {
        return DEFAULT_INHERITABLE.contains(cssName);
    }
    
    /**
     * Returns the initial value of the named property, according to the CSS2
     * spec, as a String. Casting must be taken care of by the caller, as there
     * is too much variation in value-types.
     *
     * @param cssName PARAM
     * @return Returns
     */
    public final static String initialValue(CSSName cssName) {
        return (String) INITIAL_VALUE_MAP.get(cssName);
    }
    
    /**
     * Gets the byPropertyName attribute of the CSSName class
     *
     * @param propName PARAM
     * @return The byPropertyName value
     */
    public static CSSName getByPropertyName(String propName) {
        CSSName cssName = (CSSName) ALL_PROPERTY_NAMES.get(propName);
        
        return cssName;
    }
    
    public static CSSName getByID(int id) {
        return ALL_PROPERTIES[id];
    }
    
    /**
     * Adds a feature to the Property attribute of the CSSName class
     *
     * @param propName    The feature to be added to the Property attribute
     * @param isPrimitive
     * @return Returns
     */
    private final static synchronized CSSName addProperty(String propName, boolean isPrimitive) {
        CSSName cssName = new CSSName(propName, isPrimitive);
        if ( ALL_PROPERTY_NAMES.get(propName) != null ) System.err.println("!!! PROPERTY " + propName);
        ALL_PROPERTY_NAMES.put(propName, cssName);
        if (isPrimitive) {
            ALL_PRIMITIVE_PROPERTY_NAMES.put(propName, cssName);
        }
        return cssName;
    }
    
    static {
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
        DEFAULT_INHERITABLE.add(QUOTES);
        DEFAULT_INHERITABLE.add(PAGE_BREAK_INSIDE);
        DEFAULT_INHERITABLE.add(TEXT_ALIGN);
        DEFAULT_INHERITABLE.add(TEXT_INDENT);
        DEFAULT_INHERITABLE.add(TEXT_TRANSFORM);
        DEFAULT_INHERITABLE.add(WHITE_SPACE);
        DEFAULT_INHERITABLE.add(WIDOWS);
        DEFAULT_INHERITABLE.add(WORD_SPACING);
        
        // TODO: add placeholders for custom CSS properties
        
        
        INITIAL_VALUE_MAP = new HashMap();
        INITIAL_VALUE_MAP.put(BACKGROUND_ATTACHMENT, "scroll");
        INITIAL_VALUE_MAP.put(BACKGROUND_COLOR, "transparent");
        INITIAL_VALUE_MAP.put(BACKGROUND_IMAGE, "none");
        INITIAL_VALUE_MAP.put(BACKGROUND_POSITION, "0% 0%");
        INITIAL_VALUE_MAP.put(BACKGROUND_REPEAT, "repeat");
        
        INITIAL_VALUE_MAP.put(BORDER_COLLAPSE, "separate");
        
        INITIAL_VALUE_MAP.put(BORDER_COLOR_TOP, "=color");//initial is the style's own color property
        INITIAL_VALUE_MAP.put(BORDER_COLOR_RIGHT, "=color");//initial is the style's own color property
        INITIAL_VALUE_MAP.put(BORDER_COLOR_BOTTOM, "=color");//initial is the style's own color property
        INITIAL_VALUE_MAP.put(BORDER_COLOR_LEFT, "=color");//initial is the style's own color property
        
        INITIAL_VALUE_MAP.put(BORDER_SPACING, "0px");
        
        INITIAL_VALUE_MAP.put(BORDER_STYLE_SHORTHAND, "none");// CLEAN, normally don't assign value for shorthand but code for B.S. is not side-specific yet (PWW 24-08-04)
        
        INITIAL_VALUE_MAP.put(BORDER_STYLE_TOP, "none");
        INITIAL_VALUE_MAP.put(BORDER_STYLE_RIGHT, "none");
        INITIAL_VALUE_MAP.put(BORDER_STYLE_BOTTOM, "none");
        INITIAL_VALUE_MAP.put(BORDER_STYLE_LEFT, "none");
        
        INITIAL_VALUE_MAP.put(BORDER_WIDTH_TOP, "medium");
        INITIAL_VALUE_MAP.put(BORDER_WIDTH_RIGHT, "medium");
        INITIAL_VALUE_MAP.put(BORDER_WIDTH_BOTTOM, "medium");
        INITIAL_VALUE_MAP.put(BORDER_WIDTH_LEFT, "medium");
        
        INITIAL_VALUE_MAP.put(BOTTOM, "auto");
        
        INITIAL_VALUE_MAP.put(CAPTION_SIDE, "top");
        INITIAL_VALUE_MAP.put(CLEAR, "none");
        INITIAL_VALUE_MAP.put(CLIP, "auto");
        
        INITIAL_VALUE_MAP.put(COLOR, "black");// TODO: UA dependent
        
        INITIAL_VALUE_MAP.put(CONTENT, "");
        
        INITIAL_VALUE_MAP.put(COUNTER_INCREMENT, "none");
        INITIAL_VALUE_MAP.put(COUNTER_RESET, "none");
        
        INITIAL_VALUE_MAP.put(CURSOR, "auto");
        
        INITIAL_VALUE_MAP.put(DIRECTION, "ltr");
        
        INITIAL_VALUE_MAP.put(DISPLAY, "inline");
        
        INITIAL_VALUE_MAP.put(EMPTY_CELLS, "show");
        
        INITIAL_VALUE_MAP.put(FLOAT, "none");
        
        INITIAL_VALUE_MAP.put(FONT_FAMILY, "\"Times New Roman\"");
        INITIAL_VALUE_MAP.put(FONT_SIZE, "medium");
        INITIAL_VALUE_MAP.put(FONT_SIZE_ADJUST, "none");
        INITIAL_VALUE_MAP.put(FONT_STRETCH, "normal");
        INITIAL_VALUE_MAP.put(FONT_STYLE, "normal");
        INITIAL_VALUE_MAP.put(FONT_VARIANT, "normal");
        INITIAL_VALUE_MAP.put(FONT_WEIGHT, "normal");
        
        INITIAL_VALUE_MAP.put(FS_COLSPAN, "1");
        INITIAL_VALUE_MAP.put(FS_ROWSPAN, "1");
        INITIAL_VALUE_MAP.put(FS_BORDER_SPACING_HORIZONTAL, "0");
        INITIAL_VALUE_MAP.put(FS_BORDER_SPACING_VERTICAL, "0");
        
        INITIAL_VALUE_MAP.put(HEIGHT, "auto");
        
        INITIAL_VALUE_MAP.put(LEFT, "auto");
        
        INITIAL_VALUE_MAP.put(LETTER_SPACING, "normal");
        INITIAL_VALUE_MAP.put(LINE_HEIGHT, "normal");
        
        INITIAL_VALUE_MAP.put(LIST_STYLE_IMAGE, "none");
        INITIAL_VALUE_MAP.put(LIST_STYLE_POSITION, "outside");
        INITIAL_VALUE_MAP.put(LIST_STYLE_TYPE, "disc");
        
        INITIAL_VALUE_MAP.put(MARGIN_TOP, "0px");
        INITIAL_VALUE_MAP.put(MARGIN_RIGHT, "0px");
        INITIAL_VALUE_MAP.put(MARGIN_BOTTOM, "0px");
        INITIAL_VALUE_MAP.put(MARGIN_LEFT, "0px");
        
        INITIAL_VALUE_MAP.put(MARKER_OFFSET, "auto");
        INITIAL_VALUE_MAP.put(MARKS, "none");
        
        INITIAL_VALUE_MAP.put(MAX_HEIGHT, "none");
        INITIAL_VALUE_MAP.put(MIN_HEIGHT, "0");
        INITIAL_VALUE_MAP.put(MAX_WIDTH, "none");
        INITIAL_VALUE_MAP.put(MIN_WIDTH, "0");// TODO: UA dependent
        
        INITIAL_VALUE_MAP.put(ORPHANS, "2");
        
        INITIAL_VALUE_MAP.put(OUTLINE_COLOR, "invert");
        INITIAL_VALUE_MAP.put(OUTLINE_STYLE, "none");
        INITIAL_VALUE_MAP.put(OUTLINE_WIDTH, "medium");
        
        INITIAL_VALUE_MAP.put(OVERFLOW, "visible");
        
        INITIAL_VALUE_MAP.put(PADDING_TOP, "0px");
        INITIAL_VALUE_MAP.put(PADDING_RIGHT, "0px");
        INITIAL_VALUE_MAP.put(PADDING_BOTTOM, "0px");
        INITIAL_VALUE_MAP.put(PADDING_LEFT, "0px");
        
        INITIAL_VALUE_MAP.put(PAGE, "auto");
        INITIAL_VALUE_MAP.put(PAGE_BREAK_AFTER, "auto");
        INITIAL_VALUE_MAP.put(PAGE_BREAK_BEFORE, "auto");
        INITIAL_VALUE_MAP.put(PAGE_BREAK_INSIDE, "auto");
        
        INITIAL_VALUE_MAP.put(POSITION, "static");
        INITIAL_VALUE_MAP.put(QUOTES, "none");// TODO: UA dependent
        
        INITIAL_VALUE_MAP.put(RIGHT, "auto");
        INITIAL_VALUE_MAP.put(SIZE, "auto");
        
        INITIAL_VALUE_MAP.put(TABLE_LAYOUT, "auto");
        
        INITIAL_VALUE_MAP.put(TEXT_ALIGN, "left");// TODO: UA dependent
        INITIAL_VALUE_MAP.put(TEXT_DECORATION, "none");
        INITIAL_VALUE_MAP.put(TEXT_INDENT, "0px");
        INITIAL_VALUE_MAP.put(TEXT_SHADOW, "none");
        INITIAL_VALUE_MAP.put(TEXT_TRANSFORM, "none");
        
        INITIAL_VALUE_MAP.put(TOP, "auto");
        
        INITIAL_VALUE_MAP.put(UNICODE_BIDI, "normal");
        INITIAL_VALUE_MAP.put(VERTICAL_ALIGN, "baseline");
        
        INITIAL_VALUE_MAP.put(VISIBILITY, "inherit");
        
        INITIAL_VALUE_MAP.put(WHITE_SPACE, "normal");
        INITIAL_VALUE_MAP.put(WIDOWS, "2");
        INITIAL_VALUE_MAP.put(WIDTH, "auto");
        INITIAL_VALUE_MAP.put(WORD_SPACING, "normal");
        INITIAL_VALUE_MAP.put(Z_INDEX, "auto");
        
        Iterator iter = ALL_PROPERTY_NAMES.values().iterator();
        ALL_PROPERTIES = new CSSName[ALL_PROPERTY_NAMES.size()];
        while(iter.hasNext()) {
            CSSName name = (CSSName) iter.next();
            ALL_PROPERTIES[name.FS_ID] = name;
        }
    }
}

/*
 * $Id$
 *
 * $Log$
 * Revision 1.19  2005/10/24 10:19:38  pdoubleya
 * CSSName FS_ID is now public and final, allowing direct access to the id, bypassing getAssignedID(); micro-optimization :); getAssignedID() and setAssignedID() have been removed. IdentValue string property is also final (as should have been).
 *
 * Revision 1.18  2005/10/21 18:10:07  pdoubleya
 * new arrays of properties.
 *
 * Revision 1.17  2005/10/21 13:02:20  pdoubleya
 * Changed to cache padding in RectPropertySet.
 *
 * Revision 1.16  2005/10/21 12:20:04  pdoubleya
 * Added array for margin side props.
 *
 * Revision 1.15  2005/10/20 20:48:03  pdoubleya
 * Updates for refactoring to style classes. CalculatedStyle now has lookup methods to cover all general cases, so propertyByName() is private, which means the backing classes for styling were able to be replaced.
 *
 * Revision 1.14  2005/06/27 00:05:44  tobega
 * Added support for fs-specific colspan and rowspan css properties. Created a modified version of cssparser
 *
 * Revision 1.13  2005/06/26 15:48:11  tobega
 * Converted to almost standard html4 default css, which shook out a bug: position should not inherit
 *
 * Revision 1.12  2005/06/21 08:36:00  pdoubleya
 * Fixed id assignment to scope primitive names to start of list, in static block.
 *
 * Revision 1.11  2005/06/21 08:23:13  pdoubleya
 * Added specific list and count of primitive, non shorthand properties, and CalculatedStyle now sizes array to this size.
 *
 * Revision 1.10  2005/06/19 23:02:37  tobega
 * Implemented calculation of minimum cell-widths.
 * Implemented border-spacing.
 *
 * Revision 1.9  2005/06/16 11:28:38  pdoubleya
 * Initial value for TOP
 *
 * Revision 1.8  2005/06/15 17:27:37  pdoubleya
 * Allow for custom properties (don't break).
 *
 * Revision 1.7  2005/06/03 23:06:21  tobega
 * Now uses value of "color" as initial value for "border-color" and rgb-triples are supported
 *
 * Revision 1.6  2005/03/24 23:18:20  pdoubleya
 * Comments.
 *
 * Revision 1.5  2005/01/29 20:22:21  pdoubleya
 * Clean/reformat code. Removed commented blocks, checked copyright.
 *
 * Revision 1.3  2005/01/24 19:01:07  pdoubleya
 * Mass checkin. Changed to use references to CSSName, which now has a Singleton instance for each property, everywhere property names were being used before. Removed commented code. Cascaded and Calculated style now store properties in arrays rather than maps, for optimization.
 *
 * Revision 1.2  2004/10/23 13:09:13  pdoubleya
 * Re-formatted using JavaStyle tool.
 * Cleaned imports to resolve wildcards
 * except for common packages
 * (java.io, java.util, etc).
 * Added CVS log comments at bottom.
 *
 *
 */

