/*
 * {{{ header & license
 * StyleMapTest.java
 * Copyright (c) 2004 Torbjörn Gannholm
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.	See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 * }}}
 */

package net.homelinux.tobe.css;

import junit.framework.*;
import org.w3c.dom.*;

/**
 *
 * @author Torbjörn Gannholm
 */
public class StyleMapTest extends TestCase {
    
    public StyleMapTest(java.lang.String testName) {
        super(testName);
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(StyleMapTest.class);
        return suite;
    }
     
    public void setUp() throws Exception{
        //System.out.println("setting up");
        javax.xml.parsers.DocumentBuilderFactory f = javax.xml.parsers.DocumentBuilderFactory.newInstance();
        javax.xml.parsers.DocumentBuilder db = f.newDocumentBuilder();
        doc = db.newDocument();
        Element root = doc.createElement("root");
        doc.appendChild(root);
        Element first = doc.createElement("first");
        root.appendChild(first);
        Element firstfirst = doc.createElement("first");
        first.appendChild(firstfirst);
        Element second = doc.createElement("second");
        first.appendChild(second);
        Element secondsecond = doc.createElement("second");
        first.appendChild(secondsecond);
   }
   
    public void testDescendant() {
        System.out.println("testDescendant");
        
        java.util.List l = new java.util.LinkedList();
        
        Ruleset r = new Ruleset();
        Selector s = r.createSelector(Selector.DESCENDANT_AXIS, "root");
        r.setStyleDeclaration("rootProperty");
        l.add(r);
        
        r = new Ruleset();
        s = r.createSelector(Selector.DESCENDANT_AXIS, "first");
        r.setStyleDeclaration("firstProperty");
        l.add(r);
        
        r = new Ruleset();
        s = r.createSelector(Selector.DESCENDANT_AXIS, "first");
        s.appendChainedSelector(Selector.DESCENDANT_AXIS, "first");
        r.setStyleDeclaration("firstfirstProperty");
        l.add(r);
        
        StyleMap myStyles = StyleMap.createMap(doc, l, null);
        
        Element root = doc.getDocumentElement();
        java.util.List pl = myStyles.getMappedProperties(root);
        assertEquals("Should be one property for root", 1, pl.size());
        String str = (String) pl.get(0);
        assertEquals("root should have rootProperty", "rootProperty", str);
        
        NodeList n = root.getElementsByTagName("first");
        Element first = (Element) n.item(0);
        pl = myStyles.getMappedProperties(first);
        assertEquals("Should be one property for first", 1, pl.size());
        str = (String) pl.get(0);
        assertEquals("first should have firstProperty", "firstProperty", str);
        
        n = first.getElementsByTagName("first");
        Element firstfirst = (Element) n.item(0);
        pl = myStyles.getMappedProperties(firstfirst);
        assertEquals("Should be two properties for firstfirst", 2, pl.size());
        int flags = 0;
        for(int i=0; i < 2; i++) {
            str = (String) pl.get(i);
            if(str.equals("firstProperty")) flags += 1;
            else if(str.equals("firstfirstProperty")) flags += 2;
        }
        assertEquals("firstfirst should have firstProperty and firstfirstProperty", 3, flags);
        
        n = first.getElementsByTagName("second");
        Element second = (Element) n.item(0);
        pl = myStyles.getMappedProperties(second);
        assertEquals("Should be no properties for second", 0, pl.size());
        Element secondsecond = (Element) n.item(1);
        pl = myStyles.getMappedProperties(secondsecond);
        assertEquals("Should be no properties for secondsecond", 0, pl.size());
    }
   
