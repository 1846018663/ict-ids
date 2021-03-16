package com.hnu.ict.ids.control;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.hnu.common.respone.PojoBaseResponse;
import com.hnu.ict.ids.entity.IvsAppCarInfo;
import com.hnu.ict.ids.entity.OrderInfo;
import com.hnu.ict.ids.entity.OrderInfoHistotry;
import com.hnu.ict.ids.entity.TravelInfo;
import com.hnu.ict.ids.exception.ResultEntity;
import com.hnu.ict.ids.exception.ResutlMessage;
import com.hnu.ict.ids.service.IvsAppCarInfoService;
import com.hnu.ict.ids.service.OrderInfoService;
import com.hnu.ict.ids.service.TravelInfoService;
import com.hnu.ict.ids.utils.DateUtil;
import com.hnu.ict.ids.utils.ParamsNotNull;
import com.hnu.ict.ids.utils.UtilConf;
import com.hnu.ict.ids.webHttp.CustomerWebAPI;
import com.mysql.cj.protocol.x.ResultMessageListener;
import io.swagger.annotations.Api;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
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

    @Value("${travel.algorithm.url}")
    private String URL;

    @Resource
    OrderInfoService orderInfoService;

    @Autowired
    TravelInfoService travelInfoService;

    @Autowired
    IvsAppCarInfoService ivsAppCarInfoService;

    @RequestMapping(value="/add" , method = RequestMethod.POST)
    public ResultEntity addOrder(@RequestBody  String body){
        ResultEntity result=new ResultEntity();
        logger.info(body);
        JSONObject json=JSONObject.parseObject(body);
        //创建数据对象
        OrderInfo order=new OrderInfo();
        order.setSourceOrderId(json.getString("o_id"));
        order.setBeginStationId(json.getInteger("from_p_id"));
        order.setEndStationId(json.getInteger("to_p_id"));
        order.setTicketNumber(json.getInteger("ticket_number"));
        order.setBuyUid(json.getBigInteger("buy_uid"));
        order.setStartTime(DateUtil.strToDateyyyyMMddHHmmss(json.getString("start_time")));
        order.setCreateTime(DateUtil.strToDateyyyyMMddHHmmss(json.getString("create_time")));
        order.setOrderNo(UtilConf.getUUID());
        order.setOrderSource("乘客服务系统");


        //操作数据库
        try {
            orderInfoService.insertOrder(order);

            result.setCode(ResutlMessage.SUCCESS.getName());
            result.setMessage(ResutlMessage.SUCCESS.getValue());
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return result;
        }



    }


    @RequestMapping(value="/deleteOrderNo" ,method = RequestMethod.POST)
    public ResultEntity deleteOrderNo(@RequestBody  String body) {
        ResultEntity result = new ResultEntity();
        JSONObject json=JSONObject.parseObject(body);
        Long o_id=json.getLong("o_id");
        String travel_id=json.getString("travel_id");
        if(StringUtils.hasText(o_id.toString())){
            OrderInfo order=orderInfoService.getBySourceOrderId(o_id.toString());
            OrderInfoHistotry orderInfoHistotry=new OrderInfoHistotry();
            BeanUtils.copyProperties(order,orderInfoHistotry);
            orderInfoHistotry.setId(null);
            orderInfoService.deleteSourceOrderId(o_id.toString(),orderInfoHistotry);

        }


        result.setCode(ResutlMessage.SUCCESS.getName());
        result.setMessage(ResutlMessage.SUCCESS.getValue());
        return result;
    }


    @RequestMapping(value="/addExistence" ,method = RequestMethod.POST)
    public ResultEntity addExistence(@RequestBody  String body){
        ResultEntity result=new ResultEntity();
        JSONObject json=JSONObject.parseObject(body);

        //判断数据操作类型   是添加  还是移除
        int oper_type=json.getInteger("oper_type");
        String travel_id=json.getString("travel_id");
        //根据行程id查询订单信息   并创建订单数据
        TravelInfo travelInfo= travelInfoService.getById(new BigInteger(travel_id));
        if(oper_type==1){//添加已有行程
            //判断车辆是否满载  根据已有行程关联所有订单   查询总票数
            int  sum=orderInfoService.getByTravelCount(new BigInteger(travel_id));
            //根据id查询车辆
            IvsAppCarInfo car=ivsAppCarInfoService.getByCarId(travelInfo.getCarId());

            int ticketNumber=json.getInteger("ticket_number");
            sum=sum+ticketNumber;
            if(sum<=car.getCarSeatNumber()){//可以载入
                //创建数据对象
                OrderInfo order=new OrderInfo();
                order.setSourceOrderId(json.getLong("o_id").toString());
                order.setBeginStationId(travelInfo.getBeginStationId());
                order.setEndStationId(travelInfo.getEndStationId());
                order.setTicketNumber(json.getInteger("ticket_number"));
                order.setBuyUid(json.getBigInteger("buy_uid"));
                order.setStartTime(travelInfo.getStartTime());
                order.setCreateTime(DateUtil.strToDateyyyyMMddHHmmss(json.getString("create_time")));
                order.setOrderNo(UtilConf.getUUID());
                order.setOrderSource("乘客服务系统");
                order.setTravelSource(1);//乘客服务系统来源
                orderInfoService.insertOrder(order);
                result.setCode(ResutlMessage.SUCCESS.getName());
                result.setMessage(ResutlMessage.SUCCESS.getValue());
                return result;
            }else{
                result.setCode(ResutlMessage.FAIL.getName());
                result.setMessage("本次行程车载人数以上限");
                return result;

            }
        }


        if(oper_type==2){//移除已有行程
            Long o_id=json.getLong("o_id");
            OrderInfo order=orderInfoService.getBySourceOrderId(o_id.toString());
            OrderInfoHistotry orderInfoHistotry=new OrderInfoHistotry();
            BeanUtils.copyProperties(order,orderInfoHistotry);
            orderInfoHistotry.setId(null);
            orderInfoService.deleteSourceOrderId(o_id.toString(),orderInfoHistotry);
            result.setCode(ResutlMessage.SUCCESS.getName());
            result.setMessage(ResutlMessage.SUCCESS.getValue());
            return  result;
        }



        result.setCode(ResutlMessage.FAIL.getName());
        result.setMessage(ResutlMessage.FAIL.getValue());
        return  result;

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


        String json= JSON.toJSONString(listOrder);
        System.out.println("发送请求数据"+json);



        try {
            CustomerWebAPI customerWebAPI=new CustomerWebAPI();
            String body = customerWebAPI.doPost(URL,json);
            System.out.println("接收数据"+body);

            //占时先不返回   这里做行程数据解析  存储操作
        } catch (Exception e) {
            e.printStackTrace();
        }

        result.setData(listOrder);
        return result;
    }


}
