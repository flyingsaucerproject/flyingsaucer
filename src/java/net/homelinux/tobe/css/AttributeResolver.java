/*
 * ClassAndIDResolver.java
 *
 * Created on den 27 juli 2004, 00:56
 */

package net.homelinux.tobe.css;

/**
 * In XML, an application may or may not know how to find the ID and/or class and/or attribute defaults of an element.
 *
 * To enable matching of identity conditions, class conditions and attribute defaults you need to provide a ClassAndIDResolver to the StyleMap.
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
    
}
