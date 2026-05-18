package org.xhtmlrenderer.chrome;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ChromeBinaryLocatorTest {

    @Test
    void usesExplicitPathWhenProvided(@TempDir Path tmp) throws IOException {
        Path explicit = Files.createFile(tmp.resolve("chrome-headless-shell"));
        FakeDownloader downloader = new FakeDownloader();

        Path located = new ChromeBinaryLocator(explicit, "1.0.0", tmp.resolve("cache"),
                ChromePlatform.LINUX64, downloader).locate();

        assertThat(located).isEqualTo(explicit);
        assertThat(downloader.callCount).hasValue(0);
    }

    @Test
    void failsWhenExplicitPathDoesNotExist(@TempDir Path tmp) {
        Path missing = tmp.resolve("nope");
        ChromeBinaryLocator locator = new ChromeBinaryLocator(missing, "1.0.0", tmp.resolve("cache"),
                ChromePlatform.LINUX64, new FakeDownloader());

        assertThatThrownBy(locator::locate)
                .isInstanceOf(IOException.class)
                .hasMessageContaining("not found");
    }

    @Test
    void usesCachedBinaryWithoutDownloading(@TempDir Path tmp) throws IOException {
        Path cacheDir = tmp.resolve("cache");
        Path versionDir = cacheDir.resolve("1.2.3").resolve(ChromePlatform.LINUX64.id());
        Path nestedDir = versionDir.resolve("chrome-headless-shell-" + ChromePlatform.LINUX64.id());
        Files.createDirectories(nestedDir);
        Path executable = Files.createFile(nestedDir.resolve(ChromePlatform.LINUX64.executableName()));
        FakeDownloader downloader = new FakeDownloader();

        Path located = new ChromeBinaryLocator(null, "1.2.3", cacheDir, ChromePlatform.LINUX64, downloader).locate();

        assertThat(located).isEqualTo(executable);
        assertThat(downloader.callCount).hasValue(0);
    }

    @Test
    void downloadsWhenCacheIsEmpty(@TempDir Path tmp) throws IOException {
        Path cacheDir = tmp.resolve("cache");
        FakeDownloader downloader = new FakeDownloader();

        new ChromeBinaryLocator(null, "9.9.9", cacheDir, ChromePlatform.MAC_ARM64, downloader).locate();

        assertThat(downloader.callCount).hasValue(1);
        assertThat(downloader.lastVersion).isEqualTo("9.9.9");
        assertThat(downloader.lastPlatform).isEqualTo(ChromePlatform.MAC_ARM64);
    }

    @Test
    void platformBuildsCorrectZipUrl() {
        ChromeBinaryDownloader downloader = new ChromeBinaryDownloader();

        assertThat(downloader.zipUrl("145.0.7632.26", ChromePlatform.LINUX64).toString())
                .isEqualTo("https://storage.googleapis.com/chrome-for-testing-public/145.0.7632.26/linux64/chrome-headless-shell-linux64.zip");
        assertThat(downloader.zipUrl("145.0.7632.26", ChromePlatform.MAC_ARM64).toString())
                .isEqualTo("https://storage.googleapis.com/chrome-for-testing-public/145.0.7632.26/mac-arm64/chrome-headless-shell-mac-arm64.zip");
    }

    private static class FakeDownloader extends ChromeBinaryDownloader {
        final AtomicInteger callCount = new AtomicInteger();
        String lastVersion = "";
        ChromePlatform lastPlatform = ChromePlatform.LINUX64;

        @Override
        Path download(String version, ChromePlatform platform, Path versionDir) throws IOException {
            callCount.incrementAndGet();
            lastVersion = version;
            lastPlatform = platform;
            Files.createDirectories(versionDir);
            return Files.createFile(versionDir.resolve(platform.executableName()));
        }
    }
}
