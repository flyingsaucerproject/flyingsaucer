/*
 * {{{ header & license
 * Copyright (c) 2007 Wisconsin Court System
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 * }}}
 */
package org.xhtmlrenderer.css.parser.property;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.w3c.dom.css.CSSPrimitiveValue;
import org.xhtmlrenderer.css.constants.CSSName;
import org.xhtmlrenderer.css.constants.IdentValue;
import org.xhtmlrenderer.css.parser.CSSParseException;
import org.xhtmlrenderer.css.parser.FSFunction;
import org.xhtmlrenderer.css.parser.FSRGBColor;
import org.xhtmlrenderer.css.parser.PropertyValue;
import org.xhtmlrenderer.css.parser.Token;
import org.xhtmlrenderer.css.sheet.PropertyDeclaration;

public class PrimitivePropertyBuilders {
    // none | hidden | dotted | dashed | solid | double | groove | ridge | inset | outset
    public static final BitSet BORDER_STYLES = setFor(
            new IdentValue[] { IdentValue.NONE, IdentValue.HIDDEN, IdentValue.DOTTED,
                    IdentValue.DASHED, IdentValue.SOLID, IdentValue.DOUBLE,
                    IdentValue.GROOVE, IdentValue.RIDGE, IdentValue.INSET,
                    IdentValue.OUTSET });

    // thin | medium | thick
    public static final BitSet BORDER_WIDTHS = setFor(
            new IdentValue[] { IdentValue.THIN, IdentValue.MEDIUM, IdentValue.THICK });

    // normal | small-caps | inherit
    public static final BitSet FONT_VARIANTS = setFor(
            new IdentValue[] { IdentValue.NORMAL, IdentValue.SMALL_CAPS });

    // normal | italic | oblique | inherit
    public static final BitSet FONT_STYLES = setFor(
            new IdentValue[] { IdentValue.NORMAL, IdentValue.ITALIC, IdentValue.OBLIQUE });

    public static final BitSet FONT_WEIGHTS = setFor(
            new IdentValue[] { IdentValue.NORMAL, IdentValue.BOLD, IdentValue.BOLDER, IdentValue.LIGHTER });

    public static final BitSet PAGE_ORIENTATIONS = setFor(
            new IdentValue[] { IdentValue.AUTO, IdentValue.PORTRAIT, IdentValue.LANDSCAPE });

    // inside | outside | inherit
    public static final BitSet LIST_STYLE_POSITIONS = setFor(new IdentValue[] {
            IdentValue.INSIDE, IdentValue.OUTSIDE });

    // disc | circle | square | decimal
    // | decimal-leading-zero | lower-roman | upper-roman
    // | lower-greek | lower-latin | upper-latin | armenian
    // | georgian | lower-alpha | upper-alpha | none | inherit
    public static final BitSet LIST_STYLE_TYPES = setFor(new IdentValue[] {
            IdentValue.DISC, IdentValue.CIRCLE, IdentValue.SQUARE,
            IdentValue.DECIMAL, IdentValue.DECIMAL_LEADING_ZERO,
            IdentValue.LOWER_ROMAN, IdentValue.UPPER_ROMAN,
            IdentValue.LOWER_GREEK, IdentValue.LOWER_LATIN,
            IdentValue.UPPER_LATIN, IdentValue.ARMENIAN,
            IdentValue.GEORGIAN, IdentValue.LOWER_ALPHA,
            IdentValue.UPPER_ALPHA, IdentValue.NONE });

    // repeat | repeat-x | repeat-y | no-repeat | inherit
    public static final BitSet BACKGROUND_REPEATS = setFor(
            new IdentValue[] {
                    IdentValue.REPEAT, IdentValue.REPEAT_X,
                    IdentValue.REPEAT_Y, IdentValue.NO_REPEAT });

    // scroll | fixed | inherit
    public static final BitSet BACKGROUND_ATTACHMENTS = setFor(
            new IdentValue[] { IdentValue.SCROLL, IdentValue.FIXED });

    // left | right | top | bottom | center
    public static final BitSet BACKGROUND_POSITIONS = setFor(
            new IdentValue[] {
                    IdentValue.LEFT, IdentValue.RIGHT, IdentValue.TOP,
                    IdentValue.BOTTOM, IdentValue.CENTER });

    public static final BitSet ABSOLUTE_FONT_SIZES = setFor(
            new IdentValue[] {
                    IdentValue.XX_SMALL, IdentValue.X_SMALL, IdentValue.SMALL,
                    IdentValue.MEDIUM, IdentValue.LARGE, IdentValue.X_LARGE,
                    IdentValue.XX_LARGE });

    public static final BitSet RELATIVE_FONT_SIZES = setFor(
            new IdentValue[] {
                    IdentValue.SMALLER, IdentValue.LARGER });

    public static final PropertyBuilder COLOR = new GenericColor();
    public static final PropertyBuilder BORDER_STYLE = new GenericBorderStyle();
    public static final PropertyBuilder BORDER_WIDTH = new GenericBorderWidth();
    public static final PropertyBuilder BORDER_RADIUS = new NonNegativeLengthLike();
    public static final PropertyBuilder MARGIN = new LengthLikeWithAuto();
    public static final PropertyBuilder PADDING = new NonNegativeLengthLike();

    private static BitSet setFor(IdentValue[] values) {
        BitSet result = new BitSet(IdentValue.getIdentCount());
        for (int i = 0; i < values.length; i++) {
            IdentValue ident = values[i];
            result.set(ident.FS_ID);
        }
        return result;
    }

    private static abstract class SingleIdent extends AbstractPropertyBuilder {
        protected abstract BitSet getAllowed();

        public List buildDeclarations(
                CSSName cssName, List values, int origin, boolean important, boolean inheritAllowed) {
            checkValueCount(cssName, 1, values.size());
            CSSPrimitiveValue value = (CSSPrimitiveValue)values.get(0);
            checkInheritAllowed(value, inheritAllowed);
            if (value.getCssValueType() != CSSPrimitiveValue.CSS_INHERIT) {
                checkIdentType(cssName, value);
                IdentValue ident = checkIdent(cssName, value);

                checkValidity(cssName, getAllowed(), ident);
            }

            return Collections.singletonList(
                    new PropertyDeclaration(cssName, value, important, origin));

        }
    }

    private static class GenericColor extends AbstractPropertyBuilder {
        private static final BitSet ALLOWED = setFor(
                new IdentValue[] { IdentValue.TRANSPARENT });

        public List buildDeclarations(
                CSSName cssName, List values, int origin, boolean important, boolean inheritAllowed) {
            checkValueCount(cssName, 1, values.size());
            CSSPrimitiveValue value = (CSSPrimitiveValue)values.get(0);
            checkInheritAllowed(value, inheritAllowed);
            if (value.getCssValueType() != CSSPrimitiveValue.CSS_INHERIT) {
                checkIdentOrColorType(cssName, value);

                if (value.getPrimitiveType() == CSSPrimitiveValue.CSS_IDENT) {
                    FSRGBColor color = Conversions.getColor(value.getStringValue());
                    if (color != null) {
                        return Collections.singletonList(
                                new PropertyDeclaration(
                                        cssName,
                                        new PropertyValue(color),
                                        important,
                                        origin));
                    }

                    IdentValue ident = checkIdent(cssName, value);
                    checkValidity(cssName, ALLOWED, ident);
                }
            }

            return Collections.singletonList(
                    new PropertyDeclaration(cssName, value, important, origin));
        }
    }

