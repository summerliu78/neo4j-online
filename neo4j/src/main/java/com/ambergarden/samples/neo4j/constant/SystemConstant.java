package com.ambergarden.samples.neo4j.constant;

/**
 * <p>[描述信息：系统类常量]</p>
 *
 * @author olivia.wei
 * @version 1.0 Created on 2017/11/30 10:52
 */
public class SystemConstant {

    /**
     * 成功状态码
     */
    public static final Integer CODE_SUCCESS = 2;
    public static final String MSG_SUCCESS = "success";

    /**
     * 未执行计算，置为-3
     */
    public static final Integer NO_EXEC_RESULT = -3;
    /**
     * 未执行计算，置为-3.00
     */
    public static final Double NO_EXEC_RESULT_DOUBLE_TYPE = -3.00;
    /**
     * 当前用户不存在，置为-1
     */
    public static final Integer NONE_RESULT = -1;
    /**
     * 当前用户不存在，置为-1
     */
    public static final Double NONE_RESULT_DOUBLE_TYPE = -1.00;
    /**
     * 特征值为0
     */
    public static final Integer EMPTY_RESULT = 0;
    /**
     * 特征值为0
     */
    public static final Double EMPTY_RESULT_DOUBLE_TYPE = 0.00;
    /**
     * 一度联系人标志
     */
    public static final String FLAG_OF_ONE_DEGREE = "1";
    /**
     * 二度联系人标志
     */
    public static final String FLAG_OF_TWO_DEGREE = "2";
    /**
     * 团伙联系人标志
     */
    public static final String FLAG_OF_ZERO_DEGREE = "0";
    /**
     * 系统异常，置为-2
     */
    public static final Integer EXCEPTION_RESULT = -2;
    /**
     * 系统异常，置为-2.00
     */
    public static final Double EXCEPTION_RESULT_DOUBLE_TYPE = -2.00;
    /**
     * 0代表查所有平台
     */
    public static final String ALL_CHANNEL_FLAG = "0";
    /**
     * 1查询当前平台
     */
    public static final String CURRENT_CHANNEL_FLAG = "1";
    /**
     * 团伙中单个节点的入度
     */
    public static final String IN="IN";
    /**
     * 团伙中单个节点的出度
     */
    public static final String OUT="OUT";
    /**
     * 团伙中单个节点的入度+初度
     */
    public static final String ALL="ALL";

}
