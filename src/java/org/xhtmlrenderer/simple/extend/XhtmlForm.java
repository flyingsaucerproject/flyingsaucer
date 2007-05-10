/*
 * Copyright (c) 2004, 2005 Torbj�rn Gannholm
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 */
package org.xhtmlrenderer.simple.extend;

import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.DefaultListSelectionModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xhtmlrenderer.extend.FSImage;
import org.xhtmlrenderer.extend.UserAgentCallback;
import org.xhtmlrenderer.swing.AWTFSImage;
import org.xhtmlrenderer.util.XRLog;


/**
 * Represents a form object
 *
 * @author Torbj�rn Gannholm
 */
public class XhtmlForm {
    /**
     * Description of the Field
     */
    protected LinkedHashMap components = new LinkedHashMap();
    /**
     * Description of the Field
     */
    protected UserAgentCallback uac;
    /**
     * Description of the Field
     */
    protected Element formElement;

    private Map buttonGroups;

    /**
     * Constructor for the XhtmlForm object
     *
     * @param uac PARAM
     * @param e   PARAM
     */
    public XhtmlForm(UserAgentCallback uac, Element e) {
        this.uac = uac;
        formElement = e;
    }

    /**
     * Adds a feature to the Component attribute of the XhtmlForm object
     *
     * @param e The feature to be added to the Component attribute
     * @return Returns
     */
    public JComponent addComponent(Element e) {
        JComponent cc = (JComponent) components.get(e);
        if (cc != null) {
            return cc;
        }
        if (e.getNodeName().equals("input")) {
            String type = e.getAttribute("type");
            if (type == null || type.equals("")) {
                type = "button";
            }
            String label = e.getAttribute("value");
            if (label == null || label.equals("")) {
                if (type.equals("reset")) {
                    label = "Reset";//TODO: this text should be from properties
                }
                if (type.equals("submit")) {
                    label = "Submit";
                }
            }
            if (type.equals("button") || type.equals("reset") || type.equals("submit")) {
                JButton button = new JButton();
                button.setText(label);
                cc = button;
            } else if (type.equals("image")) {
                JButton jb = new JButton();
                Image im = null;
                if (e.hasAttribute("src")) {
                    System.out.println("pulling from here: " + e.getAttribute("src"));
                    FSImage fsImage = uac.getImageResource(e.getAttribute("src")).getImage();
                    if (fsImage != null) {
                        im = ((AWTFSImage) fsImage).getImage();
                    }
                }
                if (im == null) {
                    jb = new JButton("Image unreachable. " + e.getAttribute("alt"));
                } else {
                    ImageIcon ii = new ImageIcon(im, e.getAttribute("alt"));
                    jb = new JButton(ii);
                }
                jb.setBorder(BorderFactory.createEmptyBorder());
                jb.setContentAreaFilled(false);
                cc = jb;
            } else if (type.equals("checkbox")) {
                JCheckBox checkbox = new JCheckBox();
                checkbox.setText("");
                checkbox.setOpaque(false);
                if (e.hasAttribute("checked") &&
                        e.getAttribute("checked").equals("checked")) {
                    checkbox.setSelected(true);
                }
                cc = checkbox;
            } else if (type.equals("password")) {
                JPasswordField pw = new JPasswordField();
                if (e.hasAttribute("size")) {
                    pw.setColumns(Integer.parseInt(e.getAttribute("size")));
                } else {
                    pw.setColumns(15);
                }
                if (e.hasAttribute("maxlength")) {
                    int maxlength = Integer.parseInt(e.getAttribute("maxlength"));
                    pw.setDocument(new SizeLimitedDocument(maxlength));
                }
                cc = pw;
            } else if (type.equals("radio")) {
                JRadioButton radio = new JRadioButton();
                radio.setText("");
                radio.setOpaque(false);
                if (e.hasAttribute("checked") &&
                        e.getAttribute("checked").equals("checked")) {
                    radio.setSelected(true);
                }
                if (buttonGroups == null) {
                    buttonGroups = new HashMap();
                }
                String name = e.getAttribute("name");
                ButtonGroup group = (ButtonGroup) buttonGroups.get(name);
                if (group == null) {
                    group = new ButtonGroup();
                    buttonGroups.put(name, group);
                }
                group.add(radio);
                cc = radio;
            } else if (type.equals("text")) {
                JTextField text = new JTextField();
                if (e.hasAttribute("value")) {
                    text.setText(e.getAttribute("value"));
                }
                if (e.hasAttribute("size")) {
                    text.setColumns(Integer.parseInt(e.getAttribute("size")));
                } else {
                    text.setColumns(15);
                }
                if (e.hasAttribute("maxlength")) {
                    int maxlength = Integer.parseInt(e.getAttribute("maxlength"));
                    text.setDocument(new SizeLimitedDocument(maxlength));
                }
                if (e.hasAttribute("readonly") &&
                        e.getAttribute("readonly").equals("readonly")) {
                    text.setEditable(false);
                }
                cc = text;
            } else if (type.equals("text")) {
                JTextField text = new JTextField();
                if (e.hasAttribute("value")) {
                    text.setText(e.getAttribute("value"));
                }
                cc = null;//don't return it
                components.put(e, text);
            } else if (type.equals("hidden")) {
                // TODO: hidden form fields.
            } else {
                XRLog.layout("unknown input type " + type);
            }
            if (type.equals("submit") || type.equals("image")) {
                ((JButton) cc).addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent evt) {
                        submit((JComponent) evt.getSource());
                    }
                });

            }
        } else if (e.getNodeName().equals("textarea")) {
            int rows = 4;
            int cols = 10;
            if (e.hasAttribute("rows")) {
                rows = Integer.parseInt(e.getAttribute("rows"));
            }
            if (e.hasAttribute("cols")) {
                cols = Integer.parseInt(e.getAttribute("cols"));
            }

            JTextArea ta = new JTextArea(rows, cols);
            JScrollPane sp = new JScrollPane(ta);
            sp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
            sp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
            if (e.getFirstChild() != null) {
                //Uu.p("setting text to: " + elem.getFirstChild().getNodeValue());
                ta.setText(e.getFirstChild().getNodeValue());
            }
            cc = sp;

        } else if (e.getNodeName().equals("select")) {
            // either a select list or a drop down/combobox
            if (e.hasAttribute("multiple") && e.getAttribute("multiple").equals("true")) {
                // select list.
                //    we'll pass the items in to the constructor, so put the list together first
                DefaultListModel listModel = new DefaultListModel();

                //    and capture selection at the same time
                ListSelectionModel selModel = new DefaultListSelectionModel();

                NodeList options = e.getElementsByTagName("option");
                for (int i = 0; i < options.getLength(); i++) {
                    Element option = (Element) options.item(i);
                    
                    String optionText = collectText(option);
                    String optionValue = optionText;
                    
                    if (option.hasAttribute("value")) {
                        optionValue = option.getAttribute("value");
                    }

                    listModel.addElement(new NameValuePair(optionText, optionValue));

                    if (option.hasAttribute("selected") && option.getAttribute("selected").equals("selected")) {
                        selModel.addSelectionInterval(i, i);
                    }
                }

                JList list = new JList(listModel);
                list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
                list.setSelectionModel(selModel);

                // the component is actually a scrollpane
                JScrollPane sp = new JScrollPane(list);
                cc = sp;
            } else {
                // drop down list
                JComboBox select = new JComboBox();
                select.setEditable(false); //cannot edit it in HTML
                NodeList options = e.getElementsByTagName("option");
                int selected = -1;
                for (int i = 0; i < options.getLength(); i++) {
                    Element option = (Element) options.item(i);
                    
                    String optionText = collectText(option);
                    String optionValue = optionText;
                    
                    if (option.hasAttribute("value")) {
                        optionValue = option.getAttribute("value");
                    }

                    select.addItem(new NameValuePair(optionText, optionValue));

                    if (option.hasAttribute("selected") && option.getAttribute("selected").equals("selected")) {
                        selected = i;
                    }
                }

                if (selected != -1) {
                    select.setSelectedIndex(selected);
                }
                cc = select;
            }
        }
        if (cc != null) {//was a form object
            cc.setSize(cc.getPreferredSize());
            if (e.hasAttribute("disabled") &&
                    e.getAttribute("disabled").equals("disabled")) {
                cc.setEnabled(false);
            }
            components.put(e, cc);
        }
        return cc;
    }
    
    private String collectText(Element e) {
        StringBuffer result = new StringBuffer();
        Node n = e.getFirstChild();
        if (n != null) {
            do {
                if (n.getNodeType() == Node.TEXT_NODE) {
                    Text text = (Text)n;
                    result.append(text.getData());
                }
            } while ( (n = n.getNextSibling()) != null);
        }
        return result.toString().trim();
    }

    /**
     * Description of the Method
     *
     * @param source PARAM
     */
    public void submit(JComponent source) {
        StringBuffer data = new StringBuffer();
        Iterator fields = components.entrySet().iterator();
        while (fields.hasNext()) {
            Map.Entry field = (Map.Entry) fields.next();
            Element e = (Element) field.getKey();
            JComponent cc = (JComponent) field.getValue();
            if (e.hasAttribute("disabled") &&
                    e.getAttribute("disabled").equals("disabled")) {
                continue;
            }
            if (e.getNodeName().equals("input")) {
                String type = e.getAttribute("type");
                if (type == null || type.equals("")) {
                    type = "button";
                }
                if (type.equals("submit") && cc == source) {
                    String value = e.getAttribute("value");
                    if (value.equals("")) {
                        value = "submit";
                    }
                    data.append('&');
                    data.append(URLUTF8Encoder.encode(e.getAttribute("name")));
                    data.append("=");
                    data.append(URLUTF8Encoder.encode(value));
                } else if (type.equals("image")) {
                    data.append('&');
                    data.append(URLUTF8Encoder.encode(e.getAttribute("name")));
                    data.append("=");
                    data.append(URLUTF8Encoder.encode(e.getAttribute("value")));
                } else if (type.equals("checkbox")) {
                    JCheckBox checkbox = (JCheckBox) cc;
                    if (!checkbox.isSelected()) {
                        continue;
                    }
                    data.append('&');
                    data.append(URLUTF8Encoder.encode(e.getAttribute("name")));
                    data.append("=");
                    data.append(URLUTF8Encoder.encode(e.getAttribute("value")));
                } else if (type.equals("password")) {
                    JPasswordField pw = new JPasswordField();
                    data.append('&');
                    data.append(URLUTF8Encoder.encode(e.getAttribute("name")));
                    data.append("=");
                    data.append(URLUTF8Encoder.encode(pw.getPassword()));
                } else if (type.equals("radio")) {
                    JRadioButton radio = (JRadioButton) cc;
                    if (!radio.isSelected()) {
                        continue;
                    }
                    data.append('&');
                    data.append(URLUTF8Encoder.encode(e.getAttribute("name")));
                    data.append("=");
                    data.append(URLUTF8Encoder.encode(e.getAttribute("value")));
                } else if (type.equals("text") || type.equals("hidden")) {
                    JTextField text = (JTextField) cc;
                    data.append('&');
                    data.append(URLUTF8Encoder.encode(e.getAttribute("name")));
                    data.append("=");
                    data.append(URLUTF8Encoder.encode(text.getText()));
                }
            } else if (e.getNodeName().equals("textarea")) {
                JTextArea ta = (JTextArea) cc;
                data.append('&');
                data.append(URLUTF8Encoder.encode(e.getAttribute("name")));
                data.append("=");
                data.append(URLUTF8Encoder.encode(ta.getText()));//TODO:check if we have to make linefeeds into CR-LF
            } else if (e.getNodeName().equals("select")) {
                if (e.hasAttribute("multiple") && e.getAttribute("multiple").equals("true")) {
                    JScrollPane scrollpane = (JScrollPane) cc;

                    // This is ugly, but I plan to re-write most of this class anyway...
                    JList list = (JList) scrollpane.getViewport().getView();

                    Object [] selectedItems = list.getSelectedValues();
                    
                    for (int i = 0; i < selectedItems.length; i++) {
                        NameValuePair selectedItem = (NameValuePair) selectedItems[i];

                        data.append('&');
                        data.append(URLUTF8Encoder.encode(e.getAttribute("name")));
                        data.append("=");
                        data.append(URLUTF8Encoder.encode(selectedItem.getValue()));
                    }
                } else {
                    JComboBox select = (JComboBox) cc;

                    data.append('&');
                    data.append(URLUTF8Encoder.encode(e.getAttribute("name")));
                    data.append("=");

                    if (select.getSelectedItem() != null) {
                        NameValuePair selectedItem = (NameValuePair) select.getSelectedItem();

                        data.append(URLUTF8Encoder.encode(selectedItem.getValue()));
                    }
                }
            }
        }
        data.deleteCharAt(0);//remove the first &
        // TODO: Make this all work but comment out for now because its making eclipse angry
        /*
        String action = formElement.getAttribute("action");
        String method = formElement.getAttribute("method");
        if (method.equals("")) {
            method = "get";
        }
        String formData = data.toString();
        */
        //TODO: make a real submission via uac
    }
}

/**
 * When applied to a Swing component, limits the total number of
 * characters that can be entered.
 */
class SizeLimitedDocument extends PlainDocument
{
    private static final long serialVersionUID = 1L;

    private int maximumLength;
    
    public SizeLimitedDocument(int maximumLength) {
        this.maximumLength = maximumLength;
    }
    
    public int getMaximumLength() {
        return this.maximumLength;
    }

    public void insertString(int offset, String str, AttributeSet attr)
        throws BadLocationException {
        if (str == null) {
            return;
        }
        if ((getLength() + str.length()) <= this.maximumLength) {
            super.insertString(offset, str, attr);
        }
    }
}
/**
 * Provides a simple container for name/value data, such as that used
 * by the &lt;option&gt; elements in a &lt;select&gt; list.
 */
class NameValuePair {
    private String name;
    private String value;
    
    public NameValuePair(String name, String value) {
        this.name = name;
        this.value = value;
    }
    
    public String getName() {
        return this.name;
    }
    
    public String getValue() {
        return this.value;
    }
    
    public String toString() {
        return this.getName();
    }
}

