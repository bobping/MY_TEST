package isaphttpclienttest;

import java.util.Map;

/**
 * @Decription TODO http 和 https 可以通用的请求参数封装
 * @Authur Benjamin
 * @Date 2020/4/21 16:08
 * @Version 1.0
 */
public class HttpParamsDto {

    /**
     * 必填
     */
    private String url;
    /**
     * 必填
     */
    private String charset;
    /**
     * 请求体（请求body）
     */
    private String content;
    /**
     * post,put方法url添加参数的集合
     */
    private Map<String, String> params;
    private Map<String, String> headers;
    private String action;
    private String method;


    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getCharset() {
        return charset;
    }

    public void setCharset(String charset) {
        this.charset = charset;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public Map<String, String> getParams() {
        return params;
    }

    public void setParams(Map<String, String> params) {
        this.params = params;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }
}