    public void testChild() {
        System.out.println("testChild");
        
        java.util.List l = new java.util.LinkedList();
        
        Ruleset r = new Ruleset();
        Selector s = r.createSelector(Selector.DESCENDANT_AXIS, "root");
        s.appendChainedSelector(Selector.CHILD_AXIS, "first");
        r.setStyleDeclaration("childProperty");
        l.add(r);
        
        r = new Ruleset();
        s = r.createSelector(Selector.DESCENDANT_AXIS, "first");
        s.appendChainedSelector(Selector.CHILD_AXIS, "first");
        r.setStyleDeclaration("firstChildProperty");
        l.add(r);
        
        r = new Ruleset();
        s = r.createSelector(Selector.DESCENDANT_AXIS, "root");
        s.appendChainedSelector(Selector.CHILD_AXIS, "second");
        r.setStyleDeclaration("noChildProperty");
        l.add(r);
        
        StyleMap myStyles = StyleMap.createMap(doc, l, null);
        
        Element root = doc.getDocumentElement();
        java.util.List pl = myStyles.getMappedProperties(root);
        assertEquals("Should be no properties for root", 0, pl.size());
        
        NodeList n = root.getElementsByTagName("first");
        Element first = (Element) n.item(0);
        pl = myStyles.getMappedProperties(first);
        assertEquals("Should be one property for first", 1, pl.size());
        String str = (String) pl.get(0);
        assertEquals("first should have childProperty", "childProperty", str);
        
        n = first.getElementsByTagName("first");
        Element firstfirst = (Element) n.item(0);
        pl = myStyles.getMappedProperties(firstfirst);
        assertEquals("Should be one property for firstfirst", 1, pl.size());
        str = (String) pl.get(0);
        assertEquals("firstfirst should have firstChildProperty", "firstChildProperty", str);
        
        
        n = first.getElementsByTagName("second");
        Element second = (Element) n.item(0);
        pl = myStyles.getMappedProperties(second);
        assertEquals("Should be no properties for second", 0, pl.size());
        Element secondsecond = (Element) n.item(1);
        pl = myStyles.getMappedProperties(secondsecond);
        assertEquals("Should be no properties for secondsecond", 0, pl.size());
    }
    
    public void testImmediateSibling() {
        System.out.println("testImmediateSibling");
        
        java.util.List l = new java.util.LinkedList();
        
        Ruleset r = new Ruleset();
        Selector s = r.createSelector(Selector.DESCENDANT_AXIS, "root");
        s.appendChainedSelector(Selector.IMMEDIATE_SIBLING_AXIS, "first");
        r.setStyleDeclaration("noProperty");
        l.add(r);
        
        r = new Ruleset();
        s = r.createSelector(Selector.DESCENDANT_AXIS, "first");
        s.appendChainedSelector(Selector.IMMEDIATE_SIBLING_AXIS, "second");
        r.setStyleDeclaration("siblingProperty");
        l.add(r);
        
        StyleMap myStyles = StyleMap.createMap(doc, l, null);
        
        Element root = doc.getDocumentElement();
        java.util.List pl = myStyles.getMappedProperties(root);
        assertEquals("Should be no properties for root", 0, pl.size());
        
        NodeList n = root.getElementsByTagName("first");
        Element first = (Element) n.item(0);
        pl = myStyles.getMappedProperties(first);
        assertEquals("Should be no properties for first", 0, pl.size());
        
        n = first.getElementsByTagName("first");
        Element firstfirst = (Element) n.item(0);
        pl = myStyles.getMappedProperties(firstfirst);
        assertEquals("Should be no properties for firstfirst", 0, pl.size());
        
        n = first.getElementsByTagName("second");
        Element firstsecond = (Element) n.item(0);
        pl = myStyles.getMappedProperties(firstsecond);
        assertEquals("Should be one property for firstsecond", 1, pl.size());
        String str = (String) pl.get(0);
        assertEquals("firstsecond should have sibling property", "siblingProperty", str);
        Element secondsecond = (Element) n.item(1);
        pl = myStyles.getMappedProperties(secondsecond);
        assertEquals("Should be no properties for secondsecond", 0, pl.size());
    }
    
