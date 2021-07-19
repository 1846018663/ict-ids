package com.hnu.ict.ids.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.models.auth.In;
import lombok.Data;

import java.math.BigInteger;
import java.util.Date;


@Data
@TableName(value = "order_info")
public class OrderInfo {
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
    private Integer beginStationId;


    /**
     * 终点站id
     */
    private Integer endStationId;


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

    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Date startTime;


    /**
     * 行程id
     */
    private String travelId;


    /**
     * 行程来源   0 算法   1 乘客服务
     */
    private Integer travelSource;
    /**
     * 创建时间
     */
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Date createTime;


    /**
     * 同步状态  0未同步  1已同步   2同步失败
     */
    private Integer status;


    /**
     * 推送乘客服务系统次数
     */
    private Integer pushNumber;


    /**
     * 推送状态  1成功  2失败
     */
    private Integer pushStatus;

    /**
     * 运力检测说明
     */
    private String message;


    /**
     * 包车类型
     * 1：车辆5座，
     * 2：车辆15座，3：车辆23座
     */
    private String charteredBus;


    /**
     * 订单状态
     * 1、创建订单
     * 2、运力检测预约成功
     * 3、运力检测预约失败
     * 4、新增行程成功
     * 5、上报大数据
     * 6、下发车载终端成功
     * 7、下发车载终端失败
     * 8、变更指派车辆上报大数据
     * 9、上报乘客服务系统
     * 10、乘客服务系统接收成功
     * 11、乘客服务系统接收失败
     * 12、取消订单
     */
    private int orderStatus;


    private  Integer direction;

    private Integer timeNumber;

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
