package org.example.spring.test.review.util.mvc.modelview;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.Test;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.server.reactive.AbstractServerHttpResponse;
import org.springframework.lang.Nullable;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.ModelAndViewAssert;
import org.springframework.web.context.request.async.DeferredResult;
import org.springframework.web.context.request.async.WebAsyncManager;
import org.springframework.web.context.request.async.WebAsyncUtils;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.function.*;
import org.springframework.web.servlet.function.support.HandlerFunctionAdapter;
import org.springframework.web.servlet.function.support.RouterFunctionMapping;
import org.springframework.web.servlet.mvc.Controller;
import org.springframework.web.servlet.mvc.SimpleControllerHandlerAdapter;

import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * @author jasonj
 * @date 2023/10/31
 * @time 23:21
 *
 * @description Model with View Assert
 *
 *
 * 这种也算对对 spring mvc 的controller 进行单元测试
 *
 * 引用来自 spring 上下文测试框架中的引言:
 *      单元测试spring mvc Controllers 为了测试作为pojo的spring mvc Controller, 使用 ModelAndViewAssert 连同 MockHttpServletRequest 以及 MockHttpSession,以及其他来自spring Servlet api mocks的类 ..
 * <a href="https://github.com/EthanHTL/java-advance-share-club/blob/main/doc/spring/test/spring-context-framework.md#spring-mvc-%E6%B5%8B%E8%AF%95%E5%B7%A5%E5%85%B7%E7%B1%BB">单元测试 spring mvc 组件</a>
 **/
public class UseModelAndViewTests {

    @Test
    public void modelAndViewTests() throws Exception {
        MockHttpServletRequest servletRequest = new MockHttpServletRequest();
        // mock session
        HttpSession session = servletRequest.getSession();
        // 设置mock session
//        servletRequest.setSession();

        // 首先使用一个合适的HandlerAdapter,执行对应合适的Handler 来返回 ModelAndView ..
        // ctrl + h 查看 HandlerAdapter的实现类 ..

        SimpleControllerHandlerAdapter adapter = new SimpleControllerHandlerAdapter();
        ModelAndView view = adapter.handle(servletRequest, new MockHttpServletResponse(),

                // 使用最简单的处理器
                new Controller() {
                    @Nullable
                    @Override
                    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
                        return new ModelAndView("mock-model-view", new LinkedHashMap<>() {{
                            put("a", "a");
                            put("b", "b");
                            put("c", "C");
                            put("d",String.class);
                            put("e", Arrays.asList("1","3","2"));
                        }});
                    }
                }
        );

        ModelAndViewAssert.assertViewName(view,"mock-model-view");
        ModelAndViewAssert.assertAndReturnModelAttributeOfType(view,"a",String.class);
        ModelAndViewAssert.assertAndReturnModelAttributeOfType(view,"d",Class.class);

        ModelAndViewAssert.assertModelAttributeAvailable(view,"c");
        ModelAndViewAssert.assertModelAttributeValues(view,new LinkedHashMap<>(){{
            put("a", "a");
            put("b", "b");
            put("c", "C");
            put("d",String.class);
            put("e", Arrays.asList("1","3","2"));
        }});

        // 比较两个列表是否相同
        ModelAndViewAssert.assertCompareListModelAttribute(view,"e",Arrays.asList("1","3","2"));

        // 比较排序,并比较列表
        ModelAndViewAssert.assertSortAndCompareListModelAttribute(view,
                "e",Arrays.asList("1","2","3"), Comparator.comparing(String::valueOf));
    }


    @Test
    public void handlerFunctionAsyncTest() throws Exception {
        // 用了另一种处理器函数适配器
        HandlerFunctionAdapter adapter = new HandlerFunctionAdapter();

        // 对于这个请求,需要通过 RouterFunctionMapping 进行映射分发 ..

        RouterFunctionMapping routerFunctionMapping = new RouterFunctionMapping(
                RouterFunctions.route(RequestPredicates.all(), new HandlerFunction<ServerResponse>() {
                    @Override
                    public ServerResponse handle(ServerRequest request) throws Exception {
                        return ServerResponse.sse(builder -> {
                            CompletableFuture.runAsync(() -> {
                                try {
                                    builder.send("1");
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                            });

                            CompletableFuture.runAsync(() -> {
                                try {
                                    TimeUnit.SECONDS.sleep(2);
                                    builder.send("2");

                                    // 执行完成
                                    // 当这里触发完成的时候,才会进行真正的DeferredResult 结束 ..
                                    builder.complete();
                                } catch (InterruptedException | IOException e) {
                                    throw new RuntimeException(e);
                                }
                            });
                        },

                                // 5秒钟响应??
                                // 这应该需要真正的线程池上下文 来检测,否则这种单元测试不会管理超时问题 ..
                                // org.apache.catalina.core.AsyncContextImpl 说明了真正的 异步上下文
                                // 会重新派发新请求到 servlet上下文中 。。
                                // org.apache.catalina.core.AsyncContextImpl.setStarted
                                //   this.request.getCoyoteRequest().action(ActionCode.ASYNC_START, this); 核心重点 ...

                                Duration.ofSeconds(5));
                    }
                })
        );
        MockHttpServletRequest request = new MockHttpServletRequest();
        // 支持异步(否则无法进行异步处理)
        request.setAsyncSupported(true);
        HttpServletRequest servletRequest = ServerRequest.create(
                        request,
                        Arrays.asList(
                                new StringHttpMessageConverter(),
                                new ByteArrayHttpMessageConverter(),
                                new MappingJackson2HttpMessageConverter()
                        )
                )
                .servletRequest();
        HandlerExecutionChain handler = routerFunctionMapping.getHandler(servletRequest);
        assert  handler != null;
        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        ModelAndView view = adapter.handle(
                servletRequest,
                servletResponse,
                handler.getHandler()
        );

        System.out.println(view);

        WebAsyncManager asyncManager = WebAsyncUtils.getAsyncManager(servletRequest);
        while (!asyncManager.hasConcurrentResult()) {
            TimeUnit.SECONDS.sleep(1);
        }

        System.out.println(servletResponse.getContentAsString());
    }
}
