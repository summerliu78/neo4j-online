package com.ambergarden.samples.neo4j.repositories;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import com.ambergarden.samples.neo4j.entities.Person;
import com.ambergarden.samples.neo4j.service.DataServiceI;
import com.ambergarden.samples.neo4j.service.impl.DataServiceImpl;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.*;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "classpath*:/spring/applicationContext.xml" })
public class PersonRepositoryTest {
   private static String TEST_PERSON_NAME_1 = "Person1";
   private static String TEST_PERSON_NAME_2 = "Person2";

   @Autowired
   private PersonRepository personRepository;
    @Autowired
    private DataServiceI dataServiceI;

   @Test
   public void test1() {
      String  json = dataServiceI.testMethod();
      System.out.println(json);
   }
//   @Test
//   public void testGang(){
//      Person p=new Person();
//      p.setPid("18801118468");
//      Map<String,Integer> inoutall=dataServiceI.nodeInOutAndAllCount(p);
//      System.out.println("in="+inoutall.get("IN"));
//      System.out.println("out="+inoutall.get("OUT"));
//      System.out.println("all="+inoutall.get("ALL"));
////      System.out.println("gang="+gang);
//
//   }
   @Test
   public void testUpdate(){
      Person p=new Person();
      p.setPid("15210540833");
      p.setSid("15637848712");
      p.setAvgTime("0.00");
      p.setCallTime("1");
      dataServiceI.updatePerson(p);

   }
   @Test
   public void testSelect(){
      Person p=new Person();
      p.setPid("13910843622");
      List<Person> list=dataServiceI.selectPerson(p);
      List<Person> list2=dataServiceI.find2DNum(p);
      System.out.println("******: " + list);
   }
   @Test
   public void testInsert(){
      Person p=new Person();
      p.setPid("18601943325");
      p.setSid("17702237724");
      p.setCallTime("1");
      p.setAvgTime("2.0");
      dataServiceI.insertPerson(p);
   }
   @Test
   public void testRelation(){
      boolean exit=dataServiceI.exitRelation("15810386897","18801118468");
      System.out.println(exit);
   }
   @Test
   public void testAll1(){
      DataServiceImpl impl=new DataServiceImpl();
      Person p=new Person();
      p.setPid("18601166451");
      p.setApplyTime("2017-12-13 16:22:16");
      p.setChannel("weibo");
      List<List<Person>> gangs=dataServiceI.selectGang(p);
      List<Person> tmp=new ArrayList<>();
      int max=0;
      for(List<Person> l:gangs){
         if(l.size()>=max){
            max=l.size();
            tmp=l;
         }
      }
      StringBuffer str=new StringBuffer();
      String mobiles="";
      if(CollectionUtils.isNotEmpty(tmp)){
         for(Person pe:tmp){
            str.append(",").append(pe.getPid());
            mobiles=str.toString().substring(1);
         }
      }
      if(StringUtils.isNoneBlank(mobiles)){
         System.out.println(mobiles);
         /*int apply=impl.applyNum(p,tmp,"0","1");
         int due=impl.overDueNum(p,tmp,"0","1");
         int declineNum=impl.declineNum(p,tmp,"0","1");
         int loanButUnpaidNum=impl.loanButUnpaidNum(p,tmp,"0","1");
         System.out.println(apply==2);
         System.out.println(due==5);
         System.out.println(declineNum==3);
         System.out.println(loanButUnpaidNum==5);*/

      }

   }
   @Test
   public void testAll2(){
       Person p=new Person();
       p.setChannel("weibo");
       p.setApplyTime("2017-12-13 16:22:16");
       p.setPid("18601166451");
       p.setUser_id("28710124380");
       String json = dataServiceI.selectCallRecords(p, true, null, "18601166451");
       JSONObject obj=new JSONObject(json);
//       System.out.println(obj);

       assertEquals( obj.getInt("0d_overdue_num_current"),4);
       assertEquals( obj.getInt("2d_decline_num_current"),5);
       assertEquals( obj.getDouble("2d_overdue_amount"),2573043.02,10.0);
       assertEquals( obj.getInt("mobile_decline_num"),95);
       assertEquals( obj.getInt("1d_decline_num_current"),7);
       assertEquals( obj.getInt("1d_unpaid_num_current"),7);
       assertEquals( obj.getInt("1d_overdue_num"),10);
       assertEquals( obj.getInt("mobile_ corresponding_multi_name_num"),1);
       assertEquals( obj.getInt("0d_unpaid_num_current"),4);
       assertEquals( obj.getDouble("1d_overdue_amount"),24333.33,0.01);
       assertEquals( obj.getInt("2d_overdue_num"),15);
       assertEquals( obj.getInt("1d_renew_loan_num"),3);
       assertEquals( obj.getInt("0d_mobile_address_num"),3);
       assertEquals( obj.getInt("2d_decline_num"),28);
       assertEquals( obj.getInt("1d_decline_num"),25);
       assertEquals( obj.getInt("2d_renew_loan_num"),6);
       assertEquals( obj.getInt("2d_unpaid_num_current"),4);
       assertEquals( obj.getInt("2d_hit_black_num"),4);
       assertEquals( obj.getInt("1d_apply_num"),14);
       assertEquals( obj.getInt("0d_apply_num_current"),1);
       assertEquals( obj.getInt("1d_apply_num_current"),5);
       assertEquals( obj.getInt("2d_unpaid_num"),12);
       assertEquals( obj.getInt("urgent_unpaid_num"),1);
       assertEquals( obj.getInt("1d_contact_num"),9);
       assertEquals( obj.getInt("1d_hit_black_num"),4);
       assertEquals( obj.getInt("0d_contact_num"),4);
       assertEquals( obj.getInt("0d_decline_num_current"),2);
       assertEquals( obj.getInt("2d_contact_num"),8);
       assertEquals( obj.getInt("2d_apply_num_current"),4);
       assertEquals( obj.getInt("urgent_corresponding_applicant_num"),3);
       assertEquals( obj.getInt("2d_apply_num"),22);

   }
/*   @Test
   public void testCRUDPerson() {
      Person person = new Person();
      person.setName(TEST_PERSON_NAME_1);
      person = personRepository.save(person);
      assertNotNull(person);
      assertNotNull(person.getId());
      assertEquals(TEST_PERSON_NAME_1, person.getName());

      Long originalId = person.getId();
      person.setName(TEST_PERSON_NAME_2);
      person = personRepository.save(person);
      assertEquals(originalId, person.getId());
      assertEquals(TEST_PERSON_NAME_2, person.getName());
      person = personRepository.findOne(originalId);

      personRepository.delete(person);
      person = personRepository.findOne(originalId);
      assertNull(person);
   }*/
}
