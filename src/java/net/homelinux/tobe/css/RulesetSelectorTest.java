/*
 *
 * RulesetSelectorTest.java
 * Copyright (c) 2004 Torbjörn Gannholm
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
 *
 */


package net.homelinux.tobe.css;

import junit.framework.*;
import org.w3c.dom.*;

/**
 *
 * @author Torbjörn Gannholm
 */
public class RulesetSelectorTest extends TestCase {
    
    public RulesetSelectorTest(java.lang.String testName) {
        super(testName);
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(RulesetSelectorTest.class);
        return suite;
    }
    
    public void setUp() throws Exception{
        //System.out.println("setting up");
        javax.xml.parsers.DocumentBuilderFactory f = javax.xml.parsers.DocumentBuilderFactory.newInstance();
        javax.xml.parsers.DocumentBuilder db = f.newDocumentBuilder();
        doc = db.newDocument();
    }
    
    /** test that the selector for any element works */
    public void testAny() {
        System.out.println("testAny");
        Ruleset rules = new Ruleset();
        Selector sel = rules.createSelector(Selector.DESCENDANT_AXIS, null);
        Element e = doc.createElement("e");
        assertTrue("selector for any should match 'e'",  sel.matches(e, null));
        Element f = doc.createElement("f");
        assertTrue("selector for any should match 'f'",  sel.matches(f, null));
    }
    
    /** test that the selector for a named element works */
    public void testNamed() {
        System.out.println("testNamed");
        Ruleset rules = new Ruleset();
        Selector sel = rules.createSelector(Selector.DESCENDANT_AXIS, "e");
        Element e = doc.createElement("e");
        assertTrue("selector for 'e' should match "+e.getNodeName(),  sel.matches(e, null));
        Element f = doc.createElement("f");
        assertFalse("selector for 'e' should not match 'f'",  sel.matches(f, null));
    }
    
    /** test that an id condition works */
    public void testId() {
        System.out.println("testId");
        Ruleset rules = new Ruleset();
        Selector sel = rules.createSelector(Selector.DESCENDANT_AXIS, "e");
        sel.addIDCondition("eid");
        AttributeResolver eid = new AttributeResolver() { public String getID(org.w3c.dom.Element e){ return "eid";}
            public String getClass(org.w3c.dom.Element e){ return null;}
            public String getLang(org.w3c.dom.Element e){ return null;}
            public boolean isPseudoClass(org.w3c.dom.Element e, int pc) { return false;}
            public String getAttributeValue(org.w3c.dom.Element e, String a){ return null;}};
        AttributeResolver fid = new AttributeResolver() { public String getID(org.w3c.dom.Element e){ return "fid";}
            public String getClass(org.w3c.dom.Element e){ return null;}
            public String getLang(org.w3c.dom.Element e){ return null;}
            public boolean isPseudoClass(org.w3c.dom.Element e, int pc) { return false;}
            public String getAttributeValue(org.w3c.dom.Element e, String a){ return null;}};
        AttributeResolver noid = new AttributeResolver() { public String getID(org.w3c.dom.Element e){ return null;}
            public String getClass(org.w3c.dom.Element e){ return null;}
            public String getLang(org.w3c.dom.Element e){ return null;}
            public boolean isPseudoClass(org.w3c.dom.Element e, int pc) { return false;}
            public String getAttributeValue(org.w3c.dom.Element e, String a){ return null;}};
        Element e = doc.createElement("e");
        assertTrue("selector for 'e#eid' should match e#eid",  sel.matches(e, eid));
        assertFalse("selector for 'e#eid' should not match e#fid",  sel.matches(e, fid));
        Element f = doc.createElement("f");
        assertFalse("selector for 'e#eid' should not match f#eid",  sel.matches(f, eid));
        Selector selAny = rules.createSelector(Selector.DESCENDANT_AXIS, null);
        selAny.addIDCondition("eid");
        assertTrue("selector for '*#eid' should match e#eid",  selAny.matches(e, eid));
        assertFalse("selector for '*#eid' should not match e#fid",  selAny.matches(e, fid));
        assertTrue("selector for '*#eid' should match f#eid",  selAny.matches(f, eid));
        assertFalse("selector for '*#eid' should not match with null ID",  selAny.matches(e, noid));
        assertFalse("selector for '*#eid' should not match with no IDResolver",  selAny.matches(e, null));
    }
    
