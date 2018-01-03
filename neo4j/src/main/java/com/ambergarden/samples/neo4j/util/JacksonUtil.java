package com.ambergarden.samples.neo4j.util;

/**
 * Created with IntelliJ IDEA.
 * User: YKDZ5901703
 * Date: 2017/7/20
 * Time: 10:51
 * To change this template use File | Settings | File Templates.
 */

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;

/**
 * The class JacksonUtil
 *
 * json字符与对像转换
 *
 * @version: $Revision$ $Date$ $LastChangedBy$
 *
 */
public final class JacksonUtil {

    public static ObjectMapper objectMapper;

    /**
     * 使用泛型方法，把json字符串转换为相应的JavaBean对象。
     * (1)转换为普通JavaBean：readValue(json,Student.class)
     * (2)转换为List,如List<Student>,将第二个参数传递为Student
     * [].class.然后使用Arrays.asList();方法把得到的数组转换为特定类型的List
     *
     * @param jsonStr
     * @param valueType
     * @return
     */
    public static <T> T readValue(String jsonStr, Class<T> valueType) {
        if (objectMapper == null) {
            objectMapper = new ObjectMapper();
        }

        try {
           if(jsonStr.substring(0,1).equals("\"")){
                String  startNewJsonString = jsonStr.substring(1);
                String  endNewJsonString=startNewJsonString.substring(0,startNewJsonString.length()-1);
                return objectMapper.readValue(endNewJsonString.replace("\\",""), valueType);
            }else{
                return objectMapper.readValue(jsonStr, valueType);
            }
           // return objectMapper.readValue(jsonStr, valueType);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * json数组转List
     * @param jsonStr
     * @param valueTypeRef
     * @return
     */
    public static <T> T readValue(String jsonStr, TypeReference<T> valueTypeRef){
        if (objectMapper == null) {
            objectMapper = new ObjectMapper();
        }

        try {
            return objectMapper.readValue(jsonStr, valueTypeRef);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * 把JavaBean转换为json字符串 , 支持list
     *
     * @param object
     * @return
     */
    public static String toJSon(Object object) {
        if (objectMapper == null) {
            objectMapper = new ObjectMapper();
        }
        try {
            return objectMapper.writeValueAsString(object);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static  void main(String [] args)
    {
/*
       UserInfo userinfo = new UserInfo();
        userinfo.setAge(10);
      userinfo.setAttribution1("123");
      userinfo.setCardnumber("222");
        //转换成json串
        System.out.println(JacksonUtil.toJSon(userinfo));
        String  json ="{\"name\":null,\"gender\":0,\"age\":10,\"phone\":null,\"attribution1\":\"123\",\"attribution2\":null,\"cardnumber\":\"222\",\"phone2\":null}";
        userinfo = new UserInfo() ;
        //转换成java 对象
        userinfo= JacksonUtil.readValue(json,UserInfo.class);
        System.out.println(userinfo.getAge());
        System.out.println(userinfo.getGender());
*/


        Map<Long,String > map = new HashMap<Long ,String>();
        map.put(1L,"1");
        map.put(2l,"2");

        for(int i=0;i<8 ;i++){
            System.out.println(i+map.get(i));

        }

      /*  String    str ="妈妈,妈,母亲";
        String [] arr= str.split(",");
        String str2 ="[妈东北]";
        for(String s : arr){
            System.out.println(s);
            System.out.println(str2.indexOf(s));
        }*/

    }
}