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

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.*;
import org.xhtmlrenderer.demo.browser.swt.DemosNavigation.Demo;
import org.xhtmlrenderer.demo.browser.swt.actions.*;
import org.xhtmlrenderer.event.DocumentListener;
import org.xhtmlrenderer.simple.SWTXHTMLRenderer;

public class Browser implements DisposeListener, DocumentListener {

    private final Shell _shell;
    private final SWTXHTMLRenderer _xhtml;
    private final BrowserStatus _status;
    private Text _url;

    private Map _imageCache = new HashMap();

    private Action _backAction, _forwardAction, _reloadAction, _homeAction;
    private MenuItem _miBack, _miForward;
    private ToolItem _tiBack, _tiForward;

    private static final String USER_GUIDE_URL = "demo:/r7/users-guide-r7.html";

    public Browser(Display display) {
        _shell = new Shell(display);
        _shell.setText("Flying Saucer");

        // create widgets
        _xhtml = new SWTXHTMLRenderer(_shell, SWT.BORDER, new BrowserUserAgent(
            display));

        final Menu menu = new Menu(_shell, SWT.BAR);
        _shell.setMenuBar(menu);

        final CoolBar coolbar = new CoolBar(_shell, SWT.NONE);
        final ToolBar toolbar = new ToolBar(coolbar, SWT.FLAT);
        final CoolItem ciToolbar = new CoolItem(coolbar, SWT.NONE);
        ciToolbar.setControl(toolbar);
        final Composite address = createAddressBar(coolbar);
        final CoolItem ciAddress = new CoolItem(coolbar, SWT.NONE);
        ciAddress.setControl(address);

        _status = new BrowserStatus(_shell);

        // layout
        _shell.setLayout(new FormLayout());
        FormData fd;
        fd = new FormData();
        fd.top = new FormAttachment(0);
        fd.left = new FormAttachment(0);
        fd.right = new FormAttachment(100);
        coolbar.setLayoutData(fd);
        fd = new FormData();
        fd.top = new FormAttachment(coolbar);
        fd.left = new FormAttachment(0);
        fd.right = new FormAttachment(100);
        fd.bottom = new FormAttachment(_status);
        _xhtml.setLayoutData(fd);
        fd = new FormData();
        fd.left = new FormAttachment(0);
        fd.right = new FormAttachment(100);
        fd.bottom = new FormAttachment(100);
        _status.setLayoutData(fd);

        // create common actions
        _backAction = new BackAction();
        _forwardAction = new ForwardAction();
        _reloadAction = new ReloadAction();
        _homeAction = new LoadAction("demo:/demos/splash/splash.html", "Home",
            "demo:/images/go-home.png");

        // populate menu and toolbar
        populateMenu(menu);
        populateToolBar(toolbar);

        // set coolbar dimensions
        toolbar.pack();
        Point size = toolbar.getSize();
        size = ciToolbar.computeSize(size.x, size.y);
        ciToolbar.setPreferredSize(size);
        ciToolbar.setMinimumSize(size);
        ciToolbar.setSize(size);

        address.pack();
        size = address.getSize();
        size = ciAddress.computeSize(size.x, size.y);
        ciAddress.setMinimumSize(size);
        ciAddress.setPreferredSize(400, size.y);
        ciAddress.setSize(400, size.y);

        // register listeners
        _shell.addDisposeListener(this);
        _xhtml.addDocumentListener(this);
        coolbar.addListener(SWT.Resize, new Listener() {
            public void handleEvent(Event event) {
                _shell.layout();
            }
        });

        // execute default home action
        _homeAction.run(this, null);
    }

