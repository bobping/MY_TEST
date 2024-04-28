//package socktest;
//
//import java.io.IOException;
//import java.net.InetSocketAddress;
//import java.nio.channels.AsynchronousServerSocketChannel;
//import java.nio.channels.AsynchronousSocketChannel;
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
//
//public class AsynchronousSocketServer {
//
//    private static final int PORT = 9999;
//    private static final int MAX_CONNECTIONS = 10;
//    private static ExecutorService executor = Executors.newFixedThreadPool(MAX_CONNECTIONS);
//
//    public static void main(String[] args) throws IOException {
//        AsynchronousServerSocketChannel server = AsynchronousServerSocketChannel.open();
//        server.bind(new InetSocketAddress(PORT));
//        System.out.println("Server started on port " + PORT);
//
//        while (true) {
//            if (executor.isShutdown()) {
//                break;
//            }
//
//            server.accept(null, new ConnectionHandler(server));
//        }
//    }
//
//    static class ConnectionHandler implements java.nio.channels.CompletionHandler<AsynchronousSocketChannel, Void> {
//        private AsynchronousServerSocketChannel server;
//
//        public ConnectionHandler(AsynchronousServerSocketChannel server) {
//            this.server = server;
//        }
//
//        @Override
//        public void completed(AsynchronousSocketChannel channel, Void attachment) {
//            server.accept(null, this);
//            if (executor.isShutdown() || executor..size() >= MAX_CONNECTIONS) {
//                try {
//                    channel.close();
//                    System.out.println("Connection rejected - server at max capacity");
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            } else {
//                executor.submit(new ClientHandler(channel));
//            }
//        }
//
//        @Override
//        public void failed(Throwable exc, Void attachment) {
//            System.out.println("Connection failed: " + exc.getMessage());
//        }
//    }
//
//    static class ClientHandler implements Runnable {
//        private AsynchronousSocketChannel channel;
//
//        public ClientHandler(AsynchronousSocketChannel channel) {
//            this.channel = channel;
//        }
//
//        @Override
//        public void run() {
//            // Handle client connection here
//        }
//    }
//}