/*
 * RulesetTest.java
 * JUnit based test
 *
 * Created on den 25 juli 2004, 12:18
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
        Ruleset.Selector sel = rules.createSelector(Ruleset.Selector.DESCENDANT_AXIS, null);
        Element e = doc.createElement("e");
        assertTrue("selector for any should match 'e'",  sel.matches(e, null));
        Element f = doc.createElement("f");
        assertTrue("selector for any should match 'f'",  sel.matches(f, null));
    }
    
    /** test that the selector for a named element works */
    public void testNamed() {
        System.out.println("testNamed");
        Ruleset rules = new Ruleset();
        Ruleset.Selector sel = rules.createSelector(Ruleset.Selector.DESCENDANT_AXIS, "e");
        Element e = doc.createElement("e");
        assertTrue("selector for 'e' should match "+e.getNodeName(),  sel.matches(e, null));
        Element f = doc.createElement("f");
        assertFalse("selector for 'e' should not match 'f'",  sel.matches(f, null));
    }
    
    /** test that an id condition works */
    public void testId() {
        System.out.println("testId");
        Ruleset rules = new Ruleset();
        Ruleset.Selector sel = rules.createSelector(Ruleset.Selector.DESCENDANT_AXIS, "e");
        sel.setIDCondition("eid");
        AttributeResolver eid = new AttributeResolver() { public String getID(org.w3c.dom.Element e){ return "eid";}
            public String getClass(org.w3c.dom.Element e){ return null;}
            public String getAttributeValue(org.w3c.dom.Element e, String a){ return null;}};
        AttributeResolver fid = new AttributeResolver() { public String getID(org.w3c.dom.Element e){ return "fid";}
            public String getClass(org.w3c.dom.Element e){ return null;}
            public String getAttributeValue(org.w3c.dom.Element e, String a){ return null;}};
        AttributeResolver noid = new AttributeResolver() { public String getID(org.w3c.dom.Element e){ return null;}
            public String getClass(org.w3c.dom.Element e){ return null;}
            public String getAttributeValue(org.w3c.dom.Element e, String a){ return null;}};
        Element e = doc.createElement("e");
        assertTrue("selector for 'e#eid' should match e#eid",  sel.matches(e, eid));
        assertFalse("selector for 'e#eid' should not match e#fid",  sel.matches(e, fid));
        Element f = doc.createElement("f");
        assertFalse("selector for 'e#eid' should not match f#eid",  sel.matches(f, eid));
        Ruleset.Selector selAny = rules.createSelector(Ruleset.Selector.DESCENDANT_AXIS, null);
        selAny.setIDCondition("eid");
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
        Ruleset.Selector sel = rules.createSelector(Ruleset.Selector.DESCENDANT_AXIS, "e");
        sel.addClassCondition("a");
        AttributeResolver a = new AttributeResolver() { public String getID(org.w3c.dom.Element e){ return null;}
            public String getClass(org.w3c.dom.Element e){ return "a";}
            public String getAttributeValue(org.w3c.dom.Element e, String a){ return null;}};
        AttributeResolver b = new AttributeResolver() { public String getID(org.w3c.dom.Element e){ return null;}
            public String getClass(org.w3c.dom.Element e){ return "b";}
            public String getAttributeValue(org.w3c.dom.Element e, String a){ return null;}};
        Element e = doc.createElement("e");
        assertTrue("selector for 'e.a' should match class=a",  sel.matches(e, a));
        assertFalse("selector for 'e.a' should not match class=b",  sel.matches(e, b));
        AttributeResolver abc = new AttributeResolver() { public String getID(org.w3c.dom.Element e){ return null;}
            public String getClass(org.w3c.dom.Element e){ return "a b c";}
            public String getAttributeValue(org.w3c.dom.Element e, String a){ return null;}};
        assertTrue("selector for 'e.a' should match class='a b c'",  sel.matches(e, abc));
        sel.addClassCondition("b");
        assertTrue("selector for 'e.a.b' should match class='a b c'",  sel.matches(e, abc));
        sel.addClassCondition("c");
        assertTrue("selector for 'e.a.b.c' should match class='a b c'",  sel.matches(e, abc));
        sel.addClassCondition("d");
        assertFalse("selector for 'e.a.b.c.d' should not match class='a b c'",  sel.matches(e, abc));
        sel = rules.createSelector(Ruleset.Selector.DESCENDANT_AXIS, "e");
        sel.addClassCondition("b");
        assertTrue("selector for 'e.b' should match class='a b c'",  sel.matches(e, abc));
        sel = rules.createSelector(Ruleset.Selector.DESCENDANT_AXIS, "e");
        sel.addClassCondition("c");
        assertTrue("selector for 'e.c' should match class='a b c'",  sel.matches(e, abc));
        sel = rules.createSelector(Ruleset.Selector.DESCENDANT_AXIS, "e");
        sel.addClassCondition("d");
        assertFalse("selector for 'e.d' should not match class='a b c'",  sel.matches(e, abc));
        AttributeResolver none = new AttributeResolver() { public String getID(org.w3c.dom.Element e){ return null;}
            public String getClass(org.w3c.dom.Element e){ return null;}
            public String getAttributeValue(org.w3c.dom.Element e, String a){ return null;}};
        assertFalse("selector for 'e.d' should not match null class",  sel.matches(e, none));
        assertFalse("selector for 'e.d' should not match no Resolver",  sel.matches(e, null));
   }
    
    private Document doc;
    
}