    /** test that a class condition works */
    public void testClass() {
        System.out.println("testClass");
        Ruleset rules = new Ruleset();
        Selector sel = rules.createSelector(Selector.DESCENDANT_AXIS, "e");
        sel.addClassCondition("a");
        AttributeResolver a = new AttributeResolver() { public String getID(org.w3c.dom.Element e){ return null;}
            public String getClass(org.w3c.dom.Element e){ return "a";}
            public String getLang(org.w3c.dom.Element e){ return null;}
            public boolean isPseudoClass(org.w3c.dom.Element e, int pc) { return false;}
            public String getAttributeValue(org.w3c.dom.Element e, String a){ return null;}};
        AttributeResolver b = new AttributeResolver() { public String getID(org.w3c.dom.Element e){ return null;}
            public String getClass(org.w3c.dom.Element e){ return "b";}
            public String getLang(org.w3c.dom.Element e){ return null;}
            public boolean isPseudoClass(org.w3c.dom.Element e, int pc) { return false;}
            public String getAttributeValue(org.w3c.dom.Element e, String a){ return null;}};
        Element e = doc.createElement("e");
        assertTrue("selector for 'e.a' should match class=a",  sel.matches(e, a));
        assertFalse("selector for 'e.a' should not match class=b",  sel.matches(e, b));
        AttributeResolver abc = new AttributeResolver() { public String getID(org.w3c.dom.Element e){ return null;}
            public String getClass(org.w3c.dom.Element e){ return "a b c";}
            public String getLang(org.w3c.dom.Element e){ return null;}
            public boolean isPseudoClass(org.w3c.dom.Element e, int pc) { return false;}
            public String getAttributeValue(org.w3c.dom.Element e, String a){ return null;}};
        assertTrue("selector for 'e.a' should match class='a b c'",  sel.matches(e, abc));
        sel.addClassCondition("b");
        assertTrue("selector for 'e.a.b' should match class='a b c'",  sel.matches(e, abc));
        sel.addClassCondition("c");
        assertTrue("selector for 'e.a.b.c' should match class='a b c'",  sel.matches(e, abc));
        sel.addClassCondition("d");
        assertFalse("selector for 'e.a.b.c.d' should not match class='a b c'",  sel.matches(e, abc));
        sel = rules.createSelector(Selector.DESCENDANT_AXIS, "e");
        sel.addClassCondition("b");
        assertTrue("selector for 'e.b' should match class='a b c'",  sel.matches(e, abc));
        sel = rules.createSelector(Selector.DESCENDANT_AXIS, "e");
        sel.addClassCondition("c");
        assertTrue("selector for 'e.c' should match class='a b c'",  sel.matches(e, abc));
        sel = rules.createSelector(Selector.DESCENDANT_AXIS, "e");
        sel.addClassCondition("d");
        assertFalse("selector for 'e.d' should not match class='a b c'",  sel.matches(e, abc));
        AttributeResolver none = new AttributeResolver() { public String getID(org.w3c.dom.Element e){ return null;}
            public String getClass(org.w3c.dom.Element e){ return null;}
            public String getLang(org.w3c.dom.Element e){ return null;}
            public boolean isPseudoClass(org.w3c.dom.Element e, int pc) { return false;}
            public String getAttributeValue(org.w3c.dom.Element e, String a){ return null;}};
        assertFalse("selector for 'e.d' should not match null class",  sel.matches(e, none));
        assertFalse("selector for 'e.d' should not match no Resolver",  sel.matches(e, null));
   }
    
