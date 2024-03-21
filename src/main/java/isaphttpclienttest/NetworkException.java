package isaphttpclienttest;

/**
 * @Decription
 * @Author NEIL
 * @Date 2023/3/17 17:50
 * @Version 1.0
 */
public class NetworkException extends Exception {
    public NetworkException() {
        super();
    }

    public NetworkException(String message) {
        super(message);
    }

    public NetworkException(Throwable cause) {
        super(cause);
    }

    public NetworkException(String message, Throwable cause) {
        super(message, cause);
    }
}
