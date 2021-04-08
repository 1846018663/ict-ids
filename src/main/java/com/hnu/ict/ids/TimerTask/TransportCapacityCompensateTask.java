package com.hnu.ict.ids.TimerTask;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.hnu.ict.ids.bean.EarlyWarning;
import com.hnu.ict.ids.bean.OrderCallBackFailed;
import com.hnu.ict.ids.entity.NetworkLog;
import com.hnu.ict.ids.entity.OrderInfo;
import com.hnu.ict.ids.entity.OrderInfoHistotry;
import com.hnu.ict.ids.exception.NetworkEnum;
import com.hnu.ict.ids.exception.PushStatusEnum;
import com.hnu.ict.ids.service.*;
import com.hnu.ict.ids.utils.DateUtil;
import com.hnu.ict.ids.utils.HttpClientUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * 补偿运力是否满足检测 结果 并回调乘客服务系统
 */
@Component
@EnableScheduling
public class TransportCapacityCompensateTask {

    Logger logger= LoggerFactory.getLogger(TransportCapacityCompensateTask.class);


    @Value("${passenger.service.app_id}")
    private String APP_ID;

    @Value("${passenger.service.capacity.test.url}")
    private String capacity_URL;

    @Value("${passenger.service.early.warning.push.url}")
    private String push_URL;

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
    NetworkLogService networkLogServer;


    @Scheduled(cron = "0 0/1 * * * ?")
    public void transportCapacity() throws Exception{
        logger.info("------------- 补偿运力是否满足检测 回调乘客服务系统运力是否满足接口-------------");

        List<OrderInfo>  orderList= orderInfoService.findPushFailedOrderIinfo();

        for (OrderInfo order:orderList){
                String  status=null;
                if(order.getStatus()==1){
                    status="201";
                }else{
                    status="301";
                }
                Map<String,String> map=new HashMap<>();
                map.put("o_id",order.getSourceOrderId());
                map.put("message",order.getMessage());
                map.put("code",status.toString());
                String jsonString=JSON.toJSONString(map);
                logger.info("------------ 补偿回调运力检测乘客服务系统发送参数"+jsonString);
                try {
                    String resultBody= HttpClientUtil.doPostJson(capacity_URL,jsonString);
                    logger.info("补偿回调运力检测乘客服务系统返回结果"+resultBody);
                    JSONObject resulJson=JSON.parseObject(resultBody);
                    String code= resulJson.getString("code");

                    if(status.equals("301")){
                        if(code.equals("00007")){
                            logger.info("----- 补偿运力回答   订单通知乘客服务失败 累加推送次数-----");
                            order.setPushNumber(order.getPushNumber()+1);
                            order.setPushStatus(PushStatusEnum.FAIL.getValue());
                        }

                        orderInfoService.updateById(order);

                        if(code.equals("00008")){
                            logger.info("----------补偿运力 订单取消成功  移除信息，保存订单日志 -------");
                            OrderInfoHistotry orderInfoHistotry=new OrderInfoHistotry();
                            BeanUtils.copyProperties(order,orderInfoHistotry);
                            //运力不足取消订单
                            orderInfoHistotryService.insert(orderInfoHistotry);
                            orderInfoService.deleteBySourceOrderId(order.getSourceOrderId());
                        }

                    }

                    if(status.equals("201")) {
                        //代表运力检测成功  修改
                        order.setPushNumber(order.getPushNumber()+1);
                        if(code.equals("00007")){
                            logger.info("---- 补偿回调运力  订单通知乘客服务失败-------");
                            //设置推送失败
                            order.setPushStatus(PushStatusEnum.FAIL.getValue());
                        }
                        if(code.equals("00008")){
                            //设置推送成功
                            order.setPushStatus(PushStatusEnum.SUCCESS.getValue());
                        }
                        orderInfoService.updateById(order);
                    }


                    //查询最新数据
                    OrderInfo orderInfo=orderInfoService.getById(order.getId().intValue());
                    if(orderInfo.getPushNumber()==4){
                        //预警通知
                        earlyWarning(orderInfo,status);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    logger.info("---- 补偿回调运力  推送信息网络异常 发送预警信息  ------");
                    //网络请求存在异常  捕获后进行预警  推送乘客服务系统时
                    if(status.equals("201")){
                        order.setStatus(PushStatusEnum.SUCCESS.getValue());
                    }
                    if(status.equals("301")){
                        order.setStatus(PushStatusEnum.FAIL.getValue());
                    }
                    order.setPushNumber(order.getPushNumber()+1);
                    order.setPushStatus(PushStatusEnum.FAIL.getValue());
                    orderInfoService.updateById(order);
                    earlyWarning(order,status);

                }

        }
        logger.info("---------------------补偿运力检测结束-------------");
    }


    public void earlyWarning(OrderInfo order,String status){
        //调用预警通知
        logger.info("========发送预警通知=========");
        EarlyWarning earlyWarning=new EarlyWarning();
        earlyWarning.setApp_id(APP_ID);
        earlyWarning.setTitle("运力是否满足回调失败");
        earlyWarning.setOcassion("order_call_back_failed");
        earlyWarning.setBiz_id(order.getSourceOrderId());
        OrderCallBackFailed orderCallBackFailed=new OrderCallBackFailed();
        orderCallBackFailed.setO_id(order.getSourceOrderId());
        orderCallBackFailed.setTime(DateUtil.strToDayDate(new Date()));
        orderCallBackFailed.setRetry_count(4);
        orderCallBackFailed.setCode(status);
        orderCallBackFailed.setMessage(order.getMessage()); ;
        earlyWarning.setParams(orderCallBackFailed);

        String httpJson=JSON.toJSONString(earlyWarning);
        //保存预警信息
        NetworkLog networkLog=new NetworkLog();
        networkLog.setCreateTime(new Date());
        networkLog.setInterfaceInfo(NetworkEnum.EALY_WARNING.getValue());
        networkLog.setType(NetworkEnum.TYPE_HTTP.getValue());
        networkLog.setMethod(NetworkEnum.METHOD_POST.getValue());
        networkLog.setUrl(push_URL);
        networkLog.setAccessContent(httpJson);
        String resultBody= null;
        try {
            logger.info("========发送预警内容========="+httpJson);
            resultBody = HttpClientUtil.doPostJson(push_URL,httpJson);
            logger.info("========接收预警内容========="+resultBody);
            networkLog.setResponseResult(resultBody);
            networkLog.setStatus(NetworkEnum.STATUS_SUCCEED.getValue());
        } catch (Exception exception) {
            exception.printStackTrace();
            networkLog.setStatus(NetworkEnum.STATUS_FAILED.getValue());
        }
        //保存接口日志
        networkLogServer.insertNetworkLog(networkLog);

        logger.info("-----------补偿回调结束-------------------");
    }

}
