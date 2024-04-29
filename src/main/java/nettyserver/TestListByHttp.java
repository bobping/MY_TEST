package nettyserver;

import com.alibaba.fastjson.JSONObject;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import isaphttpclienttest.HttpURLConnectTest;
import isaphttpclienttest.SynHttpPoolClient;
import isaphttpclienttest.TcpStatusTest;
import org.apache.http.client.methods.HttpPost;
import org.apache.rocketmq.shaded.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


public class TestListByHttp extends SimpleChannelInboundHandler<HttpContent> {

    private static final Logger log = LoggerFactory.getLogger(TestListByHttp.class);

    public void testList( String content) throws IOException {
        JSONObject jsonObject = JSONObject.parseObject(content);
        //String url = "http://172.16.24.63:7788/simulate/http";
        String url = "http://10.45.51.136:8001/simulate/http";
        String url1 = jsonObject.getString("url");
        if (StringUtils.isNotBlank(url1)) {
            url = url1;
        }

        String mode = jsonObject.getString("mode");
        int times = (int) jsonObject.get("times");

        if (mode.equals("reuse")) {
            TcpStatusTest.reuseHttp(url, times);
        }
        else if (mode.equals("new")) {
            TcpStatusTest.newHttp(url,times);
        }
        else if (mode.equals("headerClose")) {
            TcpStatusTest.headerClose(url, times);
        }
        else if (mode.equals("HttpURLConnectTest")) {
            HttpURLConnectTest test = new HttpURLConnectTest();
            test.postWriteWithHeadVPCRF(url, 10000, 10000);
        }

    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, HttpContent msg) throws IOException {

        String content = msg.content().toString(CharsetUtil.UTF_8);
        log.info("请求报文是：" + content);

        this.testList(content);

        // 准备给客户端浏览器发送的数据
        ByteBuf byteBuf = Unpooled.copiedBuffer("Ok", CharsetUtil.UTF_8);

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
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        super.channelRegistered(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }




}