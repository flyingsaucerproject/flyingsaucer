package org.xhtmlrenderer.layout.content;

import org.w3c.dom.Element;
import org.xhtmlrenderer.css.style.CalculatedStyle;
import org.xhtmlrenderer.layout.Context;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: tobe
 * Date: 2004-dec-09
 * Time: 00:10:54
 * To change this template use File | Settings | File Templates.
 */
public class AnonymousBlockContent implements Content {
    private Element _elem;
    private CalculatedStyle _style;
    private List _inline;

    public AnonymousBlockContent(Element parent, CalculatedStyle style, List inlineList) {
        _elem = parent;
        _style = style;
        _inline = inlineList;
    }

    public Element getElement() {
        return _elem;
    }

    public List getContent(Context c) {
        return _inline;
    }

    public CalculatedStyle getStyle() {
        return _style;
    }
}
