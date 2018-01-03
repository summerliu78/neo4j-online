package com.ambergarden.samples.neo4j.entities;

/**
 * Created by ThinkPad on 2017/11/16.
 */
public class Message1 {
    private   int scdata_code  ;
    private   String  scdata_msg;

    public void setScdata_code(int scdata_code) {
        this.scdata_code = scdata_code;
    }

    public void setScdata_msg(String scdata_msg) {
        this.scdata_msg = scdata_msg;
    }

    public int getScdata_code() {
        return scdata_code;
    }

    public String getScdata_msg() {
        return scdata_msg;
    }
}
