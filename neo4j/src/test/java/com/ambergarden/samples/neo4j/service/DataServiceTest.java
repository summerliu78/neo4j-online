package com.ambergarden.samples.neo4j.service;

import com.ambergarden.samples.neo4j.BaseTest;
import com.ambergarden.samples.neo4j.entities.Person;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Unit test class for DataService
 * Created by yaozy on 2018/01/02
 */

@Slf4j
public class DataServiceTest extends BaseTest {

    @Autowired
    private DataServiceI dataServiceI;

    @Test
    public void SelectCallRecordsTest() {
        boolean neo4j = true;
        Person person = new Person();
        person.setPid("13910843622");
        Map<String, List<String>> phone_1D_map = Maps.newHashMap();
        String phone = "13910843622";

        String results = dataServiceI.selectCallRecords(person, neo4j, phone_1D_map, phone);
        log.info("call records result: {}", results);
    }
}
