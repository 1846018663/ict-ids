package com.hnu.ict.ids.control;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hnu.ict.ids.Kafka.KafkaProducera;
import com.hnu.ict.ids.bean.SeatPreferenceRequset;
import com.hnu.ict.ids.entity.*;
import com.hnu.ict.ids.exception.NetworkEnum;
import com.hnu.ict.ids.exception.ResultEntity;
import com.hnu.ict.ids.exception.ResutlMessage;
import com.hnu.ict.ids.service.*;
import com.hnu.ict.ids.utils.DateUtil;
import com.hnu.ict.ids.config.UtilConf;
import com.hnu.ict.ids.utils.HttpClientUtil;
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

import java.util.*;

@Api(tags = "订单行程API")
@RestController
@RequestMapping("/order")
public class OrderInfoControl {

    Logger logger= LoggerFactory.getLogger(OrderInfoControl.class);

    @Value("${travel.algorithm.add.url}")
    private String add_URL;

    @Autowired
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

    @Autowired
    KafkaProducera kafkaProducera;

    @Autowired
    OrderInfoHistotryService orderInfoHistotryService;




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

        try {
            //操作数据库
            saveOrder(json);
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



    /**
     * 新增订单保存订单
     * @param json
     */
    private void saveOrder(JSONObject json){
        //创建数据对象
        OrderInfo order=new OrderInfo();
        order.setSourceOrderId(json.getString("o_id"));
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
        order.setStatus(0);//初始化
        order.setOrderStatus(1);
        order.setTravelSource(null);
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
        OrderInfoHistotry orderInfoHistotry=new OrderInfoHistotry();
        BeanUtils.copyProperties(order,orderInfoHistotry);
        orderInfoHistotry.setId(null);
        orderInfoHistotry.setCreateTime(new Date());
        orderInfoHistotry.setOrderStatusName("创建订单");

        orderInfoHistotryService.insert(orderInfoHistotry);
        orderInfoService.insertOrder(order,jsonArray.toString());
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
                if(uIds!=null && uIds.length()>0){
                    logger.info("取消订单关联u_id"+uIds);
                    result=orderInfoService.deleteSourceOrderId(order,orderInfoHistotry,uIds);
                }else{
                    //直接删除订单   根据订单号查询对应u_ids
                    List<OrderUserLink>  list=orderUserLinkService.findOrderNo(order.getOrderNo());
                    String ids="";
                    for (OrderUserLink orderUserLink: list){
                        ids=ids+orderUserLink.getUserId()+",";
                    }
                    ids=ids.substring(0,ids.length()-1);
                    logger.info("取消订单关联u_id"+uIds);
                    result=orderInfoService.deleteSourceOrderId(order,orderInfoHistotry,ids);
                }


            }

        }
        logger.info("删除行程"+result.toString());
        return result;
    }


    @RequestMapping(value="/addExistence" ,method = RequestMethod.POST)
    public Map addExistence(@RequestBody  String body){
        Map<String,Object> result=new HashMap<>();
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
        TravelInfo travelInfo= travelInfoService.findTravelId(travel_id);
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
            JSONArray jsonArray=new JSONArray();
            if(json.getString("seat_preference")!=null){
                jsonArray =JSONArray.parseArray(json.getString("seat_preference"));
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
//            orderInfoService.insertOrder(order,jsonArray.toString());
            //调用算法添加已有行程算法接口
            logger.info("===============调用添加已有行程算法接口=============");
            boolean bool= addYesAlgorithm(order,jsonArray.size(),jsonArray);
            logger.info("===============调用添加已有行程算法完成   对行程数据update=============");

            logger.info("===============保存已有行程对应订单信息完成=============");

            if(bool){
                result.put("code","00008");
                result.put("message","加入成功");
                result.put("result","");
                OrderInfo orederInfo= orderInfoService.getBySourceOrderId(order.getSourceOrderId());
                List<OrderUserLink> listUser= orderUserLinkService.findOrderNo(order.getOrderNo());
                JSONArray array=new JSONArray();
                for (OrderUserLink user:listUser){
                    JSONObject jsonObject=new JSONObject();
                    jsonObject.put("u_id",user.getUserId());
                    TravelTicketInfo travelTicketInfo= travelTicketInfoService.findTraveIdSeat(orederInfo.getTravelId(),user.getUserId().intValue());
                    jsonObject.put("seat_number",travelTicketInfo.getSeatNum());
                    array.add(jsonObject);
                }
                result.put("ticket_info",array);
                TravelInfo info=travelInfoService.findTravelId(orederInfo.getTravelId());
                kafkaProducera.getTripInfo(info,1);
                logger.info("添加已有行程"+result.toString());
            }else{
                result.put("code","00007");
                result.put("message","加入失败");
                result.put("result","");
                logger.info("添加已有行程"+result.toString());
            }

            return result;
        }

/**
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


**/
        result.put("code","00007");
        result.put("message","加入失败");
        result.put("result","");
        logger.info("添加移除已有行程"+result.toString());
        return  result;

    }




    public boolean addYesAlgorithm(OrderInfo order,int number,JSONArray ids){
        JSONObject json=new JSONObject();

        JSONObject dl=new JSONObject();
        dl.put("oId",order.getSourceOrderId());
        dl.put("fromId",order.getBeginStationId().toString());
        dl.put("toId",order.getEndStationId().toString());
        dl.put("startTime", DateUtil.getCurrentTime(order.getStartTime()));
        dl.put("ticketNumber",number);
        String id="";
        List<SeatPreferenceRequset> listSeatPreference=new ArrayList<>();
        for (int i=0;i<ids.size();i++){
            JSONObject jsonObject=ids.getJSONObject(i);
            SeatPreferenceRequset seatPreference=new SeatPreferenceRequset();
            seatPreference.setUserId(jsonObject.getInteger("u_id").toString());
            if(jsonObject.getString("seat_preference")!=null){
                seatPreference.setSeatPreference(jsonObject.getString("seat_preference"));
            }else{
                seatPreference.setSeatPreference("");
            }
            listSeatPreference.add(seatPreference);
            id=id+jsonObject.getInteger("u_id")+",";
        }
        id=id.substring(0,id.length()-1);
        dl.put("orderUserId",id);
        dl.put("userPreference",listSeatPreference);

        json.put("orderData",dl);
        TravelInfo info=travelInfoService.findTravelId(order.getTravelId());

        List<TravelTicketInfo> travelTicketInfoList=travelTicketInfoService.findPassengerSeating(info.getTravelId());
        JSONArray correspond_seat_id=new JSONArray();
        for (int i=0; i<travelTicketInfoList.size();i++){
            JSONObject ob=new JSONObject();
            TravelTicketInfo travelTicketInfo=travelTicketInfoList.get(i);
            ob.put("userId",travelTicketInfo.getUserId());
            ob.put("seat",travelTicketInfo.getSeatNum());
            correspond_seat_id.add(ob);
        }
        json.put("correspondSeatId",correspond_seat_id);
        json.put("travelId",info.getTravelId());
        //封装最后传参
        logger.info("调用添加已有行程接口   发送请求数据"+json.toString());
        //保存预警信息
        NetworkLog networkLog=new NetworkLog();
        networkLog.setCreateTime(new Date());
        networkLog.setInterfaceInfo(NetworkEnum.ALGORITHM_INRERFACE_UP.getValue());
        networkLog.setType(NetworkEnum.TYPE_HTTP.getValue());
        networkLog.setMethod(NetworkEnum.METHOD_POST.getValue());
        networkLog.setUrl(add_URL);
        networkLog.setAccessContent(json.toString());
        String body= null;
        try {
            body = HttpClientUtil.doPostJson(add_URL,json.toString());
            networkLog.setResponseResult(body);
            networkLog.setStatus(NetworkEnum.STATUS_SUCCEED.getValue());
        } catch (Exception e) {
            networkLog.setStatus(NetworkEnum.STATUS_FAILED.getValue());
            e.printStackTrace();
        }
        //保存接口日志
        networkLogServer.insertNetworkLog(networkLog);
        logger.info("调用添加已有行程接口 接收请求数据"+body);
        JSONObject jsonObject= JSON.parseObject(body);
        if(jsonObject.getInteger("status")==1){
            JSONObject resultJson=jsonObject.getJSONObject("task");
            //查询行程修改记录
            TravelInfo travelInfo=travelInfoService.findTravelId(resultJson.getString("travelId"));
            travelInfo.setItNumber(resultJson.getInteger("itNumber"));
            travelInfo.setUpdateTime(new Date());
            travelInfo.setCorrespondOrderNumber(resultJson.getString("correspondNumber"));
            travelInfoService.updateById(travelInfo);
            orderInfoService.insertOrder(order,ids.toString());
            //保存座位号数据
            JSONArray jsonArr=resultJson.getJSONArray("correspondSeatId");
            List<TravelTicketInfo> travelTicketInfos=new ArrayList<>();
            for (int i=0;i<jsonArr.size();i++){
                JSONObject ob=jsonArr.getJSONObject(i);
                TravelTicketInfo travelTicketInfo=new TravelTicketInfo();
                travelTicketInfo.setTravelId(travelInfo.getTravelId());
                travelTicketInfo.setSeatNum(ob.getString("seat"));
                travelTicketInfo.setUserId(ob.getInteger("userId"));
                travelTicketInfos.add(travelTicketInfo);
            }

            travelTicketInfoService.deldelTraveId(info.getTravelId());
            travelTicketInfoService.insertTravelTicketInfoList(travelTicketInfos);

            return  true;
        }else{
            return  false;
        }



    }





}
