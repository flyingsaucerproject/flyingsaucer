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

import java.awt.Color;
import java.awt.Font;
import java.awt.Toolkit;
import javax.swing.table.*;
import org.w3c.dom.css.CSSPrimitiveValue;
import org.joshy.html.*;
import com.pdoubleya.xhtmlrenderer.css.*;
import com.pdoubleya.xhtmlrenderer.css.constants.*;
import com.pdoubleya.xhtmlrenderer.css.bridge.*;

public class DOMInspector extends JPanel {
    // PW
    XRStyleReference xrStyleReference;
    Context context;
    ElementPropertiesPanel elementPropPanel;
    DOMSelectionListener nodeSelectionListener;
    // PW 
    
    Document doc;

    JTree tree;

    JScrollPane scroll;

    public void setForDocument(Document doc) {
        setForDocument(doc, null, null);
    }

    public void setForDocument(Document doc, Context context, XRStyleReference xsr) {
        this.doc = doc;
        this.xrStyleReference = xsr;
        this.context = context;
        this.initForCurrentDocument();
    }

    public DOMInspector(Document doc) {
        this(doc, null, null);
    }    

    public DOMInspector(Document doc, Context context, XRStyleReference xsr) {
        super();
        this.setLayout(new java.awt.BorderLayout());
        this.tree = new JTree();
        this.scroll = new JScrollPane(tree);
        if ( xsr == null ) {
            add(scroll,"Center");
        } else {
            add(scroll,"West");
            this.tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        }

        this.add(new JButton("close"),"South");
        this.setPreferredSize(new Dimension(300,300));

        setForDocument(doc, context, xsr);
    }

    public void paintComponent(Graphics g) {

        super.paintComponent(g);

        g.drawLine(0,0,100,100);

    }

    private void initForCurrentDocument() {
        // tree stuff
        TreeModel model = new DOMTreeModel(doc);
        tree.setModel(model);
        if ( ! (tree.getCellRenderer() instanceof DOMTreeCellRenderer) )
            tree.setCellRenderer(new DOMTreeCellRenderer());


        if ( xrStyleReference != null ) {        
            if ( elementPropPanel != null ) this.remove(elementPropPanel);
            elementPropPanel = new ElementPropertiesPanel(context, xrStyleReference);
            add(elementPropPanel,"Center");
            
            tree.removeTreeSelectionListener(nodeSelectionListener);

            nodeSelectionListener = new DOMSelectionListener(tree, xrStyleReference, elementPropPanel);
            tree.addTreeSelectionListener(nodeSelectionListener);
        }
    }
}

//-{{{ ElementPropertiesPanel
class ElementPropertiesPanel extends JPanel {
    private Context _context;
    private XRStyleReference _xsr;
    private JTable _properties;
    ElementPropertiesPanel(Context context, XRStyleReference xsr) {
        super();
        this._context = context;
        this._xsr = xsr;
        _properties = new PropertiesJTable();
        JScrollPane spn = new JScrollPane( _properties );
        this.setLayout( new BorderLayout() );
        this.add( spn, BorderLayout.CENTER );
    }
    
    public void setForElement(Node node) {
        try {
            _properties.setModel(tableModel(node));
            TableColumnModel model = _properties.getColumnModel();
            if ( model.getColumnCount() > 0 )
                model.getColumn(0).sizeWidthToFit();
        } catch (Exception ex) {
          ex.printStackTrace();
        }
    }
    
