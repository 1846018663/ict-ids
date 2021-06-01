package com.hnu.ict.ids.bean;

import lombok.Data;


@Data
public class TraveOrderTaskResult {
    Integer	itNumber;//乘车总人数
    String carId;//车辆ID
    String	fromId;//起始站点ID
    String	fromName;//起始站点名称
    String toId;//目的站点ID
    String	toName;//目的站点名称
    String parkId;//中间停站站点ID
    String parkName;//中间停站站点名称
    String	startTime;//行程开始时间
    String	correspondOrderId;//调度任务对应的订单编号
    String	correspondNumber;//每个订单对应上车人数
    String	travelId;//调度命令ID
    String	driverId;//司机ID
    String	driverContent;//司机提示信息
    String	travelPlat;//车辆行驶路径列表
    Integer	expectedTime;//全程预计时长
    String	arriveTime;//预计到站时间
    Integer	distance;//车辆预计行驶距离
    String	modifyOrderId;//是否修改命令
    String	warning;//警告
}
