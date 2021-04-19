package info.kgeorgiy.ja.buduschev.concurrent;

import info.kgeorgiy.java.advanced.concurrent.ListIP;
import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class IterativeParallelism implements ListIP {
    private final ParallelMapper mapper;

    public IterativeParallelism(ParallelMapper mapper) {
        this.mapper = mapper;
    }

    public IterativeParallelism() {
        mapper = null;
    }

    @Override
    public String join(int threads, List<?> values) throws InterruptedException {
        return collectResult(threads, values,
                stream -> stream.map(Object::toString).collect(Collectors.joining()),
                Collectors.joining());
    }

    @Override
    public <T> List<T> filter(final int threads, final List<? extends T> values, final Predicate<? super T> predicate) throws InterruptedException {
        return collectResult(threads, values,
                stream -> stream.collect(Collectors.filtering(predicate, Collectors.toList())),
                Collectors.flatMapping(Collection::stream, Collectors.toList())
        );
    }

    @Override
    public <T, U> List<U> map(final int threads, final List<? extends T> values, final Function<? super T, ? extends U> f) throws InterruptedException {
        return collectResult(threads, values,
                stream -> stream.map(f).collect(Collectors.toList()),
                Collectors.flatMapping(Collection::stream, Collectors.toList()));
    }

    @Override
    public <T> T maximum(final int threads, final List<? extends T> values, final Comparator<? super T> comparator) throws InterruptedException {
        return processTasks(threads, values, stream -> stream.max(comparator).orElseThrow()).max(comparator).orElseThrow();
    }

    @Override
    public <T> T minimum(final int threads, final List<? extends T> values, final Comparator<? super T> comparator) throws InterruptedException {
        return maximum(threads, values, comparator.reversed());
    }

    @Override
    public <T> boolean all(final int threads, final List<? extends T> values, final Predicate<? super T> predicate) throws InterruptedException {
        return processTasks(threads, values, stream -> stream.allMatch(predicate)).allMatch(Boolean::booleanValue);
    }

    @Override
    public <T> boolean any(final int threads, final List<? extends T> values, final Predicate<? super T> predicate) throws InterruptedException {
        return !all(threads, values, it -> !predicate.test(it));
    }

    private <T, R> R collectResult(final int countOfThreads, final List<? extends T> list, final Function<Stream<? extends T>, ? extends R> f, final Collector<? super R, ?, R> collector) throws InterruptedException {
        return processTasks(countOfThreads, list, f).collect(collector);
    }

    private <T, R> Stream<R> processTasks(final int countOfThreads, final List<? extends T> list, final Function<Stream<? extends T>, ? extends R> f) throws InterruptedException {
        final int countOfSegments = Integer.min(countOfThreads, list.size());
        final int sizeOfSegment = list.size() / countOfSegments;
        int rest = list.size() % countOfSegments;
        final List<R> result;
        int start;
        int end = 0;
        final List<Stream<? extends T>> segments = new ArrayList<>();
        for (int i = 0; i < countOfSegments; i++) {
            start = end;
            end += sizeOfSegment + ((rest-- > 0) ? 1 : 0);
            segments.add(list.subList(start, end).stream());
        }

        if (mapper != null) {
            result = mapper.map(f, segments);
        } else {
            final List<Thread> threads = new ArrayList<>();
            result = new ArrayList<>();
            for (int i = 0; i < countOfSegments; i++) {
                final int pos = i;
                result.add(null);
                Thread thread = new Thread(() -> result.set(pos, f.apply(segments.get(pos))));
                threads.add(thread);
                thread.start();
            }
            for (Thread thread : threads) {
                try {
                    thread.join();
                } catch (InterruptedException ignored) {
                }
            }
        }


        return result.stream();
    }

}
