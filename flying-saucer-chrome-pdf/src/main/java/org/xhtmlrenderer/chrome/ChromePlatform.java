package org.xhtmlrenderer.chrome;

import static java.util.Locale.ROOT;

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
        String arch = System.getProperty("os.arch", "");
        boolean arm64 = arch.toLowerCase(ROOT).contains("aarch64")
                || arch.toLowerCase(ROOT).contains("arm64");
        return detect(System.getProperty("os.name", ""), arch, arm64);
    }

    static ChromePlatform detect(String os, String arch, boolean arm64) {
        String o = os.toLowerCase(ROOT);
        if (o.contains("mac") || o.contains("darwin")) {
            return arm64 ? MAC_ARM64 : MAC_X64;
        }
        if (o.contains("win")) {
            return WIN64;
        }
        if (o.contains("nux") || o.contains("nix")) {
            if (arm64) {
                throw new UnsupportedOperationException(
                        "chrome-for-testing does not publish a Linux arm64 build of chrome-headless-shell " +
                                "(detected os=" + os + ", arch=" + arch + "). " +
                                "Install chrome-headless-shell manually and pass its path via ChromiumPdfRenderer.setBinaryPath().");
            }
            return LINUX64;
        }
        throw new UnsupportedOperationException("Unsupported OS for chrome-for-testing: " + os + " (arch=" + arch + ")");
    }
}
