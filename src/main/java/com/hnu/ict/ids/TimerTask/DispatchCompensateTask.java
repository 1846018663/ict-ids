package com.hnu.ict.ids.TimerTask;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.hnu.ict.ids.bean.*;
import com.hnu.ict.ids.entity.*;
import com.hnu.ict.ids.exception.NetworkEnum;
import com.hnu.ict.ids.service.*;
import com.hnu.ict.ids.utils.DateUtil;
import com.hnu.ict.ids.utils.HttpClientUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 *补偿  回调乘客服务系统预约成功接口
 */

@Component
@EnableScheduling
public class DispatchCompensateTask {

    Logger logger= LoggerFactory.getLogger(DispatchCompensateTask.class);


    @Value("${passenger.service.callback.url}")
    private String callback_URL;

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

    @Autowired
    TravelTicketInfoService travelTicketInfoService;



    @Value("${travel.algorithm.seat.url}")
    private String seat_url;

//   @Scheduled(cron = "0/1 * * * * ?")
    public void compensates() throws Exception{
       logger.info("行程订单补偿");
        //查询同步失败订单信息
        List<TravelInfo>  travelInfoList=travelInfoService.findeNotPushStatus();

        List<OrderInfo> orderInoList=orderInfoService.findCompensatesOrderIinfo();
        ArrayList<CustomerTravelRequset> list=new ArrayList<>();
        if(travelInfoList!=null && travelInfoList.size()>0){
            for(TravelInfo travelInfo:travelInfoList){
                CustomerTravelRequset customerHttpAPIBean=new CustomerTravelRequset();
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
                List<CustomerTicketInfoRequset> ticketInfoList=new ArrayList<>();
                for (OrderInfo info:orderInfoList ){
                    List<OrderUserLink> ticket_info =orderUserLinkService.findOrderNo(info.getOrderNo());
                    CustomerTicketInfoRequset ticketInfo=new CustomerTicketInfoRequset();
                    ticketInfo.setO_id(info.getSourceOrderId());
                    List<Tickets> ticketsList=new ArrayList<>();
                    for (OrderUserLink user:ticket_info){
                        Tickets tickets=new Tickets();
                        //根据订单查出乘客user_id   通过user_id与行程查询用户座位号
                        TravelTicketInfo travelTicketInfo= travelTicketInfoService.findTraveIdSeat(travelInfo.getTravelId(),user.getUserId().intValue());
                        if(travelTicketInfo!=null){
                            tickets.setU_id(travelTicketInfo.getUserId().intValue());
                            tickets.setSeat_number(travelTicketInfo.getSeatNum());
                            ticketsList.add(tickets);
                        }

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




}
