/*
 * ClassAndIDResolver.java
 *
 * Created on den 27 juli 2004, 00:56
 */

package net.homelinux.tobe.css;

/**
 * In XML, an application may or may not know how to find the ID and/or class and/or attribute defaults of an element.
 *
 * To enable matching of identity conditions, class conditions, language, and attribute defaults you need to provide an AttributeResolver to the StyleMap.
 *
 * NOTE: The application is required to look in a document's internal subset for default attribute values,
 * but the application is not required to use its built-in knowledge of a namespace or look in the external subset.
 *
 * @author  Torbjörn Gannholm
 */
public interface AttributeResolver {
    
    /** may return null. Required to return null if attribute does not exist and not null if attribute exists. */
    public String getAttributeValue(org.w3c.dom.Element e, String attrName);
    
    /** may return null */
    public String getClass(org.w3c.dom.Element e);
    
    /** may return null */
    public String getID(org.w3c.dom.Element e);
    
    /** may return null */
    public String getLang(org.w3c.dom.Element e);
    
    public boolean isPseudoClass(org.w3c.dom.Element e, int pc);
    
    static public final int LINK_PSEUDOCLASS = 1;
    static public final int VISITED_PSEUDOCLASS = 2;
    static public final int HOVER_PSEUDOCLASS = 4;
    static public final int ACTIVE_PSEUDOCLASS = 8;
    static public final int FOCUS_PSEUDOCLASS = 16;
    
}
