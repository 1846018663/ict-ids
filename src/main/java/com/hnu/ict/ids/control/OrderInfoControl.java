package com.hnu.ict.ids.control;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hnu.common.respone.BaseResponse;
import com.hnu.common.respone.PojoBaseResponse;
import com.hnu.ict.ids.bean.CustomerHttpAPIBean;
import com.hnu.ict.ids.bean.OrderTask;
import com.hnu.ict.ids.bean.TicketInfo;
import com.hnu.ict.ids.entity.*;
import com.hnu.ict.ids.exception.ConfigEnum;
import com.hnu.ict.ids.exception.NetworkEnum;
import com.hnu.ict.ids.exception.ResultEntity;
import com.hnu.ict.ids.exception.ResutlMessage;
import com.hnu.ict.ids.service.IvsAppCarInfoService;
import com.hnu.ict.ids.service.NetworkLogService;
import com.hnu.ict.ids.service.OrderInfoService;
import com.hnu.ict.ids.service.TravelInfoService;
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
        order.setStartTime(DateUtil.strToDateyyyyMMddHHmmss(json.getString("start_time")));
        order.setCreateTime(DateUtil.strToDateyyyyMMddHHmmss(json.getString("create_time")));
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
        if(oper_type==1){//添加已有行程
            //验证o_id    SourceOrderId是否已经存在

            OrderInfo orderInfo= orderInfoService.getBySourceOrderId(json.getLong("o_id").toString());

            if(orderInfo!=null){
                result.put("code","00007");
                result.put("message","该订单已经存在，请勿重复提交");
                result.put("result","");
                logger.info("添加已有行程"+result.toString());
                return result;
            }

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
                String travelId =json.getString("travel_id");
                order.setTravelId(travelId);
                logger.info("行程id"+travelId);
                orderInfoService.insertOrder(order,json.getString("u_ids"));
                result.put("code","00008");
                result.put("message","加入成功");
                result.put("result","");
                logger.info("添加已有行程"+result.toString());
                return result;
            }else{
                result.put("code","00007");
                result.put("message","本次行程车载人数已上限");
                result.put("result","");
                logger.info("添加已有行程"+result.toString());
                return result;

            }
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



    @RequestMapping(value = "/findNotTrave", method = RequestMethod.GET)
    public PojoBaseResponse findNotTrave() {
        logger.info("---------------------------执行任务------------------");
        PojoBaseResponse result=new PojoBaseResponse();
        //第一步查询订单  查询没有行程id  且当前 开始时间大约30分钟内的订单
        Date stateDate=new Date();
        //30分钟毫秒
        long time=1000*60*30+stateDate.getTime();
        Date endDate=DateUtil.millisecondToDate(time);
        List<OrderInfo>  listOrder=orderInfoService.findNotTrave(DateUtil.getCurrentTime(stateDate),DateUtil.getCurrentTime(endDate));
        String body= null;
        Map<String,Object> resultMap= redisTemplate.opsForHash().entries("setTimeConfig");
        if(listOrder.size()>0){
            List<OrderTask> list=new ArrayList<>();
            for (Iterator<OrderInfo> it = listOrder.iterator(); it.hasNext();) {
                OrderInfo info=it.next();
                OrderTask task=new OrderTask();
                task.setO_id(info.getId().intValue());
                task.setFrom_p_id(info.getBeginStationId());
                task.setTo_p_id(info.getEndStationId());
                task.setStart_time(DateUtil.getCurrentTime(info.getStartTime()));
                task.setOrder_time(DateUtil.getCurrentTime(info.getCreateTime()));
                task.setTicket_number(info.getTicketNumber());
                int DateTime=Integer.parseInt(resultMap.get(ConfigEnum.CARTIMECONFIG.getValue()).toString())*60;
                task.setSet_time(DateTime);
                list.add(task);
            }
            String json=JSON.toJSONString(list);

            //对数据进行解析
            List<TravelInfo> travelInfoList=new ArrayList<>();
            logger.info("向算法发送请求数据"+json);

            //接口访问日志操作
            NetworkLog networkLog=new NetworkLog();
            networkLog.setCreateTime(new Date());
            try {
                networkLog.setInterfaceInfo(NetworkEnum.ALGORITHM_INRERFACE_ADD.getValue());
                networkLog.setType(NetworkEnum.TYPE_HTTP.getValue());
                networkLog.setMethod(NetworkEnum.METHOD_POST.getValue());
                networkLog.setUrl(URL);
                networkLog.setAccessContent(json);
                body= HttpClientUtil.doPostJson(URL,json);
                logger.info("接收算法放回数据"+body);
                networkLog.setResponseResult(body);
                networkLog.setStatus(NetworkEnum.STATUS_SUCCEED.getValue());
            } catch (Exception e) {
                networkLog.setStatus(NetworkEnum.STATUS_FAILED.getValue());
                e.printStackTrace();
            }
            //保存接口日志
            networkLogServer.insertNetworkLog(networkLog);
            logger.info("---------------------------保存接口日志------------------");

            JSONObject jsonObject=JSONObject.parseObject(body);
            int status = jsonObject.getInteger("status");
            logger.info("-----------------开始解析算法返回结果-------------");
            //有效数据
            if(status==1){
                Map<Integer,String > map=new HashMap<>();

                JSONArray array=jsonObject.getJSONArray("task");
                for (int i=0;i<array.size();i++){
                    JSONObject object=array.getJSONObject(i);
                    OrderInfo orderInfo=orderInfoService.getById(object.getInteger("i_id"));
                    TravelInfo info=new TravelInfo();
                    info.setSourceTravelId(orderInfo.getSourceOrderId());
                    info.setBeginStationId(object.getInteger("from_p_id"));
                    info.setEndStationId(object.getInteger("to_p_id"));
                    info.setTravelStatus(1);
                    info.setItNumber(object.getInteger("it_number"));
                    info.setStartTime(DateUtil.strToDate(object.getString("start_time")));
                    info.setDistance(new BigDecimal(object.getDouble("distance")));
                    info.setExpectedTime(object.getInteger("expected_time").toString());
                    info.setDriverContent(object.getString("driver_content"));
                    info.setAllTravelPlat(object.getString("all_travel_plat"));
                    info.setCarId(object.getInteger("car_id"));
                    String taskerId=object.getString("travel_id");
                    info.setTravelId(taskerId);
//                    info.setDriverId(object.getInteger()); //司机id
                    info.setCreateTime(new Date());
                    //获取订单与行程对应数据关联
                    String orderIds=object.getString("correspond_order_id").replace("[","").replace("]","").replace(" ","");
                    String[] ids=orderIds.split(",");
                    for (int k=0;k<ids.length;k++){
                        map.put(Integer.parseInt(ids[k]),taskerId);
                    }
                    travelInfoList.add(info);
                }
                logger.info("-----------------已完成解析算法返回结果-------------");
                //解析数据封装后   service批量处理
                boolean bool=travelInfoService.addTravelInfoList(travelInfoList,map);
                if(bool){
                    //数据操作成功回调乘客服务系统
                    logger.info("-----------------回调乘客服务系统-------------");
                    String str=successfulTrip(map,listOrder);
                    logger.info("回调乘客服务系统接收参数"+str);
                    logger.info("-----------------回调乘客服务系统    结束-------------");
                }
            }

        }
        logger.info("执行任务完毕："+ new Date());

        return result;
    }



    public String successfulTrip(Map<Integer,String > map,List<OrderInfo> orderInfoList){
        ArrayList<CustomerHttpAPIBean> list=new ArrayList<>();
        for(Map.Entry<Integer,String> entry : map.entrySet()){
            TravelInfo info= travelInfoService.findTravelId(entry.getValue());
            OrderInfo order= orderInfoService.getById(entry.getKey());
            CustomerHttpAPIBean customerHttpAPIBean=new CustomerHttpAPIBean();
            customerHttpAPIBean.setDistance(info.getDistance().doubleValue());
            customerHttpAPIBean.setO_id(Long.parseLong(info.getSourceTravelId()));
            customerHttpAPIBean.setTravel_id(info.getId().toString());
            customerHttpAPIBean.setExpected_time(Integer.parseInt(info.getExpectedTime()));
            customerHttpAPIBean.setAll_travel_plat(info.getAllTravelPlat());
            customerHttpAPIBean.setDriver_content(info.getDriverContent());
            customerHttpAPIBean.setC_id(info.getCarId());
            customerHttpAPIBean.setDriver_id(info.getDriverId());
            customerHttpAPIBean.setReservation_status(info.getTravelStatus());
            customerHttpAPIBean.setIt_number(order.getTicketNumber());
            customerHttpAPIBean.setRet_status(0);
            customerHttpAPIBean.setOper_time(DateUtil.strToDayDate(new Date()));
            List<TicketInfo> ticket_info=new ArrayList<>();
            TicketInfo ticke=new TicketInfo();
            ticke.setU_id(order.getBuyUid().intValue());
            ticke.setSeat_number("");
            ticket_info.add(ticke);
            customerHttpAPIBean.setTicket_info(ticket_info);
            list.add(customerHttpAPIBean);

        }
        String json=JSON.toJSONString(list);
        //接口访问日志操作
        NetworkLog networkLog=new NetworkLog();
        networkLog.setCreateTime(new Date());
        networkLog.setStatus(NetworkEnum.STATUS_SUCCEED.getValue());
        networkLog.setInterfaceInfo(NetworkEnum.PASSENGRT_SERVICE_CALL_BACK.getValue());
        networkLog.setMethod(NetworkEnum.METHOD_POST.getValue());
        networkLog.setUrl(callback_URL);
        networkLog.setType(NetworkEnum.TYPE_HTTPS.getValue());
        networkLog.setAccessContent(json);
        logger.info("行程预约成功传参内容"+json);
        String body="";
        try {
            body= HttpClientUtil.doPostJson(callback_URL,json);
            networkLog.setResponseResult(body);
            networkLog.setStatus(NetworkEnum.STATUS_SUCCEED.getValue());
            logger.info("行程预约成功返回结果:"+body);
            JSONObject resultJson=JSONObject.parseObject(body);
            String code=resultJson.getString("code");

            if(!code.equals("00008")){
                //失败   数据信息做修改  失败status 为2
                orderInfoService.updateByIdList(orderInfoList,2);
            }else{
                //失败   数据信息做修改  成功status 为1
                orderInfoService.updateByIdList(orderInfoList,1);
            }
        } catch (Exception e) {
            //失败   数据信息做修改  失败status 为2
            orderInfoService.updateByIdList(orderInfoList,2);

            networkLog.setStatus(NetworkEnum.STATUS_FAILED.getValue());
            e.printStackTrace();
        }

        networkLogServer.insertNetworkLog(networkLog);
        return body;
    }

}
