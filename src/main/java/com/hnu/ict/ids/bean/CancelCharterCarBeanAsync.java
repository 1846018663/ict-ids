package com.hnu.ict.ids.bean;

import lombok.Data;

@Data
public class CancelCharterCarBeanAsync {
    String o_id;//订单唯一表示ID
    String from_p_id;//出发站ID
    String to_p_id;//到达站ID
    String start_time; //出发时刻
    String chartered_bus;//车号（-1为不包车，其他为包车定下的车号）
    String it_number;//`订单人数
}