    public void testAttributeExists() {
        System.out.println("testAttributeExists");
        Ruleset rules = new Ruleset();
        Selector sel = rules.createSelector(Selector.DESCENDANT_AXIS, "e");
        sel.addAttributeExistsCondition("a");
        AttributeResolver none = new AttributeResolver() { public String getID(org.w3c.dom.Element e){ return null;}
            public String getClass(org.w3c.dom.Element e){ return null;}
            public String getLang(org.w3c.dom.Element e){ return null;}
            public boolean isPseudoClass(org.w3c.dom.Element e, int pc) { return false;}
            public String getAttributeValue(org.w3c.dom.Element e, String a){ return null;}};
        AttributeResolver empty = new AttributeResolver() { public String getID(org.w3c.dom.Element e){ return null;}
            public String getClass(org.w3c.dom.Element e){ return null;}
            public String getLang(org.w3c.dom.Element e){ return null;}
            public boolean isPseudoClass(org.w3c.dom.Element e, int pc) { return false;}
            public String getAttributeValue(org.w3c.dom.Element e, String a){ return "";}};
        Element e = doc.createElement("e");
        assertTrue("selector for 'e[a]' should match a=''",  sel.matches(e, empty));
        assertFalse("selector for 'e[a]' should not match null attribute",  sel.matches(e, none));
        AttributeResolver v = new AttributeResolver() { public String getID(org.w3c.dom.Element e){ return null;}
            public String getClass(org.w3c.dom.Element e){ return null;}
            public String getLang(org.w3c.dom.Element e){ return null;}
            public boolean isPseudoClass(org.w3c.dom.Element e, int pc) { return false;}
            public String getAttributeValue(org.w3c.dom.Element e, String a){ return "v";}};
        assertTrue("selector for 'e[a]' should match a='v'",  sel.matches(e, v));
        assertFalse("selector for 'e[a]' should not match no Resolver",  sel.matches(e, null));
   }
    
    public void testAttributeEquals() {
        System.out.println("testAttributeEquals");
        Ruleset rules = new Ruleset();
        Selector sel = rules.createSelector(Selector.DESCENDANT_AXIS, "e");
        sel.addAttributeEqualsCondition("a", "v");
        AttributeResolver none = new AttributeResolver() { public String getID(org.w3c.dom.Element e){ return null;}
            public String getClass(org.w3c.dom.Element e){ return null;}
            public String getLang(org.w3c.dom.Element e){ return null;}
            public boolean isPseudoClass(org.w3c.dom.Element e, int pc) { return false;}
            public String getAttributeValue(org.w3c.dom.Element e, String a){ return null;}};
        AttributeResolver empty = new AttributeResolver() { public String getID(org.w3c.dom.Element e){ return null;}
            public String getClass(org.w3c.dom.Element e){ return null;}
            public String getLang(org.w3c.dom.Element e){ return null;}
            public boolean isPseudoClass(org.w3c.dom.Element e, int pc) { return false;}
            public String getAttributeValue(org.w3c.dom.Element e, String a){ return "";}};
        Element e = doc.createElement("e");
        assertFalse("selector for 'e[a=v]' should not match a=''",  sel.matches(e, empty));
        assertFalse("selector for 'e[a=v]' should not match null attribute",  sel.matches(e, none));
        AttributeResolver v = new AttributeResolver() { public String getID(org.w3c.dom.Element e){ return null;}
            public String getClass(org.w3c.dom.Element e){ return null;}
            public String getLang(org.w3c.dom.Element e){ return null;}
            public boolean isPseudoClass(org.w3c.dom.Element e, int pc) { return false;}
            public String getAttributeValue(org.w3c.dom.Element e, String a){ return "v";}};
        assertTrue("selector for 'e[a=v]' should match a='v'",  sel.matches(e, v));
        assertFalse("selector for 'e[a=v]' should not match no Resolver",  sel.matches(e, null));
        sel = rules.createSelector(Selector.DESCENDANT_AXIS, "e");
        sel.addAttributeEqualsCondition("a", "w");
        assertFalse("selector for 'e[a=w]' should not match a='v'",  sel.matches(e, v));
   }
    
