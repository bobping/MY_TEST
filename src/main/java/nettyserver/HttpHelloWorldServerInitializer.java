package nettyserver;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.compression.CompressionOptions;
import io.netty.handler.codec.http.HttpContentCompressor;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.HttpServerExpectContinueHandler;
import isaphttpclienttest.TcpStateTest;

public class HttpHelloWorldServerInitializer extends ChannelInitializer<SocketChannel> {

    @Override
    public void initChannel(SocketChannel socketChannel) {
        ChannelPipeline p = socketChannel.pipeline();
        p.addLast(new HttpServerCodec());
        p.addLast(new HttpContentCompressor((CompressionOptions[]) null));
        p.addLast(new HttpServerExpectContinueHandler());
        p.addLast(new TcpStateTest());
    }
} 