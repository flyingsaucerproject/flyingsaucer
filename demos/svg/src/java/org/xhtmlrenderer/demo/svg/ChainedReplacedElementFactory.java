package org.xhtmlrenderer.demo.svg;

import org.xhtmlrenderer.extend.ReplacedElementFactory;
import org.xhtmlrenderer.extend.ReplacedElement;
import org.xhtmlrenderer.extend.UserAgentCallback;
import org.xhtmlrenderer.layout.LayoutContext;
import org.xhtmlrenderer.render.BlockBox;

import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;

/**
 * 
 */
public class ChainedReplacedElementFactory implements ReplacedElementFactory {
    private List factoryList;

    public ChainedReplacedElementFactory() {
        this.factoryList = new ArrayList();
    }

    public ReplacedElement createReplacedElement(LayoutContext c, BlockBox box, UserAgentCallback uac, int cssWidth, int cssHeight) {
        ReplacedElement re = null;
        for (Iterator it = factoryList.iterator(); it.hasNext();) {
            ReplacedElementFactory  ref =  (ReplacedElementFactory) it.next();
            re = ref.createReplacedElement(c, box, uac, cssWidth, cssHeight);
            if ( re != null) break;
        }
        return re;
    }

    public void addFactory(ReplacedElementFactory ref) {
        this.factoryList.add(ref);
    }

    public void reset() {
        for (Iterator i = this.factoryList.iterator(); i.hasNext(); ) {
            ReplacedElementFactory factory = (ReplacedElementFactory)i.next();
            factory.reset();
        }       
    }
}
