package org.joshy.html.app.browser;

/** Talkback interface between BrowserPanel and anyone 
    interested in what is happening with it. */
public interface BrowserPanelListener {
    void pageLoadSuccess(String url, String title);
}
