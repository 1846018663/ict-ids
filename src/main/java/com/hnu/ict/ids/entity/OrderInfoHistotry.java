package com.hnu.ict.ids.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigInteger;
import java.util.Date;


@Data
@TableName(value = "order_info_histotry")
public class OrderInfoHistotry {
    /** 主键ID **/
    @TableId(type = IdType.AUTO)
    private BigInteger id;


    /**
     * 订单来源
     */
    private String orderSource;


    /**
     * 原始订单id 如乘客服务系统订单id
     */
    private String sourceOrderId;


    /**
     * 本系统订单号
     */
    private String orderNo;


    /**
     * 出发站id
     */
    private int beginStationId;


    /**
     * 终点站id
     */
    private int endStationId;


    /**
     * 车票数量
     */
    private int ticketNumber;


    /**
     * 购票用户id
     */
    private BigInteger buyUid;


    /**
     *开始时间
     */

    @JsonFormat(pattern="yyyy-MM-dd HH:mm")
    private Date startTime;


    /**
     * 行程id
     */
    private BigInteger travelId;


    /**
     * 行程来源   0 算法   1 乘客服务
     */
    private int travelSource;
    /**
     * 创建时间
     */
    @JsonFormat(pattern="yyyy-MM-dd HH:mm")
    private Date createTime;




    @Override
    public String toString() {
        return "OrderInfo{" +
                "id=" + id +
                ", orderSurce='" + orderSource + '\'' +
                ", sourceOrderId='" + sourceOrderId + '\'' +
                ", orderNo='" + orderNo + '\'' +
                ", beginStationId=" + beginStationId +
                ", endStationId=" + endStationId +
                ", ticketNumber=" + ticketNumber +
                ", buyUid=" + buyUid +
                ", startTime=" + startTime +
                ", travelId=" + travelId +
                ", createTime=" + createTime +
                '}';
    }
}