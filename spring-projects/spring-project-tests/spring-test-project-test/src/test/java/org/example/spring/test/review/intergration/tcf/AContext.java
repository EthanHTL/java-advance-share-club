package org.example.spring.test.review.intergration.tcf;

/**
 * @author jasonj
 * @date 2023/11/2
 * @time 22:57
 *
 * @description 这是一个测试的上下文 是否在应用起来的时候加载了它
 **/
public class AContext {
    static boolean status = false;
    static {
        System.out.println("loading");
        status = true;
    }
}
