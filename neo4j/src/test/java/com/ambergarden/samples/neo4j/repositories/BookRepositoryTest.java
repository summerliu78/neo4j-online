package com.ambergarden.samples.neo4j.repositories;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.*;

import com.ambergarden.samples.neo4j.entities.*;
import com.ambergarden.samples.neo4j.service.DataServiceI;
import com.ambergarden.samples.neo4j.util.DateUtil;
import com.ambergarden.samples.neo4j.util.JacksonUtil;
import com.google.common.collect.Lists;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath*:/spring/dal-test-context.xml"})
public class BookRepositoryTest {
    private static String TEST_PERSON_NAME_1 = "Person1";

    private static String TEST_BOOK_NAME_1 = "Book1";
    private static String TEST_BOOK_NAME_2 = "Book2";
    @Autowired
    private DataServiceI dataServiceI;
    @Autowired
    private PersonRepository personRepository;
  /* @Autowired
   private UserRepository userRepository;
   @Autowired
   private GroupRepository groupRepository;*/




/*   @Autowired
   private BookRepository bookRepository;*/

    public Map<String, String> handleCalDetails() {
//user_id=98710135094&order_id=900014510&channel=beike
        String userId = "user_id=98710135094&channel=beike&order_id=900014510";
        String channel = "beike";
        String order_id="900014510";
        Map<String, String> callDetailsParam = new HashMap<>();
        callDetailsParam.put("user_id", userId);
        callDetailsParam.put("channel", channel);
        callDetailsParam.put("order_id", order_id);
        String recods = dataServiceI.getRecordsByUserId(userId);
        System.out.println("records=" + recods);
        JSONObject jsonObject = new JSONObject(recods);
        JSONObject vcardRecordsObject = jsonObject.getJSONObject("vcardRecords");
        List<JSONObject> details = (List) vcardRecordsObject.getJSONArray("vcardDetal");
        Map<String, String> timesAndAvg = new HashMap();
        Map<String, String> cache = new HashMap<String, String>();
        //本机手机号
        String phone_num = vcardRecordsObject.getString("phone_num");
        try {
            for (JSONObject obj : details) {
                String avg = "0.0";

                //对方手机号
                String opposite_num = obj.getString("opposite_num");
                String flag = obj.getString("flag");//flag为主被叫标识，其中1为主叫，0为被叫
                String statTime = obj.getString("start_time");
                String endTime = obj.getString("end_time");
                if (StringUtils.isNoneBlank(statTime) || StringUtils.isNoneBlank(endTime)) {
                    Date startDate1 = DateUtil.formatToDayByYYYYMMDDMMHHSS(statTime);
                    Date endDate2 = DateUtil.formatToDayByYYYYMMDDMMHHSS(endTime);
                    avg = (endDate2.getTime() - startDate1.getTime()) / 1000 + "";
                }
                if (StringUtils.equals(flag, "0")) {
                    if (!timesAndAvg.containsKey(opposite_num)) {
                        timesAndAvg.put(opposite_num + "_" + flag, avg + "," + "1");
                    } else {
                        String[] value = timesAndAvg.get(opposite_num).split(",");
                        avg = Double.valueOf(value[0]) + Double.valueOf(avg) + "";
                        timesAndAvg.put(opposite_num, avg + "," + Integer.valueOf(value[1]) + 1);
                    }

                }

            }


            timesAndAvg.put("phone_num", phone_num);


        } catch (Exception ex) {

            ex.printStackTrace();
        }
        return cache;
    }

    @Test
    public void insertNode() {
        Person person = new Person();
        Map<String, String> map = handleCalDetails();
        String mphone = map.get("phone_num");

        for (Map.Entry m : map.entrySet()) {

            String sphone = (String) m.getKey();
            String[] avgandtime = ((String) m.getValue()).split(",");
            String times = avgandtime[1];
            String avg = String.format("%.2f", Double.valueOf(avgandtime[0]) / Integer.valueOf(times));

            //机主手机号
            if (StringUtils.isNoneBlank(mphone)) {
                person.setPid("p" + mphone);
            }
            //联系人手机号
            if (StringUtils.isNoneBlank(sphone)) {
                person.setSid("p" + sphone);
            }
            if (StringUtils.isNoneBlank(avg)) {
                person.setAvgTime(avg);
            }
            if (StringUtils.isNoneBlank(times)) {
                person.setCallTime(times);
            }
            String json = JacksonUtil.toJSon(dataServiceI.insertPerson(person));

        }

    }

