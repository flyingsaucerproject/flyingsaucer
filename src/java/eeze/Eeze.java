/*
 * {{{ header & license
 * Copyright (c) 2004, 2005 Patrick Wright
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
package eeze;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.*;
import java.io.*;
import java.net.URL;
import java.util.*;
import javax.swing.*;
import javax.swing.SwingUtilities;
import org.xhtmlrenderer.simple.*;
import org.xhtmlrenderer.util.*;


/**
 * Eeze is a mini-application to test the Flying Saucer renderer across
 * a set of XML/CSS files.
 *
 * @author   Who?
 */
public class Eeze {
  /** Description of the Field */
  List testFiles;
  /** Description of the Field */
  JFrame eezeFrame;
  /** Description of the Field */
  File currentDisplayed;
  /** Description of the Field */
  Action growAction;
  /** Description of the Field */
  Action shrinkAction;
  /** Description of the Field */
  Action nextDemoAction;

  /** Description of the Field */
  private XHTMLPanel html;

  /** Constructor for the Eeze object */
  private Eeze() {
  }

  private String directory;
  /** Main processing method for the Eeze object */
  private void run(String args[]) {
    directory = args[0];
    testFiles = buildFileList();
    Iterator iter = testFiles.iterator();
    buildFrame();
  }

  /**
   * Description of the Method
   *
   * @return   Returns
   */
  private List buildFileList() {
    List fileList = null;
    try {
      File dir = new File( directory );
      if ( ! dir.isDirectory()) {
        showUsage("Please enter a directory name (not a file name).");
        System.exit(-1);
      }
      File list[] = dir.listFiles(HTML_FILE_FILTER);
      fileList = Arrays.asList( list );
    } catch ( Exception ex ) {
      ex.printStackTrace();
    }
    return fileList;
  }
  
  private static final FileFilter HTML_FILE_FILTER = new FileFilter() {
              public boolean accept( File f ) {
                return f.getName().endsWith( "html" ) || f.getName().endsWith( "htm" );
              }
            }; 

