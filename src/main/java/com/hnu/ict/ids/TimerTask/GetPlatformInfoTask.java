package com.hnu.ict.ids.TimerTask;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hnu.ict.ids.async.UpdatePlatformAsync;
import com.hnu.ict.ids.bean.TravePlatfromRequset;
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
    @Scheduled(cron = "0 0/10 * * * ? ")
    public void getCarInfo() throws Exception {
        //网络请求获取全量站点信息
        StringBuffer urlInfo=new StringBuffer(URL).append("?paging=false");
        String body= HttpClientUtil.doGet(urlInfo.toString());

        //解析站点信息集合并做逻辑处理
        JSONObject object=JSONObject.parseObject(body);
        if(object.getBoolean("success")==true){
            //解析data   key值
            JSONObject dataJson=object.getJSONObject("data");
            //解析车数组内容
            JSONArray carJson= dataJson.getJSONArray("result");

            List<TravePlatfromRequset> platfromList=new ArrayList<>();

            for (int i=0;i<carJson.size();i++){
                JSONObject json=carJson.getJSONObject(i);
                //根据站台id获取站台信息
                IvsAppPlatformInfo platformInfo=ivsAppPlatformInfoService.getByPlatformId(json.getString("stationCode"));

                //异步不同步集合数据对象
                String code=json.getString("areaCode");
                if(code.equals("1002")) {
                    platfromList.add(intoTravePlatfromRequset(json));
                }

                updateData(platformInfo ,json);
            }

            if(platfromList!=null && platfromList.size()>0){
                updatePlatformAsync.updatePlatformAlgorithm(platfromList);
            }
        }
    }


    /**
     * 初始化  同步算法站点对象信息封装
     * @param json
     * @return
     */
    public TravePlatfromRequset intoTravePlatfromRequset(JSONObject  json){
        //封装数据给算法
        TravePlatfromRequset platfromRequset=new TravePlatfromRequset();
        platfromRequset.setStationId(Integer.parseInt(json.getString("stationCode")));
        platfromRequset.setStationName(json.getString("stationName"));
        platfromRequset.setStationType(json.getInteger("stationType"));
        platfromRequset.setStationRouteType(json.getInteger("stationRouteType"));
        platfromRequset.setNextStaIds(json.getString("nextStaIds"));
        platfromRequset.setNextStaDistances(json.getString("nextStaDistances"));
        platfromRequset.setStaGps(json.getDouble("lng").toString()+","+json.getDouble("lat").toString());
        platfromRequset.setRangeRadius(json.getFloat("rangeRadius"));
        platfromRequset.setCityCod(json.getString("areaCode"));
        return platfromRequset;
    }



    /**
     * 对数据库司机信息内容更新
     * @param platformInfo
     * @param json
     */
    public void updateData(IvsAppPlatformInfo platformInfo ,JSONObject json){
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
    }
}
