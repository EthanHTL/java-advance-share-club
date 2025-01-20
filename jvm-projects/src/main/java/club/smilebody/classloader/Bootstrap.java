package club.smilebody.classloader;

import java.util.concurrent.TimeUnit;

/**
 * @author jasonj
 * @date 2024/1/18
 * @time 21:39
 * @description
 **/
public class Bootstrap {

    private static Object  daemon = null;
    public static void main(String[] args) throws InterruptedException {

        if(daemon != null) {
            System.out.println("daemon 不等于null");
        }

        if (daemon == null) {
            daemon = new Object();
        }

        TimeUnit.SECONDS.sleep(10);

        System.out.println(Thread.currentThread().getName());
    }
}
