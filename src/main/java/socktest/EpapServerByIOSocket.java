package socktest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

/*
 * @Classname EpapServerMultiConnection
 * @Version information V1.0
 * @Date 2024/4/27
 * @Copyright notice iWhaleCloud
 * @userName NEIL
 */
public class EpapServerByIOSocket {
    public static void main(String[] args) throws IOException {

        Socket socket;
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(5873);
            for (int i = 0; i < 10; i++) {
                socket = serverSocket.accept(); //等待客户连接
                System.out.println("recvice a new client, port=" + socket.getLocalPort());
                ClientHandle client = new ClientHandle();
                Thread t = new Thread(client);
                t.start();
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            try {
                if (serverSocket != null) serverSocket.close(); //断开连接
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }


    }

    static class ClientHandle implements Runnable {
        private Socket socket;

        @Override
        public void run() {
            try {
                InputStream socketin = socket.getInputStream();
                BufferedReader br = new BufferedReader(new InputStreamReader(socketin));
                OutputStream socketOut = socket.getOutputStream();
                PrintWriter pw = new PrintWriter(socketOut, true);
                String msg = null;
                while ((msg = br.readLine()) != null) {
                    System.out.println("收到的消息:" + msg);
                    if (msg.contains("disconnect(iid")) { //结束通信
                        System.out.println("connection reset");
                        break;
                    }
                    else if (msg.startsWith("connect")) { //建立连接
                        System.out.println("connect...");
                        pw.println("rsp (iid 9461851, rc 0, data (connectId 1527915, side active))\n");
                    }
                    else {
                        System.out.print("response:");
                        pw.println("rc 0,");
                    }
                }
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            finally {
                try {
                    if (socket != null) socket.close(); //断开连接
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}