    @Test
    public void test4() {
/*   Person  person =personRepository.findOne(3L);
   person.setPhone("13260257172");
   personRepository.save(person);*/
// System.out.print("-----------------"+JacksonUtil.toJSon( personRepository.findAll()));
//System.out.print("-----------------"+JacksonUtil.toJSon( personRepository.getThingPropertyByThingName("13260257172")));
// System.out.print("-----------------"+JacksonUtil.toJSon( personRepository.findAllByPhoneCallInfo("p18282898809")));

// personRepository.updateNode("p18892895038",50,50);

        // personRepository.createNode("p150");

        // personRepository.mergeNode("p18282898809","p150",20,20);

        // List<Person> personList = personRepository.findAllByPhoneCallInfo("p18282898809");
        String avgTime = personRepository.findAllByPhoneGetAvgTime("p133");
        String callTime = personRepository.findAllByPhoneGetCallTime("p18282898809");


        System.out.println(avgTime);
        System.out.println(callTime);


    }

    @Test
    public void test5() {
        Person person = new Person();
        person.setPid("p18282898809");
        person.setSid("p13260257172");
        person.setCallTime("0");
        person.setAvgTime("100.1");
//System.out.println(JacksonUtil.toJSon( dataServiceI.selectPerson(person)));
//System.out.println(JacksonUtil.toJSon( dataServiceI.updatePerson(person)));
// System.out.println(JacksonUtil.toJSon( dataServiceI.insertPerson(person)));

System.out.println(JacksonUtil.toJSon( dataServiceI.selectCallRecords(person, true, null, "")));


    }

    /******************************************************************************/

