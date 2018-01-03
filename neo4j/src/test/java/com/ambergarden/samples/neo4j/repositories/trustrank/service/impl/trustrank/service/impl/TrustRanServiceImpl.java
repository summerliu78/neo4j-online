package com.ambergarden.samples.neo4j.repositories.trustrank.service.impl.trustrank.service.impl;

import com.ambergarden.samples.neo4j.GraphDBConfiguration;
import com.ambergarden.samples.neo4j.repositories.trustrank.service.impl.trustrank.service.TrustRankService;
import com.ambergarden.samples.neo4j.service.DataServiceI;
import com.ambergarden.samples.neo4j.service.TrustRankServiceI;
import com.ambergarden.samples.neo4j.util.HttpClients;
import com.ambergarden.samples.neo4j.util.TrustRankUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

/**
 * Created by lw on 2017/12/18.
 */
public class TrustRanServiceImpl implements TrustRankService {



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

    private static TrustRankUtils utils = new TrustRankUtils();


    @Autowired
    private DataServiceI dataServiceI;


    //测试读取文件
    @Override
    public void getDataFromLocal(String path) {
        if (utils.readScore(path).size() >= 10000) {
            System.out.println("success" + " ---- 读取文件成功");
        } else {
            System.out.println("faild  " + " ---- 读取文件成功");

        }
    }

    //查询mongo中是否存在输入数据
    @Override
    public void checkDataFromMongoOrElseTest(String mobile) {
        String res = dataServiceI.getRecordsByUserIdAndChannel("uniqueId:mobile=" + mobile);
        if (StringUtils.isNotBlank(res)) {
            System.out.println("faild  " + " ---- 读取文件成功");
        } else {
            System.out.println("success" + " ---- 读取文件成功");
        }

    }

    //删除mongo中数据
    @Override
    public void deleteMongoDataTest(String mobile) {
//        dataServiceI.
    }


    //测试已经存在数据
    @Override
    public void getRankExistTest(Map<String, String> result) {

        try {
            HttpClients.urlPost(pps.getProperty("declineNumURL") + "/neo4j/saveRecordsByMobile", result);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("存储数据到中间层失败");
        }
    }

    //测试不存在数据
    @Override
    public void getRankNotExistTest(String mobile) {

    }

    //测试输入不规范数据
    @Override
    public void getNonConformityTest() {

    }

    //测试spring cron定时器是否可以定时更新
    @Override
    public void cronTest() {

    }
}
