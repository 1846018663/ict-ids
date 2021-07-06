package com.hnu.ict.ids.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;

@Data
@TableName(value = "travel_info_log") //行程信息表
public class TravelInfoLog {

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
    private Integer  beginStationId;


    /**
     * 终点站点id
     */
    private Integer endStationId;


    /**
     * 行程状态  0-预约中；1-预约成功；4-预约失败；9-取消预约
     */
    private int travelStatus;

    /**
     * 行程状态  0-预约中；1-预约成功；4-预约失败；9-取消预约
     */
    private String travelStatusName;


    /**
     * 行程开始时间
     */
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
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
    private Integer carId;


    /**
     * 司机id
     */
    private Integer driverId;


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

    /**
     * 行程id
     */
    private String  travelId;

    /**
     * 乘车人数
     */
    private Integer itNumber;


    /**
     * 旧的行程id
     */
    private String modifyId;


    /**
     * 出发站台名称
     */
    private String beginStationName;


    /**
     * 出发站台名称
     */
    private String endStationName;

    /**
     * 中间停靠站台
     */
    private  Integer parkId;


    /**
     * 中间停靠站台名称
     */
    private String parkName;


    /**
     * 警告
     */
    private String warning;


    /**
     * 推送状态 0未推送  1推送成功   2推送失败
     */
    private Integer pushStatus;




    private String correspondOrderNumber;


    private String correspondOrderId;


    private String CCode;


    private String arriveTime;

    private String modifyOrderId;


}