    class PropertiesJTable extends JTable {
        Font propLabelFont;
        Font defaultFont;
        PropertiesJTable() { 
            super();
            this.setColumnSelectionAllowed(false);
            this.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            propLabelFont = new Font("Courier New", Font.BOLD, 12);
            defaultFont = new Font("Default", Font.PLAIN, 12);
        }
        public TableCellRenderer getCellRenderer(int row, int col) {
            JLabel label = (JLabel)super.getCellRenderer(row, col);
            label.setBackground(Color.white);
            label.setFont(defaultFont);
            if ( col == 0 ) {
                // BUG: not working?
                label.setFont(propLabelFont);
            } else if ( col == 2 ) {
                PropertiesTableModel pmodel = (PropertiesTableModel)this.getModel();
                XRProperty prop = (XRProperty)pmodel._properties.get(row); 
                XRValue actual = prop.actualValue();
                if ( actual.cssValue().getCssText().startsWith("rgb")) 
                    label.setBackground(actual.asColor());
            }
            return (TableCellRenderer)label;
        }
    }

    /**
     * Description of the Method
     *
     * @return               Returns
     * @exception Exception  Throws
     */
    private TableModel tableModel(Node node)
        throws Exception {
        List props = new ArrayList();
        XRElement xrElem = _xsr.getNodeXRElement(node);
        if ( xrElem == null ) {
            Toolkit.getDefaultToolkit().beep();
            return new DefaultTableModel();
        }
        Iterator iter = xrElem.derivedStyle().listXRProperties();
        while ( iter.hasNext()) {
            XRProperty prop = (XRProperty)iter.next();
            prop = xrElem.derivedStyle().propertyByName(_context, prop.propertyName());
            props.add(prop);
        }
        return new PropertiesTableModel(props);
    }

    /**
     * Description of the Class
     *
     * @author   Patrick Wright
     */
    class PropertiesTableModel extends AbstractTableModel {
        /** Description of the Field */
        String _colNames[] = {"Property Name", "Text", "Value", "Convert", "Important-Inherit"};

        List _properties;
        
        /**
         * Constructor for the PropertiesTableModel object
         *
         * @param xrProperties  PARAM
         */
        PropertiesTableModel( List xrProperties ) { 
            _properties = xrProperties;
        }

        /**
         * Gets the columnName attribute of the PropertiesTableModel object
         *
         * @param col  PARAM
         * @return     The columnName value
         */
        public String getColumnName( int col ) {
            return _colNames[col];
        }

        /**
         * Gets the columnCount attribute of the PropertiesTableModel object
         *
         * @return   The columnCount value
         */
        public int getColumnCount() {
            return _colNames.length;
        }

        /**
         * Gets the rowCount attribute of the PropertiesTableModel object
         *
         * @return   The rowCount value
         */
        public int getRowCount() {
            return _properties.size();
        }

        /**
         * Gets the valueAt attribute of the PropertiesTableModel object
         *
         * @param row  PARAM
         * @param col  PARAM
         * @return     The valueAt value
         */
        public Object getValueAt( int row, int col ) {
            XRProperty prop = (XRProperty)_properties.get(row); 
            XRValue actual = prop.actualValue();
            
            Object val = null;
            switch( col ) {
            
            case 0:
            val = prop.propertyName();
            break;
            
            case 1:
            val = actual.cssValue().getCssText();
            break;
            
            case 2:
            if ( ValueConstants.isNumber(((CSSPrimitiveValue)actual.cssValue()).getPrimitiveType())) {
                val = new Float(actual.asFloat());
            } else {
                val = ""; //actual.cssValue().getCssText();
            }
            break;
            
            case 3:
            val = (ValueConstants.isAbsoluteUnit(actual.cssValue())? "" : "RELATIVE");
            break;
            
            case 4:
            val = (actual.isImportant() ? "!Imp" : "") +
                  " " +
                  (actual.forcedInherit() ? "Inherit" : "");
            break;
            }
            return val;
        }

        /**
         * Gets the cellEditable attribute of the PropertiesTableModel object
         *
         * @param row  PARAM
         * @param col  PARAM
         * @return     The cellEditable value
         */
        public boolean isCellEditable( int row, int col ) {
            return false;
        }
    }
} //}}}

//-{{{ DOMSelectionListener
class DOMSelectionListener implements TreeSelectionListener {
    private JTree _tree;
    private XRStyleReference _xsr;
    private ElementPropertiesPanel _elemPropPanel;
    
