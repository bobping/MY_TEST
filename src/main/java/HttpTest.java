import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.SocketConfig;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

public class HttpTest {

    public static void main(String[] args) {

//        // 设置连接超时 的时间
//        PoolingHttpClientConnectionManager httpClientConnectionManager = new PoolingHttpClientConnectionManager();
//        httpClientConnectionManager.setMaxTotal(1);
//        httpClientConnectionManager.setDefaultMaxPerRoute(1);
//
//        httpClientConnectionManager.setValidateAfterInactivity(50000);
//
//        RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(50000)
//                .setConnectionRequestTimeout(50000).setSocketTimeout(50000).build();
//        //设置重定向策略
//        LaxRedirectStrategy redirectStrategy = new LaxRedirectStrategy();
//
//        CloseableHttpClient httpClient = HttpClients.custom().setConnectionManager(httpClientConnectionManager)
//                .setDefaultRequestConfig(requestConfig).setRedirectStrategy(redirectStrategy)
//                .build();

        while (true) {
            try {

                SocketConfig socketConfig = SocketConfig.custom()
                        .setSoLinger(0) // 设置SO_LINGER的超时时间为0毫秒
                        .build();

                        // 设置连接超时 的时间
        PoolingHttpClientConnectionManager httpClientConnectionManager = new PoolingHttpClientConnectionManager();
        httpClientConnectionManager.setMaxTotal(1);
        httpClientConnectionManager.setDefaultMaxPerRoute(1);

        httpClientConnectionManager.setValidateAfterInactivity(50000);

        RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(50000)
                .setConnectionRequestTimeout(50000).setSocketTimeout(50000).build();
        //设置重定向策略
        LaxRedirectStrategy redirectStrategy = new LaxRedirectStrategy();

                        CloseableHttpClient httpClient = HttpClients.custom().setDefaultSocketConfig(socketConfig).setConnectionManager(httpClientConnectionManager)
                .setDefaultRequestConfig(requestConfig).setRedirectStrategy(redirectStrategy)
                .build();

//                CloseableHttpClient httpClient = HttpClients.createDefault();


                // 发送GPOST请求
                HttpPost httpPost = new HttpPost("http://10.45.51.136:8001/simulate/http");
                CloseableHttpResponse responseGet = null;
                responseGet = httpClient.execute(httpPost);
                System.out.println("GET Response Status:: " + responseGet.getStatusLine().getStatusCode());
                String strGet = EntityUtils.toString(responseGet.getEntity());
                System.out.println("GET Response Body:: " + strGet);

                responseGet.close();
                httpClient.close();
                System.out.println("循环  响应：关闭    客户端：关闭");


            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }


    }
}
