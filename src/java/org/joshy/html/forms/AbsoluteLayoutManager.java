package org.joshy.html.forms;

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
