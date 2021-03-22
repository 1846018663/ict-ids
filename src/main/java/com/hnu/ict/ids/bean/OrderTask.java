package com.hnu.ict.ids.bean;

import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;


@Data
public class OrderTask {

    @JSONField(ordinal=1)
    int o_id;//订单id
    @JSONField(ordinal=2)
    int from_p_id;//出发站
    @JSONField(ordinal=3)
    int to_p_id;//总点站
    @JSONField(ordinal=4)
    String start_time;//出发时间
    @JSONField(ordinal=5)
    int ticket_number;//车票数量
    @JSONField(ordinal=6)
    String order_time;//下单时间
    @JSONField(ordinal=7)
    Integer set_time;



}
