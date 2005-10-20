package org.xhtmlrenderer.css.value;

import org.xhtmlrenderer.css.constants.IdentValue;

import java.util.Arrays;

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
        StringBuffer sb = new StringBuffer("Font specification: ");
        sb
                .append(" families: " + Arrays.asList(families).toString())
                .append(" size: " + size)
                .append(" weight: " + fontWeight)
                .append(" style: " + fontStyle)
                .append(" variant: " + variant);
        return sb.toString();
    }
}
