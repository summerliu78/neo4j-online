package com.ambergarden.samples.neo4j.service.impl;


import com.ambergarden.samples.neo4j.GraphDBConfiguration;
import com.ambergarden.samples.neo4j.constant.SystemConstant;
import com.ambergarden.samples.neo4j.entities.Person;
import com.ambergarden.samples.neo4j.entities.ResultSpecValues;
import com.ambergarden.samples.neo4j.repositories.PersonRepository;
import com.ambergarden.samples.neo4j.service.DataServiceI;
import com.ambergarden.samples.neo4j.util.CommonUtils;
import com.ambergarden.samples.neo4j.util.HttpClients;
import com.ambergarden.samples.neo4j.util.JacksonUtil;
import com.ambergarden.samples.neo4j.util.PermUtils;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Date;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA.
 * User: YKDZ5901703
 * Date: 2017/10/25
 * Time: 14:47
 * To change this template use File | Settings | File Templates.
 */
@Service
public class DataServiceImpl implements DataServiceI {

    public static final String REGEX = "[^0-9]";
    private static final Pattern p = Pattern.compile(REGEX);
    private static Matcher m = null;
    private static final Logger log = Logger.getLogger(DataServiceImpl.class.getName());
    @Autowired
    private PersonRepository personRepository;
    private static Properties pps;

