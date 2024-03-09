/*
 * {{{ header & license
 * Copyright (c) 2008 Patrick Wright
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
package org.xhtmlrenderer.test;

import org.w3c.dom.Element;
import org.xhtmlrenderer.extend.ReplacedElement;
import org.xhtmlrenderer.extend.ReplacedElementFactory;
import org.xhtmlrenderer.extend.UserAgentCallback;
import org.xhtmlrenderer.layout.LayoutContext;
import org.xhtmlrenderer.render.BlockBox;
import org.xhtmlrenderer.simple.extend.FormSubmissionListener;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author patrick
 */
@ParametersAreNonnullByDefault
public class DelegatingReplacedElementFactory implements ReplacedElementFactory {
    private final List<ElementReplacer> replacers = new ArrayList<>();
    private final Map<String, ElementReplacer> byNameReplacers = new HashMap<>();
    private final List<ERItem> elementReplacements = new ArrayList<>();

    @Override
    public ReplacedElement createReplacedElement(final LayoutContext context,
                                                 final BlockBox box,
                                                 final UserAgentCallback uac,
                                                 final int cssWidth,
                                                 final int cssHeight
    ) {
        final ElementReplacer nameReplacer = byNameReplacers.get(box.getElement().getNodeName());
        if (nameReplacer != null) {
            return replaceUsing(context, box, uac, cssWidth, cssHeight, nameReplacer);
        }
        for (ElementReplacer replacer : replacers) {
            if (replacer.accept(context, box.getElement())) {
                return replaceUsing(context, box, uac, cssWidth, cssHeight, replacer);
            }
        }
        return null;
    }

    private ReplacedElement replaceUsing(LayoutContext context, BlockBox box, UserAgentCallback uac, int cssWidth, int cssHeight, ElementReplacer replacer) {
        final ReplacedElement re = replacer.replace(context, box, uac, cssWidth, cssHeight);
        elementReplacements.add(new ERItem(box.getElement(), replacer));
        return re;
    }

    @Override
    public void reset() {
        System.out.println("\n\n***Factory reset()");
        elementReplacements.clear();
        for (ElementReplacer elementReplacer : replacers) {
            elementReplacer.reset();
        }
        for (ElementReplacer elementReplacer : byNameReplacers.values()) {
            elementReplacer.reset();
        }
    }

    @Override
    public void remove(final Element element) {
        // FIXME 'List<ERItem>' may not contain objects of type 'Element'
        final int idx = elementReplacements.indexOf(element);
        ERItem item = elementReplacements.get(idx);
        elementReplacements.remove(idx);
        item.elementReplacer.clear(element);
    }

    public void addReplacer(final ElementReplacer replacer) {
        if (replacer.isElementNameMatch()) {
            byNameReplacers.put(replacer.getElementNameMatch(), replacer);
        } else {
            replacers.add(replacer);
        }
    }

    public void removeReplacer(final ElementReplacer replacer) {
        replacers.remove(replacer);
    }

    @Override
    public void setFormSubmissionListener(FormSubmissionListener listener) {
        // maybe there is nothing to do...
    }

    private record ERItem(Element element, ElementReplacer elementReplacer) {
    }
}
