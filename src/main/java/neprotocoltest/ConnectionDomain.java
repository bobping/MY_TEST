package neprotocoltest;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2006</p>
 * <p>Company: </p>
 *
 * @author xiaoyuer
 * @version 1.0
 */
public class ConnectionDomain {

    public static final int STATE_BASE = 1000;
    public static final int STATE_INIT = STATE_BASE + 1; // 初始状态
    public static final int STATE_CONNECT = STATE_BASE + 2; // 连接通讯端口
    public static final int STATE_DISCONNECT = STATE_BASE + 3; // 断开通讯端口
    public static final int STATE_CONNECTION_RESET = STATE_BASE + 4; // 连接被意外重置
    public static final int STATE_CMD_EXECUTING = STATE_BASE + 5; // 执行命令中
    public static final int STATE_CMD_DONE = STATE_BASE + 6; // 命令执行完成

}
