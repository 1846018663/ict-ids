package com.hnu.ict.ids.control;


import com.hnu.ict.ids.entity.OrderInfo;
import com.hnu.ict.ids.entity.ResultEntity;
import com.hnu.ict.ids.service.OrderInfoService;
import com.hnu.ict.ids.utils.DateUtil;
import com.hnu.ict.ids.utils.ParamsNotNull;
import com.hnu.ict.ids.utils.UtilConf;
import io.swagger.annotations.Api;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigInteger;

@Api(tags = "订单API")
@RestController
@RequestMapping("/order")
public class OrderInfoControl {

    Logger logger= LoggerFactory.getLogger(OrderInfoControl.class);

    @Autowired
    OrderInfoService orderInfoService;


    @ResponseBody
    @RequestMapping("/add")
    @ParamsNotNull(str = "sourceOrderId,beginStationId,endStationId,ticketNumber,buyUid,startTime,uIds,createTime")
    public ResultEntity addOrder( String sourceOrderId, int beginStationId,
                                 int endStationId, int ticketNumber, BigInteger buyUid,
                                  Long startTime, String uIds, Long createTime){
        ResultEntity resultEntity=new ResultEntity();
        //创建数据对象
        OrderInfo order=new OrderInfo();
        order.setSourceOrderId(sourceOrderId);
        order.setBeginStationId(beginStationId);
        order.setEndStationId(endStationId);
        order.setTicketNumber(ticketNumber);
        order.setBuyUid(buyUid);
        order.setStartTime(DateUtil.millisecondToDate(startTime));
        order.setCreateTime(DateUtil.millisecondToDate(createTime));
        order.setOrderNo(UtilConf.getUUID());
        order.setOrderSurce("乘客服务系统");
        //操作数据库


        try {
            orderInfoService.insertOrder(order);
            resultEntity.setCode(ResultEntity.Type.SUCCESS.value());
            resultEntity.setMessage("操作成功");
            return resultEntity;
        } catch (Exception e) {
            e.printStackTrace();
            resultEntity.setCode(ResultEntity.Type.FAIL.value());
            resultEntity.setMessage("操作失败");
            return resultEntity;
        }



    }


    @ResponseBody
    @RequestMapping("/deleteOrderNo")
    @ParamsNotNull(str = "sourceOrderId")
    public ResultEntity deleteOrderNo(String sourceOrderId ) {
        ResultEntity result = new ResultEntity();
        int i = orderInfoService.deleteSourceOrderId(sourceOrderId);
        if (i > 0) {
            result.setCode(ResultEntity.Type.SUCCESS.value());
            result.setMessage("操作成功");
        } else {
            result.setCode(ResultEntity.Type.FAIL.value());
            result.setMessage("操作失败");
            return result;
        }
        return result;
    }


}
