package org.example.spring.test.review.intergration.tcf.execute;

import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.test.context.event.*;
import org.springframework.test.context.event.annotation.BeforeTestClass;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author jasonj
 * @date 2023/11/3
 * @time 17:55
 *
 * @description 应用事件监听
 *
 * 如果需要注入这个ApplicationEvents,必须添加如下注解 ...
 *
 * 事件派发是通过{@link EventPublishingTestExecutionListener}放入 {@link ApplicationEvents}中的 ..
 *
 * 而{@link ApplicationEvents} 是通过{@link  ApplicationEventsTestExecutionListener} 注入到应用上下文的 ...
 **/
@SpringJUnitConfig
@RecordApplicationEvents
public class ApplicationEventsTestExecuteListenerTests {

    @Autowired
    private ApplicationEvents applicationEvents;

    @Configuration
    static class  Config {

        @EventListener
        public void beforeTestEventListen(TestContextEvent prepareTestInstanceEvent) {
            System.out.println("测试上下文事件: " + prepareTestInstanceEvent.getClass().getSimpleName());
        }
    }


    /**
     * 测试方法不能被autowired,但是参数可以
     */
    @Test
    public void test(@Autowired ApplicationEvents applicationEvents) {

        // 在初始化上下文加载的过程中, @BeforeTestClass 将不会发布,因为事件仅仅在应用上下文创建之后才会发布 ..
        //在这个阶段并没有创建应用上下文 ...

        // 不发的原因是 org.springframework.test.context.support.DefaultTestContext.getApplicationContext
        // 仅仅在prepareTestInstance的时候才有被执行监听器调用 ..
        // 这就是为什么官方告诉我们可以自己注册一个执行监听器并在beforeClassTest的时候加载应用上下文 ..
        // 就是因为这个方法调用的时候才会加载上下文 ..
        // 根据SPI 机制,最先加载应用上下文的方法应该是 org.springframework.test.context.event.ApplicationEventsTestExecutionListener.prepareTestInstance

        // 它会在每一个事件回调hook中尝试注册 ApplicationEvents ..
//        new ApplicationEventsTestExecutionListener();

        // org.springframework.beans.factory.support.AutowireUtils.ObjectFactoryDelegatingInvocationHandler
        // 封装了 ApplicationEvents
        ApplicationEvents events = ApplicationEventsHolder.getRequiredApplicationEvents();
        // 由于每次通过 ObjectFactory 获取的代理,所以 不相同 ..
        Assert.assertNotSame(this.applicationEvents,applicationEvents);
        Stream<ApplicationEvent> eventStream = ApplicationEventsHolder.getRequiredApplicationEvents().stream();

        List<ApplicationEvent> collect = eventStream.collect(Collectors.toList());
        Assertions.assertThat(collect)
                .containsAll(applicationEvents.stream().collect(Collectors.toList()));


        // 此时的事件是
        List<ApplicationEvent> eventList = events.stream().collect(Collectors.toList());

        ApplicationEvent applicationEvent = eventList.get(eventList.size() - 1);

        // 测试执行之前的事件
        org.junit.jupiter.api.Assertions.assertInstanceOf(BeforeTestExecutionEvent.class,applicationEvent);
        for (ApplicationEvent event : eventList) {
            System.out.println(event.getClass());
        }
    }
}
