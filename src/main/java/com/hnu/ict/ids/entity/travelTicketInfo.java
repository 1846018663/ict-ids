package com.hnu.ict.ids.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;

import java.math.BigInteger;

public class travelTicketInfo {

    /** 主键ID **/
    @TableId(type = IdType.AUTO)
    private BigInteger id;

    /**
     * 行程id
     */
    private BigInteger travelId;


    /**
     * 乘车用户id
     */
    private BigInteger userId;


    /**
     * 座位编号
     */
    private Character seatNum;

}