    public void testAttributeMatchesList() {
        System.out.println("testAttributeMatchesList");
        Ruleset rules = new Ruleset();
        Selector sel = rules.createSelector(Selector.DESCENDANT_AXIS, "e");
        sel.addAttributeMatchesListCondition("a", "v");
        Element e = doc.createElement("e");
        assertFalse("selector for 'e[a~=v]' should not match no Resolver",  sel.matches(e, null));
        AttributeResolver none = new AttributeResolver() { public String getID(org.w3c.dom.Element e){ return null;}
            public String getClass(org.w3c.dom.Element e){ return null;}
            public String getLang(org.w3c.dom.Element e){ return null;}
            public boolean isPseudoClass(org.w3c.dom.Element e, int pc) { return false;}
            public String getAttributeValue(org.w3c.dom.Element e, String a){ return null;}};
        AttributeResolver empty = new AttributeResolver() { public String getID(org.w3c.dom.Element e){ return null;}
            public String getClass(org.w3c.dom.Element e){ return null;}
            public String getLang(org.w3c.dom.Element e){ return null;}
            public boolean isPseudoClass(org.w3c.dom.Element e, int pc) { return false;}
            public String getAttributeValue(org.w3c.dom.Element e, String a){ return "";}};
        assertFalse("selector for 'e[a~=v]' should not match a=''",  sel.matches(e, empty));
        assertFalse("selector for 'e[a~=v]' should not match null attribute",  sel.matches(e, none));
        AttributeResolver v = new AttributeResolver() { public String getID(org.w3c.dom.Element e){ return null;}
            public String getClass(org.w3c.dom.Element e){ return null;}
            public String getLang(org.w3c.dom.Element e){ return null;}
            public boolean isPseudoClass(org.w3c.dom.Element e, int pc) { return false;}
            public String getAttributeValue(org.w3c.dom.Element e, String a){ return "v";}};
        assertTrue("selector for 'e[a~=v]' should match a='v'",  sel.matches(e, v));
        AttributeResolver vwx = new AttributeResolver() { public String getID(org.w3c.dom.Element e){ return null;}
            public String getClass(org.w3c.dom.Element e){ return null;}
            public String getLang(org.w3c.dom.Element e){ return null;}
            public boolean isPseudoClass(org.w3c.dom.Element e, int pc) { return false;}
            public String getAttributeValue(org.w3c.dom.Element e, String a){ return "v w x";}};
        assertTrue("selector for 'e[a~=v]' should match a='v w x'",  sel.matches(e, vwx));
        sel = rules.createSelector(Selector.DESCENDANT_AXIS, "e");
        sel.addAttributeMatchesListCondition("a", "w");
        assertFalse("selector for 'e[a~=w]' should not match a='v'",  sel.matches(e, v));
        assertTrue("selector for 'e[a~=w]' should match a='v w x'",  sel.matches(e, vwx));
        sel = rules.createSelector(Selector.DESCENDANT_AXIS, "e");
        sel.addAttributeMatchesListCondition("a", "x");
        assertTrue("selector for 'e[a~=x]' should match a='v w x'",  sel.matches(e, vwx));
   }
    
