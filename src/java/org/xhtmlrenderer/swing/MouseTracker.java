/*
 * {{{ header & license
 * Copyright (c) 2007 Sean Bright
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 * }}}
 */
package org.xhtmlrenderer.swing;

import java.awt.event.MouseEvent;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.event.MouseInputAdapter;

import org.xhtmlrenderer.render.Box;

class MouseTracker extends MouseInputAdapter {
    private BasicPanel _panel;
    private Map _handlers;
    private Box _last;
    private boolean _enabled;

    public MouseTracker(BasicPanel panel) {
        _panel = panel;
        _handlers = new LinkedHashMap();
    }
    
    public void addListener(FSMouseListener l) {
        if (l == null) {
            return;
        }
        
        if (!_handlers.containsKey(l)) {
            _handlers.put(l, l);
        }
        
        if (!_enabled && _handlers.size() > 0) {
            _panel.addMouseListener(this);
            _panel.addMouseMotionListener(this);
            
            _enabled = true;
        }
    }
    
    public void removeListener(FSMouseListener l) {
        if (l == null) {
            return;
        }
        
        if (_handlers.containsKey(l)) {
            _handlers.remove(l);
        }
        
        if (_enabled && _handlers.size() == 0) {
            _panel.removeMouseListener(this);
            _panel.removeMouseMotionListener(this);
            
            _enabled = false;
        }
    }

    public void mouseEntered(MouseEvent e) {
        handleMouseMotion(_panel.find(e));
    }

    public void mouseExited(MouseEvent e) {
        handleMouseMotion(_panel.find(e));
    }

    public void mouseMoved(MouseEvent e) {
        handleMouseMotion(_panel.find(e));
    }

    public void mouseReleased(MouseEvent e) {
        handleMouseUp(_panel.find(e));
    }
    
    public void reset() {
        _last = null;
        Iterator iterator = _handlers.keySet().iterator();
        while (iterator.hasNext()) {
            ((FSMouseListener) iterator.next()).reset();
        }
    }

    private void handleMouseMotion(Box box) {
        if (box == null || box.equals(_last)) {
            return;
        }
        
        if (_last != null) {
            mouseOut(_last);
        }
        
        mouseOver(box);

        _last = box;
    }
    
    private void handleMouseUp(Box box) {
        if (box == null) {
            return;
        }
        
        mouseUp(box);
    }
    
    private void mouseOver(Box box) {
        Iterator iterator = _handlers.keySet().iterator();
        while (iterator.hasNext()) {
            ((FSMouseListener) iterator.next()).onMouseOver(_panel, box);
        }
    }

    private void mouseOut(Box box) {
        Iterator iterator = _handlers.keySet().iterator();
        while (iterator.hasNext()) {
            ((FSMouseListener) iterator.next()).onMouseOut(_panel, box);
        }
    }
    
    private void mouseUp(Box box) {
        Iterator iterator = _handlers.keySet().iterator();
        while (iterator.hasNext()) {
            ((FSMouseListener) iterator.next()).onMouseUp(_panel, box);
        }
    }
}
