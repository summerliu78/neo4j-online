package com.ambergarden.samples.neo4j.service;

import com.ambergarden.samples.neo4j.entities.Person;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: YKDZ5901703
 * Date: 2017/7/25
 * Time: 14:45
 * To change this template use File | Settings | File Templates.
 */
public interface DataServiceI {
    public String testMethod();

    //查询机主号码信息
    public List<Person> selectPerson(Person person);

    public List<Person> find2DNum(Person person);

    public String  timesAndAvgEn (Map<String,String> result);
    //更新节点
    public String updatePerson(Person person);

    //插入新节点
    public List<Person> insertPerson(Person person);

    //查询团伙信息
    public List<List<Person>> selectGang(Person person);

    //计算各种决策
    public String selectCallRecords(Person person, boolean neo4j_ok, Map<String, List<String>> phone_1D_map, String phone);

    //通过userid获取通话详单
    public String getRecordsByUserId(String callDetails);

    public String getRecordsByUserIdAndChannel(String callDetails);

    public void saveRecordsByUserIdAndChannel(Map<String,String> result);
    public  void remove(Map<String,String> result);
    //判断节点是否存在
    public boolean exitNode(String pid);
    //判断两个节点的关系是否存在
    public boolean exitRelation(String pid,String sid);
    public String getApplyTime(Map<String,String> map);
    public Map<String,Integer> nodeInOutAndAllCount(Person person, List<List<Person>> zeroPersonList);
    /**
     * 根据当前用户信息和flag查询联系人
     *
     * @param person
     * @param flag
     * @return
     */
    public List<Person> queryContactsByPersonAndFlag(Person person, String flag);

    /**
     * 获取当前用户授信申请时间、借款申请时间
     *
     * @param map
     * @return
     * @olivia.wei
     */
    void getCurrentUserTime(Map<String, Object> map);
}
