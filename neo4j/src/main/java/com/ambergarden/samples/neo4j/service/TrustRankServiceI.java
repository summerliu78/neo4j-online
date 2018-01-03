package com.ambergarden.samples.neo4j.service;

import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Created by think on 2017/12/7.
 */

public interface TrustRankServiceI {

    public void saveScoreToCenterPath(Map<String, String> result);


}
