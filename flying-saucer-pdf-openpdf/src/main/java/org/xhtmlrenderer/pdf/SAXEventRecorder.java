/*
 * {{{ header & license
 * Copyright (c) 2007 Wisconsin Court System
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
package org.xhtmlrenderer.pdf;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

public class SAXEventRecorder implements ContentHandler {
    private List _events = new LinkedList();
    
    private interface Event {
        public void replay(ContentHandler handler) throws SAXException;
    }
    
    public void characters(final char[] ch, final int start, final int length) throws SAXException {
        _events.add(new Event() {
            public void replay(ContentHandler handler) throws SAXException {
                handler.characters(ch, start, length);
            }
        });
    }

    public void endDocument() throws SAXException {
        _events.add(new Event() {
            public void replay(ContentHandler handler) throws SAXException {
                handler.endDocument();
            }
        });
    }

    public void endElement(final String uri, final String localName, final String qName) throws SAXException {
        _events.add(new Event() {
            public void replay(ContentHandler handler) throws SAXException {
                handler.endElement(uri, localName, qName);
            }
        });
        
    }

    public void endPrefixMapping(final String prefix) throws SAXException {
        _events.add(new Event() {
            public void replay(ContentHandler handler) throws SAXException {
                handler.endPrefixMapping(prefix);
            }
        });
    }

    public void ignorableWhitespace(final char[] ch, final int start, final int length) throws SAXException {
        _events.add(new Event() {
            public void replay(ContentHandler handler) throws SAXException {
                handler.ignorableWhitespace(ch, start, length);
            }
        });
    }

    public void processingInstruction(final String target, final String data) throws SAXException {
        _events.add(new Event() {
            public void replay(ContentHandler handler) throws SAXException {
                handler.processingInstruction(target, data);
            }
        });
    }

    public void setDocumentLocator(final Locator locator) {
    }

    public void skippedEntity(final String name) throws SAXException {
        _events.add(new Event() {
            public void replay(ContentHandler handler) throws SAXException {
                handler.skippedEntity(name);
            }
        });        
    }

    public void startDocument() throws SAXException {
        _events.add(new Event() {
            public void replay(ContentHandler handler) throws SAXException {
                handler.startDocument();
            }
        });        
    }

    public void startElement(
            final String uri, final String localName, final String qName, final Attributes atts) throws SAXException {
        _events.add(new Event() {
            public void replay(ContentHandler handler) throws SAXException {
                handler.startElement(uri, localName, qName, atts);
            }
        });        
    }

    public void startPrefixMapping(final String prefix, final String uri) throws SAXException {
        _events.add(new Event() {
            public void replay(ContentHandler handler) throws SAXException {
                handler.startPrefixMapping(prefix, uri);
            }
        });        
    }
    
    public void replay(ContentHandler handler) throws SAXException {
        for (Iterator i = _events.iterator(); i.hasNext(); ) {
            Event e = (Event)i.next();
            e.replay(handler);
        }
    }
}
