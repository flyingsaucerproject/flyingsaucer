package org.xhtmlrenderer.extend;

/**
 * Created by IntelliJ IDEA.
 * User: tobe
 * Date: 2005-jan-07
 * Time: 23:20:40
 * To change this template use File | Settings | File Templates.
 */
public interface UserInterface {

    public boolean isHover(org.w3c.dom.Element e);

    public boolean isActive(org.w3c.dom.Element e);

    public boolean isFocus(org.w3c.dom.Element e);
}
