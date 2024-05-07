package isaphttpclienttest;

import org.apache.commons.codec.Charsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

public class HttpURLConnectTest {
    private static final Logger logger = LoggerFactory.getLogger(HttpURLConnectTest.class);

    public static void main(String[] args) throws IOException {
        HttpURLConnectTest test = new HttpURLConnectTest();

        for (int i = 0; i<10; i++) {

            new Thread(new Runnable() {
                @Override
                public void run() {
                    while (true) {
                        try {
                            test.postWriteWithHeadVPCRF("http://172.16.23.35:8600/epsmock", 2000, 3000);
                            Thread.sleep(10);
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                    }
                }
            }).start();


        }


    }

    public String postWriteWithHeadVPCRF(String url, int connectTimeout, int readTimeout) throws IOException {

        Map<String, Object> headMap = new HashMap<>();
        headMap.put("Content-Type", "text/xml;charset=UTF-8");
        headMap.put("SOAPAction", "Notification");
        headMap.put("Connection", "");
        headMap.put("Host", "10.45.51.136:8001");

        HttpURLConnection connection = null;
        OutputStream out = null;
        InputStream in = null;
        BufferedReader br = null;
        StringBuffer result = new StringBuffer();
        try {
            URL u = new URL(url);
            connection = (HttpURLConnection) u.openConnection();
            connection.setRequestMethod("POST");
            connection.setConnectTimeout(connectTimeout);
            connection.setReadTimeout(readTimeout);
            connection.setDoOutput(true);
            connection.setInstanceFollowRedirects(false);
            for (Map.Entry<String, Object> entry : headMap.entrySet()) {
                connection.setRequestProperty(entry.getKey(), (String) entry.getValue());
            }

            out = connection.getOutputStream();
            out.write("service command".getBytes(Charsets.UTF_8));
            out.flush();

            in = connection.getInputStream();
            int length;
            byte[] data = new byte[1024];
            while ((length = in.read(data)) != -1) {
                String readMsg = new String(data, 0, length, Charset.forName("utf-8"));
                result.append(readMsg);
            }
            logger.info("服务端响应：" + connection.getResponseCode());
            logger.info("发指令请求后结果：" + result);

            out.write("logout command".getBytes("UTF-8"));
            out.flush();
            byte[] data2 = new byte[1024];

            StringBuilder sb2 = new StringBuilder();
            int length2;
            while ((length2 = in.read(data2)) != -1) {
                String readMsg = new String(data2, 0, length2, Charset.forName("utf-8"));
                sb2.append(readMsg);
            }
            logger.info("发送登出指令后结果:" + sb2);
        }
        catch (IOException e) {
            logger.error("HttpURLConnectionClient error.\n" + e);
            throw e;
        }
        finally {
            if (in != null) {
                in.close();
            }
            if (out != null) {
                out.close();
            }
            if (br != null) {
                br.close();
            }
            if (connection != null) {
                connection.disconnect();
            }
            in = null;
            out = null;
            br = null;
            connection = null;
        }

        return result.toString();
    }


}
