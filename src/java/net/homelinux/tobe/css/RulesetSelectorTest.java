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
        IDResolver eid = new IDResolver() { public String getID(org.w3c.dom.Element e){ return "eid";}};
        IDResolver fid = new IDResolver() { public String getID(org.w3c.dom.Element e){ return "fid";}};
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
        assertFalse("selector for '*#eid' should not match with no IDResolver",  selAny.matches(e, null));
    }
    
    private Document doc;
    
}
