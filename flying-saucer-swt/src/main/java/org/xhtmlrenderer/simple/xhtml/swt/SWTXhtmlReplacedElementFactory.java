package org.xhtmlrenderer.simple.xhtml.swt;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xhtmlrenderer.extend.ReplacedElement;
import org.xhtmlrenderer.extend.UserAgentCallback;
import org.xhtmlrenderer.layout.LayoutContext;
import org.xhtmlrenderer.render.BlockBox;
import org.xhtmlrenderer.simple.xhtml.FormControl;
import org.xhtmlrenderer.simple.xhtml.XhtmlForm;
import org.xhtmlrenderer.simple.xhtml.XhtmlNamespaceHandler;
import org.xhtmlrenderer.simple.xhtml.controls.ButtonControl;
import org.xhtmlrenderer.simple.xhtml.controls.CheckControl;
import org.xhtmlrenderer.simple.xhtml.controls.SelectControl;
import org.xhtmlrenderer.simple.xhtml.controls.TextControl;
import org.xhtmlrenderer.swt.BasicRenderer;
import org.xhtmlrenderer.swt.FormControlReplacementElement;
import org.xhtmlrenderer.swt.SWTReplacedElementFactory;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class SWTXhtmlReplacedElementFactory extends SWTReplacedElementFactory {
    private final BasicRenderer _parent;
    private final Map<Element, XhtmlForm> _forms = new HashMap<>();
    private final Map<Element, ReplacedElement> _controls = new HashMap<>();

    public SWTXhtmlReplacedElementFactory(BasicRenderer parent) {
        _parent = parent;
    }

    /**
     * @return the form corresponding to element {@code e} or {@code null} if none
     */
    @Nullable
    public XhtmlForm getForm(Element e) {
        return _forms.get(e);
    }

    public ReplacedElement createReplacedElement(LayoutContext c, BlockBox box,
            UserAgentCallback uac, int cssWidth, int cssHeight) {
        ReplacedElement re = super.createReplacedElement(c, box, uac, cssWidth, cssHeight);
        if (re == null
                && c.getNamespaceHandler() instanceof XhtmlNamespaceHandler
                && !c.isPrint()) {
            XhtmlNamespaceHandler nsh = (XhtmlNamespaceHandler) c
                .getNamespaceHandler();
            Element e = box.getElement();
            if (e == null) {
                return null;
            }

            // form controls
            re = _controls.get(e);
            if (re != null) {
                if (re instanceof FormControlReplacementElement) {
                    // update the size
                    ((FormControlReplacementElement) re).calculateSize(c, box
                        .getStyle(), cssWidth, cssHeight);
                }
                return re;
            }

            // not found, try to create one
            Element parentForm = getParentForm(e, c);
            // parentForm may be null, this is not a problem
            XhtmlForm form = _forms.get(parentForm);
            if (form == null) {
                form = nsh.createForm(parentForm);
                _forms.put(parentForm, form);
            }

            FormControl control = form.createControl(e);
            if (control == null) {
                // this is not a form control
                return null;
            }
            final SWTFormControl swtControl;
            if (control instanceof TextControl) {
                swtControl = new SWTTextControl(control, _parent, c, box.getStyle(), uac);
            } else if (control instanceof ButtonControl) {
                swtControl = new SWTButtonControl(control, _parent, c, box.getStyle(), uac);
            } else if (control instanceof CheckControl) {
                swtControl = new SWTCheckControl(control, _parent, c, box.getStyle(), uac);
            } else if (control instanceof SelectControl) {
                swtControl = new SWTSelectControl(control, _parent, c, box.getStyle(), uac);
            } else {
                // no replacement found
                return null;
            }
            swtControl.getSWTControl().setVisible(false);

            FormControlReplacementElement element = new FormControlReplacementElement(
                swtControl);
            element.calculateSize(c, box.getStyle(), cssWidth, cssHeight);
            re = element;
            _controls.put(e, re);
        }
        return re;
    }

    public void remove(Element e) {
        super.remove(e);
        ReplacedElement re = _controls.get(e);
        if (re instanceof FormControlReplacementElement) {
            SWTFormControl control = ((FormControlReplacementElement) re)
                .getControl();
            if (control != null) {
                control.dispose();
            }
        }
        _controls.remove(e);
    }

    public void reset() {
        super.reset();
        _forms.clear();
        for (ReplacedElement re : _controls.values()) {
            if (re instanceof FormControlReplacementElement) {
                SWTFormControl control = ((FormControlReplacementElement) re)
                        .getControl();
                if (control != null) {
                    control.dispose();
                }
            }
        }
        _controls.clear();
    }

    protected Element getParentForm(Element e, LayoutContext context) {
        Node node = e;
        XhtmlNamespaceHandler nsh = (XhtmlNamespaceHandler) context
            .getNamespaceHandler();

        do {
            node = node.getParentNode();
        } while (node.getNodeType() == Node.ELEMENT_NODE
                && !nsh.isFormElement((Element) node));

        if (node.getNodeType() != Node.ELEMENT_NODE) {
            return null;
        }

        return (Element) node;
    }

}
