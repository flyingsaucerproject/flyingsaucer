package org.xhtmlrenderer.test;

import org.xhtmlrenderer.util.u;
import java.io.File;

public class DocumentDiffGenerate {
    public static void main(String[] args) throws Exception {
        DocumentDiffTest ddt = new DocumentDiffTest();
        ddt.generateDiffs(new File("tests/diff"),500,500);
    }
}

