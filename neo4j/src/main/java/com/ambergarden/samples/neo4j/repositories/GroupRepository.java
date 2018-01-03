package com.ambergarden.samples.neo4j.repositories;

import com.ambergarden.samples.neo4j.entities.Group;
import com.ambergarden.samples.neo4j.entities.User;
import org.springframework.data.neo4j.repository.GraphRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * The repository to perform CRUD operations on person entities
 */
@Repository
public interface GroupRepository extends GraphRepository<Group> {
    List<Group> findByName(String name);

}