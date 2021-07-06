package com.hnu.ict.ids.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName(value = "td_driver_attendance_log")//车辆信息
public class TdDriverAttendanceLog {

    /** 主键ID **/
    @TableId(type = IdType.AUTO)
    Integer id;
    String vehicleId;
    String plateNo;
    String driverId;
    String driverEmpNo;
    String  checkType;
    String attTime;
    String createTime;





}
