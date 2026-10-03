package org.xhtmlrenderer.chrome;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.xhtmlrenderer.chrome.ChromeBinaryLocator.readDownloadUrl;
import static org.xhtmlrenderer.chrome.ChromeBinaryLocator.readLatestStableVersion;
import static org.xhtmlrenderer.chrome.ChromePlatform.LINUX64;
import static org.xhtmlrenderer.chrome.ChromePlatform.MAC_ARM64;
import static org.xhtmlrenderer.chrome.ChromePlatform.MAC_X64;
import static org.xhtmlrenderer.chrome.ChromePlatform.WIN64;

class ChromeBinaryLocatorTest {

    @TempDir
    private Path tmp;

    @Test
    void usesExplicitPathWhenProvided() throws IOException {
        Path explicit = Files.createFile(tmp.resolve("chrome-headless-shell"));
        FakeDownloader downloader = new FakeDownloader();

        Path located = new ChromeBinaryLocator(explicit, "1.0.0", tmp.resolve("cache"), LINUX64, downloader).locate();

        assertThat(located).isEqualTo(explicit);
        assertThat(downloader.callCount).hasValue(0);
    }

    @Test
    void failsWhenExplicitPathDoesNotExist() {
        Path missing = tmp.resolve("nope");
        ChromeBinaryLocator locator = new ChromeBinaryLocator(missing, "1.0.0", tmp.resolve("cache"), LINUX64, new FakeDownloader());

        assertThatThrownBy(locator::locate)
                .isInstanceOf(IOException.class)
                .hasMessageContaining("not found");
    }

    @Test
    void usesCachedBinaryWithoutDownloading() throws IOException, URISyntaxException {
        Path cacheDir = tmp.resolve("cache");
        Path versionDir = cacheDir.resolve("133.0.6857.0").resolve(LINUX64.id());
        Path nestedDir = versionDir.resolve("chrome-headless-shell-" + LINUX64.id());
        Files.createDirectories(nestedDir);
        Path executable = Files.createFile(nestedDir.resolve(LINUX64.executableName()));
        Files.copy(allVersionsFile(), cacheDir.resolve(allVersionsFile().getFileName()));
        FakeDownloader downloader = new FakeDownloader();

        Path located = new ChromeBinaryLocator(null, "133.0.6857.0", cacheDir, LINUX64, downloader).locate();

        assertThat(located).isEqualTo(executable);
        assertThat(downloader.callCount).hasValue(0);
    }

    @Test
    void downloadsWhenCacheIsEmpty() throws IOException, URISyntaxException {
        Path cacheDir = tmp.resolve("cache");
        Files.createDirectories(cacheDir);
        Files.copy(allVersionsFile(), cacheDir.resolve(allVersionsFile().getFileName()));
        FakeDownloader downloader = new FakeDownloader();

        new ChromeBinaryLocator(null, "133.0.6857.0", cacheDir, MAC_ARM64, downloader).locate();

        assertThat(downloader.callCount).hasValue(1);
        assertThat(downloader.lastVersion).isEqualTo("133.0.6857.0");
        assertThat(downloader.lastPlatform).isEqualTo(MAC_ARM64);
    }

    @Test
    void parsesJsonWithKnownChromeVersions() throws URISyntaxException, IOException {
        Path file = lastVersionsFile();

        assertThat(readLatestStableVersion(file, LINUX64).url().toString())
            .isEqualTo("https://storage.googleapis.com/chrome-for-testing-public/149.0.7827.54/linux64/chrome-headless-shell-linux64.zip");
        assertThat(readLatestStableVersion(file, MAC_X64).url().toString())
            .isEqualTo("https://storage.googleapis.com/chrome-for-testing-public/149.0.7827.54/mac-arm64/chrome-headless-shell-mac-arm64.zip");
        assertThat(readLatestStableVersion(file, MAC_ARM64).url().toString())
            .isEqualTo("https://storage.googleapis.com/chrome-for-testing-public/149.0.7827.54/mac-arm64/chrome-headless-shell-mac-arm64.zip");
        assertThat(readLatestStableVersion(file, WIN64).url().toString())
            .isEqualTo("https://storage.googleapis.com/chrome-for-testing-public/149.0.7827.54/win64/chrome-headless-shell-win64.zip");
    }

    @Test
    void parsesCorrectZipUrlForGivenVersionAndPlatform() throws URISyntaxException, IOException {
        Path file = allVersionsFile();

        assertThat(readDownloadUrl(file, LINUX64, "133.0.6857.0").toString())
            .isEqualTo("https://storage.googleapis.com/chrome-for-testing-public/133.0.6857.0/linux64/chrome-headless-shell-linux64.zip");
        assertThat(readDownloadUrl(file, MAC_X64, "133.0.6857.0").toString())
            .isEqualTo("https://storage.googleapis.com/chrome-for-testing-public/133.0.6857.0/mac-x64/chrome-headless-shell-mac-x64.zip");
        assertThat(readDownloadUrl(file, MAC_ARM64, "133.0.6857.0").toString())
            .isEqualTo("https://storage.googleapis.com/chrome-for-testing-public/133.0.6857.0/mac-arm64/chrome-headless-shell-mac-arm64.zip");
        assertThat(readDownloadUrl(file, WIN64, "133.0.6857.0").toString())
            .isEqualTo("https://storage.googleapis.com/chrome-for-testing-public/133.0.6857.0/win64/chrome-headless-shell-win64.zip");
    }

    private Path lastVersionsFile() throws URISyntaxException {
        String versionsFileName = "/last-known-good-versions-with-downloads.json";
        URL versionsFile = requireNonNull(getClass().getResource(versionsFileName), () -> "Versions file not found: " + versionsFileName);
        return Path.of(versionsFile.toURI());
    }

    private Path allVersionsFile() throws URISyntaxException {
        String versionsFileName = "/known-good-versions-with-downloads.json";
        URL versionsFile = requireNonNull(getClass().getResource(versionsFileName), () -> "Versions file not found: " + versionsFileName);
        return Path.of(versionsFile.toURI());
    }

    private static class FakeDownloader extends ChromeBinaryDownloader {
        final AtomicInteger callCount = new AtomicInteger();
        String lastVersion = "";
        ChromePlatform lastPlatform = LINUX64;
        @Override
        Path download(ChromeVersion chromeVersion, ChromePlatform platform, Path versionDir) throws IOException {
            callCount.incrementAndGet();
            lastVersion = chromeVersion.version();
            lastPlatform = platform;
            Files.createDirectories(versionDir);
            return Files.createFile(versionDir.resolve(platform.executableName()));
        }
    }
}
