package com.ambergarden.samples.neo4j.Log;

import org.apache.log4j.Logger;

public class LogHelper {

    public static final String RECORD = "RECORD";
    public static final Logger record = Logger.getLogger(RECORD);


    public static final String ERROR = "ERROR";
    public static final Logger  error =Logger.getLogger(ERROR);
}
