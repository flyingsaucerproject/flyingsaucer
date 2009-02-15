/*
 * {{{ header & license
 * Copyright (c) 2006 Patrick Wright
 * Copyright (c) 2007 Wisconsin Court System
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
package org.xhtmlrenderer.demo.svg;

import org.xhtmlrenderer.extend.ReplacedElementFactory;
import org.xhtmlrenderer.extend.ReplacedElement;
import org.xhtmlrenderer.extend.UserAgentCallback;
import org.xhtmlrenderer.layout.LayoutContext;
import org.xhtmlrenderer.render.BlockBox;

import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;
import org.xhtmlrenderer.simple.extend.FormSubmissionListener;

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

    public void remove(Element e) {
        for (Iterator i = this.factoryList.iterator(); i.hasNext(); ) {
            ReplacedElementFactory factory = (ReplacedElementFactory)i.next();
            factory.remove(e);
        }       
    }

    public void setFormSubmissionListener(FormSubmissionListener listener) {
        // nothing to do ?
    }
}
