/*
 * ClassAndIDResolver.java
 *
 * Created on den 27 juli 2004, 00:56
 */

package net.homelinux.tobe.css;

/**
 * In XML, an application may or may not know how to find the ID and/or class of an element.
 *
 * To enable matching of identity conditions, you need to provide a ClassAndIDResolver to the StyleMap
 *
 * @author  Torbjörn Gannholm
 */
public interface ClassAndIDResolver {
    
    /** may return null */
    public String getClass(org.w3c.dom.Element e);
    
    /** may return null */
    public String getID(org.w3c.dom.Element e);
    
}
