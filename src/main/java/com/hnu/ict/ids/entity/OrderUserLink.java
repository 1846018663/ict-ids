package com.hnu.ict.ids.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigInteger;

@Data
@TableName(value = "order_user_link") //订单用户信息拓展表
public class OrderUserLink {

    /** 主键ID **/
    @TableId(type = IdType.AUTO)
    private BigInteger id;


  private BigInteger userId;


  private String orderNo;

  private int state;


}