    private Composite createAddressBar(CoolBar coolbar) {
        // widgets
        final Composite comp = new Composite(coolbar, SWT.NONE);
        _url = new Text(comp, SWT.BORDER);
        _url.pack();
        final Button go = new Button(comp, SWT.PUSH | SWT.FLAT);
        go.setImage(loadImage("demo:/images/media-playback-start_16x16.png"));
        go.setText("Go");
        go.pack();
        // layout
        comp.setLayout(new FormLayout());
        FormData fd;
        fd = new FormData();
        fd.left = new FormAttachment(0);
        fd.right = new FormAttachment(go);
        fd.top = new FormAttachment(50, -_url.getSize().y / 2);
        _url.setLayoutData(fd);
        fd = new FormData();
        fd.right = new FormAttachment(100);
        fd.top = new FormAttachment(50, -go.getSize().y / 2);
        go.setLayoutData(fd);
        // events
        _url.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                if (e.character == '\r') {
                    load(_url.getText());
                    e.doit = false;
                }
            }
        });
        go.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                load(_url.getText());
            }
        });
        return comp;
    }

    private void populateMenu(Menu menu) {
        Menu browser = createMenu(menu, "&Browser");
        addActionToMenu(browser, new OpenAction());
        addSeparatorToMenu(browser);
        addActionToMenu(browser, new QuitAction());

        Menu view = createMenu(menu, "&View");
        addActionToMenu(view, _reloadAction);
        addSeparatorToMenu(view);
        Menu view_size = createMenu(view, "Text size");
        addActionToMenu(view_size, new FontSizeIncreaseAction());
        addActionToMenu(view_size, new FontSizeDecreaseAction());
        addSeparatorToMenu(view_size);
        addActionToMenu(view_size, new FontSizeNormalAction());
        addSeparatorToMenu(view);
        addActionToMenu(view, new PrintPreviewAction());

        Menu go = createMenu(menu, "&Go");
        _miBack = addActionToMenu(go, _backAction);
        _miForward = addActionToMenu(go, _forwardAction);
        addSeparatorToMenu(go);
        addActionToMenu(go, _homeAction);

        Menu demos = createMenu(menu, "&Demos");
        addActionToMenu(demos, new LoadAction("demoNav:forward",
            "&Next Demo Page\tCtrl+N", SWT.CTRL | 'N'));
        addActionToMenu(demos, new LoadAction("demoNav:back",
            "&Prior Demo Page\tCtrl+P", SWT.CTRL | 'P'));
        addSeparatorToMenu(demos);
        Iterator iter = getUac().getDemos().iterate();
        while (iter.hasNext()) {
            addActionToMenu(demos, new DemoAction((Demo) iter.next()));
        }

        Menu debug = createMenu(menu, "Deb&ug");
        Menu debug_show = createMenu(debug, "Show");
        addActionToMenu(debug_show, new DebugBoxesAction());
        addActionToMenu(debug_show, new DebugLineBoxesAction());
        addActionToMenu(debug_show, new DebugInlineBoxesAction());
        addActionToMenu(debug_show, new DebugMetricsAction());

        Menu help = createMenu(menu, "&Help");
        addActionToMenu(help, new LoadAction(USER_GUIDE_URL, "FS User's Guide"));
        addSeparatorToMenu(help);
        addActionToMenu(help, new AboutAction());
    }

    private void populateToolBar(ToolBar toolbar) {
        _tiBack = addActionToToolbar(toolbar, _backAction);
        _tiForward = addActionToToolbar(toolbar, _forwardAction);
        addActionToToolbar(toolbar, _reloadAction);
        addActionToToolbar(toolbar, _homeAction);
    }

    private Image loadImage(String icon) {
        if (icon == null) {
            return null;
        }
        Image img = (Image) _imageCache.get(icon);
        if (img != null) {
            return img;
        }
        String uri = getUac().resolveFullURI(icon);
        URL url;
        try {
            url = new URL(uri);
        } catch (MalformedURLException e) {
            return null;
        }
        InputStream is;
        try {
            is = url.openStream();
        } catch (IOException e) {
            return null;
        }
        if (is == null) {
            return null;
        }
        img = new Image(_shell.getDisplay(), is);
        _imageCache.put(icon, img);
        return img;
    }

    private Menu createMenu(Menu parent, String text) {
        MenuItem mi = new MenuItem(parent, SWT.CASCADE);
        mi.setText(text);
        Menu m = new Menu(_shell, SWT.DROP_DOWN);
        mi.setMenu(m);
        return m;
    }

    private MenuItem addActionToMenu(Menu menu, final Action action) {
        final MenuItem mi = new MenuItem(menu, action.getStyle());
        mi.setText(action.getText());
        mi.setAccelerator(action.getShortcut());
        mi.setImage(loadImage(action.getIcon()));
        mi.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                action.run(Browser.this, mi);
            }
        });
        return mi;
    }

    private MenuItem addSeparatorToMenu(Menu menu) {
        return new MenuItem(menu, SWT.SEPARATOR);
    }

    private ToolItem addActionToToolbar(ToolBar toolbar, final Action action) {
        final ToolItem ti = new ToolItem(toolbar, action.getStyle());
        Image img = loadImage(action.getIcon());
        if (img == null) {
            ti.setText(action.getText());
        } else {
            ti.setImage(img);
            ti.setToolTipText(action.getText());
        }
        ti.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                action.run(Browser.this, null);
            }
        });
        return ti;
    }

    public void openAndDispatch() {
        Display display = _shell.getDisplay();
        _shell.open();
        while (!_shell.isDisposed()) {
            try {
                if (!display.readAndDispatch()) {
                    display.sleep();
                }
            } catch (Exception e) {
                e.printStackTrace();
                MessageBox box = new MessageBox(_shell, SWT.ICON_ERROR | SWT.OK);
                box.setText("Error");
                box.setMessage("An error has occured."
                        + " See console for details.\n\n"
                        + "Note: the application might be"
                        + " in an inconsistent state.");
                box.open();
            }
        }
    }

    public Shell getShell() {
        return _shell;
    }

    public SWTXHTMLRenderer getRenderer() {
        return _xhtml;
    }

    public BrowserUserAgent getUac() {
        return (BrowserUserAgent) _xhtml.getSharedContext().getUac();
    }

    public void setStatus(String message) {
        _status.setStatus(message);
    }

    public void load(String url) {
        _xhtml.setDocument(url);
    }

    public void back() {
        load(getUac().getHistory().back());
    }

    public void forward() {
        load(getUac().getHistory().forward());
    }

    public void widgetDisposed(DisposeEvent e) {
        // clean image cache
        for (Iterator iter = _imageCache.values().iterator(); iter.hasNext();) {
            ((Image) iter.next()).dispose();
        }
    }

    public void documentLoaded() {
        BrowserUserAgent uac = getUac();
        History history = uac.getHistory();
        String url = uac.getBaseURL();

        // unlock demos navigation so demoNav: will work again
        uac.getDemos().unlock();

        // update history
        String current = history.getCurrent();
        if (current == null || !current.equals(url)) {
            history.add(url);
        }

        // update back and forward buttons
        _miBack.setEnabled(history.hasBack());
        _tiBack.setEnabled(history.hasBack());
        _miForward.setEnabled(history.hasForward());
        _tiForward.setEnabled(history.hasForward());

        // update title bar
        String title = _xhtml.getDocumentTitle();
        if (title == null) {
            _shell.setText("Flying Saucer");
        } else {
            _shell.setText(title + " - Flying Saucer");
        }

        // update address bar
        if (url != null) {
            System.out.println("Document Loaded: " + url);
            _url.setText(url);

            // update status line
            setStatus("Successfully loaded '" + url + "'");
        }
        _status.refreshMemory();
    }

    public void documentStarted() {
    }

    public void onLayoutException(Throwable t) {
    }

    public void onRenderException(Throwable t) {
    }

}
