package com.hnu.ict.ids.control;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hnu.common.respone.PojoBaseResponse;
import com.hnu.ict.ids.bean.CustomerHttpAPIBean;
import com.hnu.ict.ids.bean.OrderTask;
import com.hnu.ict.ids.bean.TicketInfo;
import com.hnu.ict.ids.bean.Tickets;
import com.hnu.ict.ids.entity.*;
import com.hnu.ict.ids.exception.ConfigEnum;
import com.hnu.ict.ids.exception.NetworkEnum;
import com.hnu.ict.ids.exception.ResultEntity;
import com.hnu.ict.ids.service.*;
import com.hnu.ict.ids.utils.DateUtil;
import com.hnu.ict.ids.utils.UnicodeConvertUtils;
import com.hnu.ict.ids.webHttp.HttpClientUtil;
import io.swagger.annotations.Api;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.StringUtils;
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
    @Value("${travel.algorithm.response.url}")
    private String response_URL;

    @Value("${passenger.service.capacity.test.url}")
    private String capacity_url;


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
                if(info.getTicketNumber()>0){
                    task.setTicket_number(info.getTicketNumber());
                }else{//订单已经被需取消   中途终止该操作
                    return null;
                }
                task.setO_id(info.getId().intValue());
                task.setFrom_p_id(info.getBeginStationId());
                task.setTo_p_id(info.getEndStationId());
                task.setStart_time(DateUtil.getCurrentTime(info.getStartTime()));
                task.setOrder_time(DateUtil.getCurrentTime(info.getCreateTime()));


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
                Map<String,String > mapResult=new HashMap<>();
                JSONArray array=jsonObject.getJSONArray("task");
                if(array.size()>0){
                    for (int i=0;i<array.size();i++){
                        JSONObject object=array.getJSONObject(i);
                        TravelInfo info=new TravelInfo();
                        info.setBeginStationId(object.getInteger("from_p_id"));
                        info.setEndStationId(object.getInteger("to_p_id"));
                        info.setTravelStatus(1);//预约成功
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
                        info.setDriverId(object.getInteger("driver_id"));
                        info.setWarning(object.getString("warning"));
                        taskerId =object.getString("travel_id");

                        info.setTravelId(taskerId);
                        info.setCreateTime(new Date());
                        //获取订单与行程对应数据关联 .replace("[","").replace("]","").replace(" ","")
                        String orderIds=object.getString("correspond_order_id");
                        String[] ids=orderIds.split(",");
                        for (int k=0;k<ids.length;k++){
                            map.put(Integer.parseInt(ids[k]),taskerId);
                            OrderInfo orderInfo=orderInfoService.getById(Integer.parseInt(ids[k]));
                            mapResult.put(orderInfo.getSourceOrderId(),taskerId);
                        }
                        logger.info("-----------------已完成解析算法返回结果"+map.toString());
                        travelInfoList.add(info);
                    }
                    logger.info("-----------------已完成解析算法返回结果-------------");


                    //解析数据封装后   service批量处理
                    boolean bool=travelInfoService.addTravelInfoList(travelInfoList,map);
                    if(bool){
                        //数据操作成功回调乘客服务系统
                        logger.info("-----------------回调乘客服务系统-------------");
                        String str=successfulTrip(mapResult,travelInfoList,listOrder);
                        logger.info("回调乘客服务系统接收参数"+str);
                        logger.info("-----------------回调乘客服务系统    结束-------------");
                    }
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
    public String successfulTrip(Map<String,String > map,List<TravelInfo> travelInfoList,
                                 List<OrderInfo> orderInfoList){
        List<CustomerHttpAPIBean> customerHttpAPIBeanList=new ArrayList<>();
        for(TravelInfo  travelInfo:travelInfoList){
            CustomerHttpAPIBean customerHttpAPIBean=new CustomerHttpAPIBean();
            customerHttpAPIBean.setDistance(travelInfo.getDistance().doubleValue());

            String oIds="";
            for(Map.Entry<String, String> entry : map.entrySet()){
                String mapKey = entry.getKey();
                oIds=oIds+mapKey+",";
            }
            oIds=oIds.substring(0,oIds.length()-1);
            customerHttpAPIBean.setO_ids(oIds);
            customerHttpAPIBean.setTravel_id(travelInfo.getTravelId().toString());
            customerHttpAPIBean.setExpected_time(Integer.parseInt(travelInfo.getExpectedTime()));
            customerHttpAPIBean.setAll_travel_plat(travelInfo.getAllTravelPlat());
            customerHttpAPIBean.setDriver_content(travelInfo.getDriverContent());
            customerHttpAPIBean.setC_id(travelInfo.getCarId());
            if(travelInfo.getDriverId()==null){
                customerHttpAPIBean.setDriver_id(0);
            }else{
                customerHttpAPIBean.setDriver_id(travelInfo.getDriverId());
            }

            customerHttpAPIBean.setReservation_status(travelInfo.getTravelStatus());
            customerHttpAPIBean.setIt_number(travelInfo.getItNumber());
            customerHttpAPIBean.setRet_status(1);
            customerHttpAPIBean.setOper_time(DateUtil.strToDayDate(new Date()));

            //乘客座位信息获取封装
            List<TicketInfo> ticketInfoList=new ArrayList<>();
            for (OrderInfo info:orderInfoList ){
                List<OrderUserLink> ticket_info =orderUserLinkService.findOrderNo(info.getOrderNo());
                TicketInfo ticketInfo=new TicketInfo();
                ticketInfo.setO_id(info.getSourceOrderId());
                List<Tickets> ticketsList=new ArrayList<>();
                for (OrderUserLink user:ticket_info){
                    Tickets tickets=new Tickets();
                    tickets.setU_id(user.getUserId().intValue());
                    tickets.setSeat_number("");
                    ticketsList.add(tickets);
                }
                ticketInfo.setTickets(ticketsList);
                ticketInfoList.add(ticketInfo);
            }

            customerHttpAPIBean.setTicket_info(ticketInfoList);
            customerHttpAPIBeanList.add(customerHttpAPIBean);

        }

        String json=JSON.toJSONString(customerHttpAPIBeanList);
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
               travelInfoService.updateByIdList(travelInfoList,2);
            }else{
                //失败   数据信息做修改  成功status 为1
                travelInfoService.updateByIdList(travelInfoList,1);
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


    /**
     * 运力情况  回调乘客服务系统任务
     * @param
     * @return
     */
    @RequestMapping(value = "/quickResponse", method = RequestMethod.GET)
    public PojoBaseResponse getQuickResponse(){
        PojoBaseResponse result=new PojoBaseResponse();
        Date stateDate=new Date();
        long time=1000*60*30+stateDate.getTime();
        Date endDate= DateUtil.millisecondToDate(time);
        List<OrderInfo>  orderList= orderInfoService.findNotTrave(DateUtil.getCurrentTime(stateDate),DateUtil.getCurrentTime(endDate));

        for (OrderInfo order:orderList){
            JSONObject jsonObject=new JSONObject();
            jsonObject.put("o_id",order.getId()+"");
            jsonObject.put("from_p_id",order.getBeginStationId()+"");
            jsonObject.put("to_p_id",order.getEndStationId()+"");
            jsonObject.put("start_time",DateUtil.getCurrentTime(order.getStartTime()));
            jsonObject.put("ticket_number",order.getTicketNumber());
            logger.info("快速响应算法发送请求"+jsonObject.toJSONString());
            String body=HttpClientUtil.doPostJson(response_URL,jsonObject.toJSONString());
            logger.info("快速响应算法接收返回"+body);
            if(StringUtils.hasText(body)){
                JSONObject json=JSON.parseObject(body);
                Integer status= json.getInteger("status");
                Map<String,String> map=new HashMap<>();
                map.put("o_id",order.getSourceOrderId());

                map.put("message",json.getString("suggest"));
                map.put("code",status.toString());
                String jsonString=JSON.toJSONString(map);
                logger.info("运力检测乘客服务系统发送参数"+jsonString);
                String resultBody= HttpClientUtil.doPostJson(capacity_url,jsonString);
                logger.info("运力检测乘客服务系统返回结果"+resultBody);


            }else{
                result.setData("");
            }

        }
        return  result;



    }




}
