package com.epam.rd.autotasks;

import java.util.ArrayList;
import java.util.List;

public class ThreadUnionImpl implements ThreadUnion {
    private String name;
    private int totalSize;
    private List<Thread> threads = new ArrayList<>();
    private boolean isShutdown;
    protected List<FinishedThreadResult> finished = new ArrayList<>();

    public ThreadUnionImpl(String name) {
        this.name = name;
    }

    @Override
    public int totalSize() {
        return totalSize;
    }

    @Override
    public int activeSize() {
        return (int) threads.stream().filter(Thread::isAlive).count();
    }

    @Override
    public void shutdown() {
        threads.forEach(Thread::interrupt);
        isShutdown = true;
    }

    @Override
    public boolean isShutdown() {
        return isShutdown;
    }

    @Override
    public void awaitTermination() {
        for (Thread thread : threads) {
            try {
                if (thread.isAlive()) {
                    thread.join();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean isFinished() {
        return isShutdown && activeSize() == 0;
    }

    @Override
    public List<FinishedThreadResult> results() {
        return finished;
    }

    @Override
    public synchronized Thread newThread(Runnable r) {
        if (isShutdown) {
            throw new IllegalStateException("Thread Union is shutdown");
        }
        Thread thread = new Thread(r) {
            @Override
            public void run() {
                super.run();
                finished.add(new FinishedThreadResult(this.getName()));
            }
        };
        thread.setName(String.format("%s-worker-%d", name, totalSize++));
        thread.setUncaughtExceptionHandler((t, e) -> finished.add(new FinishedThreadResult(t.getName(), e)));
        threads.add(thread);
        return thread;
    }
}
