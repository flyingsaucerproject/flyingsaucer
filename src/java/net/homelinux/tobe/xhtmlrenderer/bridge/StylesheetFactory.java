/*
 * StylesheetFactory.java
 *
 * Created on den 7 september 2004, 21:52
 */

package net.homelinux.tobe.xhtmlrenderer.bridge;

import com.steadystate.css.parser.CSSOMParser;

import org.w3c.css.sac.InputSource;
import org.w3c.dom.css.CSSStyleSheet;
import org.w3c.dom.css.CSSStyleRule;

/**
 *
 * @author  Torbjörn Gannholm
 */
public class StylesheetFactory implements net.homelinux.tobe.xhtmlrenderer.StylesheetFactory {
    
                CSSOMParser parser = new CSSOMParser();

    /** Creates a new instance of StylesheetFactory */
    public StylesheetFactory() {
    }
    
    public net.homelinux.tobe.xhtmlrenderer.Stylesheet getStylesheet(Object key) {
        return null;
    }
    
    public net.homelinux.tobe.xhtmlrenderer.Stylesheet parse(int origin, java.io.Reader reader) {
        return null;
    }
    
    public net.homelinux.tobe.xhtmlrenderer.Ruleset parseStyleDeclaration(int origin, String styleDeclaration) {
            try {
                java.io.StringReader reader = new java.io.StringReader("* {"+styleDeclaration+"}");
                InputSource is = new InputSource( reader );
                CSSStyleSheet style = parser.parseStyleSheet( is );
                reader.close();
                return new Ruleset((CSSStyleRule) style.getCssRules().item(0), net.homelinux.tobe.xhtmlrenderer.Stylesheet.AUTHOR);
            } catch ( Exception ex ) {
                ex.printStackTrace();
            }
            return null;
    }
    
    public void putStylesheet(Object key, net.homelinux.tobe.xhtmlrenderer.Stylesheet sheet) {
    }
    
}
