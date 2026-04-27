package org.xhtmlrenderer.css.value;

import org.xhtmlrenderer.css.constants.IdentValue;

/**
 * User: tobe
 * Date: 2005-jun-23
 */
public record FontSpecification(
    float size,
    IdentValue fontWeight,
    String[] families,
    IdentValue fontStyle,
    IdentValue variant) {
}
