package org.joshy.html.app.browser;

import com.pdoubleya.xhtmlrenderer.css.bridge.XRStyleReference;
import org.joshy.u;
import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import java.util.logging.*;
import org.joshy.html.*;
import org.joshy.html.swing.*;
import java.io.File;

public class BrowserMenuBar extends JMenuBar {
    public static Logger logger = Logger.getLogger("app.browser");
    BrowserStartup root;
    
    
    JMenu file;
    JMenu edit;
    JMenu view;
    JMenu go;
    JMenuItem view_source;
    JMenu debug;
    JMenu demos;
    
    public BrowserMenuBar(BrowserStartup root) {
        this.root = root;
    }
    
    public void init() {
        file = new JMenu("File");
        file.setMnemonic('F');
        
        debug = new JMenu("Debug");
        debug.setMnemonic('B');

        demos = new JMenu("Demos");
        demos.setMnemonic('D');
        
        edit = new JMenu("Edit");
        edit.setMnemonic('E');
        
        view = new JMenu("View");
        view.setMnemonic('V');
        
        view_source = new JMenuItem("Page Source");
        view_source.setEnabled(false);
        view.add(root.actions.stop);
        view.add(root.actions.refresh);
        view.add(root.actions.reload);
        
        go = new JMenu("Go");
        go.setMnemonic('G');
    }
    
    
    public void createLayout() {
        file.add(root.actions.open_file);
        file.add(root.actions.quit);
        add(file);
        
        edit.add(root.actions.cut);
        edit.add(root.actions.copy);
        edit.add(root.actions.paste);
        add(edit);
        
        view.add(view_source);
        add(view);
        
        go.add(root.actions.forward);
        go.add(root.actions.backward);

        add(go);
        
        // CLEAN
        demos.add(new LoadAction("Bad Page","demo:demos/allclasses-noframe.xhtml"));
        demos.add(new LoadAction("Borders","demo:demos/border.xhtml"));
        demos.add(new LoadAction("Backgrounds","demo:demos/background.xhtml"));
        demos.add(new LoadAction("Paragraph","demo:demos/paragraph.xhtml"));
        demos.add(new LoadAction("Line Breaking","demo:demos/breaking.xhtml"));
        demos.add(new LoadAction("Forms","demo:demos/forms.xhtml"));
        demos.add(new LoadAction("Headers","demo:demos/header.xhtml"));
        demos.add(new LoadAction("Nested Divs","demo:demos/nested.xhtml"));
        demos.add(new LoadAction("Selectors","demo:demos/selectors.xhtml"));
        demos.add(new LoadAction("Images","demo:demos/image.xhtml"));
        demos.add(new LoadAction("Lists","demo:demos/list.xhtml"));
        try {
            demos.add(new LoadAction("File Listing (Win)","file:///c:"));
            demos.add(new LoadAction("File Listing (Unix)","file:///"));
        } catch (Exception ex) {
            u.p(ex);
        }
            
        add(demos);
        
        JMenu debugShow = new JMenu("Show");
        debug.add(debugShow);
        debugShow.setMnemonic('S');
        
        debugShow.add(new JCheckBoxMenuItem(new BoxOutlinesAction()));
        debugShow.add(new JCheckBoxMenuItem(new LineBoxOutlinesAction()));
        debugShow.add(new JCheckBoxMenuItem(new InlineBoxesAction()));

        /* 
         debug.add(new AbstractAction("DOM tree inspector") {
            public void actionPerformed(ActionEvent evt) {
                JFrame frame = new JFrame();
                frame.getContentPane().add(new DOMInspector(root.panel.view.doc));
                frame.pack();
                frame.setSize(250,500);
                frame.show();
            }
        });
         **/
        debug.add(new ShowDOMInspectorAction());
        debug.add(new AbstractAction("Validation Console") {
            public void actionPerformed(ActionEvent evt) {
                if(root.validation_console == null) {
                    root.validation_console = new JFrame("Validation Console");
                    JFrame frame = root.validation_console;
                    JTextArea jta = new JTextArea();
                    root.error_handler.setTextArea(jta);
                    
                    frame.getContentPane().setLayout(new BorderLayout());
                    frame.getContentPane().add(new JScrollPane(jta),"Center");
                    JButton close = new JButton("Close");
                    frame.getContentPane().add(close,"South");
                    close.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent evt) {
                            root.validation_console.setVisible(false);
                        }
                    });
                    
                    
                    frame.pack();
                    frame.setSize(200,400);
                }
                root.validation_console.setVisible(true);
            }
        });
        
        
        add(debug);
    }
    
    class ShowDOMInspectorAction extends AbstractAction {
        private DOMInspector inspector;
        private JFrame inspectorFrame;
        ShowDOMInspectorAction() {
            super("DOM Tree Inspector");
            putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_D));
        }
        
        public void actionPerformed(ActionEvent evt) {
            if ( inspectorFrame == null ) {
                inspectorFrame = new JFrame("DOM Tree Inspector");
            }
            if ( inspector == null ) {
                // inspectorFrame = new JFrame("DOM Tree Inspector");
                
                // CLEAN: this is more complicated than it needs to be
                // DOM Tree Inspector needs to work with either CSSBank
                // or XRStyleReference--implementations are not perfectly
                // so we have different constructors
                if ( root.panel.view.c.css instanceof CSSBank )
                    inspector = new DOMInspector(root.panel.view.doc);     
                else 
                    inspector = new DOMInspector(root.panel.view.doc, root.panel.view.c, (XRStyleReference)root.panel.view.c.css);
                    
                inspectorFrame.getContentPane().add(inspector);
                    
                inspectorFrame.pack();
                inspectorFrame.setSize(500,600);
                inspectorFrame.show();
            } else {
                if ( root.panel.view.c.css instanceof CSSBank )
                    inspector.setForDocument(root.panel.view.doc);     
                else 
                    inspector.setForDocument(root.panel.view.doc, root.panel.view.c, (XRStyleReference)root.panel.view.c.css);
            }
            inspectorFrame.show();
        }
    }
    
    class BoxOutlinesAction extends AbstractAction {
        BoxOutlinesAction() {
            super("Show Box Outlines");
            putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_B));
        }
        public void actionPerformed(ActionEvent evt) {
            root.panel.view.c.debug_draw_boxes = !root.panel.view.c.debug_draw_boxes;
            root.panel.view.repaint();
        }
    }

    class LineBoxOutlinesAction extends AbstractAction {
        LineBoxOutlinesAction() {
            super("Show Line Box Outlines");
            putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_L));
        }
        public void actionPerformed(ActionEvent evt) {
            root.panel.view.c.debug_draw_line_boxes = !root.panel.view.c.debug_draw_line_boxes;
            root.panel.view.repaint();
        }
    }

    class InlineBoxesAction extends AbstractAction {
        InlineBoxesAction() {
            super("Show Inline Boxes");
            putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_I));
        }
        public void actionPerformed(ActionEvent evt) {
            root.panel.view.c.debug_draw_inline_boxes = !root.panel.view.c.debug_draw_inline_boxes;
            root.panel.view.repaint();
        }
    }
    
    public void createActions() {
        SelectionMouseListener ma = new SelectionMouseListener();
        root.panel.view.addMouseListener(ma);
        root.panel.view.addMouseMotionListener(ma);
        logger.info("added a mouse motion listener: " + ma);
    }
    
    class LoadAction extends AbstractAction {
        protected String url;
        public LoadAction(String name, String url) {
            super(name);
            this.url = url;
        }
        
        public void actionPerformed(ActionEvent evt) {
            try {
                root.panel.loadPage(url);
            } catch (Exception ex) { 
                u.p(ex); 
            }
        }
        
    }

}

class EmptyAction extends AbstractAction {
    public EmptyAction(String name, int accel) {
        this(name);
        putValue(Action.ACCELERATOR_KEY,
            KeyStroke.getKeyStroke(accel,
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
    }
    public EmptyAction(String name) {
        super(name);
    }
    public void actionPerformed(ActionEvent evt) {
    }
}