    private static class GenericBorderStyle extends SingleIdent {
        protected BitSet getAllowed() {
            return BORDER_STYLES;
        }
    }

    private static class GenericBorderWidth extends AbstractPropertyBuilder {
        public List buildDeclarations(
                CSSName cssName, List values, int origin, boolean important, boolean inheritAllowed) {
            checkValueCount(cssName, 1, values.size());
            PropertyValue value = (PropertyValue)values.get(0);
            checkInheritAllowed(value, inheritAllowed);
            if (value.getCssValueType() != CSSPrimitiveValue.CSS_INHERIT) {
                checkIdentOrLengthType(cssName, value);

                if (value.getPrimitiveType() == CSSPrimitiveValue.CSS_IDENT) {
                    IdentValue ident = checkIdent(cssName, value);
                    checkValidity(cssName, BORDER_WIDTHS, ident);

                    return Collections.singletonList(
                            new PropertyDeclaration(
                                    cssName, Conversions.getBorderWidth(ident.toString()), important, origin));
                } else {
                    if (value.getFloatValue() < 0.0f) {
                        throw new CSSParseException(cssName + " may not be negative", -1);
                    }
                }
            }

            return Collections.singletonList(
                    new PropertyDeclaration(cssName, value, important, origin));
        }
    }
    
    private static class GenericBorderCornerRadius extends AbstractPropertyBuilder  {
    	public List buildDeclarations(CSSName cssName, List values, int origin,
                boolean important, boolean inheritAllowed) {
            checkValueCount(cssName, 1, 2, values.size());
            
            PropertyValue first = (PropertyValue)values.get(0);
            PropertyValue second = null;
            if (values.size() == 2) {
                second = (PropertyValue)values.get(1);
            }

            checkInheritAllowed(first, inheritAllowed);

            if (second != null) {
                checkInheritAllowed(second, false);
            }

            checkLengthOrPercentType(cssName, first);
             if (second == null) {
                 return createTwoValueResponse(cssName, first, first, origin, important);
             } else {
                 checkLengthOrPercentType(cssName, second);
                 return createTwoValueResponse(cssName, first, second, origin, important);
             }
        }
    }

    private static abstract class LengthWithIdent extends AbstractPropertyBuilder {
        protected abstract BitSet getAllowed();

        public List buildDeclarations(
                CSSName cssName, List values, int origin, boolean important, boolean inheritAllowed) {
            checkValueCount(cssName, 1, values.size());
            PropertyValue value = (PropertyValue)values.get(0);
            checkInheritAllowed(value, inheritAllowed);
            if (value.getCssValueType() != CSSPrimitiveValue.CSS_INHERIT) {
                checkIdentOrLengthType(cssName, value);

                if (value.getPrimitiveType() == CSSPrimitiveValue.CSS_IDENT) {
                    IdentValue ident = checkIdent(cssName, value);
                    checkValidity(cssName, getAllowed(), ident);
                } else if (! isNegativeValuesAllowed() && value.getFloatValue() < 0.0f) {
                    throw new CSSParseException(cssName + " may not be negative", -1);
                }
            }

            return Collections.singletonList(
                    new PropertyDeclaration(cssName, value, important, origin));

        }

        protected boolean isNegativeValuesAllowed() {
            return true;
        }
    }

    private static abstract class LengthLikeWithIdent extends AbstractPropertyBuilder {
        protected abstract BitSet getAllowed();

        public List buildDeclarations(
                CSSName cssName, List values, int origin, boolean important, boolean inheritAllowed) {
            checkValueCount(cssName, 1, values.size());
            PropertyValue value = (PropertyValue)values.get(0);
            checkInheritAllowed(value, inheritAllowed);
            if (value.getCssValueType() != CSSPrimitiveValue.CSS_INHERIT) {
                checkIdentLengthOrPercentType(cssName, value);

                if (value.getPrimitiveType() == CSSPrimitiveValue.CSS_IDENT) {
                    IdentValue ident = checkIdent(cssName, value);
                    checkValidity(cssName, getAllowed(), ident);
                } else if (! isNegativeValuesAllowed() && value.getFloatValue() < 0.0f) {
                    throw new CSSParseException(cssName + " may not be negative", -1);
                }
            }

            return Collections.singletonList(
                    new PropertyDeclaration(cssName, value, important, origin));

        }

        protected boolean isNegativeValuesAllowed() {
            return true;
        }
    }

    private static class LengthLike extends AbstractPropertyBuilder {
        public List buildDeclarations(
                CSSName cssName, List values, int origin, boolean important, boolean inheritAllowed) {
            checkValueCount(cssName, 1, values.size());
            PropertyValue value = (PropertyValue)values.get(0);
            checkInheritAllowed(value, inheritAllowed);
            if (value.getCssValueType() != CSSPrimitiveValue.CSS_INHERIT) {
                checkLengthOrPercentType(cssName, value);

                if (! isNegativeValuesAllowed() && value.getFloatValue() < 0.0f) {
                    throw new CSSParseException(cssName + " may not be negative", -1);
                }
            }

            return Collections.singletonList(
                    new PropertyDeclaration(cssName, value, important, origin));

        }

        protected boolean isNegativeValuesAllowed() {
            return true;
        }
    }

    private static class NonNegativeLengthLike extends LengthLike {
        protected boolean isNegativeValuesAllowed() {
            return false;
        }
    }

    private static class ColOrRowSpan extends AbstractPropertyBuilder {
        public List buildDeclarations(CSSName cssName, List values, int origin, boolean important, boolean inheritAllowed) {
            checkValueCount(cssName, 1, values.size());
            PropertyValue value = (PropertyValue)values.get(0);
            checkInheritAllowed(value, inheritAllowed);
            if (value.getCssValueType() != CSSPrimitiveValue.CSS_INHERIT) {
                checkNumberType(cssName, value);

                if (value.getFloatValue() < 1) {
                    throw new CSSParseException("colspan/rowspan must be greater than zero", -1);
                }
            }

            return Collections.singletonList(
                    new PropertyDeclaration(cssName, value, important, origin));
        }
    }

    private static class PlainInteger extends AbstractPropertyBuilder {
        public List buildDeclarations(
                CSSName cssName, List values, int origin, boolean important, boolean inheritAllowed) {
            checkValueCount(cssName, 1, values.size());
            PropertyValue value = (PropertyValue)values.get(0);
            checkInheritAllowed(value, inheritAllowed);
            if (value.getCssValueType() != CSSPrimitiveValue.CSS_INHERIT) {
                checkInteger(cssName, value);

                if (! isNegativeValuesAllowed() && value.getFloatValue() < 0.0f) {
                    throw new CSSParseException(cssName + " may not be negative", -1);
                }
            }

            return Collections.singletonList(
                    new PropertyDeclaration(cssName, value, important, origin));

        }

