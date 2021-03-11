package com.hnu.ict.ids.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;


@Data
@TableName(value = "ivs_app_car_info")//车辆信息
public class IvsAppCarInfo {

    /** 主键ID **/
    @TableId(type = IdType.AUTO)
    private int id;


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



}
