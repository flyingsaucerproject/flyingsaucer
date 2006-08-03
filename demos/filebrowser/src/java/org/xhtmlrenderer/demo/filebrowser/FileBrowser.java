package org.xhtmlrenderer.demo.filebrowser;

import org.xhtmlrenderer.simple.XHTMLPanel;
import org.xhtmlrenderer.swing.HoverListener;
import org.xhtmlrenderer.util.Uu;
import org.xhtmlrenderer.util.XMLUtil;
import org.xhtmlrenderer.util.XRLog;
import org.xhtmlrenderer.util.GeneralUtil;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileSystemView;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class FileBrowser extends JPanel {

    public XHTMLPanel xhtml;
    public FileListModel filelistmodel;
    public JList filelist;

    public FileBrowser() {
        createComponents();
        createLayout();
        createEvents();
    }

    public void createComponents() {
        xhtml = new XHTMLPanel();
        XRLog.general("url = " + getClass().getResource("main.xhtml"));
        xhtml.setDocument(getClass().getResource("main.xhtml").toExternalForm());

        filelistmodel = new FileListModel();
        filelist = new JList(filelistmodel);
    }

    public void createLayout() {
        setLayout(new BorderLayout());
        xhtml.setPreferredSize(new Dimension(200, 400));
        xhtml.setMaximumSize(new Dimension(200, 400));
        xhtml.setSize(new Dimension(200, 400));
        JScrollPane sp = new JScrollPane(xhtml);
        add("West", sp);


        filelist.setCellRenderer(new FileRenderer());
        JScrollPane scroll = new JScrollPane(filelist);
        add("Center", scroll);
    }

    public void createEvents() {
        HoverListener hov = new HoverListener(xhtml);
        xhtml.addMouseListener(hov);
        xhtml.addMouseMotionListener(hov);
        filelistmodel.showDir(new File("."));
        filelist.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent evt) {
                displayFile((File) filelist.getSelectedValue());
            }
        });
    }


    public void displayFile(File file) {
        try {
            SimpleDateFormat df = new SimpleDateFormat("EEEE, MMMM dd, yyyy,hh:mm aa");
            p("displaying: " + file);
            InputStream input = this.getClass().getResourceAsStream("main.xhtml");
            String main = GeneralUtil.inputStreamToString(input);

            main = main.replaceAll("\\$filename", file.getName());

            String name = file.getName();
            int ext_index = name.lastIndexOf(".");
            String ext = "";
            if (ext_index > 0) {
                ext = name.substring(ext_index + 1, name.length());
            }

            ext = ext.toUpperCase();

            main = main.replaceAll("\\$extension", ext);
            main = main.replaceAll("\\$date", df.format(new Date(file.lastModified())));
            main = main.replaceAll("\\$size", "" + file.length() + " bytes");

            xhtml.setDocument(XMLUtil.documentFromString(main), new File(".").toURL().toString());
        } catch (Exception ex) {
            p("exception: " + ex);
            Uu.p(ex);
        }
    }


    public void start() {
        JFrame frame = new JFrame("File Browser Test");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(this);
        frame.pack();
        frame.show();
    }


    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                try {
                    new FileBrowser().start();
                } catch (Exception e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            }
        });
    }

    public static void p(String s) {
        System.out.println(s);
    }

}

class FileListModel extends DefaultListModel {
    public void showDir(File dir) {
        File[] files = dir.listFiles();
        clear();
        for (int i = 0; i < files.length; i++) {
            addElement(files[i]);
        }
    }
}

class FileRenderer extends DefaultListCellRenderer {
    FileSystemView view;

    public FileRenderer() {
        view = FileSystemView.getFileSystemView();
    }

    public Component getListCellRendererComponent(JList list, Object obj, int i,
                                                  boolean isSelected, boolean cellHasFocus) {
        Component comp = super.getListCellRendererComponent(list, obj, i, isSelected, cellHasFocus);
        if (comp instanceof JLabel) {
            JLabel lab = (JLabel) comp;
            if (obj instanceof File) {
                File file = (File) obj;
                lab.setText(file.getName());
                try {
                    Icon icon = view.getSystemIcon(file.getCanonicalFile());
                    if (icon != null) {
                        lab.setIcon(icon);
                    }
                } catch (IOException ex) {
                    p("ex: " + ex);
                }
            }
        }
        return comp;
    }

    public static void p(String s) {
        System.out.println(s);
    }
}
