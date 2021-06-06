package info.kgeorgiy.ja.buduschev.hello;


import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;

public class HelloUDPNonblockingClient extends AbstractClient {

    private static class Request {
        private final int thread;
        private int request;

        public Request(int thread) {
            this.thread = thread;
            request = 0;
        }

        public void inc() {
            ++request;
        }

        public int getThread() {
            return thread;
        }

        public int getRequest() {
            return request;
        }
    }

    @Override
    public void run(final String host, final int port, final String prefix, final int threads, final int requests) {
        try (final Selector selector = Selector.open()) {
            final SocketAddress serverAddress = new InetSocketAddress(host, port);
            for (int thread = 0; thread < threads; thread++) {
                try {
                    DatagramChannel datagramChannel = DatagramChannel.open();
                    datagramChannel.configureBlocking(false);
                    datagramChannel.connect(serverAddress);
                    datagramChannel.register(selector, SelectionKey.OP_WRITE, new Request(thread));
                } catch (final IOException e) {
                    System.err.println("Channel creation error: " + e.getLocalizedMessage());
                    return;
                }
            }
            while (!selector.keys().isEmpty()) {
                try {
                    selector.select(S_LIMIT);
                    if (selector.selectedKeys().isEmpty()) {
                        for (final SelectionKey key : selector.keys()) {
                            key.interestOps(SelectionKey.OP_WRITE);
                        }
                    }
                    for (final Iterator<SelectionKey> i = selector.selectedKeys().iterator(); i.hasNext(); ) {
                        handleKey(prefix, requests, serverAddress, i.next());
                        i.remove();
                    }
                } catch (final IOException e) {
                    System.err.println("Error while selection:" + e.getLocalizedMessage());
                }
            }
        } catch (final IOException e) {
            System.err.println("Selector creation error: " + e.getLocalizedMessage());
        }
    }

    private void handleKey(final String prefix, final int requests, final SocketAddress serverAddress, SelectionKey key) {
        if (key.isWritable()) {
            send(prefix, serverAddress, key);
        }
        if (key.isReadable()) {
            receive(prefix, requests, key);
        }
    }

    private void send(final String prefix, final SocketAddress serverAddress, final SelectionKey key) {
        final Request request = (Request) key.attachment();
        final String message = String.format("%s%d_%d", prefix, request.getThread(), request.getRequest());
        logSent(message);
        final DatagramChannel datagramChannel = (DatagramChannel) key.channel();
        final ByteBuffer byteBuffer = ByteBuffer.allocate(BUFFER_SIZE);
        byteBuffer.put(message.getBytes(StandardCharsets.UTF_8));
        byteBuffer.flip();
        try {
            datagramChannel.send(byteBuffer, serverAddress);
        } catch (IOException e) {
            System.err.println("Error while sending message: " + e.getLocalizedMessage());
        }
        key.interestOps(SelectionKey.OP_READ);
    }

    private void receive(final String prefix, final int requests, final SelectionKey key) {
        final ByteBuffer byteBuffer = ByteBuffer.allocate(BUFFER_SIZE);
        final DatagramChannel datagramChannel = (DatagramChannel) key.channel();
        try {
            datagramChannel.receive(byteBuffer);
            final String response = new String(byteBuffer.array(), StandardCharsets.UTF_8);
            Request request = (Request) key.attachment();
            if (validateResponse(response, prefix, request.getThread(), request.getRequest())) {
                logReceived(response);
                if (request.getRequest() + 1 < requests) {
                    request.inc();
                    key.interestOps(SelectionKey.OP_WRITE);
                } else {
                    try {
                        key.channel().close();
                    } catch (IOException e) {
                        System.err.println("Error while closing channel: " + e.getLocalizedMessage());
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error while receiving: " + e.getLocalizedMessage());
        }
    }

    public static void main(String[] args) {
        main(args, HelloUDPNonblockingClient::new);
    }
}