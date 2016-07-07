package org.xhtmlrenderer.pdf.util;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xhtmlrenderer.render.BlockBox;

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
}
