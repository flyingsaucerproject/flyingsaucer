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
        assertTrue("selector for any should match 'e'",  sel.matches(e));
        Element f = doc.createElement("f");
        assertTrue("selector for any should match 'f'",  sel.matches(f));
    }
    
    /** test that the selector for a named element works */
    public void testNamed() {
        System.out.println("testNamed");
        Ruleset rules = new Ruleset();
        Ruleset.Selector sel = rules.createSelector(Ruleset.Selector.DESCENDANT_AXIS, "e");
        Element e = doc.createElement("e");
        assertTrue("selector for 'e' should match "+e.getNodeName(),  sel.matches(e));
        Element f = doc.createElement("f");
        assertFalse("selector for 'e' should not match 'f'",  sel.matches(f));
    }
    
    private Document doc;
    
}
