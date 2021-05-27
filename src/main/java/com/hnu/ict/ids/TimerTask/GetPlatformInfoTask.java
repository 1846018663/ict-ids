package com.hnu.ict.ids.TimerTask;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hnu.ict.ids.async.UpdatePlatformAsync;
import com.hnu.ict.ids.bean.UpdatePlatfromAsyncBean;
import com.hnu.ict.ids.entity.IvsAppPlatformInfo;
import com.hnu.ict.ids.service.IvsAppPlatformInfoService;
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
public class GetPlatformInfoTask {

    Logger logger= LoggerFactory.getLogger(GetPlatformInfoTask.class);

    @Value("${big.data.getPlatformInfo.url}")
    private String URL;


    @Autowired
    IvsAppPlatformInfoService ivsAppPlatformInfoService;

    @Autowired
    UpdatePlatformAsync updatePlatformAsync;

    //站台信息0 0 5,21 * * ?
    @Scheduled(cron = "0 0 5,21 * * ?")
    public void getCarInfo() throws Exception {
        StringBuffer urlInfo=new StringBuffer(URL).append("?paging=false");
        String body= HttpClientUtil.doGet(urlInfo.toString());
        JSONObject object=JSONObject.parseObject(body);
        logger.info("==========获取站台信息============"+body);
        if(object.getBoolean("success")==true){
            //解析data   key值
            JSONObject dataJson=object.getJSONObject("data");
            //解析车数组内容
            JSONArray carJson= dataJson.getJSONArray("result");

            List<UpdatePlatfromAsyncBean> list=new ArrayList<>();

            for (int i=0;i<carJson.size();i++){
                JSONObject json=carJson.getJSONObject(i);
                //封装数据给算法
                UpdatePlatfromAsyncBean updatePlatfromBean=new UpdatePlatfromAsyncBean();
                updatePlatfromBean.setStationId(Integer.parseInt(json.getString("stationCode")));
                updatePlatfromBean.setStationName(json.getString("stationName"));
                updatePlatfromBean.setStationType(json.getInteger("stationType"));
                updatePlatfromBean.setStationRouteType(json.getInteger("stationRouteType"));

                if(json.getString("nextStaIds")!=null){
                    updatePlatfromBean.setNextStaIds(json.getString("nextStaIds"));
                }else{
                    updatePlatfromBean.setNextStaIds("");
                }

                if(json.getString("nextStaDistances")!=null){
                    updatePlatfromBean.setNextStaDistances(json.getString("nextStaDistances"));
                }else{
                    updatePlatfromBean.setNextStaDistances("");
                }


                updatePlatfromBean.setStaGps(json.getDouble("lng").toString()+","+json.getDouble("lat").toString());
                updatePlatfromBean.setRangeRadius(json.getFloat("rangeRadius"));
                updatePlatfromBean.setCityCod(json.getString("areaCode"));
                list.add(updatePlatfromBean);

                //根据站台id获取站台信息
                IvsAppPlatformInfo platformInfo=ivsAppPlatformInfoService.getByPlatformId(json.getString("stationCode"));
                if(platformInfo==null){
                    platformInfo=new IvsAppPlatformInfo();
                    platformInfo.setPId(json.getString("stationCode"));
                    platformInfo.setCCode(json.getString("areaCode"));
                    platformInfo.setPName(json.getString("stationName"));
                    platformInfo.setPType(json.getInteger("stationType"));
                    platformInfo.setPRouteType(json.getInteger("stationRouteType"));
                    platformInfo.setPGroupType(json.getInteger("stationGroupType"));
                    platformInfo.setLatitude(json.getDouble("lat").toString());
                    platformInfo.setLongitude(json.getDouble("lng").toString());
                    platformInfo.setNextPIds(json.getString("nextStaIds"));
                    platformInfo.setNextPDistances(json.getString("nextStaDistances"));
                    if(json.getInteger("isDeleted")==0){
                        platformInfo.setPStatus(1);//默认可用
                    }else{
                        platformInfo.setPStatus(2);//默认不可用
                    }
                    platformInfo.setSpeedLimit(json.getInteger("speedLimit"));
                    platformInfo.setRangeRadius(json.getInteger("rangeRadius"));
                    ivsAppPlatformInfoService.insert(platformInfo);
                }else{
                    platformInfo.setPId(json.getString("stationCode"));
                    platformInfo.setCCode(json.getString("areaCode"));
                    platformInfo.setPName(json.getString("stationName"));
                    platformInfo.setPType(json.getInteger("stationType"));
                    platformInfo.setPRouteType(json.getInteger("stationRouteType"));
                    platformInfo.setPGroupType(json.getInteger("stationGroupType"));
                    platformInfo.setLatitude(json.getString("lat"));
                    platformInfo.setLongitude(json.getString("lng"));
                    platformInfo.setNextPIds(json.getString("nextStaIds"));
                    platformInfo.setNextPDistances(json.getString("nextStaDistances"));
                    if(json.getInteger("isDeleted")==0){
                        platformInfo.setPStatus(1);//默认可用
                    }else{
                        platformInfo.setPStatus(2);//默认不可用
                    }
                    platformInfo.setSpeedLimit(json.getInteger("speedLimit"));
                    platformInfo.setRangeRadius(json.getInteger("rangeRadius"));
                    ivsAppPlatformInfoService.updateById(platformInfo);
                }

                //封装数据发送给算法系统

            }
            if(list.size()>0){
                updatePlatformAsync.updatePlatformAlgorithm(list);
            }
        }


    }
}