    DOMSelectionListener(JTree tree, XRStyleReference xsr, ElementPropertiesPanel panel) { 
        _tree = tree; 
        _xsr = xsr;
        _elemPropPanel = panel;
    }
    
    public void valueChanged(TreeSelectionEvent e) {
        Node node = (Node)_tree.getLastSelectedPathComponent();
    
        if (node == null) return;
    
        _elemPropPanel.setForElement(node);
    }    
}//}}}

//-{{{
class DOMTreeModel implements TreeModel {

    Document doc;

    HashMap displayableNodes;
    
    public DOMTreeModel(Document doc) {
        this.displayableNodes = new HashMap();
        this.doc = doc;

    }

    List listeners = new ArrayList();



    //Adds a listener for the TreeModelEvent posted after the tree changes.

    public void addTreeModelListener(TreeModelListener l) {

        this.listeners.add(l);

    }

    // only adds displayable nodes--not stupid DOM text filler nodes
    private List addDisplayable(Node parent) {
        List children = (List)this.displayableNodes.get(parent);
        if ( children == null ) {
            children = new ArrayList();
            this.displayableNodes.put(parent, children);
            NodeList nl = parent.getChildNodes();
            for ( int i=0, len=nl.getLength(); i < len; i++ ) {
                Node child = nl.item(i);
                if ( child.getNodeType() == Node.ELEMENT_NODE ||
                     child.getNodeType() == Node.COMMENT_NODE ||
                   ( child.getNodeType() == Node.TEXT_NODE && (child.getNodeValue().trim().length() > 0))) {
                    children.add(child);   
                }
            }
            return children;
        } else return new ArrayList();
    }

    //Returns the child of parent at index index in the parent's child array.

    public Object getChild(Object parent, int index) {

        Node node = (Node)parent;
        
        List children = (List)this.displayableNodes.get(parent);
        if ( children == null ) {
            children = addDisplayable(node);
        }
        
        // CLEAN return node.getChildNodes().item(index);
        return (Node)children.get(index);

    }



    //Returns the number of children of parent.

    public int getChildCount(Object parent) {

        Node node = (Node)parent;
        List children = (List)this.displayableNodes.get(parent);
        if ( children == null ) {
            children = addDisplayable(node);
        }

        // CLEAN return node.getChildNodes().getLength();
        return children.size();

    }



    //Returns the index of child in parent.

    public int getIndexOfChild(Object parent, Object child) {

        Node node = (Node)parent;
        List children = (List)this.displayableNodes.get(parent);
        if ( children == null ) {
            children = addDisplayable(node);
        }
        if ( children.contains(child))
            return children.indexOf(child);
        else
            return -1;

        /* CLEAN
        for(int i=0; i<node.getChildNodes().getLength(); i++) {

            if(child == node.getChildNodes().item(i)) {

                return i;

            }

        }

        return -1;
        */

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

}//}}}

//-{{{ DOMTreeCellRenderer
class DOMTreeCellRenderer extends DefaultTreeCellRenderer {

    public Component getTreeCellRendererComponent(JTree tree, Object value, 

        boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {

            Node node = (Node)value;

            if(node.getNodeType() == node.ELEMENT_NODE) {

                String cls = "";
                if ( node.hasAttributes()) {
                    Node cn = node.getAttributes().getNamedItem("class");
                    if ( cn != null ) {
                        cls = " class='" + cn.getNodeValue() +"'";
                    }
                }
                value = "<"+node.getNodeName()+ cls + ">";

            }

            if(node.getNodeType() == node.TEXT_NODE) {

                if ( node.getNodeValue().trim().length() > 0 ) 
                    value = "\""+ node.getNodeValue() + "\"";

            }

            if(node.getNodeType() == node.COMMENT_NODE) {

                value = "<!-- " + node.getNodeValue() + " -->";

            }

            return super.getTreeCellRendererComponent(tree,value,selected,expanded,leaf,row,hasFocus);

    }
}//}}}

