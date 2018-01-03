package com.ambergarden.samples.neo4j.service.impl;

import com.ambergarden.samples.neo4j.GraphDBConfiguration;
import com.ambergarden.samples.neo4j.constant.SystemConstant;
import com.ambergarden.samples.neo4j.repositories.PersonRepository;
import com.ambergarden.samples.neo4j.service.TrustRankServiceI;
import com.ambergarden.samples.neo4j.util.HttpClients;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

/**
 * Created by think on 2017/12/7.
 */
@Service
public class TrustRankServiceImpl implements TrustRankServiceI {

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


    @Override
    public void saveScoreToCenterPath(Map<String, String> result) {
        try {

            HttpClients.urlPost(pps.getProperty("declineNumURL") + "/neo4j/saveRecordsByMobile", result);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("存储数据到中间层失败");
        }
    }
}
