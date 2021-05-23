package info.kgeorgiy.ja.buduschev.hello;

import info.kgeorgiy.java.advanced.hello.HelloClient;

import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HelloUDPClient implements HelloClient {
    private final static int SO_TIMEOUT = 50;

    @Override
    public void run(String host, int port, String prefix, int threads, int requests) {
        final SocketAddress serverAddress = new InetSocketAddress(host, port);
        final ExecutorService handlers = Executors.newFixedThreadPool(threads);
        for (int thread = 0; thread < threads; thread++) {
            handleThread(prefix, requests, serverAddress, handlers, thread);
        }
        Util.shutdownAndAwaitTermination(handlers);
    }

    private void handleThread(String prefix, int requests, SocketAddress serverAddress, ExecutorService handlers, int thread) {
        handlers.submit(() -> {
            try (final DatagramSocket socket = new DatagramSocket()) {
                socket.setSoTimeout(SO_TIMEOUT);
                final int sendBufferSize = socket.getSendBufferSize();
                final DatagramPacket packet = new DatagramPacket(new byte[sendBufferSize], sendBufferSize, serverAddress);
                for (int request = 0; request < requests; request++) {
                    handleRequest(prefix, serverAddress, thread, socket, packet, request);
                }
            } catch (SocketException e) {
                System.err.printf("Error occur, while creating socket: %s%n", e.getLocalizedMessage());
            }
        });
    }

    private void handleRequest(String prefix, SocketAddress serverAddress, int thread, DatagramSocket socket, DatagramPacket packet, int request) {
        String message = String.format("%s%d_%d", prefix, thread, request);
        System.out.printf("Sent     ~ %s%n", message);
        while (!makeRequest(socket, packet, message)) { }
    }

    /**
     * @return true if request accepted
     */
    private boolean makeRequest(DatagramSocket socket, DatagramPacket packet, String message) {
        try {
            packet.setData(message.getBytes(StandardCharsets.UTF_8));
            socket.send(packet);
            final int receiveBufferSize = socket.getReceiveBufferSize();
            packet.setData(new byte[receiveBufferSize]);
            socket.receive(packet);
            String response = new String(packet.getData(), packet.getOffset(), packet.getLength(), StandardCharsets.UTF_8);
            if (response.contains(message)) {
                System.out.printf("Received ~ %s%n", response);
                return true;
            }
        } catch (IOException e) {
            System.err.printf("Error occur, while sending request: %s%n", e.getLocalizedMessage());
        }
        return false;
    }

    public static void main(String[] args) {
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
            final HelloClient client = new HelloUDPClient();
            client.run(host, port, prefix, threads, requests);
        } catch (NumberFormatException e) {
            System.err.println("Integer value required!");
        }
    }

}
