package org.xhtmlrenderer.css.value;

import org.xhtmlrenderer.css.constants.IdentValue;

import java.util.List;

/**
 * User: tobe
 * Date: 2005-jun-23
 */
public class FontSpecification {
    public float size;
    public IdentValue fontWeight;
    public String[] families;
    public IdentValue fontStyle;
    public IdentValue variant;

    @Override
    public String toString() {
        return String.format("Font specification:  families: %s size: %s weight: %s style: %s variant: %s",
            List.of(families), size, fontWeight, fontStyle, variant);
    }
}
