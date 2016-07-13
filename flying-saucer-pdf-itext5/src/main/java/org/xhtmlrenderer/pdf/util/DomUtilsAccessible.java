package org.xhtmlrenderer.pdf.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xhtmlrenderer.layout.InlinePaintable;
import org.xhtmlrenderer.render.BlockBox;
import org.xhtmlrenderer.render.Box;
import org.xhtmlrenderer.render.InlineBox;
import org.xhtmlrenderer.render.InlineLayoutBox;
import org.xhtmlrenderer.render.InlineText;
import org.xhtmlrenderer.render.LineBox;

public class DomUtilsAccessible {
	public static String getNodeName(BlockBox parentBlockBox){
        String nodeName = parentBlockBox.getElement().getNodeName();
        return nodeName;
	}
	
	public static String getParentNodeName(BlockBox parentBlockBox){
        Node parentNode = parentBlockBox.getElement().getParentNode();
        String parentNodeName = parentNode.getNodeName();
        return parentNodeName;
	}
	
	public static String getParentNodeName(Box box){
        Node parentNode = null;
        String parentNodeName = null;
        if(box.getElement() != null){
        	parentNode = box.getElement();
        	if(parentNode != null){
        		parentNodeName = parentNode.getNodeName();
        	}
        }
        return parentNodeName;
	}
	

	
	public static int getNumChildren(Node node, String childrenNodeName){
		NodeList children = node.getChildNodes();
	    Node current = null;
	    int numChildren = 0;
	    for (int i = 0; i < children.getLength(); i++) {
	      current = children.item(i);
	      if (current.getNodeType() == Node.ELEMENT_NODE) {
	        Element currentElement = (Element) current;
	        if (currentElement.getTagName().equalsIgnoreCase(childrenNodeName)) {
	        	numChildren++;
	        }
	      }
	    }
	    return numChildren;
	}
	
	public static void getNumTextElementChildren(Node node, Vector<Integer> numChildren){
		NodeList children = node.getChildNodes();
	    Node current = null;
	    for (int i = 0; i < children.getLength(); i++) {
	      current = children.item(i);
	      if (current.getNodeType() == Node.TEXT_NODE) {
	    	  int currentValue = numChildren.get(0)  + 1;
	    	  numChildren.set(0, currentValue);
	      }else{
	    	  getNumTextElementChildren(current, numChildren);
	      }
	      // Check sibling children
	      if(current.getNextSibling() != null ){
	    	  getNumTextElementChildren(current.getNextSibling(), numChildren);
	      }
	    }
	}
	
	public static int getChildPosition(Node parent, Element child, String childrenNodeName){
		NodeList children = parent.getChildNodes();
	    Node current = null;
	    int cont = 0;
	    for (int i = 0; i < children.getLength(); i++) {
	      current = children.item(i);
	      if (current.getNodeType() == Node.ELEMENT_NODE && current.getNodeName().equalsIgnoreCase(childrenNodeName)) {
	    	cont++;
	        Element currentElement = (Element) current;
	        if (currentElement.isEqualNode(child)) {
	        	return cont;
	        }
	      }
	    }
	    return cont;
	}
	
	public static void getChildTextPosition(Node node, Vector<Integer> position, String text){
		if(position.size() <= 1){
			NodeList children = node.getChildNodes();
		    Node current = null;
		    for (int i = 0; i < children.getLength(); i++) {
		      current = children.item(i);
		      if (current.getNodeType() == Node.TEXT_NODE && position.size() <= 1) {
		    	  int currentValue = position.get(0)  + 1;
		    	  position.set(0, currentValue);
		    	  if(current.getNodeValue().equals(text)){
		    		  //Marking the vecto as found element to break recursion
		    		  position.add(100);
		    		  break;
		    	  }
		      }else{
		    	  getChildTextPosition(current, position, text);
		      }
		      // Check sibling children
		      if(current.getNextSibling() != null ){
		    	  getChildTextPosition(current.getNextSibling(), position, text);
		      }
		    }
		}
	}
	
    public static BlockBox getParentBlockBox(Box box){
    	Box lBox = box.getParent();
    	if(lBox != null){
	    	Box parent = lBox.getParent();
	    	if(parent instanceof BlockBox){
	    		return (BlockBox) parent;
	    	}else{
	    		return getParentBlockBox(parent);
	    	}
    	}else{
    		return null;
    	}
    }
    
    public static Box getParentBox(Box lBox){
    	if(lBox != null){
    		if(lBox instanceof Box){
    			return lBox;
    		}
	    	Box parent = lBox.getParent();
	    	if(parent instanceof Box){
	    		return (Box) parent;
	    	}else{
	    		return getParentBox(parent);
	    	}
    	}else{
    		return null;
    	}
    }
    
    public static Element getParentAnchorElement(Box lBox){
    	if(lBox != null){
    		String name = lBox.getElement().getNodeName();
    		if(lBox instanceof Box && "A".equalsIgnoreCase(name)){
    			return lBox.getElement();
    		}
	    	Box parent = lBox.getParent();
	    	if(parent != null && parent.getElement() != null){
		    	String parentName = parent.getElement().getNodeName();
	    		if("A".equalsIgnoreCase(parentName)){
		    		return parent.getElement();
		    	}else{
		    		return getParentAnchorElement(parent);
		    	}
	    	}else{
	    		return null;
	    	}
    	}
    	return null;
    }
    
	public static int getNumInlineTextChildren(Box box){
		Vector<Integer> cont = new Vector<Integer>(1);
		cont.add(0);
		getNumInlineTextChildren(box, cont);
		return cont.get(0);
	}
	
	private static void getNumInlineTextChildren(Box box, Vector<Integer> cont){
		List children = box.getChildren();
		if(box instanceof InlineLayoutBox){
			InlineLayoutBox inlineLB = (InlineLayoutBox)box;
			children = inlineLB.getInlineChildren();
		}
		for (Object child : children) {
			if(child instanceof LineBox){
				LineBox lineBox = (LineBox)child;
				getNumInlineTextChildren(lineBox, cont);
			}else if(child instanceof InlineLayoutBox){
				InlineLayoutBox lineBox = (InlineLayoutBox)child;
				getNumInlineTextChildren(lineBox, cont);
			}else if(child instanceof InlineText){
				cont.set(0, cont.get(0) + 1);
			}
		}
	}
	
	public static int getChildTextPosition(Box box, InlineText text){
		Vector<Integer> cont = new Vector<Integer>(1);
		cont.add(0);
		getChildTextPosition(box, text, cont);
		return cont.get(0);
	}
	
	private static void getChildTextPosition(Box box, InlineText text, Vector<Integer> cont){
		// Firstly check found element (vector with more than one element)
		if(cont.size() < 2){
			List children = box.getChildren();
			if(box instanceof InlineLayoutBox){
				InlineLayoutBox inlineLB = (InlineLayoutBox)box;
				children = inlineLB.getInlineChildren();
			}
			for (Object child : children) {
				if(child instanceof LineBox){
					LineBox lineBox = (LineBox)child;
					getChildTextPosition(lineBox, text, cont);
				}else if(child instanceof InlineLayoutBox){
					InlineLayoutBox lineBox = (InlineLayoutBox)child;
					getChildTextPosition(lineBox, text, cont);
				}else if(child instanceof InlineText){
					cont.set(0, cont.get(0) + 1);
					if(text.equals(child)){
						//Adding found element marker (vector with more than one element)
						cont.add(666);
						break;
					}
				}
			}
		}
	}
}
