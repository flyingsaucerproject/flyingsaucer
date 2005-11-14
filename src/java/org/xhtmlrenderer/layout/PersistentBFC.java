package org.xhtmlrenderer.layout;

import org.xhtmlrenderer.render.Box;

public class PersistentBFC {
    private FloatManager floatManager = new FloatManager();
    
    public PersistentBFC(Box master, LayoutContext c) {
        master.setPersistentBFC(this);
        floatManager.setMaster(master);
    }
    
    public FloatManager getFloatManager() {
        return floatManager;
    }
}
