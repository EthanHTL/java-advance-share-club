package club.smileboy.reactor.test;

import org.junit.jupiter.api.Test;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.context.ContextView;

import java.util.Arrays;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * @author jasonj
 * @date 2023/12/16
 * @time 14:04
 * @description
 **/
public class MonoTests {

    @Test
    public void tests() {
        // as 仅仅是一个 transformer 函数(但是它属于普通函数, 将一个mono 转换到 具体的普通类型)
        Integer as = Mono.just(1).as(Mono::block);
        System.out.println(as);



        Mono.usingWhen(Flux.defer(new Supplier<Publisher<Object>>() {
            @Override
            public Publisher<Object> get() {
                return Flux.concat(Mono.just(1),Mono.error(new IllegalStateException("错误")));
            }
        }), new Function<Object, Mono<?>>() {
            @Override
            public Mono<?> apply(Object o) {
//                return Mono.error( new ArithmeticException("出错了 ..."));
                throw new ArithmeticException("12312");
//                return Mono.empty();
//                return Mono.just(234);
            }
        }, new Function<Object, Publisher<?>>() {
            @Override
            public Publisher<?> apply(Object unused) {
//                System.out.println("成功结束了");
//                return Mono.empty();
                return Mono.error(new IllegalStateException("提交失败"));
            }
        }, new BiFunction<Object, Throwable, Publisher<?>>() {
            @Override
            public Publisher<?> apply(Object unused, Throwable throwable) {
//                System.out.println("获得错误了");
//                return Mono.error(throwable);
                return Mono.empty();
//
//                return Mono.error(new IllegalStateException("新的异常"));
            }
        }, new Function<Object, Publisher<?>>() {
            @Override
            public Publisher<?> apply(Object unused) {
//                System.out.println("取消了");
                return Mono.error(new IllegalStateException("取消了"));
            }
        }).subscribe(
                value -> {
                    // 有值Mono,没有错误, 则可以complete 以及 onNext ..
                    System.out.println("接收到结果");
                },
                err -> {

                    // 有值Mono,但是提交失败,则 error,值将抛弃 ..

                    System.out.println("接收到错误" +  err.getMessage());
                    if (err.getSuppressed() != null && err.getSuppressed().length > 0) {
                        System.out.println(Arrays.toString(err.getSuppressed()));
                    }
                },
                () -> {
                    System.out.println("完成了");
                }
        );


        // 官方的实例情况,也就是说,如果 usingWhen 的source 提供器如果没有弹射值而结束,则没必要清理
        // 但是如果在弹射值之后异常(这个弹射的本身也可能是异常),那么异常将会被吞没,当弹射的值是异常,则等价于没有提供值,也就没必要清理 ..

        // 当 resourceClosure 正确返回空值,那么如果完成清理回调正确执行,那么下游则可以 onNext 以及 complete
        // 同理,如果清理回调执行失败,那么下游则 同样onError (也就是error) error(弹射的值将会被吞没)

        // 同理如果,resourceClosure 返回了值,那么如果清理回调正确执行,那么下游onNext,complete
        // 同理,如果清理回调执行失败,则下游 error(弹射的值将会被吞没)

        // 如果resourceClosure 失败,那么则下游 error
        // 但是 错误清理回调有两种选择,一是吞咽异常,而是转换为另一种异常 .. 让下游 error(e/t) e表示原有异常, e-> t的映射异常


        // resourceClosure , 成功与否 决定是否完成还是error
        // asyncComplete 成功与否(决定是否进入 error 还是 成功)
        // asyncError 成功与否(决定异常是原始的还是压制的)

    }

    @Test
    public void contextTests() {

        Flux.deferContextual(contextView -> {
//            System.out.println(contextView.get("data").toString());
            return Flux.just(contextView.get(String.class));
        })

                .concatMap(e -> {
                    return Flux.deferContextual(ctx -> {
                                return Flux.just(ctx.get(String.class));
                            })
                                    .doOnNext(System.out::println)
                            ;
                })

                .contextWrite(context -> {
                    return context.put(String.class,"123");
                })
                .subscribe();

    }

}
