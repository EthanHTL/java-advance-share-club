package org.example.transaction.program;

import io.r2dbc.h2.H2ConnectionConfiguration;
import io.r2dbc.h2.H2ConnectionFactory;
import io.r2dbc.h2.H2ConnectionFactoryProvider;
import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.ConnectionFactoryOptions;
import io.r2dbc.spi.Option;
import org.example.DefaultPeopleService;
import org.example.People;
import org.example.PeopleRepository;
import org.example.PeopleService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscription;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.r2dbc.config.AbstractR2dbcConfiguration;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;
import org.springframework.data.relational.core.query.Query;
import org.springframework.r2dbc.connection.R2dbcTransactionManager;
import org.springframework.r2dbc.connection.TransactionAwareConnectionFactoryProxy;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.transaction.ReactiveTransaction;
import org.springframework.transaction.ReactiveTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.reactive.TransactionCallback;
import org.springframework.transaction.reactive.TransactionContextManager;
import org.springframework.transaction.reactive.TransactionalOperator;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import reactor.core.CoreSubscriber;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

import java.util.UUID;

/**
 * 响应式事务在 tcf 框架中，没有测试成功过 ..
 *
 * 关于取消信号 :
 * https://github.com/spring-projects/spring-framework/issues/25091
 *
 * 关于@Transactional
 * https://github.com/spring-projects/spring-framework/issues/24226
 */