        protected boolean isNegativeValuesAllowed() {
            return true;
        }
    }

    private static class Length extends AbstractPropertyBuilder {
        public List buildDeclarations(
                CSSName cssName, List values, int origin, boolean important, boolean inheritAllowed) {
            checkValueCount(cssName, 1, values.size());
            PropertyValue value = (PropertyValue)values.get(0);
            checkInheritAllowed(value, inheritAllowed);
            if (value.getCssValueType() != CSSPrimitiveValue.CSS_INHERIT) {
                checkLengthType(cssName, value);

                if (! isNegativeValuesAllowed() && value.getFloatValue() < 0.0f) {
                    throw new CSSParseException(cssName + " may not be negative", -1);
                }
            }

            return Collections.singletonList(
                    new PropertyDeclaration(cssName, value, important, origin));

        }

        protected boolean isNegativeValuesAllowed() {
            return true;
        }
    }

    /*
    private static class SingleString extends AbstractPropertyBuilder {
        public List buildDeclarations(
                CSSName cssName, List values, int origin, boolean important, boolean inheritAllowed) {
            checkValueCount(cssName, 1, values.size());
            CSSPrimitiveValue value = (CSSPrimitiveValue)values.get(0);
            checkInheritAllowed(value, inheritAllowed);
            if (value.getCssValueType() != CSSPrimitiveValue.CSS_INHERIT) {
                checkStringType(cssName, value);
            }

            return Collections.singletonList(
                    new PropertyDeclaration(cssName, value, important, origin));

        }
    }
    */

    /*
    private static abstract class SingleStringWithIdent extends AbstractPropertyBuilder {
        protected abstract BitSet getAllowed();

        public List buildDeclarations(
                CSSName cssName, List values, int origin, boolean important, boolean inheritAllowed) {
            checkValueCount(cssName, 1, values.size());
            CSSPrimitiveValue value = (CSSPrimitiveValue)values.get(0);
            checkInheritAllowed(value, inheritAllowed);
            if (value.getCssValueType() != CSSPrimitiveValue.CSS_INHERIT) {
                checkIdentOrString(cssName, value);

                if (value.getPrimitiveType() == CSSPrimitiveValue.CSS_IDENT) {
                    IdentValue ident = checkIdent(cssName, value);

                    checkValidity(cssName, getAllowed(), ident);
                }
            }

            return Collections.singletonList(
                    new PropertyDeclaration(cssName, value, important, origin));

        }
    }
    */

    /*
    private static class SingleStringWithNone extends SingleStringWithIdent {
        private static final BitSet ALLOWED = setFor(new IdentValue[] { IdentValue.NONE });

        protected BitSet getAllowed() {
            return ALLOWED;
        }
    }
    */

    private static class LengthLikeWithAuto extends LengthLikeWithIdent {
        // <length> | <percentage> | auto | inherit
        private static final BitSet ALLOWED = setFor(
                new IdentValue[] { IdentValue.AUTO });

        protected BitSet getAllowed() {
            return ALLOWED;
        }
    }

    private static class LengthWithNormal extends LengthWithIdent {
        // <length> | normal | inherit
        private static final BitSet ALLOWED = setFor(
                new IdentValue[] { IdentValue.NORMAL });

        protected BitSet getAllowed() {
            return ALLOWED;
        }
    }

    private static class LengthLikeWithNone extends LengthLikeWithIdent {
        // <length> | <percentage> | none | inherit
        private static final BitSet ALLOWED = setFor(
                new IdentValue[] { IdentValue.NONE });

        protected BitSet getAllowed() {
            return ALLOWED;
        }
    }

    private static class GenericURIWithNone extends AbstractPropertyBuilder {
        // <uri> | none | inherit
        private static final BitSet ALLOWED = setFor(new IdentValue[] { IdentValue.NONE });

        public List buildDeclarations(
                CSSName cssName, List values, int origin, boolean important, boolean inheritAllowed) {
            checkValueCount(cssName, 1, values.size());
            CSSPrimitiveValue value = (CSSPrimitiveValue)values.get(0);
            checkInheritAllowed(value, inheritAllowed);
            if (value.getCssValueType() != CSSPrimitiveValue.CSS_INHERIT) {
                checkIdentOrURIType(cssName, value);

                if (value.getPrimitiveType() == CSSPrimitiveValue.CSS_IDENT) {
                    IdentValue ident = checkIdent(cssName, value);
                    checkValidity(cssName, ALLOWED, ident);
                }
            }
            return Collections.singletonList(
                    new PropertyDeclaration(cssName, value, important, origin));
        }
    }

    public static class BackgroundAttachment extends SingleIdent {
        protected BitSet getAllowed() {
            return BACKGROUND_ATTACHMENTS;
        }
    }

    public static class BackgroundColor extends GenericColor {
    }

    public static class BackgroundImage extends GenericURIWithNone {
    }

    public static class BackgroundSize extends AbstractPropertyBuilder {
        private static final BitSet ALL_ALLOWED = setFor(new IdentValue[] {
                IdentValue.AUTO, IdentValue.CONTAIN, IdentValue.COVER
        });

        public List buildDeclarations(CSSName cssName, List values, int origin, boolean important, boolean inheritAllowed) {
            checkValueCount(cssName, 1, 2, values.size());

            CSSPrimitiveValue first = (CSSPrimitiveValue)values.get(0);
            CSSPrimitiveValue second = null;
            if (values.size() == 2) {
                second = (CSSPrimitiveValue)values.get(1);
            }

            checkInheritAllowed(first, inheritAllowed);
            if (values.size() == 1 &&
                    first.getCssValueType() == CSSPrimitiveValue.CSS_INHERIT) {
                return Collections.singletonList(
                        new PropertyDeclaration(cssName, first, important, origin));
            }

            if (second != null) {
                checkInheritAllowed(second, false);
            }

            checkIdentLengthOrPercentType(cssName, first);
            if (second == null) {
                if (first.getPrimitiveType() == CSSPrimitiveValue.CSS_IDENT) {
                    IdentValue firstIdent = checkIdent(cssName, first);
                    checkValidity(cssName, ALL_ALLOWED, firstIdent);

                    if (firstIdent == IdentValue.CONTAIN || firstIdent == IdentValue.COVER) {
                        return Collections.singletonList(
                                new PropertyDeclaration(cssName, first, important, origin));
                    } else {
                        return createTwoValueResponse(CSSName.BACKGROUND_SIZE, first, first, origin, important);
                    }
                } else {
                    return createTwoValueResponse(CSSName.BACKGROUND_SIZE, first, new PropertyValue(IdentValue.AUTO), origin, important);
                }
            } else {
                checkIdentLengthOrPercentType(cssName, second);

                if (first.getPrimitiveType() == CSSPrimitiveValue.CSS_IDENT) {
                    IdentValue firstIdent = checkIdent(cssName, first);
                    if (firstIdent != IdentValue.AUTO) {
                        throw new CSSParseException("The only ident value allowed here is 'auto'", -1);
                    }
                } else if (((PropertyValue)first).getFloatValue() < 0.0f) {
                    throw new CSSParseException(cssName + " values cannot be negative", -1);
                }

                if (second.getPrimitiveType() == CSSPrimitiveValue.CSS_IDENT) {
                    IdentValue secondIdent = checkIdent(cssName, second);
                    if (secondIdent != IdentValue.AUTO) {
                        throw new CSSParseException("The only ident value allowed here is 'auto'", -1);
                    }
                } else if (((PropertyValue)second).getFloatValue() < 0.0f) {
                    throw new CSSParseException(cssName + " values cannot be negative", -1);
                }
                
                return createTwoValueResponse(CSSName.BACKGROUND_SIZE, first, second, origin, important);
            }
        }

    }

