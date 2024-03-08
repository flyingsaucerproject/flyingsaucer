package org.xhtmlrenderer.test;

import org.xhtmlrenderer.layout.LayoutContext;
import org.xhtmlrenderer.render.Box;
import org.xhtmlrenderer.swing.BoxRenderer;
import org.xhtmlrenderer.util.IOUtil;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.Files.newInputStream;
import static java.nio.file.Files.newOutputStream;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;


/**
 * ReferenceComparison runs a comparison of rendering a set of source XHTML files against a
 */
public class ReferenceComparison {
    private final int width;
    private final boolean isVerbose;
    private static final String LINE_SEPARATOR = "\n";

    public static void main(String[] args) throws IOException {
        // TODO: check args
        ReferenceComparison rc = new ReferenceComparison(1024, false);
        File source = new File(args[0]);
        File reference = new File(args[1]);
        File failed = new File(args[2]);
        rc.compareDirectory(source, reference, failed);
    }

    /**
     * Initializes (does not launch) the reference comparison.
     *
     * @param width width at which pages should be rendered
     */
    public ReferenceComparison(int width, boolean verbose) {
        this.width = width;
        this.isVerbose = verbose;
    }

    public void compareDirectory(File sourceDirectory, File referenceDir, File failedDirectory) throws IOException {
        checkDirectories(sourceDirectory, referenceDir, failedDirectory);
        log("Starting comparison using width " + width);
        IOUtil.deleteAllFiles(failedDirectory);

        boolean wasEnabled = enableLogging(false);
        try {
            CompareStatistics stats = new CompareStatistics();
            for (File file : listSourceFiles(sourceDirectory)) {
                try {
                    compareFile(file, referenceDir, failedDirectory, stats);
                } catch (IOException e) {
                    stats.failedIOException(e);
                }
            }
            stats.report();
        } finally {
            enableLogging(wasEnabled);
        }
    }

    private boolean enableLogging(final boolean isEnabled) {
        final String prop = "xr.util-logging.loggingEnabled";
        final boolean orgVal = Boolean.parseBoolean(System.getProperty(prop));
        System.setProperty(prop, Boolean.valueOf(isEnabled).toString());
        return orgVal;
    }

    private void checkDirectories(File sourceDirectory, File referenceDir, File failedDirectory) {
        if (!sourceDirectory.exists() || !sourceDirectory.isDirectory()) {
            throw new IllegalArgumentException("Source dir. doesn't exist, or not a directory: " + sourceDirectory);
        }
        if (!referenceDir.exists() || !referenceDir.isDirectory()) {
            throw new IllegalArgumentException("Reference dir. doesn't exist, or not a directory: " + referenceDir);
        }
        if (failedDirectory.exists() && !failedDirectory.isDirectory()) {
            throw new IllegalArgumentException("Need directory for failed matches, not a directory: " + failedDirectory);
        } else if (!failedDirectory.exists()) {
            if (!failedDirectory.mkdirs()) {
                throw new RuntimeException(
                        "Could not create directory path (.mkdirs failed without an exception) " +
                                failedDirectory.getAbsolutePath()
                );
            }
        }
    }

    private boolean verbose() {
        return isVerbose;
    }

    private Iterable<File> listSourceFiles(File sourceDirectory) {
        File[] files = sourceDirectory.listFiles((dir, name) -> Regress.EXTENSIONS.contains(name.substring(name.lastIndexOf(".") + 1)));
        return files == null ? emptyList() : asList(files);
    }

    void compareFile(File source, File referenceDir, File failedDirectory, CompareStatistics stat) throws IOException {
        log("Comparing " + source.getPath());
        stat.checking(source);
        // TODO: reuse code from Regress
        BoxRenderer renderer = new BoxRenderer(source, width);
        Box box;
        try {
            log("rendering");
            box = renderer.render();
            log("rendered");
        } catch (Exception e) {
            e.printStackTrace();
            stat.failedToRender(e);
            storeFailed(failedDirectory, source);
            log("Could not render input file, skipping: " + source + " err: " + e.getMessage());
            return;
        }
        LayoutContext layoutContext = renderer.getLayoutContext();
        String inputFileName = source.getName();
        String refRendered = trimTrailingLS(readReference(referenceDir, inputFileName, Regress.RENDER_SFX));
        String rendered = trimTrailingLS(box.dump(layoutContext, "", Box.DUMP_RENDER));
        if (!compareLines(refRendered, rendered, stat)) {
            storeFailed(failedDirectory, new File(referenceDir, inputFileName), Regress.RENDER_SFX, rendered);
        }

        final String refLaidOut = trimTrailingLS(readReference(referenceDir, inputFileName, Regress.LAYOUT_SFX));
        final String laidOut = trimTrailingLS(box.dump(layoutContext, "", Box.DUMP_LAYOUT));
        if (!compareLines(refLaidOut, laidOut, stat)) {
            storeFailed(failedDirectory, new File(referenceDir, inputFileName), Regress.LAYOUT_SFX, laidOut);
        }
    }

    private String trimTrailingLS(String s) {
        if (s.endsWith(LINE_SEPARATOR)) {
            s = s.substring(0, s.length() - LINE_SEPARATOR.length());
        }
        return s;
    }

