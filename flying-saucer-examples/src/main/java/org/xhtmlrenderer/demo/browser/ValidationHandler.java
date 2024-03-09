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

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXParseException;

import javax.swing.*;


public class ValidationHandler implements ErrorHandler {
    protected JTextArea jta;

    @Override
    public void error(SAXParseException ex ) {
        print( "error: " + print( ex ) );
    }

    @Override
    public void fatalError(SAXParseException ex ) {
        print( "fatal error: " + print( ex ) );
    }

    @Override
    public void warning(SAXParseException ex ) {
        print( "warning: " + print( ex ) );
    }

    public String print(SAXParseException ex) {
        return String.format("Exception: %sfailed at column : %d on line %dentity:%n%s%n%s",
                ex.getMessage(), ex.getColumnNumber(), ex.getLineNumber(), ex.getPublicId(), ex.getSystemId());
    }

    /**
     * Sets the textArea attribute of the ValidationHandler object
     *
     * @param jta  The new textArea value
     */
    public void setTextArea( JTextArea jta ) {
        this.jta = jta;
    }

    protected void print( String str ) {
        if ( jta != null ) {
            jta.append( str );
        }
    }

}
