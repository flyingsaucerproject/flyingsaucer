package org.joshy.html.css;

import org.w3c.dom.Element;
import com.steadystate.css.*;
import com.steadystate.css.parser.*;
import org.w3c.dom.css.*;
import org.w3c.css.sac.*;

public interface RuleBank {
    public void addRule(JStyle rule);
    public CSSStyleDeclaration findRule(Element elem, String property, boolean inherit);
}
