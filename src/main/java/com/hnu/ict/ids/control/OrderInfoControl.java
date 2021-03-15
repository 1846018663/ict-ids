package com.hnu.ict.ids.control;


import com.hnu.common.respone.PojoBaseResponse;
import com.hnu.ict.ids.entity.OrderInfo;
import com.hnu.ict.ids.entity.TravelInfo;
import com.hnu.ict.ids.exception.ResultEntity;
import com.hnu.ict.ids.exception.ResutlMessage;
import com.hnu.ict.ids.service.OrderInfoService;
import com.hnu.ict.ids.service.TravelInfoService;
import com.hnu.ict.ids.utils.DateUtil;
import com.hnu.ict.ids.utils.ParamsNotNull;
import com.hnu.ict.ids.utils.UtilConf;
import com.mysql.cj.protocol.x.ResultMessageListener;
import io.swagger.annotations.Api;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.math.BigInteger;
import java.util.Date;
import java.util.List;

@Api(tags = "订单行程API")
@RestController
@RequestMapping("/order")
public class OrderInfoControl {

    Logger logger= LoggerFactory.getLogger(OrderInfoControl.class);

    @Resource
    OrderInfoService orderInfoService;

    @Autowired
    TravelInfoService travelInfoService;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private RedisTemplate redisTemplate;


    @RequestMapping(value="/add" , method = RequestMethod.POST)
    @ParamsNotNull(str = "sourceOrderId,beginStationId,endStationId,ticketNumber,buyUid,startTime,uIds,createTime")
    public PojoBaseResponse addOrder(String sourceOrderId, int beginStationId,
                                     int endStationId, int ticketNumber, BigInteger buyUid,
                                     Long startTime, String uIds, Long createTime){
        PojoBaseResponse result=new PojoBaseResponse();
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
        order.setOrderSource("乘客服务系统");


        //操作数据库
        try {
            orderInfoService.insertOrder(order);
            result.setErrorCode(ResutlMessage.SUCCESS.getName());
            result.setErrorMessage(ResutlMessage.SUCCESS.getValue());
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            result.setErrorCode(ResutlMessage.FAIL.getName());
            result.setErrorMessage(ResutlMessage.FAIL.getValue());
            return result;
        }



    }


    @RequestMapping(value="/deleteOrderNo" ,method = RequestMethod.POST)
    @ParamsNotNull(str = "sourceOrderId")
    public PojoBaseResponse deleteOrderNo(String sourceOrderId ) {
        PojoBaseResponse result = new PojoBaseResponse();
        int i = orderInfoService.deleteSourceOrderId(sourceOrderId);
        if (i > 0) {
            result.setErrorCode(ResutlMessage.SUCCESS.getName());
            result.setErrorMessage(ResutlMessage.SUCCESS.getValue());
        } else {
            result.setErrorCode(ResutlMessage.FAIL.getName());
            result.setErrorMessage(ResutlMessage.FAIL.getValue());
            return result;
        }
        return result;
    }


    @RequestMapping(value="/addExistence" ,method = RequestMethod.POST)
    @ParamsNotNull(str = "sourceOrderId,travelId,ticketNumber,buyUid,uIds,createTime")
    public PojoBaseResponse addExistence( String sourceOrderId,int ticketNumber,Long travelId, BigInteger buyUid,
                                 String uIds, Long createTime){
        PojoBaseResponse result=new PojoBaseResponse();

        //根据行程id查询订单信息   并创建订单数据
       TravelInfo travelInfo= travelInfoService.getById(BigInteger.valueOf(travelId));

        //创建数据对象
        OrderInfo order=new OrderInfo();
        order.setSourceOrderId(sourceOrderId);
        order.setBeginStationId(travelInfo.getBeginStationId());
        order.setEndStationId(travelInfo.getEndStationId());
        order.setTicketNumber(ticketNumber);
        order.setBuyUid(buyUid);
        order.setStartTime(travelInfo.getStartTime());
        order.setCreateTime(DateUtil.millisecondToDate(createTime));
        order.setOrderNo(UtilConf.getUUID());
        order.setOrderSource("乘客服务系统");


        //操作数据库
        try {
            orderInfoService.insertOrder(order);

            result.setErrorCode(ResutlMessage.SUCCESS.getName());
            result.setErrorMessage(ResutlMessage.SUCCESS.getValue());
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            result.setErrorCode(ResutlMessage.FAIL.getName());
            result.setErrorMessage(ResutlMessage.FAIL.getValue());
            return result;
        }



    }



    @RequestMapping(value = "/findNotTrave" ,method = RequestMethod.GET)
    public PojoBaseResponse findNotTrave() {
        PojoBaseResponse result = new PojoBaseResponse();
        //第一步查询订单  查询没有行程id  且当前 开始时间大约30分钟内的订单
        Date stateDate=new Date();
        //30分钟毫秒
        long time=1000*60*30+stateDate.getTime();
        Date endDate=DateUtil.millisecondToDate(time);
        ;

        logger.info("开始时间"+DateUtil.getCurrentTime(stateDate)+"结束时间"+DateUtil.getCurrentTime(endDate));
        List<OrderInfo>  listOrder=orderInfoService.findNotTrave(DateUtil.getCurrentTime(stateDate),DateUtil.getCurrentTime(endDate));

        result.setData(listOrder);
        return result;
    }


}
