package isaphttpclienttest;

import org.apache.http.client.methods.HttpPost;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class TcpStatusTest {
    private static final Logger log = LoggerFactory.getLogger(TcpStatusTest.class);

    public static void reuseHttp(String url, int times){
        int i = 0;
        try {
            SynHttpPoolClient synHttpPoolClient = new SynHttpPoolClient(1, 1,
                    5 * 1000, 1 * 1000, 1 * 1000, 60);
            for (; i < times; i++) {
                long l = System.currentTimeMillis();

                String response = synHttpPoolClient.postSoapLazyAbort(url, "{}",
                        SynHttpPoolClient.CHARSET_UTF8, "", null);

                long cost = (System.currentTimeMillis() - l);
                log.info("new:第" + (i+1) + "次，响应：" + response + "---cost(ms):" + cost);

            }
        }
        catch (Exception e) {
            log.info("", i);
            log.error("", e.getMessage(), e);
        }
    }


    public static void newHttp(String url, int times){
        int i = 0;

        try {
            for (; i < times; i++) {

                SynHttpPoolClient synHttpPoolClient = new SynHttpPoolClient(1, 1,
                        5 * 1000, 1 * 1000, 1 * 1000, 60);

                long l = System.currentTimeMillis();
                String response = synHttpPoolClient.postSoapClose(url, "{}",
                        SynHttpPoolClient.CHARSET_UTF8, "", null);

                long cost = (System.currentTimeMillis() - l);
                log.info("new:第" + (i+1) + "次，响应：" + response + "---cost(ms):" + cost);

            }
        }
        catch (Exception e) {
            log.info("", i);
            log.error("", e.getMessage(), e);
        }
    }

    public static void headerClose(String url, int times){
        int i = 0;
        try {

            HttpPost httpPost = new HttpPost(url);
            httpPost.setHeader("Connection", "close");
            for (; i < times; i++) {

                SynHttpPoolClient synHttpPoolClient = new SynHttpPoolClient(1, 1,
                        5 * 1000, 1 * 1000, 1 * 1000, 60);
                long l = System.currentTimeMillis();

                String response = synHttpPoolClient.postSoapClose(url, "{}",
                        SynHttpPoolClient.CHARSET_UTF8, "", httpPost);

                long cost = (System.currentTimeMillis() - l);

                log.info("headerClose:第" + (i+1) + "次，响应：" + response + "---cost(ms):" + cost);
            }
        }
        catch (Exception e) {
            log.info("", i);
            log.error("", e.getMessage(), e);
        }
    }
}
