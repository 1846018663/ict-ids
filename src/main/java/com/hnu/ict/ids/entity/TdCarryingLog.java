package com.hnu.ict.ids.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName(value = "td_carrying_log")//车辆信息
public class TdCarryingLog {

    /** 主键ID **/
    @TableId(type = IdType.AUTO)
    Integer id;
    String sourceOrderId;
    String algorithmCode;
    String algorithmMessage;
    String algorithmTime;
    String consumerRequset;
    String consumerResult;
    String createTime;
    String algorithmRequset;
    String consumerCode;




}
