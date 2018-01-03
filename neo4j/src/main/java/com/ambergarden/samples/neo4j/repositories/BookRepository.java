package com.ambergarden.samples.neo4j.repositories;

import com.ambergarden.samples.neo4j.entities.Book;
import org.springframework.data.neo4j.repository.GraphRepository;
import org.springframework.stereotype.Repository;

/**
 * The repository to perform CRUD operations on book entities
 */
@Repository
public interface BookRepository extends GraphRepository<Book> {
}