package com.hnu.ict.ids.control;


import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hnu.ict.ids.config.UtilConf;
import com.hnu.ict.ids.entity.OrderInfo;
import com.hnu.ict.ids.exception.ResultEntity;
import com.hnu.ict.ids.exception.ResutlMessage;
import com.hnu.ict.ids.service.OrderInfoService;
import com.hnu.ict.ids.utils.DateUtil;
import io.swagger.annotations.Api;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "包车业务API")
@RestController
@RequestMapping("/schedule")
public class ScheduleCarControl {
    Logger logger= LoggerFactory.getLogger(ScheduleCarControl.class);

    @Autowired
    OrderInfoService orderInfoService;

    @RequestMapping(value="/add" , method = RequestMethod.POST)
    public ResultEntity addOrder(@RequestBody String body){
        ResultEntity result=new ResultEntity();
        JSONObject json=JSONObject.parseObject(body);
        //验证o_id    SourceOrderId是否已经存在
        String oId=json.getString("o_id");

        OrderInfo orderInfo= orderInfoService.getBySourceOrderId(oId);

        if(orderInfo!=null){
            result.setCode(ResutlMessage.FAIL.getName());
            result.setMessage("该订单已经存在，请勿重复提交");
            logger.info("新增包车订单"+result.toString());
            return result;
        }
        //创建数据对象
        OrderInfo order=new OrderInfo();
        order.setSourceOrderId(oId);
        order.setBeginStationId(json.getInteger("from_p_id"));
        order.setEndStationId(json.getInteger("to_p_id"));
        order.setTicketNumber(json.getInteger("ticket_number"));
        order.setBuyUid(json.getBigInteger("buy_uid"));
        String startTime=DateUtil.strToDateLong(json.getString("start_time"));
        String createTime= DateUtil.strToDateLong(json.getString("create_time"));
        order.setStartTime(DateUtil.strToDate(startTime));
        order.setCreateTime(DateUtil.strToDate(createTime));
        order.setOrderNo(UtilConf.getUUID());
        order.setOrderSource("乘客服务系统");
        order.setStatus(0);//初始化
        order.setTravelSource(null);
        //是否包车属性
        order.setCharteredBus(json.getString("chartered_bus"));
        JSONArray jsonArray=new JSONArray();
        if(json.getJSONArray("seat_preferences")!=null){
            jsonArray =json.getJSONArray("seat_preferences");
        }else{
            String ids= json.getString("u_ids") ;
            String[] id=ids.split(",");
            for (int i=0; i<id.length;i++){
                JSONObject object=new JSONObject();
                object.put("u_id",id[i]);
                object.put("seat_preference","");
                jsonArray.add(object);

            }
        }
        orderInfoService.insertOrder(order,jsonArray.toString());

        //订单数据保存完成  进行数据算法调用
        //1先调取


        //操作数据库
        try {
            result.setCode(ResutlMessage.SUCCESS.getName());
            result.setMessage(ResutlMessage.SUCCESS.getValue());
            logger.info("新增行程"+result.toString());
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            result.setCode(ResutlMessage.FAIL.getName());
            result.setMessage(ResutlMessage.FAIL.getValue());
            logger.info("新增行程"+result.toString());
            return result;
        }



    }
}
