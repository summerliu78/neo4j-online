package com.ambergarden.samples.neo4j.entities;

import org.neo4j.ogm.annotation.GraphId;

/**
 * Created with IntelliJ IDEA.
 * User: YKDZ5901703
 * Date: 2017/10/26
 * Time: 17:33
 * To change this template use File | Settings | File Templates.
 */
public class Group {
    @GraphId
    private Long id;
    private String name;

    private  String createTime;

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getCreateTime() {
        return createTime;
    }

    public Group() {
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
}