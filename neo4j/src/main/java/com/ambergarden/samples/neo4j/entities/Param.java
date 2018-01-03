package com.ambergarden.samples.neo4j.entities;

import java.sql.Timestamp;

/**
 *   既是入参 ，又是出参
 * User: YKDZ5901703
 * Date: 2017/7/20
 * Time: 15:54
 * To change this template use File | Settings | File Templates.
 */
public class Param {
    public Param() {
    }
    private String loanId;//订单号
    private String  orderId;//订单号
    private String userId;//用户id
    private String taskId;//任务号
    private String channel;//渠道
    private String callbackUrl;//回调地址
    private String type;    //类型
    private String userPhone;//手机号码1
    private String userPhone2; //手机号码2
    private String userIdCard;  // 身份证
    private String applyTime;//授信申请时间
    private String  result ; //计算结果

    private String  createTime ;
    private String createTimeStr;
    private String updateTime;
    private String dataType  ;// 聚信立或者通讯录  ：   通讯录  ， 聚信立

    private String uniqueId;
    private String startTime; //请求开始时间戳

    public String getApplyTime() {
        return applyTime;
    }

    public void setApplyTime(String applyTime) {
        this.applyTime = applyTime;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }

    public String getUniqueId() {
        return uniqueId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getOrderId() {
        return orderId;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public void setCreateTimeStr(String createTimeStr) {
        this.createTimeStr = createTimeStr;
    }

    public String getCreateTimeStr() {
        return createTimeStr;
    }

    private int  expirel ;//    超时时间

    private int status ;  //数据有效   1：有效  0：过期

    public void setType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getStatus() {
        return status;
    }

    public void setExpirel(int expirel) {
        this.expirel = expirel;
    }

    public int getExpirel() {
        return expirel;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }

    public String getCreateTime() {
        return createTime;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getResult() {
        return result;
    }

    public void setUserIdCard(String userIdCard) {
        this.userIdCard = userIdCard;
    }

    public String getUserIdCard() {
        return userIdCard;
    }

    public void setUserPhone2(String userPhone2) {
        this.userPhone2 = userPhone2;
    }

    public String getUserPhone2() {
        return userPhone2;
    }

    public String getUserPhone() {
        return userPhone;
    }

    public void setUserPhone(String userPhone) {
        this.userPhone = userPhone;
    }

    public void setLoanId(String loanId) {
        this.loanId = loanId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public void setCallbackUrl(String callbackUrl) {
        this.callbackUrl = callbackUrl;
    }



    public String getLoanId() {
        return loanId;
    }

    public String getUserId() {
        return userId;
    }

    public String getTaskId() {
        return taskId;
    }

    public String getChannel() {
        return channel;
    }

    public String getCallbackUrl() {
        return callbackUrl;
    }


}
