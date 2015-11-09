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
package org.xhtmlrenderer.docx;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.xhtmlrenderer.extend.FSImage;
import org.xhtmlrenderer.extend.ReplacedElement;
import org.xhtmlrenderer.extend.ReplacedElementFactory;
import org.xhtmlrenderer.extend.UserAgentCallback;
import org.xhtmlrenderer.layout.LayoutContext;
import org.xhtmlrenderer.render.BlockBox;
import org.xhtmlrenderer.simple.extend.FormSubmissionListener;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class Docx4jReplacedElementFactory implements ReplacedElementFactory {
	private Docx4jDocxOutputDevice _outputDevice;

	private Map _radioButtonsByElem = new HashMap();
	private Map _radioButtonsByName = new HashMap();

	public Docx4jReplacedElementFactory(Docx4jDocxOutputDevice outputDevice) {
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
			FSImage fsImage = uac.getImageResource(e.getAttribute("src")).getImage();
			if (fsImage != null) {
				if (cssWidth != -1 || cssHeight != -1) {
					fsImage.scale(cssWidth, cssHeight);
				}
				return null; //new ITextImageElement(fsImage);
			}
		/*
		} else if (nodeName.equals("input")) {
			String type = e.getAttribute("type");
			if (type.equals("hidden")) {
			    return new EmptyReplacedElement(0, 0);
			} else if (type.equals("checkbox")) {
				return new CheckboxFormField(c, box, cssWidth, cssHeight);
			} else if (type.equals("radio")) {
				RadioButtonFormField result = new RadioButtonFormField(
						this, c, box, cssWidth, cssHeight);
				saveResult(e, result);
				return result;
			} else {
				return new TextFormField(c, box, cssWidth, cssHeight);
			}
		} else if (nodeName.equals("select")) {
		    return new SelectFormField(c, box, cssWidth, cssHeight);
		} else if (isTextarea(e)) {
		    return new TextAreaFormField(c, box, cssWidth, cssHeight);
		*/
//		} else if (nodeName.equals("bookmark")) {
//			// HACK Add box as named anchor and return placeholder
//			BookmarkElement result = new BookmarkElement();
//			if (e.hasAttribute("name")) {
//				String name = e.getAttribute("name");
//				c.addBoxId(name, box);
//				result.setAnchorName(name);
//			}
//			return result;
		}

		return null;
	}
	
	private boolean isTextarea(Element e) {
	    if (! e.getNodeName().equals("textarea")) {
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


	public void reset() {
		_radioButtonsByElem = new HashMap();
		_radioButtonsByName = new HashMap();
	}



	public List getRadioButtons(String name) {
		return (List)_radioButtonsByName.get(name);
	}

    public void setFormSubmissionListener(FormSubmissionListener listener) {
        // nothing to do, form submission is handled by pdf readers
    }

    public void remove(Element e) {
        // TODO Auto-generated method stub
        
    }
}
