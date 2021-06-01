package com.hnu.ict.ids.TimerTask;


import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hnu.ict.ids.async.UpdateCarAsync;
import com.hnu.ict.ids.bean.TravelCarRequest;
import com.hnu.ict.ids.entity.IvsAppCarInfo;
import com.hnu.ict.ids.service.IvsAppCarInfoService;
import com.hnu.ict.ids.utils.HttpClientUtil;
import org.checkerframework.checker.units.qual.A;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;


@Component
@EnableScheduling
public class GetCarInfoTask{


    Logger logger= LoggerFactory.getLogger(DispatchTask.class);

    @Value("${big.data.getCarInfo.url}")
    private String URL;


    @Autowired
    IvsAppCarInfoService ivsAppCarInfoService;

    @Autowired
    UpdateCarAsync updateCarAsync;


    @Scheduled(cron = "0 0/2 * * * ?")
    public void getCarInfo() throws Exception {
        //网络请求获取全量车辆信息
        StringBuffer urlInfo=new StringBuffer(URL).append("?paging=false");
        String body=HttpClientUtil.doGet(urlInfo.toString());

        //解析车辆信息集合并做逻辑处理
        JSONObject object=JSONObject.parseObject(body);
        if(object.getBoolean("success")==true){
            JSONObject dataJson=object.getJSONObject("data");
            //解析车数组内容
           JSONArray carJson= dataJson.getJSONArray("result");
           List<TravelCarRequest> carList=new ArrayList<>();
           for (int i=0;i<carJson.size();i++){
               JSONObject json=carJson.getJSONObject(i);
               IvsAppCarInfo car= ivsAppCarInfoService.getByCarId(json.getInteger("vehicleId"));
               //异步不同步集合数据对象
               String code=json.getString("areaCode");
               if(code.equals("1002")) {
                   carList.add(intoTravelCarRequest(json));
               }
               updateData(car ,json);

           }

           //异步同步数据
           if(carList!=null && carList.size()>0){
               updateCarAsync.updateCar(carList);
           }
        }
    }


    /**
     * 初始化  同步算法车辆对象信息封装
     * @param json
     * @return
     */
    public TravelCarRequest intoTravelCarRequest(JSONObject json){
        TravelCarRequest carRequest=new TravelCarRequest();
        carRequest.setCarId(json.getInteger("vehicleId").toString());
        carRequest.setCarType(json.getInteger("carType"));
        carRequest.setCityType(Integer.parseInt(json.getString("areaCode")));
        carRequest.setCarSeatNumber(json.getInteger("seatNum"));
        carRequest.setCarOperatonStatus(json.getInteger("isDeleted"));

        return  carRequest;
    }


    /**
     * 对数据库车辆信息内容更新
     * @param car
     * @param json
     */
    public void updateData(IvsAppCarInfo car ,JSONObject json){
        if(car==null){
            car=new IvsAppCarInfo();
            car.setCId(json.getInteger("vehicleId"));
            car.setCarType(json.getInteger("carType"));
            car.setCarSeatNumber(json.getInteger("seatNum"));
            car.setLicenseNumber(json.getString("plateNo"));
            car.setCStatus(null);
            ivsAppCarInfoService.insert(car);
        }else{
            car.setCId(json.getInteger("vehicleId"));
            car.setCarType(json.getInteger("carType"));
            car.setCCode(json.getString("areaCode"));
            car.setCarSeatNumber(json.getInteger("seatNum"));
            car.setLicenseNumber(json.getString("plateNo"));
            ivsAppCarInfoService.update(car);
        }
    }


}
