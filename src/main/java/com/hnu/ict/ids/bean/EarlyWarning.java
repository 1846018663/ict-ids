package com.hnu.ict.ids.bean;

import lombok.Data;

@Data
public class EarlyWarning {
    String app_id;
    String title;
    String ocassion;
    OrderCallBackFailed params;
    String phones;
    String biz_id;
    String extra;

}
