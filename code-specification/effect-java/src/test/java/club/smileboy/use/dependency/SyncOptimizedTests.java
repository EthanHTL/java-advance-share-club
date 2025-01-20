package club.smileboy.use.dependency;

import org.junit.jupiter.api.Test;

import java.io.Serializable;
import java.util.List;
import java.util.concurrent.*;

/**
 * @author jasonj
 * @date 2024/11/9
 * @time 23:35
 * @description
 **/
public class SyncOptimizedTests {

    static volatile A a;

    @Test
    public void test() throws InterruptedException {

        // 通过volatile 实现 共享可变数据的可见性 ..
        // 本质上 我们是最好共享不变数据,或者短时间修改一个对象,然后与其他线程共享,
        // 并且我们只共享对象引用的动作(同步下),这样的话, 我们也达到了 共享某个特定值的目的 ..

        // 但是相比之下,这个引用的对象 和 特定值(是不可变的) ..
        // 这种叫做 高效不可变, 将一个这样的对象引用从一个线程传递到其他的线程 叫做 安全发布。
        // 1. 放置在静态域中,作为类初始化的一部分.(这样立即可见)
        // 2. 保存在 volatile 域 或者 final 域中,或者通过同步(互斥)访问的域中
        // 3.把它放在并发的集合中 ..

        // final / 静态域(类初始化)  都属于共享不变数据 ..

        // 此外一般是volatile / sync / 并发集合

        new Thread(() -> {
            try {
                TimeUnit.SECONDS.sleep(5);
            }catch (Exception e) {
                // pass
            }
            a = new A("FINISH");
        }).start();


        Thread thread = new Thread(() -> {
            while (a == null) {

            }
            System.out.println("目标线程" + a.getValue());
        });

        thread.start();

        thread.join();

        // 等待一段时间
        String intern = new String().intern();

        Thread.currentThread().interrupt();
    }

    static class A implements Serializable {
        private static final long serialVersionUID = 4600100876461593284L;
        private final String value;
        public A(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }


}
