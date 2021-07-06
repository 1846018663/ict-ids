package com.hnu.ict.ids.bean;

import lombok.Data;

@Data
public class EditScheduleRequset {
    String travelId;
    Integer inNumber;
    String carId;
    String fromId;
    String toId;
    String startTime;


}
