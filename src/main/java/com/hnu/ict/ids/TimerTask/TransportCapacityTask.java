package com.hnu.ict.ids.TimerTask;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.hnu.ict.ids.bean.*;
import com.hnu.ict.ids.entity.*;
import com.hnu.ict.ids.exception.NetworkEnum;
import com.hnu.ict.ids.exception.PushStatusEnum;
import com.hnu.ict.ids.service.*;
import com.hnu.ict.ids.service.impl.NetworkLogServerImpl;
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

import java.util.*;


/**
 * 运力是否满足检测  并回调乘客服务系统
 */
@Component
@EnableScheduling
public class TransportCapacityTask {

    Logger logger= LoggerFactory.getLogger(TransportCapacityTask.class);


    @Value("${passenger.service.capacity.test.url}")
    private String capacity_URL;

    @Value("${travel.algorithm.response.url}")
    private String response_URL;

    @Value("${passenger.service.early.warning.push.url}")
    private String push_URL;

    @Value("${passenger.service.app_id}")
    private String APP_ID;

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


    @Scheduled(cron = "0 0/2 * * * ?")
    public void transportCapacity() throws Exception{
        logger.info("======运力检测开始=======");
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
            logger.info("====快速响应算法接收返回======"+body);
            if(StringUtils.hasText(body)){
                JSONObject json=JSON.parseObject(body);
                Integer status= json.getInteger("status");

                Map<String,String> map=new HashMap<>();
                map.put("o_id",order.getSourceOrderId());
                String message=json.getString("suggest");
                map.put("message",message);
                map.put("code",status.toString());
                String jsonString=JSON.toJSONString(map);
                logger.info("==== 运力检测乘客服务系统发送参数======"+jsonString);
                //更新订单数据
                if(status==201){
                    order.setStatus(PushStatusEnum.SUCCESS.getValue());//运力检测  预约成功
                }
                if(status==301){
                    order.setStatus(PushStatusEnum.FAIL.getValue());//运力检测  预约失败
                }
                order.setMessage(message);
                orderInfoService.updateById(order);
                //获取最新order数据内容
                OrderInfo orderInfo= orderInfoService.getById(order.getId().intValue());
                try {
                    String resultBody= HttpClientUtil.doPostJson(capacity_URL,jsonString);
                    logger.info("运力检测乘客服务系统返回结果"+resultBody);
                    JSONObject resulJson=JSON.parseObject(resultBody);
                    String code= resulJson.getString("code");

                    if(status==301){
                        if(code.equals("00007")){
                            logger.info("订单通知乘客服务失败 累加推送次数");
                            orderInfo.setPushNumber(order.getPushNumber()+1);
                            orderInfo.setPushStatus(PushStatusEnum.FAIL.getValue());
                        }

                        orderInfoService.updateById(order);

                        if(code.equals("00008")){
                            logger.info("订单取消成功  移除信息，保存订单日志");
                            OrderInfoHistotry orderInfoHistotry=new OrderInfoHistotry();
                            BeanUtils.copyProperties(orderInfo,orderInfoHistotry);
                            //运力不足取消订单
                            orderInfoHistotryService.insert(orderInfoHistotry);
                            orderInfoService.deleteBySourceOrderId(orderInfo.getSourceOrderId());
                        }

                    }

                    if(201==status) {
                        //代表运力检测成功  修改
                        orderInfo.setPushNumber(order.getPushNumber()+1);
                        if(code.equals("00007")){
                            logger.info("订单通知乘客服务失败");
                            //设置推送失败
                            orderInfo.setPushStatus(PushStatusEnum.FAIL.getValue());
                        }
                        if(code.equals("00008")){
                            //设置推送成功
                            orderInfo.setPushStatus(PushStatusEnum.SUCCESS.getValue());
                        }
                        orderInfoService.updateById(orderInfo);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    logger.info("推送信息网络异常 发送预警信息");
                    //网络请求存在异常  捕获后进行预警  推送乘客服务系统时
                    if(status==201){
                        orderInfo.setStatus(PushStatusEnum.SUCCESS.getValue());
                    }
                    if(status==301){
                        orderInfo.setStatus(PushStatusEnum.FAIL.getValue());
                    }
                    orderInfo.setPushNumber(orderInfo.getPushNumber()+3);
                    orderInfo.setPushStatus(PushStatusEnum.FAIL.getValue());
                    orderInfoService.updateById(orderInfo);

                    //调用预警通知
                    EarlyWarning earlyWarning=new EarlyWarning();
                    earlyWarning.setApp_id(APP_ID);
                    earlyWarning.setTitle("运力是否满足回调失败 网络请求异常");
                    earlyWarning.setOcassion("order_call_back_failed");
                    earlyWarning.setBiz_id(order.getSourceOrderId());
                    OrderCallBackFailed orderCallBackFailed=new OrderCallBackFailed();
                    orderCallBackFailed.setO_id(order.getSourceOrderId());
                    orderCallBackFailed.setTime(DateUtil.strToDayDate(new Date()));
                    orderCallBackFailed.setRetry_count(4);
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
                        resultBody = HttpClientUtil.doPostJson(push_URL,httpJson);
                        networkLog.setResponseResult(resultBody);
                        networkLog.setStatus(NetworkEnum.STATUS_SUCCEED.getValue());
                    } catch (Exception exception) {
                        exception.printStackTrace();
                        networkLog.setStatus(NetworkEnum.STATUS_FAILED.getValue());
                    }
                    //保存接口日志
                    networkLogServer.insertNetworkLog(networkLog);
                }



            }

        }
        logger.info("==============运力检测结束");
    }


}
