package com.ambergarden.samples.neo4j.util;

import com.google.common.collect.Lists;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;

@Component
public class FileUtils {

    private static final Logger logger = Logger.getLogger(FileUtils.class.getName());
    private static Properties properties;

    static {
        try {
            properties = new Properties();
            InputStream inStr = FileUtils.class.getClassLoader().getResourceAsStream("./conf/general.properties");
            properties.load(inStr);
        } catch (IOException e) {
            logger.error("-threadId-" + Thread.currentThread().getId() + "-general.properties load error- ", e);
        }

    }

    private FileUtils() {
    }

    public static void main(String[] args) {
    }

    public static String GetOnePersonFilePath() {
        return (String) properties.get("OnePersonFilePath");
    }

    public static String getRankFilePath() {
        return properties.getProperty("rankFilePath");
    }
}

