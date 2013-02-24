/*
 * {{{ header & license
 * Copyright (c) 2007 Vianney le Clément
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
package org.xhtmlrenderer.demo.browser.swt.actions;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MenuItem;
import org.xhtmlrenderer.demo.browser.swt.Browser;

public class DebugMetricsAction extends AbstractAction {

    public DebugMetricsAction() {
        super("Show Font Metrics", SWT.CHECK, SWT.NONE, null);
    }

    public void run(Browser browser, MenuItem mi) {
        if (mi != null) {
            browser.getRenderer().getSharedContext()
                .setDebug_draw_font_metrics(mi.getSelection());
            browser.getRenderer().invalidate();
        }
    }

}
