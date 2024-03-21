package isaphttpclienttest;


import java.io.IOException;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections.MapUtils;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.config.SocketConfig;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.AbstractHttpMessage;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.rocketmq.shaded.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.SocketTimeoutException;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;


public class SynHttpPoolClient {

    public static final String CHARSET_UTF8 = "UTF-8";
    private static Logger logger = LoggerFactory.getLogger(SynHttpPoolClient.class);
    private CloseableHttpClient httpClient;

//    //将最大连接数增加到
//    public static final int MAX_TOTAL = Integer.parseInt(ISAPConfigProviderSingleton.getInstance()
//            .getNodeValue(ISAPConfigure.HTTP_MAX_TOTAL));
//    //将每个路由基础的连接增加到
//    public static final int MAX_ROUTE_TOTAL = Integer.parseInt(ISAPConfigProviderSingleton.getInstance()
//            .getNodeValue(ISAPConfigure.HTTP_MAX_ROUTE_TOTAL));
//    public static final int SOCKET_TIME = Integer.parseInt(ISAPConfigProviderSingleton.getInstance()
//            .getNodeValue(ISAPConfigure.HTTP_SOCKET_TIME));
//    public static final int CONN_TIMEOUT = Integer.parseInt(ISAPConfigProviderSingleton.getInstance()
//            .getNodeValue(ISAPConfigure.HTTP_CONN_TIMEOUT));
//    public static final int CONN_REQUEST_TIMEOUT = Integer.parseInt(ISAPConfigProviderSingleton.getInstance()
//            .getNodeValue(ISAPConfigure.HTTP_CONN_REQUEST_TIMEOUT));

    /**
     * 默认连接池，只有一个连接
     */
    public SynHttpPoolClient() {
        // 设置连接超时 的时间
        PoolingHttpClientConnectionManager httpClientConnectionManager = new PoolingHttpClientConnectionManager();
        httpClientConnectionManager.setMaxTotal(1);
        httpClientConnectionManager.setDefaultMaxPerRoute(1);
        httpClientConnectionManager.setDefaultSocketConfig(SocketConfig.custom().setSoLinger(0).build());

        httpClientConnectionManager.setValidateAfterInactivity(50000);

        RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(50000)
                .setConnectionRequestTimeout(50000).setSocketTimeout(50000).build();
        //设置重定向策略
        LaxRedirectStrategy redirectStrategy = new LaxRedirectStrategy();

        httpClient = HttpClients.custom().setConnectionManager(httpClientConnectionManager)
                .setDefaultRequestConfig(requestConfig).setRedirectStrategy(redirectStrategy)
                .build();
    }

    /**
     * 自定义连接池配置
     */
    public SynHttpPoolClient(int maxTotal, int maxRouteTotal, int soTimeout, int connTimeout,
            int connRequestTimeout, int keepAliveTimeout) {
        // 设置连接超时 的时间
        PoolingHttpClientConnectionManager httpClientConnectionManager = new PoolingHttpClientConnectionManager();
        httpClientConnectionManager.setMaxTotal(maxTotal);
        httpClientConnectionManager.setDefaultMaxPerRoute(maxRouteTotal);
        httpClientConnectionManager.setDefaultSocketConfig(SocketConfig.custom().setSoLinger(0).build());

        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(connTimeout)/*客户端和服务器建立连接的 timeout*/
                .setConnectionRequestTimeout(connRequestTimeout)/*从连接池获取连接的 timeout*/
                .setSocketTimeout(soTimeout)/*客户端和服务器建立连接后，客户端等待服务器返回数据的 timeout*/
                .build();
        //设置重定向策略
        LaxRedirectStrategy redirectStrategy = new LaxRedirectStrategy();

        httpClient = HttpClients.custom().setConnectionManager(httpClientConnectionManager)
                .setDefaultRequestConfig(requestConfig).setRedirectStrategy(redirectStrategy)
                .setKeepAliveStrategy(((response, context) -> Duration.ofSeconds(keepAliveTimeout).toMillis())) // 设置keep-alive策略为30秒
                .build();

    }


    public String post(String url, String content, String charset) throws NetworkException {
        return post(url, content, charset, null);
    }

