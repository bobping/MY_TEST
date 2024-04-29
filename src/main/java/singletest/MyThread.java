package singletest;

public class MyThread implements Runnable{

    public void run() {
        synchronized(this) {
            for (int i = 0; i < 1; i--) {
                System.out.println(Thread.currentThread().getName() + " synchronized loop " + i);
//                try {
//                    Thread.sleep(500);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
            }
        }
    }
    public static void main(String[] args) {
        MyThread t1 = new MyThread();
        Thread ta = new Thread(t1, "A");
        Thread tb = new Thread(t1, "B");
        ta.start();
        tb.start();
    }

}