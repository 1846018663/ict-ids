package com.hnu.ict.ids.bean;

import lombok.Data;

@Data
public class CarTypeTotalFrom {

    int offTotal;//离线总数

    int onTotal;//在线总数

    int type;//车辆类型

    int onCarStatusCount;//总数

    int onIdlerStatusCount;//荷载

    int offIdlerStatusCount;//空载总数
}
