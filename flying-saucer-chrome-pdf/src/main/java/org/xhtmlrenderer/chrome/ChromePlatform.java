package org.xhtmlrenderer.chrome;

import java.util.Locale;

public enum ChromePlatform {
    LINUX64("linux64", "chrome-headless-shell"),
    MAC_X64("mac-x64", "chrome-headless-shell"),
    MAC_ARM64("mac-arm64", "chrome-headless-shell"),
    WIN64("win64", "chrome-headless-shell.exe");

    private final String id;
    private final String executableName;

    ChromePlatform(String id, String executableName) {
        this.id = id;
        this.executableName = executableName;
    }

    public String id() {
        return id;
    }

    public String executableName() {
        return executableName;
    }

    public static ChromePlatform detect() {
        String os = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
        String arch = System.getProperty("os.arch", "").toLowerCase(Locale.ROOT);
        boolean arm64 = arch.contains("aarch64") || arch.contains("arm64");

        if (os.contains("mac") || os.contains("darwin")) {
            return arm64 ? MAC_ARM64 : MAC_X64;
        }
        if (os.contains("win")) {
            return WIN64;
        }
        if (os.contains("nux") || os.contains("nix")) {
            if (arm64) {
                throw new UnsupportedOperationException(
                        "chrome-for-testing does not publish a Linux arm64 build of chrome-headless-shell. " +
                                "Install chrome-headless-shell manually and pass its path via ChromiumPdfRenderer.setBinaryPath().");
            }
            return LINUX64;
        }
        throw new UnsupportedOperationException("Unsupported OS for chrome-for-testing: " + os);
    }
}
