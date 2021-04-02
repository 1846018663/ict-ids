package com.hnu.ict.ids.TimerTask;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hnu.ict.ids.bean.CustomerHttpAPIBean;
import com.hnu.ict.ids.bean.OrderTask;
import com.hnu.ict.ids.bean.TicketInfo;
import com.hnu.ict.ids.bean.Tickets;
import com.hnu.ict.ids.entity.*;
import com.hnu.ict.ids.exception.ConfigEnum;
import com.hnu.ict.ids.exception.NetworkEnum;
import com.hnu.ict.ids.mapper.OrderInfoHistotryMapper;
import com.hnu.ict.ids.mapper.OrderInfoMapper;
import com.hnu.ict.ids.service.*;
import com.hnu.ict.ids.utils.DateUtil;
import com.hnu.ict.ids.webHttp.HttpClientUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

@Component
@EnableScheduling
public class DispatchTask {

    Logger logger= LoggerFactory.getLogger(DispatchTask.class);

    @Value("${travel.algorithm.url}")
    private String URL;

    @Value("${passenger.service.callback.url}")
    private String callback_URL;

    @Value("${passenger.service.capacity.test.url}")
    private String capacity_url;

    @Value("${travel.algorithm.response.url}")
    private String response_URL;

    @Autowired
    OrderInfoService orderInfoService;

    @Autowired
    TravelInfoService travelInfoService;

    @Autowired
    RedisTemplate redisTemplate;


    @Autowired
    NetworkLogService networkLogService;

    @Autowired
    OrderUserLinkService orderUserLinkService;

    @Autowired
    OrderInfoHistotryService  orderInfoHistotryService;




    /**
     * 每隔5分钟执行一次（按照 corn 表达式规则执行）0 0/5 * * * ?    0/10 * * * * ?
     */

   // @Scheduled(cron = "0/10 * * * * ?")
    public void addTask() throws Exception {
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
                    System.out.println("时间"+resultMap.get(ConfigEnum.CARTIMECONFIG.getValue()).toString());
                    int DateTime=Integer.parseInt(resultMap.get(ConfigEnum.CARTIMECONFIG.getValue()).toString())*60;
                    task.setSet_time(DateTime);
                    list.add(task);
            }
            String json=JSON.toJSONString(list);


            System.out.println("发送请求数据"+json);
            try {
                body=HttpClientUtil.doPostJson(URL,json);
                System.out.println("接收数据"+body);
                JSONObject jsonObject=JSONObject.parseObject(body);

                int status = jsonObject.getInteger("status");

                //有效数据
                if(status==1){
                    //对数据进行解析
                    List<TravelInfo> travelInfoList=new ArrayList<>();
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
//                      info.setDriverId(object.getInteger()); //司机id
                        info.setCreateTime(new Date());
                        //获取订单与行程对应数据关联
                        String orderIds=object.getString("correspond_order_id").replace("[","").replace("]","").replace(" ","");
                        String[] ids=orderIds.split(",");
                        for (int k=0;k<ids.length;k++){
                            map.put(Integer.parseInt(ids[k]),taskerId);
                        }

                        System.out.println("解析车辆对应关系"+ orderIds);
                        System.out.println("封装"+ map.toString());
                        travelInfoList.add(info);
                    }

                    //解析数据封装后   service批量处理
                    travelInfoService.addTravelInfoList(travelInfoList,map);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        System.out.println("执行任务job1："+ new Date());
    }


