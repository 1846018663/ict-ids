package com.hnu.ict.ids.control;

import com.alibaba.fastjson.JSONObject;
import com.hnu.common.respone.PojoBaseResponse;
import com.hnu.ict.ids.bean.PlatformInfoFrom;
import com.hnu.ict.ids.entity.DistancePlan;
import com.hnu.ict.ids.entity.IvsAppPlatformInfo;
import com.hnu.ict.ids.service.DistancePlanService;
import com.hnu.ict.ids.service.IvsAppPlatformInfoService;
import com.hnu.ict.ids.utils.ParamsNotNull;
import io.swagger.annotations.Api;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Api(tags = "站点数据API")
@RestController
@RequestMapping("/platform")
public class IvsAppPlatformInfoControl {

    Logger logger= LoggerFactory.getLogger(IvsAppPlatformInfoControl.class);

    @Autowired
    IvsAppPlatformInfoService  ivsAppPlatformInfoService;

    @Autowired
    DistancePlanService distancePlanService;


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
        Map<String,Object> map=new HashMap<>();
        if(StringUtils.hasText(stataId) && StringUtils.hasText(endId)){
            DistancePlan distancePlan= distancePlanService.findStartEnd(Integer.parseInt(stataId),Integer.parseInt(endId));
            if(distancePlan!=null){
                map.put("code","0008");
                map.put("distance",distancePlan.getDistance());
                map.put("all_travel_plat",distancePlan.getAllTravelPlat());
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
