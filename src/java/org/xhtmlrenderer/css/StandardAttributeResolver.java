package org.xhtmlrenderer.css;

import org.w3c.dom.Element;
import org.xhtmlrenderer.css.newmatch.AttributeResolver;
import org.xhtmlrenderer.extend.NamespaceHandler;
import org.xhtmlrenderer.extend.UserAgentCallback;
import org.xhtmlrenderer.extend.UserInterface;

/**
 * Created by IntelliJ IDEA.
 * User: tobe
 * Date: 2005-jan-07
 * Time: 23:23:30
 * To change this template use File | Settings | File Templates.
 */
public class StandardAttributeResolver implements AttributeResolver {
    private NamespaceHandler nsh;
    private UserAgentCallback uac;
    private UserInterface ui;

    public StandardAttributeResolver(NamespaceHandler nsh, UserAgentCallback uac, UserInterface ui) {
        this.nsh = nsh;
        this.uac = uac;
        this.ui = ui;
    }

    public String getAttributeValue(Element e, String attrName) {
        return nsh.getAttributeValue(e, attrName);
    }

    public String getClass(Element e) {
        return nsh.getClass(e);
    }

    public String getID(Element e) {
        return nsh.getID(e);
    }

    public String getElementStyling(Element e) {
        return nsh.getElementStyling(e);
    }

    public String getLang(Element e) {
        return nsh.getLang(e);
    }

    public boolean isLink(Element e) {
        return nsh.getLinkUri(e) != null;
    }

    public boolean isVisited(Element e) {
        return uac.isVisited(nsh.getLinkUri(e));
    }

    public boolean isHover(Element e) {
        return ui.isHover(e);
    }

    public boolean isActive(Element e) {
        return ui.isActive(e);
    }

    public boolean isFocus(Element e) {
        return ui.isFocus(e);
    }
}
