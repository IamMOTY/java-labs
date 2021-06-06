package info.kgeorgiy.ja.buduschev.hello;

import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HelloUDPClient extends AbstractClient {

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
                    handleRequest(prefix, thread, socket, packet, request);
                }
            } catch (SocketException e) {
                System.err.printf("Error occur, while creating socket: %s%n", e.getLocalizedMessage());
            }
        });
    }

    private void handleRequest(String prefix, int thread, DatagramSocket socket, DatagramPacket packet, int request) {
        String message = genMessage(prefix, thread, request);
        logSent(message);
        while (!makeRequest(socket, packet, message)) {
        }
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
                logReceived(response);
                return true;
            }
        } catch (IOException e) {
            System.err.printf("Error occur, while sending request: %s%n", e.getLocalizedMessage());
        }
        return false;
    }


    public static void main(String[] args) {
        main(args, HelloUDPClient::new);
    }

}