    static {
        try {
            pps = new Properties();
            InputStream inStr = GraphDBConfiguration.class.getClassLoader().getResourceAsStream("qcellcore.properties");
            pps.load(inStr);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //查找二度联系人
    @Override
    public List<Person> find2DNum(Person personParam) {
        try {
            if (personParam != null && StringUtils.isNoneBlank(personParam.getPid())) {
                List<Person> personList = personRepository.find2DNum(personParam.getPid());
                if (CollectionUtils.isNotEmpty(personList)) {
                    for (Person person : personList) {
                        m = p.matcher(person.getPid());
                        person.setPid(m.replaceAll("").trim());
                    }
                }
                return personList;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public String timesAndAvgEn(Map<String, String> result) {
        return HttpClients.urlPost(pps.getProperty("declineNumURL") + "/neo4j/timesAndAvg", result);
    }

    //查询机主号码信息
    @Override
    public List<Person> selectPerson(Person paramPerson) {
        try {
            if (paramPerson != null && StringUtils.isNoneBlank(paramPerson.getPid())) {
                List<Person> personList = personRepository.findAllByPhoneCallInfo(paramPerson.getPid());
                return personList;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    //更新属性。
    @Override
    public String updatePerson(Person person) {
        try {
            //从图数据库，取出已经存在的关系中的值
            String avgTime = personRepository.findSingleAvgTime(person.getSid(), person.getSid());
            String callTime = personRepository.findSigleCallTime(person.getPid(), person.getSid());
            //原有的值，重新赋值
            if (StringUtils.isNoneBlank(avgTime)) {
                person.setAvgTime((Double.parseDouble(avgTime) + Double.parseDouble(person.getAvgTime())) + "");
            } else {
                //已经4次，新打了1次   ，加起来=5次
                person.setAvgTime(person.getAvgTime());
            }
            if (StringUtils.isNoneBlank(callTime)) {
                person.setCallTime((Double.parseDouble(callTime) + Double.parseDouble(person.getCallTime())) + "");
            } else {
                person.setCallTime(person.getCallTime());
            }
            personRepository.updateNode(person.getSid(), person.getCallTime(), person.getAvgTime());
            return "SUCCESS";
        } catch (Exception e) {
            e.printStackTrace();
            return "FAILED";
        }
        //return "FAIL";
    }

    //插入新节点
    @Override
    public List<Person> insertPerson(Person person) {
        if (exitRelation(person.getPid(), person.getSid())) {
            updatePerson(person);
            return selectPerson(person);
        }
        boolean rootNodeflag = exitNode(person.getPid());
        boolean childNodeFlag = false;
        //判断是不是存在根节点
        if (!rootNodeflag) {
            personRepository.createNode(person.getPid());
            rootNodeflag = true;
        }
        if (StringUtils.isNoneBlank(person.getSid())) {
            childNodeFlag = exitNode(person.getSid());
            //先创建子节点
            if (!childNodeFlag) {
                personRepository.createNode(person.getSid());
                childNodeFlag = true;
            }
        }


        //再定义和机主的关系
        if (rootNodeflag && childNodeFlag)
            personRepository.mergeNode(person.getPid(), person.getSid(), person.getCallTime(), person.getAvgTime());


        return selectPerson(person);
    }

    @Override
    public String selectCallRecords(Person person, boolean neo4j_ok, Map<String, List<String>> phone_1D_map, String phone) {
        Map<String, Object> map = new HashMap<>();
        try {
            // 根据用户手机号码获取联系人信息

            Map<String, Object> contactsMap = getContactsInfoByMobile(person, neo4j_ok, phone_1D_map, phone);
            List<List<Person>> zeroPersonList = (List<List<Person>>) contactsMap.get("zeroPersonList");
            List<Person> maxZeroPersonList = (List<Person>) contactsMap.get("maxZeroPersonList");
            List<Person> onePersonList = (List<Person>) contactsMap.get("onePersonList");
            List<Person> twoPersonList = (List<Person>) contactsMap.get("twoPersonList");
            // 一度特征值
            get1DContactsInfo(person, map, onePersonList);
            // 二度特征值
            get2DContactsInfo(person, map, twoPersonList);
            // 团伙特征值
            // check permission to calculate gang info. added by yaozy on 2018/01/02
            if (PermUtils.GetCalcCallRecordPerm().equalsIgnoreCase("ON")) {
                get0DContactsInfo(person, map, maxZeroPersonList, zeroPersonList, neo4j_ok);
            } else {
                ResultSpecValues resultSpecValues = new ResultSpecValues(
                    SystemConstant.NO_EXEC_RESULT,
                    SystemConstant.NO_EXEC_RESULT_DOUBLE_TYPE,
                    SystemConstant.CODE_SUCCESS,
                    SystemConstant.MSG_SUCCESS);
                map.putAll(CommonUtils.getSpecifiedODContactsInfo(resultSpecValues));
            }
            //本机特征值
            getContactsInfo(person, map);
            map.put("scdata_code", 2);
            map.put("scdata_msg", "success");
            return JacksonUtil.toJSon(map);
        } catch (Exception e) {
            e.printStackTrace();
            getExceptionResult(map);
            return JacksonUtil.toJSon(map);
        }
    }

    /**
     * 根据用户手机号码获取联系人信息
     *
     * @param person
     * @param neo4j_ok
     * @param phone_1D_map
     * @param phone
     * @return
     * @author olivia.wei
     */
    private Map<String, Object> getContactsInfoByMobile(Person person, boolean neo4j_ok, Map<String, List<String>> phone_1D_map, String phone) {

        Map<String, Object> contactsMap = Maps.newHashMap();
        List<List<Person>> zeroPersonList = Lists.newArrayList();
        List<Person> maxZeroPersonList = Lists.newArrayList();
        List<Person> onePersonList = Lists.newArrayList();
        List<Person> twoPersonList = Lists.newArrayList();

        if (neo4j_ok) {
            // check permission to calculate gang info. added by yaozy on 2018/01/02
            if (PermUtils.GetCalcCallRecordPerm().equalsIgnoreCase("ON")) {
                // 查询当前联系人团伙
                zeroPersonList = getGangByPhone(person);
                // 获取团伙最大路径里成员信息
                maxZeroPersonList = getMaxPersons(zeroPersonList);
                // 团伙不计算本人
                removeCurrentPerson(person, maxZeroPersonList);
            }
            // 根据标志获取联系人信息
            onePersonList = queryContactsByPersonAndFlag(person, SystemConstant.FLAG_OF_ONE_DEGREE);
            twoPersonList = queryContactsByPersonAndFlag(person, SystemConstant.FLAG_OF_TWO_DEGREE);
        } else if (phone_1D_map != null) {
            if (phone_1D_map.containsKey(phone)) {
                for (String one : phone_1D_map.get(phone)) {
                    Person p = new Person();
                    p.setPid(one);
                    onePersonList.add(p);
                }
            }
            HashSet<String> two_tmp = new HashSet<String>();
            for (Person one : onePersonList) {
                if (phone_1D_map.containsKey(one.getPid())) {
                    for (String two : phone_1D_map.get(one.getPid())) {
                        two_tmp.add(two);
                    }
                }
            }
            for (String two : two_tmp) {
                Person p = new Person();
                p.setPid(two);
                twoPersonList.add(p);
            }
        }
        contactsMap.put("zeroPersonList", zeroPersonList);
        contactsMap.put("onePersonList", onePersonList);
        contactsMap.put("twoPersonList", twoPersonList);
        contactsMap.put("maxZeroPersonList", maxZeroPersonList);
        return contactsMap;
    }

    /**
     * 获取团伙最大路径里成员信息
     *
     * @param zeroPersonList
     * @return
     * @author olivia.wei
     */
    public List<Person> getMaxPersons(List<List<Person>> zeroPersonList) {
        List<Person> maxZeroPersonList = null;
        int maxSize = 0;
        for (List<Person> personList : zeroPersonList) {
            List<Person> newPersonList = new ArrayList<Person>(new HashSet<Person>(personList));
            // 查找出所有关系中成员最多的关系
            if (newPersonList.size() > maxSize) {
                maxSize = newPersonList.size();
                maxZeroPersonList = newPersonList;
            }
        }
        // 如果人数大于等于4，算作团伙
        if (CollectionUtils.isNotEmpty(maxZeroPersonList) && maxZeroPersonList.size() >= 4) {
            return maxZeroPersonList;
        } else {
            return Lists.newArrayList();
        }
    }

    /**
     * 查询当前联系人团伙
     *
     * @param person
     * @return
     * @author olivia.wei
     */
    private List<List<Person>> getGangByPhone(Person person) {
        return selectGang(person);
    }

    /**
     * 近90天同一个手机号码之前被拒绝授信次数
     *
     * @param person
     * @param channelFlag
     * @return
     * @author olivia.wei
     */
    private int mobileDeclineNum(Person person, String channelFlag) {
        String result = null;
        Long startTime = System.currentTimeMillis();
        try {
            Map paramMap = Maps.newHashMap();
            paramMap.put("mobile", person.getPid());
            paramMap.put("channel", person.getChannel());
            paramMap.put("applyTime", person.getApplyTime());
            paramMap.put("channelFlag", channelFlag);
            paramMap.put("userId", person.getUser_id());
            log.debug("-threadId-" + Thread.currentThread().getId() + "-mobile-" + person.getPid() + "-channel-" + person.getChannel() + "-mobileDeclineNum of-" + null + "-params-" + JacksonUtil.toJSon(paramMap));
            result = HttpClients.urlPost(pps.getProperty("declineNumURL") + "/neo4j/mobileDeclineNum", paramMap);
            log.debug("-threadId-" + Thread.currentThread().getId() + "-mobile-" + person.getPid() + "-channel-" + person.getChannel() + "-mobileDeclineNum of-" + null + "-result-" + result);
        } catch (Exception e) {
            log.error("-threadId-" + Thread.currentThread().getId() + "-mobile-" + person.getPid() + "-channel-" + person.getChannel() + "-mobileDeclineNum of-" + null + "-error-", e);
            return SystemConstant.EXCEPTION_RESULT;
        }
        Long endTime = System.currentTimeMillis();
        log.debug("mobileDeclineNum " + (endTime - startTime)+"ms user_id="+person.getUser_id()+"&channel="+person.getChannel());
        return StringUtils.isNotEmpty(result) ? Integer.valueOf(result) : SystemConstant.EMPTY_RESULT;
    }

    /**
     * 团伙计算，删除当前用户
     *
     * @param person
     * @param maxZeroPersonList
     * @author olivia.wei
     */
    private void removeCurrentPerson(Person person, List<Person> maxZeroPersonList) {
        if (CollectionUtils.isNotEmpty(maxZeroPersonList)) {
            for (Person p : maxZeroPersonList) {
                if (p != null && p.getPid().equals(person.getPid())) {
                    maxZeroPersonList.remove(p);
                    return;
                }
            }
        }
    }

    /**
     * 一度特征值
     *
     * @param person
     * @param map
     * @param onePersonList
     * @author olivia.wei
     */
    private void get1DContactsInfo(Person person, Map<String, Object> map, List<Person> onePersonList) {
        // 一度联系人个数 (wu)
        map.put("1d_contact_num", contactNum(onePersonList, SystemConstant.FLAG_OF_ONE_DEGREE));
        // 一度联系人过去一个月在本平台申请的人数 所有渠道
        map.put("1d_apply_num", applyNum(person, onePersonList, SystemConstant.FLAG_OF_ONE_DEGREE, SystemConstant.ALL_CHANNEL_FLAG));
        // 一度联系人过去一个月在本平台申请的人数 当前渠道
        map.put("1d_apply_num_current", applyNum(person, onePersonList, SystemConstant.FLAG_OF_ONE_DEGREE, SystemConstant.CURRENT_CHANNEL_FLAG));
        // 一度联系人申请通过人的逾期人数
        map.put("1d_overdue_num", overDueNum(person, onePersonList, SystemConstant.FLAG_OF_ONE_DEGREE, SystemConstant.ALL_CHANNEL_FLAG));
        // 一度联系人中申请授信被拒绝人数 所有渠道
        map.put("1d_decline_num", declineNum(person, onePersonList, SystemConstant.FLAG_OF_ONE_DEGREE, SystemConstant.ALL_CHANNEL_FLAG));
        // 一度联系人中申请授信被拒绝人数 当前渠道
        map.put("1d_decline_num_current", declineNum(person, onePersonList, SystemConstant.FLAG_OF_ONE_DEGREE, SystemConstant.CURRENT_CHANNEL_FLAG));
        // 一度联系人中借款但未还款人数 所有渠道
        map.put("1d_unpaid_num", loanButUnpaidNum(person, onePersonList, SystemConstant.FLAG_OF_ONE_DEGREE, SystemConstant.ALL_CHANNEL_FLAG));
        // 一度联系人中借款但未还款人数 当前渠道
        map.put("1d_unpaid_num_current", loanButUnpaidNum(person, onePersonList, SystemConstant.FLAG_OF_ONE_DEGREE, SystemConstant.CURRENT_CHANNEL_FLAG));
        // 一度联系人中逾期金额
        map.put("1d_overdue_amount", overdueAmount(person, onePersonList, SystemConstant.FLAG_OF_ONE_DEGREE, SystemConstant.ALL_CHANNEL_FLAG));
        //一度关系有过续贷的人数
        map.put("1d_renew_loan_num", repayAmount(person, onePersonList, SystemConstant.FLAG_OF_ONE_DEGREE));
        // 一度联系人中命中黑名单人数
        map.put("1d_hit_black_num", hitBlackNum(person, onePersonList, SystemConstant.FLAG_OF_ONE_DEGREE));
        // 一度联系人近六个月逾期人数占总借款人数占比
        map.put("1d_overdue_num_ratio", overdueNumRatio(person, onePersonList, SystemConstant.FLAG_OF_ONE_DEGREE, SystemConstant.CURRENT_CHANNEL_FLAG));
       //近六个月当前申请借款/授信时间与一度联系人申请借款时间间隔天数均值
        map.put("1d_diff_loantime_avg_f",getApplyAvg(person,onePersonList,SystemConstant.FLAG_OF_ONE_DEGREE));
       //近六个月当前申请借款/授信时间与一度联系人申请借款时间间隔天数均值一一人借款多次按多次计算
        map.put("1d_diff_loantime_avg",getAllApplyAvg(person,onePersonList,SystemConstant.FLAG_OF_ONE_DEGREE));
          //一度联系人近六个月当前申请借款/授信时间一度联系人逾期金额总和占一度联系人总借款金额比值
        map.put("1d_overdue_amount_ratio",getDueAmountAndLoanAmountRate(person,onePersonList,SystemConstant.FLAG_OF_ONE_DEGREE));
        //一度联系人近六个月当前申请借款/授信时间一度联系人未逾期金额总和占一度联系人总借款金额比值
        map.put("1d_normal_loan_amount_ratio",getNotDueAmountAndLoanAmountRate(person,onePersonList,SystemConstant.FLAG_OF_ONE_DEGREE));
        //一度联系人近六个月当前申请借款/授信时间平均借款金额
        map.put("1d_loan_amount_avg",getLoanAmountAvg(person,onePersonList,SystemConstant.FLAG_OF_ONE_DEGREE));
    }

    /**
     * 一度联系人近六个月平均借款金额
     *
     * @param person
     * @param degreeFlag
     * @param personList
     * @author xp
     */
    private  Double  getLoanAmountAvg(Person person,List<Person> personList,String degreeFlag){
        String result = null;
        try{

            Map paramMap = Maps.newHashMap();
            if (CollectionUtils.isNotEmpty(personList)) {
                StringBuffer str = new StringBuffer();
                for(Person p : personList){
                    str.append(",").append(p.getPid());
                }
                paramMap.put("personIds", str.toString().substring(1));
                paramMap.put("channel", person.getChannel());
                paramMap.put("phone_number",person.getPid());
                paramMap.put("credit_apply_time",person.getApplyTime());
                paramMap.put("loan_apply_time",person.getLoanApplyTime());
                log.info("credit_apply_time_______________________:"+person.getApplyTime());
                log.info("loan_apply_time_______________________:"+person.getLoanApplyTime());
               // System.out.println("credit_apply_time:"+person.getApplyTime());
               // System.out.println("loan_apply_time:"+person.getLoanApplyTime());
                String type = person.getType();
                if("".equals(type) || "loan".equals(type)){
                    result = HttpClients.urlPost(pps.getProperty("declineNumURL") + "/neo4j/getLoanLoanAmountAvg", paramMap);
                    System.out.println("result:"+result);
                }else {
                    result = HttpClients.urlPost(pps.getProperty("declineNumURL") + "/neo4j/getCreditLoanAmountAvg", paramMap);
                }
//                if(result == null){
//                    return SystemConstant.NONE_RESULT.doubleValue();
//                }
            }
            return StringUtils.isNotEmpty(result) ? new BigDecimal(result).setScale(3,RoundingMode.HALF_UP).doubleValue() : SystemConstant.EMPTY_RESULT;
        }catch (Exception e){
            log.error("-threadId-" + Thread.currentThread().getId() + "-mobile-" + person.getPid() + "-channel-" + person.getChannel() + "-overdueAmountRatio of-" + degreeFlag + "-error-", e);
            return SystemConstant.EXCEPTION_RESULT.doubleValue();
        }
    }



    /**
     * 一度联系人近六个月(申请授信时间)一度联系人逾期金额总和占一度联系人总借款金额比值
     *
     * @param person
     * @param degreeFlag
     * @param personList
     * @author xp
     */
    private Double getDueAmountAndLoanAmountRate(Person person,List<Person> personList,String degreeFlag){
        String result = null;
        try{

            Map paramMap = Maps.newHashMap();
            if (CollectionUtils.isNotEmpty(personList)) {
                StringBuffer str = new StringBuffer();
                for(Person p : personList){
                    str.append(",").append(p.getPid());
                }
                paramMap.put("personIds", str.toString().substring(1));
                paramMap.put("channel", person.getChannel());
                paramMap.put("phone_number",person.getPid());
                paramMap.put("credit_apply_time",person.getApplyTime());
                paramMap.put("loan_apply_time",person.getLoanApplyTime());
                String type = person.getType();
                if("".equals(type) || "loan".equals(type)){
                    result = HttpClients.urlPost(pps.getProperty("declineNumURL") + "/neo4j/getLoanDueAmountAndLoanAmountRate", paramMap);
                }else {
                    result = HttpClients.urlPost(pps.getProperty("declineNumURL") + "/neo4j/getCreditDueAmountAndLoanAmountRate", paramMap);
                }
//                if(result == null){
//                    return SystemConstant.NONE_RESULT.doubleValue();
//                }
            }
            return StringUtils.isNotEmpty(result) ? new BigDecimal(result).setScale(3,RoundingMode.HALF_UP).doubleValue() : SystemConstant.EMPTY_RESULT;
        }catch (Exception e){
            log.error("-threadId-" + Thread.currentThread().getId() + "-mobile-" + person.getPid() + "-channel-" + person.getChannel() + "-overdueAmountRatio of-" + degreeFlag + "-error-", e);
            return SystemConstant.EXCEPTION_RESULT.doubleValue();
        }
    }


    /**
     * 一度联系人近六个月(申请授信时间)一度联系人未逾期金额总和占一度联系人总借款金额比值
     *
     * @param person
     * @param degreeFlag
     * @param personList
     * @author xp
     */
    private Double getNotDueAmountAndLoanAmountRate(Person person,List<Person> personList,String degreeFlag){
        try {
            DecimalFormat  df = new DecimalFormat("0.000");

            Double result = getDueAmountAndLoanAmountRate(person, personList, degreeFlag);
            if (-2.0 == result) {
                return SystemConstant.EXCEPTION_RESULT.doubleValue();
            }else if(result ==null){
                return SystemConstant.NONE_RESULT.doubleValue();
            } else {
                return 1-result;
            }

        }catch (Exception e){
            log.error("-threadId-" + Thread.currentThread().getId() + "-mobile-" + person.getPid() + "-channel-" + person.getChannel() + "-overdueAmountRatio of-" + degreeFlag + "-error-", e);
            return SystemConstant.EXCEPTION_RESULT.doubleValue();
        }

    }





    /**
     * 近六个月当前申请借款/授信时间与一度联系人申请借款时间间隔天数均值
     *
     * @param person
     * @param degreeFlag
     * @param personList
     * @author xp
     */
    private Double getApplyAvg(Person person,List<Person> personList,String degreeFlag){
        String result = null;
        try{

            Map paramMap = Maps.newHashMap();
            if (CollectionUtils.isNotEmpty(personList)) {
                StringBuffer str = new StringBuffer();
                for(Person p : personList){
                    str.append(",").append(p.getPid());
                }
                paramMap.put("personIds", str.toString().substring(1));
                paramMap.put("channel", person.getChannel());
                paramMap.put("phone_number",person.getPid());
                paramMap.put("credit_apply_time",person.getApplyTime());
                paramMap.put("loan_apply_time",person.getLoanApplyTime());
                String type = person.getType();
                if("".equals(type) || "loan".equals(type)){
                    result = HttpClients.urlPost(pps.getProperty("declineNumURL") + "/neo4j/getApplyLoanAvg", paramMap);
                }else {
                    result = HttpClients.urlPost(pps.getProperty("declineNumURL") + "/neo4j/getApplyCreditAvg", paramMap);
                }
//                if(result == null){
//                    return SystemConstant.NONE_RESULT.doubleValue();
//                }
            }
            return StringUtils.isNotEmpty(result) ? new BigDecimal(result).setScale(3,RoundingMode.HALF_UP).doubleValue() : SystemConstant.EMPTY_RESULT;
        }catch (Exception e){
            log.error("-threadId-" + Thread.currentThread().getId() + "-mobile-" + person.getPid() + "-channel-" + person.getChannel() + "-overdueAmountRatio of-" + degreeFlag + "-error-", e);
            return SystemConstant.EXCEPTION_RESULT.doubleValue();
        }



    }


    /**
     * 近六个月当前申请借款/授信时间与一度联系人申请借款时间间隔天数均值一一人借款多次按多次计算
     *
     * @param person
     * @param degreeFlag
     * @param personList
     * @author xp
     */
    private Double getAllApplyAvg(Person person,List<Person> personList,String degreeFlag){
        String result = null;
        try{
            Map paramMap = Maps.newHashMap();
            if (CollectionUtils.isNotEmpty(personList)) {
                StringBuffer str = new StringBuffer();
                for(Person p : personList){
                    str.append(",").append(p.getPid());
                }
                paramMap.put("personIds", str.toString().substring(1));
                paramMap.put("channel", person.getChannel());
                paramMap.put("phone_number",person.getPid());
                paramMap.put("credit_apply_time",person.getApplyTime());
                paramMap.put("loan_apply_time",person.getLoanApplyTime());
                String type = person.getType();
                if("".equals(type) || "loan".equals(type)){
                    result = HttpClients.urlPost(pps.getProperty("declineNumURL") + "/neo4j/getAllApplyLoanAvg", paramMap);
                }else {
                    result = HttpClients.urlPost(pps.getProperty("declineNumURL") + "/neo4j/getAllApplyCreditAvg", paramMap);
                }
//                if(result == null){
//                    return SystemConstant.NONE_RESULT.doubleValue();
//                }
            }
            return StringUtils.isNotEmpty(result) ? new BigDecimal(result).setScale(3,RoundingMode.HALF_UP).doubleValue() : SystemConstant.EMPTY_RESULT;
        }catch (Exception e){
            log.error("-threadId-" + Thread.currentThread().getId() + "-mobile-" + person.getPid() + "-channel-" + person.getChannel() + "-overdueAmountRatio of-" + degreeFlag + "-error-", e);
            return SystemConstant.EXCEPTION_RESULT.doubleValue();
        }
    }

    /**
     * 联系人近六个月逾期人数占总借款人数占比
     *
     * @param person
     * @param degreeFlag
     * @param channelFlag
     * @author olivia.wei
     */
    private Object overdueNumRatio(Person person, List<Person> personList, String degreeFlag, String channelFlag) {
        String result = null;
        try{
            Map paramMap = Maps.newHashMap();
            if (CollectionUtils.isNotEmpty(personList) && (StringUtils.isNotEmpty(person.getApplyTime()) || StringUtils.isNotEmpty(person.getApplyTime()))) {
                StringBuffer str = new StringBuffer();
                for(Person p : personList){
                    str.append(",").append(p.getPid());
                }
                paramMap.put("personIds", str.toString().substring(1));
                paramMap.put("channel", person.getChannel());
                paramMap.put("applyTime", person.getApplyTime());
                paramMap.put("loanApplyTime", person.getLoanApplyTime());
                paramMap.put("channelFlag", channelFlag);
                paramMap.put("type", person.getType());
                log.debug("-threadId-" + Thread.currentThread().getId() + "-mobile-" + person.getPid() + "-channel-" + person.getChannel() + "-overdueNumRatio of-" + degreeFlag + "-params-" + JacksonUtil.toJSon(paramMap));
                result = HttpClients.urlPost(pps.getProperty("declineNumURL") + "/neo4j/overdueNumRatio", paramMap);
                log.debug("-threadId-" + Thread.currentThread().getId() + "-mobile-" + person.getPid() + "-channel-" + person.getChannel() + "-overdueNumRatio of-" + degreeFlag + "-result-" + result);
            }
            return StringUtils.isNotEmpty(result) ? Double.valueOf(result) : SystemConstant.EMPTY_RESULT_DOUBLE_TYPE;
        } catch (Exception e){
            log.error("-threadId-" + Thread.currentThread().getId() + "-mobile-" + person.getPid() + "-channel-" + person.getChannel() + "-overdueNumRatio of-" + degreeFlag + "-error-", e);
            return SystemConstant.EXCEPTION_RESULT_DOUBLE_TYPE;
        }

    }

    /**
     * 二度特征值
     *
     * @param person
     * @param map
     * @param twoPersonList
     * @author olivia.wei
     */
    private void get2DContactsInfo(Person person, Map<String, Object> map, List<Person> twoPersonList) {
        // 二度联系人个数
        map.put("2d_contact_num", contactNum(twoPersonList, SystemConstant.FLAG_OF_TWO_DEGREE));
        // 二度联系人过去一个月在本平台申请的人数 所有渠道
        map.put("2d_apply_num", applyNum(person, twoPersonList, SystemConstant.FLAG_OF_TWO_DEGREE, SystemConstant.ALL_CHANNEL_FLAG));
        // 一度联系人过去一个月在本平台申请的人数 当前渠道
        map.put("2d_apply_num_current", applyNum(person, twoPersonList, SystemConstant.FLAG_OF_TWO_DEGREE, SystemConstant.CURRENT_CHANNEL_FLAG));
        // 二度联系人申请通过的人中有多少逾期的人数
        map.put("2d_overdue_num", overDueNum(person, twoPersonList, SystemConstant.FLAG_OF_TWO_DEGREE, SystemConstant.ALL_CHANNEL_FLAG));
        // 二度联系人中申请授信被拒绝人数 所有渠道
        map.put("2d_decline_num", declineNum(person, twoPersonList, SystemConstant.FLAG_OF_TWO_DEGREE, SystemConstant.ALL_CHANNEL_FLAG));
        // 二度联系人中申请授信被拒绝人数 当前渠道
        map.put("2d_decline_num_current", declineNum(person, twoPersonList, SystemConstant.FLAG_OF_TWO_DEGREE, SystemConstant.CURRENT_CHANNEL_FLAG));
        // 二度联系人中借款但未还款人数 所有渠道
        map.put("2d_unpaid_num", loanButUnpaidNum(person, twoPersonList, SystemConstant.FLAG_OF_TWO_DEGREE, SystemConstant.ALL_CHANNEL_FLAG));
        // 二度联系人中借款但未还款人数 当前渠道
        map.put("2d_unpaid_num_current", loanButUnpaidNum(person, twoPersonList, SystemConstant.FLAG_OF_TWO_DEGREE, SystemConstant.CURRENT_CHANNEL_FLAG));
        // 二度联系人中逾期金额
        map.put("2d_overdue_amount", overdueAmount(person, twoPersonList, SystemConstant.FLAG_OF_TWO_DEGREE, SystemConstant.ALL_CHANNEL_FLAG));
        //二度关系有过续贷的人数
        map.put("2d_renew_loan_num", repayAmount(person, twoPersonList, SystemConstant.FLAG_OF_ONE_DEGREE));
        // 二度联系人中命中黑名单人数
        map.put("2d_hit_black_num", hitBlackNum(person, twoPersonList, SystemConstant.FLAG_OF_TWO_DEGREE));
    }

    /**
     * 团伙特征值
     *
     * @param person
     * @param map
     * @param maxZeroPersonList
     * @param zeroPersonList
     * @author olivia.wei
     */
    private void get0DContactsInfo(Person person, Map<String, Object> map, List<Person> maxZeroPersonList, List<List<Person>> zeroPersonList, boolean neo4j_ok) {
        // 团伙人数
        map.put("0d_contact_num", contactNum(maxZeroPersonList, SystemConstant.FLAG_OF_ZERO_DEGREE));
        // 团伙联系人过去一个月在本平台申请的人数
        map.put("0d_apply_num_current", applyNum(person, maxZeroPersonList, SystemConstant.FLAG_OF_ZERO_DEGREE, SystemConstant.CURRENT_CHANNEL_FLAG));
        // 团伙联系人中逾期人数
        map.put("0d_overdue_num_current", overDueNum(person, maxZeroPersonList, SystemConstant.FLAG_OF_ZERO_DEGREE, SystemConstant.CURRENT_CHANNEL_FLAG));
        // 团伙联系人中申请授信被拒绝人数
        map.put("0d_decline_num_current", declineNum(person, maxZeroPersonList, SystemConstant.FLAG_OF_ZERO_DEGREE, SystemConstant.CURRENT_CHANNEL_FLAG));
        // 团伙联系人中借款但未还款人数
        map.put("0d_unpaid_num_current", loanButUnpaidNum(person, maxZeroPersonList, SystemConstant.FLAG_OF_ZERO_DEGREE, SystemConstant.CURRENT_CHANNEL_FLAG));
        // 团伙中电话号码对应的城市数量
        map.put("0d_mobile_address_num", mobileAddressNum(person, maxZeroPersonList, SystemConstant.FLAG_OF_ZERO_DEGREE));
        //团伙亲密度得分
        map.put("0d_intimacy_score", intimacyScore(person, maxZeroPersonList, zeroPersonList, neo4j_ok));
    }


    /**
     * 团伙亲密度得分
     *
     * @param person
     * @param personList
     * @param zeroPersonList
     * @return
     */
    private Double intimacyScore(Person person, List<Person> personList, List<List<Person>> zeroPersonList, boolean neo4j_ok) {
        try {
            Long startTime = System.currentTimeMillis();
            if (!neo4j_ok) {
                return SystemConstant.EMPTY_RESULT.doubleValue();
            }
            Map<String, Integer> map = this.nodeInOutAndAllCount(person, zeroPersonList);
            DecimalFormat decimalFormat = new DecimalFormat("0.000%");
            Double mainnum = Double.valueOf(map.get("ALL"));
            int sumnum = 0;
            if (CollectionUtils.isNotEmpty(personList)) {
                for (Person p : personList) {
                    sumnum += this.nodeInOutAndAllCount(p, zeroPersonList).get("ALL").intValue();
                }
            } else {
                sumnum = 0;
            }
            if (sumnum != 0) {
                Long endTime = System.currentTimeMillis();
                log.debug("intimacyScore " + (endTime - startTime)+"ms user_id="+person.getUser_id()+"&channel="+person.getChannel());
                return new BigDecimal(mainnum / sumnum).setScale(3,RoundingMode.HALF_UP).doubleValue();
            } else {
                Long endTime = System.currentTimeMillis();
                log.debug("intimacyScore " + (endTime - startTime)+"ms user_id="+person.getUser_id()+"&channel="+person.getChannel());
                return SystemConstant.EMPTY_RESULT.doubleValue();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return SystemConstant.EXCEPTION_RESULT.doubleValue();
        }

    }

    /**
     * 团伙中电话号码对应的城市数量
     *
     * @param person
     * @param personList
     * @param degreeFlag
     * @return
     */
    private int mobileAddressNum(Person person, List<Person> personList, String degreeFlag) {
        String result = null;
        Long startTime = System.currentTimeMillis();
        try {
            if (CollectionUtils.isNotEmpty(personList)) {
                Map paramMap = Maps.newHashMap();
                StringBuffer str = new StringBuffer();
                for (Person p : personList) {
                    str.append(",").append(p.getPid());
                }
                paramMap.put("personIds", str.toString().substring(1));
                paramMap.put("channel", person.getChannel());
                paramMap.put("applyTime", person.getApplyTime());
                paramMap.put("userId", person.getUser_id());
                log.debug("-threadId-" + Thread.currentThread().getId() + "-mobile-" + person.getPid() + "-channel-" + person.getChannel() + "-mobileAddressNum of-" + degreeFlag + "-params-" + JacksonUtil.toJSon(paramMap));
                result = HttpClients.urlPost(pps.getProperty("declineNumURL") + "/neo4j/mobileAddressNum", paramMap);
                log.debug("-threadId-" + Thread.currentThread().getId() + "-mobile-" + person.getPid() + "-channel-" + person.getChannel() + "-mobileAddressNum of-" + degreeFlag + "-result-" + result);
            }
        } catch (Exception e) {
            log.error("-threadId-" + Thread.currentThread().getId() + "-mobile-" + person.getPid() + "-channel-" + person.getChannel() + "-mobileAddressNum of-" + degreeFlag + "-error-", e);
            return SystemConstant.EXCEPTION_RESULT;
        }
        Long endTime = System.currentTimeMillis();
        log.debug("mobileAddressNum " + (endTime - startTime)+"ms user_id="+person.getUser_id()+"&channel="+person.getChannel());
        return StringUtils.isNotEmpty(result) ? Integer.valueOf(result) : SystemConstant.EMPTY_RESULT;
    }

    /**
     * 本机特征值
     *
     * @param person
     * @param map
     * @author xp
     */
    private void getContactsInfo(Person person, Map<String, Object> map) {
        //紧急联系人处于还款中
        map.put("urgent_unpaid_num", urgentAmount(person));
        // 近90天同一个手机号码之前被拒绝授信次数
        map.put("mobile_decline_num", mobileDeclineNum(person, SystemConstant.ALL_CHANNEL_FLAG));
        // 用户填写的紧急联系人号码对应多少个申请人 近180天
        map.put("urgent_corresponding_applicant_num", urgentCorrespondingApplicantNum(person, SystemConstant.ALL_CHANNEL_FLAG));
        // 申请手机号码对应联系人姓名数量 近180天
        map.put("mobile_ corresponding_multi_name_num", mobileCorrespondingMultiNameNum(person, SystemConstant.ALL_CHANNEL_FLAG));

    }

    /**
     * 申请手机号码对应联系人姓名数量
     *
     * @param person
     * @param channelFlag
     * @return
     * @author olivia.wei
     */
    private Object mobileCorrespondingMultiNameNum(Person person, String channelFlag) {
        String result = null;
        Long startTime = System.currentTimeMillis();
        try {
            Map paramMap = Maps.newHashMap();
            paramMap.put("mobile", person.getPid());
            paramMap.put("channel", person.getChannel());
            paramMap.put("channelFlag", channelFlag);
            paramMap.put("applyTime", person.getApplyTime());
            paramMap.put("userId", person.getUser_id());
            log.debug("-threadId-" + Thread.currentThread().getId() + "-mobile-" + person.getPid() + "-channel-" + person.getChannel() + "-mobileCorrespondingMultiNameNum of-" + null + "-params-" + JacksonUtil.toJSon(paramMap));
            result = HttpClients.urlPost(pps.getProperty("declineNumURL") + "/neo4j/mobileCorrespondingMultiNameNum", paramMap);
            log.debug("-threadId-" + Thread.currentThread().getId() + "-mobile-" + person.getPid() + "-channel-" + person.getChannel() + "-mobileCorrespondingMultiNameNum of-" + null + "-result-" + result);
        } catch (Exception e) {
            log.error("-threadId-" + Thread.currentThread().getId() + "-mobile-" + person.getPid() + "-channel-" + person.getChannel() + "-mobileCorrespondingMultiNameNum of-" + null + "-error-", e);
            return SystemConstant.EXCEPTION_RESULT;
        }
        Long endTime = System.currentTimeMillis();
        log.debug("mobileCorrespondingMultiNameNum " + (endTime - startTime)+"ms user_id="+person.getUser_id()+"&channel="+person.getChannel());
        return StringUtils.isNotEmpty(result) ? Integer.valueOf(result) : SystemConstant.EMPTY_RESULT;
    }

    /**
     * 用户填写的紧急联系人号码对应多少个申请人
     *
     * @param person
     * @param channelFlag
     * @return
     * @author olivia.wei
     */
    private Object urgentCorrespondingApplicantNum(Person person, String channelFlag) {
        String result = null;
        Long startTime = System.currentTimeMillis();
        try {
            Map paramMap = Maps.newHashMap();
            paramMap.put("mobile", person.getPid());
            paramMap.put("userId", person.getUser_id());
            paramMap.put("channel", person.getChannel());
            paramMap.put("channelFlag", channelFlag);
            paramMap.put("applyTime", person.getApplyTime());
            log.debug("-threadId-" + Thread.currentThread().getId() + "-mobile-" + person.getPid() + "-channel-" + person.getChannel() + "-urgentCorrespondingApplicantNum of-" + null + "-params-" + JacksonUtil.toJSon(paramMap));
            result = HttpClients.urlPost(pps.getProperty("declineNumURL") + "/neo4j/urgentCorrespondingApplicantNum", paramMap);
            log.debug("-threadId-" + Thread.currentThread().getId() + "-mobile-" + person.getPid() + "-channel-" + person.getChannel() + "-urgentCorrespondingApplicantNum of-" + null + "-result-" + result);
        } catch (Exception e) {
            log.error("-threadId-" + Thread.currentThread().getId() + "-mobile-" + person.getPid() + "-channel-" + person.getChannel() + "-urgentCorrespondingApplicantNum of-" + null + "-error-", e);
            return SystemConstant.EXCEPTION_RESULT;
        }
        Long endTime = System.currentTimeMillis();
        log.debug("urgentCorrespondingApplicantNum " + (endTime - startTime)+"ms user_id="+person.getUser_id()+"&channel="+person.getChannel());
        return StringUtils.isNotEmpty(result) ? Integer.valueOf(result) : SystemConstant.EMPTY_RESULT;
    }

    /**
     * 紧急联系人处于还款中
     *
     * @param person
     * @return
     * @author xp
     */
    private int urgentAmount(Person person) {
        try {
            Long startTime = System.currentTimeMillis();
            int count = 0;
            String user_id = person.getUser_id();
            String channel = person.getChannel();
            Map<String, String> postMap = Maps.newHashMap();
            postMap.put("user_id", user_id);
            postMap.put("channel", channel);
            log.debug("-threadId-" + Thread.currentThread().getId() + "-mobile-" + person.getPid() + "-channel-" + person.getChannel() + "-urgentAmount of-" + null + "-params-" + JacksonUtil.toJSon(postMap));
            String entity = HttpClients.urlPost(pps.getProperty("declineNumURL") + "/neo4j/urgentAmount", postMap);
            log.debug("-threadId-" + Thread.currentThread().getId() + "-mobile-" + person.getPid() + "-channel-" + person.getChannel() + "-urgentAmount of-" + null + "-result-" + entity);
            if (StringUtils.isNoneBlank(entity)) count = Integer.parseInt(entity);
            Long endTime = System.currentTimeMillis();
            log.debug("urgentAmount " + (endTime - startTime)+"ms user_id="+person.getUser_id()+"&channel="+person.getChannel());
            return count;
        } catch (Exception e) {
            log.error("-threadId-" + Thread.currentThread().getId() + "-channel-" + person.getChannel() + "-urgentAmount of-", e);
            return SystemConstant.EXCEPTION_RESULT;
        }


    }

    /**
     * 关系中有过续贷的人数
     *
     * @param person
     * @param personList
     * @param degreeFlag
     * @return
     * @author xp
     */
    private int repayAmount(Person person, List<Person> personList, String degreeFlag) {
        try {
            Long startTime = System.currentTimeMillis();
            int count = 0;
            if (CollectionUtils.isNotEmpty(personList)) {
                StringBuffer str = new StringBuffer();
                for (Person p : personList) {
                    str.append(",").append(p.getPid());
                }
                String mobiles = str.toString().substring(1);
                Map<String, String> postMap = Maps.newHashMap();
                postMap.put("personIds", mobiles);
                postMap.put("channel", person.getChannel());
                postMap.put("userId", person.getUser_id());
                log.debug("-threadId-" + Thread.currentThread().getId() + "-mobile-" + person.getPid() + "-channel-" + person.getChannel() + "-repayAmount of-" + degreeFlag + "-params-" + JacksonUtil.toJSon(postMap));
                String entity = HttpClients.urlPost(pps.getProperty("declineNumURL") + "/neo4j/repayAmount", postMap);
                log.debug("-threadId-" + Thread.currentThread().getId() + "-mobile-" + person.getPid() + "-channel-" + person.getChannel() + "-repayAmount of-" + degreeFlag + "-result-" + entity);
                if (StringUtils.isNoneBlank(entity)) count = Integer.parseInt(entity);
            }
            Long endTime = System.currentTimeMillis();
            log.debug("repayAmount " + (endTime - startTime)+"ms user_id="+person.getUser_id()+"&channel="+person.getChannel());
            return count;
        } catch (Exception e) {
            log.error("-threadId-" + Thread.currentThread().getId() + "-channel-" + person.getChannel() + "-repayAmount of-" + degreeFlag + "-error-", e);
            return SystemConstant.EXCEPTION_RESULT;
        }

    }

    /**
     * 联系人中命中黑名单人数
     *
     * @param person
     * @param personList
     * @param degreeFlag
     * @return
     * @author olivia.wei
     */
    private Object hitBlackNum(Person person, List<Person> personList, String degreeFlag) {

        String result = null;
        Long startTime = System.currentTimeMillis();
        try {

            Map paramMap = Maps.newHashMap();
            if (CollectionUtils.isNotEmpty(personList)) {
                StringBuffer str = new StringBuffer();
                for (Person p : personList) {
                    str.append(",").append(p.getPid());
                }
                paramMap.put("personIds", str.toString().substring(1));
                paramMap.put("channel", person.getChannel());
                paramMap.put("userId", person.getUser_id());
                log.debug("-threadId-" + Thread.currentThread().getId() + "-mobile-" + person.getPid() + "-channel-" + person.getChannel() + "-hitBlackNum of-" + degreeFlag + "-params-" + JacksonUtil.toJSon(paramMap));
                result = HttpClients.urlPost(pps.getProperty("declineNumURL") + "/neo4j/hitBlackNum", paramMap);
                log.debug("-threadId-" + Thread.currentThread().getId() + "-mobile-" + person.getPid() + "-channel-" + person.getChannel() + "-hitBlackNum of-" + degreeFlag + "-result-" + result);
            }
        } catch (Exception e) {
            log.error("-threadId-" + Thread.currentThread().getId() + "-mobile-" + person.getPid() + "-channel-" + person.getChannel() + "-hitBlackNum of-" + degreeFlag + "-error-", e);
            return SystemConstant.EXCEPTION_RESULT;
        }
        Long endTime = System.currentTimeMillis();
        log.debug("hitBlackNum " + (endTime - startTime)+"ms user_id="+person.getUser_id()+"&channel="+person.getChannel());
        return StringUtils.isNotEmpty(result) ? Integer.parseInt(result) : SystemConstant.EMPTY_RESULT;
    }

    /**
     * 异常特征值初始化为-2
     *
     * @param
     * @author olivia.wei
     */
    private void getExceptionResult(Map<String, Object> map) {

        // 一度联系人个数
        map.put("1d_contact_num", SystemConstant.EXCEPTION_RESULT);
        // 一度联系人过去一个月在本平台申请的人数 所有渠道
        map.put("1d_apply_num", SystemConstant.EXCEPTION_RESULT);
        // 一度联系人过去一个月在本平台申请的人数 当前渠道
        map.put("1d_apply_num_current", SystemConstant.EXCEPTION_RESULT);
        // 一度联系人申请通过人的逾期人数
        map.put("1d_overdue_num", SystemConstant.EXCEPTION_RESULT);
        // 一度联系人中申请授信被拒绝人数 所有渠道
        map.put("1d_decline_num", SystemConstant.EXCEPTION_RESULT);
        // 一度联系人中申请授信被拒绝人数 当前渠道
        map.put("1d_decline_num_current", SystemConstant.EXCEPTION_RESULT);
        // 一度联系人中借款但未还款人数 所有渠道
        map.put("1d_unpaid_num", SystemConstant.EXCEPTION_RESULT);
        // 一度联系人中借款但未还款人数 当前渠道
        map.put("1d_unpaid_num_current", SystemConstant.EXCEPTION_RESULT);
        // 一度联系人中逾期金额
        map.put("1d_overdue_amount", SystemConstant.EXCEPTION_RESULT_DOUBLE_TYPE);
        //一度关系有过续贷的人数
        map.put("1d_renew_loan_num", SystemConstant.EXCEPTION_RESULT);
        // 一度联系人中命中黑名单人数
        map.put("1d_hit_black_num", SystemConstant.EXCEPTION_RESULT);
        // 一度联系人近六个月逾期人数占总借款人数占比
        map.put("1d_overdue_num_ratio", SystemConstant.EXCEPTION_RESULT_DOUBLE_TYPE);

        // 二度联系人个数
        map.put("2d_contact_num", SystemConstant.EXCEPTION_RESULT);
        // 二度联系人过去一个月在本平台申请的人数 所有渠道
        map.put("2d_apply_num", SystemConstant.EXCEPTION_RESULT);
        // 二度联系人过去一个月在本平台申请的人数 当前渠道
        map.put("2d_apply_num_current", SystemConstant.EXCEPTION_RESULT);
        // 二度联系人申请通过的人中有多少逾期的人数
        map.put("2d_overdue_num", SystemConstant.EXCEPTION_RESULT);
        // 二度联系人中申请授信被拒绝人数 所有渠道
        map.put("2d_decline_num", SystemConstant.EXCEPTION_RESULT);
        // 二度联系人中申请授信被拒绝人数 当前渠道
        map.put("2d_decline_num_current", SystemConstant.EXCEPTION_RESULT);
        // 二度联系人中借款但未还款人数 所有渠道
        map.put("2d_unpaid_num", SystemConstant.EXCEPTION_RESULT);
        // 二度联系人中借款但未还款人数 当前渠道
        map.put("2d_unpaid_num_current", SystemConstant.EXCEPTION_RESULT);
        // 二度联系人中逾期金额
        map.put("2d_overdue_amount", SystemConstant.EXCEPTION_RESULT_DOUBLE_TYPE);
        //二度关系有过续贷的人数
        map.put("2d_renew_loan_num", SystemConstant.EXCEPTION_RESULT);
        // 二度联系人中命中黑名单人数
        map.put("2d_hit_black_num", SystemConstant.EXCEPTION_RESULT);

        // 团伙联系人个数
        map.put("0d_contact_num", SystemConstant.EXCEPTION_RESULT);
        // 团伙联系人过去一个月在本平台申请的人数 当前渠道
        map.put("0d_apply_num_current", SystemConstant.EXCEPTION_RESULT);
        // 团伙联系人申请通过的人中有多少逾期的人数 当前渠道
        map.put("0d_overdue_num_current", SystemConstant.EXCEPTION_RESULT);
        // 团伙联系人中申请授信被拒绝人数 当前渠道
        map.put("0d_decline_num_current", SystemConstant.EXCEPTION_RESULT);
        // 团伙联系人中借款但未还款人数 当前渠道
        map.put("0d_unpaid_num_current", SystemConstant.EXCEPTION_RESULT);
        // 团伙中电话号码对应的城市数量
        map.put("0d_mobile_address_num_current", SystemConstant.EXCEPTION_RESULT);
        //团伙亲密度得分
        map.put("0d_intimacy_score", SystemConstant.EXCEPTION_RESULT);
        // 近90天同一个手机号码之前被拒绝授信次数
        map.put("mobile_decline_num", SystemConstant.EXCEPTION_RESULT);
        // 用户填写的紧急联系人号码对应多少个申请人
        map.put("urgent_corresponding_applicant_num", SystemConstant.EXCEPTION_RESULT);
        // 申请手机号码对应联系人姓名数量
        map.put("mobile_ corresponding_multi_name_num", SystemConstant.EXCEPTION_RESULT);
        //紧急联系人处于还款中
        map.put("urgent_unpaid_num", SystemConstant.EXCEPTION_RESULT);

        map.put("scdata_code", 2);
        map.put("scdata_msg", "success");
    }

    /**
     * 一度联系人个数
     *
     * @param personList
     * @return
     * @author olivia.wei
     */
    private int contactNum(List<Person> personList, String degreeFlag) {
        try {

            return CollectionUtils.isEmpty(personList) ? SystemConstant.EMPTY_RESULT : personList.size();
        } catch (Exception e) {
            log.error("-threadId-" + Thread.currentThread().getId() + "-channel-" + null + "-queryContactsNum of-" + degreeFlag + "-error-", e);
            return SystemConstant.EXCEPTION_RESULT;
        }
    }

    //一度联系人中逾期金额
    private double overdueAmount(Person person, List<Person> personList, String degreeFlag, String channelFlag) {
        try {
            Long startTime = System.currentTimeMillis();
            BigDecimal count = new BigDecimal("0.00");
            if (CollectionUtils.isNotEmpty(personList)) {
                StringBuffer str = new StringBuffer();
                for (Person p : personList) {
                    //获取一度联系人, 拼凑一个联系人串，给数据中间层查询
                    str.append(",").append(p.getPid());
                }
                String mobiles = str.toString().substring(1);
                Map<String, Object> postMap = Maps.newHashMap();
                postMap.put("personIds", mobiles);
                postMap.put("applytime", person.getApplyTime());
                postMap.put("channel", person.getChannel());
                postMap.put("channelFlag", channelFlag);
                postMap.put("userId", person.getUser_id());
                log.debug("-threadId-" + Thread.currentThread().getId() + "-mobile-" + person.getPid() + "-channel-" + person.getChannel() + "-overdueAmount of-" + degreeFlag + "-params-" + JacksonUtil.toJSon(postMap));
                String entity = HttpClients.urlPost(pps.getProperty("declineNumURL") + "/neo4j/overDueAmount", postMap);
                log.debug("-threadId-" + Thread.currentThread().getId() + "-mobile-" + person.getPid() + "-channel-" + person.getChannel() + "-overdueAmount of-" + degreeFlag + "-result-" + entity);
                if (StringUtils.isNoneBlank(entity)) count = new BigDecimal(entity);
            }
            Long endTime = System.currentTimeMillis();
            log.debug("overdueAmount " + (endTime - startTime)+"ms user_id="+person.getUser_id()+"&channel="+person.getChannel());
            return count.setScale(2, RoundingMode.HALF_UP).doubleValue();
        } catch (Exception e) {
            log.error("-threadId-" + Thread.currentThread().getId() + "-mobile-" + person.getPid() + "-channel-" + person.getChannel() + "-overdueAmount of-" + degreeFlag + "-error-", e);
            return SystemConstant.EXCEPTION_RESULT_DOUBLE_TYPE;
        }
    }


    @Override
    //查询团伙信息
    public List<List<Person>> selectGang(Person paramPerson) {
        try {
            Long startTime = System.currentTimeMillis();
            if (paramPerson != null && StringUtils.isNoneBlank(paramPerson.getPid())) {

                List<List<Person>> rounds = new ArrayList<List<Person>>();
                List<List<Object>> personList = personRepository.selectGang(paramPerson.getPid());

                for (List<Object> list : personList) {
                    List<Person> circle = new ArrayList<Person>();
                    List<String> tmp = new ArrayList<>();
                    for (Object phone : list) {
                        Map<String, String> mobiles = (LinkedHashMap) phone;
                        if (mobiles.size() != 0 && mobiles.keySet().contains("pid"))
                            tmp.add(mobiles.get("pid"));
                    }
                    int count = tmp.size();
                    for (int i = 0; i < count - 1; i++) {
                        int source = i - 1;
                        Person p = new Person();
                        if (source == -1) source = count - 2;
                        p.setSourcePid(tmp.get(source));
                        p.setPid(tmp.get(i));
                        p.setSid(tmp.get(i + 1));
                        circle.add(p);
                    }
                    rounds.add(circle);
                }
                Long endTime = System.currentTimeMillis();
                log.debug("selectGang " + (endTime - startTime)+"ms");
                return rounds;

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    //一度联系人中借款但未还款人数
    private int loanButUnpaidNum(Person person, List<Person> personList, String degreeFlag, String channelFlag) {
        try {
            Long startTime = System.currentTimeMillis();
            int count = 0;
            if (CollectionUtils.isNotEmpty(personList)) {
                StringBuffer str = new StringBuffer();
                for (Person p : personList) {
                    //获取一度联系人, 拼凑一个联系人串，给数据中间层查询
                    str.append(",").append(p.getPid());
                }
                String mobiles = str.toString().substring(1);
                Map<String, Object> postMap = Maps.newHashMap();
                postMap.put("personIds", mobiles);
                postMap.put("applytime", person.getApplyTime());
                postMap.put("channel", person.getChannel());
                postMap.put("channelFlag", channelFlag);
                postMap.put("userId", person.getUser_id());
                log.debug("-threadId-" + Thread.currentThread().getId() + "-mobile-" + person.getPid() + "-channel-" + person.getChannel() + "-loanButUnpaidNum of-" + degreeFlag + "-params-" + JacksonUtil.toJSon(postMap));
                String entity = HttpClients.urlPost(pps.getProperty("declineNumURL") + "/neo4j/loanButUnpaidNumOneD", postMap);
                log.debug("-threadId-" + Thread.currentThread().getId() + "-mobile-" + person.getPid() + "-channel-" + person.getChannel() + "-loanButUnpaidNum of-" + degreeFlag + "-result-" + entity);
                if (StringUtils.isNoneBlank(entity)) count = Integer.parseInt(entity);
            }
            Long endTime = System.currentTimeMillis();
            log.debug("loanButUnpaidNum " + (endTime - startTime)+"ms user_id="+person.getUser_id()+"&channel="+person.getChannel());
            return count;
        } catch (Exception e) {
            log.error("-threadId-" + Thread.currentThread().getId() + "-mobile-" + person.getPid() + "-channel-" + person.getChannel() + "-loanButUnpaidNum of-" + degreeFlag + "-error-", e);
            return SystemConstant.EXCEPTION_RESULT;
        }
    }

    //一度联系人申请通过人的逾期人数
    private int overDueNum(Person person, List<Person> personList, String degreeFlag, String channelFlag) {
        try {
            Long startTime = System.currentTimeMillis();
            int count = 0;
            if (CollectionUtils.isNotEmpty(personList)) {
                StringBuffer str = new StringBuffer();
                for (Person p : personList) {
                    //获取一度联系人, 拼凑一个联系人串，给数据中间层查询
                    str.append(",").append(p.getPid());
                }
                String mobiles = str.toString().substring(1);
                Map<String, Object> postMap = Maps.newHashMap();
                postMap.put("personIds", mobiles);
                postMap.put("applytime", person.getApplyTime());
                postMap.put("channel", person.getChannel());
                postMap.put("channelFlag", channelFlag);
                postMap.put("userId", person.getUser_id());
                log.debug("-threadId-" + Thread.currentThread().getId() + "-mobile-" + person.getPid() + "-channel-" + person.getChannel() + "-overDueNum of-" + degreeFlag + "-params-" + JacksonUtil.toJSon(postMap));
                String entity = HttpClients.urlPost(pps.getProperty("declineNumURL") + "/neo4j/overDueNumOneD", postMap);
                log.debug("-threadId-" + Thread.currentThread().getId() + "-mobile-" + person.getPid() + "-channel-" + person.getChannel() + "-overDueNum of-" + degreeFlag + "-result-" + entity);
                if (StringUtils.isNoneBlank(entity)) count = Integer.parseInt(entity);
            }
            Long endTime = System.currentTimeMillis();
            log.debug("overDueNum " + (endTime - startTime)+"ms user_id="+person.getUser_id()+"&channel="+person.getChannel());
            return count;
        } catch (Exception e) {
            log.error("-threadId-" + Thread.currentThread().getId() + "-mobile-" + person.getPid() + "-channel-" + person.getChannel() + "-overDueNum of-" + degreeFlag + "-error-", e);
            return SystemConstant.EXCEPTION_RESULT;
        }
    }

    //一度联系人过去一个月在本平台申请的人数
    private int applyNum(Person person, List<Person> personList, String degreeFlag, String channelFlag) {
        try {
            Long startTime = System.currentTimeMillis();
            int count = 0;
            if (CollectionUtils.isNotEmpty(personList)) {
                StringBuffer str = new StringBuffer();
                for (Person p : personList) {
                    //获取一度联系人, 拼凑一个联系人串，给数据中间层查询
                    str.append(",").append(p.getPid());
                }
                String mobiles = str.toString().substring(1);
                Map<String, Object> postMap = Maps.newHashMap();
                postMap.put("personIds", mobiles);
                postMap.put("channel", person.getChannel());
                postMap.put("applytime", person.getApplyTime());
                postMap.put("channelFlag", channelFlag);
                postMap.put("userId", person.getUser_id());
                log.debug("-threadId-" + Thread.currentThread().getId() + "-mobile-" + person.getPid() + "-channel-" + person.getChannel() + "-applyNum of-" + degreeFlag + "-params-" + JacksonUtil.toJSon(postMap));
                String entity = HttpClients.urlPost(pps.getProperty("declineNumURL") + "/neo4j/applyNumOneD", postMap);
                log.debug("-threadId-" + Thread.currentThread().getId() + "-mobile-" + person.getPid() + "-channel-" + person.getChannel() + "-applyNum of-" + degreeFlag + "-result-" + entity);
                if (StringUtils.isNoneBlank(entity)) count = Integer.parseInt(entity);
            }
            Long endTime = System.currentTimeMillis();
            log.debug("applyNum " + (endTime - startTime)+"ms  user_id="+person.getUser_id()+"&channel="+person.getChannel());
            return count;
        } catch (Exception e) {
            log.error("-threadId-" + Thread.currentThread().getId() + "-mobile-" + person.getPid() + "-channel-" + person.getChannel() + "-applyNum of-" + degreeFlag + "-error-", e);
            return SystemConstant.EXCEPTION_RESULT;
        }
    }

    //一度联系人中申请授信被拒绝人数
    private int declineNum(Person person, List<Person> personList, String degreeFlag, String channelFlag) {
        try {
            Long startTime = System.currentTimeMillis();
            int count = 0;
            if (CollectionUtils.isNotEmpty(personList)) {
                StringBuffer sbr = new StringBuffer();
                for (Person p : personList) {
                    //获取一度联系人, 拼凑一个联系人串，给数据中间层查询
                    sbr.append(",").append(p.getPid());
                }
                String personIds = sbr.toString().substring(1); //去掉首个逗(,)号
                Map postMap = Maps.newHashMap();
                postMap.put("personIds", personIds);
                postMap.put("channel", person.getChannel());
                postMap.put("applytime", person.getApplyTime());
                postMap.put("channelFlag", channelFlag);
                postMap.put("userId", person.getUser_id());
                log.debug("-threadId-" + Thread.currentThread().getId() + "-mobile-" + person.getPid() + "-channel-" + person.getChannel() + "-declineNum of-" + degreeFlag + "-params-" + JacksonUtil.toJSon(postMap));
                String result = HttpClients.urlPost(pps.getProperty("declineNumURL") + "/neo4j/declineNumOneD", postMap);
                log.debug("-threadId-" + Thread.currentThread().getId() + "-mobile-" + person.getUser_id() + "-channel-" + person.getChannel() + "-declineNum of-" + degreeFlag + "-result-" + result);
                if (StringUtils.isNotBlank(result)) count = Integer.parseInt(result);
            }
            Long endTime = System.currentTimeMillis();
            log.debug("declineNum " + (endTime - startTime)+"ms user_id="+person.getPid()+"&channel="+person.getChannel());
            return count;
        } catch (Exception e) {
            log.error("-threadId-" + Thread.currentThread().getId() + "-mobile-" + person.getPid() + "-channel-" + person.getChannel() + "-declineNum of-" + degreeFlag + "-error-", e);
            return SystemConstant.EXCEPTION_RESULT;
        }
    }

    @Override
    public String testMethod() {
      /* String TEST_PERSON_NAME_1 = "Person1";
        String TEST_PERSON_NAME_2 = "Person2";

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
        String json = JacksonUtil.toJSon(person);

        personRepository.delete(person);
        person = personRepository.findOne(originalId);
        assertNull(person);
        return json;*/

        return "";
    }

    public String getRecordsByUserId(String callDetails) {
        String result = HttpClients.urlGet(pps.getProperty("declineNumURL") + "/neo4j/one1DRecords", callDetails);
        return result;
    }

    public String getRecordsByUserIdAndChannel(String callDetails) {
        String result = HttpClients.urlGet(pps.getProperty("declineNumURL") + "/neo4j/getRecordsByUserIdAndChannel", callDetails);
        return result;
    }

    public void saveRecordsByUserIdAndChannel(Map<String, String> result) {
        HttpClients.urlPost(pps.getProperty("declineNumURL") + "/neo4j/saveRecordsByUserIdAndChannel", result);

    }

    @Override
    public List<Person> queryContactsByPersonAndFlag(Person person, String flag) {
        List<Person> personList = null;
        if (StringUtils.equals(SystemConstant.FLAG_OF_ONE_DEGREE, flag)) {
            personList = selectPerson(person);
        } else if (StringUtils.equals(SystemConstant.FLAG_OF_TWO_DEGREE, flag)) {
            personList = find2DNum(person);
        }
        return personList;
    }

    public void remove(Map<String, String> result) {
        HttpClients.urlPost(pps.getProperty("declineNumURL") + "/neo4j/removemongo", result);
    }

    ;

    //判断节点是否存在    false表示不存在否则存在
    @Override
    public boolean exitNode(String pid) {
        Person p = personRepository.exitNode(pid);
        if (p == null) return false;
        return true;
    }

    @Override
    public boolean exitRelation(String pid, String sid) {
        if (StringUtils.isBlank(pid) || StringUtils.isBlank(sid)) return false;
        Person p = personRepository.exitRelation(pid, sid);
        if (p != null) return true;
        return false;
    }

    @Override
    public String getApplyTime(Map<String, String> map) {
        String result = HttpClients.urlPost(pps.getProperty("declineNumURL") + "/neo4j/getapplytime", map);
        if (StringUtils.isBlank(result)) {
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date time = new Date(System.currentTimeMillis());
            result = df.format(time);

        }
        return result;
    }

    @Override
    public Map<String, Integer> nodeInOutAndAllCount(Person person, List<List<Person>> zeroPersonList) {
        Map<String, Integer> count = Maps.newHashMap();
        count.put(SystemConstant.IN, 0);
        count.put(SystemConstant.OUT, 0);
        count.put(SystemConstant.ALL, 0);
        Set<String> all = new HashSet<String>();
        Set<String> in = new HashSet<>();
        Set<String> out = new HashSet<>();
        if (CollectionUtils.isNotEmpty(zeroPersonList)) {
            for (List<Person> lp : zeroPersonList) {
                all.add(person.getPid() + lp.get(0).getSid());
                all.add(lp.get(0).getSourcePid() + person.getPid());
                out.add(person.getPid() + lp.get(0).getSid());
                in.add(lp.get(0).getSourcePid() + person.getPid());
            }
        }
        count.put(SystemConstant.IN, in.size());
        count.put(SystemConstant.OUT, out.size());
        count.put(SystemConstant.ALL, all.size());
        return count;
    }

    @Override
    public void getCurrentUserTime(Map<String, Object> map) {
        String result = HttpClients.urlPost(pps.getProperty("declineNumURL") + "/neo4j/getCurrentUserTime", map);
        if (StringUtils.isNotEmpty(result)) {
            Person person = JacksonUtil.readValue(result,Person.class);
            map.put("applyTime", person.getApplyTime());
            map.put("loanApplyTime", person.getLoanApplyTime());
        }
    }

}
