package org.example.transaction.program;

import org.example.Main;
import org.example.People;
import org.example.PeopleRepository;
import org.example.PeopleService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Query;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.transaction.ReactiveTransaction;
import org.springframework.transaction.ReactiveTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.reactive.TransactionCallback;
import org.springframework.transaction.reactive.TransactionalOperator;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * tcf 框架 事务测试 失效(针对响应式事务来说) ..
 *
 * 这个测试无效 ...
 */
@SpringBootTest(classes = Main.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class MainTests {

    @Autowired
    ReactiveTransactionManager reactiveTransactionManager;

    @Autowired
    DatabaseClient databaseClient;

    @Autowired
    private PeopleRepository repository;

    @Autowired
    R2dbcEntityTemplate template;

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


    @Test
    public void rollback() {

        // 新事务

        TransactionalOperator transactionalOperator = TransactionalOperator.create(reactiveTransactionManager, new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));
        transactionalOperator.transactional(
                        template.selectOne(Query.empty(), People.class)
                                .doOnNext(ele -> {
                                    System.out.println("获取的数据" + ele);
                                })
                                .flatMap(ele -> template.update(new People(ele.getId(),ele.getUsername(), UUID.randomUUID().toString().substring(0,6))))
                                .map(ele -> {
                                    throw new IllegalArgumentException("异常信息");
                                })
                )
                .then()

                .onErrorResume(ele -> {
                    System.out.println("恢复异常信息 ！！！");
                    return Mono.empty();
                })
                .then(template.selectOne(Query.empty(),People.class))
                .subscribe(System.out::println);
        System.out.println("重新抓取数据 ..............");
        transactionalOperator.execute(new TransactionCallback<People>() {
            @Override
            public Publisher<People> doInTransaction(ReactiveTransaction status) {
                return template.select(Query.empty(), People.class);
            }
        }).next().subscribe(System.out::println);
    }


    @Test
    public void serviceRollback() {
     peopleService.rollback().subscribe();

     template.selectOne(Query.empty(),People.class).subscribe(System.out::println);
    }
}
