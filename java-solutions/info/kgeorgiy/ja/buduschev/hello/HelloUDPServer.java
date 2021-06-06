package info.kgeorgiy.ja.buduschev.hello;


import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HelloUDPServer extends AbstractServer {
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
                final String response = genResponse(
                        new String(
                                receivePacket.getData(),
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
        main(args, HelloUDPServer::new);
    }
}