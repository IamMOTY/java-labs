package info.kgeorgiy.ja.buduschev.crawler;

import info.kgeorgiy.java.advanced.crawler.*;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.*;
import java.util.concurrent.*;

public class WebCrawler implements Crawler {
    private final Downloader downloader;
    private final int perHost;
    private final ExecutorService extractorPool;
    private final ExecutorService downloaderPool;
    private final ConcurrentMap<String, hostWorker> hostWorkers;

    public WebCrawler(Downloader downloader, int downloaders, int extractors, int perHost) {
        this.downloader = downloader;
        this.downloaderPool = Executors.newFixedThreadPool(downloaders);
        this.extractorPool = Executors.newFixedThreadPool(extractors);
        this.perHost = perHost;
        this.hostWorkers = new ConcurrentHashMap<>();
    }

    // :NOTE: too big method, java.concurrent.util classes?
    @Override
    public Result download(final String url, final int maxDepth) {
        final Phaser phaser = new Phaser(1);
        final Set<String> downloaded = ConcurrentHashMap.newKeySet();
        final Set<String> visited = ConcurrentHashMap.newKeySet();
        final Map<String, IOException> errors = new ConcurrentHashMap<>();
        visited.add(url);
        Queue<String> currentLinks = new ConcurrentLinkedDeque<>();
        currentLinks.add(url);
        for (int depth = 0; depth < maxDepth; depth++) {
            final Queue<String> newLinks = new ConcurrentLinkedDeque<>();
            while (!currentLinks.isEmpty()) {
                final String currentLink = currentLinks.poll();
                String host;
                try {
                    host = URLUtils.getHost(currentLink);
                } catch (MalformedURLException e) {
                    errors.put(currentLink, e);
                    continue;
                }
                final hostWorker worker = hostWorkers.computeIfAbsent(host, (n) -> new hostWorker(perHost));
                phaser.register();
                worker.submit(() -> {
                    try {
                        final Document document = downloader.download(currentLink);
                        downloaded.add(currentLink);
                        phaser.register();
                        extractorPool.submit(() -> {
                            try {
                                final List<String> links = document.extractLinks();
                                for (String link : links) {
                                    if (visited.add(link)) {
                                        newLinks.add(link);
                                    }
                                }
                            } catch (IOException ignored) {
                            } finally {
                                phaser.arriveAndDeregister();
                            }
                        });
                    } catch (IOException e) {
                        errors.put(currentLink, e);
                    } finally {
                        phaser.arriveAndDeregister();
                        worker.runWaiting();
                    }
                });
            }
            phaser.arriveAndAwaitAdvance();
            currentLinks = newLinks;
        }
        return new Result(new ArrayList<>(downloaded), errors);
    }

    @Override
    public void close() {
        shutdownAndAwaitTermination(extractorPool);
        shutdownAndAwaitTermination(downloaderPool);
    }

    void shutdownAndAwaitTermination(ExecutorService pool) {
        pool.shutdown(); // Disable new tasks from being submitted
        try {
            // Wait a while for existing tasks to terminate
            if (!pool.awaitTermination(3, TimeUnit.SECONDS)) {
                pool.shutdownNow(); // Cancel currently executing tasks
                // Wait a while for tasks to respond to being cancelled
                if (!pool.awaitTermination(3, TimeUnit.SECONDS))
                    System.err.println("Pool did not terminate");
            }
        } catch (InterruptedException ie) {
            // (Re-)Cancel if current thread also interrupted
            pool.shutdownNow();
            // Preserve interrupt status
            Thread.currentThread().interrupt();
        }
    }

    private class hostWorker {
        final Queue<Runnable> tasks;
        private int countOfRan;
        private final int perHost;

        hostWorker(int perHost) {
            this.tasks = new ArrayDeque<>();
            countOfRan = 0;
            this.perHost = perHost;
        }

        private synchronized void submit(Runnable task) {
            if (countOfRan >= perHost) {
                tasks.add(task);
            } else {
                countOfRan++;
                downloaderPool.submit(task);
            }
        }

        private synchronized void runWaiting() {
            if (!tasks.isEmpty()) {
                downloaderPool.submit(tasks.poll());
            } else {
                countOfRan--;
            }
        }
    }

    public static void main(String[] args) {
        if (args == null || args.length < 2 || args.length > 5) {
            System.err.println("Arguments count must be in 2..5.");
            return;
        }

        if (Arrays.stream(args).anyMatch(Objects::isNull)) {
            System.err.println("Null found in arguments.");
        }

        String url = args[0];
        int downloaders = 1;
        int extractors = 1;
        int perHost = 1;
        try {
            int depth = Integer.parseInt(args[1]);
            if (args.length > 2) {
                downloaders = Integer.parseInt(args[2]);
            }
            if (args.length > 3) {
                extractors = Integer.parseInt(args[3]);
            }
            if (args.length > 4) {
                perHost = Integer.parseInt(args[4]);
            }

            try (Crawler crawler = new WebCrawler(new CachingDownloader(),
                    downloaders, extractors, perHost)) {
                crawler.download(url, depth);
            } catch (IOException e) {
                System.err.println("Error occur while creation of Downloader: " + e.getLocalizedMessage());
            }

        } catch (NumberFormatException e) {
            System.err.println("Integer values required.");
        }
    }

}