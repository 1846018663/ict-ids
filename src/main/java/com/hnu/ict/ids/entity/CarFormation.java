package com.hnu.ict.ids.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName(value = "car_formation")//车辆信息
public class CarFormation {

    /** 主键ID **/
    @TableId(type = IdType.AUTO)
    Integer id;

    String fId;

    Integer fromPId;

    Date startTime;

    String carList;

    Date createTime;

}
