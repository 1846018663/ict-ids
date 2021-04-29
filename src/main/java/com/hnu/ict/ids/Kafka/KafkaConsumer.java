package com.hnu.ict.ids.Kafka;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.hnu.ict.ids.WebSocket.WebSocketServer;
import com.hnu.ict.ids.entity.*;
import com.hnu.ict.ids.service.*;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.checkerframework.checker.units.qual.A;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class KafkaConsumer {

    Logger logger= LoggerFactory.getLogger(KafkaProducera.class);

    @Value("${map.redis.car.positioning}")
    private String carPositioning;

    @Value("${map.redis.car.travel.status}")
    private String carTravelStatus;

    @Value("${map.redis.car.suspend}")
    private String carSuspend;

    @Value("${map.redis.car.suspend.end}")
    private String carSuspendEnd;

    @Value("${map.redis.car.fault}")
    private String carFault;

    @Value("${map.redis.car.state}")
    private String carState;


    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    WebSocketServer webSocketServer;


    @Autowired
    TdCarTravelInfoService tdCarTravelInfoService;
    @Autowired
    TdCarOutboundInfoService tdCarOutboundInfoService;
    @Autowired
    TdCarSuspendInfoService tdCarSuspendInfoService;
    @Autowired
    TdCarSuspendEndService tdCarSuspendEndService;
    @Autowired
    TdCarFaultInfoService tdCarFaultInfoService;
    @Autowired
    TdCarStateInfoService tdCarStateInfoService;

    /**
     * 1 、获取车辆定位信息
     */
    @KafkaListener(topics = "vms_car_location")
    public void vmsCarLocation (ConsumerRecord<?, ?> record) throws Exception {
        //解析数据并且保存redis中
        JSONObject jsonObject= JSON.parseObject( record.key().toString());
        JSONObject json=JSON.parseObject(record.value().toString());
        String carId=jsonObject.getLong("vehicleId").toString();
        redisTemplate.opsForValue().set(carPositioning+carId,json);

        //并且推送
        webSocketServer.sendMessageAll(jsonObject.toString());
        JSONObject value= (JSONObject)redisTemplate.opsForValue().get(carPositioning+carId);
    }





    /**
     * 2、车辆行程状态变更
     */
    @KafkaListener(topics = "vms_trip_status_change_info")
    public void vmsTripStatusChangeInfo(ConsumerRecord<?, ?> record) throws Exception {
        logger.info("消费---vms_trip_status_change_info---车辆行程状态变更");
        logger.info(record.value().toString());

        //对其车辆信息进行解析并保存数据库
        JSONObject jsonObject= JSON.parseObject( record.value().toString());
        TdCarTravelInfo tdCarTravelInfo = JSON.toJavaObject(jsonObject,TdCarTravelInfo.class);
        tdCarTravelInfo.setCreateTime(new Date());
        tdCarTravelInfoService.insert(tdCarTravelInfo);

        //保存redis一份数据
        String travelId=jsonObject.getString("tripNo");
        redisTemplate.opsForValue().set(carTravelStatus+travelId,jsonObject);

        //接口调用通知算法
    }


    /**
     *  3、 车辆进出站信息
     */
    @KafkaListener(topics = "vms_in_and_out_station")
    public void vmsInAndOutStation (ConsumerRecord<?, ?> record) throws Exception {
        logger.info("消费---vms_in_and_out_station---车辆进出站信息");
        logger.info(record.value().toString());

        //对其车辆信息进行解析并保存数据库
        JSONObject jsonObject= JSON.parseObject( record.value().toString());
        TdCarOutboundInfo tdCarOutboundInfo = JSON.toJavaObject(jsonObject, TdCarOutboundInfo.class);
        tdCarOutboundInfo.setCreateTime(new Date());
        tdCarOutboundInfoService.insert(tdCarOutboundInfo);

        //保存redis一份数据
        String travelId=jsonObject.getString("tripNo");
        String stationNo=jsonObject.getString("stationNo");
        redisTemplate.opsForValue().set(carTravelStatus+"_"+stationNo,jsonObject);


        //通知同济算法  接口调用逻辑

    }


    /**
     * 4、预约暂离信息
     */
    @KafkaListener(topics = "vms_temporary_leave_req")
    public void vmsTemporaryTeaveReq(ConsumerRecord<?, ?> record) throws Exception  {
        logger.info("消费---vms_temporary_leave_req---预约暂离信息");
        logger.info(record.value().toString());
        //对其车辆信息进行解析并保存数据库
        JSONObject jsonObject= JSON.parseObject( record.value().toString());
        TdCarSuspendInfo tdCarSuspendInfo = JSON.toJavaObject(jsonObject, TdCarSuspendInfo.class);
        tdCarSuspendInfo.setCreateTime(new Date());
        tdCarSuspendInfoService.insert(tdCarSuspendInfo);

        //保存redis一份数据
        String vehicleId=jsonObject.getString("vehicleId");
        redisTemplate.opsForValue().set(carSuspend+vehicleId,jsonObject);

        //通知算法  调用接口

    }



    /**
    *  5、  结束暂离信息
     */
    @KafkaListener(topics = "vms_temporary_leave_end")
    public void vmsTemporaryLeaveEnd(ConsumerRecord<?, ?> record) throws Exception  {
        logger.info("消费---vms_temporary_leave_end---结束暂离信息");
        logger.info(record.value().toString());

        //对其车辆信息进行解析并保存数据库
        JSONObject jsonObject= JSON.parseObject( record.value().toString());
        TdCarSuspendEnd tdCarSuspendEnd = JSON.toJavaObject(jsonObject, TdCarSuspendEnd.class);
        tdCarSuspendEnd.setCreateTime(new Date());
        tdCarSuspendEndService.insert(tdCarSuspendEnd);

        //保存redis一份数据
        String vehicleId=jsonObject.getString("vehicleId");
        redisTemplate.opsForValue().set(carSuspendEnd+vehicleId,jsonObject);

        //通知算法  调用接口
    }


    /**
     * 6、 车辆故障（停止运营）信息
     */
    @KafkaListener(topics = "vms_car_fault")
    public void vmsCarFault(ConsumerRecord<?, ?> record) throws Exception  {
        logger.info("消费---vms_car_fault---车辆故障（停止运营）信息");
        logger.info(record.value().toString());
        //对其车辆信息进行解析并保存数据库
        JSONObject jsonObject= JSON.parseObject( record.value().toString());
        TdCarFaultInfo tdCarFaultInfo = JSON.toJavaObject(jsonObject, TdCarFaultInfo.class);
        tdCarFaultInfo.setCreateTime(new Date());
        tdCarFaultInfoService.insert(tdCarFaultInfo);

        //保存redis一份数据
        String vehicleId=jsonObject.getString("vehicleId");
        redisTemplate.opsForValue().set(carFault+vehicleId,jsonObject);

        //通知算法  调用接口

    }


    /**
     * 7、 车辆状态信息，预留
     */
    @KafkaListener(topics = "vms_car_state")
    public void vmsCarState(ConsumerRecord<?, ?> record) throws Exception  {
        logger.info("消费---vms_car_state fault---车辆状态信息，预留");
        logger.info(record.value().toString());
        //对其车辆信息进行解析并保存数据库
        JSONObject jsonObject= JSON.parseObject( record.value().toString());
        TdCarStateInfo tdCarStateInfo = JSON.toJavaObject(jsonObject, TdCarStateInfo.class);
        tdCarStateInfo.setCreateTime(new Date());
        tdCarStateInfoService.insert(tdCarStateInfo);

        //保存redis一份数据
        String vehicleId=jsonObject.getString("vehicleId");
        redisTemplate.opsForValue().set(carState+vehicleId,jsonObject);

        //通知算法  调用接口

    }


    /**
     * 7、 行程数据
     */
  //  @KafkaListener(topics = "dms_trip_dispatching")
    public void getTripInfo(ConsumerRecord<?, ?> record) throws Exception {
        logger.info("消费---dms_trip_dispatching---行程数据，预留");
        logger.info("++++++++++++++++++topic = %s, offset = %d, value = %s \n", record.topic(), record.offset(), record.value());
        logger.info("dms_trip_dispatching=====key===="+record.key());
        logger.info("dms_trip_dispatching====value====="+record.value());
    }

}
