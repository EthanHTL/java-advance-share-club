package org.example.spring.test.review.intergration.tcf;

/**
 * @author jasonj
 * @date 2023/11/2
 * @time 22:58
 *
 * @description 这里做一个测试,是否加载AContext
 **/
public class BpTest {

    static void a() {
        System.out.println(AContext.class);
    }

    public static void main(String[] args) {
        a();
    }
}
