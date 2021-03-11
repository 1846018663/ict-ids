package com.hnu.ict.ids.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import javax.validation.constraints.DecimalMin;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Base64;
import java.util.Date;

@Data
@TableName(value = "travel_info") //行程信息表
public class TravelInfo {

    /** 主键ID **/
    @TableId(type = IdType.AUTO)
    private BigInteger id;


    /**
     * 源行程id，如乘客服务系统行程id
     */
    private String sourceTravelId;


    /**
     * 出发站点id
     */
    private int  beginStationId;


    /**
     * 终点站点id
     */
    private int endStationId;


    /**
     * 行程状态  0-预约中；1-预约成功；4-预约失败；9-取消预约
     */
    private int travelStatus;


    /**
     * 行程开始时间
     */
    @JsonFormat(pattern="yyyy-MM-dd HH:mm")
    private Date startTime;


    /**
     * 行程距离
     */
    private BigDecimal distance;


    /**
     * 全程预计时长
     */
    private String expectedTime;


    /**
     * 站点行程路线
     */
    private String allTravelPlat;



    /**
     * 司机提示信息
     */
    private String driverContent;


    /**
     * 车辆id
     */
    private int carId;


    /**
     * 司机id
     */
    private int driverId;


    /**
     * 行程创建时间
     */
    @JsonFormat(pattern="yyyy-MM-dd HH:mm")
    private Date createTime;


    /**
     * 行程更新时间
     */
    @JsonFormat(pattern="yyyy-MM-dd HH:mm")
    private Date updateTime;




}
