package socktest;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.net.UnknownHostException;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

import org.apache.log4j.Logger;

public class AsyncSocketServer implements Runnable {

	private AsynchronousChannelGroup asyncChannelGroup = null;
	private AsynchronousServerSocketChannel asyncServerSocketChannel = null;
	InetSocketAddress socketAddress = null;
	private String host;
	private int port;
	private int maxCon;
	private CompletionHandler<AsynchronousSocketChannel, Void> handler = null;

	Logger logger = Logger.getLogger(AsyncSocketServer.class.getName());

	AsyncSocketServer(String host, int port,int maxCon) {

		this.host = host;
		this.port = port;
		this.maxCon = maxCon;
		try {
//			asyncChannelGroup = AsynchronousChannelGroup.withFixedThreadPool(maxCon, Executors.defaultThreadFactory());

			if (host == null) {
				socketAddress = new InetSocketAddress(InetAddress
						.getLocalHost().getHostName(), port);
			} else {
				socketAddress = new InetSocketAddress(host, port);
			}

			asyncServerSocketChannel = AsynchronousServerSocketChannel.open();
			asyncServerSocketChannel.setOption(StandardSocketOptions.SO_RCVBUF,
					1024 * 1024 * 1024);
			asyncServerSocketChannel.setOption(
					StandardSocketOptions.SO_REUSEADDR, true);
			asyncServerSocketChannel.bind(socketAddress);
            handler = new MINSATMockHandler(asyncServerSocketChannel, maxCon);


		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			logger.error(e);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			logger.error(e);
		}

	}

	public void run() {
		try {
			if (asyncServerSocketChannel.isOpen()) {
				asyncServerSocketChannel.accept(null, handler);
			}
            logger.info( handler.getClass().getName() + ">>>"  + "[" + host + ":"
                    + port + "]" + "已启动，准备接收请求[允许的最大连接数为："+maxCon+"]>>>>>>>>>");
			System.in.read();
		} catch (IOException e) {
			logger.error(e);

		}
	}

	public AsynchronousServerSocketChannel getAsyncServerSocketChannel() {
		return asyncServerSocketChannel;
	}

	public void setAsyncServerSocketChannel(AsynchronousServerSocketChannel asyncServerSocketChannel) {
		this.asyncServerSocketChannel = asyncServerSocketChannel;
	}


}