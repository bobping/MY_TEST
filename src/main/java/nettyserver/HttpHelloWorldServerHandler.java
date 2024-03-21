package nettyserver;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;


public class HttpHelloWorldServerHandler  extends SimpleChannelInboundHandler<HttpContent> {

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        super.channelRegistered(ctx);
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, HttpContent msg) {

        System.out.println("请求报文是：" + msg.content().toString(io.netty.util.CharsetUtil.UTF_8));

        // 准备给客户端浏览器发送的数据
        ByteBuf byteBuf = Unpooled.copiedBuffer("Hello Client", CharsetUtil.UTF_8);

        // 设置 HTTP 版本, 和 HTTP 的状态码, 返回内容
        DefaultFullHttpResponse defaultFullHttpResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, byteBuf);

        // 设置 HTTP 请求头
        // 设置内容类型是文本类型
        defaultFullHttpResponse.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain");
        // 设置返回内容的长度
        defaultFullHttpResponse.headers().set(
                HttpHeaderNames.CONTENT_LENGTH,
                byteBuf.readableBytes());

        // 写出 HTTP 数据
        ctx.writeAndFlush(defaultFullHttpResponse);

        }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}