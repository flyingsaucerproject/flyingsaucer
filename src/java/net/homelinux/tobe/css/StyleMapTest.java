/*
 * ElementStyleMapTest.java
 * JUnit based test
 *
 * Created on den 25 juli 2004, 13:10
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
    }
   
    public void testDescendant() {
        System.out.println("testDescendant");
        
        java.util.List l = new java.util.LinkedList();
        
        Ruleset r = new Ruleset();
        Ruleset.Selector s = r.createSelector(Ruleset.Selector.DESCENDANT_AXIS, "root");
        r.addPropertyDeclaration("rootProperty");
        l.add(r);
        
        r = new Ruleset();
        s = r.createSelector(Ruleset.Selector.DESCENDANT_AXIS, "first");
        r.addPropertyDeclaration("firstProperty");
        l.add(r);
        
        r = new Ruleset();
        s = r.createSelector(Ruleset.Selector.DESCENDANT_AXIS, "first");
        s.appendChainedSelector(Ruleset.Selector.DESCENDANT_AXIS, "first");
        r.addPropertyDeclaration("firstfirstProperty");
        l.add(r);
        
        StyleMap myStyles = StyleMap.createMap(doc, l);
        
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
    }
    
    // TODO add test methods here, they have to start with 'test' name.
    // for example:
    // public void testHello() {}
    
    private Document doc;
    
}
