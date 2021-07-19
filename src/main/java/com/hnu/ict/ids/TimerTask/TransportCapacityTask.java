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
    TdCarryingLogEndService tdCarryingLogEndService;

    @Autowired
    NetworkLogService networkLogServer;

    static  boolean bool=true;


    @Scheduled(cron = "0/5 * * * * ? ")
    public void transportCapacity() throws Exception{

        if(bool==false){
            logger.info("运力检测尚未完成，这轮循作废");
            return;
        }
        //定时机制锁定   不执行下一轮
        bool=false;
        logger.info(bool+"运力是否满足开始");
        Date stateDate=new Date();
        long time=1000*60*30*24*7+stateDate.getTime();//一周时间
        Date endDate= DateUtil.millisecondToDate(time);
        List<OrderInfo>  orderList= orderInfoService.findNotTransportCapacity(DateUtil.getCurrentTime(stateDate),DateUtil.getCurrentTime(endDate));
        logger.info("快速响应算法发送请求数量"+orderList.size());
        for (OrderInfo order:orderList){
            TdCarryingLog carryingLog=new TdCarryingLog();
            JSONObject jsonObject=new JSONObject();
            jsonObject.put("oId",order.getSourceOrderId());
            jsonObject.put("fromId",order.getBeginStationId()+"");
            jsonObject.put("toId",order.getEndStationId()+"");
            jsonObject.put("startTime",DateUtil.getCurrentTime(order.getStartTime()));
            jsonObject.put("ticketNumber",order.getTicketNumber());
            logger.info("快速响应算法发送请求"+jsonObject.toJSONString());
            String body=HttpClientUtil.doPostJson(response_URL,jsonObject.toJSONString());

            carryingLog.setSourceOrderId(order.getSourceOrderId());
            carryingLog.setAlgorithmRequset(jsonObject.toJSONString());

            logger.info("====快速响应算法接收返回======"+body);
            if(StringUtils.hasText(body)){
                JSONObject json=JSON.parseObject(body);
                Integer status= json.getInteger("status");
                //添加逆向还是正向
                Integer direction=json.getInteger("direction");
                order.setDirection(direction);
                Long times=new Date().getTime()-order.getCreateTime().getTime();
                Integer timeNumber=times.intValue()/1000;
                logger.info("时============================间"+timeNumber);
                order.setTimeNumber(timeNumber);

                Map<String,String> map=new HashMap<>();
                map.put("o_id",order.getSourceOrderId());
                String message=json.getString("suggest");
                map.put("message",message);

                carryingLog.setAlgorithmCode(status.toString());
                carryingLog.setAlgorithmMessage(message);
                carryingLog.setAlgorithmTime(DateUtil.getCurrentTime(new Date()));

                if(status!=201) {
                    map.put("code", "301");
                }else{
                    map.put("code", "201");
                }

                OrderInfoHistotry orderInfoHistotry=new OrderInfoHistotry();
                BeanUtils.copyProperties(order,orderInfoHistotry);
                orderInfoHistotry.setId(null);
                orderInfoHistotry.setCreateTime(new Date());
                //更新订单数据
                if(status==201){
                    order.setStatus(PushStatusEnum.SUCCESS.getValue());//运力检测  预约成功
                    order.setOrderStatus(2);

                    orderInfoHistotry.setOrderStatus(2);
                    orderInfoHistotry.setOrderStatusName("运力检测预约成功 ");
                }
                if(status!=201){
                    order.setStatus(PushStatusEnum.FAIL.getValue());//运力检测  预约失败
                    order.setOrderStatus(3);

                    orderInfoHistotry.setOrderStatus(3);
                    orderInfoHistotry.setOrderStatusName("运力检测预约失败");
                }
                order.setMessage(message);
                orderInfoService.updateById(order);
                logger.info("运力检测流程节点保存"+orderInfoHistotry.toString());
                orderInfoHistotryService.insert(orderInfoHistotry);


                String jsonString=JSON.toJSONString(map);
                logger.info("==== 运力检测乘客服务系统发送参数======"+jsonString);

                carryingLog.setConsumerRequset(jsonString);

                //获取最新order数据内容
                OrderInfo orderInfo= orderInfoService.getById(order.getId().intValue());
                orderInfo.setPushNumber(order.getPushNumber()+1);
                try {
                    String resultBody= HttpClientUtil.doPostJson(capacity_URL,jsonString);
                    logger.info("运力检测乘客服务系统返回结果"+resultBody);
                    JSONObject resulJson=JSON.parseObject(resultBody);

                    String code= resulJson.getString("code");
                    carryingLog.setConsumerResult(resultBody);
                    carryingLog.setConsumerCode(code);
                    carryingLog.setCreateTime(DateUtil.getCurrentTime(new Date()));
                    tdCarryingLogEndService.insert(carryingLog);




                    if(status!=201){
                        if(code.equals("00007")){
                            logger.info("订单运力通知乘客服务失败 累加推送次数"+PushStatusEnum.FAIL.getValue()+"alkngkjsdjaigdjhnais");

                            orderInfo.setPushStatus(PushStatusEnum.FAIL.getValue());
                            order.setOrderStatus(5);

                            BeanUtils.copyProperties(order,orderInfoHistotry);
                            orderInfoHistotry.setId(null);
                            orderInfoHistotry.setCreateTime(new Date());
                            orderInfoHistotry.setOrderStatus(5);
                            orderInfoHistotry.setOrderStatusName("运力乘客服务系统接收失败");
                            orderInfoService.updateById(orderInfo);
                            orderInfoHistotryService.insert(orderInfoHistotry);
                        }


                        if(code.equals("00008")){
                            logger.info("订单取消预约失败流程结束  添加流程节点日志");
                            order.setOrderStatus(5);

                            BeanUtils.copyProperties(order,orderInfoHistotry);
                            orderInfoHistotry.setId(null);
                            orderInfoHistotry.setCreateTime(new Date());
                            orderInfoHistotry.setOrderStatus(4);
                            orderInfoHistotry.setOrderStatusName("运力乘客服务系统接收成功");
                            orderInfoService.updateById(orderInfo);
                            orderInfoHistotryService.insert(orderInfoHistotry);
                        }
                        logger.info("运力检测发送成功客服系统  运力预约失败 流程节点保存"+orderInfoHistotry.toString());

                    }

                    if(201==status) {
                        //代表运力检测成功  修改
                        orderInfo.setPushNumber(order.getPushNumber()+1);
                        if(code.equals("00007")){
                            logger.info("订单运力通知乘客服务失败");
                            //设置推送失败

                            orderInfo.setPushStatus(PushStatusEnum.FAIL.getValue());
                            order.setOrderStatus(5);

                            BeanUtils.copyProperties(order,orderInfoHistotry);
                            orderInfoHistotry.setId(null);
                            orderInfoHistotry.setCreateTime(new Date());
                            orderInfoHistotry.setOrderStatus(5);
                            orderInfoHistotry.setOrderStatusName("运力乘客服务系统接收失败");
                        }

                        if(code.equals("00008")){
                            //设置推送成功
                            order.setOrderStatus(4);

                            orderInfo.setPushStatus(PushStatusEnum.SUCCESS.getValue());
                            BeanUtils.copyProperties(order,orderInfoHistotry);
                            orderInfoHistotry.setId(null);
                            orderInfoHistotry.setCreateTime(new Date());
                            orderInfoHistotry.setOrderStatus(4);
                            orderInfoHistotry.setOrderStatusName("运力乘客服务系统接收成功");
                        }
                        orderInfoService.updateById(orderInfo);
                        orderInfoHistotryService.insert(orderInfoHistotry);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    logger.info("推送信息网络异常 发送预警信息");
                    //网络请求存在异常  捕获后进行预警  推送乘客服务系统时
                    if(status==201){
                        orderInfo.setStatus(PushStatusEnum.SUCCESS.getValue());
                    }
                    if(status!=201){
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
        //定时机制锁定解除   不执行下一轮
        bool=true;
        logger.info(bool+"运力是否满足结束");

    }


}
