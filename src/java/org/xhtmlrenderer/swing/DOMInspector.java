/*
 * {{{ header & license
 * Copyright (c) 2004 Joshua Marinacci
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
package org.xhtmlrenderer.swing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.ListSelectionModel;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.css.CSSPrimitiveValue;
import org.xhtmlrenderer.css.XRElement;
import org.xhtmlrenderer.css.XRProperty;
import org.xhtmlrenderer.css.XRValue;
import org.xhtmlrenderer.css.bridge.XRStyleReference;
import org.xhtmlrenderer.css.constants.ValueConstants;
import org.xhtmlrenderer.layout.Context;


/**
 * Description of the Class
 *
 * @author   empty
 */
public class DOMInspector extends JPanel {
    // PW
    /** Description of the Field */
    XRStyleReference xrStyleReference;
    /** Description of the Field */
    Context context;
    /** Description of the Field */
    ElementPropertiesPanel elementPropPanel;
    /** Description of the Field */
    DOMSelectionListener nodeSelectionListener;
    /** Description of the Field */
    JSplitPane splitPane;
    // PW

    /** Description of the Field */
    Document doc;
    /** Description of the Field */
    JButton close;
    /** Description of the Field */
    JTree tree;

    /** Description of the Field */
    JScrollPane scroll;

    /**
     * Constructor for the DOMInspector object
     *
     * @param doc  PARAM
     */
    public DOMInspector( Document doc ) {
        this( doc, null, null );
    }

    /**
     * Constructor for the DOMInspector object
     *
     * @param doc      PARAM
     * @param context  PARAM
     * @param xsr      PARAM
     */
    public DOMInspector( Document doc, Context context, XRStyleReference xsr ) {
        super();

        this.setLayout( new java.awt.BorderLayout() );

        JPanel treePanel = new JPanel();
        this.tree = new JTree();
        this.tree.getSelectionModel().setSelectionMode( TreeSelectionModel.SINGLE_TREE_SELECTION );
        this.scroll = new JScrollPane( tree );

        splitPane = null;
        if ( xsr == null ) {
            add( scroll, "Center" );
        } else {
            splitPane = new JSplitPane( JSplitPane.HORIZONTAL_SPLIT );
            splitPane.setOneTouchExpandable( true );
            splitPane.setDividerLocation( 150 );

            this.add( splitPane, "Center" );
            splitPane.setLeftComponent( scroll );
        }

        close = new JButton( "close" );
        this.add( close, "South" );
        this.setPreferredSize( new Dimension( 300, 300 ) );

        setForDocument( doc, context, xsr );

        close.addActionListener(
                    new ActionListener() {
                        public void actionPerformed( ActionEvent evt ) {
                            getFrame( DOMInspector.this ).hide();
                        }
                    } );
    }

    /**
     * Description of the Method
     *
     * @param g  PARAM
     */
    public void paintComponent( Graphics g ) {

        super.paintComponent( g );

        g.drawLine( 0, 0, 100, 100 );

    }

    /**
     * Sets the forDocument attribute of the DOMInspector object
     *
     * @param doc  The new forDocument value
     */
    public void setForDocument( Document doc ) {
        setForDocument( doc, null, null );
    }

    /**
     * Sets the forDocument attribute of the DOMInspector object
     *
     * @param doc      The new forDocument value
     * @param context  The new forDocument value
     * @param xsr      The new forDocument value
     */
    public void setForDocument( Document doc, Context context, XRStyleReference xsr ) {
        this.doc = doc;
        this.xrStyleReference = xsr;
        this.context = context;
        this.initForCurrentDocument();
    }

    /**
     * Gets the frame attribute of the DOMInspector object
     *
     * @param comp  PARAM
     * @return      The frame value
     */
    public JFrame getFrame( Component comp ) {
        if ( comp instanceof JFrame ) {
            return (JFrame)comp;
        }
        return getFrame( comp.getParent() );
    }

    /** Description of the Method */
    private void initForCurrentDocument() {
        // tree stuff
        TreeModel model = new DOMTreeModel( doc );
        tree.setModel( model );
        if ( !( tree.getCellRenderer() instanceof DOMTreeCellRenderer ) ) {
            tree.setCellRenderer( new DOMTreeCellRenderer() );
        }

        if ( xrStyleReference != null ) {
            if ( elementPropPanel != null ) {
                splitPane.remove( elementPropPanel );
            }
            elementPropPanel = new ElementPropertiesPanel( context, xrStyleReference );
            splitPane.setRightComponent( elementPropPanel );

            tree.removeTreeSelectionListener( nodeSelectionListener );

            nodeSelectionListener = new DOMSelectionListener( tree, xrStyleReference, elementPropPanel );
            tree.addTreeSelectionListener( nodeSelectionListener );
        }
    }
}

