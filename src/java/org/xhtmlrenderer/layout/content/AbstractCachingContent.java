/*
 * {{{ header & license
 * Copyright (c) 2005 Wisconsin Court System
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
package org.xhtmlrenderer.layout.content;

import org.xhtmlrenderer.layout.Context;
import org.xhtmlrenderer.util.XRRuntimeException;

import java.util.Iterator;
import java.util.List;

public abstract class AbstractCachingContent implements CachingContent {
    private List _childContent;

    protected abstract List makeChildContent(Context c);

    public Content getNextSibling(Context c, Content which) {
        for (Iterator i = getChildContent(c).iterator(); i.hasNext();) {
            Object obj = i.next();
            if (obj == which) {
                return (Content) (i.hasNext() ? i.next() : null);
            }
        }
        throw new XRRuntimeException("Could not find sibling to: " + which);
    }

    public Content getPreviousInFlowSibling(Context c, Content which) {
        List children = getChildContent(c);
        for (int i = 0; i < children.size(); i++) {
            Object obj = children.get(i);
            if (obj == which) {
                for (int j = i - 1; j >= 0; j--) {
                    Object result = children.get(j);
                    if (ContentUtil.isNotInFlow(result)) {
                        continue;
                    }
                    return (Content) result;
                }
            }
        }
        throw new XRRuntimeException("Could not find sibling to: " + which);
    }

    public Content getNextInFlowSibling(Context c, Content which) {
        for (Iterator i = getChildContent(c).iterator(); i.hasNext();) {
            Object obj = i.next();
            if (obj == which) {
                while (i.hasNext()) {
                    Object result = i.next();
                    if (ContentUtil.isNotInFlow(result)) {
                        continue;
                    }
                    return (Content) result;
                }
                return null;
            }
        }
        throw new XRRuntimeException("Could not find sibling to: " + which);
    }

    public List getChildContent(Context c) {
        if (_childContent == null) {
            _childContent = makeChildContent(c);
        }
        return _childContent;
    }
}
