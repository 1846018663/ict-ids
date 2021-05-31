package com.hnu.ict.ids.TimerTask;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hnu.ict.ids.async.UpdateDiriverinfoAsync;
import com.hnu.ict.ids.bean.TraveDiriverinfoRequset;
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
    UpdateDiriverinfoAsync updatediriverinfoAsync;

    //0 0 0/6 * * ?
    @Scheduled(cron = "0 0/42 * * * ?")
    public void getCarInfo() throws Exception {
        //网络请求获取全量司机信息
        StringBuffer urlInfo=new StringBuffer(URL).append("?paging=false");
        String body= HttpClientUtil.doGet(urlInfo.toString());

        //解析司机信息集合并做逻辑处理
        JSONObject object=JSONObject.parseObject(body);
        if(object.getBoolean("success")==true){
            //解析data   key值
            JSONObject dataJson=object.getJSONObject("data");
            //解析车数组内容
            JSONArray carJson= dataJson.getJSONArray("result");
            List<TraveDiriverinfoRequset> diriverinfoList=new ArrayList<>();
            for (int i=0;i<carJson.size();i++){
                JSONObject json=carJson.getJSONObject(i);
                if(json.getString("driverId")==null){
                    return ;
                }
                //根据id查询司机是否存在  存在修改   不存在新增
                IvsAppUserInfo user= ivsAppUserInfoService.getById(json.getLong("driverId").intValue());

                //异步不同步集合数据对象
                diriverinfoList.add(intoTraveDiriverinfoRequset(json));
                updateData(user ,json);
            }
            if(diriverinfoList!=null && diriverinfoList.size()>0){
                updatediriverinfoAsync.diriverinfoUpdate(diriverinfoList);
            }
        }
    }


    /**
     * 初始化  同步算法司机对象信息封装
     * @param json
     * @return
     */
    public TraveDiriverinfoRequset intoTraveDiriverinfoRequset(JSONObject  json){
        TraveDiriverinfoRequset traveDiriverinfoRequset=new TraveDiriverinfoRequset();
        traveDiriverinfoRequset.setUserId(json.getLong("driverId").toString());
        traveDiriverinfoRequset.setUserName(json.getString("driverName"));
        traveDiriverinfoRequset.setCityType(Integer.parseInt(json.getString("areaCode")));
        return traveDiriverinfoRequset;
    }



    /**
     * 对数据库司机信息内容更新
     * @param user
     * @param json
     */
    public void updateData(IvsAppUserInfo user ,JSONObject json){
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

}
