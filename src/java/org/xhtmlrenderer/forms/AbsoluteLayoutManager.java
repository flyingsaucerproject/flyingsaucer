
/* 
 * {{{ header & license 
 * Copyright (c) 2004 Joshua Marinacci 
 * 
 * This program is free software; you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation; either version 2.1 
 * of the License, or (at your option) any later version. 
 * 
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.	See the 
 * GNU Lesser General Public License for more details. 
 * 
 * You should have received a copy of the GNU Lesser General Public License 
 * along with this program; if not, write to the Free Software 
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA. 
 * }}} 
 */

package org.xhtmlrenderer.forms;

import java.awt.LayoutManager;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
public class AbsoluteLayoutManager implements LayoutManager {
    public AbsoluteLayoutManager() {
    }

    public void addLayoutComponent(String name, Component comp) {
    }

    public void layoutContainer(Container target) {

        int ncomponents = target.countComponents();
        for (int i = 0 ; i < ncomponents ; i++) {
            Component comp = target.getComponent(i);
            int x = comp.getX();
            int y = comp.getY();
            Dimension size = comp.getPreferredSize();
            comp.reshape(x,y,size.width,size.height);
        }

    }
    public Dimension minimumLayoutSize(Container parent) {
        return parent.size();
    }
    public Dimension preferredLayoutSize(Container parent) {
        return parent.size();
    }
    public void removeLayoutComponent(Component comp) {
    }
}
