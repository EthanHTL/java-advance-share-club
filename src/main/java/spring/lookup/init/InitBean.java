package spring.lookup.init;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

/**
 * @author JASONJ
 * @dateTime: 2021-05-02 10:42:33
 * @description: init test
 * 初始化方法测试回调
 */
public class InitBean {

    public void init(){
        // 默认初始化
        System.out.println("初始化init bean");
    }

    public void close(){
        // 默认摧毁!
        System.out.println("摧毁 init bean");
    }

    public void shutdown(){
        System.out.println("shutdown init bean");
    }
    public void destroy(){
        System.out.println("destroy init bean");
    }

//    @PostConstruct
//    public void postConstruct(){
//        System.out.println("PostConstruct init bean annotation occur!");
//    }
//
//    @PreDestroy
//    public void preDestroy(){
//        System.out.println("PreDestroy init bean annotation occur!");
//    }
}
