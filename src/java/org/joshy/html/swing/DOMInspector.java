package org.joshy.html.swing;

import javax.swing.event.*;
import java.util.*;
import javax.swing.tree.*;
import javax.swing.*;
import java.awt.Graphics;
import java.awt.Dimension;
import java.awt.BorderLayout;
import org.w3c.dom.*;
import org.joshy.u;
import java.awt.Component;

public class DOMInspector extends JPanel {
    Document doc;
    JTree tree;
    JScrollPane scroll;
    public void setDocument(Document doc) {
        this.doc = doc;
    }
    
    
    public DOMInspector(Document doc) {
        super();
        
        this.setLayout(new java.awt.BorderLayout());
        setDocument(doc);
        
        // tree stuff
        TreeModel model = new DOMTreeModel(doc);
        this.tree = new JTree();
        tree.setModel(model);
        //tree.setPreferredSize(new Dimension(300,300));
        //ree.setSize(new Dimension(300,300));
        tree.setCellRenderer(new DOMTreeCellRenderer());

        this.scroll = new JScrollPane(tree);
        //scroll.setPreferredSize(new Dimension(300,300));
        //scroll.setHorizontalScrollBarPolicy(scroll.HORIZONTAL_SCROLLBAR_ALWAYS);
        //scroll.setVerticalScrollBarPolicy(scroll.VERTICAL_SCROLLBAR_ALWAYS);
        add(scroll,"Center");

        //this.add(tree,"Center");
        this.add(new JButton("close"),"South");
        this.setPreferredSize(new Dimension(300,300));
    }
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawLine(0,0,100,100);
    }
}

class DOMTreeModel implements TreeModel {
    Document doc;
    public DOMTreeModel(Document doc) {
        this.doc = doc;
    }
    List listeners = new ArrayList();

    //Adds a listener for the TreeModelEvent posted after the tree changes.
    public void addTreeModelListener(TreeModelListener l) {
        this.listeners.add(l);
    }

    //Returns the child of parent at index index in the parent's child array.
    public Object getChild(Object parent, int index) {
        Node node = (Node)parent;
        return node.getChildNodes().item(index);
    }

    //Returns the number of children of parent.
    public int getChildCount(Object parent) {
        Node node = (Node)parent;
        return node.getChildNodes().getLength();
    }

    //Returns the index of child in parent.
    public int getIndexOfChild(Object parent, Object child) {
        Node node = (Node)parent;
        for(int i=0; i<node.getChildNodes().getLength(); i++) {
            if(child == node.getChildNodes().item(i)) {
                return i;
            }
        }
        return -1;
    }

    //Returns the root of the tree.
    public Object getRoot() {
        return doc;
    }

    //Returns true if node is a leaf.
    public boolean isLeaf(Object nd) {
        Node node = (Node)nd;
        //u.p("checking leaf: "  + node.getNodeName());
        //u.p("is: " + node.hasChildNodes());
        return !node.hasChildNodes();
    }

    //Removes a listener previously added with addTreeModelListener.
    public void removeTreeModelListener(TreeModelListener l) {
        this.listeners.remove(l);
    }

    //Messaged when the user has altered the value for the item identified by path to newValue.   
    public void valueForPathChanged(TreePath path, Object newValue) {
        // no-op
    }
}

class DOMTreeCellRenderer extends DefaultTreeCellRenderer {
    public Component getTreeCellRendererComponent(JTree tree, Object value, 
        boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
            Node node = (Node)value;
            if(node.getNodeType() == node.ELEMENT_NODE) {
                value = "<"+node.getNodeName()+">";
            }
            if(node.getNodeType() == node.TEXT_NODE) {
                value = "\""+ node.getNodeValue() + "\"";
            }
            if(node.getNodeType() == node.COMMENT_NODE) {
                value = "<!-- " + node.getNodeValue() + " -->";
            }
            return super.getTreeCellRendererComponent(tree,value,selected,expanded,leaf,row,hasFocus);
    }
}
