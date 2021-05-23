package info.kgeorgiy.ja.buduschev.hello;


import info.kgeorgiy.java.advanced.hello.HelloServer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HelloUDPServer implements HelloServer {
    private DatagramSocket socket;
    private ExecutorService handlers;

    @Override
    public void start(int port, int threads) {
        if (socket != null && !socket.isClosed()) {
            System.err.println("Server is working, try later!");
            return;
        }
        try {
            socket = new DatagramSocket(port);
            final int sendBufferSize = socket.getSendBufferSize();
            handlers = Executors.newFixedThreadPool(threads);
            for (int thread = 0; thread < threads; thread++) {
                handlers.submit(() -> listenPort(sendBufferSize));
            }
        } catch (SocketException e) {
            System.err.printf("Error occur, while creating socket: %s%n", e.getLocalizedMessage());
        }
    }

    private void listenPort(int sendBufferSize) {
        while (!socket.isClosed()) {
            DatagramPacket receivePacket = new DatagramPacket(new byte[sendBufferSize], sendBufferSize);
            try {
                socket.receive(receivePacket);
                final String response = String.format("Hello, %s",
                        new String(receivePacket.getData(),
                                receivePacket.getOffset(),
                                receivePacket.getLength(),
                                StandardCharsets.UTF_8));
                receivePacket.setData(response.getBytes(StandardCharsets.UTF_8));
                socket.send(receivePacket);
            } catch (IOException e) {
                System.err.printf("Error occur, while responding: %s%n", e.getLocalizedMessage());
            }
        }
    }

    @Override
    public void close() {
        socket.close();
        Util.shutdownAndAwaitTermination(handlers);
    }

    public static void main(String[] args) {
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
            try (final HelloServer server = new HelloUDPServer()) {
                server.start(port, threads);
            }
        } catch (NumberFormatException e) {
            System.err.println("Integer required!");
        }
    }
}