/*
 * {{{ header & license
 * Copyright (c) 2006 Wisconsin Court System
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
package org.xhtmlrenderer.pdf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xhtmlrenderer.extend.FSImage;
import org.xhtmlrenderer.extend.ReplacedElement;
import org.xhtmlrenderer.extend.ReplacedElementFactory;
import org.xhtmlrenderer.extend.UserAgentCallback;
import org.xhtmlrenderer.layout.LayoutContext;
import org.xhtmlrenderer.render.BlockBox;
import org.xhtmlrenderer.simple.extend.FormSubmissionListener;

public class ITextReplacedElementFactory implements ReplacedElementFactory {
    private ITextOutputDevice _outputDevice;

    private Map _radioButtonsByElem = new HashMap();
    private Map _radioButtonsByName = new HashMap();

    public ITextReplacedElementFactory(ITextOutputDevice outputDevice) {
        _outputDevice = outputDevice;
    }

    public ReplacedElement createReplacedElement(LayoutContext c, BlockBox box,
            UserAgentCallback uac, int cssWidth, int cssHeight) {
        Element e = box.getElement();
        if (e == null) {
            return null;
        }

        String nodeName = e.getNodeName();
        if (nodeName.equals("img")) {
            String srcAttr = e.getAttribute("src");
            if (srcAttr != null && srcAttr.length() > 0) {
                FSImage fsImage = uac.getImageResource(srcAttr).getImage();
                if (fsImage != null) {
                    if (cssWidth != -1 || cssHeight != -1) {
                        fsImage.scale(cssWidth, cssHeight);
                    }
                    return new ITextImageElement(fsImage);
                }                    
            }

        } else if (nodeName.equals("input")) {
            String type = e.getAttribute("type");
            if (type.equals("hidden")) {
                return new EmptyReplacedElement(1, 1);
            } else if (type.equals("checkbox")) {
                return new CheckboxFormField(c, box, cssWidth, cssHeight);
            } else if (type.equals("radio")) {
                //TODO finish support for Radio button
                //RadioButtonFormField result = new RadioButtonFormField(
                //			this, c, box, cssWidth, cssHeight);
                //		saveResult(e, result);
                //return result;
                return new EmptyReplacedElement(0, 0);

            } else {
                return new TextFormField(c, box, cssWidth, cssHeight);
            }
            /*
             } else if (nodeName.equals("select")) {//TODO Support select
             return new SelectFormField(c, box, cssWidth, cssHeight);
             } else if (isTextarea(e)) {//TODO Review if this is needed the textarea item prints fine currently
             return new TextAreaFormField(c, box, cssWidth, cssHeight);
             */
        } else if (nodeName.equals("bookmark")) {
            // HACK Add box as named anchor and return placeholder
            BookmarkElement result = new BookmarkElement();
            if (e.hasAttribute("name")) {
                String name = e.getAttribute("name");
                c.addBoxId(name, box);
                result.setAnchorName(name);
            }
            return result;
        }

        return null;
    }

    private boolean isTextarea(Element e) {
        if (!e.getNodeName().equals("textarea")) {
            return false;
        }

        Node n = e.getFirstChild();
        while (n != null) {
            short nodeType = n.getNodeType();
            if (nodeType != Node.TEXT_NODE && nodeType != Node.CDATA_SECTION_NODE) {
                return false;
            }
        }

        return true;
    }

    private void saveResult(Element e, RadioButtonFormField result) {
        _radioButtonsByElem.put(e, result);

        String fieldName = result.getFieldName(_outputDevice, e);
        List fields = (List) _radioButtonsByName.get(fieldName);
        if (fields == null) {
            fields = new ArrayList();
            _radioButtonsByName.put(fieldName, fields);
        }
        fields.add(result);
    }

    public void reset() {
        _radioButtonsByElem = new HashMap();
        _radioButtonsByName = new HashMap();
    }

    public void remove(Element e) {
        RadioButtonFormField field = (RadioButtonFormField) _radioButtonsByElem.remove(e);
        if (field != null) {
            String fieldName = field.getFieldName(_outputDevice, e);
            List values = (List) _radioButtonsByName.get(fieldName);
            if (values != null) {
                values.remove(field);
                if (values.size() == 0) {
                    _radioButtonsByName.remove(fieldName);
                }
            }
        }
    }

    public void remove(String fieldName) {
        List values = (List) _radioButtonsByName.get(fieldName);
        if (values != null) {
            for (Iterator i = values.iterator(); i.hasNext();) {
                RadioButtonFormField field = (RadioButtonFormField) i.next();
                _radioButtonsByElem.remove(field.getBox().getElement());
            }
        }

        _radioButtonsByName.remove(fieldName);
    }

    public List getRadioButtons(String name) {
        return (List) _radioButtonsByName.get(name);
    }

    public void setFormSubmissionListener(FormSubmissionListener listener) {
        // nothing to do, form submission is handled by pdf readers
    }
}
