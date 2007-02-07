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

import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import org.xhtmlrenderer.css.style.FSDerivedValue;
import org.xhtmlrenderer.css.style.derived.DerivedValueFactory;


/**
 * A CSSName is a Singleton representing a single CSS property name, like
 * border-width. The class declares a Singleton static instance for each CSS
 * Level 2 property. A CSSName instance has the property name available from the
 * {@link #toString()} method, as well as a unique (among all CSSName instances)
 * integer id ranging from 0...n instances, incremented by 1, available using
 * the final public int FS_ID (e.g. CSSName.COLOR.FS_ID).
 *
 * @author Patrick Wright
 */
public final class CSSName implements Comparable {
    /**
     * marker var, used for initialization
     */
    private static final Integer PRIMITIVE = new Integer(0);

    /**
     * marker var, used for initialization
     */
    private static final Integer SHORTHAND = new Integer(1);

    /**
     * marker var, used for initialization
     */
    private static final Integer INHERITS = new Integer(2);

    /**
     * marker var, used for initialization
     */
    private static final Integer NOT_INHERITED = new Integer(3);

    /**
     * Used to assing unique int id values to new CSSNames created in this class
     */
    private static int maxAssigned;

    /**
     * The CSS 2 property name, e.g. "border"
     */
    private final String propName;

    /**
     * A (String) initial value from the CSS 2.1 specification
     */
    private final String initialValue;

    /**
     * True if the property inherits by default, false if not inherited
     */
    private final boolean propertyInherits;
    
    private FSDerivedValue initialDerivedValue;


