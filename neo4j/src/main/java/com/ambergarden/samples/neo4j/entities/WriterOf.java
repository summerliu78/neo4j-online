package com.ambergarden.samples.neo4j.entities;


import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.GraphId;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.StartNode;

import java.util.Date;

@RelationshipEntity(type="WRITER_OF")
public class WriterOf {
   @GraphId
   private Long id;

   public Long getId() {
      return id;
   }


   @StartNode
   private Person writer;

   @EndNode
   private Book book;

   private Date startDate;

   private Date endDate;

   public void setId(Long id) {
      this.id = id;
   }

   public Person getWriter() {
      return writer;
   }

   public void setWriter(Person writer) {
      this.writer = writer;
   }

   public Book getBook() {
      return book;
   }

   public void setBook(Book book) {
      this.book = book;
   }

   public Date getStartDate() {
      return startDate;
   }

   public void setStartDate(Date startDate) {
      this.startDate = startDate;
   }

   public Date getEndDate() {
      return endDate;
   }

   public void setEndDate(Date endDate) {
      this.endDate = endDate;
   }
}