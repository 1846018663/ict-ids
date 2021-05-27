package com.hnu.ict.ids.bean;

import lombok.Data;

@Data
public class OrderInfoResult {
    Integer inNumber;
    String carId;
    String fromId;
    String fromName;
    String StringtoId;
    String toName;
    Integer parkId;
    String parkName;
    String startTime;
    String correspondOrderId;
    String correspondNumber;
    String travelId;
    Integer driverId;
    String driverContent;
    String travelPlat;
    Integer expectedTime;
    String arriveTime;
    Integer distance;
    String modifyOrderId;
    String warning;

}
