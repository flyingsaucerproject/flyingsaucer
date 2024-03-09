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
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class DOMInspector extends JPanel {
    private static final long serialVersionUID = 1L;

    private StyleReference styleReference;
    private ElementPropertiesPanel elementPropPanel;
    private DOMSelectionListener nodeSelectionListener;
    private JSplitPane splitPane;
    private Document doc;
    private final JTree tree;

    public DOMInspector(Document doc) {
        this(doc, null, null);
    }

    public DOMInspector(Document doc, SharedContext context, StyleReference sr) {
        setLayout(new java.awt.BorderLayout());

        //JPanel treePanel = new JPanel();
        tree = new JTree();
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        JScrollPane scroll = new JScrollPane(tree);

        splitPane = null;
        if (sr == null) {
            add(scroll, "Center");
        } else {
            splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
            splitPane.setOneTouchExpandable(true);
            splitPane.setDividerLocation(150);

            add(splitPane, "Center");
            splitPane.setLeftComponent(scroll);
        }

        JButton close = new JButton("close");
        add(close, "South");
        setPreferredSize(new Dimension(300, 300));

        setForDocument(doc, context, sr);

        close.addActionListener(evt -> getFrame(this).setVisible(false));
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawLine(0, 0, 100, 100);

    }

    /**
     * Sets the forDocument attribute of the DOMInspector object
     */
    public void setForDocument(Document doc) {
        setForDocument(doc, null, null);
    }

    /**
     * Sets the forDocument attribute of the DOMInspector object
     */
    public void setForDocument(Document doc, SharedContext context, StyleReference sr) {
        this.doc = doc;
        this.styleReference = sr;
        this.initForCurrentDocument();
    }

    /**
     * Gets the frame attribute of the DOMInspector object
     */
    public JFrame getFrame(Component comp) {
        if (comp instanceof JFrame) {
            return (JFrame) comp;
        }
        return getFrame(comp.getParent());
    }

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

final class ElementPropertiesPanel extends JPanel {
    private static final long serialVersionUID = 1L;

    //private SharedContext _context;
    private final StyleReference _sr;
    private final JTable _properties;
    private final TableModel _defaultTableModel;

    ElementPropertiesPanel(StyleReference sr) {
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

    private TableModel tableModel(Node node) {
        if (node.getNodeType() != Node.ELEMENT_NODE) {
            Toolkit.getDefaultToolkit().beep();
            return _defaultTableModel;
        }
        Map<String, CSSPrimitiveValue> props = _sr.getCascadedPropertiesMap((Element) node);
        return new PropertiesTableModel(props);
    }

    static final class PropertiesJTable extends JTable {
        private static final long serialVersionUID = 1L;

        private final Font propLabelFont;
        private final Font defaultFont;

        PropertiesJTable() {
            this.setColumnSelectionAllowed(false);
            this.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            propLabelFont = new Font("Courier New", Font.BOLD, 12);
            defaultFont = new Font("Default", Font.PLAIN, 12);
        }

        /**
         * Gets the cellRenderer attribute of the PropertiesJTable object
         */
        @Override
        public TableCellRenderer getCellRenderer(int row, int col) {
            JLabel label = (JLabel) super.getCellRenderer(row, col);
            label.setBackground(Color.white);
            label.setFont(defaultFont);
            if (col == 0) {
                // BUG: not working?
                label.setFont(propLabelFont);
            } else if (col == 2) {
                PropertiesTableModel pmodel = (PropertiesTableModel) getModel();
                Map.Entry<String, CSSPrimitiveValue> me = (Map.Entry<String, CSSPrimitiveValue>) pmodel._properties.entrySet().toArray()[row];
                CSSPrimitiveValue cpv = me.getValue();
                if (cpv.getCssText().startsWith("rgb")) {
                    label.setBackground(org.xhtmlrenderer.css.util.ConversionUtil.rgbToColor(cpv.getRGBColorValue()));
                }
            }
            return (TableCellRenderer) label;
        }
    }

    static class PropertiesTableModel extends AbstractTableModel {
        private static final long serialVersionUID = 1L;

        //String _colNames[] = {"Property Name", "Text", "Value", "Important-Inherit"};
        private final String[] _colNames = {"Property Name", "Text", "Value"};

        private final Map<String, CSSPrimitiveValue> _properties;

        PropertiesTableModel(Map<String, CSSPrimitiveValue> cssProperties) {
            _properties = cssProperties;
        }

        /**
         * Gets the columnName attribute of the PropertiesTableModel object
         */
        @Override
        public String getColumnName(int col) {
            return _colNames[col];
        }

        /**
         * Gets the columnCount attribute of the PropertiesTableModel object
         */
        @Override
        public int getColumnCount() {
            return _colNames.length;
        }

        /**
         * Gets the rowCount attribute of the PropertiesTableModel object
         */
        @Override
        public int getRowCount() {
            return _properties.size();
        }

        /**
         * Gets the valueAt attribute of the PropertiesTableModel object
         */
        @Override
        public Object getValueAt(int row, int col) {
            Map.Entry<String, CSSPrimitiveValue> me = (Map.Entry<String, CSSPrimitiveValue>) _properties.entrySet().toArray()[row];
            CSSPrimitiveValue cpv = me.getValue();

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
                        val = cpv.getFloatValue(cpv.getPrimitiveType());
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
         */
        @Override
        public boolean isCellEditable(int row, int col) {
            return false;
        }
    }
}


class DOMSelectionListener implements TreeSelectionListener {

    private final JTree _tree;
    //private StyleReference _sr;
    private final ElementPropertiesPanel _elemPropPanel;

    DOMSelectionListener(JTree tree, ElementPropertiesPanel panel) {
        _tree = tree;
        //_sr = sr;
        _elemPropPanel = panel;
    }

    @Override
    public void valueChanged(TreeSelectionEvent e) {
        Node node = (Node) _tree.getLastSelectedPathComponent();

        if (node == null) {
            return;
        }

        _elemPropPanel.setForElement(node);
    }
}

class DOMTreeModel implements TreeModel {

    /**
     * Our root for display
     */
    private Node root;

    private final Map<Object, List<Node>> displayableNodes = new HashMap<>();
    private final List<TreeModelListener> listeners = new ArrayList<>();

    DOMTreeModel(Document doc) {
        Node tempRoot = doc.getDocumentElement();
        NodeList nl = tempRoot.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            if (nl.item(i).getNodeName().toLowerCase().equals("body")) {
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
    @Override
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
    @Override
    public void removeTreeModelListener(TreeModelListener l) {
        this.listeners.remove(l);

    }


    //Messaged when the user has altered the value for the item identified by path to newValue.

    @Override
    public void valueForPathChanged(TreePath path, Object newValue) {
        // no-op
    }

    //Returns the child of parent at index in the parent's child array.

    /**
     * Gets the child attribute of the DOMTreeModel object
     */
    @Override
    public Object getChild(Object parent, int index) {

        Node node = (Node) parent;

        List<Node> children = this.displayableNodes.get(parent);
        if (children == null) {
            children = addDisplayable(node);
        }

        return children.get(index);
    }


    //Returns the number of children of parent.

    /**
     * Gets the childCount attribute of the DOMTreeModel object
     */
    @Override
    public int getChildCount(Object parent) {

        Node node = (Node) parent;
        List<Node> children = this.displayableNodes.get(parent);
        if (children == null) {
            children = addDisplayable(node);
        }

        return children.size();
    }


    //Returns the index of child in parent.

    /**
     * Gets the indexOfChild attribute of the DOMTreeModel object
     */
    @Override
    public int getIndexOfChild(Object parent, Object child) {

        Node node = (Node) parent;
        List<Node> children = this.displayableNodes.get(parent);
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
    @Override
    public Object getRoot() {
        return root;
    }


    //Returns true if node is a leaf.

    /**
     * Gets the leaf attribute of the DOMTreeModel object
     */
    @Override
    public boolean isLeaf(Object nd) {
        Node node = (Node) nd;
        return !node.hasChildNodes();
    }

    // only adds displayable nodes--not stupid DOM text filler nodes
    /**
     * Adds a feature to the Displayable attribute of the DOMTreeModel object
     *
     * @param parent The feature to be added to the Displayable attribute
     */
    private List<Node> addDisplayable(Node parent) {
        List<Node> children = displayableNodes.get(parent);
        if (children == null) {
            children = new ArrayList<>();
            displayableNodes.put(parent, children);
            NodeList nl = parent.getChildNodes();
            for (int i = 0, len = nl.getLength(); i < len; i++) {
                Node child = nl.item(i);
                if (child.getNodeType() == Node.ELEMENT_NODE ||
                        child.getNodeType() == Node.COMMENT_NODE ||
                        (child.getNodeType() == Node.TEXT_NODE && !child.getNodeValue().trim().isEmpty())) {
                    children.add(child);
                }
            }
            return children;
        } else {
            return new ArrayList<>();
        }
    }

}

class DOMTreeCellRenderer extends DefaultTreeCellRenderer {
    /**
     * Gets the treeCellRendererComponent attribute of the DOMTreeCellRenderer
     * object
     */
    @Override
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

            if (!node.getNodeValue().trim().isEmpty()) {
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
}
