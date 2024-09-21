/*
 * {{{ header & license
 * Copyright (c) 2024 Andreas Røsdal
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

package org.xhtmlrenderer.resource;

import org.jsoup.Jsoup;
import org.jsoup.helper.W3CDom;
import org.xhtmlrenderer.util.XRRuntimeException;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.w3c.dom.Document;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

/**
 * HTML parser using JSoup.
 *
 * @author Patrick Wright, Andreas Røsdal
 */
@ParametersAreNonnullByDefault
public class HTMLResource extends AbstractResource {
    private org.jsoup.nodes.Document jsoupDocument;

    private HTMLResource(InputStream stream) {
        super(stream);
    }

    private HTMLResource(@Nullable InputStream stream, org.jsoup.nodes.Document document) {
        super(stream);
        this.jsoupDocument = document;
    }

    public static HTMLResource load(InputStream stream) {
        return load(stream, null);
    }

    public static HTMLResource load(InputStream stream, String charset) {
        try {
            org.jsoup.nodes.Document document = Jsoup.parse(stream, charset, "");
            return new HTMLResource(stream, document);
        } catch (IOException e) {
            throw new XRRuntimeException("Could not load InputStream resource", e);
        }
    }

    public static HTMLResource load(Reader reader) {
        org.jsoup.nodes.Document document = Jsoup.parse(reader.toString());
        InputStream inputStream = toInputStream(document.html());
        return new HTMLResource(inputStream, document);
    }

    public static HTMLResource load(String html) {
        org.jsoup.nodes.Document document = Jsoup.parse(html);
        InputStream inputStream = toInputStream(html);
        return new HTMLResource(inputStream, document);
    }

    public org.jsoup.nodes.Document getJsoupDocument() {
        return jsoupDocument;
    }

    public Document getDocument() {
        try {
            W3CDom w3cDom = new W3CDom();
            return w3cDom.fromJsoup(jsoupDocument);

        } catch (Exception e) {
            throw new XRRuntimeException("Could not convert JSoup Document to w3c DOM Document", e);
        }
    }

    private static InputStream toInputStream(String html) {
        return new ByteArrayInputStream(html.getBytes(StandardCharsets.UTF_8));
    }
}