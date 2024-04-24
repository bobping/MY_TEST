package isaphttpclienttest;

import com.alibaba.fastjson.JSONObject;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import org.apache.http.client.methods.HttpPost;
import org.apache.rocketmq.shaded.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


public class TcpStateTest extends SimpleChannelInboundHandler<HttpContent> {
    private static final Logger log = LoggerFactory.getLogger(TcpStateTest.class);

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        super.channelRegistered(ctx);
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, HttpContent msg) throws IOException {


        String content = msg.content().toString(io.netty.util.CharsetUtil.UTF_8);
        log.info("请求报文是：" + content);


        this.testTcpState(content);

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

    public String testTcpState( String content) throws IOException {
        JSONObject jsonObject = JSONObject.parseObject(content);

        //String url = "http://172.16.24.63:7788/simulate/http";
        String url = "http://10.45.51.136:8001/simulate/http";
        String url1 = jsonObject.getString("url");
        if (StringUtils.isNotBlank(url1)) {
            url = url1;
        }

        //String cookie = jsonObject.getString("cookie");
        String mode = jsonObject.getString("mode");
        int times = 0;
        Object o = jsonObject.get("times");
        if (o instanceof String) {
            times = Integer.parseInt((String) o);
        }
        else if (o instanceof Integer) {
            times = (Integer) o;
        }
        else {
            return "times not found";
        }

        int i = 0;
        if (mode.equals("reuse")) {
            try {
                SynHttpPoolClient synHttpPoolClient = new SynHttpPoolClient(1, 1,
                        5 * 1000, 1 * 1000, 1 * 1000, 60);

                for (; i < times; i++) {
                    long l = System.currentTimeMillis();

                    String repsonse = synHttpPoolClient.postSoapLazyAbort(url, "{}",
                            SynHttpPoolClient.CHARSET_UTF8, "", null);

                    long cost = (System.currentTimeMillis() - l);
                    log.info("new:第" + (i+1) + "次，响应：" + repsonse + "---cost(ms):" + cost);

                }
            }
            catch (Exception e) {
                log.info("", i);
                log.error("", e.getMessage(), e);
            }
        }
        else if (mode.equals("new")) {
            try {
                for (; i < times; i++) {

                    SynHttpPoolClient synHttpPoolClient = new SynHttpPoolClient(1, 1,
                            5 * 1000, 1 * 1000, 1 * 1000, 60);

                    long l = System.currentTimeMillis();
                    String repsonse = synHttpPoolClient.postSoapClose(url, "{}",
                            SynHttpPoolClient.CHARSET_UTF8, "", null);

                    long cost = (System.currentTimeMillis() - l);
                    log.info("new:第" + (i+1) + "次，响应：" + repsonse + "---cost(ms):" + cost);

                }
            }
            catch (Exception e) {
                log.info("", i);
                log.error("", e.getMessage(), e);
            }
        }
        else if (mode.equals("keepalive")) {
            try {
                SynHttpPoolClient synHttpPoolClient = new SynHttpPoolClient(1, 1,
                        5 * 1000, 1 * 1000, 1 * 1000, 10);
                HttpPost httpPost = new HttpPost(url);
                httpPost.setHeader("Connection", "close");
                for (; i < times; i++) {
                    long l = System.currentTimeMillis();

                    String repsonse = synHttpPoolClient.postSoapLazyAbort(url, "{}",
                            SynHttpPoolClient.CHARSET_UTF8, "", httpPost);

                    long cost = (System.currentTimeMillis() - l);

                    log.info("keepalive:第" + (i+1) + "次，响应：" + repsonse + "---cost(ms):" + cost);
                }
            }
            catch (Exception e) {
                log.info("", i);
                log.error("", e.getMessage(), e);
            }
        }
        else if (mode.equals("urlConnectTest")) {
            HttpURLConnectTest test = new HttpURLConnectTest();
            Map<String, Object> headMap = new HashMap<>();
            headMap.put("Content-Type", "text/xml;charset=UTF-8");
            headMap.put("SOAPAction", "Notification");
            headMap.put("Connection", "");
            headMap.put("Host", "10.45.51.136:8001");
            test.postWriteWithHeadVPCRF(url, 10000, 10000, headMap);
        }

        return null;
    }


}