    /**
     * Unique integer id for a CSSName.
     */
    public final int FS_ID;

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
     * TODO: UA dependent
     */
    public final static CSSName COLOR =
            addProperty(
                    "color",
                    PRIMITIVE,
                    "black",
                    INHERITS
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName BACKGROUND_COLOR =
            addProperty(
                    "background-color",
                    PRIMITIVE,
                    "transparent",
                    NOT_INHERITED
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName BACKGROUND_IMAGE =
            addProperty(
                    "background-image",
                    PRIMITIVE,
                    "none",
                    NOT_INHERITED
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName BACKGROUND_REPEAT =
            addProperty(
                    "background-repeat",
                    PRIMITIVE,
                    "repeat",
                    NOT_INHERITED
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName BACKGROUND_ATTACHMENT =
            addProperty(
                    "background-attachment",
                    PRIMITIVE,
                    "scroll",
                    NOT_INHERITED
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName BACKGROUND_POSITION =
            addProperty(
                    "background-position",
                    PRIMITIVE,
                    "0% 0%",
                    NOT_INHERITED
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName BORDER_COLLAPSE =
            addProperty(
                    "border-collapse",
                    PRIMITIVE,
                    "separate",
                    INHERITS
            );

    /**
     * Unique CSSName instance for fictitious property.
     */
    public final static CSSName FS_BORDER_SPACING_HORIZONTAL =
            addProperty(
                    "-fs-border-spacing-horizontal",
                    PRIMITIVE,
                    "0",
                    NOT_INHERITED
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName FS_BORDER_SPACING_VERTICAL =
            addProperty(
                    "-fs-border-spacing-vertical",
                    PRIMITIVE,
                    "0",
                    NOT_INHERITED
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName FS_FLOW_TOP =
            addProperty(
                    "-fs-flow-top",
                    PRIMITIVE,
                    "none",
                    NOT_INHERITED
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName FS_FLOW_RIGHT =
            addProperty(
                    "-fs-flow-right",
                    PRIMITIVE,
                    "none",
                    NOT_INHERITED
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName FS_FLOW_BOTTOM =
            addProperty(
                    "-fs-flow-bottom",
                    PRIMITIVE,
                    "none",
                    NOT_INHERITED
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName FS_FLOW_LEFT =
            addProperty(
                    "-fs-flow-left",
                    PRIMITIVE,
                    "none",
                    NOT_INHERITED
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName FS_MOVE_TO_FLOW =
            addProperty(
                    "-fs-move-to-flow",
                    PRIMITIVE,
                    "none",
                    NOT_INHERITED
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName FS_PAGE_WIDTH =
            addProperty(
                    "-fs-page-width",
                    PRIMITIVE,
                    "auto",
                    NOT_INHERITED
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName FS_PAGE_HEIGHT =
            addProperty(
                    "-fs-page-height",
                    PRIMITIVE,
                    "auto",
                    NOT_INHERITED
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName FS_PAGE_ORIENTATION =
            addProperty(
                    "-fs-page-orientation",
                    PRIMITIVE,
                    "auto",
                    NOT_INHERITED
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName FS_TEXT_DECORATION_EXTENT =
            addProperty(
                    "-fs-text-decoration-extent",
                    PRIMITIVE,
                    "line",
                    NOT_INHERITED
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName BOTTOM =
            addProperty(
                    "bottom",
                    PRIMITIVE,
                    "auto",
                    NOT_INHERITED
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName CAPTION_SIDE =
            addProperty(
                    "caption-side",
                    PRIMITIVE,
                    "top",
                    INHERITS
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName CLEAR =
            addProperty(
                    "clear",
                    PRIMITIVE,
                    "none",
                    NOT_INHERITED
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName CLIP =
            addProperty(
                    "clip",
                    PRIMITIVE,
                    "auto",
                    NOT_INHERITED
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName CONTENT =
            addProperty(
                    "content",
                    PRIMITIVE,
                    "normal",
                    NOT_INHERITED
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName COUNTER_INCREMENT =
            addProperty(
                    "counter-increment",
                    PRIMITIVE,
                    "none",
                    NOT_INHERITED
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName COUNTER_RESET =
            addProperty(
                    "counter-reset",
                    PRIMITIVE,
                    "none",
                    NOT_INHERITED
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName CURSOR =
            addProperty(
                    "cursor",
                    PRIMITIVE,
                    "auto",
                    INHERITS
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName DIRECTION =
            addProperty(
                    "direction",
                    PRIMITIVE,
                    "ltr",
                    INHERITS
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName DISPLAY =
            addProperty(
                    "display",
                    PRIMITIVE,
                    "inline",
                    NOT_INHERITED
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName EMPTY_CELLS =
            addProperty(
                    "empty-cells",
                    PRIMITIVE,
                    "show",
                    INHERITS
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName FLOAT =
            addProperty(
                    "float",
                    PRIMITIVE,
                    "none",
                    NOT_INHERITED
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName FONT_STYLE =
            addProperty(
                    "font-style",
                    PRIMITIVE,
                    "normal",
                    INHERITS
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName FONT_VARIANT =
            addProperty(
                    "font-variant",
                    PRIMITIVE,
                    "normal",
                    INHERITS
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName FONT_WEIGHT =
            addProperty(
                    "font-weight",
                    PRIMITIVE,
                    "normal",
                    INHERITS
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName FONT_SIZE =
            addProperty(
                    "font-size",
                    PRIMITIVE,
                    "medium",
                    INHERITS
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName LINE_HEIGHT =
            addProperty(
                    "line-height",
                    PRIMITIVE,
                    "normal",
                    INHERITS
            );

    /**
     * Unique CSSName instance for CSS2 property.
     * TODO: UA dependent
     */
    public final static CSSName FONT_FAMILY =
            addProperty(
                    "font-family",
                    PRIMITIVE,
                    "\"Times New Roman\"",
                    INHERITS
            );

    /**
     * Unique CSSName instance for CSS2 property.
     * CLEAN: not in the spec
     */
    public final static CSSName FONT_SIZE_ADJUST =
            addProperty(
                    "font-size-adjust",
                    PRIMITIVE,
                    "none",
                    INHERITS
            );

    /**
     * Unique CSSName instance for CSS2 property.
     * CLEAN: not in the spec
     */
    public final static CSSName FONT_STRETCH =
            addProperty(
                    "font-stretch",
                    PRIMITIVE,
                    "normal",
                    INHERITS
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName FS_COLSPAN =
            addProperty(
                    "-fs-table-cell-colspan",
                    PRIMITIVE,
                    "1",
                    NOT_INHERITED
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName FS_ROWSPAN =
            addProperty(
                    "-fs-table-cell-rowspan",
                    PRIMITIVE,
                    "1",
                    NOT_INHERITED
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName HEIGHT =
            addProperty(
                    "height",
                    PRIMITIVE,
                    "auto",
                    NOT_INHERITED
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName LEFT =
            addProperty(
                    "left",
                    PRIMITIVE,
                    "auto",
                    NOT_INHERITED
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName LETTER_SPACING =
            addProperty(
                    "letter-spacing",
                    PRIMITIVE,
                    "normal",
                    INHERITS
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName LIST_STYLE_TYPE =
            addProperty(
                    "list-style-type",
                    PRIMITIVE,
                    "disc",
                    INHERITS
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName LIST_STYLE_POSITION =
            addProperty(
                    "list-style-position",
                    PRIMITIVE,
                    "outside",
                    INHERITS
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName LIST_STYLE_IMAGE =
            addProperty(
                    "list-style-image",
                    PRIMITIVE,
                    "none",
                    INHERITS
            );

    /**
     * Unique CSSName instance for CSS2 property.
     * CLEAN: not in spec
     */
    public final static CSSName MARKER_OFFSET =
            addProperty(
                    "marker-offset",
                    PRIMITIVE,
                    "auto",
                    NOT_INHERITED
            );

    /**
     * Unique CSSName instance for CSS2 property.
     * CLEAN: not in spec
     */
    public final static CSSName MARKS =
            addProperty(
                    "marks",
                    PRIMITIVE,
                    "none",
                    NOT_INHERITED
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName MAX_HEIGHT =
            addProperty(
                    "max-height",
                    PRIMITIVE,
                    "none",
                    NOT_INHERITED
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName MAX_WIDTH =
            addProperty(
                    "max-width",
                    PRIMITIVE,
                    "none",
                    NOT_INHERITED
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName MIN_HEIGHT =
            addProperty(
                    "min-height",
                    PRIMITIVE,
                    "0",
                    NOT_INHERITED
            );

    /**
     * Unique CSSName instance for CSS2 property.
     * TODO: UA dependent
     */
    public final static CSSName MIN_WIDTH =
            addProperty(
                    "min-width",
                    PRIMITIVE,
                    "0",
                    NOT_INHERITED
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName ORPHANS =
            addProperty(
                    "orphans",
                    PRIMITIVE,
                    "2",
                    INHERITS
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName OUTLINE_COLOR =
            addProperty(
                    "outline-color",
                    PRIMITIVE,
                    /* "invert", */ "black",  // XXX Wrong (but doesn't matter for now)
                    NOT_INHERITED
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName OUTLINE_STYLE =
            addProperty(
                    "outline-style",
                    PRIMITIVE,
                    "none",
                    NOT_INHERITED
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName OUTLINE_WIDTH =
            addProperty(
                    "outline-width",
                    PRIMITIVE,
                    "medium",
                    NOT_INHERITED
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName OVERFLOW =
            addProperty(
                    "overflow",
                    PRIMITIVE,
                    "visible",
                    NOT_INHERITED
            );

    /**
     * Unique CSSName instance for CSS2 property.
     * CLEAN: not in spec
     */
    public final static CSSName PAGE =
            addProperty(
                    "page",
                    PRIMITIVE,
                    "auto",
                    INHERITS
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName PAGE_BREAK_AFTER =
            addProperty(
                    "page-break-after",
                    PRIMITIVE,
                    "auto",
                    NOT_INHERITED
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName PAGE_BREAK_BEFORE =
            addProperty(
                    "page-break-before",
                    PRIMITIVE,
                    "auto",
                    NOT_INHERITED
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName PAGE_BREAK_INSIDE =
            addProperty(
                    "page-break-inside",
                    PRIMITIVE,
                    "auto",
                    INHERITS
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName POSITION =
            addProperty(
                    "position",
                    PRIMITIVE,
                    "static",
                    NOT_INHERITED
            );

    /**
     * Unique CSSName instance for CSS2 property.
     * TODO: UA dependent
     */
    public final static CSSName QUOTES =
            addProperty(
                    "quotes",
                    PRIMITIVE,
                    "none",
                    INHERITS
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName RIGHT =
            addProperty(
                    "right",
                    PRIMITIVE,
                    "auto",
                    NOT_INHERITED
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName TABLE_LAYOUT =
            addProperty(
                    "table-layout",
                    PRIMITIVE,
                    "auto",
                    NOT_INHERITED
            );

    /**
     * Unique CSSName instance for CSS2 property.
     * TODO: UA dependent
     */
    public final static CSSName TEXT_ALIGN =
            addProperty(
                    "text-align",
                    PRIMITIVE,
                    "left",
                    INHERITS
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName TEXT_DECORATION =
            addProperty(
                    "text-decoration",
                    PRIMITIVE,
                    "none",
                    NOT_INHERITED
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName TEXT_INDENT =
            addProperty(
                    "text-indent",
                    PRIMITIVE,
                    "0",
                    INHERITS
            );

    /**
     * Unique CSSName instance for CSS2 property.
     * CLEAN: not in spec
     */
    public final static CSSName TEXT_SHADOW =
            addProperty(
                    "text-shadow",
                    PRIMITIVE,
                    "none",
                    NOT_INHERITED
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName TEXT_TRANSFORM =
            addProperty(
                    "text-transform",
                    PRIMITIVE,
                    "none",
                    INHERITS
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName TOP =
            addProperty(
                    "top",
                    PRIMITIVE,
                    "auto",
                    NOT_INHERITED
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName UNICODE_BIDI =
            addProperty(
                    "unicode-bidi",
                    PRIMITIVE,
                    "normal",
                    NOT_INHERITED
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName VERTICAL_ALIGN =
            addProperty(
                    "vertical-align",
                    PRIMITIVE,
                    "baseline",
                    NOT_INHERITED
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName VISIBILITY =
            addProperty(
                    "visibility",
                    PRIMITIVE,
                    "visible",
                    INHERITS
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName WHITE_SPACE =
            addProperty(
                    "white-space",
                    PRIMITIVE,
                    "normal",
                    INHERITS
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName WIDOWS =
            addProperty(
                    "widows",
                    PRIMITIVE,
                    "2",
                    INHERITS
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName WIDTH =
            addProperty(
                    "width",
                    PRIMITIVE,
                    "auto",
                    NOT_INHERITED
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName WORD_SPACING =
            addProperty(
                    "word-spacing",
                    PRIMITIVE,
                    "normal",
                    INHERITS
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName Z_INDEX =
            addProperty(
                    "z-index",
                    PRIMITIVE,
                    "auto",
                    NOT_INHERITED
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName BORDER_COLOR_TOP =
            addProperty(
                    "border-top-color",
                    PRIMITIVE,
                    "=color",
                    NOT_INHERITED
            );
    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName BORDER_COLOR_RIGHT =
            addProperty(
                    "border-right-color",
                    PRIMITIVE,
                    "=color",
                    NOT_INHERITED
            );
    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName BORDER_COLOR_BOTTOM =
            addProperty(
                    "border-bottom-color",
                    PRIMITIVE,
                    "=color",
                    NOT_INHERITED
            );
    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName BORDER_COLOR_LEFT =
            addProperty(
                    "border-left-color",
                    PRIMITIVE,
                    "=color",
                    NOT_INHERITED
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName BORDER_STYLE_TOP =
            addProperty(
                    "border-top-style",
                    PRIMITIVE,
                    "none",
                    NOT_INHERITED
            );
    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName BORDER_STYLE_RIGHT =
            addProperty(
                    "border-right-style",
                    PRIMITIVE,
                    "none",
                    NOT_INHERITED
            );
    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName BORDER_STYLE_BOTTOM =
            addProperty(
                    "border-bottom-style",
                    PRIMITIVE,
                    "none",
                    NOT_INHERITED
            );
    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName BORDER_STYLE_LEFT =
            addProperty(
                    "border-left-style",
                    PRIMITIVE,
                    "none",
                    NOT_INHERITED
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName BORDER_WIDTH_TOP =
            addProperty(
                    "border-top-width",
                    PRIMITIVE,
                    "medium",
                    NOT_INHERITED
            );
    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName BORDER_WIDTH_RIGHT =
            addProperty(
                    "border-right-width",
                    PRIMITIVE,
                    "medium",
                    NOT_INHERITED
            );
    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName BORDER_WIDTH_BOTTOM =
            addProperty(
                    "border-bottom-width",
                    PRIMITIVE,
                    "medium",
                    NOT_INHERITED
            );
    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName BORDER_WIDTH_LEFT =
            addProperty(
                    "border-left-width",
                    PRIMITIVE,
                    "medium",
                    NOT_INHERITED
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName MARGIN_TOP =
            addProperty(
                    "margin-top",
                    PRIMITIVE,
                    "0",
                    NOT_INHERITED
            );
    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName MARGIN_RIGHT =
            addProperty(
                    "margin-right",
                    PRIMITIVE,
                    "0",
                    NOT_INHERITED
            );
    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName MARGIN_BOTTOM =
            addProperty(
                    "margin-bottom",
                    PRIMITIVE,
                    "0",
                    NOT_INHERITED
            );
    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName MARGIN_LEFT =
            addProperty(
                    "margin-left",
                    PRIMITIVE,
                    "0",
                    NOT_INHERITED
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName PADDING_TOP =
            addProperty(
                    "padding-top",
                    PRIMITIVE,
                    "0",
                    NOT_INHERITED
            );
    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName PADDING_RIGHT =
            addProperty(
                    "padding-right",
                    PRIMITIVE,
                    "0",
                    NOT_INHERITED
            );
    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName PADDING_BOTTOM =
            addProperty(
                    "padding-bottom",
                    PRIMITIVE,
                    "0",
                    NOT_INHERITED
            );
    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName PADDING_LEFT =
            addProperty(
                    "padding-left",
                    PRIMITIVE,
                    "0",
                    NOT_INHERITED
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName BACKGROUND_SHORTHAND =
            addProperty(
                    "background",
                    SHORTHAND,
                    "transparent none repeat scroll 0% 0%",
                    NOT_INHERITED
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName BORDER_WIDTH_SHORTHAND =
            addProperty(
                    "border-width",
                    SHORTHAND,
                    "medium",
                    NOT_INHERITED
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName BORDER_STYLE_SHORTHAND =
            addProperty(
                    "border-style",
                    SHORTHAND,
                    "none",
                    NOT_INHERITED
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName BORDER_SHORTHAND =
            addProperty(
                    "border",
                    SHORTHAND,
                    "medium none black",
                    NOT_INHERITED
            );
    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName BORDER_TOP_SHORTHAND =
            addProperty(
                    "border-top",
                    SHORTHAND,
                    "medium none black",
                    NOT_INHERITED
            );
    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName BORDER_RIGHT_SHORTHAND =
            addProperty(
                    "border-right",
                    SHORTHAND,
                    "medium none black",
                    NOT_INHERITED
            );
    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName BORDER_BOTTOM_SHORTHAND =
            addProperty(
                    "border-bottom",
                    SHORTHAND,
                    "medium none black",
                    NOT_INHERITED
            );
    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName BORDER_LEFT_SHORTHAND =
            addProperty(
                    "border-left",
                    SHORTHAND,
                    "medium none black",
                    NOT_INHERITED
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName BORDER_COLOR_SHORTHAND =
            addProperty(
                    "border-color",
                    SHORTHAND,
                    "black",
                    NOT_INHERITED
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName BORDER_SPACING =
            addProperty(
                    "border-spacing",
                    SHORTHAND,
                    "0",
                    INHERITS
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName FONT_SHORTHAND =
            addProperty(
                    "font",
                    SHORTHAND,
                    "",
                    INHERITS
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName LIST_STYLE_SHORTHAND =
            addProperty(
                    "list-style",
                    SHORTHAND,
                    "disc outside none",
                    INHERITS
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName MARGIN_SHORTHAND =
            addProperty(
                    "margin",
                    SHORTHAND,
                    "0",
                    NOT_INHERITED
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName OUTLINE_SHORTHAND =
            addProperty(
                    "outline",
                    SHORTHAND,
                    "invert none medium",
                    NOT_INHERITED
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName PADDING_SHORTHAND =
            addProperty(
                    "padding",
                    SHORTHAND,
                    "0",
                    NOT_INHERITED
            );


    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName SIZE_SHORTHAND =
            addProperty(
                    "size",
                    SHORTHAND,
                    "auto",
                    NOT_INHERITED
            );

    public final static CSSName[] MARGIN_SIDE_PROPERTIES =
            new CSSName[]{
                    CSSName.MARGIN_TOP,
                    CSSName.MARGIN_RIGHT,
                    CSSName.MARGIN_BOTTOM,
                    CSSName.MARGIN_LEFT
            };

    public final static CSSName[] PADDING_SIDE_PROPERTIES =
            new CSSName[]{
                    CSSName.PADDING_TOP,
                    CSSName.PADDING_RIGHT,
                    CSSName.PADDING_BOTTOM,
                    CSSName.PADDING_LEFT
            };

    public final static CSSName[] BORDER_SIDE_PROPERTIES =
            new CSSName[]{
                    CSSName.BORDER_WIDTH_TOP,
                    CSSName.BORDER_WIDTH_RIGHT,
                    CSSName.BORDER_WIDTH_BOTTOM,
                    CSSName.BORDER_WIDTH_LEFT
            };

    public final static CSSName[] BORDER_STYLE_PROPERTIES =
            new CSSName[]{
                    CSSName.BORDER_STYLE_TOP,
                    CSSName.BORDER_STYLE_RIGHT,
                    CSSName.BORDER_STYLE_BOTTOM,
                    CSSName.BORDER_STYLE_LEFT
            };

    public final static CSSName[] BORDER_COLOR_PROPERTIES =
            new CSSName[]{
                    CSSName.BORDER_COLOR_TOP,
                    CSSName.BORDER_COLOR_RIGHT,
                    CSSName.BORDER_COLOR_BOTTOM,
                    CSSName.BORDER_COLOR_LEFT
            };

    /**
     * Constructor for the CSSName object
     *
     * @param propName     PARAM
     * @param initialValue
     * @param inherits
     */
    private CSSName(String propName, String initialValue, boolean inherits, Object type) {
        this.propName = propName;
        this.FS_ID = CSSName.maxAssigned++;
        this.initialValue = initialValue;
        this.propertyInherits = inherits;
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
        return ALL_PRIMITIVE_PROPERTY_NAMES.size();
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
    // CLEAN: method is now unnecessary
    public final static boolean propertyInherits(CSSName cssName) {
        return cssName.propertyInherits;
    }

    /**
     * Returns the initial value of the named property, according to the CSS2
     * spec, as a String. Casting must be taken care of by the caller, as there
     * is too much variation in value-types.
     *
     * @param cssName PARAM
     * @return Returns
     */
    // CLEAN: method is now unnecessary
    public final static String initialValue(CSSName cssName) {
        return cssName.initialValue;
    }
    
    public final static FSDerivedValue initialDerivedValue(CSSName cssName) {
        return cssName.initialDerivedValue;
    }

    /**
     * Gets the byPropertyName attribute of the CSSName class
     *
     * @param propName PARAM
     * @return The byPropertyName value
     */
    public static CSSName getByPropertyName(String propName) {

        return (CSSName) ALL_PROPERTY_NAMES.get(propName);
    }

    public static CSSName getByID(int id) {
        return ALL_PROPERTIES[id];
    }

    /**
     * Adds a feature to the Property attribute of the CSSName class
     *
     * @param propName     The feature to be added to the Property attribute
     * @param type
     * @param initialValue
     * @param inherit
     * @return Returns
     */
    private final static synchronized CSSName addProperty(
            String propName,
            Object type,
            String initialValue, Object inherit
    ) {
        CSSName cssName = new CSSName(propName, initialValue, (inherit == INHERITS), type);

        ALL_PROPERTY_NAMES.put(propName, cssName);

        if (type == PRIMITIVE) {
            ALL_PRIMITIVE_PROPERTY_NAMES.put(propName, cssName);
        }

        return cssName;
    }

    static {
        Iterator iter = ALL_PROPERTY_NAMES.values().iterator();
        ALL_PROPERTIES = new CSSName[ALL_PROPERTY_NAMES.size()];
        while (iter.hasNext()) {
            CSSName name = (CSSName) iter.next();
            ALL_PROPERTIES[name.FS_ID] = name;
        }
    }
    
    static {
        for (Iterator i = ALL_PRIMITIVE_PROPERTY_NAMES.values().iterator(); i.hasNext(); ) {
            CSSName cssName = (CSSName)i.next();
            if (cssName.initialValue.charAt(0) != '=') {
                String convertedValue = Idents.convertIdent(cssName, cssName.initialValue);

                short valueType = ValueConstants.guessType(convertedValue);

                cssName.initialDerivedValue = DerivedValueFactory.newDerivedValue(null,
                        cssName,
                        valueType,
                        convertedValue,
                        convertedValue,
                        null);
            }
        }
    }

    //Assumed to be consistent with equals because CSSName is in essence an enum
    public int compareTo(Object object) {
        if (object == null) throw new NullPointerException();//required by Comparable
        return FS_ID - ((CSSName) object).FS_ID;//will throw ClassCastException according to Comparable if not a CSSName
    }
    
    public int hashCode() {
        return FS_ID;
    }


}

/*
 * $Id$
 *
 * $Log$
 * Revision 1.25  2007/02/07 16:33:35  peterbrant
 * Initial commit of rewritten table support and associated refactorings
 *
 * Revision 1.24  2006/07/03 23:37:09  tobega
 * fixed Comparable so that the previous TreeMap change works. TreeMap does not seem to affect performance but assures that CalculatedStyles get shared properly as intended.
 *
 * Revision 1.23  2006/01/03 17:04:51  peterbrant
 * Many pagination bug fixes / Add ability to position absolute boxes in margin area
 *
 * Revision 1.22  2005/12/28 00:50:51  peterbrant
 * Continue ripping out first try at pagination / Minor method name refactoring
 *
 * Revision 1.21  2005/12/13 20:46:10  peterbrant
 * Improve list support (implement list-style-position: inside, marker "sticks" to first line box even if there are other block boxes in between, plus other minor fixes) / Experimental support for optionally extending text decorations to box edge vs line edge
 *
 * Revision 1.20  2005/10/25 15:07:05  pdoubleya
 * Reviewed all initial values, cleaned code to remove use of unnecessary maps and lists. Reformatted for readability.
 *
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

