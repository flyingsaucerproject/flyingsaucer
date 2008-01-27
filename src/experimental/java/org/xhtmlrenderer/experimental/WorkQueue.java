/*
 * {{{ header & license
 * Copyright (c) 2008 Patrick Wright
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 * }}}
 */
package org.xhtmlrenderer.experimental;

import java.util.LinkedList;

/**
 * @author patrick
*/
public class WorkQueue {
    private final LinkedList queue;
    private final Worker[] workers;

    WorkQueue(final int numThreads) {
        this.queue = new LinkedList();
        this.workers = new Worker[numThreads];
        int cnt = numThreads;
        while (--cnt >= 0) {
            this.workers[cnt] = new Worker();
            this.workers[cnt].start();
        }
    }

    public void queueTask(Runnable r) {
        if (r == null) {
            throw new IllegalArgumentException("! null runnable given to org.xhtmlrenderer.experimental.WorkQueue, ignoring");
        }
        synchronized (queue) {
            queue.addLast(r);
            queue.notify();
        }
    }

    class Worker extends Thread {
        public void run() {
            Runnable task = null;

            try {
                while (true) {
                    synchronized (queue) {
                        while (queue.isEmpty()) {
                            try {
                                queue.wait();
                            } catch (InterruptedException e) {
                                // ignore
                            }
                            task = (Runnable) queue.removeFirst();
                            try {
                                task.run();
                            } catch (RuntimeException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();  // FIXME
            }
        }
    }

}
