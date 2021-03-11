package com.hnu.ict.ids.bean;


import lombok.Data;

@Data
public class CarTrave {
    int actual;

    int carType; //车辆类型

    String carStatus; //车辆状态

    String endStationName;  //终点站名称

    String carNo;//车牌号

    int nuclear;//最大载客量

    String startStationName;//起点站名称
}
