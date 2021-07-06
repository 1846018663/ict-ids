package com.hnu.ict.ids.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName(value = "td_data_call_back")//车辆信息
public class DataCallBackResult {

    /** 主键ID **/
    @TableId(type = IdType.AUTO)
    Integer id;
    String tripNo;
    Long  vehicleId;
    String plateNo;
    int status;
    String msg;
    Date time;

}
