package com.hnu.ict.ids.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigInteger;


@Data
@TableName(value = "travel_ticket_info")
public class TravelTicketInfo {

    /** 主键ID **/
    @TableId(type = IdType.AUTO)
    private BigInteger id;

    /**
     * 行程id
     */
    private String travelId;


    /**
     * 乘车用户id
     */
    private Integer userId;


    /**
     * 座位编号
     */
    private String seatNum;

}
