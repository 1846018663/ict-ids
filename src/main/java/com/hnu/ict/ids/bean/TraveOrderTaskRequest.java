package com.hnu.ict.ids.bean;

import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;


@Data
public class TraveOrderTaskRequest {
    String oId;
    String fromId;
    String toId;
    String startTime;
    Integer ticketNumber;
    Integer orderStatus;
    String orderTime;
    Integer setTime;

}