    public String post(String url, String content, String charset, HttpPost httpPost)
            throws NetworkException {
        try {
            if (httpPost == null) {
                httpPost = new HttpPost(url);
                httpPost.setHeader("SOAPAction", "Notification");
                httpPost.setHeader("Content-Type", "text/xml; charset=UTF-8");
            }
            if (StringUtils.isNotEmpty(content)) {
                StringEntity contentEntity = new StringEntity(content, charset);
                httpPost.setEntity(contentEntity);
            }

            CloseableHttpResponse response = httpClient.execute(httpPost);
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != 200) {
                httpPost.abort();
                logger.error("Exception send request" + ",url=" + url + ",statusCode=" + statusCode
                        + ",content=" + content);
            }
            HttpEntity httpEntity = response.getEntity();
            String result = null;
            if (httpEntity != null) {
                result = EntityUtils.toString(httpEntity, charset);
            }
            EntityUtils.consume(httpEntity);
            response.close();
            return result;
        }
        catch (Exception ex) {
            logger.error("doPost failed:", ex);
            throw new NetworkException(ex);
        }
    }

    public String postByForm(String url, String content, String charset) throws NetworkException {
        return postByForm(url, content, charset, null);
    }

    public String getByForm(String url, String content, String charset, HttpGet httpRequest) throws NetworkException {
        try {
            if (httpRequest == null) {
                if (StringUtils.isNotEmpty(content)) {
                    url = url + "?" + content;
                }
                httpRequest = new HttpGet(url);
                httpRequest.addHeader("Content-Type", "application/x-www-form-urlencoded");
            }

            CloseableHttpResponse response = httpClient.execute(httpRequest);
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != 200) {
                logger.error("Exception send request" + ",url=" + url + ",statusCode=" + statusCode
                        + ",content=" + content);
            }
            HttpEntity httpEntity = response.getEntity();
            String result = null;
            if (httpEntity != null) {
                result = EntityUtils.toString(httpEntity, charset);
            }
            EntityUtils.consume(httpEntity);
            response.close();
            return result;
        }
        catch (Exception ex) {
            logger.error("doPost failed:", ex);
            throw new NetworkException(ex);
        }
        finally {
            try {
                httpRequest.abort();
            }
            catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    /**
     * 通过表格的方式提交的方式，参数格式是a=1&b=2&c=3，不能用?拼接到URL中
     * 要留意参数中是否有转义字符，如果是&，界面传递时要变为%26
     * @param url
     * @param content
     * @param charset
     * @param httpPost
     * @return java.lang.String
     * @throws
     */
    public String postByForm(String url, String content, String charset, HttpPost httpPost) throws NetworkException {
        CloseableHttpResponse response = null;
        try {
            if (httpPost == null) {
                httpPost = new HttpPost(url);
                httpPost.addHeader("Content-Type", "application/x-www-form-urlencoded");
            }
            if (StringUtils.isNotEmpty(content)) {
                List<BasicNameValuePair> list = new LinkedList<>();
                Map<String, String> paramMap = new HashMap<>();
                String[] paraStr = content.split("&");
                for (String para : paraStr) {
                    String[] nameValue = para.split("=");
                    paramMap.put(nameValue[0], nameValue.length > 1 ? nameValue[1] : "");
                }
                paramMap.forEach((k, v) -> {
                    if (v.contains("%26")) {
                        v = v.replaceAll("%26", "&");
                    }
                    if (v.contains("\n")) {
                        v = v.substring(0, v.length() - 1);
                    }
                    list.add(new BasicNameValuePair(k, v));
                });

                httpPost.setEntity(new UrlEncodedFormEntity(list, SynHttpPoolClient.CHARSET_UTF8));
            }

            response = httpClient.execute(httpPost);
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != 200) {
                logger.error("Exception send request" + ",url=" + url + ",statusCode=" + statusCode
                        + ",content=" + content);
            }
            HttpEntity httpEntity = response.getEntity();
            String result = null;
            if (httpEntity != null) {
                result = EntityUtils.toString(httpEntity, charset);
            }
            EntityUtils.consume(httpEntity);
            return result;
        }
        catch (Exception ex) {
            logger.error("doPost failed:", ex);
            throw new NetworkException(ex);
        }
        finally {
            if (httpPost != null) {
                httpPost.abort();
            }

            if (response != null) {
                try {
                    response.close();
                }
                catch (IOException e) {
                    logger.error("", e.getMessage(), e);
                }
            }

            if (httpClient != null) {
                try {
                    httpClient.close();
                }
                catch (IOException e) {
                    logger.error("", e.getMessage(), e);
                }
            }
        }
    }

    public String postResponeBody(String url, String content, String charset,
            HttpEntityEnclosingRequestBase httpPost) throws NetworkException {
        try {
            if (httpPost == null) {
                httpPost = new HttpPost(url);
                httpPost.setHeader("SOAPAction", "Notification");
                httpPost.setHeader("Content-Type", "text/xml; charset=UTF-8");
            }
            StringEntity contentEntity = new StringEntity(content, charset);
            httpPost.setEntity(contentEntity);
            CloseableHttpResponse response = httpClient.execute(httpPost);
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != 200) {
                httpPost.abort();
                logger.error("Exception send request,url=" + url + ",statusCode=" + statusCode
                        + ",content=" + content);
            }
            HttpEntity httpEntity = response.getEntity();
            logger.info("Request response httpEntity:" + httpEntity);
            String result = "";
            if (httpEntity != null) {
                result = EntityUtils.toString(httpEntity, charset);
            }
            logger.info("Request response statusCode:" + statusCode);

            EntityUtils.consume(httpEntity);
            response.close();
            result = result.replaceAll("\"", "'");
            return String.format("{\"status\":%d,\"message\":\"%s\"}", statusCode,
                    StringUtils.isEmpty(result) ? "{}" : result);
        }
        catch (Exception ex) {
            logger.error("doPost failed:", ex);
            throw new NetworkException(ex);
        }
    }

    public String sdpSendBody(String httpMethod, String url, String content, String charset,
            Map<String, String> header) throws Exception {
        logger.info("begin to send msg detail:\n {}", content);
        long st = System.currentTimeMillis();
        String response = "";
        if (StringUtils.isNotEmpty(url) && content != null) {
            switch (httpMethod) {
                case "POST":
                    HttpPost httpPost = new HttpPost(url);
                    setPublicHttpHeader(httpPost, header);
                    response = sdpPostOrPutResponeBody(url, content, charset, httpPost);
                    break;
                case "PUT":
                    HttpPut httpPut = new HttpPut(url);
                    setPublicHttpHeader(httpPut, header);
                    response = sdpPostOrPutResponeBody(url, content, charset, httpPut);
                    break;
                case "GET":
                    HttpGet httpGet = new HttpGet(url);
                    setPublicHttpHeader(httpGet, header);
                    response = sdpGetOrDelResponeBody(url, charset, httpGet);
                    break;
                case "DELETE":
                    IsapHttpDelete httpDelete = new IsapHttpDelete(url);
                    setPublicHttpHeader(httpDelete, header);
                    response = sdpPostOrPutResponeBody(url, content, charset, httpDelete);
                    break;
                case "PATCH":
                    HttpPatch httpPatch = new HttpPatch(url);
                    setPublicHttpHeader(httpPatch, header);
                    response = sdpPostOrPutResponeBody(url, content, charset, httpPatch);
                    break;
                default:
                    throw new IllegalArgumentException(
                            "Not support action type: [" + httpMethod + "]");
            }
        }
        else {
            throw new IllegalArgumentException("please check the instruction config");
        }
        logger.info("response msg: {}", response);
        if (logger.isDebugEnabled()) {
            logger.debug("send msg cost:{} ms", (System.currentTimeMillis() - st));
        }
        return response;
    }

    /**
     * 批量增加header
     */
    protected void setPublicHttpHeader(AbstractHttpMessage http, Map<String, String> header) {
        header.forEach((k, v) -> {
            http.addHeader(k, v);
        });
    }

    public String sdpPostOrPutResponeBody(String url, String content, String charset,
            HttpEntityEnclosingRequestBase httpPost) throws NetworkException, NpiTimeOutException {
        JSONObject feedback = new JSONObject();
        CloseableHttpResponse response = null;
        try {
            if (httpPost == null) {
                httpPost = new HttpPost(url);
                httpPost.setHeader("SOAPAction", "Notification");
                httpPost.setHeader("Content-Type", "application/json; charset=UTF-8");
            }
            logger.info("Send request http={}", JSON.toJSONString(httpPost));
            StringEntity contentEntity = new StringEntity(content, charset);
            httpPost.setEntity(contentEntity);
            response = httpClient.execute(httpPost);
            HttpEntity httpEntity = response.getEntity();
            int statusCode = response.getStatusLine().getStatusCode();
            logger.info("response httpEntity={},receive statusCode={}", httpEntity, statusCode);
            if (!(statusCode + "").startsWith("20")) {
//                httpPost.abort();
                logger.error("Exception send request,url=" + url + ",statusCode=" + statusCode
                        + ",content=" + content);
            }

            String result = null;
            if (httpEntity != null) {
                result = EntityUtils.toString(httpEntity, charset);
            }
//            EntityUtils.consume(contentEntity);
            EntityUtils.consume(httpEntity);

            feedback.put("status", statusCode);
            feedback.put("message", StringUtils.isEmpty(result) ? "" : result);
            return feedback.toJSONString();

        }
        catch (SocketTimeoutException e) {
            feedback.put("url", url);
            throw new NpiTimeOutException(
                    IsapModuleCode.getValue("5043030") + feedback.toJSONString());
        }
        catch (Exception ex) {
//            logger.error("doPost failed:",ex);
            throw new NetworkException(ex);
        }
        finally {
            if (httpPost != null) {
                httpPost.abort();
            }

            if (response != null) {
                try {
                    response.close();
                }
                catch (IOException e) {
                    logger.error("", e.getMessage(), e);
                }
            }

            if (httpClient != null) {
                try {
                    httpClient.close();
                }
                catch (IOException e) {
                    logger.error("", e.getMessage(), e);
                }
            }
        }
    }

    /**
     * 可以自定议SOAPAction的post方法
     */
    public String postSoap(String url, String content, String charset, String action)
            throws NetworkException, SocketTimeoutException {
        return postSoap(url, content, charset, action, null);
    }

    /**
     * 可以自定议SOAPAction的post方法
     */
    public String postSoap(String url, String content, String charset, String action,
            HttpPost httpPost) throws SocketTimeoutException, NetworkException {
        CloseableHttpResponse response = null;
        try {
            if (httpPost == null) {
                httpPost = new HttpPost(url);
            }
            httpPost.setHeader("SOAPAction", action);
            httpPost.setHeader("Content-Type", "text/xml; charset=UTF-8");
            StringEntity contentEntity = new StringEntity(content, charset);
            httpPost.setEntity(contentEntity);
            response = httpClient.execute(httpPost);
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != 200) {
                httpPost.abort();
                logger.error("Exception send request,url=" + url + ",statusCode=" + statusCode
                        + ",content=" + content);
            }
            HttpEntity httpEntity = response.getEntity();
            String result = null;
            if (httpEntity != null) {
                result = EntityUtils.toString(httpEntity, charset);
            }
            EntityUtils.consume(httpEntity);
            return result;
        }
        catch (SocketTimeoutException e) {
            throw e;
        }
        catch (Exception ex) {
            logger.error("doPost failed:", ex);
            throw new NetworkException(ex);
        }
        finally {
            if (httpPost != null) {
                httpPost.abort();
            }

            if (response != null) {
                try {
                    response.close();
                }
                catch (IOException e) {
                    logger.error("", e.getMessage(), e);
                }
            }

            if (httpClient != null) {
                try {
                    httpClient.close();
                }
                catch (IOException e) {
                    logger.error("", e.getMessage(), e);
                }
            }
        }
    }

    public String postSoapLazyAbort(String url, String content, String charset, String action,
                           HttpPost httpPost) throws SocketTimeoutException, NetworkException {
        try {
            if (httpPost == null) {
                httpPost = new HttpPost(url);
            }
            httpPost.setHeader("SOAPAction", action);
            httpPost.setHeader("Content-Type", "text/xml; charset=UTF-8");
            if (content != null) {
                StringEntity contentEntity = new StringEntity(content, charset);
                httpPost.setEntity(contentEntity);
            }
            CloseableHttpResponse response = httpClient.execute(httpPost);
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != 200) {
                logger.error("Exception send request,url=" + url + ",statusCode=" + statusCode
                        + ",content=" + content);
            }
            HttpEntity httpEntity = response.getEntity();
            String result = null;
            if (httpEntity != null) {
                result = EntityUtils.toString(httpEntity, charset);
            }
            EntityUtils.consume(httpEntity);
            response.close();
            return result;
        }
        catch (SocketTimeoutException e) {
            throw e;
        }
        catch (Exception ex) {
            logger.error("doPost failed:", ex);
            throw new NetworkException(ex);
        }
        finally {
            httpPost.abort();
        }
    }

    public String postSoapClose(String url, String content, String charset, String action,
                                    HttpPost httpPost) throws SocketTimeoutException, NetworkException {
        CloseableHttpResponse response = null;
        try {
            if (httpPost == null) {
                httpPost = new HttpPost(url);
            }
            httpPost.setHeader("SOAPAction", action);
            httpPost.setHeader("Content-Type", "text/xml; charset=UTF-8");
            if (content != null) {
                StringEntity contentEntity = new StringEntity(content, charset);
                httpPost.setEntity(contentEntity);
            }
            response = httpClient.execute(httpPost);
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != 200) {
                logger.error("Exception send request,url=" + url + ",statusCode=" + statusCode
                        + ",content=" + content);
            }
            HttpEntity httpEntity = response.getEntity();
            String result = null;
            if (httpEntity != null) {
                result = EntityUtils.toString(httpEntity, charset);
            }
            EntityUtils.consume(httpEntity);
            return result;
        }
        catch (SocketTimeoutException e) {
            throw e;
        }
        catch (Exception ex) {
            logger.error("doPost failed:", ex);
            throw new NetworkException(ex);
        }
        finally {
            if (httpPost != null) {
                httpPost.abort();
            }

            if (response != null) {
                try {
                    response.close();
                }
                catch (IOException e) {
                    logger.error("", e.getMessage(), e);
                }
            }

            if (httpClient != null) {
                try {
                    httpClient.close();
                }
                catch (IOException e) {
                    logger.error("", e.getMessage(), e);
                }
            }
        }
    }

    public String get(String url, String charset) throws NetworkException {
        return get(url, charset, null);
    }

    public String get(String url, String charset, HttpGet httpGet) throws NetworkException {
        try {
            if (httpGet == null) {
                httpGet = new HttpGet(url);
            }
            CloseableHttpResponse response = httpClient.execute(httpGet);
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != 200) {
                httpGet.abort();
                logger.error("Exception happen when http get method execute!");
            }
            HttpEntity httpEntity = response.getEntity();
            String result = null;
            if (httpEntity != null) {
                result = EntityUtils.toString(httpEntity, charset);
            }
            EntityUtils.consume(httpEntity);
            response.close();
            return result;
        }
        catch (Exception ex) {
            logger.error("doGet failed:", ex);
            throw new NetworkException(ex);
        }
    }

    public String sdpGetOrDelResponeBody(String url, String charset, HttpRequestBase http)
            throws NpiTimeOutException, NetworkException {
        CloseableHttpResponse response = null;
                JSONObject feedback = new JSONObject();
        try {
            if (http == null) {
                http = new HttpGet(url);
            }
            logger.info("Send request http={}", JSON.toJSONString(http));
            response = httpClient.execute(http);
            int statusCode = response.getStatusLine().getStatusCode();
            logger.info("receive httpEntity={},receive statusCode={}", http, statusCode);
            if (!(statusCode + "").startsWith("20")) {
//                http.abort();
                logger.error("Exception send request,url=" + url + ",statusCode=" + statusCode);
            }
            HttpEntity httpEntity = response.getEntity();
            String result = null;
            if (httpEntity != null) {
                result = EntityUtils.toString(httpEntity, charset);
            }
            EntityUtils.consume(httpEntity);
            feedback.put("status", statusCode);
            feedback.put("message", StringUtils.isEmpty(result) ? "" : result);
            return feedback.toJSONString();
        }
        catch (SocketTimeoutException e) {
            feedback.put("url", url);
            throw new NpiTimeOutException(
                    IsapModuleCode.getValue("5043030") + feedback.toJSONString());
        }
        catch (Exception ex) {
//            logger.error("doGet failed:",ex);
            throw new NetworkException(ex);
        }
        finally {
            if (http != null) {
                http.abort();
            }

            if (response != null) {
                try {
                    response.close();
                }
                catch (IOException e) {
                    logger.error("", e.getMessage(), e);
                }
            }

            if (httpClient != null) {
                try {
                    httpClient.close();
                }
                catch (IOException e) {
                    logger.error("", e.getMessage(), e);
                }
            }
        }
    }

    public String sdpGetOrDelResponeBody(String url, String charset, HttpRequestBase http, String successStatus)
            throws NpiTimeOutException, NetworkException {
        JSONObject feedback = new JSONObject();
        try {
            if (http == null) {
                http = new HttpGet(url);
            }
            logger.info("Send request http={}", JSON.toJSONString(http));
            CloseableHttpResponse response = httpClient.execute(http);
            int statusCode = response.getStatusLine().getStatusCode();
            logger.info("receive httpEntity={},receive statusCode={}", http, statusCode);
            if (!(statusCode + "").startsWith(successStatus)) {
                logger.error("Exception send request,url=" + url + ",statusCode=" + statusCode);
            }
            HttpEntity httpEntity = response.getEntity();
            String result = null;
            if (httpEntity != null) {
                result = EntityUtils.toString(httpEntity, charset);
            }
            EntityUtils.consume(httpEntity);
            response.close();
            feedback.put("status", statusCode);
            feedback.put("message", StringUtils.isEmpty(result) ? "" : result);
            return feedback.toJSONString();
        }
        catch (SocketTimeoutException e) {
            feedback.put("url", url);
            throw new NpiTimeOutException(
                    IsapModuleCode.getValue("5043030") + feedback.toJSONString());
        }
        catch (Exception ex) {
            throw new NetworkException(ex);
        }
        finally {
            try {
                http.abort();
            }
            catch (Exception e) {
                throw e;
            }
        }
    }

    public String getResponeBody(String url, String charset, HttpGet httpGet) throws NetworkException {
        try {
            if (httpGet == null) {
                httpGet = new HttpGet(url);
            }
            CloseableHttpResponse response = httpClient.execute(httpGet);
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != 200) {
                httpGet.abort();
                logger.error("Exception happen when http get method execute!");
            }
            HttpEntity httpEntity = response.getEntity();
            String result = null;
            if (httpEntity != null) {
                result = EntityUtils.toString(httpEntity, charset);
            }
            EntityUtils.consume(httpEntity);
            response.close();
            return String.format("{\"status\":%d,\"message\":%s}", statusCode,
                    StringUtils.isEmpty(result) ? "{}" : result);
        }
        catch (Exception ex) {
            logger.error("doGet failed:", ex);
            throw new NetworkException(ex);
        }
    }

    public String delete(String url, String charset) throws NetworkException {
        return delete(url, charset, null);
    }

    public String delete(String url, String charset, HttpDelete httpDelete) throws NetworkException {
        try {
            if (httpDelete == null) {
                httpDelete = new HttpDelete(url);
            }
            CloseableHttpResponse response = httpClient.execute(httpDelete);
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != 200) {
                httpDelete.abort();
                logger.error("Exception happen when http get method execute!");
            }
            HttpEntity httpEntity = response.getEntity();
            String result = null;
            if (httpEntity != null) {
                result = EntityUtils.toString(httpEntity, charset);
            }
            EntityUtils.consume(httpEntity);
            response.close();
            return result;
        }
        catch (Exception ex) {
            logger.error("doDelete failed:", ex);
            throw new NetworkException(ex);
        }
    }

    public String deleteResponeBody(String url, String charset, HttpDelete httpDelete)
            throws NetworkException {
        try {
            if (httpDelete == null) {
                httpDelete = new HttpDelete(url);
            }
            CloseableHttpResponse response = httpClient.execute(httpDelete);
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != 200) {
                httpDelete.abort();
                logger.error("Exception happen when http get method execute!");
            }
            HttpEntity httpEntity = response.getEntity();
            String result = null;
            if (httpEntity != null) {
                result = EntityUtils.toString(httpEntity, charset);
            }
            EntityUtils.consume(httpEntity);
            response.close();
            return String.format("{\"status\":%d,\"message\":%s}", statusCode,
                    StringUtils.isEmpty(result) ? "{}" : result);
        }
        catch (Exception ex) {
            logger.error("doDelete failed:", ex);
            throw new NetworkException(ex);
        }
    }

    /*public String sdpDeleteResponeBody(String url, String charset,HttpDelete httpDelete) throws Exception {
        try {
            if(httpDelete == null){
                httpDelete = new HttpDelete(url);
            }
            CloseableHttpResponse response = httpClient.execute(httpDelete);
            int statusCode = response.getStatusLine().getStatusCode();
            if (!(statusCode+"").startsWith("20")) {
                httpDelete.abort();
                logger.error("Exception happen when http delete method execute!");
            }
            HttpEntity httpEntity = response.getEntity();
            String result = null;
            if (httpEntity != null) {
                result = EntityUtils.toString(httpEntity, charset);
            }
            EntityUtils.consume(httpEntity);
            response.close();
            return String.format("{\"status\":%d,\"message\":%s}", statusCode,
                    StringUtils.isEmpty(result ) ? "{}" : result );
        } catch (Exception ex) {
            logger.error("doDelete failed:",ex);
            throw ex;
        }
    }*/

    public HttpResultDto httpCommon(HttpParamsDto httpParamsDto) throws NetworkException, URISyntaxException {
        switch (httpParamsDto.getMethod()) {
            case "POST":
                return this.httpsPost(httpParamsDto);
            case "PUT":
                return this.httpsPut(httpParamsDto);
            case "GET":
                return this.httpsGet(httpParamsDto);
            case "DELETE":
                return this.httpsDelete(httpParamsDto);
            default:
                throw new IllegalArgumentException(
                        "Not support action type: [" + httpParamsDto.getMethod() + "]");
        }
    }

    public HttpResultDto httpsGet(HttpParamsDto httpsParamsDto) throws NetworkException {
        HttpRequestBase httpGet = new HttpGet(httpsParamsDto.getUrl());
        httpGet = this.httpsAddSoapHeader(httpsParamsDto.getAction(), httpGet,
                httpsParamsDto.getHeaders());
        return this.httpsExecute(httpsParamsDto.getCharset(), httpGet);
    }

    public HttpResultDto httpsDelete(HttpParamsDto httpsParamsDto) throws NetworkException {
        HttpRequestBase httpDelete = new HttpDelete(httpsParamsDto.getUrl());
        httpDelete = this.httpsAddSoapHeader(httpsParamsDto.getAction(), httpDelete,
                httpsParamsDto.getHeaders());
        return this.httpsExecute(httpsParamsDto.getCharset(), httpDelete);
    }


    public HttpResultDto httpsPost(HttpParamsDto httpsParamsDto) throws NetworkException, URISyntaxException {
        return httpsWithEntityAndHeader(httpsParamsDto, "POST");
    }


    public HttpResultDto httpsPut(HttpParamsDto httpsParamsDto) throws NetworkException, URISyntaxException {
        return httpsWithEntityAndHeader(httpsParamsDto, "PUT");
    }


    public HttpResultDto httpsWithEntityAndHeader(HttpParamsDto httpsParamsDto, String method)
            throws NetworkException, URISyntaxException {
        URIBuilder uriBuilder = this
                .generateURIBuilder(httpsParamsDto.getUrl(), httpsParamsDto.getParams());
        HttpRequestBase httpRequestBase =
                "POST".equalsIgnoreCase(method) ? new HttpPost(uriBuilder.build())
                        : new HttpPut(uriBuilder.build());
        httpRequestBase = this
                .httpsAddEntity(httpsParamsDto.getContent(), httpsParamsDto.getCharset(),
                        httpRequestBase);
        httpRequestBase = this.httpsAddSoapHeader(httpsParamsDto.getAction(), httpRequestBase,
                httpsParamsDto.getHeaders());
        return this.httpsExecute(httpsParamsDto.getCharset(), httpRequestBase);
    }


    /**
     * post，put方法在url后添加参数 例：?username=1&password=1
     */
    public URIBuilder generateURIBuilder(String url, Map<String, String> params) throws NetworkException, URISyntaxException {
        URIBuilder uriBuilder = new URIBuilder(url);
        List<NameValuePair> list;
        if (MapUtils.isNotEmpty(params)) {
            list = new LinkedList<>();
            for (Map.Entry entry : params.entrySet()) {
                BasicNameValuePair param = new BasicNameValuePair(entry.getKey().toString(),
                        entry.getValue().toString());
                list.add(param);
            }
            uriBuilder.setParameters(list);
        }
        return uriBuilder;
    }


    /**
     * 添加请求body
     */
    public HttpRequestBase httpsAddEntity(String content, String charset,
            HttpRequestBase httpRequestBase) throws NetworkException {
        StringEntity contentEntity = new StringEntity(content, charset);
        if (httpRequestBase instanceof HttpPut) {
            ((HttpPut) httpRequestBase).setEntity(contentEntity);
        }
        else if (httpRequestBase instanceof HttpPost) {
            ((HttpPost) httpRequestBase).setEntity(contentEntity);
        }
        return httpRequestBase;
    }


    /**
     * 添加请求头 Content-type为空时，设置默认内容类型：text/xml
     */
    public HttpRequestBase httpsAddSoapHeader(String action, HttpRequestBase httpRequestBase,
            Map<String, String> headers) throws NetworkException {
        // 设置请求头
        if (MapUtils.isNotEmpty(headers)) {
            httpRequestBase.setHeader("SOAPAction", action);
            // Content-type为空时，设置默认内容类型：text/xml
            if (!headers.containsKey("Content-type")) {
                httpRequestBase.setHeader("Content-Type", "text/xml;charset=UTF-8");
            }
            for (Map.Entry entry : headers.entrySet()) {
                httpRequestBase.setHeader(entry.getKey().toString(), entry.getValue().toString());
            }
        }
        return httpRequestBase;
    }


    /**
     * https请求execute（发送请求操作）
     */
    public HttpResultDto httpsExecute(String charset, HttpRequestBase httpRequestBase)
            throws NetworkException {
        try {
            CloseableHttpResponse response = httpClient.execute(httpRequestBase);
            int statusCode = response.getStatusLine().getStatusCode();
            HttpEntity httpEntity = response.getEntity();
            String result = null;
            if (httpEntity != null) {
                result = EntityUtils.toString(httpEntity, charset);
            }
            if (statusCode != 200) {
                httpRequestBase.abort();
                logger.error("Exception send request,url:{} ,statusCode:{}",
                        httpRequestBase.getURI(), statusCode);
            }
            EntityUtils.consume(httpEntity);
            response.close();
            HttpResultDto httpResultDto = new HttpResultDto();
            httpResultDto.setResult(result);
            httpResultDto.setStatusCode(String.valueOf(statusCode));
            return httpResultDto;
        }
        catch (Exception ex) {
            logger.error("Https {} request failed. url:{}", httpRequestBase.getMethod(),
                    httpRequestBase.getURI(), ex);
            throw new NetworkException(ex);
        }
    }

    /**
     * 可以自定议SOAPAction的post方法，这是仿照之前的写法，适用于SOAP 1.2,1适用于1.1
     */
    public String postSoap2(String url, String content, String charset, String action)
            throws NetworkException, SocketTimeoutException {
        return postSoap2(url, content, charset, action, null);
    }

    /**
     * 可以自定议SOAPAction的post方法,这个是SOAP 1.2 版本的方法
     */
    public String postSoap2(String url, String content, String charset, String action,
                           HttpPost httpPost) throws SocketTimeoutException, NetworkException {
        try {
            if (httpPost == null) {
                httpPost = new HttpPost(url);
            }
            httpPost.setHeader("SOAPAction", action);
            httpPost.setHeader("Content-Type", "application/soap+xml; charset=UTF-8");
            StringEntity contentEntity = new StringEntity(content, charset);
            httpPost.setEntity(contentEntity);
            CloseableHttpResponse response = httpClient.execute(httpPost);
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != 200) {
                httpPost.abort();
                logger.error("Exception send request,url=" + url + ",statusCode=" + statusCode
                        + ",content=" + content);
            }
            HttpEntity httpEntity = response.getEntity();
            String result = null;
            if (httpEntity != null) {
                result = EntityUtils.toString(httpEntity, charset);
            }
            EntityUtils.consume(httpEntity);
            response.close();
            return result;
        }
        catch (SocketTimeoutException e) {
            throw e;
        }
        catch (Exception ex) {
            logger.error("doPost failed:", ex);
            throw new NetworkException(ex);
        }
    }

    public String itefSendBody(String httpMethod, String url, String content, String charset,
                              Map<String, String> header) throws NetworkException, NpiTimeOutException {
        logger.info("begin to send msg detail:\n {}", content);
        long st = System.currentTimeMillis();
        String response = "";
        if (StringUtils.isNotEmpty(url) && content != null) {
            switch (httpMethod) {
                case "POST":
                    HttpPost httpPost = new HttpPost(url);
                    setPublicHttpHeader(httpPost, header);
                    response = sdpPostOrPutResponeBody(url, content, charset, httpPost);
                    break;
                case "PUT":
                    HttpPut httpPut = new HttpPut(url);
                    setPublicHttpHeader(httpPut, header);
                    response = sdpPostOrPutResponeBody(url, content, charset, httpPut);
                    break;
                case "GET":
                    HttpGet httpGet = new HttpGet(url);
                    setPublicHttpHeader(httpGet, header);
                    response = sdpGetOrDelResponeBody(url, charset, httpGet);
                    break;
                case "DELETE":
                    IsapHttpDelete httpDelete = new IsapHttpDelete(url);
                    setPublicHttpHeader(httpDelete, header);
                    response = sdpGetOrDelResponeBody(url, charset, httpDelete);
                    break;
                case "PATCH":
                    HttpPatch httpPatch = new HttpPatch(url);
                    setPublicHttpHeader(httpPatch, header);
                    response = sdpPostOrPutResponeBody(url, content, charset, httpPatch);
                    break;
                default:
                    throw new IllegalArgumentException(
                            "Not support action type: [" + httpMethod + "]");
            }
        }
        else {
            throw new IllegalArgumentException("please check the instruction config");
        }
        logger.info("response msg: {}", response);
        if (logger.isDebugEnabled()) {
            logger.debug("send msg cost:{} ms", (System.currentTimeMillis() - st));
        }
        return response;
    }

    public String ietfWithRquestBodyResponseBody(String url, String content, String charset,
                                                   HttpEntityEnclosingRequestBase httpPost) {

        return "";
    }

    public String ietfWithOutRquestBodyResponseBody(String url, String charset,
                                                    HttpRequestBase httpRequestBase, String method)
            throws NpiTimeOutException, NetworkException {
        JSONObject feedback = new JSONObject();
        try {
            if (httpRequestBase == null && "GET".equals(method)) {
                httpRequestBase = new HttpGet(url);
            }
            if (httpRequestBase == null && "DELETE".equals(method)) {
                httpRequestBase = new HttpDelete(url);
            }
            logger.info("Send request http={}", JSON.toJSONString(httpRequestBase));
            CloseableHttpResponse response = httpClient.execute(httpRequestBase);
            int statusCode = response.getStatusLine().getStatusCode();
            logger.info("receive httpEntity={},receive statusCode={}", httpRequestBase, statusCode);
            if (!(statusCode + "").startsWith("20")) {
//                http.abort();
                logger.error("Exception send request,url=" + url + ",statusCode=" + statusCode);
            }
            HttpEntity httpEntity = response.getEntity();
            String result = null;
            if (httpEntity != null) {
                result = EntityUtils.toString(httpEntity, charset);
            }
            EntityUtils.consume(httpEntity);
            response.close();
            feedback.put("status", statusCode);
            feedback.put("message", StringUtils.isEmpty(result) ? "" : result);
            return feedback.toJSONString();
        }
        catch (SocketTimeoutException e) {
            feedback.put("url", url);
            throw new NpiTimeOutException(
                    IsapModuleCode.getValue("5043030") + feedback.toJSONString());
        }
        catch (Exception ex) {
//            logger.error("doGet failed:",ex);
            throw new NetworkException(ex);
        }
    }


