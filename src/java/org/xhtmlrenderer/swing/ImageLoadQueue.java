/*
 * {{{ header & license
 * Copyright (c) 2009 Patrick Wright
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 * }}}
 */
package org.xhtmlrenderer.swing;

import org.xhtmlrenderer.util.XRLog;

import java.util.LinkedList;
import java.util.logging.Level;


/**
 * A thread-safe queue containing BackgroundImageLoaderItem, each of which represents one image (identified by a URI)
 * which needs to be loaded.
 */
class ImageLoadQueue {
    private final java.util.LinkedList loadQueue;
    private static final Object KILL_SWITCH = new ImageLoadItem(null, null);

    public ImageLoadQueue() {
        this.loadQueue = new LinkedList();
    }

    public synchronized void addToQueue(String uri, MutableFSImage re) {
        XRLog.general("Queueing load for image uri " + uri);
        loadQueue.addLast(new ImageLoadItem(uri, re));
        notifyAll();
    }

    public synchronized void kill() {
        loadQueue.addLast(KILL_SWITCH);
        notifyAll();
    }

    public static boolean isKillSwitch(Object queueItem) {
        return queueItem == KILL_SWITCH;
    }

    public synchronized ImageLoadItem getTask() throws InterruptedException {
        while (loadQueue.isEmpty()) {
            wait();
        }
        ImageLoadItem item = (ImageLoadItem) loadQueue.removeLast();
        XRLog.general(Level.FINE, "Thread " + Thread.currentThread().getName() + " pulled item " + item.uri + " from queue, " + (loadQueue.size() - 1) + " remaining");
        return item;
    }

    public synchronized void reset() {
        loadQueue.clear();
    }
}
