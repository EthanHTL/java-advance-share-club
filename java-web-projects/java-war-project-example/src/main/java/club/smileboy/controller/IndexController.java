package club.smileboy.controller;

import club.smileboy.model.User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.beans.BeanInfo;
import java.beans.Introspector;

@RestController
@RequestMapping("/api/index")
public class IndexController {

    @GetMapping
    public String existsUserClassBeanInfo() {
        String threadName = Thread.currentThread().getContextClassLoader().getParent().toString();
        String name = Thread.currentThread().getContextClassLoader().toString();
        try {
           BeanInfo beanInfo = Introspector.getBeanInfo(User.class);

           return  "threadName: " + threadName + " current thread " + name + "存在 club.smileboy.model.User 的 beanInfo";
       }catch (Exception e) {
           return "threadName: " + threadName + " current thread " + name + "不存在 club.smileboy.model.User 的 beanInfo";
       }
    }
}
