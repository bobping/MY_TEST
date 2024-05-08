package socktest;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.ExecutionException;


public class MINSATMockHandler implements CompletionHandler<AsynchronousSocketChannel, Void> {


	private static final Logger logger = Logger.getLogger(MINSATMockHandler.class);

	AsynchronousServerSocketChannel asynchronousServerSocketChannel = null;
	private int maxCon;
	private static int connectionCount = 0;

	public MINSATMockHandler(AsynchronousServerSocketChannel asynchronousServerSocketChannel, int maxCon) {
		this.asynchronousServerSocketChannel = asynchronousServerSocketChannel;
		this.maxCon = maxCon;
	}

	@Override
	public void completed(AsynchronousSocketChannel asynchSocketChannel, Void attachment) {

		connectionCount++;
		asynchronousServerSocketChannel.accept(null, this);
		ByteBuffer inbuffer = ByteBuffer.allocate(1024 * 64);
		ByteBuffer outbuffer = ByteBuffer.allocate(256);
		StringBuilder msgStr = new StringBuilder();

		try {
			// 超出请求拒绝服务
			if(connectionCount > maxCon ) {
				outbuffer = ByteBuffer.wrap("Connection refused for maximum number of connections exceeded!".getBytes("utf-8"));
				logger.warn("连接数超出最大限定值，拒绝服务，最大连接数为：" + maxCon);
				while(asynchSocketChannel.write(outbuffer).get()>0){
					logger.info("正在返回响应...........");
				}

				return;
			}
			logger.info("Incoming connection from: " + asynchSocketChannel.getRemoteAddress());
			while (asynchSocketChannel.read(inbuffer).get() != -1) {
				
				inbuffer.flip();

				byte[] in = new byte[inbuffer.limit()];

				inbuffer.get(in);

				msgStr = msgStr.append(new String(in,"utf-8"));

				if (msgStr.toString().endsWith("\r\n")) {
					logger.info("当前连接数：" + connectionCount + "最大连接数：" + maxCon);
					logger.info("根据回车符号结尾完成请求接收，请求体为：" + msgStr.toString());

					if (msgStr.toString().startsWith("LOGIN")) { //登录
						logger.info(">>>>>>>>>>受理LOGIN请求：" + msgStr);
						String response = "RESP:0\r\n";
						outbuffer = ByteBuffer.wrap(response.getBytes("utf-8"));
						while(asynchSocketChannel.write(outbuffer).get()>0){
							logger.info("<<<<<<<<<<正在LOGIN响应:" + response);
						}
					}
					else if (msgStr.toString().startsWith("LOGOUT")) { //登出
						logger.info(">>>>>>>>>>受理LOGOUT请求：" + msgStr);
						outbuffer = ByteBuffer.wrap("Logout successfully!\r\n".getBytes("utf-8"));
						while(asynchSocketChannel.write(outbuffer).get()>0){
							logger.info("<<<<<<<<<<正在LOGOUT响应");
						}
						return;
					}
					else if (msgStr.toString().startsWith("CREATE")) { //业务指令
						logger.info(">>>>>>>>>>受理指令请求：" + msgStr);
						String response = "RESP:0\r\n";
						outbuffer = ByteBuffer.wrap(response.getBytes("utf-8"));
						while(asynchSocketChannel.write(outbuffer).get()>0){
							logger.info("<<<<<<<<<<正在指令响应: " + response);
						}
					}

					msgStr.setLength(0);

				}
				
				inbuffer.clear();
				outbuffer.clear();

				

			}
		} catch (IOException | InterruptedException | ExecutionException ex) {
			logger.error(ex);
		} catch (Exception e) {
			logger.error(e);
		} finally {
			try {
				SocketAddress remote = asynchSocketChannel.getRemoteAddress();
				asynchSocketChannel.close();
				connectionCount--;
				logger.warn("远程服务器:" + remote + "连接关闭<<<<<<<<");
			} catch (IOException e) {
				logger.error(e);
			}
		}

	}

	@Override
	public void failed(Throwable exc, Void attachment) {
		// TODO Auto-generated method stub
//		asynchronousServerSocketChannel.accept(null, this);

		throw new UnsupportedOperationException("Cannot accept connections!");
	}
	


}