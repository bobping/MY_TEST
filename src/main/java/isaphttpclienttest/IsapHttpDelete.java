package isaphttpclienttest;

import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;

import java.net.URI;

/**
 * HttpClient自带的HttpDelete方法是不支持上传body的，所以重写delete方法
 */
public class IsapHttpDelete extends HttpEntityEnclosingRequestBase {

    public static final String METHOD_NAME = "DELETE";

    public IsapHttpDelete(final String uri) {
        super();
        setURI(URI.create(uri));
    }

    public IsapHttpDelete(final URI uri) {
        super();
        setURI(uri);
    }

    public IsapHttpDelete() {
        super();
    }

    public String getMethod() {
        return METHOD_NAME;
    }
}