//-{{{ ElementPropertiesPanel
/**
 * Description of the Class
 *
 * @author   empty
 */
class ElementPropertiesPanel extends JPanel {

    /** Description of the Field */
    private Context _context;
    /** Description of the Field */
    private XRStyleReference _xsr;
    /** Description of the Field */
    private JTable _properties;
    /** Description of the Field */
    private TableModel _defaultTableModel;

    /**
     * Constructor for the ElementPropertiesPanel object
     *
     * @param context  PARAM
     * @param xsr      PARAM
     */
    ElementPropertiesPanel( Context context, XRStyleReference xsr ) {
        super();
        this._context = context;
        this._xsr = xsr;

        this._properties = new PropertiesJTable();
        this._defaultTableModel = new DefaultTableModel();

        this.setLayout( new BorderLayout() );
        this.add( new JScrollPane( _properties ), BorderLayout.CENTER );
    }

    /**
     * Sets the forElement attribute of the ElementPropertiesPanel object
     *
     * @param node  The new forElement value
     */
    public void setForElement( Node node ) {
        try {
            _properties.setModel( tableModel( node ) );
            TableColumnModel model = _properties.getColumnModel();
            if ( model.getColumnCount() > 0 ) {
                model.getColumn( 0 ).sizeWidthToFit();
            }
        } catch ( Exception ex ) {
            ex.printStackTrace();
        }
    }

    /**
     * Description of the Method
     *
     * @param node           PARAM
     * @return               Returns
     * @exception Exception  Throws
     */
    private TableModel tableModel( Node node )
        throws Exception {
        List props = new ArrayList();
        XRElement xrElem = _xsr.getNodeXRElement( node );
        if ( xrElem == null ) {
            Toolkit.getDefaultToolkit().beep();
            return _defaultTableModel;
        }
        Iterator iter = xrElem.derivedStyle().listXRProperties();
        while ( iter.hasNext() ) {
            XRProperty prop = (XRProperty)iter.next();
            prop = xrElem.derivedStyle().propertyByName( _context, prop.propertyName() );
            props.add( prop );
        }
        return new PropertiesTableModel( props );
    }

    /**
     * Description of the Class
     *
     * @author   empty
     */
    class PropertiesJTable extends JTable {
        /** Description of the Field */
        Font propLabelFont;
        /** Description of the Field */
        Font defaultFont;

        /** Constructor for the PropertiesJTable object */
        PropertiesJTable() {
            super();
            this.setColumnSelectionAllowed( false );
            this.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
            propLabelFont = new Font( "Courier New", Font.BOLD, 12 );
            defaultFont = new Font( "Default", Font.PLAIN, 12 );
        }

        /**
         * Gets the cellRenderer attribute of the PropertiesJTable object
         *
         * @param row  PARAM
         * @param col  PARAM
         * @return     The cellRenderer value
         */
        public TableCellRenderer getCellRenderer( int row, int col ) {
            JLabel label = (JLabel)super.getCellRenderer( row, col );
            label.setBackground( Color.white );
            label.setFont( defaultFont );
            if ( col == 0 ) {
                // BUG: not working?
                label.setFont( propLabelFont );
            } else if ( col == 2 ) {
                PropertiesTableModel pmodel = (PropertiesTableModel)this.getModel();
                XRProperty prop = (XRProperty)pmodel._properties.get( row );
                XRValue actual = prop.actualValue();
                if ( actual.cssValue().getCssText().startsWith( "rgb" ) ) {
                    label.setBackground( actual.asColor() );
                }
            }
            return (TableCellRenderer)label;
        }
    }

    /**
     * Description of the Class
     *
     * @author   Patrick Wright
     */
    class PropertiesTableModel extends AbstractTableModel {
        /** Description of the Field */
        String _colNames[] = {"Property Name", "Text", "Value", "Important-Inherit"};

        /** Description of the Field */
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
            XRProperty prop = (XRProperty)_properties.get( row );
            XRValue actual = prop.actualValue();

