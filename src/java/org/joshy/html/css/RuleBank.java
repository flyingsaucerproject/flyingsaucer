package org.joshy.html.css;

import org.w3c.dom.Node;
import com.steadystate.css.*;
import com.steadystate.css.parser.*;
import org.w3c.dom.css.*;
import org.w3c.css.sac.*;

public interface RuleBank {
    public void addRule(JStyle rule);
    public CSSStyleDeclaration findRule(Node elem, String property, boolean inherit);
}
