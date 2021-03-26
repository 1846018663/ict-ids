package com.hnu.ict.ids.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName(value = "distance_plan")//车辆信息
public class DistancePlan {

    /** 主键ID **/
    @TableId(type = IdType.AUTO)
    Integer id;

    Integer startId;

    Integer endId;

    Double distance;

    String allTravelPlat;



}
