package com.hnu.ict.ids.Kafka;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hnu.ict.ids.entity.*;
import com.hnu.ict.ids.service.IvsAppCarInfoService;
import com.hnu.ict.ids.service.IvsAppPlatformInfoService;
import com.hnu.ict.ids.service.IvsAppUserInfoService;
import com.hnu.ict.ids.service.OrderInfoService;
import com.hnu.ict.ids.utils.DateUtil;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.swing.text.Keymap;
import java.util.*;
import java.util.concurrent.Future;

@Component
public class KafkaProducera {
    Logger logger= LoggerFactory.getLogger(KafkaProducera.class);
    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    IvsAppCarInfoService ivsAppCarInfoService;
    @Autowired
    IvsAppUserInfoService ivsAppUserInfoService;
    @Autowired
    IvsAppPlatformInfoService ivsAppPlatformInfoService;
    @Autowired
    OrderInfoService orderInfoService;

    static  int boo=0;


    // 发送消息
    public Map<String ,Object> getTripInfo(TravelInfo travelInfo ,int tripType) {
        JSONObject key=new JSONObject();
        key.put("vehicleId",travelInfo.getCarId());
        key.put("timestamp",new Date().getTime());

        JSONObject value=new JSONObject();
        value.put("tripNo",travelInfo.getTravelId());

        value.put("tripType",tripType);//行程类别，1-车列，2-包车，3-其他
        //车辆信息封装
        IvsAppCarInfo carInfo=ivsAppCarInfoService.getByCarId(travelInfo.getCarId());
        if(boo%2==1){
            value.put("vehicleId",1);
            value.put("plateNo","沪BR7072");
        }else{
            value.put("vehicleId",5);
            value.put("plateNo","沪BG3974");
        }
        boo++;
//        value.put("vehicleId",carInfo.getCId());
//        value.put("plateNo",carInfo.getLicenseNumber());
        //司机信息封装
        IvsAppUserInfo userInfo=ivsAppUserInfoService.getById(travelInfo.getDriverId());
        value.put("driverId",userInfo.getUId());
        value.put("driverEmpNo",userInfo.getNumber());
        value.put("driverName",userInfo.getUName());

        value.put("tripStartTimeEst", DateUtil.getCurrentTime(travelInfo.getStartTime()));
        //基础站台行程数据封装
        IvsAppPlatformInfo platformInfo=ivsAppPlatformInfoService.getByPlatformId(travelInfo.getBeginStationId().toString());
        String[] platform=travelInfo.getAllTravelPlat().split(",");
        String[] arrTime=travelInfo.getArriveTime().split(",");
        value.put("startLng",platformInfo.getLongitude());
        value.put("startLat",platformInfo.getLatitude());
        value.put("stationNum",platform.length);
        value.put("tripMileage",travelInfo.getDistance().intValue());
        value.put("passengerNum",travelInfo.getItNumber());
        value.put("dspTime",DateUtil.getCurrentTime(travelInfo.getCreateTime()));

        //封装站点信息列表
        JSONArray stationList=new JSONArray();
        for (int i=0;i<platform.length;i++){
            JSONObject object=new JSONObject();
            IvsAppPlatformInfo info=ivsAppPlatformInfoService.getByPlatformId(platform[i]);
            object.put("stationId",info.getPId());
            object.put("stationName",info.getPName());
            object.put("stationCode",info.getPId());
            object.put("stationType",info.getPType());
            object.put("stationSn",i);
            object.put("lng",Double.parseDouble(info.getLongitude()));
            object.put("lat",Double.parseDouble(info.getLatitude()));
            object.put("speedLimit",info.getSpeedLimit());
            object.put("rangeRadius",info.getRangeRadius());
            //根据行程查询订单   通过站台id查询为出发站台订单人数为上车人数
            int onNum=0;//上车人数
            int offNum=0;//下车人数
            List<OrderInfo> list=orderInfoService.findOrderTravelId(travelInfo.getTravelId());
            for (OrderInfo orderInfo : list){
                String startId=orderInfo.getBeginStationId().toString();
                if(info.getPId().equals(startId)){
                    onNum=onNum+1;
                }
                String endId=orderInfo.getEndStationId().toString();
                if(info.getPId().equals(endId)){
                    offNum=offNum+1;
                }
            }
            object.put("onNum",onNum);
            object.put("offNum",offNum);

            object.put("arrivalTimeEst",arrTime[i]);
            stationList.add(object);
        }
        value.put("stationList",stationList);


        Map<String ,Object> map=new HashMap<>();
        map.put("key",key);
        map.put("value",value);
        logger.info("kafka发布消息："+map);
        kafkaTemplate.send("dms_trip_dispatching",key.toJSONString(),value.toJSONString());
        return map;
    }





}
