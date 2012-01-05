/*
 * Copyright (c) 2006 Patrick Wright
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the
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
 */


import com.lowagie.text.DocumentException;
import org.xhtmlrenderer.pdf.ITextOutputDevice;
import org.xhtmlrenderer.pdf.ITextRenderer;
import org.xhtmlrenderer.pdf.ITextUserAgent;
import org.xhtmlrenderer.resource.XMLResource;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import java.io.*;

public class PDFRender {
    public static void main(String[] args) throws IOException, DocumentException {
        if (args.length != 2) {
            System.err.println("Usage: ... [url] [pdf]");
            System.exit(1);
        }
        String url = args[0];
        if (url.indexOf("://") == -1) {
            // maybe it's a file
            File f = new File(url);
            if (f.exists()) {
                url = f.toURI().toURL().toString();
            }
        }
        createPDF(url, args[1]);
    }

    public static void createPDF(String url, String pdf)
            throws IOException, DocumentException {
        OutputStream os = null;
        try {
            os = new FileOutputStream(pdf);

            /* standard approach
            ITextRenderer renderer = new ITextRenderer();

            renderer.setDocument(url);
            renderer.layout();
            renderer.createPDF(os);
            */

            ITextRenderer renderer = new ITextRenderer();
            ResourceLoaderUserAgent callback = new ResourceLoaderUserAgent(renderer.getOutputDevice());
            callback.setSharedContext(renderer.getSharedContext());
            renderer.getSharedContext ().setUserAgentCallback(callback);

            Document doc = XMLResource.load(new InputSource(url)).getDocument();

            renderer.setDocument(doc, url);
            renderer.layout();
            renderer.createPDF(os);

            os.close();
            os = null;
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    // ignore
                }
            }
        }
    }

    private static class ResourceLoaderUserAgent extends ITextUserAgent
    {
        public ResourceLoaderUserAgent(ITextOutputDevice outputDevice) {
            super(outputDevice);
        }

        protected InputStream resolveAndOpenStream(String uri) {
            InputStream is = super.resolveAndOpenStream(uri);
            System.out.println("IN resolveAndOpenStream() " + uri);
            return is;
        }
    }
}