            Object val = null;
            switch ( col ) {

                case 0:
                    val = prop.propertyName();
                    break;
                case 1:
                    val = actual.cssValue().getCssText();
                    break;
                case 2:
                    if ( ValueConstants.isNumber( ( (CSSPrimitiveValue)actual.cssValue() ).getPrimitiveType() ) ) {
                        val = new Float( actual.asFloat() );
                    } else {
                        val = "";//actual.cssValue().getCssText();
                    }
                    break;
                case 3:
                    val = ( actual.isImportant() ? "!Imp" : "" ) +
                            " " +
                            ( actual.forcedInherit() ? "Inherit" : "" );
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
}//}}}

//-{{{ DOMSelectionListener
/**
 * Description of the Class
 *
 * @author   empty
 */
class DOMSelectionListener implements TreeSelectionListener {

    /** Description of the Field */
    private JTree _tree;
    /** Description of the Field */
    private XRStyleReference _xsr;
    /** Description of the Field */
    private ElementPropertiesPanel _elemPropPanel;

    /**
     * Constructor for the DOMSelectionListener object
     *
     * @param tree   PARAM
     * @param xsr    PARAM
     * @param panel  PARAM
     */
    DOMSelectionListener( JTree tree, XRStyleReference xsr, ElementPropertiesPanel panel ) {
        _tree = tree;
        _xsr = xsr;
        _elemPropPanel = panel;
    }

    /**
     * Description of the Method
     *
     * @param e  PARAM
     */
    public void valueChanged( TreeSelectionEvent e ) {
        Node node = (Node)_tree.getLastSelectedPathComponent();

        if ( node == null ) {
            return;
        }

        _elemPropPanel.setForElement( node );
    }
}//}}}

//-{{{
/**
 * Description of the Class
 *
 * @author   empty
 */
class DOMTreeModel implements TreeModel {

    /** Description of the Field */
    Document doc;

    /** Description of the Field */
    HashMap displayableNodes;

    /** Description of the Field */
    List listeners = new ArrayList();

    /**
     * Constructor for the DOMTreeModel object
     *
     * @param doc  PARAM
     */
    public DOMTreeModel( Document doc ) {
        this.displayableNodes = new HashMap();
        this.doc = doc;

    }


    //Adds a listener for the TreeModelEvent posted after the tree changes.

    /**
     * Adds the specified TreeModel listener to receive TreeModel events from
     * this component. If listener l is null, no exception is thrown and no
     * action is performed.
     *
     * @param l  Contains the TreeModelListener for TreeModelEvent data.
     */
    public void addTreeModelListener( TreeModelListener l ) {

        this.listeners.add( l );

    }


    //Removes a listener previously added with addTreeModelListener.

    /**
     * Removes the specified TreeModel listener so that it no longer receives
     * TreeModel events from this component. This method performs no function,
     * nor does it throw an exception, if the listener specified by the argument
     * was not previously added to this component. If listener l is null, no
     * exception is thrown and no action is performed.
     *
     * @param l  Contains the TreeModelListener for TreeModelEvent data.
     */
    public void removeTreeModelListener( TreeModelListener l ) {

        this.listeners.remove( l );

    }


    //Messaged when the user has altered the value for the item identified by path to newValue.

    /**
     * Description of the Method
     *
     * @param path      PARAM
     * @param newValue  PARAM
     */
    public void valueForPathChanged( TreePath path, Object newValue ) {

        // no-op

    }

    //Returns the child of parent at index index in the parent's child array.

    /**
     * Gets the child attribute of the DOMTreeModel object
     *
     * @param parent  PARAM
     * @param index   PARAM
     * @return        The child value
     */
    public Object getChild( Object parent, int index ) {

        Node node = (Node)parent;

        List children = (List)this.displayableNodes.get( parent );
        if ( children == null ) {
            children = addDisplayable( node );
        }

        // CLEAN return node.getChildNodes().item(index);
        return (Node)children.get( index );
    }


    //Returns the number of children of parent.

    /**
     * Gets the childCount attribute of the DOMTreeModel object
     *
     * @param parent  PARAM
     * @return        The childCount value
     */
    public int getChildCount( Object parent ) {

        Node node = (Node)parent;
        List children = (List)this.displayableNodes.get( parent );
        if ( children == null ) {
            children = addDisplayable( node );
        }

        // CLEAN return node.getChildNodes().getLength();
        return children.size();
    }


    //Returns the index of child in parent.

