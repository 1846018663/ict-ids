package com.hnu.ict.ids.bean;


import lombok.Data;

@Data
public class TdDriverAttendance {
    Long vehicleId;
    String plateNo;
    Long driverId;
    String driverEmpNo;
    Integer checkType;
    String time;


}
