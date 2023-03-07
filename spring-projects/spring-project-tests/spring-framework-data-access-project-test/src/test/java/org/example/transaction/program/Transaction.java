package org.example.transaction.program;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class Transaction {

  private  final TransactionalOperator rxtx;

  @Autowired
  public Transaction(final TransactionalOperator rxtx) {
    this.rxtx = rxtx;
  }

  public  <T> Mono<T> withRollback(final Mono<T> publisher) {
    return rxtx.execute(tx -> {
      tx.setRollbackOnly();
      return publisher;
    })
    .next();
  }

  public  <T> Flux<T> withRollback(final Flux<T> publisher) {
    return rxtx.execute(tx -> {
      tx.setRollbackOnly();
      return publisher;
    });
  }
}
