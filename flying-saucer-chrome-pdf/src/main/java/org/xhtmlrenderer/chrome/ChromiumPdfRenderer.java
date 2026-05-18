package org.xhtmlrenderer.chrome;

import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class ChromiumPdfRenderer implements AutoCloseable {
    private static final Logger log = LoggerFactory.getLogger(ChromiumPdfRenderer.class);

    @Nullable
    private Path binaryPath;
    private Path cacheDir = ChromeBinaryLocator.defaultCacheDir();
    private String chromeVersion = ChromeBinaryLocator.DEFAULT_VERSION;
    private ChromiumPdfOptions options = new ChromiumPdfOptions();
    private Duration timeout = Duration.ofSeconds(60);
    private Duration idleProcessTimeout = Duration.ofSeconds(5);

    private final Object lock = new Object();
    @Nullable
    private DevToolsSession session;
    @Nullable
    private ScheduledExecutorService idleScheduler;
    @Nullable
    private ScheduledFuture<?> idleTask;
    @Nullable
    private Thread shutdownHook;

    public ChromiumPdfRenderer setBinaryPath(@Nullable Path binaryPath) {
        this.binaryPath = binaryPath;
        return this;
    }

    public ChromiumPdfRenderer setCacheDir(Path cacheDir) {
        this.cacheDir = cacheDir;
        return this;
    }

    public ChromiumPdfRenderer setChromeVersion(String chromeVersion) {
        this.chromeVersion = chromeVersion;
        return this;
    }

    public ChromiumPdfRenderer setOptions(ChromiumPdfOptions options) {
        this.options = options;
        return this;
    }

    public ChromiumPdfRenderer setTimeout(Duration timeout) {
        this.timeout = timeout;
        return this;
    }

    /**
     * Keep the chrome subprocess alive between renders for this many seconds of inactivity.
     * Set to {@link Duration#ZERO} to spawn a fresh process per render (slower for batches, no idle process).
     * Default: 5 seconds.
     */
    public ChromiumPdfRenderer setIdleProcessTimeout(Duration idleProcessTimeout) {
        this.idleProcessTimeout = idleProcessTimeout;
        return this;
    }

    public byte[] renderToPdf(URL htmlSource) throws IOException {
        return renderToPdf(htmlSource.toString());
    }

    public byte[] renderToPdf(File htmlFile) throws IOException {
        return renderToPdf(htmlFile.toURI().toString());
    }

    public byte[] renderFromHtml(String html) throws IOException {
        Path tempHtml = Files.createTempFile("flying-saucer-chrome-", ".html");
        try {
            Files.writeString(tempHtml, html, StandardCharsets.UTF_8);
            return renderToPdf(tempHtml.toUri().toString());
        } finally {
            Files.deleteIfExists(tempHtml);
        }
    }

    public void renderToPdf(URL htmlSource, Path outputPdf) throws IOException {
        byte[] pdf = renderToPdf(htmlSource);
        Files.write(outputPdf, pdf);
    }

    private byte[] renderToPdf(String htmlInput) throws IOException {
        if (idleProcessTimeout.isZero()) {
            return renderOneShot(htmlInput);
        }
        return renderReusing(htmlInput);
    }

    private byte[] renderReusing(String htmlInput) throws IOException {
        synchronized (lock) {
            cancelIdleClose();
            if (session != null && !session.isAlive()) {
                closeSessionLocked();
            }
            if (session == null) {
                Path binary = locateBinary();
                session = DevToolsSession.start(binary, options, Duration.ofSeconds(30));
                ensureShutdownHookLocked();
            }
            try {
                return session.printToPdf(htmlInput, options, timeout);
            } catch (IOException e) {
                closeSessionLocked();
                throw e;
            } finally {
                scheduleIdleCloseLocked();
            }
        }
    }

    private byte[] renderOneShot(String htmlInput) throws IOException {
        Path binary = locateBinary();
        Path outputPdf = Files.createTempFile("flying-saucer-chrome-", ".pdf");
        try {
            List<String> command = buildOneShotCommand(binary, outputPdf, htmlInput);
            runOneShotProcess(command);
            return Files.readAllBytes(outputPdf);
        } finally {
            Files.deleteIfExists(outputPdf);
        }
    }

    private Path locateBinary() throws IOException {
        return new ChromeBinaryLocator(binaryPath, chromeVersion, cacheDir,
                ChromePlatform.detect(), new ChromeBinaryDownloader()).locate();
    }

    private List<String> buildOneShotCommand(Path binary, Path outputPdf, String htmlInput) {
        List<String> cmd = new ArrayList<>();
        cmd.add(binary.toString());
        cmd.add("--headless");
        if (options.isDisableGpu()) cmd.add("--disable-gpu");
        if (options.isNoSandbox()) cmd.add("--no-sandbox");
        if (options.isNoPdfHeaderFooter()) cmd.add("--no-pdf-header-footer");
        cmd.addAll(options.getExtraArgs());
        cmd.add("--print-to-pdf=" + outputPdf);
        cmd.add(htmlInput);
        return cmd;
    }

    private void runOneShotProcess(List<String> command) throws IOException {
        log.debug("Running: {}", command);
        Process process = new ProcessBuilder(command).redirectErrorStream(true).start();
        String output;
        try (InputStream in = process.getInputStream()) {
            output = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
        boolean finished;
        try {
            finished = process.waitFor(timeout.toMillis(), TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            process.destroyForcibly();
            Thread.currentThread().interrupt();
            throw new IOException("Interrupted while waiting for chrome-headless-shell", e);
        }
        if (!finished) {
            process.destroyForcibly();
            throw new IOException("chrome-headless-shell timed out after " + timeout + ". Output:\n" + output);
        }
        int exitCode = process.exitValue();
        if (exitCode != 0) {
            throw new IOException("chrome-headless-shell exited with code " + exitCode + ". Output:\n" + output);
        }
        if (!output.isBlank()) {
            log.debug("chrome-headless-shell output: {}", output);
        }
    }

    private void scheduleIdleCloseLocked() {
        if (idleScheduler == null) {
            idleScheduler = Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "fs-chrome-idle");
                t.setDaemon(true);
                return t;
            });
        }
        idleTask = idleScheduler.schedule(this::onIdle, idleProcessTimeout.toMillis(), TimeUnit.MILLISECONDS);
    }

    private void cancelIdleClose() {
        if (idleTask != null) {
            idleTask.cancel(false);
            idleTask = null;
        }
    }

    private void onIdle() {
        synchronized (lock) {
            closeSessionLocked();
        }
    }

    private void closeSessionLocked() {
        if (session != null) {
            try {
                session.close();
            } catch (Exception e) {
                log.debug("Error while closing chrome session", e);
            }
            session = null;
        }
    }

    private void ensureShutdownHookLocked() {
        if (shutdownHook != null) return;
        shutdownHook = new Thread(this::closeQuietly, "fs-chrome-shutdown");
        try {
            Runtime.getRuntime().addShutdownHook(shutdownHook);
        } catch (IllegalStateException alreadyShuttingDown) {
            shutdownHook = null;
        }
    }

    private void closeQuietly() {
        try {
            closeInternal(false);
        } catch (Exception e) {
            log.debug("Error during shutdown hook", e);
        }
    }

    @Override
    public void close() {
        closeInternal(true);
    }

    private void closeInternal(boolean removeShutdownHook) {
        synchronized (lock) {
            cancelIdleClose();
            closeSessionLocked();
            if (idleScheduler != null) {
                idleScheduler.shutdownNow();
                idleScheduler = null;
            }
            if (removeShutdownHook && shutdownHook != null) {
                try {
                    Runtime.getRuntime().removeShutdownHook(shutdownHook);
                } catch (IllegalStateException ignored) {
                    // JVM already shutting down
                }
                shutdownHook = null;
            }
        }
    }
}
