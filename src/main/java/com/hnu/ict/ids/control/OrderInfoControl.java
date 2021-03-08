package com.hnu.ict.ids.control;


import com.hnu.ict.ids.entity.OrderInfo;
import com.hnu.ict.ids.exception.ResultEntity;
import com.hnu.ict.ids.exception.ResutlMessage;
import com.hnu.ict.ids.service.OrderInfoService;
import com.hnu.ict.ids.utils.DateUtil;
import com.hnu.ict.ids.utils.ParamsNotNull;
import com.hnu.ict.ids.utils.UtilConf;
import com.mysql.cj.protocol.x.ResultMessageListener;
import io.swagger.annotations.Api;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.math.BigInteger;

@Api(tags = "订单行程API")
@RestController
@RequestMapping("/order")
public class OrderInfoControl {

    Logger logger= LoggerFactory.getLogger(OrderInfoControl.class);

    @Resource
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
            resultEntity.setCode(ResutlMessage.SUCCESS.getName());
            resultEntity.setMessage(ResutlMessage.SUCCESS.getValue());
            return resultEntity;
        } catch (Exception e) {
            e.printStackTrace();
            resultEntity.setCode(ResutlMessage.FAIL.getName());
            resultEntity.setMessage(ResutlMessage.FAIL.getValue());
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
            result.setCode(ResutlMessage.SUCCESS.getName());
            result.setMessage(ResutlMessage.SUCCESS.getValue());
        } else {
            result.setCode(ResutlMessage.FAIL.getName());
            result.setMessage(ResutlMessage.FAIL.getValue());
            return result;
        }
        return result;
    }



    @ResponseBody
    @RequestMapping("/findOrderNo")
    @ParamsNotNull(str = "sourceOrderId")
    public ResultEntity findOrderNo(String sourceOrderId ) {
        ResultEntity result = new ResultEntity();
        result.setCode(ResutlMessage.SUCCESS.getName());
        OrderInfo order=orderInfoService.getBySourceOrderId(sourceOrderId);
        logger.info("测试数据"+order.toString());
        //测试
        return result;
    }


}