    public void testAttributeMatchesFirstPart() {
        System.out.println("testAttributeMatchesFirstPart");
        Ruleset rules = new Ruleset();
        Selector sel = rules.createSelector(Selector.DESCENDANT_AXIS, "e");
        sel.addAttributeMatchesFirstPartCondition("a", "v");
        Element e = doc.createElement("e");
        assertFalse("selector for 'e[a|=v]' should not match no Resolver",  sel.matches(e, null));
        AttributeResolver none = new AttributeResolver() { public String getID(org.w3c.dom.Element e){ return null;}
            public String getClass(org.w3c.dom.Element e){ return null;}
            public String getLang(org.w3c.dom.Element e){ return null;}
            public boolean isPseudoClass(org.w3c.dom.Element e, int pc) { return false;}
            public String getAttributeValue(org.w3c.dom.Element e, String a){ return null;}};
        AttributeResolver empty = new AttributeResolver() { public String getID(org.w3c.dom.Element e){ return null;}
            public String getClass(org.w3c.dom.Element e){ return null;}
            public String getLang(org.w3c.dom.Element e){ return null;}
            public boolean isPseudoClass(org.w3c.dom.Element e, int pc) { return false;}
            public String getAttributeValue(org.w3c.dom.Element e, String a){ return "";}};
        assertFalse("selector for 'e[a|=v]' should not match a=''",  sel.matches(e, empty));
        assertFalse("selector for 'e[a|=v]' should not match null attribute",  sel.matches(e, none));
        AttributeResolver v = new AttributeResolver() { public String getID(org.w3c.dom.Element e){ return null;}
            public String getClass(org.w3c.dom.Element e){ return null;}
            public String getLang(org.w3c.dom.Element e){ return null;}
            public boolean isPseudoClass(org.w3c.dom.Element e, int pc) { return false;}
            public String getAttributeValue(org.w3c.dom.Element e, String a){ return "v";}};
        assertTrue("selector for 'e[a|=v]' should match a='v'",  sel.matches(e, v));
        AttributeResolver vwx = new AttributeResolver() { public String getID(org.w3c.dom.Element e){ return null;}
            public String getClass(org.w3c.dom.Element e){ return null;}
            public String getLang(org.w3c.dom.Element e){ return null;}
            public boolean isPseudoClass(org.w3c.dom.Element e, int pc) { return false;}
            public String getAttributeValue(org.w3c.dom.Element e, String a){ return "v-w-x";}};
        assertTrue("selector for 'e[a|=v]' should match a='v-w-x'",  sel.matches(e, vwx));
        sel = rules.createSelector(Selector.DESCENDANT_AXIS, "e");
        sel.addAttributeMatchesFirstPartCondition("a", "w");
        assertFalse("selector for 'e[a|=w]' should not match a='v'",  sel.matches(e, v));
        assertFalse("selector for 'e[a|=w]' should not match a='v-w-x'",  sel.matches(e, vwx));
        sel = rules.createSelector(Selector.DESCENDANT_AXIS, "e");
        sel.addAttributeMatchesFirstPartCondition("a", "x");
        assertFalse("selector for 'e[a|=x]' should not match a='v-w-x'",  sel.matches(e, vwx));
   }
    
    public void testLanguage() {
        System.out.println("testLanguage");
        Ruleset rules = new Ruleset();
        Selector sel = rules.createSelector(Selector.DESCENDANT_AXIS, "e");
        sel.addLangCondition("v");
        Element e = doc.createElement("e");
        assertFalse("selector for ':lang(v)' should not match no Resolver",  sel.matches(e, null));
        AttributeResolver none = new AttributeResolver() { public String getID(org.w3c.dom.Element e){ return null;}
            public String getClass(org.w3c.dom.Element e){ return null;}
            public String getLang(org.w3c.dom.Element e){ return null;}
            public boolean isPseudoClass(org.w3c.dom.Element e, int pc) { return false;}
            public String getAttributeValue(org.w3c.dom.Element e, String a){ return null;}};
        AttributeResolver empty = new AttributeResolver() { public String getID(org.w3c.dom.Element e){ return null;}
            public String getClass(org.w3c.dom.Element e){ return null;}
            public String getLang(org.w3c.dom.Element e){ return "";}
            public boolean isPseudoClass(org.w3c.dom.Element e, int pc) { return false;}
            public String getAttributeValue(org.w3c.dom.Element e, String a){ return null;}};
        assertFalse("selector for 'lang(v)' should not match ''",  sel.matches(e, empty));
        assertFalse("selector for 'lang(v)' should not match null attribute",  sel.matches(e, none));
        AttributeResolver v = new AttributeResolver() { public String getID(org.w3c.dom.Element e){ return null;}
            public String getClass(org.w3c.dom.Element e){ return null;}
            public String getLang(org.w3c.dom.Element e){ return "v";}
            public boolean isPseudoClass(org.w3c.dom.Element e, int pc) { return false;}
            public String getAttributeValue(org.w3c.dom.Element e, String a){ return null;}};
        assertTrue("selector for 'lang(v)' should match 'v'",  sel.matches(e, v));
        AttributeResolver vwx = new AttributeResolver() { public String getID(org.w3c.dom.Element e){ return null;}
            public String getClass(org.w3c.dom.Element e){ return null;}
            public String getLang(org.w3c.dom.Element e){ return "v-w-x";}
            public boolean isPseudoClass(org.w3c.dom.Element e, int pc) { return false;}
            public String getAttributeValue(org.w3c.dom.Element e, String a){ return null;}};
        assertTrue("selector for 'lang(v)' should match 'v-w-x'",  sel.matches(e, vwx));
        sel = rules.createSelector(Selector.DESCENDANT_AXIS, "e");
        sel.addLangCondition("w");
        assertFalse("selector for 'lang(w)' should not match a='v'",  sel.matches(e, v));
        assertFalse("selector for 'lang(w)' should not match a='v-w-x'",  sel.matches(e, vwx));
        sel = rules.createSelector(Selector.DESCENDANT_AXIS, "e");
        sel.addLangCondition("x");
        assertFalse("selector for 'lang(x)' should not match a='v-w-x'",  sel.matches(e, vwx));
   }
    
