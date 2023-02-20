package org.example.spring.test.entity;

import org.springframework.data.repository.CrudRepository;

public interface UserRepository extends CrudRepository<User,String> {

    public User findByNameIs(String user);
}
