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

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;

import org.w3c.dom.Document;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

public class DocumentSplitter implements ContentHandler {
    private static final String HEAD_ELEMENT_NAME = "head";
    
    private List _processingInstructions = new LinkedList();
    private SAXEventRecorder _head = new SAXEventRecorder();
    private boolean _inHead = false;
    
    private int _depth = 0;
    
    private boolean _needNewNSScope = false;
    private NamespaceScope _currentNSScope = new NamespaceScope();

    private boolean _needNSScopePop;
    
    private Locator _locator;
    
    private TransformerHandler _handler;
    private boolean _inDocument = false;
    
    private List _documents = new LinkedList();
    
    private boolean _replayedHead = false;

    public void characters(char[] ch, int start, int length) throws SAXException {
        if (_inHead) {
            _head.characters(ch, start, length);
        } else if (_inDocument) {
            _handler.characters(ch, start, length);
        }
    }

    public void endDocument() throws SAXException {
    }

    public void endPrefixMapping(String prefix) throws SAXException {
        if (_inHead) {
            _head.endPrefixMapping(prefix);
        } else if (_inDocument) {
            _handler.endPrefixMapping(prefix);
        } else {
            _needNSScopePop = true;
        }
    }

    public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
        if (_inHead) {
            _head.ignorableWhitespace(ch, start, length);
        } else if (_inDocument) {
            _handler.ignorableWhitespace(ch, start, length);
        }
    }

    public void processingInstruction(String target, String data) throws SAXException {
        _processingInstructions.add(new ProcessingInstruction(target, data));
        
    }

    public void setDocumentLocator(Locator locator) {
        _locator = locator;
    }

    public void skippedEntity(String name) throws SAXException {
        if (_inHead) {
            _head.skippedEntity(name);
        } else if (_inDocument) {
            _handler.skippedEntity(name);
        }
    }

    public void startDocument() throws SAXException {
    }

    public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
        if (_inHead) {
            _head.startElement(uri, localName, qName, atts);
        } else if (_inDocument) {
            if (_depth == 2 && ! _replayedHead) {
                if (HEAD_ELEMENT_NAME.equalsIgnoreCase(qName)) {
                    _handler.startElement(uri, localName, qName, atts);
                    _head.replay(_handler);
                } else {
                    _handler.startElement("", HEAD_ELEMENT_NAME, HEAD_ELEMENT_NAME, new AttributesImpl());
                    _head.replay(_handler);
                    _handler.endElement("", HEAD_ELEMENT_NAME, HEAD_ELEMENT_NAME);
                    
                    _handler.startElement(uri, localName, qName, atts);
                }
                
                _replayedHead = true;
            } else {
                _handler.startElement(uri, localName, qName, atts);
            }
        } else {
            if (_needNewNSScope) {
                _needNewNSScope = false;
                _currentNSScope = new NamespaceScope(_currentNSScope);
            }
            
            if (_depth == 1) {
                if (HEAD_ELEMENT_NAME.equalsIgnoreCase(qName)) {
                    _inHead = true;
                    _currentNSScope.replay(_head, true);
                } else {
                    try {
                        _inDocument = true;
                        _replayedHead = false;
                        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                        factory.setNamespaceAware(true);
                        factory.setValidating(false);
                        
                        Document doc = factory.newDocumentBuilder().newDocument();
                        _documents.add(doc);
                        _handler = 
                            ((SAXTransformerFactory)SAXTransformerFactory.newInstance()).newTransformerHandler();
                        _handler.setResult(new DOMResult(doc));
                        
                        _handler.startDocument();
                        _handler.setDocumentLocator(_locator);
                        for (Iterator i = _processingInstructions.iterator(); i.hasNext(); ) {
                            ProcessingInstruction pI = (ProcessingInstruction)i.next();
                            _handler.processingInstruction(pI.getTarget(), pI.getData());
                        }
                        
                        _currentNSScope.replay(_handler, true);
                        _handler.startElement(uri, localName, qName, atts);
                    } catch (ParserConfigurationException e) {
                        throw new SAXException(e.getMessage(), e);
                    } catch (TransformerConfigurationException e) {
                        throw new SAXException(e.getMessage(), e);
                    }
                }
            }
        }
        
        _depth++;
    }
    
    public void endElement(String uri, String localName, String qName) throws SAXException {
        _depth--;
        
        if (_needNSScopePop) {
            _needNSScopePop = false;
            _currentNSScope = _currentNSScope.getParent();
        }
        
        if (_inHead) {
            if (_depth == 1) {
                _currentNSScope.replay(_head, false);
                _inHead = false;
            } else {
                _head.endElement(uri, localName, qName);
            }
        } else if (_inDocument) {
            if (_depth == 1) {
                _currentNSScope.replay(_handler, false);
                _handler.endElement(uri, localName, qName);
                _handler.endDocument();
                _inDocument = false;
            } else {
                _handler.endElement(uri, localName, qName);
            }
        }
    }    

    public void startPrefixMapping(String prefix, String uri) throws SAXException {
        if (_inHead) {
            _head.startPrefixMapping(prefix, uri);
        } else if (_inDocument) {
            _handler.startPrefixMapping(prefix, uri);
        } else {
            _needNewNSScope = true;
            _currentNSScope.addNamespace(new Namespace(prefix, uri));
        }
    }
    
    public List getDocuments() {
        return _documents;
    }
    
    private static final class Namespace {
        private String _prefix;
        private String _uri;
        
        public Namespace(String prefix, String uri) {
            _prefix = prefix;
            _uri = uri;
        }

        public String getPrefix() {
            return _prefix;
        }
        
        public String getUri() {
            return _uri;
        }
    }
    
    private static final class NamespaceScope {
        private NamespaceScope _parent;
        private List _namespaces = new LinkedList();
        
        public NamespaceScope() {
        }
        
        public NamespaceScope(NamespaceScope parent) {
            _parent = parent;
        }
        
        public void addNamespace(Namespace namespace) {
            _namespaces.add(namespace);
        }
        
        public void replay(ContentHandler contentHandler, boolean start) throws SAXException {
            replay(contentHandler, new HashSet(), start);
        }
        
        private void replay(ContentHandler contentHandler, Set seen, boolean start) 
                throws SAXException {
            for (Iterator i = _namespaces.iterator(); i.hasNext(); ) {
                Namespace ns = (Namespace)i.next();
                
                if (! seen.contains(ns.getPrefix())) {
                    seen.add(ns.getPrefix());
                    if (start) {
                        contentHandler.startPrefixMapping(ns.getPrefix(), ns.getUri());
                    } else {
                        contentHandler.endPrefixMapping(ns.getPrefix());
                    }
                }
            }
            
            if (_parent != null) {
                _parent.replay(contentHandler, seen, start);
            }
        }
        
        public NamespaceScope getParent() {
            return _parent;
        }
    }
    
    private static class ProcessingInstruction {
        private String _target;
        private String _data;
        
        public ProcessingInstruction(String target, String data) {
            _target = target;
            _data = data;
        }

        public String getData() {
            return _data;
        }
        
        public String getTarget() {
            return _target;
        }
    }
}
