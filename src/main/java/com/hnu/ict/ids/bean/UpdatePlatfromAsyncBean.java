package com.hnu.ict.ids.bean;


import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

@Data
public class UpdatePlatfromAsyncBean {

    Integer stationId;//车站ID
    String stationName;//车站名称
    Integer stationType;//车站类型
    Integer stationRouteType;//站点线路类型
    String nextStaIds="";//最近下一个站点ID
    String nextStaDistances="";//最近下一个站点距离
    String staGps;//车站GPS
    float  rangeRadius;//车站范围半径
    String cityCod;


}
