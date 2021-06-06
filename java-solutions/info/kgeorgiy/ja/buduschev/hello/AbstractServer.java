package info.kgeorgiy.ja.buduschev.hello;

import info.kgeorgiy.java.advanced.hello.HelloServer;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.Supplier;

public abstract class AbstractServer implements HelloServer {
    protected final static int BUFFER_SIZE = 1024;

    protected String genResponse(final String message) {
        return String.format("Hello, %s", message);
    }


    protected static void main(String[] args, final Supplier<HelloServer> getIns) {
        if (args == null || args.length != 2) {
            System.err.println("Invalid count of arguments!");
            return;
        }
        if (Arrays.stream(args).anyMatch(Objects::isNull)) {
            System.err.println("Null value found in arguments");
            return;
        }
        try {
            final int port = Integer.parseInt(args[0]);
            final int threads = Integer.parseInt(args[1]);
            try (final HelloServer server = getIns.get()) {
                server.start(port, threads);
            }
        } catch (NumberFormatException e) {
            System.err.println("Integer required!");
        }
    }
}