    public static class BackgroundPosition extends AbstractPropertyBuilder {
        public List buildDeclarations(
                CSSName cssName, List values, int origin, boolean important, boolean inheritAllowed) {
            checkValueCount(cssName, 1, 2, values.size());

            CSSPrimitiveValue first = (CSSPrimitiveValue)values.get(0);
            CSSPrimitiveValue second = null;
            if (values.size() == 2) {
                second = (CSSPrimitiveValue)values.get(1);
            }

            checkInheritAllowed(first, inheritAllowed);
            if (values.size() == 1 &&
                    first.getCssValueType() == CSSPrimitiveValue.CSS_INHERIT) {
                return Collections.singletonList(
                        new PropertyDeclaration(cssName, first, important, origin));
            }

            if (second != null) {
                checkInheritAllowed(second, false);
            }

            checkIdentLengthOrPercentType(cssName, first);
            if (second == null) {
                if (isLength(first) || first.getPrimitiveType() == CSSPrimitiveValue.CSS_PERCENTAGE) {
                    List responseValues = new ArrayList(2);
                    responseValues.add(first);
                    responseValues.add(new PropertyValue(
                            CSSPrimitiveValue.CSS_PERCENTAGE, 50.0f, "50%"));
                    return Collections.singletonList(new PropertyDeclaration(
                                CSSName.BACKGROUND_POSITION,
                                new PropertyValue(responseValues), important, origin));
                }
            } else {
                checkIdentLengthOrPercentType(cssName, second);
            }


            IdentValue firstIdent = null;
            if (first.getPrimitiveType() == CSSPrimitiveValue.CSS_IDENT) {
                firstIdent = checkIdent(cssName, first);
                checkValidity(cssName, getAllowed(), firstIdent);
            }

            IdentValue secondIdent = null;
            if (second == null) {
                secondIdent = IdentValue.CENTER;
            } else if (second.getPrimitiveType() == CSSPrimitiveValue.CSS_IDENT) {
                secondIdent = checkIdent(cssName, second);
                checkValidity(cssName, getAllowed(), secondIdent);
            }

            if (firstIdent == null && secondIdent == null) {
                return Collections.singletonList(new PropertyDeclaration(
                        CSSName.BACKGROUND_POSITION, new PropertyValue(values), important, origin));
            } else if (firstIdent != null && secondIdent != null) {
                if (firstIdent == IdentValue.TOP || firstIdent == IdentValue.BOTTOM ||
                        secondIdent == IdentValue.LEFT || secondIdent == IdentValue.RIGHT) {
                    IdentValue temp = firstIdent;
                    firstIdent = secondIdent;
                    secondIdent = temp;
                }

                checkIdentPosition(cssName, firstIdent, secondIdent);

                return createTwoPercentValueResponse(
                        getPercentForIdent(firstIdent),
                        getPercentForIdent(secondIdent),
                        important,
                        origin);
            } else {
                checkIdentPosition(cssName, firstIdent, secondIdent);

                List responseValues = new ArrayList(2);

                if (firstIdent == null) {
                    responseValues.add(first);
                    responseValues.add(createValueForIdent(secondIdent));
                } else {
                    responseValues.add(createValueForIdent(firstIdent));
                    responseValues.add(second);
                }

                return Collections.singletonList(new PropertyDeclaration(
                        CSSName.BACKGROUND_POSITION,
                        new PropertyValue(responseValues), important, origin));
            }
        }

        private void checkIdentPosition(CSSName cssName, IdentValue firstIdent, IdentValue secondIdent) {
            if (firstIdent == IdentValue.TOP || firstIdent == IdentValue.BOTTOM ||
                    secondIdent == IdentValue.LEFT || secondIdent == IdentValue.RIGHT) {
                throw new CSSParseException("Invalid combination of keywords in " + cssName, -1);
            }
        }

        private float getPercentForIdent(IdentValue ident) {
            float percent = 0.0f;

            if (ident == IdentValue.CENTER) {
                percent = 50.f;
            } else if (ident == IdentValue.BOTTOM || ident == IdentValue.RIGHT) {
                percent = 100.0f;
            }

            return percent;
        }

        private PropertyValue createValueForIdent(IdentValue ident) {
            float percent = getPercentForIdent(ident);
            return new PropertyValue(
                    CSSPrimitiveValue.CSS_PERCENTAGE, percent, percent + "%");
        }

        private List createTwoPercentValueResponse(
                float percent1, float percent2, boolean important, int origin) {
            PropertyValue value1 = new PropertyValue(
                    CSSPrimitiveValue.CSS_PERCENTAGE, percent1, percent1 + "%");
            PropertyValue value2 = new PropertyValue(
                    CSSPrimitiveValue.CSS_PERCENTAGE, percent2, percent2 + "%");

            List values = new ArrayList(2);
            values.add(value1);
            values.add(value2);

            PropertyDeclaration result = new PropertyDeclaration(
                    CSSName.BACKGROUND_POSITION,
                    new PropertyValue(values), important, origin);

            return Collections.singletonList(result);
        }

        private BitSet getAllowed() {
            return BACKGROUND_POSITIONS;
        }
    }

    public static class BackgroundRepeat extends SingleIdent {
        protected BitSet getAllowed() {
            return BACKGROUND_REPEATS;
        }
    }

    public static class BorderCollapse extends SingleIdent {
        // collapse | separate | inherit
        private static final BitSet ALLOWED = setFor(
                new IdentValue[] { IdentValue.COLLAPSE, IdentValue.SEPARATE });

        protected BitSet getAllowed() {
            return ALLOWED;
        }
    }

    public static class BorderTopColor extends GenericColor {
    }

    public static class BorderRightColor extends GenericColor {
    }

    public static class BorderBottomColor extends GenericColor {
    }

    public static class BorderLeftColor extends GenericColor {
    }

    public static class BorderTopStyle extends GenericBorderStyle {
    }

    public static class BorderRightStyle extends GenericBorderStyle {
    }

    public static class BorderBottomStyle extends GenericBorderStyle {
    }

    public static class BorderLeftStyle extends GenericBorderStyle {
    }

    public static class BorderTopWidth extends GenericBorderWidth {
    }

    public static class BorderRightWidth extends GenericBorderWidth {
    }

    public static class BorderBottomWidth extends GenericBorderWidth {
    }

    public static class BorderLeftWidth extends GenericBorderWidth {
    }
    
