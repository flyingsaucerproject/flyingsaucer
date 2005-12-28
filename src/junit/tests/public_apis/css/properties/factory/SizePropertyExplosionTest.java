/*
 * {{{ header & license
 * Copyright (c) 2005 Wisconsin Court System
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
package tests.public_apis.css.properties.factory;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.xhtmlrenderer.css.constants.CSSName;
import org.xhtmlrenderer.css.sheet.factory.PropertyDeclarationFactory;
import org.xhtmlrenderer.css.sheet.factory.SizePropertyDeclarationFactory;

public class SizePropertyExplosionTest extends AbstractPropertyExplosionTest {
    public SizePropertyExplosionTest( String name ) {
        super( name );
    }

    protected PropertyDeclarationFactory newPropertyDeclarationFactory() {
        return SizePropertyDeclarationFactory.instance();
    }

    protected Map buildTestsMap() {
        Map temp = new TreeMap();

        Map testVals = null;

        testVals = new HashMap();
        testVals.put(CSSName.FS_PAGE_WIDTH, "8.5in");
        testVals.put(CSSName.FS_PAGE_HEIGHT, "11in");
        testVals.put(CSSName.FS_PAGE_ORIENTATION, "auto");
        temp.put( "p#one", new Object[]{"{ size: letter; }", testVals});
        
        testVals = new HashMap();
        testVals.put(CSSName.FS_PAGE_WIDTH, "8.5in");
        testVals.put(CSSName.FS_PAGE_HEIGHT, "11in");
        testVals.put(CSSName.FS_PAGE_ORIENTATION, "auto");
        temp.put( "p#two", new Object[]{"{ size: 8.5in 11in; }", testVals});

        testVals = new HashMap();
        testVals.put(CSSName.FS_PAGE_WIDTH, "8.5in");
        testVals.put(CSSName.FS_PAGE_HEIGHT, "11in");
        testVals.put(CSSName.FS_PAGE_ORIENTATION, "landscape");
        temp.put( "p#three", new Object[]{"{ size: letter landscape; }", testVals});
        
        testVals = new HashMap();
        testVals.put(CSSName.FS_PAGE_WIDTH, "auto");
        testVals.put(CSSName.FS_PAGE_HEIGHT, "auto");
        testVals.put(CSSName.FS_PAGE_ORIENTATION, "landscape");
        temp.put( "p#four", new Object[]{"{ size: landscape; }", testVals});
        
        testVals = new HashMap();
        testVals.put(CSSName.FS_PAGE_WIDTH, "8.5in");
        testVals.put(CSSName.FS_PAGE_HEIGHT, "8.5in");
        testVals.put(CSSName.FS_PAGE_ORIENTATION, "auto");
        temp.put( "p#five", new Object[]{"{ size: 8.5in; }", testVals});
        
        testVals = new HashMap();
        testVals.put(CSSName.FS_PAGE_WIDTH, "auto");
        testVals.put(CSSName.FS_PAGE_HEIGHT, "auto");
        testVals.put(CSSName.FS_PAGE_ORIENTATION, "auto");
        temp.put( "p#six", new Object[]{"{ size: auto; }", testVals});
        
        return temp;
    }
    
    public static Test suite() {
        return new TestSuite( SizePropertyExplosionTest.class );
    }
}
