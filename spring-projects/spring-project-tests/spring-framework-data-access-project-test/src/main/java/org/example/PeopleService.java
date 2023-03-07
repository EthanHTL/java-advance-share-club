package org.example;

import reactor.core.publisher.Mono;

public interface PeopleService {

    Mono<People> rollback();


}
