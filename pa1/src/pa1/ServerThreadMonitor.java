/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pa1;

/**
 *
 * @author BBoss
 */
public class ServerThreadMonitor {

    private int threads;

    public ServerThreadMonitor() {
        threads = 0;
    }

    synchronized int numThreads() {
        return threads;
    }

    synchronized void startNewThread() {
        threads++;
    }

    synchronized void endThread() {
        threads--;
        notifyAll();
    }

    synchronized void allThreadsComplete() {
        while (threads > 0) {
            try {
                wait();
            } catch (InterruptedException e) {
                System.err.println("Error waiting on thread.");
            }
        }
    }
}
