package org.joshy.html.css;

import org.joshy.*;
import java.io.*;

import com.steadystate.css.*;
import com.steadystate.css.parser.*;
import org.w3c.dom.*;
import org.w3c.dom.css.*;
import org.w3c.css.sac.*;

public class CSSTest {

    public static void main(String[] args) throws Exception {
        Reader r = new FileReader("test.css");
        //CSS2Parser parser = new CSS2Parser(r);
        CSSOMParser parser = new CSSOMParser();
        InputSource is = new InputSource(r);
        CSSStyleSheet stylesheet = parser.parseStyleSheet(is);
        //u.p("stylesheet = " + stylesheet);
        
        String css_text = "p { color: #f0f0f0; }";
        stylesheet.insertRule(css_text,stylesheet.getCssRules().getLength());
        stylesheet.insertRule("@import \"second.css\";",0);
        u.p("stylesheet = " + stylesheet);
        u.p("title = " + stylesheet.getTitle());
        u.p("type = " + stylesheet.getType());
        CSSRule rule = stylesheet.getCssRules().item(0);
        u.p("first rule = " + rule);
        CSSImportRule impru = (CSSImportRule)rule;
        u.p("sub sheet = " + impru.getStyleSheet());
        
        /*
        CSSRuleList list = stylesheet.getCssRules();
        for(int i=0; i<list.getLength(); i++) {
            CSSRule rule = list.item(i);
            u.p("rule = " + rule);
            if(rule.getType() == rule.STYLE_RULE) {
                CSSStyleRule style = (CSSStyleRule)rule;
                u.p("selector = " + style.getSelectorText());
                CSSStyleDeclaration decl = style.getStyle();
                u.p("decl = " + decl);
                for(int j=0; j<decl.getLength(); j++) {
                    u.p("item = " + decl.item(j));
                    u.p("value = " + decl.getPropertyCSSValue("color"));
                }
            }
        }
        */
    }

}
