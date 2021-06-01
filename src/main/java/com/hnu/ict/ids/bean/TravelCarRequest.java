package com.hnu.ict.ids.bean;

import lombok.Data;

/*
车辆信息更新  算法同步数据对象类
 */
@Data
public class TravelCarRequest {
    Integer cityType;//城市类型  城市编码
    String carId;//车辆id
    Integer carType;//车辆类型
    Integer carSeatNumber;//车辆座位数
    Integer carOperatonStatus;


}
