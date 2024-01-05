package org.xhtmlrenderer.pdf;

import com.lowagie.text.pdf.BaseFont;
import org.xhtmlrenderer.css.constants.IdentValue;

public class FontDescription {
    private IdentValue _style;
    private int _weight;

    private BaseFont _font;

    private float _underlinePosition;
    private float _underlineThickness;

    private float _yStrikeoutSize;
    private float _yStrikeoutPosition;

    private boolean _isFromFontFace;

    public FontDescription() {
    }

    public FontDescription(BaseFont font) {
        this(font, IdentValue.NORMAL, 400);
    }

    public FontDescription(BaseFont font, IdentValue style, int weight) {
        _font = font;
        _style = style;
        _weight = weight;
        setMetricDefaults();
    }

    public BaseFont getFont() {
        return _font;
    }

    public void setFont(BaseFont font) {
        _font = font;
    }

    public int getWeight() {
        return _weight;
    }

    public void setWeight(int weight) {
        _weight = weight;
    }

    public IdentValue getStyle() {
        return _style;
    }

    public void setStyle(IdentValue style) {
        _style = style;
    }

    public float getUnderlinePosition() {
        return _underlinePosition;
    }

    /**
     * This refers to the top of the underline stroke
     */
    public void setUnderlinePosition(float underlinePosition) {
        _underlinePosition = underlinePosition;
    }

    public float getUnderlineThickness() {
        return _underlineThickness;
    }

    public void setUnderlineThickness(float underlineThickness) {
        _underlineThickness = underlineThickness;
    }

    public float getYStrikeoutPosition() {
        return _yStrikeoutPosition;
    }

    public void setYStrikeoutPosition(float strikeoutPosition) {
        _yStrikeoutPosition = strikeoutPosition;
    }

    public float getYStrikeoutSize() {
        return _yStrikeoutSize;
    }

    public void setYStrikeoutSize(float strikeoutSize) {
        _yStrikeoutSize = strikeoutSize;
    }

    private void setMetricDefaults() {
        _underlinePosition = -50;
        _underlineThickness = 50;

        int[] box = _font.getCharBBox('x');
        if (box != null) {
            _yStrikeoutPosition = box[3] / 2 + 50;
            _yStrikeoutSize = 100;
        } else {
            // Do what the JDK does, size will be calculated by ITextTextRenderer
            _yStrikeoutPosition = _font.getFontDescriptor(BaseFont.BBOXURY, 1000.0f) / 3.0f;
        }
    }

    public boolean isFromFontFace() {
        return _isFromFontFace;
    }

    public void setFromFontFace(boolean isFromFontFace) {
        _isFromFontFace = isFromFontFace;
    }

    @Override
    public String toString() {
        return String.format("Font %s:%s", _font.getPostscriptFontName(), _weight);
    }
}
