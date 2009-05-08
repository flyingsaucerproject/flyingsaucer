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
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.xhtmlrenderer.demo.browser.swt.Browser;
import org.xhtmlrenderer.demo.browser.swt.BrowserUserAgent;
import org.xhtmlrenderer.simple.SWTXHTMLRenderer;

public class AboutAction extends AbstractAction {

    private static final String ABOUT_URL = "demo:/demos/r7/about.xhtml";

    public AboutAction() {
        super("About", SWT.PUSH, SWT.NONE, null);
    }

    public void run(Browser browser, MenuItem mi) {
        // create about dialog
        final Display display = browser.getShell().getDisplay();
        final Shell shell = new Shell(browser.getShell(), SWT.DIALOG_TRIM
                | SWT.APPLICATION_MODAL);
        // create widgets
        final SWTXHTMLRenderer xhtml = new SWTXHTMLRenderer(shell, SWT.NONE,
            new BrowserUserAgent(display));
        final Button close = new Button(shell, SWT.PUSH);
        close.setText("Close");
        shell.setDefaultButton(close);
        close.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                shell.dispose();
            }
        });
        // layout
        shell.setLayout(new FormLayout());
        FormData fd;
        fd = new FormData();
        fd.top = new FormAttachment(0);
        fd.left = new FormAttachment(0);
        fd.right = new FormAttachment(100);
        fd.bottom = new FormAttachment(close, -5);
        xhtml.setLayoutData(fd);
        fd = new FormData();
        fd.right = new FormAttachment(100);
        fd.bottom = new FormAttachment(100);
        close.setLayoutData(fd);
        shell.setSize(500, 450);
        // load page
        xhtml.setDocument(ABOUT_URL);
        // open dialog
        shell.open();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch())
                display.sleep();
        }
    }

}
