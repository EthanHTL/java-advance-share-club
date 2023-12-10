package org.example.spring.test.review.intergration.tcf.execute;

import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.event.*;
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
 **/
@SpringJUnitConfig
@RecordApplicationEvents
public class ApplicationEventsTestExecuteListenerTests {

    @Autowired
    private ApplicationEvents applicationEvents;

    @Configuration
    static class  Config {

    }

    /**
     * 测试方法不能被autowired,但是参数可以
     */
    @Test
    public void test(@Autowired ApplicationEvents applicationEvents) {

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
