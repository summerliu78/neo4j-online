package com.ambergarden.samples.neo4j.entities;


import org.neo4j.ogm.annotation.GraphId;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

import java.sql.Timestamp;
import java.util.Set;

/**
 * Entity which represents a person
 */
@NodeEntity
public class Person {
   @GraphId
   private Long id;
   private String user_id;
   private String pid;
   private String avgTime="0.00";   // 通话平均时长
   private String  callTime="0"; // 通话次数
   private String sid;
   private String channel = "";
   private String applyTime = "";
   private  String type = "";
   private  String loanApplyTime = "";

   public String getType() {
      return type;
   }

   public void setType(String type) {
      this.type = type;
   }

   public String getSourcePid() {
      return sourcePid;
   }

   public void setSourcePid(String sourcePid) {
      this.sourcePid = sourcePid;
   }

   private String sourcePid;
    public String getApplyTime() {
        return applyTime;
    }

    public void setApplyTime(String applyTime) {
        this.applyTime = applyTime;
    }

    public void setChannel(String channel) {
      this.channel = channel;
   }

   public String getChannel() {
      return channel;
   }

   public void setSid(String sid) {
      this.sid = sid;
   }

   public String getSid() {
      return sid;
   }

   public void setAvgTime(String avgTime) {
      this.avgTime = avgTime;
   }

   public void setCallTime(String callTime) {
      this.callTime = callTime;
   }

   public String getAvgTime() {
      return avgTime;
   }

   public String getCallTime() {
      return callTime;
   }

   public void setPid(String pid) {
      this.pid = pid;
   }

   public String getPid() {
      return pid;
   }

   public Long getId() {
      return id;
   }
   public void setId(Long id) {
      this.id = id;
   }

   public String getUser_id() {
      return user_id;
   }
   
   public void setUser_id(String user_id) {
      this.user_id = user_id;
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;

      Person person = (Person) o;

      return pid.equals(person.pid);
   }

   @Override
   public int hashCode() {
      return pid.hashCode();
   }

   public String getLoanApplyTime() {
      return loanApplyTime;
   }

   public void setLoanApplyTime(String loanApplyTime) {
      this.loanApplyTime = loanApplyTime;
   }
}
