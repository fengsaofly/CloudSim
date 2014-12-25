package scu.fly.main;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

public class CommunicateWhitPiping {
    public static void main(String[] args) {
        /**
         * 创建管道输出流
         */
        PipedOutputStream pos = new PipedOutputStream();
        /**
         * 创建管道输入流
         */
        PipedInputStream pis = new PipedInputStream();
        try {
            /**
             * 将管道输入流与输出流连接 此过程也可通过重载的构造函数来实现
             */
            pos.connect(pis);
        } catch (IOException e) {
            e.printStackTrace();
        }
        /**
         * 创建生产者线程
         */
        Producer p = new Producer(pos);
        /**
         * 创建消费者线程
         */
        Consumer c = new Consumer(pis);
        /**
         * 启动线程
         */
        p.start();
        c.start();
    }
}

/**
 * 生产者线程(与一个管道输出流相关联)
 * 
 */
class Producer extends Thread {
    private PipedOutputStream pos;

    public Producer(PipedOutputStream pos) {
        this.pos = pos;
    }

    public void run() {
        int i = 8;
        String s = "sdads";
        try {
            pos.write(s.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

/**
 * 消费者线程(与一个管道输入流相关联)
 * 
 */
class Consumer extends Thread {
    private PipedInputStream pis;

    public Consumer(PipedInputStream pis) {
        this.pis = pis;
    }

    public void run() {
        try {
        	
        	byte[] b = new byte[10];
			pis.read(b);
			
			System.out.println();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}