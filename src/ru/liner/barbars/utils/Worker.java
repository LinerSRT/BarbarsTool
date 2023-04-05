package ru.liner.barbars.utils;

public abstract class Worker implements Runnable {
    protected boolean isRunning;
    private Thread workerThread;

    public Worker() {
    }

    public void start() {
        if ((workerThread != null && workerThread.isAlive()) || isRunning)
            return;
        workerThread = new Thread(this);
        workerThread.start();
        isRunning = true;
    }

    public void stop() {
        if (!isRunning)
            return;
        isRunning = false;
        if (workerThread != null)
            workerThread.interrupt();
        try {
            if (workerThread != null)
                workerThread.join();
        } catch (InterruptedException ignored) {
        }
        workerThread = null;
    }


    public boolean isRunning() {
        return isRunning;
    }

    public abstract void process();

    public abstract long delay();


    @Override
    public void run() {
        while (isRunning) {
            process();
            sleep(delay());
        }
    }

    public void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ignored) {

        }
    }
}