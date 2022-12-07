package club.smileboy.app;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.EnableLoadTimeWeaving;
import org.springframework.context.annotation.ImportResource;
import org.springframework.instrument.InstrumentationSavingAgent;

import java.lang.instrument.Instrumentation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@EnableCaching
@EnableLoadTimeWeaving
//@ImportResource("classpath:aop-load-time-weaver.xml")
@SpringBootApplication(scanBasePackages = "club.smileboy.app.common")
public class WebApplication {
    public static void main(String[] args) {
        //System.out.println(InstrumentationSavingAgent.class);

        //ModuleLayer.boot().findModule("java.instrument").ifPresent(m -> {
        //    try {
        //        String cn = "sun.instrument.InstrumentationImpl";
        //        Class<?> clazz = Class.forName(cn, false, null);
        //        Method loadAgent = clazz.getMethod("loadAgent", String.class);
        //        loadAgent.invoke(null, jarname);
        //    } catch (Throwable e) {
        //        if (e instanceof InvocationTargetException) e = e.getCause();
        //        System.out.println("加载agent 异常");
        //    }
        //});


        SpringApplication.run(WebApplication.class, args);
    }
}
