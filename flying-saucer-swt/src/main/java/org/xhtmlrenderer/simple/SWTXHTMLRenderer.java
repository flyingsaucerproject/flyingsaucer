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
package org.xhtmlrenderer.simple;

import java.io.File;
import java.io.InputStream;
import java.net.MalformedURLException;

import org.eclipse.swt.widgets.Composite;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xhtmlrenderer.extend.ReplacedElementFactory;
import org.xhtmlrenderer.extend.UserAgentCallback;
import org.xhtmlrenderer.simple.xhtml.XhtmlForm;
import org.xhtmlrenderer.simple.xhtml.XhtmlNamespaceHandler;
import org.xhtmlrenderer.simple.xhtml.swt.SWTXhtmlReplacedElementFactory;
import org.xhtmlrenderer.swt.BasicRenderer;
import org.xhtmlrenderer.swt.CursorListener;
import org.xhtmlrenderer.swt.HoverListener;
import org.xhtmlrenderer.swt.LinkListener;
import org.xhtmlrenderer.util.Configuration;

/**
 * Simplified {@link BasicRenderer}, for use with XHTML documents.
 * 
 * @author Vianney le Clément
 * 
 */
public class SWTXHTMLRenderer extends BasicRenderer {

    public SWTXHTMLRenderer(Composite parent, int style) {
        super(parent, style);
        init();
    }

    public SWTXHTMLRenderer(Composite parent, int style, UserAgentCallback uac) {
        super(parent, style, uac);
        init();
    }

    protected void init() {
        getSharedContext().setReplacedElementFactory(
            new SWTXhtmlReplacedElementFactory(this));
        if (Configuration.isTrue("xr.use.listeners", true)) {
            new HoverListener(this);
            new CursorListener(this);
            new LinkListener(this);
        }
    }

    /**
     * Loads and renders a Document given a uri. The uri is resolved by the
     * UserAgentCallback
     * 
     * @param uri
     */
    public void setDocument(String uri) {
        setDocument(loadDocument(uri), uri);
    }

    /**
     * Renders an XML Document instance. Make sure that no relative resources
     * are needed
     * 
     * @param doc The document to render.
     */
    public void setDocument(Document doc) {
        setDocument(doc, "");
    }

    /**
     * Renders a Document using a URL as a base URL for relative paths.
     * 
     * @param doc The new document value
     * @param url The new document value
     */
    public void setDocument(Document doc, String url) {
        super.setDocument(doc, url, new XhtmlNamespaceHandler());
    }

    /**
     * Renders a Document read from an InputStream using a URL as a base URL for
     * relative paths.
     * 
     * @param stream The stream to read the Document from.
     * @param url The URL used to resolve relative path references.
     */
    public void setDocument(InputStream stream, String url) {
        super.setDocument(stream, url, new XhtmlNamespaceHandler());
    }

    /**
     * Renders a Document read from an InputStream using a URL as a base URL for
     * relative paths.
     * 
     * @param file The file to read the Document from. Relative paths will be
     *            resolved based on the file's parent directory.
     * @throws MalformedURLException
     */
    public void setDocument(File file) throws MalformedURLException {
        File parent = file.getParentFile();
        String parentURL = (parent == null ? "" : parent.toURI().toURL()
            .toExternalForm());
        setDocument(loadDocument(file.toURI().toURL().toExternalForm()),
            parentURL);
    }

    /**
     * @param e
     * @return the form corresponding to element <code>e</code> or
     *         <code>null</code> if none
     */
    public XhtmlForm getForm(Element e) {
        ReplacedElementFactory ref = getSharedContext()
            .getReplacedElementFactory();
        if (ref != null && ref instanceof SWTXhtmlReplacedElementFactory) {
            return ((SWTXhtmlReplacedElementFactory) ref).getForm(e);
        }
        return null;
    }

}
