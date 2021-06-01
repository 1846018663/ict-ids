package com.hnu.ict.ids.bean;


import lombok.Data;

import java.util.List;

@Data
public class SeatBeanRequest {
    String	travelId;//	调度命令ID
    String	modifyOrderId;//	对应的旧的调度命令ID
    String	carId;//	车辆ID
    String	correspondOrderId;//	调度任务对应的订单编号
    String	correspondNumber;//	每个订单对应的上车人数
    List<SeatUserRequset>	orderUserId;//	各订单包含的所有乘客的uid
    List<SeatPreferenceRequset>	userPreference;//	订单包含的乘客及其座位偏好信息
    Integer	carType;//	是否包车


}
