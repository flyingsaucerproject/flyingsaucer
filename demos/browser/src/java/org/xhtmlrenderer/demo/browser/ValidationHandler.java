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
package org.xhtmlrenderer.demo.browser;

import java.util.logging.*;
import javax.swing.JTextArea;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXParseException;


/**
 * Description of the Class
 *
 * @author   empty
 */
public class ValidationHandler implements ErrorHandler {
    /** Description of the Field */
    protected JTextArea jta;
    /** Description of the Field */
    public static Logger logger = Logger.getLogger( "app.browser" );

    /**
     * Description of the Method
     *
     * @param ex  PARAM
     */
    public void error( SAXParseException ex ) {
        print( "error: " + print( ex ) );
    }

    /**
     * Description of the Method
     *
     * @param ex  PARAM
     */
    public void fatalError( SAXParseException ex ) {
        print( "fatal error: " + print( ex ) );
    }

    /**
     * Description of the Method
     *
     * @param ex  PARAM
     */
    public void warning( SAXParseException ex ) {
        print( "warning: " + print( ex ) );
    }

    /**
     * Description of the Method
     *
     * @param ex  PARAM
     * @return    Returns
     */
    public String print( SAXParseException ex ) {
        StringBuffer sb = new StringBuffer();
        sb.append( "Exception: " + ex.getMessage() );
        sb.append( "failed at column : " + ex.getColumnNumber() +
                " on line " + ex.getLineNumber() );
        sb.append( "entity:\n" + ex.getPublicId() + "\n" + ex.getSystemId() );
        return sb.toString();
    }

    /**
     * Sets the textArea attribute of the ValidationHandler object
     *
     * @param jta  The new textArea value
     */
    public void setTextArea( JTextArea jta ) {
        this.jta = jta;
    }

    /**
     * Description of the Method
     *
     * @param str  PARAM
     */
    protected void print( String str ) {
        if ( jta != null ) {
            jta.append( str );
        }
    }

}

/*
 * $Id$
 *
 * $Log$
 * Revision 1.2  2004/10/23 14:38:58  pdoubleya
 * Re-formatted using JavaStyle tool.
 * Cleaned imports to resolve wildcards except for common packages (java.io, java.util, etc)
 * Added CVS log comments at bottom.
 *
 *
 */

