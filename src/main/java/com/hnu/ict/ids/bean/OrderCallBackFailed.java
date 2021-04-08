package com.hnu.ict.ids.bean;

import lombok.Data;

@Data
public class OrderCallBackFailed {
    String o_id;
    String time;
    Integer retry_count;
    String code;
    String message;

}
