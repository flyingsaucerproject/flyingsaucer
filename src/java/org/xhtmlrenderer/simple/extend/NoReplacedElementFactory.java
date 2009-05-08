package org.xhtmlrenderer.simple.extend;

import org.w3c.dom.Element;
import org.xhtmlrenderer.extend.ReplacedElement;
import org.xhtmlrenderer.extend.ReplacedElementFactory;
import org.xhtmlrenderer.extend.UserAgentCallback;
import org.xhtmlrenderer.layout.LayoutContext;
import org.xhtmlrenderer.render.BlockBox;

public class NoReplacedElementFactory implements ReplacedElementFactory {

    public ReplacedElement createReplacedElement(LayoutContext c, BlockBox box,
            UserAgentCallback uac, int cssWidth, int cssHeight) {
        return null;
    }

    public void remove(Element e) {

    }

    public void setFormSubmissionListener(FormSubmissionListener listener) {
        //TODO
    }

    public void reset() {
    }

}
