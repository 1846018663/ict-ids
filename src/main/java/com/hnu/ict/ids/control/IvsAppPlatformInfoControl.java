package com.hnu.ict.ids.control;

import com.alibaba.fastjson.JSONObject;
import com.hnu.common.respone.PojoBaseResponse;
import com.hnu.ict.ids.bean.PlatformInfoFrom;
import com.hnu.ict.ids.entity.DistancePlan;
import com.hnu.ict.ids.entity.IvsAppPlatformInfo;
import com.hnu.ict.ids.entity.NetworkLog;
import com.hnu.ict.ids.exception.NetworkEnum;
import com.hnu.ict.ids.service.DistancePlanService;
import com.hnu.ict.ids.service.IvsAppPlatformInfoService;
import com.hnu.ict.ids.service.NetworkLogService;
import com.hnu.ict.ids.utils.HttpClientUtil;
import com.hnu.ict.ids.utils.ParamsNotNull;
import io.swagger.annotations.Api;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Api(tags = "站点数据API")
@RestController
@RequestMapping("/platform")
public class IvsAppPlatformInfoControl {

    Logger logger= LoggerFactory.getLogger(IvsAppPlatformInfoControl.class);

    @Value("${travel.algorithm.planning.url}")
    private String url;

    @Autowired
    IvsAppPlatformInfoService  ivsAppPlatformInfoService;

    @Autowired
    DistancePlanService distancePlanService;

    @Autowired
    NetworkLogService networkLogServer;


    /**
     * 根据城市代码返回全部城市站点信息
     * @param cityCode
     * @return
     */
    @RequestMapping(value="/getPlatformAllInfo" , method = RequestMethod.POST)
    @ParamsNotNull(str ="cityCode")
    public PojoBaseResponse getPlatformAllInfo(String cityCode){
        PojoBaseResponse result=new PojoBaseResponse();
        List<PlatformInfoFrom> list=ivsAppPlatformInfoService.getPlatformAll(cityCode);
        result.setData(list);
        return result;

    }


    @RequestMapping(value="/findPlatform" , method = RequestMethod.POST)
    public  Map<String,Object> getfindPlatform(@RequestBody String body){
        JSONObject json=JSONObject.parseObject(body);
        logger.info("乘客服务系统路径规划与距离计算接口"+body);
        String stataId=json.getString("start_id");
        String endId=json.getString("end_id");
        String waypoints=json.getString("waypoints");
        Map<String,Object> map=new HashMap<>();
        if(StringUtils.hasText(stataId) && StringUtils.hasText(endId)){
            JSONObject jsonObject=new JSONObject();
            jsonObject.put("start_id",Integer.parseInt(stataId));
            jsonObject.put("end_id",Integer.parseInt(endId));
            if(waypoints!=null){
                jsonObject.put("way_id",waypoints);
            }else{
                jsonObject.put("way_id","");
            }


            NetworkLog networkLog=new NetworkLog();
            networkLog.setCreateTime(new Date());
            networkLog.setInterfaceInfo(NetworkEnum.ALGORITHM_PLANNING.getValue());
            networkLog.setType(NetworkEnum.TYPE_HTTP.getValue());
            networkLog.setMethod(NetworkEnum.METHOD_POST.getValue());
            networkLog.setUrl(url);
            networkLog.setAccessContent(jsonObject.toJSONString());

            String result=null;
            try {
                logger.info("获取路径"+jsonObject.toJSONString());
                result= HttpClientUtil.doPostJson(url,jsonObject.toJSONString());
                networkLog.setResponseResult(result);
                networkLog.setStatus(NetworkEnum.STATUS_SUCCEED.getValue());
            } catch (Exception e) {
                networkLog.setStatus(NetworkEnum.STATUS_FAILED.getValue());
                e.printStackTrace();
            }
            //保存接口日志
            networkLogServer.insertNetworkLog(networkLog);

            //解析算法内容数据
            JSONObject object=JSONObject.parseObject(result);
            logger.info("路径行程与距离算法返回值"+result);
            int status = object.getInteger("status");

            if(status==1){
                map.put("code","0008");
                map.put("distance",object.getDouble("distance"));
                map.put("all_travel_plat",object.getString("all_travel_plat"));
            }else{
                map.put("code","0007");
                map.put("distance","");
                map.put("all_travel_plat","");
            }


        }else{
            map.put("code","0007");
            map.put("distance","");
            map.put("all_travel_plat","");

        }

        return map;

    }




}
