package singletest;

//此类需要在32位系统下测试
public class AtomicTest implements Runnable {

    static long value = 0;
    private final long valueToSet;

    public AtomicTest(long valueToSet) {
        this.valueToSet = valueToSet;
    }

    public static void main(String[] args) {

        Thread thread1 = new Thread(new AtomicTest(0L));
        Thread thread2 = new Thread(new AtomicTest(-1L));

        thread1.start();
        thread2.start();

        long snapShort;
        //java模式为client模式，不会进行循环优化，snapShort=value不会循环外提
        while (0 == (snapShort = value) || -1 == snapShort) {
        }
        //不等于0和1的时候打印出来
        System.out.printf("Unexpected data: %d(0x%016x)", snapShort, snapShort);
        //退出程序，否则子线程无限循环，程序永远不会终止
        System.exit(0);
    }

    @Override
    public void run() {
        //两个线程不断的给共享变量value进行赋值，如果
        for (; ; ) {
            value = valueToSet;
        }
    }
}

