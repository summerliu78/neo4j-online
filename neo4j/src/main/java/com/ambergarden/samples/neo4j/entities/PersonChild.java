package com.ambergarden.samples.neo4j.entities;


import org.neo4j.ogm.annotation.GraphId;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

/**
 * Entity which represents a person
 */
@NodeEntity
public class PersonChild {
   @GraphId
   private Long id;
   private String pid;


   @Relationship(type = "CALL_OUT", direction = Relationship.INCOMING)
   private Person  outPerson;
/*
   @Relationship(type = "被叫", direction = Relationship.OUTGOING)
   private Person  inPerson;*/

   public void setPid(String pid) {
      this.pid = pid;
   }

   public String getPid() {
      return pid;
   }

   public void setOutPerson(Person outPerson) {
      this.outPerson = outPerson;
   }


   public Person getOutPerson() {
      return outPerson;
   }

   public Long getId() {
      return id;
   }

   public void setId(Long id) {
      this.id = id;
   }
}