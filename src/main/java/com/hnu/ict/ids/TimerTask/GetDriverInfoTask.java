package com.hnu.ict.ids.TimerTask;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hnu.ict.ids.async.DiriverinfoUpdateAsync;
import com.hnu.ict.ids.bean.DiriverinfoUpdateBeanAsync;
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

import java.util.ArrayList;
import java.util.List;

@Component
@EnableScheduling
public class GetDriverInfoTask {

    Logger logger= LoggerFactory.getLogger(GetDriverInfoTask.class);

    @Value("${big.data.getDriverInfo.url}")
    private String URL;


    @Autowired
    IvsAppUserInfoService ivsAppUserInfoService;

    @Autowired
    DiriverinfoUpdateAsync diriverinfoUpdateAsync;

    //0 0 0/6 * * ?
    @Scheduled(cron = "0 5 * * * ?")
    public void getCarInfo() throws Exception {
        StringBuffer urlInfo=new StringBuffer(URL).append("?paging=false");
        String body= HttpClientUtil.doGet(urlInfo.toString());
        JSONObject object=JSONObject.parseObject(body);
        logger.info("==========获取司机信息============"+body);
        if(object.getBoolean("success")==true){
            //解析data   key值
            JSONObject dataJson=object.getJSONObject("data");
            //解析车数组内容
            JSONArray carJson= dataJson.getJSONArray("result");
            List<DiriverinfoUpdateBeanAsync> list=new ArrayList<>();
            for (int i=0;i<carJson.size();i++){
                JSONObject json=carJson.getJSONObject(i);
                if(json.getString("driverId")==null){
                    return ;
                }
                //调用算法接口  发送司机最新数据
                DiriverinfoUpdateBeanAsync diriverinfoUpdateBeanAsync=new DiriverinfoUpdateBeanAsync();
                diriverinfoUpdateBeanAsync.setU_id(json.getLong("driverId").intValue());
                diriverinfoUpdateBeanAsync.setU_name(json.getString("driverName"));
                list.add(diriverinfoUpdateBeanAsync);


                //根据id查询司机是否存在  存在修改   不存在新增
                IvsAppUserInfo user= ivsAppUserInfoService.getById(json.getLong("driverId").intValue());
                if(user==null){
                    user=new IvsAppUserInfo();
                    user.setUId(json.getLong("driverId").intValue());
                    user.setUName(json.getString("driverName"));
                    user.setNumber(json.getString("driverEmpNo"));
                    user.setOpenid(json.getString("driverEmpNo"));
                    user.setPhone(json.getString("mobile"));
                    user.setType(3);
                    if(json.getString("gmtModified")!=null){
                        user.setLastLoginTime(DateUtil.strToDate(json.getString("gmtModified")));
                    }

                    ivsAppUserInfoService.insert(user);
                }else{
                    user.setUName(json.getString("driverName"));
                    user.setNumber(json.getString("driverEmpNo"));
                    user.setOpenid(json.getLong("driverId").toString());
                    user.setPhone(json.getString("mobile"));
                    user.setType(3);
                    if(json.getString("gmtModified")!=null){
                        user.setLastLoginTime(DateUtil.strToDate(json.getString("gmtModified")));
                    }
                    ivsAppUserInfoService.updateById(user);
                }
            }
            if(list.size()>0){
                diriverinfoUpdateAsync.diriverinfoUpdate(list);
            }
        }


    }
}
