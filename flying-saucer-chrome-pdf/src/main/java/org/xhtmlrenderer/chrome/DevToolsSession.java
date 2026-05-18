package org.xhtmlrenderer.chrome;

import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.WebSocket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class DevToolsSession implements AutoCloseable {
    private static final Logger log = LoggerFactory.getLogger(DevToolsSession.class);
    private static final Pattern LISTENING_LINE = Pattern.compile("DevTools listening on (ws://\\S+)");

    private final Process process;
    private final WebSocket webSocket;
    private final AtomicLong messageId = new AtomicLong();
    private final ConcurrentHashMap<Long, CompletableFuture<String>> pending = new ConcurrentHashMap<>();
    private final AtomicReference<@Nullable CompletableFuture<Void>> currentLoad = new AtomicReference<>();
    private volatile boolean closed;

    private DevToolsSession(Process process, WebSocket webSocket) {
        this.process = process;
        this.webSocket = webSocket;
    }

    static DevToolsSession start(Path binary, ChromiumPdfOptions options, Duration startupTimeout) throws IOException {
        Process proc = new ProcessBuilder(buildCommand(binary, options))
                .redirectErrorStream(false)
                .start();

        CompletableFuture<URI> listening = new CompletableFuture<>();
        startStderrReader(proc, listening);

        URI browserWs;
        try {
            browserWs = listening.get(startupTimeout.toMillis(), TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            proc.destroyForcibly();
            throw new IOException("chrome-headless-shell failed to start within " + startupTimeout, e);
        }

        URI pageWs = findFirstPageWebSocketUrl(browserWs.getPort(), startupTimeout);

        SessionHolder holder = new SessionHolder();
        WebSocket ws;
        try {
            ws = HttpClient.newHttpClient()
                    .newWebSocketBuilder()
                    .buildAsync(pageWs, holder.listener())
                    .get(startupTimeout.toMillis(), TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            proc.destroyForcibly();
            throw new IOException("Failed to connect to chrome DevTools WebSocket at " + pageWs, e);
        }

        DevToolsSession session = new DevToolsSession(proc, ws);
        holder.session = session;
        session.call("Page.enable", "{}", Duration.ofSeconds(5));
        return session;
    }

    private static void startStderrReader(Process proc, CompletableFuture<URI> listening) {
        Thread reader = new Thread(() -> {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(proc.getErrorStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) {
                    log.trace("chrome stderr: {}", line);
                    if (!listening.isDone()) {
                        Matcher m = LISTENING_LINE.matcher(line);
                        if (m.find()) {
                            listening.complete(URI.create(m.group(1)));
                        }
                    }
                }
            } catch (IOException e) {
                listening.completeExceptionally(e);
            }
        }, "fs-chrome-stderr");
        reader.setDaemon(true);
        reader.start();
    }

    private static URI findFirstPageWebSocketUrl(int port, Duration timeout) throws IOException {
        HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(2)).build();
        long deadline = System.nanoTime() + timeout.toNanos();
        IOException lastError = null;
        while (System.nanoTime() < deadline) {
            try {
                HttpResponse<String> resp = http.send(
                        HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + "/json/list"))
                                .timeout(Duration.ofSeconds(2))
                                .GET()
                                .build(),
                        HttpResponse.BodyHandlers.ofString());
                if (resp.statusCode() == 200) {
                    URI url = findPageWsInJsonArray(resp.body());
                    if (url != null) return url;
                }
            } catch (IOException e) {
                lastError = e;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IOException("Interrupted while waiting for chrome page target", e);
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IOException("Interrupted while waiting for chrome page target", e);
            }
        }
        throw new IOException("chrome did not expose a page target on port " + port + " within " + timeout, lastError);
    }

    private static final Pattern PAGE_WS_URL = Pattern.compile(
            "\"type\"\\s*:\\s*\"page\".*?\"webSocketDebuggerUrl\"\\s*:\\s*\"(ws://[^\"]+)\"",
            Pattern.DOTALL);

    @Nullable
    private static URI findPageWsInJsonArray(String body) {
        Matcher m = PAGE_WS_URL.matcher(body);
        return m.find() ? URI.create(m.group(1)) : null;
    }

    private void onMessage(String json) {
        Long id = extractLong(json, "id");
        if (id != null) {
            CompletableFuture<String> fut = pending.remove(id);
            if (fut != null) fut.complete(json);
            return;
        }
        String method = extractStringField(json, "method");
        if ("Page.loadEventFired".equals(method)) {
            CompletableFuture<Void> load = currentLoad.get();
            if (load != null) load.complete(null);
        }
    }

    private void onWsClosed() {
        closed = true;
        IOException reason = new IOException("DevTools WebSocket closed");
        for (CompletableFuture<String> f : pending.values()) {
            f.completeExceptionally(reason);
        }
        pending.clear();
        CompletableFuture<Void> load = currentLoad.getAndSet(null);
        if (load != null) load.completeExceptionally(reason);
    }

    String call(String method, String params, Duration timeout) throws IOException {
        if (closed) throw new IOException("DevTools session is closed");
        long id = messageId.incrementAndGet();
        String msg = "{\"id\":" + id + ",\"method\":\"" + method + "\",\"params\":" + params + "}";
        CompletableFuture<String> fut = new CompletableFuture<>();
        pending.put(id, fut);
        try {
            webSocket.sendText(msg, true).get(5, TimeUnit.SECONDS);
        } catch (Exception e) {
            pending.remove(id);
            throw new IOException("Failed to send " + method, e);
        }
        try {
            String response = fut.get(timeout.toMillis(), TimeUnit.MILLISECONDS);
            String error = extractObjectField(response, "error");
            if (error != null) {
                String errMsg = extractStringField(error, "message");
                throw new IOException("DevTools " + method + " failed: " + (errMsg != null ? errMsg : error));
            }
            return response;
        } catch (TimeoutException e) {
            pending.remove(id);
            throw new IOException("DevTools " + method + " timed out after " + timeout, e);
        } catch (InterruptedException e) {
            pending.remove(id);
            Thread.currentThread().interrupt();
            throw new IOException("Interrupted waiting for DevTools " + method, e);
        } catch (ExecutionException e) {
            pending.remove(id);
            throw new IOException("DevTools " + method + " failed", e.getCause());
        }
    }

    byte[] printToPdf(String htmlUrl, ChromiumPdfOptions options, Duration timeout) throws IOException {
        CompletableFuture<Void> load = new CompletableFuture<>();
        currentLoad.set(load);
        try {
            call("Page.navigate", "{\"url\":\"" + escapeJson(htmlUrl) + "\"}", timeout);
            try {
                load.get(timeout.toMillis(), TimeUnit.MILLISECONDS);
            } catch (TimeoutException e) {
                throw new IOException("Page.loadEventFired did not fire within " + timeout + " for " + htmlUrl, e);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IOException("Interrupted waiting for page load", e);
            } catch (ExecutionException e) {
                throw new IOException("Page load failed", e.getCause());
            }
        } finally {
            currentLoad.compareAndSet(load, null);
        }
        String pdfResponse = call("Page.printToPDF", buildPrintParams(options), timeout);
        String resultObj = extractObjectField(pdfResponse, "result");
        if (resultObj == null) throw new IOException("Page.printToPDF response missing result: " + pdfResponse);
        String base64 = extractStringField(resultObj, "data");
        if (base64 == null) throw new IOException("Page.printToPDF result missing data");
        return Base64.getDecoder().decode(base64);
    }

    boolean isAlive() {
        return !closed && process.isAlive();
    }

    @Override
    public void close() {
        if (closed) return;
        closed = true;
        try {
            webSocket.sendClose(WebSocket.NORMAL_CLOSURE, "bye").get(2, TimeUnit.SECONDS);
        } catch (Exception ignored) {
            // best-effort
        }
        webSocket.abort();
        process.destroy();
        try {
            if (!process.waitFor(5, TimeUnit.SECONDS)) {
                process.destroyForcibly();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            process.destroyForcibly();
        }
        IOException reason = new IOException("DevTools session closed");
        for (CompletableFuture<String> f : pending.values()) {
            f.completeExceptionally(reason);
        }
        pending.clear();
    }

    private static List<String> buildCommand(Path binary, ChromiumPdfOptions options) {
        List<String> cmd = new ArrayList<>();
        cmd.add(binary.toString());
        cmd.add("--headless");
        if (options.isDisableGpu()) cmd.add("--disable-gpu");
        if (options.isNoSandbox()) cmd.add("--no-sandbox");
        cmd.add("--remote-debugging-port=0");
        cmd.addAll(options.getExtraArgs());
        cmd.add("about:blank");
        return cmd;
    }

    private static String buildPrintParams(ChromiumPdfOptions options) {
        StringBuilder sb = new StringBuilder("{");
        sb.append("\"printBackground\":true");
        sb.append(",\"displayHeaderFooter\":").append(!options.isNoPdfHeaderFooter());
        sb.append("}");
        return sb.toString();
    }

    private static class SessionHolder {
        volatile @Nullable DevToolsSession session;

        WebSocket.Listener listener() {
            return new WebSocket.Listener() {
                final StringBuilder buf = new StringBuilder();

                @Override
                public void onOpen(WebSocket ws) {
                    ws.request(1);
                }

                @Override
                public CompletionStage<?> onText(WebSocket ws, CharSequence data, boolean last) {
                    buf.append(data);
                    if (last) {
                        String full = buf.toString();
                        buf.setLength(0);
                        DevToolsSession s = session;
                        if (s != null) s.onMessage(full);
                    }
                    ws.request(1);
                    return null;
                }

                @Override
                public CompletionStage<?> onClose(WebSocket ws, int statusCode, String reason) {
                    DevToolsSession s = session;
                    if (s != null) s.onWsClosed();
                    return null;
                }

                @Override
                public void onError(WebSocket ws, Throwable error) {
                    log.debug("WebSocket error", error);
                    DevToolsSession s = session;
                    if (s != null) s.onWsClosed();
                }
            };
        }
    }

    // --- minimal JSON extraction (avoid pulling in a JSON library) ---

    /** Find the index in {@code json} after {@code "<field>":} (whitespace tolerant), or -1. */
    private static int findFieldValueStart(String json, String field) {
        String key = "\"" + field + "\"";
        int i = 0;
        while ((i = json.indexOf(key, i)) >= 0) {
            int j = i + key.length();
            while (j < json.length() && Character.isWhitespace(json.charAt(j))) j++;
            if (j < json.length() && json.charAt(j) == ':') {
                j++;
                while (j < json.length() && Character.isWhitespace(json.charAt(j))) j++;
                return j;
            }
            i++;
        }
        return -1;
    }

    @Nullable
    private static Long extractLong(String json, String field) {
        int start = findFieldValueStart(json, field);
        if (start < 0) return null;
        int end = start;
        if (end < json.length() && json.charAt(end) == '-') end++;
        while (end < json.length() && Character.isDigit(json.charAt(end))) end++;
        if (end == start) return null;
        try {
            return Long.parseLong(json.substring(start, end));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @Nullable
    private static String extractStringField(String json, String field) {
        int start = findFieldValueStart(json, field);
        if (start < 0 || start >= json.length() || json.charAt(start) != '"') return null;
        StringBuilder out = new StringBuilder();
        int i = start + 1;
        while (i < json.length()) {
            char c = json.charAt(i);
            if (c == '\\') {
                if (i + 1 >= json.length()) return null;
                char next = json.charAt(i + 1);
                switch (next) {
                    case '"': out.append('"'); break;
                    case '\\': out.append('\\'); break;
                    case '/': out.append('/'); break;
                    case 'n': out.append('\n'); break;
                    case 't': out.append('\t'); break;
                    case 'r': out.append('\r'); break;
                    case 'b': out.append('\b'); break;
                    case 'f': out.append('\f'); break;
                    default: out.append(next); break;
                }
                i += 2;
            } else if (c == '"') {
                return out.toString();
            } else {
                out.append(c);
                i++;
            }
        }
        return null;
    }

    @Nullable
    private static String extractObjectField(String json, String field) {
        int start = findFieldValueStart(json, field);
        if (start < 0 || start >= json.length() || json.charAt(start) != '{') return null;
        int depth = 0;
        int end = start;
        boolean inString = false;
        boolean escape = false;
        while (end < json.length()) {
            char c = json.charAt(end);
            if (inString) {
                if (escape) escape = false;
                else if (c == '\\') escape = true;
                else if (c == '"') inString = false;
            } else {
                if (c == '"') inString = true;
                else if (c == '{') depth++;
                else if (c == '}') {
                    depth--;
                    if (depth == 0) return json.substring(start, end + 1);
                }
            }
            end++;
        }
        return null;
    }

    private static String escapeJson(String s) {
        StringBuilder sb = new StringBuilder(s.length() + 16);
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"': sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\n': sb.append("\\n"); break;
                case '\t': sb.append("\\t"); break;
                case '\r': sb.append("\\r"); break;
                case '\b': sb.append("\\b"); break;
                case '\f': sb.append("\\f"); break;
                default:
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
                    break;
            }
        }
        return sb.toString();
    }
}
