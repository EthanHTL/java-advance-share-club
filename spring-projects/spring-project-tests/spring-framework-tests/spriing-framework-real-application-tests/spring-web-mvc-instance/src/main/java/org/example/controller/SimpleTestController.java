package org.example.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("api/simple/test")
public class SimpleTestController {

    @PutMapping
    public String formDataEarlyResolve(@RequestParam Map<String,Object> maps) {
        return "key=" + maps.getOrDefault("key","unknown");
    }

    @PostMapping
    public String formDataHandle(@RequestParam Map<String,Object> maps) {
        return "key=" +maps.getOrDefault("key","unknown");
    }
    @GetMapping
    public String get() {
        return "ok";
    }



    @GetMapping("async/callable")
    public Callable<String> asyncCallable() {

        RequestContextHolder.getRequestAttributes().setAttribute("requestName","currentRequest",RequestAttributes.SCOPE_REQUEST);

        return new Callable<String>() {
            @Override
            public String call() throws Exception {
                RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();

                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {
                    // pass ..
                }

                return requestAttributes.getAttribute("requestName",RequestAttributes.SCOPE_REQUEST).toString();
            }
        };
    }

    @GetMapping("async/deferred")
    public DeferredResult<String> asyncDeferred() {

        RequestContextHolder.getRequestAttributes().setAttribute("requestName","currentRequest",RequestAttributes.SCOPE_REQUEST);
        DeferredResult<String> deferredResult = new DeferredResult<>();

        CompletableFuture.runAsync(() -> {
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                // pass ..
            }

            System.out.println("执行异步 deferred");

            RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
            if(requestAttributes != null) {
                deferredResult.setResult(Objects.requireNonNullElse(requestAttributes.getAttribute("requestName",RequestAttributes.SCOPE_REQUEST),"deferredRequest").toString());
            }
            else {
                deferredResult.setResult("deferredRequest");
            }
        });


        return deferredResult;
    }
}
