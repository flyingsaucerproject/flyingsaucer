package org.xhtmlrenderer.chrome;

import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ChromeBinaryLocator {
    private static final Logger log = LoggerFactory.getLogger(ChromeBinaryLocator.class);

    public static final String DEFAULT_VERSION = "145.0.7632.26";

    @Nullable
    private final Path explicitPath;
    private final String version;
    private final Path cacheDir;
    private final ChromePlatform platform;
    private final ChromeBinaryDownloader downloader;

    public ChromeBinaryLocator() {
        this(null, DEFAULT_VERSION, defaultCacheDir(), ChromePlatform.detect(), new ChromeBinaryDownloader());
    }

    ChromeBinaryLocator(@Nullable Path explicitPath, String version, Path cacheDir,
                        ChromePlatform platform, ChromeBinaryDownloader downloader) {
        this.explicitPath = explicitPath;
        this.version = version;
        this.cacheDir = cacheDir;
        this.platform = platform;
        this.downloader = downloader;
    }

    public Path locate() throws IOException {
        if (explicitPath != null) {
            if (!Files.exists(explicitPath)) {
                throw new IOException("chrome-headless-shell binary not found at " + explicitPath);
            }
            return explicitPath;
        }
        Path versionDir = cacheDir.resolve(version).resolve(platform.id());
        Path cached = findCachedExecutable(versionDir);
        if (cached != null) {
            log.debug("Using cached chrome-headless-shell at {}", cached);
            return cached;
        }
        return downloader.download(version, platform, versionDir);
    }

    @Nullable
    private Path findCachedExecutable(Path versionDir) {
        Path nested = versionDir
                .resolve("chrome-headless-shell-" + platform.id())
                .resolve(platform.executableName());
        if (Files.exists(nested)) {
            return nested;
        }
        Path direct = versionDir.resolve(platform.executableName());
        return Files.exists(direct) ? direct : null;
    }

    public static Path defaultCacheDir() {
        return Paths.get(System.getProperty("user.home"), ".cache", "flying-saucer-chrome-pdf");
    }
}
