package org.xhtmlrenderer.demo.browser;

/**
 * Entry point for launching Browser application in a sandboxed
 * environment; use instead of BrowserStartup in that case.
 * Disables or removes menu items and controls that are not
 * useful in a sandboxed environment.
 *
 * @author Patrick Wright
 */
public class BrowserSandboxLauncher {
    public static void main(String[] args) {
        BrowserStartup bs = new BrowserStartup();
        bs.initUI();

        bs.panel.url.setVisible(false);
        bs.panel.goToPage.setVisible(false);
        bs.actions.open_file.setEnabled(false);

        bs.launch();
    }
}