    //@Scheduled(cron = "0/5 * * * * ?")
    public void compensates() throws Exception{
        //查询同步失败订单信息
        List<TravelInfo>  travelInfoList=travelInfoService.findeNotPushStatus();

        List<OrderInfo> orderInoList=orderInfoService.findCompensatesOrderIinfo();
        ArrayList<CustomerHttpAPIBean> list=new ArrayList<>();
        if(travelInfoList!=null && travelInfoList.size()>0){
            for(TravelInfo travelInfo:travelInfoList){
                CustomerHttpAPIBean customerHttpAPIBean=new CustomerHttpAPIBean();
                List<OrderInfo> orderInfoList=orderInfoService.findOrderTravelId(travelInfo.getTravelId());
                String ids="";
                for (OrderInfo ordre:orderInfoList){
                    ids=ids+ordre.getSourceOrderId()+",";
                }
                ids.substring(0,ids.length()-1);
                customerHttpAPIBean.setO_ids(ids);

                customerHttpAPIBean.setDistance(travelInfo.getDistance().doubleValue());

                customerHttpAPIBean.setTravel_id(travelInfo.getId().toString());
                customerHttpAPIBean.setExpected_time(Integer.parseInt(travelInfo.getExpectedTime()));
                customerHttpAPIBean.setAll_travel_plat(travelInfo.getAllTravelPlat());
                customerHttpAPIBean.setDriver_content(travelInfo.getDriverContent());
                customerHttpAPIBean.setC_id(travelInfo.getCarId());
                customerHttpAPIBean.setDriver_id(travelInfo.getDriverId());
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
                list.add(customerHttpAPIBean);
            }

            String json=JSON.toJSONString(list);
            logger.info("行程预约成功后的返回传参内容"+json);
            //接口访问日志操作
            NetworkLog networkLog=new NetworkLog();
            networkLog.setCreateTime(new Date());
            networkLog.setStatus(NetworkEnum.STATUS_SUCCEED.getValue());
            networkLog.setInterfaceInfo(NetworkEnum.PASSENGRT_SERVICE_CALL_BACK.getValue());
            networkLog.setMethod(NetworkEnum.METHOD_POST.getValue());
            networkLog.setUrl(callback_URL);
            networkLog.setType(NetworkEnum.TYPE_HTTPS.getValue());
            networkLog.setAccessContent(json);
            logger.info("行程预约成功后的返回传参内容"+json);
            String body="";
            try {
                body= HttpClientUtil.doPostJson(callback_URL,json);
                networkLog.setResponseResult(body);
                networkLog.setStatus(NetworkEnum.STATUS_SUCCEED.getValue());
                logger.info("行程预约成功后的返回结果:"+body);
                JSONObject resultJson=JSONObject.parseObject(body);
                logger.info("回调乘客服务系统接收参数"+resultJson.toString());
                String code=resultJson.getString("code");

                if(!code.equals("00008")){
                    //失败   数据信息做修改  失败status 为2
                    orderInfoService.updateByIdList(orderInoList,2);
                }else{
                    //失败   数据信息做修改  成功status 为1
                    orderInfoService.updateByIdList(orderInoList,1);
                }
            } catch (Exception e) {
                //失败   数据信息做修改  失败status 为2
                orderInfoService.updateByIdList(orderInoList,2);
                networkLog.setStatus(NetworkEnum.STATUS_FAILED.getValue());
                e.printStackTrace();
            }

            networkLogService.insertNetworkLog(networkLog);
        }
    }


    @Scheduled(cron = "0 0/2 * * * ?")
    public void transportCapacity() throws Exception{
        logger.info("运力检测开始");
        Date stateDate=new Date();
        long time=1000*60*30*24*7+stateDate.getTime();//一周时间
        Date endDate= DateUtil.millisecondToDate(time);
        List<OrderInfo>  orderList= orderInfoService.findNotTransportCapacity(DateUtil.getCurrentTime(stateDate),DateUtil.getCurrentTime(endDate));

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
                JSONObject resulJson=JSON.parseObject(resultBody);
                String code= resulJson.getString("code");
                if(301==status && code.equals("00008")){
                    logger.info("订单取消移除信息，保存订单日志");
                    OrderInfoHistotry orderInfoHistotry=new OrderInfoHistotry();
                    BeanUtils.copyProperties(order,orderInfoHistotry);
                    //运力不足取消订单

                    orderInfoHistotryService.insert(orderInfoHistotry);
                    orderInfoService.deleteBySourceOrderId(order.getSourceOrderId());
                }else if(201==status) {
                    //代表运力检测  修改
                    order.setStatus(1);
                    orderInfoService.updateById(order);
                }


            }

        }
        logger.info("运力检测结束");
    }


}
