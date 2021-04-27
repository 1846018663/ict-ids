package com.hnu.ict.ids.TimerTask;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hnu.ict.ids.entity.IvsAppUserInfo;
import com.hnu.ict.ids.service.IvsAppUserInfoService;
import com.hnu.ict.ids.utils.DateUtil;
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
public class GetDriverInfoTask {

    Logger logger= LoggerFactory.getLogger(GetDriverInfoTask.class);

    @Value("${big.data.getDriverInfo.url}")
    private String URL;

    @Autowired
    IvsAppUserInfoService ivsAppUserInfoService;


//    @Scheduled(cron = "0/55 * * * * ?")
    public void getCarInfo() throws Exception {
        String body= HttpClientUtil.doGet(URL);
        JSONObject object=JSONObject.parseObject(body);
        logger.info("==========获取司机信息============"+body);
        if(object.getBoolean("success")==true){
            //解析data   key值
            JSONObject dataJson=object.getJSONObject("data");
            //解析车数组内容
            JSONArray carJson= dataJson.getJSONArray("result");
            for (int i=0;i<carJson.size();i++){
                JSONObject json=carJson.getJSONObject(i);
                if(json.getString("gmtCreate")==null){
                    return ;
                }
                //根据id查询车辆是否存在  存在修改   不存在新增
                IvsAppUserInfo user= ivsAppUserInfoService.findNumber(json.getString("driverEmpNo"));
                if(user==null){
                    user=new IvsAppUserInfo();
                    user.setUName(json.getString("driverName"));
                    user.setNumber(json.getString("driverEmpNo"));
                    user.setOpenid(json.getLong("driverId").toString());
                    user.setPhone(json.getString("mobile"));
                    user.setType(3);
                    user.setLastLoginTime(DateUtil.strToDate(json.getString("gmtModified")));
                    ivsAppUserInfoService.insert(user);
                }else{
                    user.setUName(json.getString("driverName"));
                    user.setNumber(json.getString("driverEmpNo"));
                    user.setOpenid(json.getLong("driverId").toString());
                    user.setPhone(json.getString("mobile"));
                    user.setType(3);
                    user.setLastLoginTime(DateUtil.strToDate(json.getString("gmtModified")));
                    ivsAppUserInfoService.updateById(user);
                }
            }
        }


    }
}
