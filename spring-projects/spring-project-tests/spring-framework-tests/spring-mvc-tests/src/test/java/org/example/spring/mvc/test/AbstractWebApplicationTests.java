package org.example.spring.mvc.test;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.mock.web.MockServletContext;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;

public abstract class AbstractWebApplicationTests {

    protected AnnotationConfigWebApplicationContext wac;


    @BeforeEach
    public void setUp() throws Exception {
        MockServletContext sc = new MockServletContext("");
        wac = new AnnotationConfigWebApplicationContext();
        registerComponent(wac);
        wac.setServletContext(sc);
        wac.refresh();
    }

    protected void registerComponent(AnnotationConfigWebApplicationContext wac) {

    }

    public <T> T getBean(Class<T> beanType) {
        return wac.getBean(beanType);
    }

    public <T> T getBean(String beanName) {
        return (T)wac.getBean(beanName);
    }
}
