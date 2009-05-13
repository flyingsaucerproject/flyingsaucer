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
    // marker queue item which, if read, means the reading threads should simply stop their polling
    // introduced by kill()
    private static final ImageLoadItem KILL_SWITCH = new ImageLoadItem(null, null);

    // list of items to be loaded
    private final java.util.LinkedList loadQueue;

    /**
     * Intantiates a new queue.
     */
    public ImageLoadQueue() {
        this.loadQueue = new LinkedList();
    }

    /**
     * Queues a new item to be loaded. Thread-safe.
     *
     * @param uri URI of the item to be loaded. As there is no good way of reporting failures, you should ensure
     *            the URI is a proper URL before calling this method.
     * @param re  container for the image to be loaded; will be updated via
     *            {@link org.xhtmlrenderer.swing.MutableFSImage#setImage(String, java.awt.Image)} once image is loaded
     */
    public synchronized void addToQueue(String uri, MutableFSImage re) {
        XRLog.general("Queueing load for image uri " + uri);
        loadQueue.addLast(new ImageLoadItem(uri, re));
        notifyAll();
    }

    /**
     * Returns the next available task from the queue, or blocks if there are no more; items are returned in FIFO order.
     * If none are available, the method will block until the next items is pushed into the queue.
     *
     * @return an ImageLoadItem
     * @throws InterruptedException if the wait (block) was interrupted externally
     */
    public synchronized ImageLoadItem getTask() throws InterruptedException {
        while (loadQueue.isEmpty()) {
            wait();
        }
        if (loadQueue.peekLast() == KILL_SWITCH) {
            XRLog.general(Level.FINE, "Thread " + Thread.currentThread().getName() +
                    " requested item, but queue is shutting down; returning kill switch.");
            return KILL_SWITCH;
        } else {
            ImageLoadItem item = (ImageLoadItem) loadQueue.removeLast();

            XRLog.general(Level.FINE, "Thread " + Thread.currentThread().getName() +
                    " pulled item " + item.uri + " from queue, " + (loadQueue.size() - 1) + " remaining");
            return item;
        }
    }

    /**
     * Removes all items currently in the queue.
     */
    public synchronized void reset() {
        loadQueue.clear();
    }

    /**
     * Indicates that no more items will be added to the queue, no more items currently in the queue will be loaded,
     * and that worker threads polling this queue should shut down.
     */
    public synchronized void kill() {
        loadQueue.addLast(KILL_SWITCH);
        notifyAll();
    }

    /**
     * Returns true if the item, retrieved from the queue via {@link #getTask()}, is a kill switch, meaning the worker
     * that retrieved it should stop polling.
     *
     * @param queueItem an item retrieved from the queue.
     * @return true if the item, retrieved from the queue via {@link #getTask()}, is a kill switch, meaning the worker
     *         that retrieved it should stop polling.
     */
    public static boolean isKillSwitch(Object queueItem) {
        return queueItem == KILL_SWITCH;
    }
}
