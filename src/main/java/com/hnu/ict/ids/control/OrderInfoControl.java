package com.hnu.ict.ids.control;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hnu.ict.ids.entity.*;
import com.hnu.ict.ids.exception.ResultEntity;
import com.hnu.ict.ids.exception.ResutlMessage;
import com.hnu.ict.ids.service.*;
import com.hnu.ict.ids.utils.DateUtil;
import com.hnu.ict.ids.utils.UtilConf;
import com.hnu.ict.ids.webHttp.HttpClientUtil;
import io.swagger.annotations.Api;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

@Api(tags = "订单行程API")
@RestController
@RequestMapping("/order")
public class OrderInfoControl {

    Logger logger= LoggerFactory.getLogger(OrderInfoControl.class);

    @Value("${travel.algorithm.url}")
    private String URL;
    @Value("${passenger.service.callback.url}")
    private String callback_URL;

    @Resource
    OrderInfoService orderInfoService;

    @Autowired
    TravelInfoService travelInfoService;

    @Autowired
    IvsAppCarInfoService ivsAppCarInfoService;

    @Autowired
    RedisTemplate redisTemplate;

    @Autowired
    NetworkLogService networkLogServer;

    @Autowired
    OrderUserLinkService orderUserLinkService;

    @Autowired
    TravelTicketInfoService travelTicketInfoService;