    public static class BorderTopLeftRadius extends GenericBorderCornerRadius {
    }
    
    public static class BorderTopRightRadius extends GenericBorderCornerRadius {
    }
    
    public static class BorderBottomRightRadius extends GenericBorderCornerRadius {
    }
    
    public static class BorderBottomLeftRadius extends GenericBorderCornerRadius {
    }

    public static class Bottom extends LengthLikeWithAuto {
    }

    public static class CaptionSide extends SingleIdent {
        // top | bottom | inherit
        private static final BitSet ALLOWED = setFor(
                new IdentValue[] { IdentValue.TOP, IdentValue.BOTTOM });

        protected BitSet getAllowed() {
            return ALLOWED;
        }
    }

    public static class Clear extends SingleIdent {
        // none | left | right | both | inherit
        private static final BitSet ALLOWED = setFor(
                new IdentValue[] { IdentValue.NONE, IdentValue.LEFT, IdentValue.RIGHT, IdentValue.BOTH });

        protected BitSet getAllowed() {
            return ALLOWED;
        }
    }

    public static class Color extends GenericColor {
    }

    public static class Cursor extends SingleIdent {
        // [ [<uri> ,]* [ auto | crosshair | default | pointer | move | e-resize
        // | ne-resize | nw-resize | n-resize | se-resize | sw-resize | s-resize
        // | w-resize | text | wait | help | progress ] ] | inherit
        private static final BitSet ALLOWED = setFor(
                new IdentValue[] {
                        IdentValue.AUTO, IdentValue.CROSSHAIR,
                        IdentValue.DEFAULT, IdentValue.POINTER,
                        IdentValue.MOVE, IdentValue.E_RESIZE,
                        IdentValue.NE_RESIZE, IdentValue.NW_RESIZE,
                        IdentValue.N_RESIZE, IdentValue.SE_RESIZE,
                        IdentValue.SW_RESIZE, IdentValue.S_RESIZE,
                        IdentValue.W_RESIZE, IdentValue.TEXT,
                        IdentValue.WAIT, IdentValue.HELP,
                        IdentValue.PROGRESS});

        protected BitSet getAllowed() {
            return ALLOWED;
        }
    }

    public static class Display extends SingleIdent {
        // inline | block | list-item | run-in | inline-block | table | inline-table
        // | table-row-group | table-header-group
        // | table-footer-group | table-row | table-column-group | table-column
        // | table-cell | table-caption | none | inherit
        private static final BitSet ALLOWED = setFor(
                new IdentValue[] {
                        IdentValue.INLINE, IdentValue.BLOCK,
                        IdentValue.LIST_ITEM, /* IdentValue.RUN_IN, */
                        IdentValue.INLINE_BLOCK, IdentValue.TABLE,
                        IdentValue.INLINE_TABLE, IdentValue.TABLE_ROW_GROUP,
                        IdentValue.TABLE_HEADER_GROUP, IdentValue.TABLE_FOOTER_GROUP,
                        IdentValue.TABLE_ROW, IdentValue.TABLE_COLUMN_GROUP,
                        IdentValue.TABLE_COLUMN, IdentValue.TABLE_CELL,
                        IdentValue.TABLE_CAPTION, IdentValue.NONE });

        protected BitSet getAllowed() {
            return ALLOWED;
        }
    }

    public static class EmptyCells extends SingleIdent {
        // show | hide | inherit
        private static final BitSet ALLOWED = setFor(
                new IdentValue[] { IdentValue.SHOW, IdentValue.HIDE });

        protected BitSet getAllowed() {
            return ALLOWED;
        }
    }

    public static class Float extends SingleIdent {
        // left | right | none | inherit
        private static final BitSet ALLOWED = setFor(
                new IdentValue[] { IdentValue.LEFT, IdentValue.RIGHT, IdentValue.NONE });

        protected BitSet getAllowed() {
            return ALLOWED;
        }
    }

    public static class FontFamily extends AbstractPropertyBuilder {
        // [[ <family-name> | <generic-family> ] [, <family-name>| <generic-family>]* ] | inherit

        public List buildDeclarations(
                CSSName cssName, List values, int origin, boolean important, boolean inheritAllowed) {
            if (values.size() == 1) {
                CSSPrimitiveValue value = (CSSPrimitiveValue)values.get(0);
                checkInheritAllowed(value, inheritAllowed);
                if (value.getCssValueType() == CSSPrimitiveValue.CSS_INHERIT) {
                    return Collections.singletonList(
                            new PropertyDeclaration(cssName, value, important, origin));
                }
            }

            // Both Opera and Firefox parse "Century Gothic" Arial sans-serif as
            // [Century Gothic], [Arial sans-serif] (i.e. the comma is assumed
            // after a string).  Seems wrong per the spec, but FF (at least)
            // does it in standards mode so we do too.
            List consecutiveIdents = new ArrayList();
            List normalized = new ArrayList(values.size());
            for (Iterator i = values.iterator(); i.hasNext(); ) {
                PropertyValue value = (PropertyValue)i.next();

                Token operator = value.getOperator();
                if (operator != null && operator != Token.TK_COMMA) {
                    throw new CSSParseException("Invalid font-family definition", -1);
                }

                if (operator != null) {
                    if (consecutiveIdents.size() > 0) {
                        normalized.add(concat(consecutiveIdents, ' '));
                        consecutiveIdents.clear();
                    }
                }

                checkInheritAllowed(value, false);
                short type = value.getPrimitiveType();
                if (type == CSSPrimitiveValue.CSS_STRING) {
                    if (consecutiveIdents.size() > 0) {
                        normalized.add(concat(consecutiveIdents, ' '));
                        consecutiveIdents.clear();
                    }
                    normalized.add(value.getStringValue());
                } else if (type == CSSPrimitiveValue.CSS_IDENT) {
                    consecutiveIdents.add(value.getStringValue());
                } else {
                    throw new CSSParseException("Invalid font-family definition", -1);
                }
            }
            if (consecutiveIdents.size() > 0) {
                normalized.add(concat(consecutiveIdents, ' '));
            }

            String text = concat(normalized, ',');
            PropertyValue result = new PropertyValue(
                    CSSPrimitiveValue.CSS_STRING, text, text);  // HACK cssText can be wrong
            result.setStringArrayValue((String[]) normalized.toArray(new String[normalized.size()]));

            return Collections.singletonList(
                    new PropertyDeclaration(cssName, result, important, origin));
        }

        private String concat(List strings, char separator) {
            StringBuffer buf = new StringBuffer(64);
            for (Iterator i = strings.iterator(); i.hasNext(); ) {
                String s = (String)i.next();
                buf.append(s);
                if (i.hasNext()) {
                    buf.append(separator);
                }
            }
            return buf.toString();
        }
    }

    public static class FontSize extends AbstractPropertyBuilder {
        // <absolute-size> | <relative-size> | <length> | <percentage> | inherit
        private static final BitSet ALLOWED;

        static {
            ALLOWED = new BitSet(IdentValue.getIdentCount());
            ALLOWED.or(ABSOLUTE_FONT_SIZES);
            ALLOWED.or(RELATIVE_FONT_SIZES);
        }

