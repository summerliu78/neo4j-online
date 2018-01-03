package com.ambergarden.samples.neo4j.util;

import com.alibaba.fastjson.JSON;
import com.ambergarden.samples.neo4j.constant.SystemConstant;
import com.ambergarden.samples.neo4j.entities.ResultSpecValues;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.util.Map;

/**
 * Unit test class for CommonUtil
 */
@Slf4j
public class CommonUtilsTest {

    @Test
    public void GetSpecifiedResultTest(){
        ResultSpecValues specValues = new ResultSpecValues(
            SystemConstant.NO_EXEC_RESULT,
            SystemConstant.NO_EXEC_RESULT_DOUBLE_TYPE,
            SystemConstant.CODE_SUCCESS,
            SystemConstant.MSG_SUCCESS);
        Map<String, Object> results = CommonUtils.getSpecifiedResult(specValues);

        log.info("Specified results: {}", JSON.toJSONString(results));
    }

    @Test
    public void GetSpecified0DContactsInfoTest() {
        ResultSpecValues specValues = new ResultSpecValues(
            SystemConstant.NO_EXEC_RESULT,
            SystemConstant.NO_EXEC_RESULT_DOUBLE_TYPE,
            SystemConstant.CODE_SUCCESS,
            SystemConstant.MSG_SUCCESS);
        Map<String, Object> results = CommonUtils.getSpecifiedODContactsInfo(specValues);

        log.info("Specified 0D contacts info: {}", JSON.toJSONString(results));
    }

}