    private void storeFailed(File failedDirectory, File refFile, String suffix, String compareTo) {
        copyToFailed(failedDirectory, refFile, "");
        copyToFailed(failedDirectory, refFile, Regress.PNG_SFX);
        copyToFailed(failedDirectory, refFile, suffix);

        try (OutputStreamWriter fw = new OutputStreamWriter(newOutputStream(new File(failedDirectory, refFile.getName() + ".err" + suffix).toPath()), UTF_8)) {
            try (BufferedWriter bw = new BufferedWriter(fw)) {
                bw.write(compareTo);
                bw.flush();
            } catch (IOException e) {
                throw new RuntimeException("unexpected IO exception on writing 'failed' info for test.", e);
            }
            // swallow
        } catch (IOException e) {
            throw new RuntimeException("unexpected IO exception on writing 'failed' info for test.", e);
        }
        // swallow
    }

    private void copyToFailed(File failedDirectory, File refFile, String suffix) {
        File source = new File(failedDirectory, refFile.getName() + suffix);
        if (!source.exists()) {
            source = new File(refFile.getAbsoluteFile().getParentFile(), refFile.getName() + suffix);
            try {
                IOUtil.copyFile(source, failedDirectory);
            } catch (IOException e) {
                System.err.println("Failed to copy file (reference) " + source + " to failed directory, err " + e.getMessage());
            }

        }
    }

    private boolean compareLines(String refText, String text, CompareStatistics statistics) throws IOException {
        log("running comparison");
        LineNumberReader lnrRef = new LineNumberReader(new StringReader(refText));
        LineNumberReader lnrOther = new LineNumberReader(new StringReader(text));
        String lineRef;
        String lineOther;
        while ((lineRef = lnrRef.readLine()) != null) {
            lineOther = lnrOther.readLine();
            if (lineOther == null) {
                statistics.failedRefIsLonger();
                return false;
            }
            if (!lineRef.equals(lineOther)) {
                statistics.failedDontMatch(lineRef, lineOther);
                return false;
            }
        }
        if (lnrOther.readLine() != null) {
            statistics.failedOtherIsLonger();
            return false;
        }
        return true;
    }

    private void storeFailed(File failedDirectory, File sourceFile) {
        try {
            IOUtil.copyFile(sourceFile, failedDirectory);
        } catch (IOException e) {
            System.err.println("Failed to copy file to failed directory: " + sourceFile + ", err: " + e.getMessage());
        }
    }

    private String readReference(File referenceDir, String input, String sfx) throws IOException {
        File f = new File(referenceDir, input + sfx);
        
        try (BufferedReader rdr = new BufferedReader(new InputStreamReader(newInputStream(f.toPath()), UTF_8))) {
            String line;
            StringBuilder sb = new StringBuilder();
            while ((line = rdr.readLine()) != null) {
                sb.append(line);
                sb.append(LINE_SEPARATOR);
            }
            return sb.toString();
        }
    }

    private void log(final String msg) {
        if (verbose()) {
            System.out.println(msg);
        }
    }

    private static class CompareStatistics {
        private File currentFile;
        private static final Result OK = new ResultOK();
        private final Map<File, Result> files = new HashMap<>();

        public void failedToRender(Exception e) {
            files.put(currentFile, new RenderFailed(e));
        }

        public void failedRefIsLonger() {
            files.put(currentFile, new RefIsLonger());
        }

        public void failedDontMatch(String lineRef, String lineOther) {
            files.put(currentFile, new LineMismatch(lineRef, lineOther));
        }

        public void failedOtherIsLonger() {
            files.put(currentFile, new OtherIsLonger());
        }

        public void failedIOException(IOException e) {
            files.put(currentFile, new FailedIO(e));
        }

        public boolean failed() {
            return files.get(currentFile) instanceof FailedResult;
        }

        public void checking(File source) {
            currentFile = source;
            files.put(currentFile, OK);
        }

        public boolean succeeded() {
            return files.get(currentFile) instanceof ResultOK;
        }

        public void report() {
            int failed = 0;
            for (File file : files.keySet()) {
                Result result = files.get(file);

                if (result instanceof FailedResult) {
                    failed++;
                    System.out.println(result.describe(file));
                }
            }
            System.out.println("Checked " + files.keySet().size() + " files, " + (failed > 0 ? failed + " failed." : "all OK."));
        }

        private record RenderFailed(Exception exception) implements Result {
            public String describe(File file) {
                return "FAIL: Render operation threw exception for %s, err %s".formatted(file.getName(), exception.getMessage());
            }
        }

        private static class RefIsLonger implements FailedResult {
            public String describe(File file) {
                return "FAIL: reference is longer (more lines): %s".formatted(file.getName());
            }
        }

        private record LineMismatch(String lineRef, String lineOther) implements FailedResult {
            public String describe(File file) {
                return "FAIL: line content doesn't match for %s%sref: %s%sother: %s".formatted(
                        file.getName(), LINE_SEPARATOR, lineRef, LINE_SEPARATOR, lineOther);
            }
        }

        private static class OtherIsLonger implements FailedResult {
            public String describe(File file) {
                return "FAIL: new rendered output is longer (more lines): %s".formatted(file.getName());
            }
        }

        private static class FailedIO implements FailedResult {
            private final IOException exception;

            public FailedIO(IOException e) {
                this.exception = e;
            }

            public String describe(File file) {
                return "FAIL: IOException when comparing: %s (err: %s".formatted(file, exception.getMessage());
            }
        }

        private interface Result {
            String describe(File file);
        }

        private interface FailedResult extends Result {}

        private static class ResultOK implements Result {
            public String describe(File file) {
                return "OK: %s".formatted(file.getName());
            }
        }
    }

}