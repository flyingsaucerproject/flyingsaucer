/*
 * StaticHtmlAttributeResolver.java
 *
 * Created on den 31 juli 2004, 01:17
 */

package org.joshy.html.css;

/**
 *
 * @author  Torbjörn Gannholm
 */
public class StaticHtmlAttributeResolver implements net.homelinux.tobe.css.AttributeResolver {
    
    /** Creates a new instance of StaticHtmlAttributeResolver */
    public StaticHtmlAttributeResolver() {
    }
    
    public String getAttributeValue(org.w3c.dom.Element e, String attrName) {
        return e.getAttribute(attrName);
    }
    
    public String getClass(org.w3c.dom.Element e) {
        return e.getAttribute("class");
    }
    
    public String getID(org.w3c.dom.Element e) {
        return e.getAttribute("id");
   }
    
    public String getLang(org.w3c.dom.Element e) {
        return e.getAttribute("lang");
    }
    
    public boolean isPseudoClass(org.w3c.dom.Element e, int pc) {
        if(pc == LINK_PSEUDOCLASS && e.getNodeName().equalsIgnoreCase("a") && !e.getAttribute("href").equals("")) return true;
        return false;
    }
    
}
