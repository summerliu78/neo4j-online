package com.ambergarden.samples.neo4j.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Utility tools to get permission setting
 * Create by yaozy on 2017/12/29
 */

@Component
@Slf4j
public class PermUtils {

    private static Properties properties;

    static {
        try {
            properties = new Properties();
            InputStream inStr =
                PermUtils.class.getClassLoader().getResourceAsStream("./conf/graph-perm.properties");
            properties.load(inStr);
        } catch (IOException e) {
            log.error("-threadId-" + Thread.currentThread().getId()
                + "-graph-perm.properties load error- ", e);
        }

    }

    private PermUtils() {
    }

    public static void main(String[] args) {
    }

    public static String GetCalcCallRecordPerm() {
        Object calcCallRecordPerm = properties.get("calc.call.record.perm");
        return calcCallRecordPerm == null ? "OFF" : calcCallRecordPerm.toString();
    }
}