        public List buildDeclarations(
                CSSName cssName, List values, int origin, boolean important, boolean inheritAllowed) {
            checkValueCount(cssName, 1, values.size());
            PropertyValue value = (PropertyValue)values.get(0);
            checkInheritAllowed(value, inheritAllowed);
            if (value.getCssValueType() != CSSPrimitiveValue.CSS_INHERIT) {
                checkIdentLengthOrPercentType(cssName, value);

                if (value.getPrimitiveType() == CSSPrimitiveValue.CSS_IDENT) {
                    IdentValue ident = checkIdent(cssName, value);
                    checkValidity(cssName, ALLOWED, ident);
                } else if (value.getFloatValue() < 0.0f) {
                    throw new CSSParseException("font-size may not be negative", -1);
                }
            }

            return Collections.singletonList(
                    new PropertyDeclaration(cssName, value, important, origin));

        }
    }

    public static class FontStyle extends SingleIdent {
        protected BitSet getAllowed() {
            return FONT_STYLES;
        }
    }

    public static class FontVariant extends SingleIdent {
        protected BitSet getAllowed() {
            return FONT_VARIANTS;
        }
    }

    public static class FontWeight extends AbstractPropertyBuilder {
        // normal | bold | bolder | lighter | 100 | 200 | 300 | 400 | 500 | 600 | 700 | 800 | 900 | inherit
        public List buildDeclarations(
                CSSName cssName, List values, int origin, boolean important, boolean inheritAllowed) {
            checkValueCount(cssName, 1, values.size());
            PropertyValue value = (PropertyValue)values.get(0);
            checkInheritAllowed(value, inheritAllowed);
            if (value.getCssValueType() != CSSPrimitiveValue.CSS_INHERIT) {
                checkIdentOrNumberType(cssName, value);

                short type = value.getPrimitiveType();
                if (type == CSSPrimitiveValue.CSS_IDENT) {
                    checkIdentType(cssName, value);
                    IdentValue ident = checkIdent(cssName, value);

                    checkValidity(cssName, getAllowed(), ident);
                } else if (type == CSSPrimitiveValue.CSS_NUMBER) {
                    IdentValue weight = Conversions.getNumericFontWeight(value.getFloatValue());
                    if (weight == null) {
                        throw new CSSParseException(value + " is not a valid font weight", -1);
                    }

                    PropertyValue replacement = new PropertyValue(
                            CSSPrimitiveValue.CSS_IDENT, weight.toString(), weight.toString());
                    replacement.setIdentValue(weight);
                    return Collections.singletonList(
                            new PropertyDeclaration(cssName, replacement, important, origin));

                }
            }

            return Collections.singletonList(
                    new PropertyDeclaration(cssName, value, important, origin));
        }

        private BitSet getAllowed() {
            return FONT_WEIGHTS;
        }
    }

    public static class FSBorderSpacingHorizontal extends Length {
    }

    public static class FSBorderSpacingVertical extends Length {
    }

    public static class FSFontMetricSrc extends GenericURIWithNone {
    }

    public static class FSPageHeight extends LengthLikeWithAuto {
        protected boolean isNegativeValuesAllowed() {
            return false;
        }
    }

    public static class FSPageWidth extends LengthLikeWithAuto {
        protected boolean isNegativeValuesAllowed() {
            return false;
        }
    }

    public static class FSPageSequence extends SingleIdent {
        // start | auto
        private static final BitSet ALLOWED = setFor(
                new IdentValue[] { IdentValue.START, IdentValue.AUTO });

        protected BitSet getAllowed() {
            return ALLOWED;
        }
    }

    public static class FSPageOrientation extends SingleIdent {
        protected BitSet getAllowed() {
            return PAGE_ORIENTATIONS;
        }
    }

    public static class FSPDFFontEmbed extends SingleIdent {
        // auto | embed
        private static final BitSet ALLOWED = setFor(
                new IdentValue[] { IdentValue.AUTO, IdentValue.EMBED });

        protected BitSet getAllowed() {
            return ALLOWED;
        }
    }

    public static class FSPDFFontEncoding extends AbstractPropertyBuilder {
        public List buildDeclarations(
                CSSName cssName, List values, int origin, boolean important, boolean inheritAllowed) {
            checkValueCount(cssName, 1, values.size());
            CSSPrimitiveValue value = (CSSPrimitiveValue)values.get(0);
            checkInheritAllowed(value, inheritAllowed);
            if (value.getCssValueType() != CSSPrimitiveValue.CSS_INHERIT) {
                checkIdentOrString(cssName, value);

                if (value.getPrimitiveType() == CSSPrimitiveValue.CSS_IDENT) {
                    // Convert to string
                    return Collections.singletonList(
                            new PropertyDeclaration(
                                    cssName,
                                    new PropertyValue(
                                            CSSPrimitiveValue.CSS_STRING,
                                            value.getStringValue(),
                                            value.getCssText()),
                                    important,
                                    origin));
                }
            }

            return Collections.singletonList(
                    new PropertyDeclaration(cssName, value, important, origin));
        }
    }

    public static class FSTableCellColspan extends ColOrRowSpan {
    }

    public static class FSTableCellRowspan extends ColOrRowSpan {
    }

    public static class FSTablePaginate extends SingleIdent {
        private static final BitSet ALLOWED = setFor(
                new IdentValue[] { IdentValue.PAGINATE, IdentValue.AUTO });

        protected BitSet getAllowed() {
            return ALLOWED;
        }
     }

    public static class FSTextDecorationExtent extends SingleIdent {
       private static final BitSet ALLOWED = setFor(
               new IdentValue[] { IdentValue.LINE, IdentValue.BLOCK });

       protected BitSet getAllowed() {
           return ALLOWED;
       }
    }

    public static class FSFitImagesToWidth extends LengthLikeWithAuto {
        protected boolean isNegativeValuesAllowed() {
            return false;
        }
     }

    public static class Height extends LengthLikeWithAuto {
        protected boolean isNegativeValuesAllowed() {
            return false;
        }
    }

    public static class FSDynamicAutoWidth extends SingleIdent {
        private static final BitSet ALLOWED = setFor(
                new IdentValue[] { IdentValue.DYNAMIC, IdentValue.STATIC });

        protected BitSet getAllowed() {
            return ALLOWED;
        }
    }

    public static class FSKeepWithInline extends SingleIdent {
        // auto | keep
        private static final BitSet ALLOWED = setFor(
                new IdentValue[] { IdentValue.AUTO, IdentValue.KEEP });

        protected BitSet getAllowed() {
            return ALLOWED;
        }
    }

    public static class FSNamedDestination extends SingleIdent {
        // none | create
        private static final BitSet ALLOWED = setFor(
                new IdentValue[] { IdentValue.NONE, IdentValue.CREATE });

        protected BitSet getAllowed() {
            return ALLOWED;
        }
    }

    public static class Left extends LengthLikeWithAuto {
    }

    public static class LetterSpacing extends LengthWithNormal {
    }

