package org.xhtmlrenderer.context;

import org.w3c.dom.DOMException;
import org.w3c.dom.css.*;

public class CSSPageRuleAdapter implements CSSStyleRule {
    private CSSPageRule _pageRule;

    public CSSPageRuleAdapter(CSSPageRule pageRule) {
        _pageRule = pageRule;
    }

    public String getCssText() {
        return _pageRule.getCssText();
    }

    public CSSRule getParentRule() {
        return _pageRule.getParentRule();
    }

    public CSSStyleSheet getParentStyleSheet() {
        return _pageRule.getParentStyleSheet();
    }

    public String getSelectorText() {
        return _pageRule.getSelectorText();
    }

    public CSSStyleDeclaration getStyle() {
        return _pageRule.getStyle();
    }

    public short getType() {
        return _pageRule.getType();
    }

    public void setCssText(String cssText) throws DOMException {
        _pageRule.setCssText(cssText);
    }

    public void setSelectorText(String selectorText) throws DOMException {
        _pageRule.setSelectorText(selectorText);
    }
}
