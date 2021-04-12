package info.kgeorgiy.ja.buduschev.concurrent;

import info.kgeorgiy.java.advanced.concurrent.ListIP;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class IterativeParallelism implements ListIP {
    @Override
    public String join(int threads, List<?> values) throws InterruptedException {
        return collectResult(threads, values,
                stream -> stream.map(Object::toString).collect(Collectors.joining()),
                Collectors.joining());
    }

    @Override
    public <T> List<T> filter(int threads, List<? extends T> values, Predicate<? super T> predicate) throws InterruptedException {
        return collectResult(threads, values,
                stream -> stream.collect(Collectors.filtering(predicate, Collectors.toList())),
                Collectors.flatMapping(Collection::stream, Collectors.toList())
        );
    }

    @Override
    public <T, U> List<U> map(int threads, List<? extends T> values, Function<? super T, ? extends U> f) throws InterruptedException {
        return collectResult(threads, values,
                stream -> stream.map(f).collect(Collectors.toList()),
                Collectors.flatMapping(Collection::stream, Collectors.toList()));
    }

    @Override
    public <T> T maximum(int threads, List<? extends T> values, Comparator<? super T> comparator) throws InterruptedException {
        return processTasks(threads, values, stream -> stream.max(comparator).get()).max(comparator).get();
    }

    @Override
    public <T> T minimum(int threads, List<? extends T> values, Comparator<? super T> comparator) throws InterruptedException {
        return processTasks(threads, values, stream -> stream.min(comparator).get()).min(comparator).get();
    }

    @Override
    public <T> boolean all(int threads, List<? extends T> values, Predicate<? super T> predicate) throws InterruptedException {
        return processTasks(threads, values, stream -> stream.allMatch(predicate)).allMatch(Boolean::booleanValue);
    }

    @Override
    public <T> boolean any(int threads, List<? extends T> values, Predicate<? super T> predicate) throws InterruptedException {
        return processTasks(threads, values, stream -> stream.anyMatch(predicate)).anyMatch(Boolean::booleanValue);
    }

    private <T, R> R collectResult(final int countOfThreads, final List<? extends T> list, Function<Stream<? extends T>, ? extends R> f, Collector<? super R, ?, R> collector) {
        return processTasks(countOfThreads, list, f).collect(collector);
    }

    private <T, R> Stream<R> processTasks(final int countOfThreads, final List<? extends T> list, Function<Stream<? extends T>, ? extends R> f) {
        final int countOfSegments = Integer.max(Integer.min(countOfThreads, list.size()), 1);
        final int sizeOfSegment = list.size() / countOfSegments;
        int rest = list.size() % countOfSegments;
        List<R> result = new ArrayList<>();
        List<Thread> threads = new ArrayList<>();
        int start;
        int end = 0;
        for (int i = 0; i < countOfSegments; i++) {
            start = end;
            end += sizeOfSegment + ((rest-- > 0) ? 1 : 0);
            result.add(null);
            final int pos = i;
            final List<? extends T> segment = list.subList(start, end);
            threads.add(new Thread(() -> result.set(pos, f.apply(segment.stream()))));
        }
        for (Thread thread : threads) {
            thread.start();
        }
        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException ignored) {
            }
        }
        return result.stream();
    }
}
