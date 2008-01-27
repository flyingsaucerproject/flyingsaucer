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
