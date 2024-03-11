package org.xhtmlrenderer.tool;

import org.xhtmlrenderer.test.DocumentDiffTest;

import java.io.File;

public class Boxer {

    private void run(String filen) throws Exception {
        File file = new File(filen);
        if (!file.exists() || file.isDirectory()) {
            throw new IllegalArgumentException(filen + " not a file, or is a directory. Give me a single file name.");
        }

        System.out.println(DocumentDiffTest.xhtmlToDiff(filen, 1024, 768));
    }

    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            throw new IllegalArgumentException("Give a file name");
        }
        new Boxer().run(args[0]);
    }
}
