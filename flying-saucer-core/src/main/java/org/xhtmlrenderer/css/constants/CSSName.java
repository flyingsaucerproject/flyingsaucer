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

import org.xhtmlrenderer.css.parser.CSSErrorHandler;
import org.xhtmlrenderer.css.parser.CSSParser;
import org.xhtmlrenderer.css.parser.PropertyValue;
import org.xhtmlrenderer.css.parser.property.BackgroundPropertyBuilder;
import org.xhtmlrenderer.css.parser.property.BorderPropertyBuilders;
import org.xhtmlrenderer.css.parser.property.BorderSpacingPropertyBuilder;
import org.xhtmlrenderer.css.parser.property.ContentPropertyBuilder;
import org.xhtmlrenderer.css.parser.property.CounterPropertyBuilder;
import org.xhtmlrenderer.css.parser.property.FontPropertyBuilder;
import org.xhtmlrenderer.css.parser.property.ListStylePropertyBuilder;
import org.xhtmlrenderer.css.parser.property.OneToFourPropertyBuilders;
import org.xhtmlrenderer.css.parser.property.PrimitivePropertyBuilders;
import org.xhtmlrenderer.css.parser.property.PropertyBuilder;
import org.xhtmlrenderer.css.parser.property.QuotesPropertyBuilder;
import org.xhtmlrenderer.css.parser.property.SizePropertyBuilder;
import org.xhtmlrenderer.css.sheet.StylesheetInfo;
import org.xhtmlrenderer.css.style.FSDerivedValue;
import org.xhtmlrenderer.css.style.derived.DerivedValueFactory;
import org.xhtmlrenderer.util.XRLog;


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

    private final boolean implemented;

    private final PropertyBuilder builder;

    /**
     * Unique integer id for a CSSName.
     */
    public final int FS_ID;

    /**
     * Map of all CSS properties
     */
    private static final CSSName[] ALL_PROPERTIES;

    /**
     * Map of all CSS properties
     */
    private static final Map ALL_PROPERTY_NAMES = new TreeMap();

    /**
     * Map of all non-shorthand CSS properties
     */
    private static final Map ALL_PRIMITIVE_PROPERTY_NAMES = new TreeMap();

    /**
     * Unique CSSName instance for CSS2 property.
     * TODO: UA dependent
     */
    public final static CSSName COLOR =
            addProperty(
                    "color",
                    PRIMITIVE,
                    "black",
                    INHERITS,
                    new PrimitivePropertyBuilders.Color()
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName BACKGROUND_COLOR =
            addProperty(
                    "background-color",
                    PRIMITIVE,
                    "transparent",
                    NOT_INHERITED,
                    new PrimitivePropertyBuilders.BackgroundColor()
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName BACKGROUND_IMAGE =
            addProperty(
                    "background-image",
                    PRIMITIVE,
                    "none",
                    NOT_INHERITED,
                    new PrimitivePropertyBuilders.BackgroundImage()
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName BACKGROUND_REPEAT =
            addProperty(
                    "background-repeat",
                    PRIMITIVE,
                    "repeat",
                    NOT_INHERITED,
                    new PrimitivePropertyBuilders.BackgroundRepeat()
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName BACKGROUND_ATTACHMENT =
            addProperty(
                    "background-attachment",
                    PRIMITIVE,
                    "scroll",
                    NOT_INHERITED,
                    new PrimitivePropertyBuilders.BackgroundAttachment()
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName BACKGROUND_POSITION =
            addProperty(
                    "background-position",
                    PRIMITIVE,
                    "0% 0%",
                    NOT_INHERITED,
                    new PrimitivePropertyBuilders.BackgroundPosition()
            );

    public final static CSSName BACKGROUND_SIZE =
        addProperty(
                "background-size",
                PRIMITIVE,
                "auto auto",
                NOT_INHERITED,
                new PrimitivePropertyBuilders.BackgroundSize()
        );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName BORDER_COLLAPSE =
            addProperty(
                    "border-collapse",
                    PRIMITIVE,
                    "separate",
                    INHERITS,
                    new PrimitivePropertyBuilders.BorderCollapse()
            );

    /**
     * Unique CSSName instance for fictitious property.
     */
    public final static CSSName FS_BORDER_SPACING_HORIZONTAL =
            addProperty(
                    "-fs-border-spacing-horizontal",
                    PRIMITIVE,
                    "0",
                    NOT_INHERITED,
                    new PrimitivePropertyBuilders.FSBorderSpacingHorizontal()
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName FS_BORDER_SPACING_VERTICAL =
            addProperty(
                    "-fs-border-spacing-vertical",
                    PRIMITIVE,
                    "0",
                    NOT_INHERITED,
                    new PrimitivePropertyBuilders.FSBorderSpacingVertical()
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName FS_DYNAMIC_AUTO_WIDTH =
            addProperty(
                    "-fs-dynamic-auto-width",
                    PRIMITIVE,
                    "static",
                    NOT_INHERITED,
                    new PrimitivePropertyBuilders.FSDynamicAutoWidth()
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName FS_FONT_METRIC_SRC =
            addProperty(
                    "-fs-font-metric-src",
                    PRIMITIVE,
                    "none",
                    NOT_INHERITED,
                    new PrimitivePropertyBuilders.FSFontMetricSrc()
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName FS_KEEP_WITH_INLINE =
            addProperty(
                    "-fs-keep-with-inline",
                    PRIMITIVE,
                    "auto",
                    NOT_INHERITED,
                    new PrimitivePropertyBuilders.FSKeepWithInline()
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName FS_PAGE_WIDTH =
            addProperty(
                    "-fs-page-width",
                    PRIMITIVE,
                    "auto",
                    NOT_INHERITED,
                    new PrimitivePropertyBuilders.FSPageWidth()
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName FS_PAGE_HEIGHT =
            addProperty(
                    "-fs-page-height",
                    PRIMITIVE,
                    "auto",
                    NOT_INHERITED,
                    new PrimitivePropertyBuilders.FSPageHeight()
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName FS_PAGE_SEQUENCE =
            addProperty(
                    "-fs-page-sequence",
                    PRIMITIVE,
                    "auto",
                    NOT_INHERITED,
                    new PrimitivePropertyBuilders.FSPageSequence()
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName FS_PDF_FONT_EMBED =
            addProperty(
                    "-fs-pdf-font-embed",
                    PRIMITIVE,
                    "auto",
                    NOT_INHERITED,
                    new PrimitivePropertyBuilders.FSPDFFontEmbed()
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName FS_PDF_FONT_ENCODING =
            addProperty(
                    "-fs-pdf-font-encoding",
                    PRIMITIVE,
                    "Cp1252",
                    NOT_INHERITED,
                    new PrimitivePropertyBuilders.FSPDFFontEncoding()
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName FS_PAGE_ORIENTATION =
            addProperty(
                    "-fs-page-orientation",
                    PRIMITIVE,
                    "auto",
                    NOT_INHERITED,
                    new PrimitivePropertyBuilders.FSPageOrientation()
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName FS_TABLE_PAGINATE =
            addProperty(
                    "-fs-table-paginate",
                    PRIMITIVE,
                    "auto",
                    NOT_INHERITED,
                    new PrimitivePropertyBuilders.FSTablePaginate()
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName FS_TEXT_DECORATION_EXTENT =
            addProperty(
                    "-fs-text-decoration-extent",
                    PRIMITIVE,
                    "line",
                    NOT_INHERITED,
                    new PrimitivePropertyBuilders.FSTextDecorationExtent()
            );

    /**
     * Used for forcing images to scale to a certain width
     */
    public final static CSSName FS_FIT_IMAGES_TO_WIDTH =
        addProperty(
                "-fs-fit-images-to-width",
                PRIMITIVE,
                "auto",
                NOT_INHERITED,
                new PrimitivePropertyBuilders.FSFitImagesToWidth()
        );

    /**
     * Used to control creation of named destinations for boxes having the id attribute set.
     */
    public final static CSSName FS_NAMED_DESTINATION =
            addProperty(
                    "-fs-named-destination",
                    PRIMITIVE,
                    "none",
                    NOT_INHERITED,
                    new PrimitivePropertyBuilders.FSNamedDestination()
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName BOTTOM =
            addProperty(
                    "bottom",
                    PRIMITIVE,
                    "auto",
                    NOT_INHERITED,
                    new PrimitivePropertyBuilders.Bottom()
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName CAPTION_SIDE =
            addProperty(
                    "caption-side",
                    PRIMITIVE,
                    "top",
                    INHERITS,
                    new PrimitivePropertyBuilders.CaptionSide()
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName CLEAR =
            addProperty(
                    "clear",
                    PRIMITIVE,
                    "none",
                    NOT_INHERITED,
                    new PrimitivePropertyBuilders.Clear()
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName CLIP =
            addProperty(
                    "clip",
                    PRIMITIVE,
                    "auto",
                    NOT_INHERITED,
                    false,
                    null
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName CONTENT =
            addProperty(
                    "content",
                    PRIMITIVE,
                    "normal",
                    NOT_INHERITED,
                    new ContentPropertyBuilder()
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName COUNTER_INCREMENT =
            addProperty(
                    "counter-increment",
                    PRIMITIVE,
                    "none",
                    NOT_INHERITED,
                    true,
                    new CounterPropertyBuilder.CounterIncrement()
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName COUNTER_RESET =
            addProperty(
                    "counter-reset",
                    PRIMITIVE,
                    "none",
                    NOT_INHERITED,
                    true,
                    new CounterPropertyBuilder.CounterReset()
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName CURSOR =
            addProperty(
                    "cursor",
                    PRIMITIVE,
                    "auto",
                    INHERITS,
                    true,
                    new PrimitivePropertyBuilders.Cursor()
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName DIRECTION =
            addProperty(
                    "direction",
                    PRIMITIVE,
                    "ltr",
                    INHERITS,
                    false,
                    null
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName DISPLAY =
            addProperty(
                    "display",
                    PRIMITIVE,
                    "inline",
                    NOT_INHERITED,
                    new PrimitivePropertyBuilders.Display()
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName EMPTY_CELLS =
            addProperty(
                    "empty-cells",
                    PRIMITIVE,
                    "show",
                    INHERITS,
                    new PrimitivePropertyBuilders.EmptyCells()
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName FLOAT =
            addProperty(
                    "float",
                    PRIMITIVE,
                    "none",
                    NOT_INHERITED,
                    new PrimitivePropertyBuilders.Float()
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName FONT_STYLE =
            addProperty(
                    "font-style",
                    PRIMITIVE,
                    "normal",
                    INHERITS,
                    new PrimitivePropertyBuilders.FontStyle()
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName FONT_VARIANT =
            addProperty(
                    "font-variant",
                    PRIMITIVE,
                    "normal",
                    INHERITS,
                    new PrimitivePropertyBuilders.FontVariant()
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName FONT_WEIGHT =
            addProperty(
                    "font-weight",
                    PRIMITIVE,
                    "normal",
                    INHERITS,
                    new PrimitivePropertyBuilders.FontWeight()
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName FONT_SIZE =
            addProperty(
                    "font-size",
                    PRIMITIVE,
                    "medium",
                    INHERITS,
                    new PrimitivePropertyBuilders.FontSize()
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName LINE_HEIGHT =
            addProperty(
                    "line-height",
                    PRIMITIVE,
                    "normal",
                    INHERITS,
                    new PrimitivePropertyBuilders.LineHeight()
            );

    /**
     * Unique CSSName instance for CSS2 property.
     * TODO: UA dependent
     */
    public final static CSSName FONT_FAMILY =
            addProperty(
                    "font-family",
                    PRIMITIVE,
                    "serif",
                    INHERITS,
                    new PrimitivePropertyBuilders.FontFamily()
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName FS_COLSPAN =
            addProperty(
                    "-fs-table-cell-colspan",
                    PRIMITIVE,
                    "1",
                    NOT_INHERITED,
                    new PrimitivePropertyBuilders.FSTableCellColspan()
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName FS_ROWSPAN =
            addProperty(
                    "-fs-table-cell-rowspan",
                    PRIMITIVE,
                    "1",
                    NOT_INHERITED,
                    new PrimitivePropertyBuilders.FSTableCellRowspan()
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName HEIGHT =
            addProperty(
                    "height",
                    PRIMITIVE,
                    "auto",
                    NOT_INHERITED,
                    new PrimitivePropertyBuilders.Height()
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName LEFT =
            addProperty(
                    "left",
                    PRIMITIVE,
                    "auto",
                    NOT_INHERITED,
                    new PrimitivePropertyBuilders.Left()
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName LETTER_SPACING =
            addProperty(
                    "letter-spacing",
                    PRIMITIVE,
                    "normal",
                    INHERITS,
                    true,
                    new PrimitivePropertyBuilders.LetterSpacing()
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName LIST_STYLE_TYPE =
            addProperty(
                    "list-style-type",
                    PRIMITIVE,
                    "disc",
                    INHERITS,
                    new PrimitivePropertyBuilders.ListStyleType()
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName LIST_STYLE_POSITION =
            addProperty(
                    "list-style-position",
                    PRIMITIVE,
                    "outside",
                    INHERITS,
                    new PrimitivePropertyBuilders.ListStylePosition()
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName LIST_STYLE_IMAGE =
            addProperty(
                    "list-style-image",
                    PRIMITIVE,
                    "none",
                    INHERITS,
                    new PrimitivePropertyBuilders.ListStyleImage()
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName MAX_HEIGHT =
            addProperty(
                    "max-height",
                    PRIMITIVE,
                    "none",
                    NOT_INHERITED,
                    new PrimitivePropertyBuilders.MaxHeight()
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName MAX_WIDTH =
            addProperty(
                    "max-width",
                    PRIMITIVE,
                    "none",
                    NOT_INHERITED,
                    new PrimitivePropertyBuilders.MaxWidth()
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName MIN_HEIGHT =
            addProperty(
                    "min-height",
                    PRIMITIVE,
                    "0",
                    NOT_INHERITED,
                    new PrimitivePropertyBuilders.MinHeight()
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
                    NOT_INHERITED,
                    new PrimitivePropertyBuilders.MinWidth()
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName ORPHANS =
            addProperty(
                    "orphans",
                    PRIMITIVE,
                    "2",
                    INHERITS,
                    true,
                    new PrimitivePropertyBuilders.Orphans()
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName OUTLINE_COLOR =
            addProperty(
                    "outline-color",
                    PRIMITIVE,
                    /* "invert", */ "black",  // XXX Wrong (but doesn't matter for now)
                    NOT_INHERITED,
                    false,
                    null
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName OUTLINE_STYLE =
            addProperty(
                    "outline-style",
                    PRIMITIVE,
                    "none",
                    NOT_INHERITED,
                    false,
                    null
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName OUTLINE_WIDTH =
            addProperty(
                    "outline-width",
                    PRIMITIVE,
                    "medium",
                    NOT_INHERITED,
                    false,
                    null
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName OVERFLOW =
            addProperty(
                    "overflow",
                    PRIMITIVE,
                    "visible",
                    NOT_INHERITED,
                    new PrimitivePropertyBuilders.Overflow()
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName PAGE =
            addProperty(
                    "page",
                    PRIMITIVE,
                    "auto",
                    INHERITS,
                    new PrimitivePropertyBuilders.Page()
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName PAGE_BREAK_AFTER =
            addProperty(
                    "page-break-after",
                    PRIMITIVE,
                    "auto",
                    NOT_INHERITED,
                    new PrimitivePropertyBuilders.PageBreakAfter()
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName PAGE_BREAK_BEFORE =
            addProperty(
                    "page-break-before",
                    PRIMITIVE,
                    "auto",
                    NOT_INHERITED,
                    new PrimitivePropertyBuilders.PageBreakBefore()
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName PAGE_BREAK_INSIDE =
            addProperty(
                    "page-break-inside",
                    PRIMITIVE,
                    "auto",
                    INHERITS,
                    new PrimitivePropertyBuilders.PageBreakInside()
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName POSITION =
            addProperty(
                    "position",
                    PRIMITIVE,
                    "static",
                    NOT_INHERITED,
                    new PrimitivePropertyBuilders.Position()
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
                    INHERITS,
                    new QuotesPropertyBuilder()
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName RIGHT =
            addProperty(
                    "right",
                    PRIMITIVE,
                    "auto",
                    NOT_INHERITED,
                    new PrimitivePropertyBuilders.Right()
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName SRC =
            addProperty(
                    "src",
                    PRIMITIVE,
                    "none",
                    NOT_INHERITED,
                    new PrimitivePropertyBuilders.Src()
            );

    /**
     * Used for controlling tab size in pre tags. See http://dev.w3.org/csswg/css3-text/#tab-size
     */
    public final static CSSName TAB_SIZE =
            addProperty(
                    "tab-size",
                    PRIMITIVE,
                    "8",
                    INHERITS,
                    new PrimitivePropertyBuilders.TabSize()
                    );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName TABLE_LAYOUT =
            addProperty(
                    "table-layout",
                    PRIMITIVE,
                    "auto",
                    NOT_INHERITED,
                    new PrimitivePropertyBuilders.TableLayout()
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
                    INHERITS,
                    new PrimitivePropertyBuilders.TextAlign()
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName TEXT_DECORATION =
            addProperty(
                    "text-decoration",
                    PRIMITIVE,
                    "none",
                    NOT_INHERITED,
                    new PrimitivePropertyBuilders.TextDecoration()
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName TEXT_INDENT =
            addProperty(
                    "text-indent",
                    PRIMITIVE,
                    "0",
                    INHERITS,
                    new PrimitivePropertyBuilders.TextIndent()
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName TEXT_TRANSFORM =
            addProperty(
                    "text-transform",
                    PRIMITIVE,
                    "none",
                    INHERITS,
                    new PrimitivePropertyBuilders.TextTransform()
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName TOP =
            addProperty(
                    "top",
                    PRIMITIVE,
                    "auto",
                    NOT_INHERITED,
                    new PrimitivePropertyBuilders.Top()
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName UNICODE_BIDI =
            addProperty(
                    "unicode-bidi",
                    PRIMITIVE,
                    "normal",
                    NOT_INHERITED,
                    false,
                    null
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName VERTICAL_ALIGN =
            addProperty(
                    "vertical-align",
                    PRIMITIVE,
                    "baseline",
                    NOT_INHERITED,
                    new PrimitivePropertyBuilders.VerticalAlign()
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName VISIBILITY =
            addProperty(
                    "visibility",
                    PRIMITIVE,
                    "visible",
                    INHERITS,
                    new PrimitivePropertyBuilders.Visibility()
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName WHITE_SPACE =
            addProperty(
                    "white-space",
                    PRIMITIVE,
                    "normal",
                    INHERITS,
                    new PrimitivePropertyBuilders.WhiteSpace()
            );

    /**
     * Unique CSSName instance for CSS3 property.
     */
    public final static CSSName WORD_WRAP =
            addProperty(
                    "word-wrap",
                    PRIMITIVE,
                    "normal",
                    INHERITS,
                    new PrimitivePropertyBuilders.WordWrap()
            );
    
    /**
     * Unique CSSName instance for CSS3 property.
     */
    public final static CSSName HYPHENS =
            addProperty(
                    "hyphens",
                    PRIMITIVE,
                    "none",
                    INHERITS,
                    new PrimitivePropertyBuilders.Hyphens()
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName WIDOWS =
            addProperty(
                    "widows",
                    PRIMITIVE,
                    "2",
                    INHERITS,
                    true,
                    new PrimitivePropertyBuilders.Widows()
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName WIDTH =
            addProperty(
                    "width",
                    PRIMITIVE,
                    "auto",
                    NOT_INHERITED,
                    new PrimitivePropertyBuilders.Width()
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName WORD_SPACING =
            addProperty(
                    "word-spacing",
                    PRIMITIVE,
                    "normal",
                    INHERITS,
                    true,
                    new PrimitivePropertyBuilders.WordSpacing()
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName Z_INDEX =
            addProperty(
                    "z-index",
                    PRIMITIVE,
                    "auto",
                    NOT_INHERITED,
                    new PrimitivePropertyBuilders.ZIndex()
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName BORDER_TOP_COLOR =
            addProperty(
                    "border-top-color",
                    PRIMITIVE,
                    "=color",
                    NOT_INHERITED,
                    new PrimitivePropertyBuilders.BorderTopColor()
            );
    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName BORDER_RIGHT_COLOR =
            addProperty(
                    "border-right-color",
                    PRIMITIVE,
                    "=color",
                    NOT_INHERITED,
                    new PrimitivePropertyBuilders.BorderLeftColor()
            );
    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName BORDER_BOTTOM_COLOR =
            addProperty(
                    "border-bottom-color",
                    PRIMITIVE,
                    "=color",
                    NOT_INHERITED,
                    new PrimitivePropertyBuilders.BorderBottomColor()
            );
    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName BORDER_LEFT_COLOR =
            addProperty(
                    "border-left-color",
                    PRIMITIVE,
                    "=color",
                    NOT_INHERITED,
                    new PrimitivePropertyBuilders.BorderLeftColor()
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName BORDER_TOP_STYLE =
            addProperty(
                    "border-top-style",
                    PRIMITIVE,
                    "none",
                    NOT_INHERITED,
                    new PrimitivePropertyBuilders.BorderTopStyle()
            );
    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName BORDER_RIGHT_STYLE =
            addProperty(
                    "border-right-style",
                    PRIMITIVE,
                    "none",
                    NOT_INHERITED,
                    new PrimitivePropertyBuilders.BorderRightStyle()
            );
    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName BORDER_BOTTOM_STYLE =
            addProperty(
                    "border-bottom-style",
                    PRIMITIVE,
                    "none",
                    NOT_INHERITED,
                    new PrimitivePropertyBuilders.BorderBottomStyle()
            );
    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName BORDER_LEFT_STYLE =
            addProperty(
                    "border-left-style",
                    PRIMITIVE,
                    "none",
                    NOT_INHERITED,
                    new PrimitivePropertyBuilders.BorderLeftStyle()
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName BORDER_TOP_WIDTH =
            addProperty(
                    "border-top-width",
                    PRIMITIVE,
                    "medium",
                    NOT_INHERITED,
                    new PrimitivePropertyBuilders.BorderTopWidth()
            );
    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName BORDER_RIGHT_WIDTH =
            addProperty(
                    "border-right-width",
                    PRIMITIVE,
                    "medium",
                    NOT_INHERITED,
                    new PrimitivePropertyBuilders.BorderRightWidth()
            );
    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName BORDER_BOTTOM_WIDTH =
            addProperty(
                    "border-bottom-width",
                    PRIMITIVE,
                    "medium",
                    NOT_INHERITED,
                    new PrimitivePropertyBuilders.BorderBottomWidth()
            );
    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName BORDER_LEFT_WIDTH =
            addProperty(
                    "border-left-width",
                    PRIMITIVE,
                    "medium",
                    NOT_INHERITED,
                    new PrimitivePropertyBuilders.BorderLeftWidth()
            );

    /**
     * Unique CSSName instance for CSS3 property.
     */
    public final static CSSName BORDER_TOP_LEFT_RADIUS =
            addProperty(
                    "border-top-left-radius",
                    PRIMITIVE,
                    "0 0",
                    NOT_INHERITED,
                    true,
                    new PrimitivePropertyBuilders.BorderTopLeftRadius()
            );

    /**
     * Unique CSSName instance for CSS3 property.
     */
    public final static CSSName BORDER_TOP_RIGHT_RADIUS =
            addProperty(
                    "border-top-right-radius",
                    PRIMITIVE,
                    "0 0",
                    NOT_INHERITED,
                    true,
                    new PrimitivePropertyBuilders.BorderTopRightRadius()
            );

    /**
     * Unique CSSName instance for CSS3 property.
     */
    public final static CSSName BORDER_BOTTOM_RIGHT_RADIUS =
            addProperty(
                    "border-bottom-right-radius",
                    PRIMITIVE,
                    "0 0",
                    NOT_INHERITED,
                    true,
                    new PrimitivePropertyBuilders.BorderBottomRightRadius()
            );

    /**
     * Unique CSSName instance for CSS3 property.
     */
    public final static CSSName BORDER_BOTTOM_LEFT_RADIUS =
            addProperty(
                    "border-bottom-left-radius",
                    PRIMITIVE,
                    "0 0",
                    NOT_INHERITED,
                    true,
                    new PrimitivePropertyBuilders.BorderBottomLeftRadius()
            );
    
    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName MARGIN_TOP =
            addProperty(
                    "margin-top",
                    PRIMITIVE,
                    "0",
                    NOT_INHERITED,
                    new PrimitivePropertyBuilders.MarginTop()
            );
    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName MARGIN_RIGHT =
            addProperty(
                    "margin-right",
                    PRIMITIVE,
                    "0",
                    NOT_INHERITED,
                    new PrimitivePropertyBuilders.MarginRight()
            );
    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName MARGIN_BOTTOM =
            addProperty(
                    "margin-bottom",
                    PRIMITIVE,
                    "0",
                    NOT_INHERITED,
                    new PrimitivePropertyBuilders.MarginBottom()
            );
    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName MARGIN_LEFT =
            addProperty(
                    "margin-left",
                    PRIMITIVE,
                    "0",
                    NOT_INHERITED,
                    new PrimitivePropertyBuilders.MarginLeft()
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName PADDING_TOP =
            addProperty(
                    "padding-top",
                    PRIMITIVE,
                    "0",
                    NOT_INHERITED,
                    new PrimitivePropertyBuilders.PaddingTop()
            );
    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName PADDING_RIGHT =
            addProperty(
                    "padding-right",
                    PRIMITIVE,
                    "0",
                    NOT_INHERITED,
                    new PrimitivePropertyBuilders.PaddingRight()
            );
    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName PADDING_BOTTOM =
            addProperty(
                    "padding-bottom",
                    PRIMITIVE,
                    "0",
                    NOT_INHERITED,
                    new PrimitivePropertyBuilders.PaddingBottom()
            );
    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName PADDING_LEFT =
            addProperty(
                    "padding-left",
                    PRIMITIVE,
                    "0",
                    NOT_INHERITED,
                    new PrimitivePropertyBuilders.PaddingLeft()
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName BACKGROUND_SHORTHAND =
            addProperty(
                    "background",
                    SHORTHAND,
                    "transparent none repeat scroll 0% 0%",
                    NOT_INHERITED,
                    new BackgroundPropertyBuilder()
            );
    

    /**
     * Unique CSSName instance for CSS3 property.
     */
    public final static CSSName BORDER_RADIUS_SHORTHAND =
            addProperty(
                    "border-radius",
                    SHORTHAND,
                    "0px",
                    NOT_INHERITED,
                    true,
                    new OneToFourPropertyBuilders.BorderRadius()
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName BORDER_WIDTH_SHORTHAND =
            addProperty(
                    "border-width",
                    SHORTHAND,
                    "medium",
                    NOT_INHERITED,
                    new OneToFourPropertyBuilders.BorderWidth()
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName BORDER_STYLE_SHORTHAND =
            addProperty(
                    "border-style",
                    SHORTHAND,
                    "none",
                    NOT_INHERITED,
                    new OneToFourPropertyBuilders.BorderStyle()
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName BORDER_SHORTHAND =
            addProperty(
                    "border",
                    SHORTHAND,
                    "medium none black",
                    NOT_INHERITED,
                    new BorderPropertyBuilders.Border()
            );
    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName BORDER_TOP_SHORTHAND =
            addProperty(
                    "border-top",
                    SHORTHAND,
                    "medium none black",
                    NOT_INHERITED,
                    new BorderPropertyBuilders.BorderTop()
            );
    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName BORDER_RIGHT_SHORTHAND =
            addProperty(
                    "border-right",
                    SHORTHAND,
                    "medium none black",
                    NOT_INHERITED,
                    new BorderPropertyBuilders.BorderRight()
            );
    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName BORDER_BOTTOM_SHORTHAND =
            addProperty(
                    "border-bottom",
                    SHORTHAND,
                    "medium none black",
                    NOT_INHERITED,
                    new BorderPropertyBuilders.BorderBottom()
            );
    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName BORDER_LEFT_SHORTHAND =
            addProperty(
                    "border-left",
                    SHORTHAND,
                    "medium none black",
                    NOT_INHERITED,
                    new BorderPropertyBuilders.BorderLeft()
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName BORDER_COLOR_SHORTHAND =
            addProperty(
                    "border-color",
                    SHORTHAND,
                    "black",
                    NOT_INHERITED,
                    new OneToFourPropertyBuilders.BorderColor()
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName BORDER_SPACING =
            addProperty(
                    "border-spacing",
                    SHORTHAND,
                    "0",
                    INHERITS,
                    new BorderSpacingPropertyBuilder()
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName FONT_SHORTHAND =
            addProperty(
                    "font",
                    SHORTHAND,
                    "",
                    INHERITS,
                    new FontPropertyBuilder()
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName LIST_STYLE_SHORTHAND =
            addProperty(
                    "list-style",
                    SHORTHAND,
                    "disc outside none",
                    INHERITS,
                    new ListStylePropertyBuilder()
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName MARGIN_SHORTHAND =
            addProperty(
                    "margin",
                    SHORTHAND,
                    "0",
                    NOT_INHERITED,
                    new OneToFourPropertyBuilders.Margin()
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName OUTLINE_SHORTHAND =
            addProperty(
                    "outline",
                    SHORTHAND,
                    "invert none medium",
                    NOT_INHERITED,
                    false,
                    null
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName PADDING_SHORTHAND =
            addProperty(
                    "padding",
                    SHORTHAND,
                    "0",
                    NOT_INHERITED,
                    new OneToFourPropertyBuilders.Padding()
            );
    
    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName SIZE_SHORTHAND =
            addProperty(
                    "size",
                    SHORTHAND,
                    "auto",
                    NOT_INHERITED,
                    new SizePropertyBuilder()
            );

    /**
     * Unique CSSName instance for CSS2 property.
     */
    public final static CSSName BOX_SIZING =
            addProperty(
                    "box-sizing",
                    PRIMITIVE,
                    "content-box",
                    NOT_INHERITED,
                    new PrimitivePropertyBuilders.BoxSizing()
            );

    public final static CSSSideProperties MARGIN_SIDE_PROPERTIES =
            new CSSSideProperties(
                    CSSName.MARGIN_TOP,
                    CSSName.MARGIN_RIGHT,
                    CSSName.MARGIN_BOTTOM,
                    CSSName.MARGIN_LEFT);

    public final static CSSSideProperties PADDING_SIDE_PROPERTIES =
            new CSSSideProperties(
                    CSSName.PADDING_TOP,
                    CSSName.PADDING_RIGHT,
                    CSSName.PADDING_BOTTOM,
                    CSSName.PADDING_LEFT);

    public final static CSSSideProperties BORDER_SIDE_PROPERTIES =
            new CSSSideProperties(
                    CSSName.BORDER_TOP_WIDTH,
                    CSSName.BORDER_RIGHT_WIDTH,
                    CSSName.BORDER_BOTTOM_WIDTH,
                    CSSName.BORDER_LEFT_WIDTH);

    public final static CSSSideProperties BORDER_STYLE_PROPERTIES =
            new CSSSideProperties(
                    CSSName.BORDER_TOP_STYLE,
                    CSSName.BORDER_RIGHT_STYLE,
                    CSSName.BORDER_BOTTOM_STYLE,
                    CSSName.BORDER_LEFT_STYLE);

    public final static CSSSideProperties BORDER_COLOR_PROPERTIES =
            new CSSSideProperties(
                    CSSName.BORDER_TOP_COLOR,
                    CSSName.BORDER_RIGHT_COLOR,
                    CSSName.BORDER_BOTTOM_COLOR,
                    CSSName.BORDER_LEFT_COLOR);


    /**
     * Constructor for the CSSName object
     *
     * @param propName     PARAM
     * @param initialValue
     * @param inherits
     * @param implemented
     * @param builder
     */
    private CSSName(
            String propName, String initialValue, boolean inherits,
            boolean implemented, PropertyBuilder builder) {
        this.propName = propName;
        this.FS_ID = CSSName.maxAssigned++;
        this.initialValue = initialValue;
        this.propertyInherits = inherits;
        this.implemented = implemented;
        this.builder = builder;
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
    public static int countCSSNames() {
        return CSSName.maxAssigned;
    }

    /**
     * Returns a count of all CSS primitive (non-shorthand) properties known to this class.
     *
     * @return Returns
     */
    public static int countCSSPrimitiveNames() {
        return ALL_PRIMITIVE_PROPERTY_NAMES.size();
    }

    /**
     * Iterator of ALL CSS 2 visual property names.
     *
     * @return Returns
     */
    public static Iterator allCSS2PropertyNames() {
        return ALL_PROPERTY_NAMES.keySet().iterator();
    }

    /**
     * Iterator of ALL primitive (non-shorthand) CSS 2 visual property names.
     *
     * @return Returns
     */
    public static Iterator allCSS2PrimitivePropertyNames() {
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
    public static boolean propertyInherits(CSSName cssName) {
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
    public static String initialValue(CSSName cssName) {
        return cssName.initialValue;
    }

    public static FSDerivedValue initialDerivedValue(CSSName cssName) {
        return cssName.initialDerivedValue;
    }

    public static boolean isImplemented(CSSName cssName) {
        return cssName.implemented;
    }

    public static PropertyBuilder getPropertyBuilder(CSSName cssName) {
        return cssName.builder;
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

    private static synchronized CSSName addProperty(
            String propName,
            Object type,
            String initialValue,
            Object inherit,
            PropertyBuilder builder
    ) {
        return addProperty(propName, type, initialValue, inherit, true, builder);
    }

    /**
     * Adds a feature to the Property attribute of the CSSName class
     *
     * @param propName     The feature to be added to the Property attribute
     * @param ‚type
     * @param initialValue
     * @param inherit
     * @param implemented
     * @param builder
     * @return Returns
     */
    private static synchronized CSSName addProperty(
            String propName,
            Object type,
            String initialValue,
            Object inherit,
            boolean implemented,
            PropertyBuilder builder
    ) {
        CSSName cssName = new CSSName(
                propName, initialValue, (inherit == INHERITS), implemented, builder);

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
        CSSParser parser = new CSSParser(new CSSErrorHandler() {
            public void error(String uri, String message) {
                XRLog.cssParse("(" + uri + ") " + message);
            }
        });
        for (Iterator i = ALL_PRIMITIVE_PROPERTY_NAMES.values().iterator(); i.hasNext(); ) {
            CSSName cssName = (CSSName)i.next();
            if (cssName.initialValue.charAt(0) != '=' && cssName.implemented) {
                PropertyValue value = parser.parsePropertyValue(
                        cssName, StylesheetInfo.USER_AGENT, cssName.initialValue);

                if (value == null) {
                    XRLog.exception("Unable to derive initial value for " + cssName);
                } else {
                    cssName.initialDerivedValue = DerivedValueFactory.newDerivedValue(
                            null,
                            cssName,
                            value);
                }
            }
        }
    }

    //Assumed to be consistent with equals because CSSName is in essence an enum
    public int compareTo(Object object) {
        if (object == null) throw new NullPointerException();//required by Comparable
        return FS_ID - ((CSSName) object).FS_ID;//will throw ClassCastException according to Comparable if not a CSSName
    }

    // FIXME equals, hashcode

    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CSSName)) return false;

        CSSName cssName = (CSSName) o;

        return FS_ID == cssName.FS_ID;
    }

    public int hashCode() {
        return FS_ID;
    }

    public static class CSSSideProperties {
        public final CSSName top;
        public final CSSName right;
        public final CSSName bottom;
        public final CSSName left;

        public CSSSideProperties(CSSName top, CSSName right, CSSName bottom, CSSName left) {
            this.top = top;
            this.right = right;
            this.bottom = bottom;
            this.left = left;
        }
    }
}

/*
 * $Id$
 *
 * $Log$
 * Revision 1.40  2009/05/13 18:13:53  pdoubleya
 * FindBugs: static methods don't need to be declared final; remove unused constructor parameter
 *
 * Revision 1.39  2009/05/09 14:17:41  pdoubleya
 * FindBugs: static field should not be mutable; use inner class to declare CSS 4-side properties
 *
 * Revision 1.38  2008/12/14 13:53:30  peterbrant
 * Implement -fs-keep-with-inline: keep property that instructs FS to try to avoid breaking a box so that only borders and padding appear on a page
 *
 * Revision 1.37  2007/10/31 23:14:40  peterbrant
 * Add rudimentary support for @font-face rules
 *
 * Revision 1.36  2007/08/29 22:18:17  peterbrant
 * Experiment with text justification
 *
 * Revision 1.35  2007/08/28 22:31:26  peterbrant
 * Implement widows and orphans properties
 *
 * Revision 1.34  2007/08/27 19:44:06  peterbrant
 * Rename -fs-table-pagination to -fs-table-paginate
 *
 * Revision 1.33  2007/08/19 22:22:51  peterbrant
 * Merge R8pbrant changes to HEAD
 *
 * Revision 1.32.2.4  2007/08/15 21:29:31  peterbrant
 * Initial draft of support for running headers and footers on tables
 *
 * Revision 1.32.2.3  2007/08/08 21:44:09  peterbrant
 * Implement more flexible page numbering
 *
 * Revision 1.32.2.2  2007/08/07 17:06:30  peterbrant
 * Implement named pages / Implement page-break-before/after: left/right / Experiment with efficient selection
 *
 * Revision 1.32.2.1  2007/07/09 22:18:01  peterbrant
 * Begin work on running headers and footers and named pages
 *
 * Revision 1.32  2007/06/20 19:06:48  peterbrant
 * Default font-family should come from CSSName and not the UA stylesheet
 *
 * Revision 1.31  2007/05/26 22:08:16  peterbrant
 * Begin work on counter support
 *
 * Revision 1.30  2007/05/24 19:56:51  peterbrant
 * Add support for cursor property (predefined cursors only)
 *
 * Patch from Sean Bright
 *
 * Revision 1.29  2007/02/22 18:21:19  peterbrant
 * Add support for overflow: visible/hidden
 *
 * Revision 1.28  2007/02/20 00:59:14  peterbrant
 * Fix wrong property builder for caption-side / Use new CSS parser for
 * parsing default values
 *
 * Revision 1.27  2007/02/19 23:18:40  peterbrant
 * Further work on new CSS parser / Misc. bug fixes
 *
 * Revision 1.26  2007/02/19 14:53:36  peterbrant
 * Integrate new CSS parser
 *
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

