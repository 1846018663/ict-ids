package com.hnu.ict.ids.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;


@Data
@TableName(value = "ivs_app_car_info")//车辆信息
public class IvsAppCarInfo {

    /** 主键ID **/
    @TableId(type = IdType.INPUT)
    private int cId;


    /**
     * 车辆类型
     */
    private int carType;


    /**
     * 车辆最大载客量
     */
    private int carSeatNumber;


    /**
     * 车牌号
     */
    private String licenseNumber;


    /**
     * 城市编码
     */
    private String cCode;


    /**
     * 车辆状态  0=不可用,1=可用
     */
    private String cStatus;


    private Date lastMaintenanceTime;

    private Date lastAnnualInspection;

    //是否在线(1在线 2离线)
    private String cIsuse;

    //修改时间
    private Date cIsuseDate;

    //司机ID
    Long driverId;
    //司机工号
    String driverEmpNo;
    //类型，1-登入   2-登出
    Integer checkType;
    //时间，登入时间
    String time;
}
