// package PACKAGE;
package org.xhtmlrenderer.tool;

import org.xhtmlrenderer.test.*;
import java.io.*;

/** New class */
public class Boxer {
  
  private void run(String filen) {
    try {
        File file = new File(filen);
        if ( !file.exists() || file.isDirectory()) {
          System.out.println(filen + " not a file, or is a directory. Give me a single file name.");
          System.exit(-1);
        }
        
        System.out.println(DocumentDiffTest.xhtmlToDiff(filen, 1024, 768));
     } catch (Exception ex) {
       ex.printStackTrace();
     } 
  }
  
  public static void main(String args[]) {
    try {
      if ( args.length == 0 ) {
        System.out.println("Give a file name");
        System.exit(-1);
      }
      new Boxer().run(args[0]);

    } catch ( Exception ex ) {
      ex.printStackTrace();
    }
  }
} // end class
