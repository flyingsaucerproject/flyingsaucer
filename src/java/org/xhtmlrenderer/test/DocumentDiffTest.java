package org.xhtmlrenderer.test;

import org.joshy.u;
import org.joshy.x;
import java.io.*;
import org.xhtmlrenderer.render.*;
import org.xhtmlrenderer.swing.*;
import org.w3c.dom.*;
import java.awt.*;
import java.awt.image.*;

public class DocumentDiffTest {

    public static void main(String[] args) throws Exception {
        String testfile = "tests/diff/background/01.xhtml";
        String difffile = "tests/diff/background/01.diff";
        
        DocumentDiffTest ddt = new DocumentDiffTest();
        ddt.runTests(new File("tests/diff"),500,500);
    }
    
    public void runTests(File dir, int width, int height) throws Exception {
        File[] files = dir.listFiles();
        for(int i=0; i<files.length; i++) {
            if(files[i].isDirectory()) {
                runTests(files[i],width,height);
                continue;
            }
            if(files[i].getName().endsWith(".xhtml")) {
                String testfile = files[i].getAbsolutePath();
                String difffile = testfile.substring(0,testfile.length()-6) + ".diff";
                u.p("test file = " + testfile);
                u.p("diff file = " + difffile);
                boolean is_correct = compareTestFile(testfile, difffile, 500, 500);
                u.p("is correct = " + is_correct);
            }
        }
        
    }
    public void generateDiffs(File dir, int width, int height) throws Exception {
        File[] files = dir.listFiles();
        for(int i=0; i<files.length; i++) {
            if(files[i].isDirectory()) {
                generateDiffs(files[i],width,height);
                continue;
            }
            if(files[i].getName().endsWith(".xhtml")) {
                String testfile = files[i].getAbsolutePath();
                String difffile = testfile.substring(0,testfile.length()-6) + ".diff";
                //u.p("test file = " + testfile);
                generateTestFile(testfile, difffile, 500, 500);
                u.p("generated = " + difffile);
            }
        }
        
    }
    
    public void generateTestFile(String test, String diff, int width, int height) throws Exception {
        String out = xhtmlToDiff(test,width,height);
        //u.p("diff = \n" + out);
        u.string_to_file(out,new File(diff));
    }
    public String xhtmlToDiff(String xhtml, int width, int height) throws Exception {
        Document doc = x.loadDocument(xhtml);
        HTMLPanel panel = new HTMLPanel();
        panel.setDocument(doc);
        panel.setSize(width,height);
        BufferedImage buff = new BufferedImage(width,height, BufferedImage.TYPE_4BYTE_ABGR);
        Graphics g = buff.getGraphics();
        panel.paintComponent(g);
        StringBuffer sb = new StringBuffer();
        getDiff(sb,panel.getRootBox(),"");
        return sb.toString();
    }
    
    public boolean compareTestFile(String test, String diff, int width, int height) throws Exception {
        String tin = xhtmlToDiff(test, width, height);
        String din = u.file_to_string(diff);
        //u.p("tin = ");
        //u.p(tin);
        //u.p("din = ");
        //u.p(din);
        if(tin.equals(din)) {
            return true;
        }
        return false;
    }
    
    public void getDiff(StringBuffer sb, Box box, String tab) {
        sb.append(tab+box.getTestString()+"\n");
        for(int i=0; i<box.getChildCount(); i++) {
            getDiff(sb,(Box)box.getChild(i),tab+" ");
        }
        
    }
    
}