  /** Description of the Method */
  private void buildFrame() {
    try {
      eezeFrame = new JFrame( "FS Eeze" );
      final JFrame frame = eezeFrame;
      frame.setExtendedState( JFrame.NORMAL );
      frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );

      SwingUtilities.invokeLater(
            new Runnable() {
              public void run() {
                html = new XHTMLPanel();
                frame.getContentPane().add( new FSScrollPane( html ));
                frame.pack();
                frame.setSize( 1024, 768 );
                frame.setVisible( true );

                frame.addComponentListener(
                      new ComponentAdapter() {
                        public void componentResized( ComponentEvent e ) {
                          html.relayout();
                        }
                      } );

                nextDemoAction = new NextDemoAction();
                growAction = new GrowAction();
                shrinkAction = new ShrinkAction();

                increase_font = new FontSizeAction(FontSizeAction.INCREMENT, KeyStroke.getKeyStroke(KeyEvent.VK_I, InputEvent.CTRL_MASK));
                reset_font = new FontSizeAction(FontSizeAction.RESET, KeyStroke.getKeyStroke(KeyEvent.VK_0, InputEvent.CTRL_MASK));
                decrease_font = new FontSizeAction(FontSizeAction.DECREMENT, KeyStroke.getKeyStroke(KeyEvent.VK_D, InputEvent.CTRL_MASK));
                
                showHelp = new ShowHelpAction();

                frame.setJMenuBar( new JMenuBar() );
                JMenu doMenu = new JMenu( "Do" );
                doMenu.add( nextDemoAction );
                doMenu.add( growAction );
                doMenu.add( shrinkAction );
                doMenu.add( increase_font );
                doMenu.add( reset_font );
                doMenu.add( decrease_font );
                doMenu.add( showHelp );
                doMenu.setVisible( false );
                frame.getJMenuBar().add( doMenu );

                
                try {
                  //switchPage( (File)testFiles.get( 0 ) );
                  showHelpPage();
                } catch ( Exception ex ) {
                  ex.printStackTrace();
                }
              }
            } );

    } catch ( Exception ex ) {
      ex.printStackTrace();
    }
  }
                        
  Action increase_font, reset_font, decrease_font, showHelp;
  
  private void switchPage(File file) {
    XRLog.load( "Loading " + currentDisplayed );
    eezeFrame.setCursor( Cursor.getPredefinedCursor( Cursor.WAIT_CURSOR ) );
    try {
      html.setDocument(file.toURL());
      currentDisplayed = file;
      changeTitle(file.toURL().toString());
    } catch ( Exception ex ) {
      ex.printStackTrace(); 
    } finally {
      eezeFrame.setCursor( Cursor.getPredefinedCursor( Cursor.DEFAULT_CURSOR ) );
    }
  }
  
  private void showHelpPage() {
    eezeFrame.setCursor( Cursor.getPredefinedCursor( Cursor.WAIT_CURSOR ) );
    try {
      URL help = eezeHelp();
      html.setDocument(help.openStream(), help);
      changeTitle(html.getDocumentTitle());
    } catch ( Exception ex ) {
      ex.printStackTrace(); 
    } finally {
      eezeFrame.setCursor( Cursor.getPredefinedCursor( Cursor.DEFAULT_CURSOR ) );
    }
  }

  private void resizeFrame(float hdelta, float vdelta) {
    Dimension d = eezeFrame.getSize();
    eezeFrame.setSize( (int)( d.getWidth() * hdelta ), 
                         (int)( d.getHeight() * vdelta ) );
  }
  
  /**
   * Description of the Method
   *
   * @param newPage  PARAM
   */
  private void changeTitle( String newPage ) {
    eezeFrame.setTitle( "Eeze:  " + html.getDocumentTitle() + "  (" + newPage + ")" );
  }

  /**
   * Description of the Method
   *
   * @param args  PARAM
   */
  public static void main( String args[] ) {
    try {
      if ( args.length == 0 ) {
        showUsage("Eeze needs some information to work.");
        System.exit(-1);
      }
      new Eeze().run(args);
    } catch ( Exception ex ) {
      ex.printStackTrace();
    }
  }
  
  private URL eezeHelp() throws IOException {
    return this.getClass().getClassLoader().getResource("eeze/eeze_help.html");    
  }
  
  private static void showUsage(String error) {
    StringBuffer sb = new StringBuffer();
    sb
    .append("Oops! " + error + " \n")
    .append(" \n")
    .append("Eeze \n")
    .append("  A frame to walk through a set of XHTML/XML pages with Flying Saucer \n")
    .append(" \n")
    .append(" Usage: \n")
    .append("    java eeze.Eeze {directory}\n")
    .append(" \n")
    .append(" where {directory} is a directory containing XHTML/XML files.\n")
    .append(" \n")
    .append(" All files ending in .*htm* are loaded in a list, in alphabetical \n")
    .append(" order. The first is rendered. Use Alt-h to show keyboard navigation \n")
    .append(" shortcuts.\n")
    .append(" \n");
    System.out.println(sb.toString());
  }

  /**
   * Action to trigger frame to grow in size.
   *
   * @author   Who?
   */
  class GrowAction extends AbstractAction {
    /** Description of the Field */
    private float increment = 1.1F;

    /** Constructor for the GrowAction object */
    public GrowAction() {
      super( "Grow Page" );
      putValue( MNEMONIC_KEY, new Integer( KeyEvent.VK_G ) );
      putValue( ACCELERATOR_KEY, KeyStroke.getKeyStroke( KeyEvent.VK_L, KeyEvent.ALT_MASK ) );
    }

    /**
     * Invoked when an action occurs.
     *
     * @param e  PARAM
     */
    public void actionPerformed( ActionEvent e ) {
      resizeFrame(increment, increment);
    }
  }

  /**
   * Action to trigger frame to shrink in size.
   *
   * @author   Who?
   */
  class ShrinkAction extends AbstractAction {
    /** Description of the Field */
    private float increment = 1 / 1.1F;

    /** Constructor for the ShrinkAction object */
    public ShrinkAction() {
      super( "Shrink Page" );
      putValue( MNEMONIC_KEY, new Integer( KeyEvent.VK_S ) );
      putValue( ACCELERATOR_KEY, KeyStroke.getKeyStroke( KeyEvent.VK_S, KeyEvent.ALT_MASK ) );
    }

    /**
     * Invoked when an action occurs.
     *
     * @param e  PARAM
     */
    public void actionPerformed( ActionEvent e ) {
      resizeFrame(increment, increment);
    }
  }
  class ShowHelpAction extends AbstractAction {

    /** Constructor for the ShowHelpAction object */
    public ShowHelpAction() {
      super( "Show Help Page" );
      putValue( MNEMONIC_KEY, new Integer( KeyEvent.VK_H ) );
      putValue( ACCELERATOR_KEY, KeyStroke.getKeyStroke( KeyEvent.VK_H, KeyEvent.ALT_MASK ) );
    }

    /**
     * Invoked when an action occurs.
     *
     * @param e  PARAM
     */
    public void actionPerformed( ActionEvent e ) {
      showHelpPage();
    }
  }
  
  /**
   * Description of the Class
   *
   * @author   Who?
   */
  class NextDemoAction extends AbstractAction {

    /** Constructor for the NextDemoAction object */
    public NextDemoAction() {
      super( "Next Demo Page" );
      putValue( MNEMONIC_KEY, new Integer( KeyEvent.VK_N ) );
      putValue( ACCELERATOR_KEY, KeyStroke.getKeyStroke( KeyEvent.VK_N, KeyEvent.ALT_MASK ) );
    }

    /**
     * Invoked when an action occurs.
     *
     * @param e  PARAM
     */
    public void actionPerformed( ActionEvent e ) {
      File nextPage = null;
      for ( Iterator iter = testFiles.iterator(); iter.hasNext();  ) {
        File f = (File)iter.next();
        if ( f.equals( currentDisplayed ) ) {
          if ( iter.hasNext() ) {
            nextPage = (File)iter.next();
            break;
          }
        }
      }
      if ( nextPage == null ) {
        // go to first page
        Iterator iter = testFiles.iterator();
        nextPage = (File)iter.next();
      }

      try {
        switchPage(nextPage);
      } catch ( Exception ex ) {
        ex.printStackTrace();
      }
    }
  }
  
  class FontSizeAction extends AbstractAction {

    static final int DECREMENT = 0;
    static final int INCREMENT = 1;
    static final int RESET     = 2;
    private int whichDirection;

    public FontSizeAction(int which, KeyStroke ks) {
        super("FontSize");
        this.whichDirection = which;
        this.putValue(Action.ACCELERATOR_KEY, ks); 
    }

    public FontSizeAction(float scale, int which, KeyStroke ks) {
        this(which, ks);
        html.setFontScalingFactor(scale);
    }

    public void actionPerformed(ActionEvent evt) {
        switch ( whichDirection ) {
            case INCREMENT:
                html.incrementFontSize();
                break;
            case RESET:
                html.resetFontSize();
                break;
            case DECREMENT:
                html.decrementFontSize();
                break;
        }
    }
}  
}// end class

