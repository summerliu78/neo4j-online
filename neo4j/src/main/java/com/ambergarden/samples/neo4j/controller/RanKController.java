package com.ambergarden.samples.neo4j.controller;


//import com.ambergarden.samples.neo4j.util.TrustRank;

import com.ambergarden.samples.neo4j.GraphDBConfiguration;
import com.ambergarden.samples.neo4j.service.DataServiceI;
import com.ambergarden.samples.neo4j.service.TrustRankServiceI;
import com.ambergarden.samples.neo4j.util.FileUtils;
import com.ambergarden.samples.neo4j.util.HttpClients;
import com.ambergarden.samples.neo4j.util.JacksonUtil;
import com.ambergarden.samples.neo4j.util.TrustRankUtils;
import com.google.common.collect.Maps;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.regex.Pattern;

@Controller
public class RanKController {
    private static final Logger log = Logger.getLogger(DataController.class.getName());
    private static Map<String, String> ScoreMap = new HashMap<>();
    private static TrustRankUtils utils = new TrustRankUtils();


    @Autowired
    private TrustRankServiceI trustRankServiceI;


    static {
        ScoreMap = utils.readScore(FileUtils.getRankFilePath());
    }

    @Scheduled(cron = "01 01 */1 * * *")
    public void readDataFromLocalSystem() {
        Map<String, String> ScoreMapBak = utils.readScore(FileUtils.getRankFilePath());
        if (ScoreMapBak.size() >= ScoreMap.size()) {
            ScoreMap = ScoreMapBak;
        }
    }


    @RequestMapping(value = "/selectRankScore")
    @ResponseBody
    public String run(HttpServletRequest request) throws IOException {

//        System.out.println("ScoreMap  = " + ScoreMap.size());
        String mobile = request.getParameter("mobile");
        Map<String, String> scoreMaps = Maps.newHashMap();
        Map<String, String> resultMaps = Maps.newHashMap();
        String str = "";

//         * filter掉不是手机号的请求
//         * -9998.0 命中过滤规则规范的请求
//         * -9999.0 map中无数据或数据不完整
        if (Pattern.matches("1[0-9]{10}", mobile)) {
            String scores = ScoreMap.get(mobile);
            if (scores != null) {
                String[] split = scores.split(",");
                String goodScore = split[0];
                String badScore = split[1];
                String finalScore = split[2];
                scoreMaps.put("finalScore", finalScore);
                scoreMaps.put("goodScore", goodScore);
                scoreMaps.put("badScore", badScore);
                str = JacksonUtil.toJSon(scoreMaps);
                resultMaps.put("mobile", mobile);
                resultMaps.put("result", str);
                trustRankServiceI.saveScoreToCenterPath(resultMaps);
            } else {
                scoreMaps.put("finalScore", "-9999.0");
                scoreMaps.put("goodScore", "-9999.0");
                scoreMaps.put("badScore", "-9999.0");
                str = JacksonUtil.toJSon(scoreMaps);
            }
        } else {
            scoreMaps.put("finalScore", "-9998.0");
            scoreMaps.put("goodScore", "-9998.0");
            scoreMaps.put("badScore", "-9998.0");
            str = JacksonUtil.toJSon(scoreMaps);

        }

        log.info("mobile : " + str);
//        System.out.println(JacksonUtil.toJSon(resultMaps));
        return str;
    }

}
