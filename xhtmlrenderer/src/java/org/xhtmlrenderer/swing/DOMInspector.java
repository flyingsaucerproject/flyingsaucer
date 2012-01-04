/*
 * {{{ header & license
 * Copyright (c) 2004, 2005 Joshua Marinacci
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

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.css.CSSPrimitiveValue;
import org.xhtmlrenderer.context.StyleReference;
import org.xhtmlrenderer.css.constants.ValueConstants;
import org.xhtmlrenderer.layout.SharedContext;

import javax.swing.*;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.table.*;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Description of the Class
 *
 * @author empty
 */
public class DOMInspector extends JPanel {
    private static final long serialVersionUID = 1L;

    // PW
    /**
     * Description of the Field
     */
    StyleReference styleReference;
    /**
     * Description of the Field
     */
    SharedContext context;
    /**
     * Description of the Field
     */
    ElementPropertiesPanel elementPropPanel;
    /**
     * Description of the Field
     */
    DOMSelectionListener nodeSelectionListener;
    /**
     * Description of the Field
     */
    JSplitPane splitPane;
    // PW

    /**
     * Description of the Field
     */
    Document doc;
    /**
     * Description of the Field
     */
    JButton close;
    /**
     * Description of the Field
     */
    JTree tree;

    /**
     * Description of the Field
     */
    JScrollPane scroll;

    /**
     * Constructor for the DOMInspector object
     *
     * @param doc PARAM
     */
    public DOMInspector(Document doc) {
        this(doc, null, null);
    }

    /**
     * Constructor for the DOMInspector object
     *
     * @param doc     PARAM
     * @param context PARAM
     * @param sr      PARAM
     */
    public DOMInspector(Document doc, SharedContext context, StyleReference sr) {
        super();

        this.setLayout(new java.awt.BorderLayout());

        //JPanel treePanel = new JPanel();
        this.tree = new JTree();
        this.tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        this.scroll = new JScrollPane(tree);

        splitPane = null;
        if (sr == null) {
            add(scroll, "Center");
        } else {
            splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
            splitPane.setOneTouchExpandable(true);
            splitPane.setDividerLocation(150);

            this.add(splitPane, "Center");
            splitPane.setLeftComponent(scroll);
        }

        close = new JButton("close");
        this.add(close, "South");
        this.setPreferredSize(new Dimension(300, 300));

        setForDocument(doc, context, sr);

        close.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                getFrame(DOMInspector.this).setVisible(false);
            }
        });
    }

    /**
     * Description of the Method
     *
     * @param g PARAM
     */
    public void paintComponent(Graphics g) {

        super.paintComponent(g);

        g.drawLine(0, 0, 100, 100);

    }

    /**
     * Sets the forDocument attribute of the DOMInspector object
     *
     * @param doc The new forDocument value
     */
    public void setForDocument(Document doc) {
        setForDocument(doc, null, null);
    }

    /**
     * Sets the forDocument attribute of the DOMInspector object
     *
     * @param doc     The new forDocument value
     * @param context The new forDocument value
     * @param sr      The new forDocument value
     */
    public void setForDocument(Document doc, SharedContext context, StyleReference sr) {
        this.doc = doc;
        this.styleReference = sr;
        this.context = context;
        this.initForCurrentDocument();
    }

    /**
     * Gets the frame attribute of the DOMInspector object
     *
     * @param comp PARAM
     * @return The frame value
     */
    public JFrame getFrame(Component comp) {
        if (comp instanceof JFrame) {
            return (JFrame) comp;
        }
        return getFrame(comp.getParent());
    }

    /**
     * Description of the Method
     */
    private void initForCurrentDocument() {
        // tree stuff
        TreeModel model = new DOMTreeModel(doc);
        tree.setModel(model);
        if (!(tree.getCellRenderer() instanceof DOMTreeCellRenderer)) {
            tree.setCellRenderer(new DOMTreeCellRenderer());
        }

        if (styleReference != null) {
            if (elementPropPanel != null) {
                splitPane.remove(elementPropPanel);
            }
            elementPropPanel = new ElementPropertiesPanel(styleReference);
            splitPane.setRightComponent(elementPropPanel);

            tree.removeTreeSelectionListener(nodeSelectionListener);

            //nodeSelectionListener = new DOMSelectionListener( tree, styleReference, elementPropPanel );
            nodeSelectionListener = new DOMSelectionListener(tree, elementPropPanel);
            tree.addTreeSelectionListener(nodeSelectionListener);
        }
    }
}

