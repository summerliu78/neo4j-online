package com.ambergarden.samples.neo4j.controller;


import com.ambergarden.samples.neo4j.constant.SystemConstant;
import com.ambergarden.samples.neo4j.entities.Message;
import com.ambergarden.samples.neo4j.entities.Param;
import com.ambergarden.samples.neo4j.entities.Person;
import com.ambergarden.samples.neo4j.entities.ResultSpecValues;
import com.ambergarden.samples.neo4j.service.DataServiceI;
import com.ambergarden.samples.neo4j.util.*;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;

//curl http://10.2.20.118:8080/neo4j/checkServer
//curl http://10.2.20.118:8080/neo4j/selectNode?mphone=p18282898809
//curl http://10.2.20.118:8080/neo4j/updateNode?sphone=p18892895038&avgTime=1&callTime=1
//curl http://10.2.20.118:8080/neo4j/insertNode?mphone=p18282898809&sphone=p110&avgTime=1&callTime=1

//@Controller
public class DataController {
    private static final int size = 10;
    private static final Logger log = Logger.getLogger(DataController.class.getName());
    public static BlockingQueue<String> queue = new LinkedBlockingQueue<String>();
    @Autowired
    @Qualifier("taskExecutor")
    private TaskExecutor taskExecutor;
    @Autowired
    private DataServiceI dataServiceI;

    private static Map<String, List<String>> phone_1D_map = null;
    private static boolean neo4j_ok = true;

    //http://localhost:9090/selectNode?mphone=p18282898809
    @RequestMapping(value = "/selectNode", produces = "application/json; charset=utf-8")
    @ResponseBody
    public String selectNode(HttpServletRequest request, HttpServletResponse response) {
        try {
            neo4j_ok = true;
            long startTime = System.currentTimeMillis();
            String userId = request.getParameter("user_id");
            String channel = request.getParameter("channel");
            String type = request.getParameter("type");
            log.info("query start : userid=" + userId + "&channel=" + channel+"type="+type);
            String param1 = "user_id=" + userId + "&channel=" + channel;
            Param param = new Param();
            param.setUserId(userId);//用户AAAAID
            param.setChannel(channel);//渠道
            param.setType(type);
            Map resultMap = Maps.newHashMap();
            resultMap.put("user_id",userId);
            resultMap.put("channel",channel);
            resultMap.put("type",type);
            String recods = dataServiceI.getRecordsByUserIdAndChannel(param1).replaceAll("\\\\", "");
            if (recods.startsWith("\"")) {
                recods = recods.substring(1, recods.length() - 1);
            }
            JSONObject obj = new JSONObject(recods);
            if (obj.keySet().contains("scdata_code") && obj.getInt("scdata_code") == 1) {
                this.insertQueue(param);
                long endTime = System.currentTimeMillis();
                log.info("query finish : userid=" + userId + "&channel=" + channel + "&scdata_code=1&elapsed_time=" + String.valueOf(endTime - startTime) + "ms");
                return recods;
            } else if (!obj.keySet().contains("scdata_code")) {
                obj.put("scdata_code", 2);
                obj.put("scdata_msg", "success");
                dataServiceI.remove(resultMap);
                this.insertQueue(param);
                long endTime = System.currentTimeMillis();
                log.info("query finish : userid=" + userId + "&channel=" + channel + "&scdata_code=null&elapsed_time=" + String.valueOf(endTime - startTime) + "ms");
                return obj.toString();
            } else {
                dataServiceI.remove(resultMap);
                this.insertQueue(param);
                long endTime = System.currentTimeMillis();
                log.info("query finish : userid=" + userId + "&channel=" + channel + "&elapsed_time=" + String.valueOf(endTime - startTime) + "ms");
                return recods;
            }
        } catch (Exception e){
            JSONObject obj = new JSONObject();
            obj.put("scdata_code", 3);
            obj.put("scdata_msg", "failed");
            log.info("query finish : scdata_code=3");
            return obj.toString();
        }
    }

