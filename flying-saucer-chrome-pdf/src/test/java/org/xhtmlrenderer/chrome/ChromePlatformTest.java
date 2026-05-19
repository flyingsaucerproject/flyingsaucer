package org.xhtmlrenderer.chrome;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.xhtmlrenderer.chrome.ChromePlatform.LINUX64;
import static org.xhtmlrenderer.chrome.ChromePlatform.MAC_ARM64;
import static org.xhtmlrenderer.chrome.ChromePlatform.MAC_X64;
import static org.xhtmlrenderer.chrome.ChromePlatform.WIN64;

class ChromePlatformTest {

    @ParameterizedTest
    @CsvSource({
            "Mac OS X,    x86_64,   false, MAC_X64",
            "Mac OS X,    aarch64,  true,  MAC_ARM64",
            "Darwin,      arm64,    true,  MAC_ARM64",
            "Windows 11,  amd64,    false, WIN64",
            "Windows 10,  x86_64,   false, WIN64",
            "Linux,       x86_64,   false, LINUX64",
            "Linux,       amd64,    false, LINUX64",
    })
    void detectsSupportedPlatform(String os, String arch, boolean arm64, ChromePlatform expected) {
        assertThat(ChromePlatform.detect(os, arch, arm64)).isEqualTo(expected);
    }

    @ParameterizedTest
    @CsvSource({
            "Linux, aarch64, true",
            "Linux, arm64,   true",
    })
    void linuxArm64IsNotSupported(String os, String arch, boolean arm64) {
        assertThatThrownBy(() -> ChromePlatform.detect(os, arch, arm64))
                .isInstanceOf(UnsupportedOperationException.class)
                .hasMessageContaining("Linux arm64")
                .hasMessageContaining("setBinaryPath");
    }

    @ParameterizedTest
    @CsvSource({
            "FreeBSD,  x86_64,  false",
            "Solaris,  sparc,   false",
            "'',       x86_64,  false",
    })
    void unknownOsIsNotSupported(String os, String arch, boolean arm64) {
        assertThatThrownBy(() -> ChromePlatform.detect(os, arch, arm64))
                .isInstanceOf(UnsupportedOperationException.class)
                .hasMessageContaining("Unsupported OS")
                .hasMessageContaining(arch);
    }

    @org.junit.jupiter.api.Test
    void platformIdAndExecutableName() {
        assertThat(LINUX64.id()).isEqualTo("linux64");
        assertThat(LINUX64.executableName()).isEqualTo("chrome-headless-shell");
        assertThat(WIN64.executableName()).isEqualTo("chrome-headless-shell.exe");
        assertThat(MAC_X64.id()).isEqualTo("mac-x64");
        assertThat(MAC_ARM64.id()).isEqualTo("mac-arm64");
    }
}
