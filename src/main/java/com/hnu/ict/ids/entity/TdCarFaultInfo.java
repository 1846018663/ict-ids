package com.hnu.ict.ids.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName(value = "td_car_fault_info")
public class TdCarFaultInfo {

    /** 主键ID **/
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 车辆id
     */
   private String vehicleId;


    /**
     * 车牌
     */
   private String plateNo;

    /**
     * 请求id
     */
   private String faultCode;


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