    /**
     * Gets the indexOfChild attribute of the DOMTreeModel object
     *
     * @param parent  PARAM
     * @param child   PARAM
     * @return        The indexOfChild value
     */
    public int getIndexOfChild( Object parent, Object child ) {

        Node node = (Node)parent;
        List children = (List)this.displayableNodes.get( parent );
        if ( children == null ) {
            children = addDisplayable( node );
        }
        if ( children.contains( child ) ) {
            return children.indexOf( child );
        } else {
            return -1;
        }

        /*
         * CLEAN
         * for(int i=0; i<node.getChildNodes().getLength(); i++) {
         * if(child == node.getChildNodes().item(i)) {
         * return i;
         * }
         * }
         * return -1;
         */
    }


    //Returns the root of the tree.

    /**
     * Gets the root attribute of the DOMTreeModel object
     *
     * @return   The root value
     */
    public Object getRoot() {

        return doc;
    }


    //Returns true if node is a leaf.

    /**
     * Gets the leaf attribute of the DOMTreeModel object
     *
     * @param nd  PARAM
     * @return    The leaf value
     */
    public boolean isLeaf( Object nd ) {

        Node node = (Node)nd;

        return !node.hasChildNodes();
    }

    // only adds displayable nodes--not stupid DOM text filler nodes
    /**
     * Adds a feature to the Displayable attribute of the DOMTreeModel object
     *
     * @param parent  The feature to be added to the Displayable attribute
     * @return        Returns
     */
    private List addDisplayable( Node parent ) {
        List children = (List)this.displayableNodes.get( parent );
        if ( children == null ) {
            children = new ArrayList();
            this.displayableNodes.put( parent, children );
            NodeList nl = parent.getChildNodes();
            for ( int i = 0, len = nl.getLength(); i < len; i++ ) {
                Node child = nl.item( i );
                if ( child.getNodeType() == Node.ELEMENT_NODE ||
                        child.getNodeType() == Node.COMMENT_NODE ||
                        ( child.getNodeType() == Node.TEXT_NODE && ( child.getNodeValue().trim().length() > 0 ) ) ) {
                    children.add( child );
                }
            }
            return children;
        } else {
            return new ArrayList();
        }
    }

}//}}}

//-{{{ DOMTreeCellRenderer
/**
 * Description of the Class
 *
 * @author   empty
 */
class DOMTreeCellRenderer extends DefaultTreeCellRenderer {

    /**
     * Gets the treeCellRendererComponent attribute of the DOMTreeCellRenderer
     * object
     *
     * @param tree      PARAM
     * @param value     PARAM
     * @param selected  PARAM
     * @param expanded  PARAM
     * @param leaf      PARAM
     * @param row       PARAM
     * @param hasFocus  PARAM
     * @return          The treeCellRendererComponent value
     */
    public Component getTreeCellRendererComponent( JTree tree, Object value,
                                                   boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus ) {

        Node node = (Node)value;

        if ( node.getNodeType() == node.ELEMENT_NODE ) {

            String cls = "";
            if ( node.hasAttributes() ) {
                Node cn = node.getAttributes().getNamedItem( "class" );
                if ( cn != null ) {
                    cls = " class='" + cn.getNodeValue() + "'";
                }
            }
            value = "<" + node.getNodeName() + cls + ">";

        }

        if ( node.getNodeType() == node.TEXT_NODE ) {

            if ( node.getNodeValue().trim().length() > 0 ) {
                value = "\"" + node.getNodeValue() + "\"";
            }
        }

        if ( node.getNodeType() == node.COMMENT_NODE ) {

            value = "<!-- " + node.getNodeValue() + " -->";

        }

        return super.getTreeCellRendererComponent( tree, value, selected, expanded, leaf, row, hasFocus );
    }
}//}}}

/*
 * $Id$
 *
 * $Log$
 * Revision 1.5  2004/10/28 13:46:33  joshy
 * removed dead code
 * moved code about specific elements to the layout factory (link and br)
 * fixed form rendering bug
 *
 * Issue number:
 * Obtained from:
 * Submitted by:
 * Reviewed by:
 *
 * Revision 1.4  2004/10/23 13:51:54  pdoubleya
 * Re-formatted using JavaStyle tool.
 * Cleaned imports to resolve wildcards except for common packages (java.io, java.util, etc).
 * Added CVS log comments at bottom.
 *
 *
 */