    public void testSortOrder() {
        System.out.println("testSortOrder");
        
        java.util.List l = new java.util.LinkedList();
        
        Ruleset r = new Ruleset();
        Selector s = r.createSelector(Selector.DESCENDANT_AXIS, "first");
        s.appendChainedSelector(Selector.IMMEDIATE_SIBLING_AXIS, "second");
        r.setStyleDeclaration(new Integer(1));
        l.add(r);
        
        r = new Ruleset();
        s = r.createSelector(Selector.DESCENDANT_AXIS, "first");
        s.appendChainedSelector(Selector.CHILD_AXIS, "second");
        r.setStyleDeclaration(new Integer(2));
        l.add(r);
        
        r = new Ruleset();
        s = r.createSelector(Selector.DESCENDANT_AXIS, "root");
        s.appendChainedSelector(Selector.DESCENDANT_AXIS, "second");
        r.setStyleDeclaration(new Integer(3));
        l.add(r);
        
        r = new Ruleset();
        s = r.createSelector(Selector.DESCENDANT_AXIS, "second");
        r.setStyleDeclaration(new Integer(4));
        l.add(r);
        
        r = new Ruleset();
        s = r.createSelector(Selector.DESCENDANT_AXIS, "first");
        s.appendChainedSelector(Selector.IMMEDIATE_SIBLING_AXIS, "second");
        r.setStyleDeclaration(new Integer(5));
        l.add(r);
        
        r = new Ruleset();
        s = r.createSelector(Selector.DESCENDANT_AXIS, "first");
        s.appendChainedSelector(Selector.CHILD_AXIS, "second");
        r.setStyleDeclaration(new Integer(6));
        l.add(r);
        
        r = new Ruleset();
        s = r.createSelector(Selector.DESCENDANT_AXIS, "root");
        s.appendChainedSelector(Selector.DESCENDANT_AXIS, "second");
        r.setStyleDeclaration(new Integer(7));
        l.add(r);
        
        r = new Ruleset();
        s = r.createSelector(Selector.DESCENDANT_AXIS, "second");
        r.setStyleDeclaration(new Integer(8));
        l.add(r);
        
        StyleMap myStyles = StyleMap.createMap(doc, l, null);
        
        Element root = doc.getDocumentElement();
        NodeList n = root.getElementsByTagName("first");
        Element first = (Element) n.item(0);
        n = first.getElementsByTagName("second");
        Element firstsecond = (Element) n.item(0);
        java.util.List pl = myStyles.getMappedProperties(firstsecond);
        assertEquals(8,pl.size());
        for(int i = 0; i < pl.size(); i++) {
            Integer ord = (Integer) pl.get(i);
            assertEquals(i+1,ord.intValue());
        }
    }
   
    public void testDynamic() {
        System.out.println("testDynamic");
        AttributeResolver no = new AttributeResolver() { public String getID(org.w3c.dom.Element e){ return null;}
            public String getClass(org.w3c.dom.Element e){ return null;}
            public String getLang(org.w3c.dom.Element e){ return null;}
            public boolean isPseudoClass(org.w3c.dom.Element e, int pc) { return false;}
            public String getAttributeValue(org.w3c.dom.Element e, String a){ return null;}};
        AttributeResolver yes = new AttributeResolver() { public String getID(org.w3c.dom.Element e){ return null;}
            public String getClass(org.w3c.dom.Element e){ return null;}
            public String getLang(org.w3c.dom.Element e){ return null;}
            public boolean isPseudoClass(org.w3c.dom.Element e, int pc) { return true;}
            public String getAttributeValue(org.w3c.dom.Element e, String a){ return null;}};
        
        java.util.List l = new java.util.LinkedList();
        
        Ruleset r = new Ruleset();
        Selector s = r.createSelector(Selector.DESCENDANT_AXIS, "root");
        s = s.appendChainedSelector(Selector.DESCENDANT_AXIS, "first");
        s.setPseudoClass(AttributeResolver.HOVER_PSEUDOCLASS);
        s = s.appendChainedSelector(Selector.DESCENDANT_AXIS, "first");
        r.setStyleDeclaration("firstfirstProperty");
        l.add(r);
        
        StyleMap myStyles = StyleMap.createMap(doc, l, no);
        
        Element root = doc.getDocumentElement();
        NodeList n = root.getElementsByTagName("first");
        Element first = (Element) n.item(0);
        n = first.getElementsByTagName("first");
        Element firstfirst = (Element) n.item(0);
        java.util.List pl = myStyles.getMappedProperties(firstfirst);
        assertEquals("Should be no properties for firstfirst", 0, pl.size());
        
        myStyles = StyleMap.createMap(doc, l, yes);
        
        root = doc.getDocumentElement();
        n = root.getElementsByTagName("first");
        first = (Element) n.item(0);
        n = first.getElementsByTagName("first");
        firstfirst = (Element) n.item(0);
        pl = myStyles.getMappedProperties(firstfirst);
        assertEquals("Should be one property for firstfirst", 1, pl.size());
    }
   
    // TODO add test methods here, they have to start with 'test' name.
    // for example:
    // public void testHello() {}
    
    private Document doc;
    
}
