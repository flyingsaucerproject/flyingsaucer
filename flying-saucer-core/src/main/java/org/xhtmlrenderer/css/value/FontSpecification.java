package org.xhtmlrenderer.css.value;

import org.xhtmlrenderer.css.constants.IdentValue;

import static java.util.Arrays.asList;

/**
 * Created by IntelliJ IDEA.
 * User: tobe
 * Date: 2005-jun-23
 * Time: 00:28:43
 * To change this template use File | Settings | File Templates.
 */
public class FontSpecification {
    public float size;
    public IdentValue fontWeight;
    public String[] families;
    public IdentValue fontStyle;
    public IdentValue variant;

    public String toString() {
        return String.format("Font specification:  families: %s size: %s weight: %s style: %s variant: %s", 
                asList(families), size, fontWeight, fontStyle, variant);
    }
}