    public static class LineHeight extends AbstractPropertyBuilder {
        // normal | <number> | <length> | <percentage> | inherit
        private static final BitSet ALLOWED = setFor(
                new IdentValue[] { IdentValue.NORMAL });

        public List buildDeclarations(
                CSSName cssName, List values, int origin, boolean important, boolean inheritAllowed) {
            checkValueCount(cssName, 1, values.size());
            PropertyValue value = (PropertyValue)values.get(0);
            checkInheritAllowed(value, inheritAllowed);
            if (value.getCssValueType() != CSSPrimitiveValue.CSS_INHERIT) {
                checkIdentLengthNumberOrPercentType(cssName, value);

                if (value.getPrimitiveType() == CSSPrimitiveValue.CSS_IDENT) {
                    IdentValue ident = checkIdent(cssName, value);
                    checkValidity(cssName, ALLOWED, ident);
                } else if (value.getFloatValue() < 0.0) {
                    throw new CSSParseException("line-height may not be negative", -1);
                }
            }
            return Collections.singletonList(
                    new PropertyDeclaration(cssName, value, important, origin));
        }
    }

    public static class ListStyleImage extends GenericURIWithNone {
    }

    public static class ListStylePosition extends SingleIdent {
        protected BitSet getAllowed() {
            return LIST_STYLE_POSITIONS;
        }
    }

    public static class ListStyleType extends SingleIdent {
        protected BitSet getAllowed() {
            return LIST_STYLE_TYPES;
        }
    }

    public static class MarginTop extends LengthLikeWithAuto {
    }

    public static class MarginRight extends LengthLikeWithAuto {
    }

    public static class MarginBottom extends LengthLikeWithAuto {
    }

    public static class MarginLeft extends LengthLikeWithAuto {
    }

    public static class MaxHeight extends LengthLikeWithNone {
        protected boolean isNegativeValuesAllowed() {
            return false;
        }
    }

    public static class MaxWidth extends LengthLikeWithNone {
        protected boolean isNegativeValuesAllowed() {
            return false;
        }
    }

    public static class MinHeight extends NonNegativeLengthLike {
    }

    public static class MinWidth extends NonNegativeLengthLike {
    }

    public static class Orphans extends PlainInteger {
        protected boolean isNegativeValuesAllowed() {
            return false;
        }
    }

    public static class Overflow extends SingleIdent {
        // visible | hidden | scroll | auto | inherit
        private static final BitSet ALLOWED = setFor(
                new IdentValue[] {
                        IdentValue.VISIBLE, IdentValue.HIDDEN,
                        /* IdentValue.SCROLL, IdentValue.AUTO, */ });

        // We only support visible or hidden for now

        protected BitSet getAllowed() {
            return ALLOWED;
        }
    }

    public static class PaddingTop extends NonNegativeLengthLike {
    }

    public static class PaddingRight extends NonNegativeLengthLike {
    }

    public static class PaddingBottom extends NonNegativeLengthLike {
    }

    public static class PaddingLeft extends NonNegativeLengthLike {
    }

    public static class PageBreakBefore extends SingleIdent {
        // auto | always | avoid | left | right | inherit
        private static final BitSet ALLOWED = setFor(
                new IdentValue[] {
                        IdentValue.AUTO, IdentValue.ALWAYS,
                        IdentValue.AVOID, IdentValue.LEFT,
                        IdentValue.RIGHT });

        protected BitSet getAllowed() {
            return ALLOWED;
        }
    }

    public static class Page extends AbstractPropertyBuilder {
        public List buildDeclarations(
                CSSName cssName, List values, int origin, boolean important, boolean inheritAllowed) {
            checkValueCount(cssName, 1, values.size());
            CSSPrimitiveValue value = (CSSPrimitiveValue)values.get(0);
            checkInheritAllowed(value, inheritAllowed);
            if (value.getCssValueType() != CSSPrimitiveValue.CSS_INHERIT) {
                checkIdentType(cssName, value);

                if (! value.getStringValue().equals("auto")) {
                    // Treat as string since it won't be a proper IdentValue
                    value = new PropertyValue(
                            CSSPrimitiveValue.CSS_STRING, value.getStringValue(), value.getCssText());
                }
            }

            return Collections.singletonList(
                    new PropertyDeclaration(cssName, value, important, origin));


        }
    }

    public static class PageBreakAfter extends SingleIdent {
        // auto | always | avoid | left | right | inherit
        private static final BitSet ALLOWED = setFor(
                new IdentValue[] {
                        IdentValue.AUTO, IdentValue.ALWAYS,
                        IdentValue.AVOID, IdentValue.LEFT,
                        IdentValue.RIGHT });

        protected BitSet getAllowed() {
            return ALLOWED;
        }
    }

    public static class PageBreakInside extends SingleIdent {
        // avoid | auto | inherit
        private static final BitSet ALLOWED = setFor(
                new IdentValue[] {
                        IdentValue.AVOID, IdentValue.AUTO });

        protected BitSet getAllowed() {
            return ALLOWED;
        }
    }

    public static class Position extends AbstractPropertyBuilder {
        // static | relative | absolute | fixed | inherit
        private static final BitSet ALLOWED = setFor(
                new IdentValue[] {
                        IdentValue.STATIC, IdentValue.RELATIVE,
                        IdentValue.ABSOLUTE, IdentValue.FIXED });

        public List buildDeclarations(
                CSSName cssName, List values, int origin, boolean important, boolean inheritAllowed) {
            checkValueCount(cssName, 1, values.size());
            PropertyValue value = (PropertyValue)values.get(0);
            checkInheritAllowed(value, inheritAllowed);
            if (value.getCssValueType() != CSSPrimitiveValue.CSS_INHERIT) {
                if (value.getPrimitiveType() == CSSPrimitiveValue.CSS_IDENT) {
                    checkIdentType(cssName, value);
                    IdentValue ident = checkIdent(cssName, value);

                    checkValidity(cssName, getAllowed(), ident);
                } else if (value.getPropertyValueType() == PropertyValue.VALUE_TYPE_FUNCTION) {
                    FSFunction function = value.getFunction();
                    if (function.getName().equals("running")) {
                        List params = function.getParameters();
                        if (params.size() == 1) {
                            PropertyValue param = (PropertyValue)params.get(0);
                            if (param.getPrimitiveType() != CSSPrimitiveValue.CSS_IDENT) {
                                throw new CSSParseException("The running function takes an identifier as a parameter", -1);
                            }
                        } else {
                            throw new CSSParseException("The running function takes one parameter", -1);
                        }
                    } else {
                        throw new CSSParseException("Only the running function is supported here", -1);
                    }
                } else {
                    throw new CSSParseException("Value for " + cssName + " must be an identifier or function", -1);
                }
            }

            return Collections.singletonList(
                    new PropertyDeclaration(cssName, value, important, origin));

        }

        private BitSet getAllowed() {
            return ALLOWED;
        }
    }

    public static class Right extends LengthLikeWithAuto {
    }

    public static class Src extends GenericURIWithNone {
    }

    public static class TabSize extends PlainInteger {
        protected boolean isNegativeValuesAllowed() {
            return false;
        }
    }

