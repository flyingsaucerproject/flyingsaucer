package org.xhtmlrenderer.chrome;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import static java.nio.file.Files.getLastModifiedTime;
import static java.util.concurrent.TimeUnit.DAYS;

public class ChromeBinaryLocator {
    private static final Logger log = LoggerFactory.getLogger(ChromeBinaryLocator.class);

    private static final String CHROME_INFO_URL = "https://googlechromelabs.github.io/chrome-for-testing/";
    private static final String LAST_VERSIONS_FILE = "last-known-good-versions-with-downloads.json";
    private static final String ALL_VERSIONS_FILE = "known-good-versions-with-downloads.json";
    private static final URI LAST_VERSIONS_URI = URI.create(CHROME_INFO_URL + LAST_VERSIONS_FILE);
    private static final URI ALL_VERSIONS_URI = URI.create(CHROME_INFO_URL + ALL_VERSIONS_FILE);

    static final String DEFAULT_VERSION = "LATEST";

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
        ChromeVersion chromeVersion = resolveChromeVersion(version);
        Path versionDir = cacheDir.resolve(chromeVersion.version()).resolve(platform.id());
        Path cached = findCachedExecutable(versionDir);
        if (cached != null) {
            log.debug("Using cached chrome-headless-shell at {}", cached);
            return cached;
        }
        return downloader.download(chromeVersion, platform, versionDir);
    }

    private ChromeVersion resolveChromeVersion(String requestedVersion) throws IOException {
        if (!DEFAULT_VERSION.equals(requestedVersion)) {
            return new ChromeVersion(requestedVersion, zipUrl(requestedVersion, platform));
        }
        Path knownVersionsFile = downloadIfNeeded(LAST_VERSIONS_FILE, LAST_VERSIONS_URI);
        return readLatestStableVersion(knownVersionsFile, platform);
    }

    private Path downloadIfNeeded(String fileName, URI source) throws IOException {
        Path cachedFile = cacheDir.resolve(fileName);
        if (!Files.exists(cachedFile) || System.currentTimeMillis() - getLastModifiedTime(cachedFile).toMillis() > DAYS.toMillis(1)) {
            Files.createDirectories(cacheDir);
            downloader.httpGetToFile(source, cachedFile);
        }
        return cachedFile;
    }

    static ChromeVersion readLatestStableVersion(Path lastVersionsFile, ChromePlatform platform) throws IOException {
        String json = Files.readString(lastVersionsFile);
        JSONObject root = new JSONObject(json);
        JSONObject stableChannel = root.getJSONObject("channels").getJSONObject("Stable");
        String version = stableChannel.getString("version");
        return parseVersionElement(platform, stableChannel, version)
            .orElseThrow(() -> new IllegalStateException("Cannot find download URL for platform " + platform.id() + " in file " + lastVersionsFile));
    }

    static URI readDownloadUrl(Path allVersionsFile, ChromePlatform platform, String version) throws IOException {
        String json = Files.readString(allVersionsFile);
        JSONObject root = new JSONObject(json);
        JSONArray versions = root.getJSONArray("versions");

        for (int i = 0; i < versions.length(); i++) {
            JSONObject versionElement = versions.getJSONObject(i);
            if (versionElement.getString("version").equals(version)) {
                return parseVersionElement(platform, versionElement, version)
                    .map(ver -> ver.url())
                    .orElseThrow(() ->
                        new IllegalStateException("Cannot find download URL for version " + version + " and platform " + platform.id() + " in file " + allVersionsFile));
            }
        }
        throw new IllegalStateException("Cannot find download URL for version " + version + " and platform " + platform.id() + " in file " + allVersionsFile);
    }

    private static Optional<ChromeVersion> parseVersionElement(ChromePlatform platform, JSONObject stableChannel, String version) {
        JSONArray platforms = stableChannel.getJSONObject("downloads").getJSONArray("chrome-headless-shell");
        for (int i = 0; i < platforms.length(); i++) {
            JSONObject platformElement = platforms.getJSONObject(i);
            if (platformElement.getString("platform").equals(platform.id())) {
                return Optional.of(new ChromeVersion(version, URI.create(platformElement.getString("url"))));
            }
        }
        return Optional.empty();
    }

    private URI zipUrl(String version, ChromePlatform platform) throws IOException {
        Path allVersionsFile = downloadIfNeeded(ALL_VERSIONS_FILE, ALL_VERSIONS_URI);
        return readDownloadUrl(allVersionsFile, platform, version);
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
        return Paths.get(System.getProperty("user.home"), ".cache", "flying-saucer");
    }
}
