package org.xhtmlrenderer.chrome;

import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.unmodifiableList;

public class ChromiumPdfOptions {
    private boolean noPdfHeaderFooter = true;
    private boolean noSandbox = true;
    private boolean disableGpu = true;
    private final List<String> extraArgs = new ArrayList<>();

    public ChromiumPdfOptions noPdfHeaderFooter(boolean v) {
        this.noPdfHeaderFooter = v;
        return this;
    }

    public ChromiumPdfOptions noSandbox(boolean v) {
        this.noSandbox = v;
        return this;
    }

    public ChromiumPdfOptions disableGpu(boolean v) {
        this.disableGpu = v;
        return this;
    }

    public ChromiumPdfOptions addExtraArg(String arg) {
        extraArgs.add(arg);
        return this;
    }

    public boolean isNoPdfHeaderFooter() {
        return noPdfHeaderFooter;
    }

    public boolean isNoSandbox() {
        return noSandbox;
    }

    public boolean isDisableGpu() {
        return disableGpu;
    }

    public List<String> getExtraArgs() {
        return unmodifiableList(extraArgs);
    }
}