    @Scheduled(cron = "0 0 0 * * *")
    public void read1DData() {
        FileReader fr = null;
        BufferedReader br = null;
        try {
            Map<String, List<String>> tmp_map = Maps.newHashMap();
            String path = FileUtils.GetOnePersonFilePath();
            File file = new File(path);
            if (!file.canRead()) {
                return;
            }
            fr = new FileReader(file);
            br = new BufferedReader(fr);
            String word = null;
            while ((word = br.readLine()) != null) {
                String[] s = word.split("\t");
                if (s.length != 0) {
                    List<String> values = new ArrayList<String>();
                    for (int i = 1; i < s.length; ++i) {
                        values.add(s[i]);
                    }
                    tmp_map.put(s[0], values);
                }
            }
            phone_1D_map = tmp_map;
            log.info("1D file read success : file_line=" + String.valueOf(tmp_map.size()));
        } catch (IOException e) {
            e.printStackTrace();
            log.error("1D file read fail");
        } finally {
            try {
                if (br != null) br.close();
                if (fr != null) fr.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return;
    }

    public String insertQueue(Param param) {
        Long startTime = System.currentTimeMillis();
        //要计算的参数，放入队列。
        String insertQueueJson = JacksonUtil.toJSon(param);
        // * offer :添加一个元素并返回true，如果队列已满，则返回false
//        if (queue.offer(insertQueueJson)) {
//            log.info(param.getLoanId() + " " + param.getChannel());
//        }
        queue.offer(insertQueueJson);
        return "";
    }

    @Scheduled(fixedDelay = 2000)    //上次任务执行完以后，3秒再执行一次// @Scheduled(cron="0/3 * *  * * ? ")
    public void queueThread() {

        for (int i = 0; i < 10; i++) {
            Future<String> future = getQueueData(queue);
            try {
                String jsonParam = future.get();
                if (StringUtils.isNotBlank(jsonParam)) {
                    Param param = JacksonUtil.readValue(jsonParam, Param.class);
                    String channel = StringUtils.isBlank(param.getChannel()) ? "A" : param.getChannel();
                    String userid = param.getUserId();
                    log.info("queueTask start : index=" + i + "&userid=" + userid + "&channel=" + channel);
                    taskExecutor.execute(new DataController.TaskThread(future));
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }

    }

    private class TaskThread implements Runnable {
        private Future<String> future;

        public TaskThread(Future<String> future) {
            this.future = future;
        }

        @Override
        public void run() {
//            log.info("线程开始运行：id=" + Thread.currentThread().getId());
            queueTask(future);
        }

    }

    @RequestMapping(value = "/selectNode3", produces = "application/json; charset=utf-8")
    @ResponseBody
    public String selectNode3(HttpServletRequest request){
        return selectNode2(request,null);
    }

    public String selectNode2(HttpServletRequest request, Param param) {
        Map resultMap = getCurrentUserTime(request, param);
        Map<String, String> map = handleCalDetails(request, param);
        Person person = new Person();
        String userId = "";
        String channel = "";
        String type = "";
        if (request != null) {
            userId = request.getParameter("user_id");
            channel = request.getParameter("channel");
            type = request.getParameter("type");
        }
        if (param != null) {
            userId = param.getUserId();
            channel = param.getChannel();
            type = param.getType();
        }
        person.setUser_id(userId);
        person.setChannel(channel);
        person.setApplyTime(resultMap.get("applyTime") == null ? "" : resultMap.get("applyTime") + "");
        person.setLoanApplyTime(resultMap.get("loanApplyTime") == null ? "" : resultMap.get("loanApplyTime") + "");
        person.setType(type);
        Map<String, Object> map1 = new HashMap<>();
        Map<String, Object> map2 = new HashMap<>();
        try {
            insertNode(request, param, map);
        } catch (Exception e) {
            neo4j_ok = false;
            e.printStackTrace();
            log.fatal("neo4j db error : userid=" + userId + "&channel=" + channel);
        }
//        log.info(result)
        if (map.size() == 0) {
            getNoneResult(map1);
            return JacksonUtil.toJSon(map1);
        } else {
            String mphone = map.get("phone_num");

            person.setPid(mphone);

//            //机主手机号
//            if (StringUtils.isNoneBlank(mphone) && map.size() == 1) {
//                getEmptyResult(map2);
//                return JacksonUtil.toJSon(map2);
//            }
            //渠道
//            if (StringUtils.isNoneBlank(param.getChannel())) {
//                person.setChannel(param.getChannel());
//            }
            //String json = JacksonUtil.toJSon(map);
            String json = dataServiceI.selectCallRecords(person, neo4j_ok, phone_1D_map, mphone);
                //            log.info("计算成功，json=" + json);
            return json;
        }
    }

    /**
     * 初始化特征【特征值为0】
     *
     * @param map
     * @author olivia.wei
     */
    private void getEmptyResult(Map<String, Object> map) {
        // 一度联系人个数
        map.put("1d_contact_num", SystemConstant.EMPTY_RESULT);
        // 一度联系人过去一个月在本平台申请的人数 所有渠道
        map.put("1d_apply_num", SystemConstant.EMPTY_RESULT);
        // 一度联系人过去一个月在本平台申请的人数 当前渠道
        map.put("1d_apply_num_current", SystemConstant.EMPTY_RESULT);
        // 一度联系人申请通过人的逾期人数
        map.put("1d_overdue_num", SystemConstant.EMPTY_RESULT);
        // 一度联系人中申请授信被拒绝人数 所有渠道
        map.put("1d_decline_num", SystemConstant.EMPTY_RESULT);
        // 一度联系人中申请授信被拒绝人数 当前渠道
        map.put("1d_decline_num_current", SystemConstant.EMPTY_RESULT);
        // 一度联系人中借款但未还款人数 所有渠道
        map.put("1d_unpaid_num", SystemConstant.EMPTY_RESULT);
        // 一度联系人中借款但未还款人数 当前渠道
        map.put("1d_unpaid_num_current", SystemConstant.EMPTY_RESULT);
        // 一度联系人中逾期金额
        map.put("1d_overdue_amount", SystemConstant.EMPTY_RESULT_DOUBLE_TYPE);
        //一度关系有过续贷的人数
        map.put("1d_renew_loan_num",SystemConstant.EMPTY_RESULT);
        // 一度联系人中命中黑名单人数
        map.put("1d_hit_black_num", SystemConstant.EMPTY_RESULT);
        // 一度联系人近六个月逾期人数占总借款人数占比
        map.put("1d_overdue_num_ratio", SystemConstant.EMPTY_RESULT_DOUBLE_TYPE);

        // 二度联系人个数
        map.put("2d_contact_num", SystemConstant.EMPTY_RESULT);
        // 二度联系人过去一个月在本平台申请的人数 所有渠道
        map.put("2d_apply_num", SystemConstant.EMPTY_RESULT);
        // 二度联系人过去一个月在本平台申请的人数 当前渠道
        map.put("2d_apply_num_current", SystemConstant.EMPTY_RESULT);
        // 二度联系人申请通过的人中有多少逾期的人数
        map.put("2d_overdue_num", SystemConstant.EMPTY_RESULT);
        // 二度联系人中申请授信被拒绝人数 所有渠道
        map.put("2d_decline_num", SystemConstant.EMPTY_RESULT);
        // 二度联系人中申请授信被拒绝人数 当前渠道
        map.put("2d_decline_num_current", SystemConstant.EMPTY_RESULT);
        // 二度联系人中借款但未还款人数 所有渠道
        map.put("2d_unpaid_num", SystemConstant.EMPTY_RESULT);
        // 二度联系人中借款但未还款人数 当前渠道
        map.put("2d_unpaid_num_current", SystemConstant.EMPTY_RESULT);
        // 二度联系人中逾期金额
        map.put("2d_overdue_amount", SystemConstant.EMPTY_RESULT_DOUBLE_TYPE);
        //二度关系有过续贷的人数
        map.put("2d_renew_loan_num",SystemConstant.EMPTY_RESULT);
        // 二度联系人中命中黑名单人数
        map.put("2d_hit_black_num", SystemConstant.EMPTY_RESULT);

        // 团伙联系人个数
        map.put("0d_contact_num", SystemConstant.EMPTY_RESULT);
        // 团伙联系人过去一个月在本平台申请的人数 当前渠道
        map.put("0d_apply_num_current", SystemConstant.EMPTY_RESULT);
        // 团伙联系人申请通过的人中有多少逾期的人数 当前渠道
        map.put("0d_overdue_num_current", SystemConstant.EMPTY_RESULT);
        // 团伙联系人中申请授信被拒绝人数 当前渠道
        map.put("0d_decline_num_current", SystemConstant.EMPTY_RESULT);
        // 团伙联系人中借款但未还款人数 当前渠道
        map.put("0d_unpaid_num_current", SystemConstant.EMPTY_RESULT);
        // 团伙中电话号码对应的城市数量
        map.put("0d_mobile_address_num",  SystemConstant.EMPTY_RESULT);
        //团伙亲密度得分
        map.put("0d_intimacy_score",SystemConstant.EMPTY_RESULT);
        // 近90天同一个手机号码之前被拒绝授信次数
        map.put("mobile_decline_num",  SystemConstant.EMPTY_RESULT);
        // 用户填写的紧急联系人号码对应多少个申请人
        map.put("urgent_corresponding_applicant_num",  SystemConstant.EMPTY_RESULT);
        // 申请手机号码对应联系人姓名数量
        map.put("mobile_ corresponding_multi_name_num",  SystemConstant.EMPTY_RESULT);
        //紧急联系人处于还款中
        map.put("urgent_unpaid_num",SystemConstant.EMPTY_RESULT);

        map.put("scdata_code", 2);
        map.put("scdata_msg", "success");
    }

    /**
     * 初始化特征【当前用户不存在，置为-1】
     *
     * @param map
     * @author olivia.wei
     */
    private void getNoneResult(Map<String, Object> map) {

        // 一度联系人个数
        map.put("1d_contact_num", SystemConstant.NONE_RESULT);
        // 一度联系人过去一个月在本平台申请的人数 所有渠道
        map.put("1d_apply_num", SystemConstant.NONE_RESULT);
        // 一度联系人过去一个月在本平台申请的人数 当前渠道
        map.put("1d_apply_num_current", SystemConstant.NONE_RESULT);
        // 一度联系人申请通过人的逾期人数
        map.put("1d_overdue_num", SystemConstant.NONE_RESULT);
        // 一度联系人中申请授信被拒绝人数 所有渠道
        map.put("1d_decline_num", SystemConstant.NONE_RESULT);
        // 一度联系人中申请授信被拒绝人数 当前渠道
        map.put("1d_decline_num_current", SystemConstant.NONE_RESULT);
        // 一度联系人中借款但未还款人数 所有渠道
        map.put("1d_unpaid_num", SystemConstant.NONE_RESULT);
        // 一度联系人中借款但未还款人数 当前渠道
        map.put("1d_unpaid_num_current", SystemConstant.NONE_RESULT);
        // 一度联系人中逾期金额
        map.put("1d_overdue_amount", SystemConstant.NONE_RESULT_DOUBLE_TYPE);
        //一度关系有过续贷的人数
        map.put("1d_renew_loan_num",SystemConstant.NONE_RESULT);
        // 一度联系人中命中黑名单人数
        map.put("1d_hit_black_num", SystemConstant.NONE_RESULT);
        // 一度联系人近六个月逾期人数占总借款人数占比
        map.put("1d_overdue_num_ratio", SystemConstant.NONE_RESULT_DOUBLE_TYPE);

        // 二度联系人个数
        map.put("2d_contact_num", SystemConstant.NONE_RESULT);
        // 二度联系人过去一个月在本平台申请的人数 所有渠道
        map.put("2d_apply_num", SystemConstant.NONE_RESULT);
        // 二度联系人过去一个月在本平台申请的人数 当前渠道
        map.put("2d_apply_num_current", SystemConstant.NONE_RESULT);
        // 二度联系人申请通过的人中有多少逾期的人数
        map.put("2d_overdue_num", SystemConstant.NONE_RESULT);
        // 二度联系人中申请授信被拒绝人数 所有渠道
        map.put("2d_decline_num", SystemConstant.NONE_RESULT);
        // 二度联系人中申请授信被拒绝人数 当前渠道
        map.put("2d_decline_num_current", SystemConstant.NONE_RESULT);
        // 二度联系人中借款但未还款人数 所有渠道
        map.put("2d_unpaid_num", SystemConstant.NONE_RESULT);
        // 二度联系人中借款但未还款人数 当前渠道
        map.put("2d_unpaid_num_current", SystemConstant.NONE_RESULT);
        // 二度联系人中逾期金额
        map.put("2d_overdue_amount", SystemConstant.NONE_RESULT_DOUBLE_TYPE);
        //二度关系有过续贷的人数
        map.put("2d_renew_loan_num",SystemConstant.NONE_RESULT);
        // 二度联系人中命中黑名单人数
        map.put("2d_hit_black_num", SystemConstant.NONE_RESULT);

        // 团伙联系人个数
        map.put("0d_contact_num", SystemConstant.NONE_RESULT);
        // 团伙联系人过去一个月在本平台申请的人数 当前渠道
        map.put("0d_apply_num_current", SystemConstant.NONE_RESULT);
        // 团伙联系人申请通过的人中有多少逾期的人数 当前渠道
        map.put("0d_overdue_num_current", SystemConstant.NONE_RESULT);
        // 团伙联系人中申请授信被拒绝人数 当前渠道
        map.put("0d_decline_num_current", SystemConstant.NONE_RESULT);
        // 团伙联系人中借款但未还款人数 当前渠道
        map.put("0d_unpaid_num_current", SystemConstant.NONE_RESULT);
        // 团伙中电话号码对应的城市数量
        map.put("0d_mobile_address_num",  SystemConstant.NONE_RESULT);
        //团伙亲密度得分
        map.put("0d_intimacy_score",SystemConstant.NONE_RESULT);
        // 近90天同一个手机号码之前被拒绝授信次数
        map.put("mobile_decline_num",  SystemConstant.NONE_RESULT);
        // 用户填写的紧急联系人号码对应多少个申请人
        map.put("urgent_corresponding_applicant_num",  SystemConstant.NONE_RESULT);
        // 申请手机号码对应联系人姓名数量
        map.put("mobile_ corresponding_multi_name_num",  SystemConstant.NONE_RESULT);
        //紧急联系人处于还款中
        map.put("urgent_unpaid_num",SystemConstant.NONE_RESULT);

        map.put("scdata_code", 2);
        map.put("scdata_msg", "success");
    }

    public void queueTask(Future<String> future) {
        Long startTime = System.currentTimeMillis();
        try {
            String jsonParam = future.get();
            Param param = JacksonUtil.readValue(jsonParam, Param.class);
            Map resultMap = Maps.newHashMap();
            String result = selectNode2(null, param);
            String channel = StringUtils.isBlank(param.getChannel()) ? "A" : param.getChannel();
            String userid = param.getUserId();
            resultMap.put("result", result);
            resultMap.put("channel", channel);
            resultMap.put("user_id", userid);
            //String result1 = "result=" + result + "&user_id=" + param.getUserId() + "&channel=" + param.getChannel();
            dataServiceI.saveRecordsByUserIdAndChannel(resultMap);
            //DataServiceI.rawData(result,param);
//            JacksonUtil.toJSon(result);
            long endTime = System.currentTimeMillis();
            log.info("queueTask finish : userid=" + userid + "&channel=" + channel + "&elapsed_time=" + String.valueOf(endTime - startTime) + "ms");
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    private static Future<String> getQueueData(BlockingQueue queue) {
        ExecutorService executorServer = null;
        try {
            //创建一个线程池，开始去Queue中读取队列数据。如果没有队列数，读出来的null ，要判断下。
            executorServer = Executors.newFixedThreadPool(size);
            Callable callable = new ReaderQueue(queue);
            return executorServer.submit(callable);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            executorServer.shutdown();
            // 当所有任务执行完成后，终止线程池的运行
            try {
                executorServer.awaitTermination(Long.MAX_VALUE, TimeUnit.MINUTES);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    //http://localhost:9090/updateNode?sphone=p18892895038&avgTime=1&callTime=1
    @RequestMapping(value = "/updateNode", produces = "application/json; charset=utf-8")
    @ResponseBody
    public String updateNode(HttpServletRequest request, Param param, Map<String, String> details) {
        Person person = new Person();
        String json = "";
        Map<String, String> map = Maps.newHashMap();

        if (param != null&&StringUtils.isNoneBlank(param.getUserId())) {
            map = handleCalDetails(null, param);
        } else if (request != null) {
            map = handleCalDetails(request, null);
        }else if(details!=null&&!details.keySet().contains("user_id")){
            map=details;
        }


        if (map.size() == 0) {
            return "此user_id和渠道无法查到通话详单,更新neo4j数据库失败";
        } else {
            String phoneNum=map.get("phone_num");
            person.setPid(phoneNum);
            for (Map.Entry m : map.entrySet()) {
                String sphone = (String) m.getKey();
                if (!StringUtils.equals(sphone, "phone_num")) {
                    String[] avgandtime = ((String) m.getValue()).split(",");
                    String times = avgandtime[1];
                    String avg = String.format("%.2f", Double.valueOf(avgandtime[0]) / Integer.valueOf(times));
                    if (StringUtils.isNoneBlank(sphone)) {
                        person.setSid(sphone);
                    } else {
                        return new Message("sphone").toString();
                    }
                    if (StringUtils.isNoneBlank(avg)) {
                        person.setAvgTime(avg);
                    }
                    if (StringUtils.isNoneBlank(times)) {
                        person.setCallTime(times);
                    }

//                    log.info("updateNode person=" + JacksonUtil.toJSon(person));
                    json = dataServiceI.updatePerson(person);
                }
            }
            //联系人手机号

            return json;
        }

    }

    // http://localhost:9090/insertNode?mphone=p18282898809&sphone=p110&avgTime=1&callTime=1
    @RequestMapping(value = "/insertNode", produces = "application/json; charset=utf-8")
    @ResponseBody
    public String insertNode(HttpServletRequest request,Param param,Map<String,String> details) {
        Person person = new Person();
        String json = "";
        int size = details.size();
        if (size == 0 ) {
            return "此user_id和渠道无法查到通话详单,插入neo4j数据库失败";
        } else if(size == 1){
            String mphone = details.get("phone_num");
            if(StringUtils.isNoneBlank(mphone))
                person.setPid(mphone);

            json = JacksonUtil.toJSon(dataServiceI.insertPerson(person));
            return "此人的联系人没有通过本公司平台借过钱...";
        }
        else {
            String mphone = details.get("phone_num");

            for (Map.Entry m : details.entrySet()) {
                String sphone = (String) m.getKey();
                if (!StringUtils.equals(sphone, "phone_num")) {
                    String[] avgandtime = ((String) m.getValue()).split(",");
                    String times = avgandtime[1];
                    String avg = String.format("%.2f", Double.valueOf(avgandtime[0]) / Integer.valueOf(times));
                    //机主手机号
                    if (StringUtils.isNoneBlank(mphone)) {
                        person.setPid(mphone);
                    } else {
                        return new Message("mphone").toString();
                    }
                    //联系人手机号
                    if (StringUtils.isNoneBlank(sphone)) {
                        person.setSid(sphone);
                    } else {
                        return new Message("sphone").toString();
                    }
                    if (StringUtils.isNoneBlank(avg)) {
                        person.setAvgTime(avg);
                    } else {
                        return new Message("avgTime").toString();
                    }
                    if (StringUtils.isNoneBlank(times)) {
                        person.setCallTime(times);
                    } else {
                        return new Message("callTime").toString();
                    }
//                    log.info("insertNode person=" + JacksonUtil.toJSon(person));
                    json = JacksonUtil.toJSon(dataServiceI.insertPerson(person));
//                    log.info("insertNode json=" + JacksonUtil.toJSon(json));
                }
            }
            return json;
        }

    }


    @RequestMapping(value = "/test", produces = "application/json; charset=utf-8")
    @ResponseBody
    public String test() {
        String json = dataServiceI.testMethod();
        log.info("json=" + json);
        return json;
    }

    @RequestMapping(value = "/checkServer", produces = "application/json; charset=utf-8")
    @ResponseBody
    public String checkServer() {
        return "{\"moduleName\":\"neo4j\",\"state\":200}";
    }

    public Map<String, String> handleCalDetails(HttpServletRequest request, Param param1) {
        String userId = "";
        String channel = "";
        if (request != null) {
            userId = request.getParameter("user_id");
            channel = request.getParameter("channel");
        }
        if (param1 != null) {
            userId = param1.getUserId();
            channel = param1.getChannel();
        }
        String param = "user_id=" + userId + "&channel=" + channel;
        long startTime1 = System.currentTimeMillis();
        String recods = dataServiceI.getRecordsByUserId(param);
        long endTime1 = System.currentTimeMillis();
        log.info("get vcardRecords : userid=" + userId + "&channel=" + channel + "&elapsed_time=" + String.valueOf(endTime1 - startTime1) + "ms");
        if(StringUtils.isBlank(recods)){
            log.fatal("record is empty, mongo db error : userid=" + userId + "&channel=" + channel);
            return new HashMap<>();
        }
        JSONObject jsonObject = new JSONObject(recods);
        Object o = jsonObject.get("vcardRecords");
        if (o instanceof String && StringUtils.isBlank((String) o)) {
            log.fatal("vcardRecords string is empty, mongo db error : userid=" + userId + "&channel=" + channel);
            return new HashMap<String, String>();
        } else {
            List<Object> details = jsonObject.getJSONArray("vcardRecords").toList();
            if (details == null || details.isEmpty()) {
                log.fatal("vcardRecords list is empty, mongo db error : userid=" + userId + "&channel=" + channel);
                return new HashMap<>();
            }
            Map<String, String> timesAndAvg = new HashMap<>();
            Map<String, String> timesAndAvgEn = new HashMap<>();
            //本机手机号
            String phone_num = jsonObject.getString("phone_num");
            try {
                for (Object ob : details) {
                    String avg = "0.00";
                    Map<String, String> obj = (Map<String, String>) ob;
                    //对方手机号
                    String opposite_num = obj.get("opposite_num");
                    String flag = obj.get("flag");//flag为主被叫标识，其中1为主叫，0为被叫
                    String statTime = obj.get("start_time");
                    String endTime = obj.get("end_time");
                    if (StringUtils.isNoneBlank(statTime) && StringUtils.isNoneBlank(endTime)) {
                        Date startDate1 = DateUtil.formatToDayByYYYYMMDDMMHHSS(statTime);
                        Date endDate2 = DateUtil.formatToDayByYYYYMMDDMMHHSS(endTime);
                        avg = (endDate2.getTime() - startDate1.getTime()) / 1000 + "";
                    }
                    if (StringUtils.equals(flag, "1")) {
                        if (!timesAndAvg.containsKey(opposite_num)) {
                            timesAndAvg.put(opposite_num, avg + "," + "1");
                        } else {
                            String[] value = timesAndAvg.get(opposite_num).split(",");
                            avg = (Double.valueOf(value[0]) + Double.valueOf(avg)) + "";
                            Integer times = Integer.valueOf(value[1]) + 1;
                            timesAndAvg.put(opposite_num, avg + "," + times);
                        }
                    }
                }
                timesAndAvg.put("phone_num", phone_num);
                timesAndAvgEn.put("phone_num", phone_num);
                timesAndAvg.put("channel",channel);
                timesAndAvg.put("userId",userId);
                long startTime2 = System.currentTimeMillis();
                String result = dataServiceI.timesAndAvgEn(timesAndAvg);
                long endTime2 = System.currentTimeMillis();
                if(StringUtils.isNoneBlank(result)) {
                    String[] arr = result.split(",");
                    for (String str : arr) {
                        timesAndAvgEn.put(str, timesAndAvg.get(str));
                    }
                }
                log.info("timesAndAvgEn : userid=" + userId + "&channel=" + channel + "&elapsed_time="
                        + String.valueOf(endTime2 - startTime2) + "ms&detail_size=" + details.size() + "&timesAndAvgEn_size=" + timesAndAvgEn.size());
            } catch (Exception ex) {
                ex.printStackTrace();
                log.fatal("mongo db error : userid=" + userId + "&channel=" + channel);
            }
            return timesAndAvgEn;
        }

    }
    public String getApplyTime(HttpServletRequest request, Param param){
        Map<String,String> map=Maps.newHashMap();
        if(request!=null){
            String userId=request.getParameter("user_id");
            String channel=request.getParameter("channel");
            map.put("user_id",userId);
            map.put("channel",channel);
        }else if(param!=null){
            String userId=param.getUserId();
            String channel=param.getChannel();
            map.put("user_id",userId);
            map.put("channel",channel);
        }
        String applyTime=dataServiceI.getApplyTime(map);
        return applyTime;
    }

    public Map<String, Object> getCurrentUserTime(HttpServletRequest request, Param param){
        Map<String, Object> map = Maps.newHashMap();
        if(request!=null){
            String userId=request.getParameter("user_id");
            String channel=request.getParameter("channel");
            map.put("user_id",userId);
            map.put("channel",channel);
        }else if(param!=null){
            String userId=param.getUserId();
            String channel=param.getChannel();
            map.put("user_id",userId);
            map.put("channel",channel);
        }
        dataServiceI.getCurrentUserTime(map);
        return map;
    }
}





