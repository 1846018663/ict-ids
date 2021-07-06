package com.hnu.ict.ids.bean;

import lombok.Data;

@Data
public class CarStatusUpRequset {

    String cityType	;//	城市类型
    String carId;//	车辆ID
    Integer carType;//	车辆类型
    Integer carSeatNumber;//	车辆座位数
    String carLoc;//	车辆GPS数据
    Integer carStatus;//	车辆状态
    String driverId;

}
