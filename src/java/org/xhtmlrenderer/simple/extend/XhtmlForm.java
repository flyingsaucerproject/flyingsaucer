package org.xhtmlrenderer.simple.extend;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xhtmlrenderer.layout.Context;
import org.xhtmlrenderer.util.ImageUtil;
import org.xhtmlrenderer.util.XRLog;

import javax.swing.*;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;
import java.awt.Image;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedHashMap;

/**
 * Represents a form object
 */
class XhtmlForm {
    protected LinkedHashMap components = new LinkedHashMap();

    public JComponent addComponent(Context c, Element e) {
        JComponent cc = (JComponent) components.get(e);
        if (cc != null) return cc;
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
                    try {
                        im = ImageUtil.loadImage(c, new URL(c.getRenderingContext().getBaseURL(), e.getAttribute("src")).toString());
                    } catch (MalformedURLException ex) {

                    }
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
            }//hidden?
            //HACK:
            if (cc == null) XRLog.layout("unknown input type " + type);
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
}
