package com.ambergarden.samples.neo4j.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by yaozy on 2017/12/29
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResultSpecValues {

    // body
    Integer integerSpecValue;
    Double doubleSpecValue;

    // msg
    Integer msgCode;
    String msgValue;
}
