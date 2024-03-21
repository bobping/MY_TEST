package isaphttpclienttest;

/**
 * @Decription TODO http 和 https 可以通用的返回结果封装 为了能解析get方法是否成功，新增statusCode
 * @Authur Benjamin
 * @Date 2020/4/24 16:26
 * @Version 1.0
 */
public class HttpResultDto {

    private String result;
    private String statusCode;

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(String statusCode) {
        this.statusCode = statusCode;
    }
}