//-{{{ ElementPropertiesPanel

/**
 * Description of the Class
 *
 * @author empty
 */
class ElementPropertiesPanel extends JPanel {
    private static final long serialVersionUID = 1L;

    /**
     * Description of the Field
     */
    //private SharedContext _context;
    /**
     * Description of the Field
     */
    private StyleReference _sr;
    /**
     * Description of the Field
     */
    private JTable _properties;
    /**
     * Description of the Field
     */
    private TableModel _defaultTableModel;

    /**
     * Constructor for the ElementPropertiesPanel object
     *
     * @param sr PARAM
     */
    ElementPropertiesPanel(StyleReference sr) {
        super();
        //this._context = context;
        this._sr = sr;

        this._properties = new PropertiesJTable();
        this._defaultTableModel = new DefaultTableModel();

        this.setLayout(new BorderLayout());
        this.add(new JScrollPane(_properties), BorderLayout.CENTER);
    }

    /**
     * Sets the forElement attribute of the ElementPropertiesPanel object
     *
     * @param node The new forElement value
     */
    public void setForElement(Node node) {
        try {
            _properties.setModel(tableModel(node));
            TableColumnModel model = _properties.getColumnModel();
            if (model.getColumnCount() > 0) {
                model.getColumn(0).sizeWidthToFit();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Description of the Method
     *
     * @param node PARAM
     * @return Returns
     * @throws Exception Throws
     */
    private TableModel tableModel(Node node) {
        if (node.getNodeType() != Node.ELEMENT_NODE) {
            Toolkit.getDefaultToolkit().beep();
            return _defaultTableModel;
        }
        Map props = _sr.getCascadedPropertiesMap((Element) node);
        return new PropertiesTableModel(props);
    }

    /**
     * Description of the Class
     *
     * @author empty
     */
    static class PropertiesJTable extends JTable {
        private static final long serialVersionUID = 1L;

        /**
         * Description of the Field
         */
        Font propLabelFont;
        /**
         * Description of the Field
         */
        Font defaultFont;

        /**
         * Constructor for the PropertiesJTable object
         */
        PropertiesJTable() {
            super();
            this.setColumnSelectionAllowed(false);
            this.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            propLabelFont = new Font("Courier New", Font.BOLD, 12);
            defaultFont = new Font("Default", Font.PLAIN, 12);
        }

        /**
         * Gets the cellRenderer attribute of the PropertiesJTable object
         *
         * @param row PARAM
         * @param col PARAM
         * @return The cellRenderer value
         */
        public TableCellRenderer getCellRenderer(int row, int col) {
            JLabel label = (JLabel) super.getCellRenderer(row, col);
            label.setBackground(Color.white);
            label.setFont(defaultFont);
            if (col == 0) {
                // BUG: not working?
                label.setFont(propLabelFont);
            } else if (col == 2) {
                PropertiesTableModel pmodel = (PropertiesTableModel) this.getModel();
                Map.Entry me = (Map.Entry) pmodel._properties.entrySet().toArray()[row];
                CSSPrimitiveValue cpv = (CSSPrimitiveValue) me.getValue();
                if (cpv.getCssText().startsWith("rgb")) {
                    label.setBackground(org.xhtmlrenderer.css.util.ConversionUtil.rgbToColor(cpv.getRGBColorValue()));
                }
            }
            return (TableCellRenderer) label;
        }
    }

    /**
     * Description of the Class
     *
     * @author Patrick Wright
     */
    static class PropertiesTableModel extends AbstractTableModel {
        private static final long serialVersionUID = 1L;

        /**
         * Description of the Field
         */
        //String _colNames[] = {"Property Name", "Text", "Value", "Important-Inherit"};
        String _colNames[] = {"Property Name", "Text", "Value"};

        /**
         * Description of the Field
         */
        Map _properties;

        /**
         * Constructor for the PropertiesTableModel object
         *
         * @param cssProperties PARAM
         */
        PropertiesTableModel(Map cssProperties) {
            _properties = cssProperties;
        }

        /**
         * Gets the columnName attribute of the PropertiesTableModel object
         *
         * @param col PARAM
         * @return The columnName value
         */
        public String getColumnName(int col) {
            return _colNames[col];
        }

        /**
         * Gets the columnCount attribute of the PropertiesTableModel object
         *
         * @return The columnCount value
         */
        public int getColumnCount() {
            return _colNames.length;
        }

        /**
         * Gets the rowCount attribute of the PropertiesTableModel object
         *
         * @return The rowCount value
         */
        public int getRowCount() {
            return _properties.size();
        }

        /**
         * Gets the valueAt attribute of the PropertiesTableModel object
         *
         * @param row PARAM
         * @param col PARAM
         * @return The valueAt value
         */
        public Object getValueAt(int row, int col) {
            Map.Entry me = (Map.Entry) _properties.entrySet().toArray()[row];
            CSSPrimitiveValue cpv = (CSSPrimitiveValue) me.getValue();

            Object val = null;
            switch (col) {

                case 0:
                    val = me.getKey();
                    break;
                case 1:
                    val = cpv.getCssText();
                    break;
                case 2:
                    if (ValueConstants.isNumber(cpv.getPrimitiveType())) {
                        val = new Float(cpv.getFloatValue(cpv.getPrimitiveType()));
                    } else {
                        val = "";//actual.cssValue().getCssText();
                    }
                    break;
                    /* ouch, can't do this now: case 3:
                        val = ( cpv.actual.isImportant() ? "!Imp" : "" ) +
                                " " +
                                ( actual.forcedInherit() ? "Inherit" : "" );
                        break;
                     */
            }
            return val;
        }

        /**
         * Gets the cellEditable attribute of the PropertiesTableModel object
         *
         * @param row PARAM
         * @param col PARAM
         * @return The cellEditable value
         */
        public boolean isCellEditable(int row, int col) {
            return false;
        }
    }
}//}}}

//-{{{ DOMSelectionListener

/**
 * Description of the Class
 *
 * @author empty
 */
class DOMSelectionListener implements TreeSelectionListener {

    /**
     * Description of the Field
     */
    private JTree _tree;
    /** Description of the Field */
    //private StyleReference _sr;
    /**
     * Description of the Field
     */
    private ElementPropertiesPanel _elemPropPanel;

    /**
     * Constructor for the DOMSelectionListener object
     *
     * @param tree  PARAM
     * @param panel PARAM
     */
    //DOMSelectionListener( JTree tree, StyleReference sr, ElementPropertiesPanel panel ) {
    DOMSelectionListener(JTree tree, ElementPropertiesPanel panel) {
        _tree = tree;
        //_sr = sr;
        _elemPropPanel = panel;
    }

    /**
     * Description of the Method
     *
     * @param e PARAM
     */
    public void valueChanged(TreeSelectionEvent e) {
        Node node = (Node) _tree.getLastSelectedPathComponent();

        if (node == null) {
            return;
        }

        _elemPropPanel.setForElement(node);
    }
}//}}}

//-{{{

/**
 * Description of the Class
 *
 * @author empty
 */
class DOMTreeModel implements TreeModel {

    /**
     * Description of the Field
     */
    Document doc;

    /**
     * Our root for display
     */
    Node root;

    /**
     * Description of the Field
     */
    HashMap displayableNodes;

    /**
     * Description of the Field
     */
    List listeners = new ArrayList();

    /**
     * Constructor for the DOMTreeModel object
     *
     * @param doc PARAM
     */
    public DOMTreeModel(Document doc) {
        this.displayableNodes = new HashMap();
        this.doc = doc;
        setRoot("body");
    }

    private void setRoot(String rootNodeName) {
        Node tempRoot = doc.getDocumentElement();
        NodeList nl = tempRoot.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            if (nl.item(i).getNodeName().toLowerCase().equals(rootNodeName)) {
                this.root = nl.item(i);
            }
        }
    }


    //Adds a listener for the TreeModelEvent posted after the tree changes.

    /**
     * Adds the specified TreeModel listener to receive TreeModel events from
     * this component. If listener l is null, no exception is thrown and no
     * action is performed.
     *
     * @param l Contains the TreeModelListener for TreeModelEvent data.
     */
    public void addTreeModelListener(TreeModelListener l) {

        this.listeners.add(l);

    }


    //Removes a listener previously added with addTreeModelListener.

    /**
     * Removes the specified TreeModel listener so that it no longer receives
     * TreeModel events from this component. This method performs no function,
     * nor does it throw an exception, if the listener specified by the argument
     * was not previously added to this component. If listener l is null, no
     * exception is thrown and no action is performed.
     *
     * @param l Contains the TreeModelListener for TreeModelEvent data.
     */
    public void removeTreeModelListener(TreeModelListener l) {

        this.listeners.remove(l);

    }


    //Messaged when the user has altered the value for the item identified by path to newValue.

    /**
     * Description of the Method
     *
     * @param path     PARAM
     * @param newValue PARAM
     */
    public void valueForPathChanged(TreePath path, Object newValue) {

        // no-op

    }

    //Returns the child of parent at index index in the parent's child array.

    /**
     * Gets the child attribute of the DOMTreeModel object
     *
     * @param parent PARAM
     * @param index  PARAM
     * @return The child value
     */
    public Object getChild(Object parent, int index) {

        Node node = (Node) parent;

        List children = (List) this.displayableNodes.get(parent);
        if (children == null) {
            children = addDisplayable(node);
        }

        return (Node) children.get(index);
    }


    //Returns the number of children of parent.

    /**
     * Gets the childCount attribute of the DOMTreeModel object
     *
     * @param parent PARAM
     * @return The childCount value
     */
    public int getChildCount(Object parent) {

        Node node = (Node) parent;
        List children = (List) this.displayableNodes.get(parent);
        if (children == null) {
            children = addDisplayable(node);
        }

        return children.size();
    }


    //Returns the index of child in parent.

    /**
     * Gets the indexOfChild attribute of the DOMTreeModel object
     *
     * @param parent PARAM
     * @param child  PARAM
     * @return The indexOfChild value
     */
    public int getIndexOfChild(Object parent, Object child) {

        Node node = (Node) parent;
        List children = (List) this.displayableNodes.get(parent);
        if (children == null) {
            children = addDisplayable(node);
        }
        if (children.contains(child)) {
            return children.indexOf(child);
        } else {
            return -1;
        }
    }


    //Returns the root of the tree.

    /**
     * Gets the root attribute of the DOMTreeModel object
     *
     * @return The root value
     */
    public Object getRoot() {

        return this.root;
    }


    //Returns true if node is a leaf.

    /**
     * Gets the leaf attribute of the DOMTreeModel object
     *
     * @param nd PARAM
     * @return The leaf value
     */
    public boolean isLeaf(Object nd) {

        Node node = (Node) nd;

        return !node.hasChildNodes();
    }

    // only adds displayable nodes--not stupid DOM text filler nodes
    /**
     * Adds a feature to the Displayable attribute of the DOMTreeModel object
     *
     * @param parent The feature to be added to the Displayable attribute
     * @return Returns
     */
    private List addDisplayable(Node parent) {
        List children = (List) this.displayableNodes.get(parent);
        if (children == null) {
            children = new ArrayList();
            this.displayableNodes.put(parent, children);
            NodeList nl = parent.getChildNodes();
            for (int i = 0, len = nl.getLength(); i < len; i++) {
                Node child = nl.item(i);
                if (child.getNodeType() == Node.ELEMENT_NODE ||
                        child.getNodeType() == Node.COMMENT_NODE ||
                        (child.getNodeType() == Node.TEXT_NODE && (child.getNodeValue().trim().length() > 0))) {
                    children.add(child);
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
 * @author empty
 */
class DOMTreeCellRenderer extends DefaultTreeCellRenderer {
    private static final long serialVersionUID = 1L;

    /**
     * Gets the treeCellRendererComponent attribute of the DOMTreeCellRenderer
     * object
     *
     * @param tree     PARAM
     * @param value    PARAM
     * @param selected PARAM
     * @param expanded PARAM
     * @param leaf     PARAM
     * @param row      PARAM
     * @param hasFocus PARAM
     * @return The treeCellRendererComponent value
     */
    public Component getTreeCellRendererComponent(JTree tree, Object value,
                                                  boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {

        Node node = (Node) value;

        if (node.getNodeType() == Node.ELEMENT_NODE) {

            String cls = "";
            if (node.hasAttributes()) {
                Node cn = node.getAttributes().getNamedItem("class");
                if (cn != null) {
                    cls = " class='" + cn.getNodeValue() + "'";
                }
            }
            value = "<" + node.getNodeName() + cls + ">";

        }

        if (node.getNodeType() == Node.TEXT_NODE) {

            if (node.getNodeValue().trim().length() > 0) {
                value = "\"" + node.getNodeValue() + "\"";
            }
        }

        if (node.getNodeType() == Node.COMMENT_NODE) {

            value = "<!-- " + node.getNodeValue() + " -->";

        }

        DefaultTreeCellRenderer tcr = (DefaultTreeCellRenderer) super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
        tcr.setOpenIcon(null);
        tcr.setClosedIcon(null);

        return super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
    }
}//}}}

/*
 * $Id$
 *
 * $Log$
 * Revision 1.18  2009/05/09 14:15:14  pdoubleya
 * FindBugs: inner class could be static
 *
 * Revision 1.17  2007/05/20 23:25:33  peterbrant
 * Various code cleanups (e.g. remove unused imports)
 *
 * Patch from Sean Bright
 *
 * Revision 1.16  2005/10/27 00:09:08  tobega
 * Sorted out Context into RenderingContext and LayoutContext
 *
 * Revision 1.15  2005/06/22 23:48:46  tobega
 * Refactored the css package to allow a clean separation from the core.
 *
 * Revision 1.14  2005/06/16 07:24:53  tobega
 * Fixed background image bug.
 * Caching images in browser.
 * Enhanced LinkListener.
 * Some house-cleaning, playing with Idea's code inspection utility.
 *
 * Revision 1.13  2005/01/29 20:22:17  pdoubleya
 * Clean/reformat code. Removed commented blocks, checked copyright.
 *
 * Revision 1.12  2005/01/25 14:45:54  pdoubleya
 * Added support for IdentValue mapping on property declarations. On both CascadedStyle and PropertyDeclaration you can now request the value as an IdentValue, for object-object comparisons. Updated 99% of references that used to get the string value of PD to return the IdentValue instead; remaining cases are for pseudo-elements where the PD content needs to be manipulated as a String.
 *
 * Revision 1.11  2005/01/24 14:36:35  pdoubleya
 * Mass commit, includes: updated for changes to property declaration instantiation, and new use of DerivedValue. Removed any references to older XR... classes (e.g. XRProperty). Cleaned imports.
 *
 * Revision 1.10  2005/01/03 23:40:40  tobega
 * Cleaned out unnecessary styling/matching code. styling/matching is now called during boxing/rendering rather than as a separate stage.
 *
 * Revision 1.9  2004/12/29 10:39:35  tobega
 * Separated current state Context into LayoutContext and the rest into SharedContext.
 *
 * Revision 1.8  2004/12/12 04:18:58  tobega
 * Now the core compiles at least. Now we must make it work right. Table layout is one point that really needs to be looked over
 *
 * Revision 1.7  2004/12/11 18:18:12  tobega
 * Still broken, won't even compile at the moment. Working hard to fix it, though. Replace the StyleReference interface with our only concrete implementation, it was a bother changing in two places all the time.
 *
 * Revision 1.6  2004/11/07 01:17:56  tobega
 * DOMInspector now works with any StyleReference
 *
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

