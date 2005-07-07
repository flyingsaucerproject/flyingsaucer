/*
 * Copyright (c) 2004, 2005 Torbjörn Gannholm
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
 *
 */
package org.xhtmlrenderer.simple.extend;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xhtmlrenderer.extend.UserAgentCallback;
import org.xhtmlrenderer.layout.Context;
import org.xhtmlrenderer.util.XRLog;

import javax.swing.*;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;


/**
 * Represents a form object
 *
 * @author Torbjörn Gannholm
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
     * @param c PARAM
     * @param e PARAM
     */
    public XhtmlForm(Context c, Element e) {
        uac = c.getRenderingContext().getUac();
        formElement = e;
    }

    /**
     * Adds a feature to the Component attribute of the XhtmlForm object
     *
     * @param c The feature to be added to the Component attribute
     * @param e The feature to be added to the Component attribute
     * @return Returns
     */
    public JComponent addComponent(Context c, Element e) {
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
                    im = c.getCtx().getUac().getImageResource(e.getAttribute("src")).getImage();
                }
                if (im == null) {
                    jb = new JButton("Image unreachable. " + e.getAttribute("alt"));
                } else {
                    ImageIcon ii = new ImageIcon(im, e.getAttribute("alt"));
                    jb = new JButton(ii);
                }
                jb.setBorder(BorderFactory.createEmptyBorder());
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
                    final int maxlength = Integer.parseInt(e.getAttribute("maxlength"));
                    pw.setDocument(new PlainDocument() {
                        public void insertString(int offset, String str, AttributeSet attr)
                                throws BadLocationException {
                            if (str == null) {
                                return;
                            }
                            if ((getLength() + str.length()) <= maxlength) {
                                super.insertString(offset, str, attr);
                            }
                        }
                    });
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
                    final int maxlength = Integer.parseInt(e.getAttribute("maxlength"));
                    text.setDocument(new PlainDocument() {
                        public void insertString(int offset, String str, AttributeSet attr)
                                throws BadLocationException {
                            if (str == null) {
                                return;
                            }
                            if ((getLength() + str.length()) <= maxlength) {
                                super.insertString(offset, str, attr);
                            }
                        }
                    });
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
            JComboBox select = new JComboBox();
            select.setEditable(false);//cannot edit it in HTML
            NodeList options = e.getElementsByTagName("option");
            int selected = -1;
            for (int i = 0; i < options.getLength(); i++) {
                Element value = (Element) options.item(i);
                String svalue = value.getFirstChild().getNodeValue();
                select.addItem(svalue);
                if (value.hasAttribute("selected") && value.getAttribute("selected").equals("selected")) {
                    selected = i;
                }
            }

            if (selected != -1) {
                select.setSelectedIndex(selected);
            }
            cc = select;
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
                JComboBox select = new JComboBox();
                data.append('&');
                data.append(URLUTF8Encoder.encode(e.getAttribute("name")));
                data.append("=");
                data.append(URLUTF8Encoder.encode(select.getSelectedItem().toString()));
            }
        }
        data.deleteCharAt(0);//remove the first &
        String action = formElement.getAttribute("action");
        String method = formElement.getAttribute("method");
        if (method.equals("")) {
            method = "get";
        }
        String formData = data.toString();
        //TODO: make a real submission via uac
        System.out.println("Submitting form");
        System.out.println("action: " + action);
        System.out.println("method: " + method);
        System.out.println("form data: " + formData);
    }
}

