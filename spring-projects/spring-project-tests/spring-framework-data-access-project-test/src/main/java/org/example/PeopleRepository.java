package org.example;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface PeopleRepository extends ReactiveCrudRepository<People,String> {
}
