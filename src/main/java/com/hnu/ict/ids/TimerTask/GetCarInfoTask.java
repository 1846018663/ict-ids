package com.hnu.ict.ids.TimerTask;


import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hnu.ict.ids.entity.IvsAppCarInfo;
import com.hnu.ict.ids.service.IvsAppCarInfoService;
import com.hnu.ict.ids.utils.HttpClientUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;


@Component
@EnableScheduling
public class GetCarInfoTask{


    Logger logger= LoggerFactory.getLogger(DispatchTask.class);

    @Value("${big.data.getCarInfo.url}")
    private String URL;


    @Autowired
    IvsAppCarInfoService ivsAppCarInfoService;

    //
    @Scheduled(cron = "0 0 0/4 * * ?")
    public void getCarInfo() throws Exception {
        StringBuffer urlInfo=new StringBuffer(URL).append("?paging=false");
        String body=HttpClientUtil.doGet(urlInfo.toString());
        JSONObject object=JSONObject.parseObject(body);
        logger.info("==========获取车辆信息============"+body);
        if(object.getBoolean("success")==true){
            //解析data   key值
            JSONObject dataJson=object.getJSONObject("data");
            //解析车数组内容
           JSONArray carJson= dataJson.getJSONArray("result");
           for (int i=0;i<carJson.size();i++){
               JSONObject json=carJson.getJSONObject(i);
               //根据id查询车辆是否存在  存在修改   不存在新增
               IvsAppCarInfo car= ivsAppCarInfoService.getByCarId(json.getInteger("vehicleId"));
               logger.info("同步车辆id"+json.getInteger("vehicleId"));
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


    }


}
