package info.kgeorgiy.ja.buduschev.hello;

import info.kgeorgiy.java.advanced.hello.HelloClient;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.Supplier;

public abstract class AbstractClient implements HelloClient {
    protected final static int BUFFER_SIZE = 1024;
    // :NOTE: SOCKET_LIMIT_IN_MILLIS
    protected final static int S_LIMIT = 50;

    protected String genMessage(final String prefix, final int thread, final int request) {
        return String.format("%s%d_%d", prefix, thread, request);
    }

    protected boolean validateResponse(final String response, final String prefix, final int thread, final int request) {
        return response.contains(genMessage(prefix, thread, request));
    }

    protected void logSent(final String message) {
        System.out.printf("Sent     ~ %s%n", message);
    }

    protected void logReceived(final String response) {
        System.out.printf("Received ~ %s%n", response);
    }

    protected static void main(final String[] args, final Supplier<HelloClient> getImp) {
        if (args == null || args.length != 5) {
            System.err.println("Invalid count of arguments!");
            return;
        }
        if (Arrays.stream(args).anyMatch(Objects::isNull)) {
            System.err.println("Null value found in arguments");
            return;
        }
        final String host = args[0];
        try {
            final int port = Integer.parseInt(args[1]);
            final String prefix = args[2];
            final int threads = Integer.parseInt(args[3]);
            final int requests = Integer.parseInt(args[4]);
            final HelloClient client = getImp.get();
            client.run(host, port, prefix, threads, requests);
        } catch (NumberFormatException e) {
            System.err.println("Integer value required!");
        }
    }

}
