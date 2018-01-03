package com.ambergarden.samples.neo4j.entities;

import org.neo4j.ogm.annotation.GraphId;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

/**
 * Created with IntelliJ IDEA.
 * User: YKDZ5901703
 * Date: 2017/10/26
 * Time: 17:33
 * To change this template use File | Settings | File Templates.
 */
    @NodeEntity
    public class User {
        @GraphId
        private Long id;
        private String name;
        private String age;


        @Relationship(type = "从属", direction = Relationship.INCOMING)
        private Group group;

    public void setAge(String age) {
        this.age = age;
    }

    public String getAge() {
        return age;
    }

    public User() {
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Group getGroup() {
            return group;
        }

        public void setGroup(Group group) {
            this.group = group;
        }
    }

