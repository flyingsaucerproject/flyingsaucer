/*
 * IDResolver.java
 *
 * Created on den 27 juli 2004, 00:56
 */

package net.homelinux.tobe.css;

/**
 * In XML, an application may or may not know how to find the ID of an element.
 *
 * To enable matching of identity conditions, you need to provide an IDResolver to the StyleMap
 *
 * @author  Torbjörn Gannholm
 */
public interface IDResolver {
    
    public String getID(org.w3c.dom.Element e);
    
}
