package org.joshy.html.css;

import org.w3c.dom.Element;
import com.steadystate.css.*;
import com.steadystate.css.parser.*;
import org.w3c.dom.css.*;
import org.w3c.css.sac.*;

public interface RuleBank {
    public void addStyleRule(JStyle rule);
    public CSSStyleDeclaration findRule(Element elem, String property, boolean inherit);

    /**
   * Notes that an Element was already parsed for inline styles; after this call, hasStyleForElement(elem) will return true.
   *
   * @param elem   The DOM Element
   */
   void elementWasParsed( Element elem );

  /**
   * Returns true if this Element was already parsed for internal styles. Elements are tracked by the elementWasParsed() method.
   *
   * @param elem  The Element to check on.
   * @return      Returns
   */
  boolean wasElementParsed( Element elem );    
}
