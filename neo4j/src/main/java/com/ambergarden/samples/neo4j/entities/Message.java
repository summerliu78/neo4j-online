package com.ambergarden.samples.neo4j.entities;

/**
 * Created with IntelliJ IDEA.
 * User: YKDZ5901703
 * Date: 2017/10/31
 * Time: 14:30
 * To change this template use File | Settings | File Templates.
 */
public class Message {
    String  msg;

    public Message(String msg){
        this.msg=msg;
    }
    public String toString(){
        return "{Error:"+msg+" is null }";
    }
}
