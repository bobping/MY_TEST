package ldaptest;

import javax.net.SocketFactory;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

public class CustomSocketFactory extends SocketFactory {

    public static SocketFactory getDefault() {
        synchronized(CustomSocketFactory.class) {
            return new CustomSocketFactory();
        }

    }

    @Override
    public Socket createSocket(String host, int port) throws IOException {
        Socket socket = new Socket(host, port);
        socket.setSoTimeout(5000);
        socket.setSoLinger(true, 0);
        return socket;
    }

    @Override
    public Socket createSocket(String host, int port, InetAddress localHost, int localPort) throws IOException {
        Socket socket = new Socket(host, port, localHost, localPort);
        socket.setSoLinger(true, 0);
        return socket;
    }

    @Override
    public Socket createSocket(InetAddress host, int port) throws IOException {
        Socket socket = new Socket(host, port);
        socket.setSoLinger(true, 0);
        socket.setSoTimeout(2000);
        return socket;
    }

    @Override
    public Socket createSocket(InetAddress host, int port, InetAddress localHost, int localPort) throws IOException {
        Socket socket = new Socket(host, port, localHost, localPort);
        socket.setSoLinger(true, 0);
        socket.setSoTimeout(2000);
        return socket;
    }
}