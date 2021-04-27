package com.hnu.ict.ids.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName(value = "td_car_outbound_info") //行程信息表
public class TdCarOutboundInfo {

    /** 主键ID **/
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 车辆id
     */
    private String vehicleId;


    /**
     * 车牌号
     */
    private String plateNo;


    /**
     * 行程id
     */
    private String tripNo;

    /**
     * 经度
     */
    private Double lat;

    /**
     * 纬度
     */
    private Double lng;

    /**
     * 海拔
     */
    private Integer altitude;


    /**
     * 速度  km/h
     */
    private Double velocity;

    /**
     * 方向，0-359，正北为 0，顺时针
     */
    private Integer direction;

    /**
     * 站台编号
     */
    private String stationNo;

    /**
     * 站台序列号
     */
    private Integer stationSn;


    /**
     * 站点类型，1-干线站，2-支线站，3-虚拟站，4-其它
     */
    private Integer stationType;


    /**
     * 到离站类型，1：到站；2：离站
     */
    private Integer type;


    /**
     * 时间
     */
    private Date time;

    /**
     *通知状态 1通知成功，2通知失败
     */
    private Integer pushStatus;

    /**
     * 通知时间
     */
    private Date pushTime;


    /**
     * 系统创建时间
     */
    private Date createTime;

    /**
     * 系统修改时间
     */
    private Date updateTime;
}
