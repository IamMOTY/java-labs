package info.kgeorgiy.ja.buduschev.concurrent;

import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.function.Function;
import java.util.stream.IntStream;

public class ParallelMapperImpl implements ParallelMapper {
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
        IntStream.range(0, args.size()).forEach(pos -> {
            try {
                addTask(() -> result.set(pos, f.apply(args.get(pos))));
            } catch (InterruptedException ignored) {
            }
        });
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

