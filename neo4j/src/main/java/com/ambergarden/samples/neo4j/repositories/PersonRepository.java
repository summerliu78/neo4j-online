package com.ambergarden.samples.neo4j.repositories;

import com.ambergarden.samples.neo4j.entities.Group;
import com.ambergarden.samples.neo4j.entities.Person;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.GraphRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * The repository to perform CRUD operations on person entities
 */
@Repository
public interface PersonRepository extends GraphRepository<Person> {

    //查找团伙
    @Query("MATCH res=(p)-[r:CALL_OUT *1..5]->(p:Person) WHERE p.pid={0} RETURN res")
    List<List<Object>> selectGang(String pid);

    //查找机主电话
    @Query("MATCH (p1:Person)-[r:CALL_OUT]-(p2:Person)   where  p1.pid={0} RETURN distinct p2 ")
    List<Person> findAllByPhoneCallInfo(String phone);

    //查找二度联系人个数
    @Query("MATCH (p1:Person)-[r:CALL_OUT*2]-(p2:Person)  where p1.pid={0} return distinct p2 ")
    List<Person> find2DNum(String phone);


    @Query("MATCH (p2:Person)-[r:CALL_OUT]->(p1:Person)   where  p1.pid={0} RETURN r.avg_time ")
    String findAllByPhoneGetAvgTime(String phone);

    @Query("MATCH (p2:Person)-[r:CALL_OUT]->(p1:Person)   where  p1.pid={0} RETURN r.call_times")
    String findAllByPhoneGetCallTime(String phone);

    @Query("MATCH (p2:Person)-[r:CALL_OUT]->(p1:Person)   where  p2={0} and p1.pid={0} RETURN  r.call_times")
    String findSigleCallTime(String sourceId,String targetId);
    @Query("MATCH (p2:Person)-[r:CALL_OUT]->(p1:Person)   where  p2={0} and p1.pid={0} RETURN r.avg_time ")
    String findSingleAvgTime(String sourceId,String targetId);
    //更新关系属性
    @Query("MATCH (p1:Person)-[r:CALL_OUT]->(p2:Person)   where  p2.pid={0} set r.call_times={1}, r.avg_time={2} ")
    public void updateNode(String param, String callTime, String avgTime);

    //创建机主节点
    @Query("merge (p:Person{pid:{0}})")
    public void createRootNode(String pid);

    //创建子节点
    @Query("merge (p:Person{pid:{0}})")
    public void createNode(String sid);

    //创建关系
    @Query("MATCH (a:Person),(b:Person) WHERE a.pid = {0} AND b.pid ={1}  merge (a)-[r:CALL_OUT {avg_time:{2},call_times:{3}}]->(b) RETURN r")
    public void mergeNode(String mPid, String sPid, String callTime, String avgTime);
    @Query("match(a:Person) where a.pid={0} return a")
    public Person exitNode(String mobile);
    @Query("match(a:Person)-[r:CALL_OUT]->(b:Person) where a.pid={0} and b.pid={1} return a")
    public Person exitRelation(String pid,String sid);
    /**
     * // 先创建节点,然后建立关系
     create (p:Person{pid:"p133"})

     MATCH (a:Person),(b:Person)
     WHERE a.pid = 'p18282898809' AND b.pid = 'p133'
     merge (a)-[r:CALL_OUT {avg_time:23.7,call_times:3}]->(b) RETURN r

     // 更新属性
     match (a)-[r:CALL_OUT]->(b:Person) where b.pid='p18892895038' set r.call_times=7 return r
     */
}
