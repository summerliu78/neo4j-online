package com.ambergarden.samples.neo4j.util;

import com.ambergarden.samples.neo4j.constant.SystemConstant;
import com.ambergarden.samples.neo4j.entities.ResultSpecValues;
import com.google.common.collect.Maps;

import java.util.Map;

/**
 * Utility tools for common use.
 * Created by yaozy on 2017/12/29
 */
public class CommonUtils {

    /**
     * return result map whose values are set by the given specValue.
     * @param resultSpecValues specified value to set all available dimensions.
     * @return result with specified values
     */
    public static Map<String, Object> getSpecifiedResult(ResultSpecValues resultSpecValues) {

        Map<String, Object> results = Maps.newHashMap();

        Integer integerSpecValue  = resultSpecValues.getIntegerSpecValue() == null ?
            SystemConstant.EXCEPTION_RESULT : resultSpecValues.getIntegerSpecValue();
        Double doubleSpecValue = resultSpecValues.getDoubleSpecValue() == null ?
            SystemConstant.EXCEPTION_RESULT_DOUBLE_TYPE : resultSpecValues.getDoubleSpecValue();
        Integer msgCode = resultSpecValues.getMsgCode() == null ?
            SystemConstant.CODE_SUCCESS : resultSpecValues.getMsgCode();
        String msgValue = resultSpecValues.getMsgValue() == null ?
            SystemConstant.MSG_SUCCESS : resultSpecValues.getMsgValue();

        // 一度联系人个数
        results.put("1d_contact_num", integerSpecValue);
        // 一度联系人过去一个月在本平台申请的人数 所有渠道
        results.put("1d_apply_num", integerSpecValue);
        // 一度联系人过去一个月在本平台申请的人数 当前渠道
        results.put("1d_apply_num_current", integerSpecValue);
        // 一度联系人申请通过人的逾期人数
        results.put("1d_overdue_num", integerSpecValue);
        // 一度联系人中申请授信被拒绝人数 所有渠道
        results.put("1d_decline_num", integerSpecValue);
        // 一度联系人中申请授信被拒绝人数 当前渠道
        results.put("1d_decline_num_current", integerSpecValue);
        // 一度联系人中借款但未还款人数 所有渠道
        results.put("1d_unpaid_num", integerSpecValue);
        // 一度联系人中借款但未还款人数 当前渠道
        results.put("1d_unpaid_num_current", integerSpecValue);
        // 一度联系人中逾期金额
        results.put("1d_overdue_amount", doubleSpecValue);
        //一度关系有过续贷的人数
        results.put("1d_renew_loan_num",integerSpecValue);
        // 一度联系人中命中黑名单人数
        results.put("1d_hit_black_num", integerSpecValue);

        // 二度联系人个数
        results.put("2d_contact_num", integerSpecValue);
        // 二度联系人过去一个月在本平台申请的人数 所有渠道
        results.put("2d_apply_num", integerSpecValue);
        // 二度联系人过去一个月在本平台申请的人数 当前渠道
        results.put("2d_apply_num_current", integerSpecValue);
        // 二度联系人申请通过的人中有多少逾期的人数
        results.put("2d_overdue_num", integerSpecValue);
        // 二度联系人中申请授信被拒绝人数 所有渠道
        results.put("2d_decline_num", integerSpecValue);
        // 二度联系人中申请授信被拒绝人数 当前渠道
        results.put("2d_decline_num_current", integerSpecValue);
        // 二度联系人中借款但未还款人数 所有渠道
        results.put("2d_unpaid_num", integerSpecValue);
        // 二度联系人中借款但未还款人数 当前渠道
        results.put("2d_unpaid_num_current", integerSpecValue);
        // 二度联系人中逾期金额
        results.put("2d_overdue_amount", doubleSpecValue);
        //二度关系有过续贷的人数
        results.put("2d_renew_loan_num",integerSpecValue);
        // 二度联系人中命中黑名单人数
        results.put("2d_hit_black_num", integerSpecValue);

        // 团伙联系人个数
        results.put("0d_contact_num", integerSpecValue);
        // 团伙联系人过去一个月在本平台申请的人数 当前渠道
        results.put("0d_apply_num_current", integerSpecValue);
        // 团伙联系人申请通过的人中有多少逾期的人数 当前渠道
        results.put("0d_overdue_num_current", integerSpecValue);
        // 团伙联系人中申请授信被拒绝人数 当前渠道
        results.put("0d_decline_num_current", integerSpecValue);
        // 团伙联系人中借款但未还款人数 当前渠道
        results.put("0d_unpaid_num_current", integerSpecValue);
        // 团伙中电话号码对应的城市数量
        results.put("0d_mobile_address_num",  integerSpecValue);
        //团伙亲密度得分
        results.put("0d_intimacy_score",integerSpecValue);
        // 近90天同一个手机号码之前被拒绝授信次数
        results.put("mobile_decline_num",  integerSpecValue);
        // 用户填写的紧急联系人号码对应多少个申请人
        results.put("urgent_corresponding_applicant_num",  integerSpecValue);
        // 申请手机号码对应联系人姓名数量
        results.put("mobile_ corresponding_multi_name_num",  integerSpecValue);
        //紧急联系人处于还款中
        results.put("urgent_unpaid_num",integerSpecValue);

        results.put("scdata_code", msgCode);
        results.put("scdata_msg", msgValue);

        return results;
    }

    /**
     * return result map whose values of gang info are set by the given specValue.
     * @param resultSpecValues specified value to set all available dimensions.
     * @return
     */
    public static Map<String, Object> getSpecifiedODContactsInfo(ResultSpecValues resultSpecValues) {
        Map<String, Object> results = Maps.newHashMap();

        Integer integerSpecValue  = resultSpecValues.getIntegerSpecValue() == null ?
            SystemConstant.EXCEPTION_RESULT : resultSpecValues.getIntegerSpecValue();

        results.put("0d_contact_num", integerSpecValue);
        // 团伙联系人过去一个月在本平台申请的人数
        results.put("0d_apply_num_current", integerSpecValue);
        // 团伙联系人中逾期人数
        results.put("0d_overdue_num_current", integerSpecValue);
        // 团伙联系人中申请授信被拒绝人数
        results.put("0d_decline_num_current", integerSpecValue);
        // 团伙联系人中借款但未还款人数
        results.put("0d_unpaid_num_current", integerSpecValue);
        // 团伙中电话号码对应的城市数量
        results.put("0d_mobile_address_num", integerSpecValue);
        //团伙亲密度得分
        results.put("0d_intimacy_score", integerSpecValue);

        return results;
    }

}
