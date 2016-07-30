package org.xhtmlrenderer.pdf.util;

import java.util.List;
import java.util.Vector;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xhtmlrenderer.render.BlockBox;
import org.xhtmlrenderer.render.Box;
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
	
	private static void getNumTextElementChildren(Node node, Vector<Integer> numChildren){
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
	
	public static int getNumInlineLayoutBoxChildren(BlockBox parentBlockBox){
		List children = parentBlockBox.getChildren();
	    Object current = null;
	    int cont = 0;
	    for (int i = 0; i < children.size(); i++) {
	      current = children.get(i);
	      if(current instanceof LineBox){
	    	  LineBox lineBox = (LineBox)current;
	    	  return getNumInlineChildren(lineBox);
	      }
	    }
	    return cont;
	}
	
	public static int getNumInlineChildren(InlineLayoutBox inlineLayoutBox){
		return inlineLayoutBox.getInlineChildren().size();
	}
	
	public static int getNumInlineChildren(LineBox parentLineBox){
		List children = parentLineBox.getChildren();
	    Object current = null;
	    int cont = 0;
	    for (int i = 0; i < children.size(); i++) {
	      current = children.get(i);
	      if (current instanceof InlineLayoutBox) {
	    	cont+= getNumInlineChildren((InlineLayoutBox)current);
	      }
	    }
	    return cont;
	}
	
	public static int getChildPosition(BlockBox parentBlockBox, InlineText child){
		List children = parentBlockBox.getChildren();
		InlineLayoutBox childLayoutBox = child.getParent();
	    Object current = null;
	    int cont = 0;
	    for (int i = 0; i < children.size(); i++) {
	      current = children.get(i);
	      if(current instanceof LineBox){
	    	  LineBox lineBox = (LineBox)current;
	    	  return getChildPosition(lineBox, child);
	      }
	    }
	    return cont;
	}
	
	public static int getChildPosition(LineBox parentLineBox, InlineText child){
		List children = parentLineBox.getChildren();
		InlineLayoutBox childLayoutBox = child.getParent();
	    Object current = null;
	    int cont = 0;
	    for (int i = 0; i < children.size(); i++) {
	      current = children.get(i);
	      if (current instanceof InlineLayoutBox) {
	    	cont++;
	    	InlineLayoutBox currentElement = (InlineLayoutBox) current;
	        if (currentElement == childLayoutBox) {
	        	return cont;
	        }
	      }
	    }
	    return cont;
	}
	
	
	
	private static void getChildTextPosition(Node node, Vector<Integer> position, String text){
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
    	if(box instanceof BlockBox){
    		return (BlockBox) box;
    	}else{
	    	return getParentBlockBox(box.getParent());
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
    
    public static Element getParentAnchorElement(Element element){
    	if(element != null){
    		String name = element.getNodeName();
    		if(element instanceof Box && "A".equalsIgnoreCase(name)){
    			return element;
    		}
	    	Node parent = element.getParentNode();
	    	if(parent != null){
	    		if(parent.getNodeType() == Node.ELEMENT_NODE){
			    	String parentName = parent.getNodeName();
		    		if("A".equalsIgnoreCase(parentName)){
			    		return (Element)parent;
		    		}
		    	}else{
		    		return getParentAnchorElement((Element)parent);
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
				InlineText inlineText = (InlineText)child;
				String text = inlineText.getMasterText();
				// Controlling return carriage char, not count it as text
				if(!text.equals("\n")){
					cont.set(0, cont.get(0) + 1);
				}
			}
		}
	}
	
	public static int getChildTextPosition(Box box, InlineText text){
		Vector<Integer> cont = new Vector<Integer>(1);
		cont.add(0);
		getChildTextPosition(box, text, cont);
		return cont.get(0);
	}
	
	private static void getChildTextPosition(Box box, InlineText inlineText, Vector<Integer> cont){
		// Firstly check if element has been found (vector with more than one element)
		if(cont.size() < 2){
			List children = box.getChildren();
			if(box instanceof InlineLayoutBox){
				InlineLayoutBox inlineLB = (InlineLayoutBox)box;
				children = inlineLB.getInlineChildren();
			}
			for (Object child : children) {
				if(child instanceof LineBox){
					LineBox lineBox = (LineBox)child;
					getChildTextPosition(lineBox, inlineText, cont);
				}else if(child instanceof InlineLayoutBox){
					InlineLayoutBox lineBox = (InlineLayoutBox)child;
					getChildTextPosition(lineBox, inlineText, cont);
				}else if(child instanceof InlineText){
					// check if element has been found (vector with more than one element)
					if(cont.size() < 2){
						InlineText inlineTextChild = (InlineText)child;
						String sText = inlineTextChild.getMasterText();
						// Controlling return carriage char, not count it as text
						if(!sText.equals("\n")){
							cont.set(0, cont.get(0) + 1);
							if(inlineText.equals(inlineTextChild)){
								//Adding found element marker (vector with more than one element)
								cont.add(666);
								break;
							}
						}
					}
				}
			}
		}
	}
}
