package com.hnu.ict.ids.Kafka;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.hnu.ict.ids.WebSocket.WebSocketServer;
import com.hnu.ict.ids.bean.CarStatusUpRequset;
import com.hnu.ict.ids.bean.TdDriverAttendance;
import com.hnu.ict.ids.entity.*;
import com.hnu.ict.ids.exception.CarTypeEnum;
import com.hnu.ict.ids.service.*;
import com.hnu.ict.ids.utils.DateUtil;
import com.hnu.ict.ids.utils.HttpClientUtil;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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


    @Value("${travel.algorithm.carInfo.statusUp.url}")
    private String carStatusUpUrl;

    @Value("${sz.travel.algorithm.carInfo.statusUp.url}")
    private String szcarStatusUpUrl;


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
    @Autowired
    IvsAppCarInfoService ivsAppCarInfoService;
    @Autowired
    TdDriverAttendanceLogService tdDriverAttendanceLogService;
    @Autowired
    TravelInfoService travelInfoService;
    @Autowired
    TravelInfoLogService travelInfoLogService;

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
        JSONObject res= (JSONObject) redisTemplate.opsForValue().get(carPositioning+carId);
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

        TravelInfo  info=travelInfoService.findTravelId(tdCarTravelInfo.getTripNo());
        if(info!=null){
            info.setUpdateTime(new Date());
            //历史轨迹节点保存
            TravelInfoLog travelInfoLog=new TravelInfoLog();
            BeanUtils.copyProperties(info,travelInfoLog);
            travelInfoLog.setId(null);
            travelInfoLog.setCreateTime(new Date());
            if(tdCarTravelInfo.getStatus()==1){//行程开始
                travelInfoLog.setTravelStatus(2);
                travelInfoLog.setTravelStatusName("进行中");
                info.setTravelStatus(2);
            }

            if(tdCarTravelInfo.getStatus()==2){//行程开始
                travelInfoLog.setTravelStatus(3);
                travelInfoLog.setTravelStatusName("已完成");
                info.setTravelStatus(3);
            }

            travelInfoService.updateById(info);
            travelInfoLogService.insert(travelInfoLog);

        }

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
     * 8、 司机登陆数据
     */
    @KafkaListener(topics = "vms_driver_attendance")
    public void driverAttendance(ConsumerRecord<?, ?> record) throws Exception {
        logger.info("消费---vms_driver_attendance---司机登入登出"+record.value().toString());
        String regEx="[\n`~!@#$%^&*()+=|';'\\[\\].<>/?~！@#￥%……&*（）——+|【】‘；：”“’。， 、？]";
        //可以在中括号内加上任何想要替换的字符，实际上是一个正则表达式
        String aa = " ";//这里是将特殊字符换为aa字符串," "代表直接去掉
        String str=record.value().toString().replaceAll(regEx,aa);
        JSONObject jsonObject= JSONObject.parseObject(str);
        TdDriverAttendance tdDriverAttendance = new TdDriverAttendance();
        TdDriverAttendanceLog tdDriverAttendanceLog=new TdDriverAttendanceLog();

        tdDriverAttendance.setDriverId(jsonObject.getLong("driverId"));
        tdDriverAttendanceLog.setDriverId(jsonObject.getLong("driverId").toString());

        tdDriverAttendance.setDriverEmpNo(jsonObject.getString("driverEmpNo"));
        tdDriverAttendanceLog.setDriverEmpNo(jsonObject.getString("driverEmpNo"));

        tdDriverAttendance.setPlateNo(jsonObject.getString("plateNo"));
        tdDriverAttendanceLog.setPlateNo(jsonObject.getString("plateNo"));

        tdDriverAttendance.setVehicleId(jsonObject.getLong("vehicleId"));
        tdDriverAttendanceLog.setVehicleId(jsonObject.getLong("vehicleId").toString());

        tdDriverAttendance.setTime(jsonObject.getString("time"));
        tdDriverAttendanceLog.setAttTime(jsonObject.getString("time"));

        tdDriverAttendance.setCheckType(jsonObject.getInteger("checkType"));
        tdDriverAttendanceLog.setCheckType(jsonObject.getInteger("checkType").toString());

        tdDriverAttendanceLog.setCreateTime(DateUtil.getCurrentTime(new Date()));

        tdDriverAttendanceLogService.insert(tdDriverAttendanceLog);

        //更新数据
        IvsAppCarInfo carInfo=ivsAppCarInfoService.getByCarId(tdDriverAttendance.getVehicleId().intValue());
        //该车存在修改  车辆数据表   发送至算法  小于等于6是生产   大于6是测试
        if(carInfo.getCId()>6){

            if(tdDriverAttendance.getCheckType()==1){//登入
                carInfo.setDriverId(tdDriverAttendance.getDriverId());
                carInfo.setDriverEmpNo(tdDriverAttendance.getDriverEmpNo());
                carInfo.setCheckType(tdDriverAttendance.getCheckType());
                carInfo.setTime(tdDriverAttendance.getTime());
                //登入代表车辆上线   修改状态
                carInfo.setCIsuse("1");//在线
            }else{//登出  离线
                carInfo.setDriverId(null);
                carInfo.setDriverEmpNo(null);
                carInfo.setCheckType(null);
                carInfo.setTime(null);
                //登出代表车辆离线   修改状态
                carInfo.setCIsuse("2");//离线
            }
            ivsAppCarInfoService.update(carInfo);

        }else{
            logger.info("发布在该环境处理中"+carInfo.getCId());
            return;
        }
            //发送数据给算法
            List<CarStatusUpRequset> list=new ArrayList<>();
            CarStatusUpRequset carStatusUpRequset=new CarStatusUpRequset();

            JSONObject res= (JSONObject) redisTemplate.opsForValue().get(carPositioning+carInfo.getCId());
            if(res!=null){
                logger.info("Redis数据"+res);
                BigDecimal lng=res.getBigDecimal("lng");//经度
                BigDecimal lat=res.getBigDecimal("lat");//纬度
                carStatusUpRequset.setCarLoc(lng+","+lat);
                logger.info("该车上下线经纬度"+carStatusUpRequset.getCarLoc());
            }else {
                carStatusUpRequset.setCarLoc("121.261028,31.364997");
            }
            carStatusUpRequset.setCarType(Integer.parseInt(carInfo.getCCode()));
            carStatusUpRequset.setCarId(carInfo.getCId()+"");
            if(carInfo.getCIsuse().equals("1")){
                carStatusUpRequset.setCarStatus(1);//可用
                carStatusUpRequset.setDriverId(carInfo.getDriverId().toString());
            }else{
                carStatusUpRequset.setCarStatus(0);//不可用
                carStatusUpRequset.setDriverId(" ");
            }
            carStatusUpRequset.setCarSeatNumber(carInfo.getCarSeatNumber());
            carStatusUpRequset.setCityType(carInfo.getCarType()+"");
            list.add(carStatusUpRequset);
            String json = JSON.toJSONString(list);
            logger.info("登入登出状态修改"+json);
            //网络请求
            String result=null;
            if(carInfo.getCarType()== CarTypeEnum.SZ.getValue()){
                result= HttpClientUtil.doPostJson(szcarStatusUpUrl,json);
            }
            if(carInfo.getCarType()== CarTypeEnum.SH.getValue()){
                result= HttpClientUtil.doPostJson(carStatusUpUrl,json);
            }
           JSONObject resultJson=JSONObject.parseObject(result);
           if(resultJson!=null){
               if (resultJson.getInteger("status")==1){
                   logger.info("发送数据通知算法车辆状态修改成功");
               }else{
                   logger.info("发送数据通知算法车辆状态修改失败");
               }
           }
    }

}