    public void testPseudoClass() {
        System.out.println("testPseudoClass");
        Ruleset rules = new Ruleset();
        Selector sel = rules.createSelector(Selector.DESCENDANT_AXIS, "e");
        assertFalse(sel.isPseudoClass(AttributeResolver.LINK_PSEUDOCLASS));
        assertFalse(sel.isPseudoClass(AttributeResolver.VISITED_PSEUDOCLASS));
        assertFalse(sel.isPseudoClass(AttributeResolver.HOVER_PSEUDOCLASS));
        assertFalse(sel.isPseudoClass(AttributeResolver.ACTIVE_PSEUDOCLASS));
        assertFalse(sel.isPseudoClass(AttributeResolver.FOCUS_PSEUDOCLASS));
        sel = rules.createSelector(Selector.DESCENDANT_AXIS, "e");
        sel.setPseudoClass(AttributeResolver.LINK_PSEUDOCLASS);
        assertTrue(sel.isPseudoClass(AttributeResolver.LINK_PSEUDOCLASS));
        assertFalse(sel.isPseudoClass(AttributeResolver.VISITED_PSEUDOCLASS));
        assertFalse(sel.isPseudoClass(AttributeResolver.HOVER_PSEUDOCLASS));
        assertFalse(sel.isPseudoClass(AttributeResolver.ACTIVE_PSEUDOCLASS));
        assertFalse(sel.isPseudoClass(AttributeResolver.FOCUS_PSEUDOCLASS));
        sel = rules.createSelector(Selector.DESCENDANT_AXIS, "e");
        sel.setPseudoClass(AttributeResolver.VISITED_PSEUDOCLASS);
        assertFalse(sel.isPseudoClass(AttributeResolver.LINK_PSEUDOCLASS));
        assertTrue(sel.isPseudoClass(AttributeResolver.VISITED_PSEUDOCLASS));
        assertFalse(sel.isPseudoClass(AttributeResolver.HOVER_PSEUDOCLASS));
        assertFalse(sel.isPseudoClass(AttributeResolver.ACTIVE_PSEUDOCLASS));
        assertFalse(sel.isPseudoClass(AttributeResolver.FOCUS_PSEUDOCLASS));
        sel = rules.createSelector(Selector.DESCENDANT_AXIS, "e");
        sel.setPseudoClass(AttributeResolver.HOVER_PSEUDOCLASS);
        assertFalse(sel.isPseudoClass(AttributeResolver.LINK_PSEUDOCLASS));
        assertFalse(sel.isPseudoClass(AttributeResolver.VISITED_PSEUDOCLASS));
        assertTrue(sel.isPseudoClass(AttributeResolver.HOVER_PSEUDOCLASS));
        assertFalse(sel.isPseudoClass(AttributeResolver.ACTIVE_PSEUDOCLASS));
        assertFalse(sel.isPseudoClass(AttributeResolver.FOCUS_PSEUDOCLASS));
        sel = rules.createSelector(Selector.DESCENDANT_AXIS, "e");
        sel.setPseudoClass(AttributeResolver.ACTIVE_PSEUDOCLASS);
        assertFalse(sel.isPseudoClass(AttributeResolver.LINK_PSEUDOCLASS));
        assertFalse(sel.isPseudoClass(AttributeResolver.VISITED_PSEUDOCLASS));
        assertFalse(sel.isPseudoClass(AttributeResolver.HOVER_PSEUDOCLASS));
        assertTrue(sel.isPseudoClass(AttributeResolver.ACTIVE_PSEUDOCLASS));
        assertFalse(sel.isPseudoClass(AttributeResolver.FOCUS_PSEUDOCLASS));
        sel = rules.createSelector(Selector.DESCENDANT_AXIS, "e");
        sel.setPseudoClass(AttributeResolver.FOCUS_PSEUDOCLASS);
        assertFalse(sel.isPseudoClass(AttributeResolver.LINK_PSEUDOCLASS));
        assertFalse(sel.isPseudoClass(AttributeResolver.VISITED_PSEUDOCLASS));
        assertFalse(sel.isPseudoClass(AttributeResolver.HOVER_PSEUDOCLASS));
        assertFalse(sel.isPseudoClass(AttributeResolver.ACTIVE_PSEUDOCLASS));
        assertTrue(sel.isPseudoClass(AttributeResolver.FOCUS_PSEUDOCLASS));
   }
    