 /*  @Test
public void test1() {
   Group group = new Group();
   group.setName("admins");
   group.setCreateTime("创建时间：2017-10-10");

   User user = new User();
   user.setName("admin");
   user.setGroup(group);
   user.setAge("22");

   User user2 = new User();
   user2.setName("superAdmin");
   user2.setGroup(group);
   user2.setAge("40");
*//*   group.setUser(user);
   groupRepository.save(group);
   *//*
   userRepository.save(user);
   userRepository.save(user2);

   User user3 = userRepository.findByName("admin");
   System.out.println(user3.getName());
}

@Test
public void testUpdate(){
   //在一个组下面，再增加一个子属性。
   List<Group> group= groupRepository.findByName("admins");
   User user2 = new User();
   user2.setName("superAdmin3");
   user2.setGroup(group.get(0));
   user2.setAge("70");
   userRepository.save(user2);
}

   @Test
   public void testUpdate2(){
   *//*   //在一个子组下面，再增加一个子属性。
     User u = userRepository.findByName("赵六");
      User user2 = new User();
      user2.setName("superAdmin4");
      user2.setUser(u);
      user2.setAge("80");
      userRepository.save(user2);*//*
   }

   @Test
   public void testUpdate3(){
      List<Group> group= groupRepository.findByName("admins");
      Group group1 =group.get(0);
      group1.setCreateTime("创建时间：2018-11-10");
      groupRepository.save(group1);
   }




   @Test
   public void delete(){

 *//*   User u = userRepository.findByName("superAdmin4");
      userRepository.delete(u);*//*

      List<Group> group= groupRepository.findByName("admins");
        groupRepository.delete(group.get(0));
   }
   @Test
   public void  find(){
      List   list = new ArrayList();
      list.add(313113468l);
      HashSet  userList = (HashSet) userRepository.findAll(list);
      System.out.println( userList.size() );
      //System.out.println(JacksonUtil.toJSon( personRepository.));
   }

*/







/*
   @Test
   public void testCRUDBook() {
      Book book = new Book();
      book.setName(TEST_BOOK_NAME_1);  //"Book1";
      book = bookRepository.save(book);
      assertNotNull(book);
      assertNotNull(book.getId());
      assertEquals(TEST_BOOK_NAME_1, book.getName());

      Long originalId = book.getId();
      book.setName(TEST_BOOK_NAME_2);//"Book2";
      book = bookRepository.save(book);
      assertEquals(originalId, book.getId());
      assertEquals(TEST_BOOK_NAME_2, book.getName());

      bookRepository.delete(book);
      book = bookRepository.findOne(originalId);
      assertNull(book);
   }

   @Test
   public void testCRUDRelationships() {
      Person person1 = new Person();
      person1.setName(TEST_PERSON_NAME_1);
      person1 = personRepository.save(person1);

      // Test create with readers
      Set<Person> readers = new HashSet<Person>();
      readers.add(person1);

      Set<Book> books = new HashSet<Book>();
      Book book1 = new Book();
      books.add(book1);
      person1.setBooks(books);

      book1.setName(TEST_BOOK_NAME_1);
      book1.setReaders(readers);
      book1 = bookRepository.save(book1);
      assertNotNull(book1);
      assertNotNull(book1.getReaders());
      assertEquals(1, book1.getReaders().size());

      // Test add readers
      Book book2 = new Book();
      book2.setName(TEST_BOOK_NAME_2);
      book2 = bookRepository.save(book2);
      assertNotNull(book2);
      assertNull(book2.getReaders());

      readers = new HashSet<Person>();
      readers.add(person1);
      person1.getBooks().add(book2);
      book2.setReaders(readers);
      book2 = bookRepository.save(book2);
      assertNotNull(book2.getReaders());
      assertEquals(1, book2.getReaders().size());

      // Verify that the person entity is in sync
      person1 = personRepository.findOne(person1.getId());
      assertNotNull(person1.getBooks());
      assertEquals(2, person1.getBooks().size());

      // Test remove readers
      book2.setReaders(null);
      person1.getBooks().remove(book2);
      book2 = bookRepository.save(book2);

      person1 = personRepository.findOne(person1.getId());
      assertNotNull(person1.getBooks());
      assertEquals(1, person1.getBooks().size());

      // Test remove all readers
      book1.setReaders(null);
      person1.getBooks().remove(book1);
      book1 = bookRepository.save(book1);

      person1 = personRepository.findOne(person1.getId());
      assertNull(person1.getBooks());
   }

   @Test
   public void testRichRelationship() {
      Date timestamp = new Date();
      Person person1 = new Person();
      person1.setName(TEST_PERSON_NAME_1);
      person1 = personRepository.save(person1);

      // Test create with writers
      Book book1 = new Book();
      Set<WriterOf> writings = new HashSet<WriterOf>();
      person1.setWritings(writings);

      WriterOf writer = new WriterOf();
      writer.setBook(book1);
      writer.setWriter(person1);
      writer.setStartDate(timestamp);
      writer.setEndDate(timestamp);
      writings.add(writer);

      book1.setName(TEST_BOOK_NAME_1);
      book1.setWriters(writings);
      book1 = bookRepository.save(book1);
      assertNotNull(book1);
      assertNotNull(book1.getWriters());
      assertEquals(1, book1.getWriters().size());

      for (WriterOf writerOf : book1.getWriters()) {
         assertNotNull(writerOf.getStartDate());
         assertNotNull(writerOf.getEndDate());
      }
   }

   @Test
   public void testCRUDRelationshipEntities() {
      Person person1 = new Person();
      person1.setName(TEST_PERSON_NAME_1);
      person1 = personRepository.save(person1);

      // Test create with writers
      Book book1 = new Book();
      Set<WriterOf> writings = new HashSet<WriterOf>();
      person1.setWritings(writings);

      WriterOf writer = new WriterOf();
      writer.setBook(book1);
      writer.setWriter(person1);
      writings.add(writer);

      book1.setName(TEST_BOOK_NAME_1);
      book1.setWriters(writings);
      book1 = bookRepository.save(book1);
      assertNotNull(book1);
      assertNotNull(book1.getWriters());
      assertEquals(1, book1.getWriters().size());

      // Test add readers
      Book book2 = new Book();
      book2.setName(TEST_BOOK_NAME_2);
      book2 = bookRepository.save(book2);
      assertNotNull(book2);
      assertNull(book2.getReaders());

      writings = new HashSet<WriterOf>();
      writer = new WriterOf();
      writer.setBook(book2);
      writer.setWriter(person1);
      writings.add(writer);

      book2.setWriters(writings);
      book2 = bookRepository.save(book2);
      assertNotNull(book2.getWriters());
      assertEquals(1, book2.getWriters().size());

      // Verify that the person entity is in sync
      person1 = personRepository.findOne(person1.getId());
      assertNotNull(person1.getWritings());
      assertEquals(2, person1.getWritings().size());

      // Test remove writers
      person1.getWritings().removeAll(book1.getWriters());
      person1.getWritings().removeAll(book2.getWriters());
      book2.setWriters(null);
      book2 = bookRepository.save(book2);
      book1.setWriters(null);
      book1 = bookRepository.save(book1);

      person1 = personRepository.findOne(person1.getId());
      assertNull(person1.getWritings());
   }*/
}