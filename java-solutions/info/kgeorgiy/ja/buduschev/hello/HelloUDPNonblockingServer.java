package info.kgeorgiy.ja.buduschev.hello;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HelloUDPNonblockingServer extends AbstractServer {

    private Selector selector;
    private DatagramChannel serverChannel;
    private ExecutorService pool;
    private final ConcurrentLinkedQueue<Client> readyToSend = new ConcurrentLinkedQueue<>();

    private static class Client {
        private final ByteBuffer buffer;
        private final SocketAddress socketAddress;

        public Client(ByteBuffer buffer, SocketAddress socketAddress) {
            this.buffer = buffer;
            this.socketAddress = socketAddress;
        }

        public ByteBuffer getBuffer() {
            return buffer;
        }

        public SocketAddress getSocketAddress() {
            return socketAddress;
        }
    }

    @Override
    public void start(int port, int threads) {
        try {
            pool = Executors.newFixedThreadPool(threads + 1);
            selector = Selector.open();
            serverChannel = DatagramChannel.open();
            serverChannel.configureBlocking(false);
            serverChannel.bind(new InetSocketAddress(port));
            serverChannel.register(selector, SelectionKey.OP_READ);
            pool.submit(this::listenPort);
        } catch (IOException e) {
            System.err.printf("Server didn't start cause: %s %s%n", e.getClass().getSimpleName(), e.getLocalizedMessage());
        }
    }

    private void listenPort() {
        while (serverChannel.isOpen()) {
            try {
                selector.selectNow(key -> {
                    if (key.isReadable()) {
                        try {
                            ByteBuffer byteBuffer = ByteBuffer.allocate(BUFFER_SIZE);
                            SocketAddress socketAddress = serverChannel.receive(byteBuffer);
                            if (socketAddress != null) {
                                pool.submit(() -> handleRequest(key, byteBuffer, socketAddress));
                            }
                        } catch (IOException e) {
                            System.err.printf("Error while receiving: %s%n", e.getLocalizedMessage());
                        }
                    }
                    if (key.isWritable()) {
                        final Client toSend = readyToSend.remove();
                        if (readyToSend.isEmpty()) {
                            key.interestOps(SelectionKey.OP_READ);
                        }
                        try {
                            serverChannel.send(toSend.getBuffer(), toSend.getSocketAddress());
                        } catch (IOException e) {
                            System.err.printf("Error while responding: %s%n", e.getLocalizedMessage());
                        }
                        key.interestOpsOr(SelectionKey.OP_READ);
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void handleRequest(SelectionKey key, ByteBuffer byteBuffer, SocketAddress socketAddress) {
        byteBuffer.flip();
        String respond = genResponse(StandardCharsets.UTF_8.decode(byteBuffer).toString());
        readyToSend.add(new Client(ByteBuffer.wrap(respond.getBytes(StandardCharsets.UTF_8)), socketAddress));
        key.interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
    }


    @Override
    public void close() {
        try {
            selector.close();
        } catch (IOException e) {
            System.err.printf("Error occur while closing selector: %s%n", e.getLocalizedMessage());
        }
        try {
            serverChannel.close();
        } catch (IOException e) {
            System.err.printf("Error occur while closing channel: %s%n", e.getLocalizedMessage());
        }
        Util.shutdownAndAwaitTermination(pool);
    }
}
