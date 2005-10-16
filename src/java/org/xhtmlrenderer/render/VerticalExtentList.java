/*
 * {{{ header & license
 * Copyright (c) 2005 Torbjšrn Gannholm
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
package org.xhtmlrenderer.render;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Can find all children overlapping a certain VerticalExtent, possibly
 * returning the same child twice (but since it adds them to a Set, duplicates are removed.
 */
public class VerticalExtentList {
    List children = new ArrayList();

    public void addChild(VerticalExtent b) {
        int l = findFirstBottomAfterTop(b.getAbsTop());
        //now l is the first child whose bottom is greater than b's top
        //i.e. c.getAbsBottom() > b.getAbsTop()
        if (l >= children.size()) {
            //append b
            children.add(b);
            return;
        }
        VerticalExtent c = (VerticalExtent) children.get(l);
        if (c.getAbsTop() >= b.getAbsBottom()) {
            //insert b before c
            children.add(l, b);
        } else if (b.getAbsTop() >= c.getAbsTop() && b.getAbsBottom() <= c.getAbsBottom()) {
            //b included in c
            if (c instanceof ParentVerticalExtent) {
                ((ParentVerticalExtent) c).addChild(b);
            } else {
                ParentVerticalExtent p = new ParentVerticalExtent(c);
                p.addChild(b);
                children.set(l, p);
            }
        } else if (b.getAbsTop() <= c.getAbsTop() && b.getAbsBottom() >= c.getAbsBottom()) {
            //b includes c
            ParentVerticalExtent p = new ParentVerticalExtent(b);
            while (c != null && p.getAbsBottom() >= c.getAbsBottom()) {
                p.addChild(c);
                children.remove(l);
                if (l < children.size())
                    c = (VerticalExtent) children.get(l);
                else
                    c = null;
            }
            if (c != null && c.getAbsTop() < p.getAbsBottom()) {
                //overlap, replace c by p and add c (tail iterate)
                children.set(l, p);
                addChild(c);
            } else {
                children.add(l, p);
            }
        } else if (b.getAbsTop() > c.getAbsTop()) {
            //b overlaps c, order c.top, b.top, c.bottom, b.bottom
            PartialVerticalExtent p1 = new PartialVerticalExtent(c, c.getAbsTop(), b.getAbsTop());
            OverlapVerticalExtent p2 = new OverlapVerticalExtent(b, c, b.getAbsTop(), c.getAbsBottom());
            PartialVerticalExtent p3 = new PartialVerticalExtent(b, c.getAbsBottom(), b.getAbsBottom());
            //add the new c reps at l
            children.set(l, p2);
            children.add(l, p1);
            //tail iterate
            addChild(p3);
        } else {
            //b overlaps c, order b.top, c.top, b.bottom, c.bottom
            PartialVerticalExtent p1 = new PartialVerticalExtent(b, b.getAbsTop(), c.getAbsTop());
            OverlapVerticalExtent p2 = new OverlapVerticalExtent(c, b, c.getAbsTop(), b.getAbsBottom());
            PartialVerticalExtent p3 = new PartialVerticalExtent(c, b.getAbsBottom(), c.getAbsBottom());
            //add the new c reps at l
            children.set(l, p2);
            children.add(l, p1);
            //tail iterate
            addChild(p3);
        }
    }

    public void getIntersectingChildSet(Set intersectingChildren, double t, double b) {
        int l = findFirstBottomAfterTop(t);
        VerticalExtent c;
        while (l < children.size() && (c = (VerticalExtent) children.get(l)).getAbsTop() < b) {
            if (c instanceof VirtualVerticalExtent) {
                ((VirtualVerticalExtent) c).getIntersectingChildren(intersectingChildren, t, b);
            } else {
                intersectingChildren.add(c);
            }
            l = l + 1;
        }
    }

    private int findFirstBottomAfterTop(double t) {
        int l = 0;
        int r = children.size();
        while (l < r) {
            int m = (l + r) / 2;
            VerticalExtent c = (VerticalExtent) children.get(m);
            if (c.getAbsBottom() > t) {
                r = m;
            } else {
                l = m + 1;
            }
        }
        return l;
    }

    private interface VirtualVerticalExtent extends VerticalExtent {
        void getIntersectingChildren(Set intersectingChildren, double t, double b);
    }

    private class ParentVerticalExtent implements VirtualVerticalExtent {
        private VerticalExtent parent;
        private VerticalExtentList vel = new VerticalExtentList();

        ParentVerticalExtent(VerticalExtent p) {
            parent = p;
        }

        public double getAbsTop() {
            return parent.getAbsTop();
        }

        public double getAbsBottom() {
            return parent.getAbsBottom();
        }

        public void addChild(VerticalExtent b) {
            vel.addChild(b);
        }

        public void getIntersectingChildren(Set intersectingChildren, double t, double b) {
            final double itop = Math.max(getAbsTop(), t);
            final double ibot = Math.min(getAbsBottom(), b);
            if (ibot <= itop) return;//just to be safe
            if (parent instanceof VirtualVerticalExtent) {
                ((VirtualVerticalExtent) parent).getIntersectingChildren(intersectingChildren,
                        itop, ibot);
            } else {
                intersectingChildren.add(parent);
            }
            vel.getIntersectingChildSet(intersectingChildren,
                    itop, ibot);
        }
    }

    private class PartialVerticalExtent implements VirtualVerticalExtent {
        private double top;
        private double bottom;
        private VerticalExtent parent;

        PartialVerticalExtent(VerticalExtent p, double t, double b) {
            parent = p;
            top = t;
            bottom = b;
        }

        public double getAbsTop() {
            return top;
        }

        public double getAbsBottom() {
            return bottom;
        }

        public void getIntersectingChildren(Set intersectingChildren, double t, double b) {
            final double itop = Math.max(getAbsTop(), t);
            final double ibot = Math.min(getAbsBottom(), b);
            if (ibot <= itop) return;//just to be safe
            if (parent instanceof VirtualVerticalExtent) {
                ((VirtualVerticalExtent) parent).getIntersectingChildren(intersectingChildren,
                        itop, ibot);
            } else {
                intersectingChildren.add(parent);
            }
        }
    }

    private class OverlapVerticalExtent implements VirtualVerticalExtent {
        private VerticalExtent v1;
        private VerticalExtent v2;
        private double top;
        private double bottom;

        OverlapVerticalExtent(VerticalExtent c1, VerticalExtent c2, double t, double b) {
            v1 = c1;
            v2 = c2;
            top = t;
            bottom = b;
        }

        public double getAbsTop() {
            return top;
        }

        public double getAbsBottom() {
            return bottom;
        }

        public void getIntersectingChildren(Set intersectingChildren, double t, double b) {
            final double itop = Math.max(getAbsTop(), t);
            final double ibot = Math.min(getAbsBottom(), b);
            if (ibot <= itop) return;//just to be safe
            if (v1 instanceof VirtualVerticalExtent) {
                ((VirtualVerticalExtent) v1).getIntersectingChildren(intersectingChildren,
                        itop, ibot);
            } else {
                intersectingChildren.add(v1);
            }
            if (v2 instanceof VirtualVerticalExtent) {
                ((VirtualVerticalExtent) v2).getIntersectingChildren(intersectingChildren,
                        itop, ibot);
            } else {
                intersectingChildren.add(v2);
            }
        }
    }
}
