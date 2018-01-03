package com.ambergarden.samples.neo4j.repositories;

import com.ambergarden.samples.neo4j.entities.Person;
import com.ambergarden.samples.neo4j.entities.User;
import org.springframework.data.neo4j.repository.GraphRepository;
import org.springframework.stereotype.Repository;

/**
 * The repository to perform CRUD operations on person entities
 */
@Repository
public interface UserRepository extends GraphRepository<User> {
    User findByName(String name);
}