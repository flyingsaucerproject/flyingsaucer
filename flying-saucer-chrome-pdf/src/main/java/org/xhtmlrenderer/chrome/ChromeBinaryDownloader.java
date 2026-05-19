package org.xhtmlrenderer.chrome;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.util.Set;

import static java.nio.file.attribute.PosixFilePermission.GROUP_EXECUTE;
import static java.nio.file.attribute.PosixFilePermission.GROUP_READ;
import static java.nio.file.attribute.PosixFilePermission.OTHERS_EXECUTE;
import static java.nio.file.attribute.PosixFilePermission.OTHERS_READ;
import static java.nio.file.attribute.PosixFilePermission.OWNER_EXECUTE;
import static java.nio.file.attribute.PosixFilePermission.OWNER_READ;
import static java.nio.file.attribute.PosixFilePermission.OWNER_WRITE;

class ChromeBinaryDownloader {
    private static final Logger log = LoggerFactory.getLogger(ChromeBinaryDownloader.class);

    private static final String BASE_URL = "https://storage.googleapis.com/chrome-for-testing-public";

    private final ZipExtractor zipExtractor = new ZipExtractor();

    URI zipUrl(String version, ChromePlatform platform) {
        return URI.create("%s/%s/%s/chrome-headless-shell-%s.zip"
                .formatted(BASE_URL, version, platform.id(), platform.id()));
    }

    Path download(String version, ChromePlatform platform, Path versionDir) throws IOException {
        Files.createDirectories(versionDir);
        URI url = zipUrl(version, platform);
        Path zipFile = Files.createTempFile("chrome-headless-shell-", ".zip");
        try {
            log.info("Downloading chrome-headless-shell from {}", url);
            httpGetToFile(url, zipFile);
            zipExtractor.extract(zipFile, versionDir);
        } finally {
            Files.deleteIfExists(zipFile);
        }
        Path executable = findExecutable(versionDir, platform);
        makeExecutable(executable);
        return executable;
    }

    private void httpGetToFile(URI url, Path target) throws IOException {
        HttpRequest request = HttpRequest.newBuilder(url)
                .timeout(Duration.ofMinutes(5))
                .GET()
                .build();
        try (HttpClient client = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.ALWAYS)
                .connectTimeout(Duration.ofSeconds(30))
                .build()) {
            HttpResponse<Path> response = client.send(request,
                    HttpResponse.BodyHandlers.ofFile(target, StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING));
            if (response.statusCode() != 200) {
                throw new IOException("Failed to download " + url + ": HTTP " + response.statusCode());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Interrupted while downloading " + url, e);
        }
    }

    private Path findExecutable(Path versionDir, ChromePlatform platform) throws IOException {
        // chrome-for-testing zips extract to a subdirectory like "chrome-headless-shell-linux64/chrome-headless-shell"
        Path nested = versionDir
                .resolve("chrome-headless-shell-" + platform.id())
                .resolve(platform.executableName());
        if (Files.exists(nested)) {
            return nested;
        }
        Path direct = versionDir.resolve(platform.executableName());
        if (Files.exists(direct)) {
            return direct;
        }
        throw new IOException("chrome-headless-shell binary not found under " + versionDir);
    }

    private void makeExecutable(Path executable) throws IOException {
        if (executable.getFileSystem().supportedFileAttributeViews().contains("posix")) {
            Files.setPosixFilePermissions(executable, Set.of(
                    OWNER_READ, OWNER_WRITE, OWNER_EXECUTE,
                    GROUP_READ, GROUP_EXECUTE,
                    OTHERS_READ, OTHERS_EXECUTE));
        }
    }
}
