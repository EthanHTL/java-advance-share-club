package org.example;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

public class DefaultPeopleService implements PeopleService{

    @Autowired
    private PeopleRepository peopleRepository;
    @Override
    @Transactional
    public Mono<People> rollback() {
        return peopleRepository.findAll().take(1)
                .map(ele -> new People(ele.getId(), ele.getUsername(), "123456788"))
                .flatMap(peopleRepository::save)
                .doOnNext(ele -> {
                    throw new IllegalArgumentException("rollback");
                })
                .next();
    }
}