//    public static void main(String[] args ) {
//        SynHttpPoolClient httpClient = new SynHttpPoolClient( );
//
//        String url = "http://localhost:8900/device/12345678/ztp";
//        String msg = "{\"connectivity\":{\"bandwidth\":\"\",\"technology\":\"D\",\"connectivityId\":\"202005091430974977101\",\"equipmentNumber\":\"\",\"vc\":\"MULTI_VC\"},\"domainName\":\"voip.dt.ept.lu\",\"voip\":[{\"directoryNumber\":\"999999244\",\"sipProxyServer\":\"voip.dt.ept.lu\",\"sipRegistrarServer\":\"voip.dt.ept.lu\",\"sipAuthPassword\":\"password11\",\"sipOutboundProxy\":\"residential.dt.ept.lu\",\"sipUri\":\"sip:999999244@voip.dt.ept.lu\",\"sipAuthUsername\":\"999999244@voip.dt.ept.lu\"}],\"sag\":{\"pppoePassword\":\"111111\",\"pppoeLogin\":\"grace1\",\"profile\":\"65\",\"ipv4range\":{\"subscriptionStatus\":\"SUBSCRIBE\",\"ipv4range\":\"1111\"}},\"header\":{\"context\":{\"orderId\":\"P2005091430975041\",\"requestId\":\"16678695\",\"correlationId\":\"C202005091430974976101\",\"parameters\":[{\"value\":\"31400100\",\"key\":\"OM_TASK_ID\"}]},\"callbackUrl\":\"{callbackUrl}\"},\"subscriberId\":\"202005091430974986101\"}";
//
//
//        try {
//            String response = httpClient.sdpPostOrPutResponeBody("PUT",url, msg, SynHttpPoolClient.CHARSET_UTF8, httpClient.addPublicHttpHeader());
//            System.out.println(response);
//        } catch (Exception e ) {
//            e.printStackTrace();
//        }
//
//    }
//
//    /**
//     * 设置公共的Header
//     */
//    protected Map<String, String> addPublicHttpHeader(){
//        Map<String, String> header = new HashMap<>( );
//        header.put("Content-Type", "application/json; charset=UTF-8");
////        header.put("X-IBM-Client-Id", "k");
////        header.put("X-IBM-Client-Secret", "k");
//        return header;
//    }

}