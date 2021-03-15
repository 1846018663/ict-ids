package com.hnu.ict.ids.bean;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;


@Data
public class OrderTask {
    int o_id;//订单id

    int from_p_id;//出发站

    int to_p_id;//总点站

    String start_time;//出发时间

    String order_time;//下单时间

    int ticet_number;//车票数量
}