@SpringJUnitConfig
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class TransactionOperatorTests {

    @Configuration
    @EnableTransactionManagement
    @EnableR2dbcRepositories(basePackages = "org.example")
    public static class Config extends AbstractR2dbcConfiguration {

        @Bean
        public Transaction transactionComponent() {
            return new Transaction(transactionOperator());
        }

        @Bean
        public TransactionalOperator transactionOperator() {
            return TransactionalOperator.create(reactiveTransactionManager());
        }

        @Override
        public ConnectionFactory connectionFactory() {

            return  new H2ConnectionFactoryProvider()
                    .create(ConnectionFactoryOptions.builder()
                            .option(ConnectionFactoryOptions.USER,"root")
                            .option(ConnectionFactoryOptions.PASSWORD,"123456")
                            .option(ConnectionFactoryOptions.PROTOCOL,"mem")
                            .option(ConnectionFactoryOptions.DATABASE,"testdb")
                            // ?options=DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
                            .option(H2ConnectionFactoryProvider.URL,"r2dbc:h2:mem:///testdb")
                            .build());
        }

        @Bean
        public ReactiveTransactionManager reactiveTransactionManager() {
            return new R2dbcTransactionManager(connectionFactory());
        }

//        @Bean
//        public DatabaseClient databaseClient() {
//            return DatabaseClient.create(connectionFactory());
//        }

        @Bean
        PeopleService peopleService() {
            return new DefaultPeopleService();
        }

        @Bean
        public R2dbcEntityTemplate r2dbcEntityTemplate() {
            return new R2dbcEntityTemplate(connectionFactory());
        }

    }

    @Autowired
    ReactiveTransactionManager reactiveTransactionManager;

    @Autowired
    DatabaseClient databaseClient;

    @Autowired
    R2dbcEntityTemplate template;

    @Autowired
    PeopleRepository peopleRepository;

    @Autowired
    Transaction transaction;

    @Autowired
    TransactionalOperator operator;

    @Autowired
    PeopleService peopleService;

    @BeforeAll
    public void before() {
        databaseClient.sql("create table if not exists people(\n" +
                "`id` varchar(20) default '',\n" +
                "    `username` varchar(20) default '',\n" +
                "    `password` varchar(20) default ''\n" +
                ")")
                .fetch()
                .rowsUpdated()
                .block();

        databaseClient.sql("delete from people")
                        .fetch().rowsUpdated().block();

        databaseClient.sql("insert into people values ('1','jasonj','20')")
                .fetch()
                .rowsUpdated()
                .block();
    }

//    @Test
//    public void test() {
//
//        TransactionalOperator transactionalOperator = TransactionalOperator.create(reactiveTransactionManager);
//
//
//        Flux<People> userFlux = template.select(Query.empty(), People.class).buffer()
//                .flatMap(list -> Flux.just(list.toArray(People[]::new)))
//                .doOnEach(new Consumer<Signal<People>>() {
//                    @Override
//                    public void accept(Signal<People> userSignal) {
//                        People user = userSignal.get();
//                        System.out.println(user);
//                    }
//                })
//                .filter(user -> user.getUsername().equals("jasonj"))
//                .map(user -> new People(user.getId(),user.getUsername(),"45678"))
//                .transform(flux -> {
//                    Mono<People> update = template.update(flux.blockFirst())
//                                    .doOnNext(ele -> {
//                                        System.out.println("处理" + ele);
//                                    });
//
//                    return update;
//
//                });
//
//
//
//        transactionalOperator.transactional(userFlux).blockFirst();
//
//        System.out.println(template.select(Query.empty(), People.class).blockFirst());
//    }


    @Test
    public void rollback() {

        // 新事务
        Disposable subscribe = peopleService.rollback().subscribe();

        // 需要取消

        subscribe.dispose();



        template.selectOne(Query.empty(),People.class)
                .subscribe(System.out::println);
    }

    @Test
    public void callbackRollback() {
        TransactionalOperator transactionalOperator = TransactionalOperator.create(reactiveTransactionManager);
        transactionalOperator.execute(new TransactionCallback<People>() {
            @Override
            public Publisher<People> doInTransaction(ReactiveTransaction status) {
                return template.select(Query.empty(), People.class)
                        .next()
                        .transform(peopleMono ->  peopleMono.map(ele -> new People(ele.getId(), ele.getUsername(), "4567")))
                        .map(people -> {
                            template.update(people).subscribe();
                            return people;
                        })
                        .map(people -> {
                            System.out.println("设置回滚 !!!");
                            status.setRollbackOnly();
                            return people;
                        });

            }
        }).subscribe();

        transactionalOperator.execute(new TransactionCallback<People>() {
            @Override
            public Publisher<People> doInTransaction(ReactiveTransaction status) {
                return template.select(Query.empty(),People.class)
                        .next();
            }
        }).subscribe(System.out::println);
    }

    /**
     * 依旧回滚失败 ...
     */
    @Test
    public void rollbackByManager() {
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
// explicitly setting the transaction name is something that can be done only programmatically
        def.setName("SomeTxName");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        Context context = TransactionContextManager.createTransactionContext().apply(Context.empty());
        Mono<ReactiveTransaction> reactiveTx = reactiveTransactionManager.getReactiveTransaction(def);

        reactiveTx.flatMap(status -> {


            Mono<People> tx = template.select(Query.empty(), People.class)
                    .take(1)
                    .doOnNext(ele -> ele.setPassword("567890"))
                    .next()
                    .flatMap(template::update)
                    .doOnNext(ele -> System.out.println("更新后的数据"+ ele) )
                    .doOnNext(ele -> {
                        throw new IllegalArgumentException("12312");
                    });

            // Mono<Void> 将会导致 后续下游流无法执行 ..

            return tx.then(reactiveTransactionManager.commit(status).then(Mono.empty()))
                    .onErrorResume(ex -> {
                        ex.printStackTrace();
                        return reactiveTransactionManager.rollback(status).then(Mono.error(ex));
                    });
        })
                .onErrorResume(e -> {
                    e.printStackTrace();
                    return Mono.just(new People("","",""));
                })
                .doOnNext(ele -> {
                    System.out.println("123141");
                })
                .then()
                .subscribe((ele) -> {
                    System.out.println("result " + ele);
                },error -> {
                    error.printStackTrace();
                    System.out.println("出错了 !!!");
                },() -> {
                    System.out.println("完成了 !!!");
                },context);


        template.select(Query.empty(),People.class).take(1)
                .next()
                .subscribe(System.out::println);
    }
}
