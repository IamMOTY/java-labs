package info.kgeorgiy.ja.buduschev.concurrent;

import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

import java.util.*;
import java.util.function.Function;

public class ParallelMapperImpl implements ParallelMapper {
    private static final int TASKS_CAPACITY = 128;
    private final List<Thread> threads;
    private final Queue<Runnable> tasks;

    public ParallelMapperImpl(final int thread) {
        threads = new ArrayList<>();
        tasks = new ArrayDeque<>();
        for (int i = 0; i < thread; i++) {
            startThread();
        }
    }

    @Override
    public <T, R> List<R> map(final Function<? super T, ? extends R> f, final List<? extends T> args) throws InterruptedException {
        Results<R> result = new Results<>(args.size());
        for (int i = 0; i < args.size(); i++) {
            final int pos = i;
            addTask(() -> result.set(pos, f.apply(args.get(pos))));
        }
        return result.toList();
    }

    @Override
    public void close() {
        for (Thread thread : threads) {
            thread.interrupt();
        }
        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException ignored) {
            }
        }
    }

    private void startThread() {
        Thread thread = new Thread(() -> {
            try {
                while (!Thread.interrupted()) {
                    runTask();
                }
            } catch (InterruptedException ignored) {
            } finally {
                Thread.currentThread().interrupt();
            }
        });
        threads.add(thread);
        thread.start();
    }

    private void addTask(final Runnable task) throws InterruptedException {
        synchronized (tasks) {
            while (tasks.size() == TASKS_CAPACITY) {
                tasks.wait();
            }
            tasks.add(task);
            tasks.notifyAll();
        }
    }

    private void runTask() throws InterruptedException {
        Runnable task;
        synchronized (tasks) {
            while (tasks.isEmpty()) {
                tasks.wait();
            }
            task = tasks.poll();
        }
        task.run();
    }


    private static class Results<T> {
        private final List<T> result;
        private int counter;

        public Results(final int size) {
            result = new ArrayList<>();
            for (int i = 0; i < size; i++) {
                result.add(null);
            }
        }

        private void set(final int pos, final T value) {
            result.set(pos, value);
            notifySucceed();
        }

        private synchronized void notifySucceed() {
            if (++counter == result.size()) {
                notify();
            }
        }

        private synchronized List<T> toList() throws InterruptedException {
            while (counter < result.size()) {
                wait();
            }
            return result;
        }
    }
}

