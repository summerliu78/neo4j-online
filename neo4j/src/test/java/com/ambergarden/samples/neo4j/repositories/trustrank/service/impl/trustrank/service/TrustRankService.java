package com.ambergarden.samples.neo4j.repositories.trustrank.service.impl.trustrank.service;

import java.util.Map;

/**
 * Created by lw on 2017/12/18.
 */
public interface TrustRankService {

    //测试读取文件
    public void getDataFromLocal(String string);


    //查询mongo中是否存在输入数据
    public void checkDataFromMongoOrElseTest(String mobile);

    //删除mongo中数据
    public void  deleteMongoDataTest(String mobile);

    //测试已经存在数据
    public void getRankExistTest(Map<String, String> result);

    //测试不存在数据
    public void getRankNotExistTest(String mobile);

    //测试输入不规范数据
    public void getNonConformityTest();

    //测试spring cron定时器是否可以定时更新
    public void cronTest();







}