    @RequestMapping(value="/add" , method = RequestMethod.POST)
    public ResultEntity addOrder(@RequestBody  String body){
        ResultEntity result=new ResultEntity();
        logger.info(body);
        JSONObject json=JSONObject.parseObject(body);
        //验证o_id    SourceOrderId是否已经存在
        String oId=json.getString("o_id");
        OrderInfo orderInfo= orderInfoService.getBySourceOrderId(oId);

        if(orderInfo!=null){
            result.setCode(ResutlMessage.FAIL.getName());
            result.setMessage("该订单已经存在，请勿重复提交");
            logger.info("新增行程"+result.toString());
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
        String createTime=DateUtil.strToDateLong(json.getString("create_time"));
        logger.info("时间换算"+startTime);
        order.setStartTime(DateUtil.strToDate(startTime));
        logger.info("时间换算后"+DateUtil.strToDate(startTime));
        order.setCreateTime(DateUtil.strToDate(createTime));
        order.setOrderNo(UtilConf.getUUID());
        order.setOrderSource("乘客服务系统");
        order.setTravelSource(null);
        String u_ids=json.getString("u_ids");
        //操作数据库
        try {
            orderInfoService.insertOrder(order,u_ids);
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


    @RequestMapping(value="/deleteOrderNo" ,method = RequestMethod.POST)
    public ResultEntity deleteOrderNo(@RequestBody  String body) {
        ResultEntity result = new ResultEntity();
        JSONObject json=JSONObject.parseObject(body);
        Long o_id=json.getLong("o_id");
        logger.info(o_id+"删除行程o_id"+body);
        String travel_id=json.getString("travel_id");
        if(StringUtils.hasText(o_id.toString())){
            OrderInfo order=orderInfoService.getBySourceOrderId(o_id.toString());
            if(order==null){
                result.setCode(ResutlMessage.FAIL.getName());
                result.setMessage("该订单号不存在！");
                logger.info("删除行程"+result.toString());
                return result;
            }else{
                OrderInfoHistotry orderInfoHistotry=new OrderInfoHistotry();
                BeanUtils.copyProperties(order,orderInfoHistotry);
                orderInfoHistotry.setId(null);
                //判断删除指定用户
                String uIds=json.getString("u_ids");
                orderInfoService.deleteSourceOrderId(order,orderInfoHistotry,uIds);

            }

        }


        result.setCode(ResutlMessage.SUCCESS.getName());
        result.setMessage(ResutlMessage.SUCCESS.getValue());
        logger.info("删除行程"+result.toString());
        return result;
    }


    @RequestMapping(value="/addExistence" ,method = RequestMethod.POST)
    public Map addExistence(@RequestBody  String body){
        Map<String,String> result=new HashMap<>();
        JSONObject json=JSONObject.parseObject(body);
        logger.info("添加已有行程"+body);

        String u_ids=json.getString("u_ids");
        if(!StringUtils.hasText(u_ids)){
            result.put("code","00007");
            result.put("message","该订单对应信息用户组u_ids为空");
            result.put("result","");
            logger.info("已有行程"+result.toString());
            return result;
        }
        //判断数据操作类型   是添加  还是移除
        int oper_type=json.getInteger("oper_type");
        String travel_id=json.getString("travel_id");
        //根据行程id查询订单信息   并创建订单数据
        TravelInfo travelInfo= travelInfoService.getById(new BigInteger(travel_id));
        //添加已有行程
        if(oper_type==1){
            //验证o_id    SourceOrderId是否已经存在

            OrderInfo orderInfo= orderInfoService.getBySourceOrderId(json.getLong("o_id").toString());

            if(orderInfo!=null){
                result.put("code","00007");
                result.put("message","该订单已经存在，请勿重复提交");
                result.put("result","");
                logger.info("添加已有行程"+result.toString());
                return result;
            }
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
            order.setTravelId(travel_id);
            logger.info("创建订单数据行程id"+travel_id);
            String[] ids=json.getString("u_ids").split(",");
            //调用算法添加已有行程算法接口
            logger.info("===============调用添加已有行程算法接口=============");
            TravelInfo info=addYesAlgorithm(order,ids.length,travelInfo.getItNumber());
            info.setId(travelInfo.getId());
            info.setCreateTime(travelInfo.getCreateTime());
            info.setUpdateTime(new Date());
            //修改行程数据
            travelInfoService.updateById(info);
            logger.info("===============调用添加已有行程算法完成   对行程数据update=============");
            orderInfoService.insertOrder(order,json.getString("u_ids"));
            logger.info("===============保存已有行程对应订单信息完成=============");

            result.put("code","00008");
            result.put("message","加入成功");
            result.put("result","");
            logger.info("添加已有行程"+result.toString());
            return result;
        }


        if(oper_type==2){//移除已有行程
            Long o_id=json.getLong("o_id");

            OrderInfo order=orderInfoService.getBySourceOrderId(o_id.toString());
            if(order==null){
                result.put("code","00007");
                result.put("message","该订单不存在，移除操作失败");
                result.put("result","");
                logger.info("删除已有行程"+result.toString());
                return result;
            }
            OrderInfoHistotry orderInfoHistotry=new OrderInfoHistotry();
            BeanUtils.copyProperties(order,orderInfoHistotry);
            orderInfoHistotry.setId(null);
            orderInfoService.deleteSourceOrderId(order,orderInfoHistotry,u_ids);
            result.put("code","00008");
            result.put("message","移除成功");
            result.put("result","");
            logger.info("移除已有行程"+result.toString());
            return  result;
        }



        result.put("code","00007");
        result.put("message","加入失败");
        result.put("result","");
        logger.info("添加移除已有行程"+result.toString());
        return  result;

    }




    public TravelInfo addYesAlgorithm(OrderInfo order,int number,int ticketNumber){
        JSONObject json=new JSONObject();

        JSONArray dlJSON=new JSONArray();
        JSONObject dl=new JSONObject();
        dl.put("o_id",order.getId());
        dl.put("from_p_id",order.getBeginStationId());
        dl.put("to_p_id",order.getEndStationId());
        dl.put("start_time", DateUtil.getCurrentTime(order.getStartTime()));
        dl.put("ticket_number",number+"/"+ticketNumber);
        dlJSON.add(dl);

        JSONArray taskJson=new JSONArray();
        JSONObject task=new JSONObject();
        TravelInfo info=travelInfoService.findTravelId(order.getTravelId());

        task.put("it_number",info.getItNumber());
        task.put("car_id",info.getCarId());
        task.put("from_p_id",info.getBeginStationId());
        task.put("from_order_name",info.getBeginStationName());
        task.put("to_p_id",info.getEndStationId());
        task.put("to_order_name",info.getEndStationName());
        if(info.getParkId()!=null){
            task.put("park_id",info.getParkId());
        }else{
            task.put("park_id","");
        }
        if(info.getParkName()!=null){
            task.put("park_name",info.getParkName());
        }else{
            task.put("park_name","");
        }

        task.put("start_time",info.getStartTime());
        task.put("travel_id",info.getTravelId());
        task.put("driver_content",info.getDriverContent());
        task.put("all_travel_plat",info.getAllTravelPlat());
        task.put("expected_time",info.getExpectedTime());
        task.put("distance",info.getDistance());
        if(info.getModifyId()!=null){
            task.put("modify_id",info.getModifyId());
        }else{
            task.put("modify_id","");
        }
        if(info.getWarning()!=null){
            task.put("warning",info.getWarning());
        }else{
            task.put("warning","");
        }


        List<OrderInfo> orderList= orderInfoService.findOrderTravelId(order.getTravelId());
        String oIds="[";
        if(orderList!=null &&orderList.size()>0){
            for (int i=0;i<orderList.size();i++){
                oIds=oIds+orderList.get(i).getId()+",";
            }
            oIds.substring(0,oIds.length()-1);
            oIds=oIds+"]";
            task.put("correspond_order_id",oIds);
            taskJson.add(task);
        }

        //封装最后传参
        json.put("dl_1",dlJSON);
        json.put("task_record",taskJson);
        logger.info("调用添加已有行程接口   发送请求数据"+json.toString());
        String body= HttpClientUtil.doPostJson(URL,json.toString());
        logger.info("调用添加已有行程接口 接收请求数据"+body);
        JSONObject jsonObject= JSON.parseObject(body);
        JSONArray array=jsonObject.getJSONArray("task");
        //对数据进行解析
        List<TravelInfo> travelInfoList=new ArrayList<>();
        Map<Integer,String > map=new HashMap<>();
        TravelInfo travelInfo=new TravelInfo();
        JSONObject object=array.getJSONObject(0);
        OrderInfo orderInfo=orderInfoService.getById(object.getInteger("i_id"));

        travelInfo.setBeginStationId(object.getInteger("from_p_id"));
        travelInfo.setEndStationId(object.getInteger("to_p_id"));
        travelInfo.setTravelStatus(1);
        travelInfo.setItNumber(object.getInteger("it_number"));
        travelInfo.setStartTime(DateUtil.strToDate(object.getString("start_time")));
        travelInfo.setDistance(new BigDecimal(object.getDouble("distance")));
        travelInfo.setExpectedTime(object.getInteger("expected_time").toString());
        travelInfo.setDriverContent(object.getString("driver_content"));
        travelInfo.setAllTravelPlat(object.getString("all_travel_plat"));
        travelInfo.setCarId(object.getInteger("car_id"));
        travelInfo.setBeginStationName(object.getString("from_order_name"));
        travelInfo.setEndStationName(object.getString("to_order_name"));
        travelInfo.setParkName(object.getString("park_name"));
        travelInfo.setParkId(object.getInteger("park_id"));
        travelInfo.setWarning(object.getString("warning"));
        String taskerId =object.getString("travel_id");
        travelInfo.setTravelId(taskerId);
        //info.setDriverId(object.getInteger()); //司机id
        //获取订单与行程对应数据关联
        String orderIds=object.getString("correspond_order_id").replace("[","").replace("]","").replace(" ","");
        String[] ids=orderIds.split(",");
        for (int k=0;k<ids.length;k++){
            map.put(Integer.parseInt(ids[k]),taskerId);
        }
        travelInfo.setModifyId(object.getString("modify_id"));

        return travelInfo;
    }





}
