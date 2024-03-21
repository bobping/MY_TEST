package isaphttpclienttest;

/**
 * @Decription timeout exception
 * @Authur guo.songjin
 * @Date 2020/3/31 15:41
 * @Version 1.0
 */
public class NpiTimeOutException extends Exception {

    public NpiTimeOutException(String mssage) {
        super(mssage);
    }

    public NpiTimeOutException() {
        super();
    }

    public NpiTimeOutException(String message, Throwable cause) {
        super(message, cause);
    }

    public NpiTimeOutException(Throwable cause) {
        super(cause);
    }

    protected NpiTimeOutException(String message, Throwable cause, boolean enableSuppression,
                                  boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
