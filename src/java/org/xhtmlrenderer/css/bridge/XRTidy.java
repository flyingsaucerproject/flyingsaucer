package org.xhtmlrenderer.css.bridge;

import java.net.*;
import java.io.*;
import org.w3c.tidy.*;
import org.w3c.dom.*;

/**
 * New class
 *
 * @author   Patrick Wright
 */
public class XRTidy {
    public Document getTidyDocument(URL url) throws IOException {
        Tidy tidy = new Tidy();
        Configuration config = tidy.getConfiguration();
        tidy.setConfigurationFromFile("c:/j3p/xhtmlrenderer/tidy.props");
        config.adjust();
        tidy.setErrout(tidy.getErrout());
        return tidy.parseDOM(url.openStream(), null);
    }
    
    /**
     * Description of the Method
     *
     * @param args  PARAM
     */
    public static void main( String args[] ) {
        try {

        } catch ( Exception ex ) {
            ex.printStackTrace();
        }
        System.out.println( "Done. Exiting." );
    }
}

