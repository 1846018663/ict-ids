package com.hnu.ict.ids.control;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hnu.common.respone.PojoBaseResponse;
import com.hnu.ict.ids.bean.CustomerHttpAPIBean;
import com.hnu.ict.ids.bean.OrderTask;
import com.hnu.ict.ids.bean.TicketInfo;
import com.hnu.ict.ids.entity.NetworkLog;
import com.hnu.ict.ids.entity.OrderInfo;
import com.hnu.ict.ids.entity.TravelInfo;
import com.hnu.ict.ids.entity.TravelTicketInfo;
import com.hnu.ict.ids.exception.ConfigEnum;
import com.hnu.ict.ids.exception.NetworkEnum;
import com.hnu.ict.ids.service.*;
import com.hnu.ict.ids.utils.DateUtil;
import com.hnu.ict.ids.webHttp.HttpClientUtil;
import io.swagger.annotations.Api;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.*;

@Api(tags = "调度任务API")
@RestController
@RequestMapping("/schedulingTask")
public class DispatchTaskControl {

    Logger logger= LoggerFactory.getLogger(DispatchTaskControl.class);

    @Autowired
    OrderInfoService orderInfoService;

    @Value("${travel.algorithm.url}")
    private String URL;
    @Value("${passenger.service.callback.url}")
    private String callback_URL;


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


    /**
     * 调用新增行程算法接口
     * @return
     */
    @RequestMapping(value = "/findNotTrave", method = RequestMethod.GET)
    public PojoBaseResponse findNotTrave() {
        logger.info("---------------------------执行任务------------------");
        PojoBaseResponse result=new PojoBaseResponse();
        //第一步查询订单  查询没有行程id  且当前 开始时间大约30分钟内的订单
        Date stateDate=new Date();
        //30分钟毫秒
        long time=1000*60*30+stateDate.getTime();
        Date endDate= DateUtil.millisecondToDate(time);
        List<OrderInfo> listOrder=orderInfoService.findNotTrave(DateUtil.getCurrentTime(stateDate),DateUtil.getCurrentTime(endDate));
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
            String json= JSON.toJSONString(list);

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
            String taskerId=null;
            JSONObject jsonObject=JSONObject.parseObject(body);
            int status = jsonObject.getInteger("status");
            logger.info("-----------------开始解析算法返回结果-------------");
            //有效数据
            if(status==1){
                Map<Integer,String > map=new HashMap<>();

                JSONArray array=jsonObject.getJSONArray("task");
                for (int i=0;i<array.size();i++){
                    JSONObject object=array.getJSONObject(i);
                    TravelInfo info=new TravelInfo();
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
                    info.setBeginStationName(object.getString("from_order_name"));
                    info.setEndStationName(object.getString("to_order_name"));
                    info.setParkName(object.getString("park_name"));
                    info.setParkId(object.getInteger("park_id"));
                    info.setWarning(object.getString("warning"));
                    taskerId =object.getString("travel_id");

                    info.setTravelId(taskerId);
//                    info.setDriverId(object.getInteger()); //司机id
                    info.setCreateTime(new Date());
                    //获取订单与行程对应数据关联
                    String orderIds=object.getString("correspond_order_id").replace("[","").replace("]","").replace(" ","");
                    String[] ids=orderIds.split(",");
                    for (int k=0;k<ids.length;k++){
                        map.put(Integer.parseInt(ids[k]),taskerId);
                    }
                    logger.info("-----------------已完成解析算法返回结果"+map.toString());
                    travelInfoList.add(info);
                }
                logger.info("-----------------已完成解析算法返回结果-------------");






                //解析数据封装后   service批量处理
                boolean bool=travelInfoService.addTravelInfoList(travelInfoList,map);

//                //座位编排   1获取本次行程所有对应订单   根据订单获取对应id   根据对应id  根据序号编排   保存数据库
//                List<OrderInfo> orderList=orderInfoService.findOrderTravelId(taskerId);
//                //循环订单获取对应userId
//                List<TravelTicketInfo> travelTicketInfoList=new ArrayList<>();
//                Integer k=0;
//                for(OrderInfo info : orderList) {//其内部实质上还是调用了迭代器遍历方式，这种循环方式还有其他限制，不建议使用。
//                    List<OrderUserLink>  userLinks=orderUserLinkService.findOrderNo(info.getOrderNo());
//                    for (OrderUserLink userLink : userLinks){
//                        TravelTicketInfo ticketInfo=new TravelTicketInfo();
//                        ticketInfo.setTravelId(taskerId);
//                        k=k+1;
//                        ticketInfo.setSeatNum(k.toString());
//                        ticketInfo.setUserId(userLink.getUserId());
//                        travelTicketInfoList.add(ticketInfo);
//                    }
//                }
//
//                //数据内容添加座位表 并给与排序
//                travelTicketInfoService.insertTravelTicketInfoList(travelTicketInfoList);
//
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


    /**
     * 回调乘客服务系统封装  预约行程成功回调接口
     * @param map
     * @param orderInfoList
     * @return
     */
    public String successfulTrip(Map<Integer,String > map,List<OrderInfo> orderInfoList){
        ArrayList<CustomerHttpAPIBean> list=new ArrayList<>();
        for(Map.Entry<Integer,String> entry : map.entrySet()){
            TravelInfo info= travelInfoService.findTravelId(entry.getValue());
            OrderInfo order= orderInfoService.getById(entry.getKey());
            CustomerHttpAPIBean customerHttpAPIBean=new CustomerHttpAPIBean();
            customerHttpAPIBean.setDistance(info.getDistance().doubleValue());
            customerHttpAPIBean.setO_id(Long.parseLong(order.getSourceOrderId()));
            customerHttpAPIBean.setTravel_id(info.getTravelId().toString());
            customerHttpAPIBean.setExpected_time(Integer.parseInt(info.getExpectedTime()));
            customerHttpAPIBean.setAll_travel_plat(info.getAllTravelPlat());
            customerHttpAPIBean.setDriver_content(info.getDriverContent());
            customerHttpAPIBean.setC_id(info.getCarId());
            customerHttpAPIBean.setDriver_id(info.getDriverId());
            customerHttpAPIBean.setReservation_status(info.getTravelStatus());
            customerHttpAPIBean.setIt_number(info.getItNumber());
            customerHttpAPIBean.setRet_status(0);
            customerHttpAPIBean.setOper_time(DateUtil.strToDayDate(new Date()));
            //乘客座位信息获取封装
            List<TicketInfo> ticketInfoList=new ArrayList<>();
            List<TravelTicketInfo> ticket_info =travelTicketInfoService.findPassengerSeating(info.getTravelId());
            for (TravelTicketInfo travelTicketInfo:ticket_info){
                TicketInfo ticketInfo=new TicketInfo();
                ticketInfo.setSeat_number(travelTicketInfo.getSeatNum());
                ticketInfo.setU_id(travelTicketInfo.getUserId().intValue());
                ticketInfoList.add(ticketInfo);
            }

            customerHttpAPIBean.setTicket_info(ticketInfoList);
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
