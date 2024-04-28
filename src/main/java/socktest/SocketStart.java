package socktest;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SocketStart {
	public static void main(String[] args) throws IOException {

		Socket socket = new Socket();
		socket.connect(new InetSocketAddress("172.16.23.36", 5873), 5000); //设置超时3000毫秒


		ExecutorService exe = Executors.newFixedThreadPool(1);
		AsyncSocketServer epapMock = new AsyncSocketServer( "172.16.23.35", 5873,20);
//		AsyncSocketServer epapMock = new AsyncSocketServer( "192.168.8.129", 5873,2);

		exe.execute(epapMock);
	}

}