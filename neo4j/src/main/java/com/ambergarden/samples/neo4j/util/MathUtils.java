package com.ambergarden.samples.neo4j.util;

/**
 * <p>[描述信息：说明类的基本功能]</p>
 *
 * @author olivia.wei
 * @version 1.0 Created on 2017/8/25 18:15
 */
public class MathUtils {
    public static double round(double value){
        return Math.round( value * 10 ) / 10.0;
    }

    public static double round8(double value){
        return Math.round( value * 100000000 ) / 100000000.0;
    }
}
