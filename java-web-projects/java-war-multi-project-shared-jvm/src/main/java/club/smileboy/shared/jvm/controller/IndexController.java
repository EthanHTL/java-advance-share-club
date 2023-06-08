package club.smileboy.shared.jvm.controller;


import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.beans.BeanInfo;
import java.beans.Introspector;

@RestController
@RequestMapping("/api/index")
public class IndexController {

    @GetMapping
    public String existsUserClassBeanInfo(HttpServletRequest request) {

        String threadName = Thread.currentThread().getContextClassLoader().getParent().toString();
        String name = Thread.currentThread().getContextClassLoader().toString();
        try {
            try {
                Class<?> aClass1 = Thread.currentThread().getContextClassLoader().getParent()
                        .loadClass("club.smileboy.model.User");
                return "存在";
            }catch (Exception e) {
                // pass
            }


           Class<?> aClass = Class.forName("club.smileboy.model.User");

           BeanInfo beanInfo = Introspector.getBeanInfo(aClass);

           return  "存在 club.smileboy.model.User 的 beanInfo";
       }catch (Exception e) {
           return "thread Name: + " + threadName + "current thread" + name + "  " + "不存在 club.smileboy.model.User 的 beanInfo: cause: " + (StringUtils.hasText(e.getMessage()) ? e.getMessage() : "没有异常信息");
       }
    }
}
