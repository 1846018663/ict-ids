package com.hnu.ict.ids.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;


@Data
@TableName(value = "network_log")//车辆信息
public class NetworkLog {
    /** 主键ID **/
    @TableId(type = IdType.AUTO)
    private int id;

    private String type;

    private String method;

    private String url;

    private String accessContent;

    private String responseResult;

    private String status;

    private String interfaceInfo;

    private Date createTime;


}