    public static class Top extends LengthLikeWithAuto {
    }

    public static class TableLayout extends SingleIdent {
        // auto | fixed | inherit
        private static final BitSet ALLOWED = setFor(
                new IdentValue[] {
                        IdentValue.AUTO, IdentValue.FIXED });

        protected BitSet getAllowed() {
            return ALLOWED;
        }
    }

    public static class TextAlign extends SingleIdent {
        // left | right | center | justify | inherit
        private static final BitSet ALLOWED = setFor(
                new IdentValue[] {
                        IdentValue.LEFT, IdentValue.RIGHT,
                        IdentValue.CENTER, IdentValue.JUSTIFY });

        protected BitSet getAllowed() {
            return ALLOWED;
        }
    }

    public static class TextDecoration extends AbstractPropertyBuilder {
        // none | [ underline || overline || line-through || blink ] | inherit
        private static final BitSet ALLOWED = setFor(
                new IdentValue[] {
                        /* IdentValue.NONE, */ IdentValue.UNDERLINE,
                        IdentValue.OVERLINE, IdentValue.LINE_THROUGH,
                        /* IdentValue.BLINK */ });

        private BitSet getAllowed() {
            return ALLOWED;
        }

        public List buildDeclarations(
                CSSName cssName, List values, int origin, boolean important, boolean inheritAllowed) {
            if (values.size() == 1) {
                CSSPrimitiveValue value = (CSSPrimitiveValue)values.get(0);
                boolean goWithSingle = false;
                if (value.getCssValueType() == CSSPrimitiveValue.CSS_INHERIT) {
                    goWithSingle = true;
                } else {
                    checkIdentType(CSSName.TEXT_DECORATION, value);
                    IdentValue ident = checkIdent(cssName, value);
                    if (ident == IdentValue.NONE) {
                        goWithSingle = true;
                    }
                }

                if (goWithSingle) {
                    return Collections.singletonList(
                            new PropertyDeclaration(cssName, value, important, origin));
                }
            }

            for (Iterator i = values.iterator(); i.hasNext(); ) {
                PropertyValue value = (PropertyValue)i.next();
                checkInheritAllowed(value, false);
                checkIdentType(cssName, value);
                IdentValue ident = checkIdent(cssName, value);
                if (ident == IdentValue.NONE) {
                    throw new CSSParseException("Value none may not be used in this position", -1);
                }
                checkValidity(cssName, getAllowed(), ident);
            }

            return Collections.singletonList(
                    new PropertyDeclaration(cssName, new PropertyValue(values), important, origin));

        }
    }

    public static class TextIndent extends LengthLike {
    }

    public static class TextTransform extends SingleIdent {
       // capitalize | uppercase | lowercase | none | inherit
        private static final BitSet ALLOWED = setFor(
                new IdentValue[] {
                        IdentValue.CAPITALIZE, IdentValue.UPPERCASE,
                        IdentValue.LOWERCASE, IdentValue.NONE });

        protected BitSet getAllowed() {
            return ALLOWED;
        }
    }

    public static class VerticalAlign extends LengthLikeWithIdent {
        // baseline | sub | super | top | text-top | middle
        // | bottom | text-bottom | <percentage> | <length> | inherit
        private static final BitSet ALLOWED = setFor(
                new IdentValue[] {
                        IdentValue.BASELINE, IdentValue.SUB,
                        IdentValue.SUPER, IdentValue.TOP,
                        IdentValue.TEXT_TOP, IdentValue.MIDDLE,
                        IdentValue.BOTTOM, IdentValue.TEXT_BOTTOM });

        protected BitSet getAllowed() {
            return ALLOWED;
        }
    }

    public static class Visibility extends SingleIdent {
        // visible | hidden | collapse | inherit
        private static final BitSet ALLOWED = setFor(
                new IdentValue[] {
                        IdentValue.VISIBLE, IdentValue.HIDDEN, IdentValue.COLLAPSE });

        protected BitSet getAllowed() {
            return ALLOWED;
        }
    }

    public static class WhiteSpace extends SingleIdent {
        // normal | pre | nowrap | pre-wrap | pre-line | inherit
        private static final BitSet ALLOWED = setFor(
                new IdentValue[] {
                        IdentValue.NORMAL, IdentValue.PRE, IdentValue.NOWRAP,
                        IdentValue.PRE_WRAP, IdentValue.PRE_LINE});

        protected BitSet getAllowed() {
            return ALLOWED;
        }
    }

    public static class WordWrap extends SingleIdent {
        // normal | break-word
        private static final BitSet ALLOWED = setFor(
                new IdentValue[] {
                        IdentValue.NORMAL, IdentValue.BREAK_WORD});

        protected BitSet getAllowed() {
            return ALLOWED;
        }
    }
    
    public static class Hyphens extends SingleIdent {
        // none | manual | auto
        private static final BitSet ALLOWED = setFor(
                new IdentValue[] {
                        IdentValue.NONE, IdentValue.MANUAL, IdentValue.AUTO});

        protected BitSet getAllowed() {
            return ALLOWED;
        }
    }

    public static class BoxSizing extends SingleIdent {
        // border-box | content-box
        private static final BitSet ALLOWED = setFor(
                new IdentValue[] {
                        IdentValue.BORDER_BOX, IdentValue.CONTENT_BOX});

        protected BitSet getAllowed() {
            return ALLOWED;
        }
    }

    public static class Widows extends PlainInteger {
        protected boolean isNegativeValuesAllowed() {
            return false;
        }
    }

    public static class Width extends LengthLikeWithAuto {
        protected boolean isNegativeValuesAllowed() {
            return false;
        }
    }

    public static class WordSpacing extends LengthWithNormal {
    }

    public static class ZIndex extends AbstractPropertyBuilder {
        // auto | <integer> | inherit
        private static final BitSet ALLOWED = setFor(
                new IdentValue[] { IdentValue.AUTO });

        public List buildDeclarations(
                CSSName cssName, List values, int origin, boolean important, boolean inheritAllowed) {
            checkValueCount(cssName, 1, values.size());
            CSSPrimitiveValue value = (CSSPrimitiveValue)values.get(0);
            checkInheritAllowed(value, inheritAllowed);
            if (value.getCssValueType() != CSSPrimitiveValue.CSS_INHERIT) {
                checkIdentOrIntegerType(cssName, value);

                if (value.getPrimitiveType() == CSSPrimitiveValue.CSS_IDENT) {
                    IdentValue ident = checkIdent(cssName, value);
                    checkValidity(cssName, ALLOWED, ident);
                }
            }

            return Collections.singletonList(
                    new PropertyDeclaration(cssName, value, important, origin));
        }
    }
    

    private static List createTwoValueResponse(CSSName cssName, CSSPrimitiveValue value1, CSSPrimitiveValue value2,
            int origin, boolean important) {
        List values = new ArrayList(2);
        values.add(value1);
        values.add(value2);

        PropertyDeclaration result = new PropertyDeclaration(
                cssName,
                new PropertyValue(values), important, origin);

        return Collections.singletonList(result);
    }
}