    public void testPseudoClassMatch() {
        System.out.println("testPseudoClassMatch");
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
        Element e = doc.createElement("e");
        Ruleset rules = new Ruleset();
        Selector sel = rules.createSelector(Selector.DESCENDANT_AXIS, "e");
        assertTrue(sel.matchesDynamic(e, no));
        assertTrue(sel.matchesDynamic(e, yes));
        sel.setPseudoClass(AttributeResolver.LINK_PSEUDOCLASS);
        assertFalse(sel.matchesDynamic(e, no));
        assertTrue(sel.matchesDynamic(e, yes));
        sel.setPseudoClass(AttributeResolver.VISITED_PSEUDOCLASS);
        assertFalse(sel.matchesDynamic(e, no));
        assertTrue(sel.matchesDynamic(e, yes));
        sel.setPseudoClass(AttributeResolver.HOVER_PSEUDOCLASS);
        assertFalse(sel.matchesDynamic(e, no));
        assertTrue(sel.matchesDynamic(e, yes));
        sel.setPseudoClass(AttributeResolver.ACTIVE_PSEUDOCLASS);
        assertFalse(sel.matchesDynamic(e, no));
        assertTrue(sel.matchesDynamic(e, yes));
        sel.setPseudoClass(AttributeResolver.FOCUS_PSEUDOCLASS);
        assertFalse(sel.matchesDynamic(e, no));
        assertTrue(sel.matchesDynamic(e, yes));
        //just check that the element matches non-dynamic
        assertTrue(sel.matches(e, no));
        assertTrue(sel.matches(e, yes));
    }
    
    public void testFirstChild() {
        System.out.println("testFirstChild");
        Ruleset rules = new Ruleset();
        Selector sel = rules.createSelector(Selector.DESCENDANT_AXIS, null);
        sel.addFirstChildCondition();
        Element e = doc.createElement("e");
        doc.appendChild(e);
        Element one = doc.createElement("one");
        e.appendChild(one);
        Element two = doc.createElement("two");
        e.appendChild(two);
        assertTrue("selector for :first-child should match root element",  sel.matches(e, null));
        assertTrue("selector for :first-child should match first element",  sel.matches(one, null));
        assertFalse("selector for :first-child should not match second element",  sel.matches(two, null));
    }
    
    private Document doc;
    
}
