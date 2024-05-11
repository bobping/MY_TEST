package neprotocoltest;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.codec.Charsets;
import org.apache.log4j.Logger;
import socktest.MINSATMockHandler;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 特殊处理
 */
public class NeProtocolMINSAT {
    //匹配查询结果的FAFNumberList
    private final Pattern patternParam = Pattern.compile("\\$\\{(.*?)\\}");
    private static final Logger logger = Logger.getLogger(NeProtocolMINSAT.class);
    protected int state; // 状态
    private static final String loginSuccess = "RESP:0";
    private Pattern loginSuccessRe;
    /**
     * true表示在发指令，直到接受完网元返回才是false。异常情况也是false
     */
    private volatile boolean running = false;
    /**
     * true表示定时任务正在运行，准备发指令时要等待
     */
    public volatile boolean taskRunning = false;

    private ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(5);
    private boolean scheduledTaskHasBeenActivated = false;
    private Semaphore semaphore = new Semaphore(1);
    InputStream in = null;
    OutputStream out = null;
    Socket socket;

    public static void main(String[] args) throws Exception {

        NeProtocolMINSAT con = new NeProtocolMINSAT();

        if (con.getState() == 0) {
            con.connect();
        }
        con.login();

        while (true) {

            try {
                if (con.getState() == ConnectionDomain.STATE_CONNECTION_RESET // 连接重置
                        || con.getState() == ConnectionDomain.STATE_DISCONNECT) { // 连接断开

                    logger.info(">>>>>>>>>check connect>>>>>>>>>>>>>>");

                    try {
                        con.disConnect(); // 断开连接
                    } catch (Exception ex) {
                        logger.error("", ex);
                    }
                    con.connect();
                    con.login();
                }
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
            Thread.sleep(100);
        }

    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public boolean connect() throws Exception {
        this.setState(ConnectionDomain.STATE_DISCONNECT);
        socket = new Socket();
        long s = System.currentTimeMillis();
        socket.connect(new InetSocketAddress("172.16.23.35", 5873), 3000);
        logger.info("connect 耗时：" + (System.currentTimeMillis() - s));
        socket.setKeepAlive(true);
        // 暂时不需要
        //socket.setSoLinger(true, 0);
        socket.setSoTimeout((int) 5000);
        in = socket.getInputStream();
        out = socket.getOutputStream();
        this.setState(ConnectionDomain.STATE_CONNECT);
        return true;
    }

    public boolean disConnect() throws Exception {
        this.setState(ConnectionDomain.STATE_DISCONNECT);
        if (socket != null) {
            String msg1 = "LOGOUT;";
            try {
                logger.info("begin to send logout msg ... msg1 detail: " + msg1);
                long s = System.currentTimeMillis();
                this.sendObject(msg1 + "\r\n");
                logger.info("telnet send logout msg cost time ms:" + (System.currentTimeMillis() - s));
            }
            catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
            socket.close();
            if (in != null) {
                in.close();
            }
            if (out != null) {
                out.close();
            }
            socket = null;
            in = null;
            out = null;
        }
//        Thread.sleep(2000);
        return true;
    }

    public boolean sendObject(Object o) throws Exception {
        out.write(o.toString().getBytes(Charsets.UTF_8));
        out.flush();
        return true;
    }

    public String recvAll(int timeoutMills, String untilRegexp, boolean timeoutFromClient)
            throws Exception {

        StringBuilder sb = new StringBuilder();
        byte[] buff = new byte[2048];
        StringBuilder oriBytes = new StringBuilder(); //记录协议所接收到的
        try {
            // 需要设置合适的 socket 操作的超时时间
            if (timeoutFromClient) {
                socket.setSoTimeout(timeoutMills);
            }
            Pattern pattern = Pattern.compile(untilRegexp);
            Matcher matcher = null;
            int length;
            long s = System.currentTimeMillis();
            while (true) {
                length = in.read(buff);
                if (length == -1) {
                    logger.warn("connection reset");
                    break;
                }
                if (System.currentTimeMillis() - s > timeoutMills) {
                    logger.error("recv timeout!");
                    break;
                }
                sb.append(new String(buff, 0, length, Charsets.UTF_8));
                logger.info("recv msg: " + sb);
                matcher = pattern.matcher(sb.toString());
                if (matcher.find()) { //匹配到对应的正则表达式时则退出
                    break;
                }
            }
        } catch (SocketTimeoutException e) {
            logger.error("telnet socket timeout exception: ", e);
            if (oriBytes.length() > 1) {
                oriBytes.deleteCharAt(oriBytes.length() - 1);
            }
            logger.error("oriBytes: " + oriBytes.toString()); //在异常时把接收到的所有字节都打印出来.
            // 这里直接返回的内容无法做异常码转换，导致回单异常
            //sb.append("TIMEOUT: CMD has been sent, but response TIMEOUT!");
            logger.error("TIMEOUT: CMD has been sent, but response TIMEOUT!");
            throw new Exception(e.getMessage(), e);
        } catch (Exception e) {
            logger.error("telnet read msg happen an exception: ", e);
            if (e.getMessage().contains("Socket closed")) {
            }
            throw new Exception(e.getMessage(), e);
        }
        return sb.toString();
    }


    public boolean login() throws Exception {
        loginSuccessRe = Pattern.compile(loginSuccess);
        String loginStr = "LOGIN";
        running = true;
        long s = System.currentTimeMillis();
        this.sendObject(loginStr + "\r\n");
        while (true) {
            if (System.currentTimeMillis() - s > 10000) {
                logger.error("Login timeout!");
                break;
            }
            String line = this.recvAll(5000, loginSuccess, true);

            if (line == null) {
                logger.warn("recv msg is empty!");
                continue;
            }

            logger.info("Login response: " + line);
            if (loginSuccessRe.matcher(line).find()) {
                break;
            }
        }
        logger.info("login 耗时：" + (System.currentTimeMillis() - s));

        running = false;
        // 开启定时任务，每隔一段时间自动登录
        // session id的到期时间所有的接口机都是一样，5分钟内什么都不做网元就会关闭连接。APSP老系统60秒更新一次连接
        if (!scheduledTaskHasBeenActivated) {
            scheduledTaskHasBeenActivated = true;

            int second = 2;
            scheduledExecutorService.scheduleAtFixedRate(() -> {
                taskRunning = true;
                logger.info("Scheduled task start login...Acquires a permit.");
                try {
                    semaphore.acquire();
                    try {
                        long runningS = System.currentTimeMillis();
                        while (running) {
                            Thread.sleep(500);
                            if (System.currentTimeMillis() - runningS > 5 * 1000) {
                                break;
                            }
                        }
                        this.disConnect();
//                        Thread.sleep(2000);
                    } catch (Exception e) {
                        // 断开连接的异常忽略
                    }
                    this.connect();
                    this.login();

                } catch (InterruptedException e) {
                    logger.error(e.getMessage(), e);
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                } finally {
                    taskRunning = false;
                    semaphore.release();
                }
                logger.info("Scheduled task finish login...Acquires a permit.");
            }, second, second, TimeUnit.SECONDS);


//            scheduledExecutorService.scheduleAtFixedRate(() -> {
//
//                System.gc();
//
//            }, 5, 5, TimeUnit.SECONDS);
        }
        return true;
    }


}
