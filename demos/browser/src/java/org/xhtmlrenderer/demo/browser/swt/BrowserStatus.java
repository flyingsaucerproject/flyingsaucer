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
package org.xhtmlrenderer.demo.browser.swt;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public class BrowserStatus extends Composite {

    private static final int REFRESH_INTERVAL = 5000;

    private Label _status, _memory;
    private MemoryThread _memthread;

    public BrowserStatus(Composite parent) {
        super(parent, SWT.NONE);
        // widgets
        _status = new Label(this, SWT.LEFT);
        _status.setText("Flying Saucer initialized");
        _memory = new Label(this, SWT.RIGHT);
        _memory.setText("?MB / ?MB");
        // layout
        setLayout(new FormLayout());
        FormData fd;
        fd = new FormData();
        fd.top = new FormAttachment(0);
        fd.left = new FormAttachment(0);
        fd.right = new FormAttachment(_memory, -5);
        fd.bottom = new FormAttachment(100);
        _status.setLayoutData(fd);
        fd = new FormData();
        fd.top = new FormAttachment(0);
        fd.right = new FormAttachment(100);
        fd.bottom = new FormAttachment(100);
        _memory.setLayoutData(fd);
        _memory.pack();
        // thread
        _memthread = new MemoryThread();
        _memthread.start();
        // listeners
        addDisposeListener(new DisposeListener() {
            public void widgetDisposed(DisposeEvent e) {
                _memthread.interrupt();
            }
        });
    }

    public void setStatus(final String text) {
        getDisplay().asyncExec(new Runnable() {
            public void run() {
                if (!_status.isDisposed()) {
                    _status.setText(text);
                }
            }
        });
    }

    public void refreshMemory() {
        _memthread.refresh();
    }

    private class MemoryThread extends Thread {
        public synchronized void run() {
            while (true) {
                try {
                    Runtime rt = Runtime.getRuntime();
                    long used = rt.totalMemory() - rt.freeMemory();
                    long total = rt.totalMemory();

                    used = used / (1024 * 1024);
                    total = total / (1024 * 1024);

                    final String text = used + "MB / " + total + "MB";
                    getDisplay().asyncExec(new Runnable() {
                        public void run() {
                            if (!_memory.isDisposed()) {
                                _memory.setText(text);
                                _memory.pack();
                                layout();
                            }
                        }
                    });
                    wait(REFRESH_INTERVAL);
                } catch (InterruptedException e) {
                    break;
                }
            }
        }

        /**
         * Force an immediate memory status refresh
         */
        public synchronized void refresh() {
            notifyAll();
        }
    }

}
