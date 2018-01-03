package com.ambergarden.samples.neo4j.entities;

import org.neo4j.ogm.annotation.GraphId;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

import java.util.Set;

/**
 * Entity which represents a book
 */
@NodeEntity
public class Book {
   private String name;

   public String getName() {
      return name;
   }


   @Relationship(type="WRITER_OF", direction=Relationship.INCOMING)
   private Set<WriterOf> writers;

   @Relationship(type="READER_OF", direction=Relationship.INCOMING)
   private Set<Person> readers;

   private String description;

   @GraphId
   private Long id;

   public Long getId() {
      return id;
   }

   public void setId(Long id) {
      this.id = id;
   }

   public void setName(String name) {
      this.name = name;
   }
   public String getDescription() {
      return description;
   }

   public void setDescription(String description) {
      this.description = description;
   }
   public Set<WriterOf> getWriters() {
      return writers;
   }

   public void setWriters(Set<WriterOf> writers) {
      this.writers = writers;
   }

   public Set<Person> getReaders() {
      return readers;
   }

   public void setReaders(Set<Person> readers) {
      this.readers = readers;
   }
}