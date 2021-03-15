package com.hnu.ict.ids.control;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hnu.common.respone.PojoBaseResponse;
import com.hnu.ict.ids.WebSocket.WebSocketServer;
import com.hnu.ict.ids.bean.CarPosition;
import io.swagger.annotations.Api;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.*;


@Api(tags = "redis数据API")
@RestController
@RequestMapping("/redis")
public class RedisAPIController {

    Logger logger= LoggerFactory.getLogger(RedisAPIController.class);

    @Resource
    private RedisTemplate redisTemplate;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;


    @Autowired
    private WebSocketServer webSocketServer;

    /**
     * 查询所有的车辆信息位置   如果carId有值查单个车辆位置信息
     * @param carId
     * @return
     * @throws IOException
     */
    @RequestMapping(value="/getCarPosition" )
    public PojoBaseResponse getCarPosition(String carId)  throws IOException {
        PojoBaseResponse response=new PojoBaseResponse();
        String prefix = "carLocateInfo::";
        // *号 必须要加，否则无法模糊查询
        if(StringUtils.hasText(carId)){
            prefix=prefix+carId;
        }
        prefix=prefix+"*";

        // 获取所有的key
        Set<String> keys = redisTemplate.keys(prefix);
        System.out.println(keys);
        JSONObject json=new JSONObject();
        Map<String, Object> map = new HashMap<>();
        for(String key : keys){

            String str=key.replace("carLocateInfo::","");
            String[] keyStr=str.split("_");

            String[] position =stringRedisTemplate.opsForValue().get(key).split(",");
            map.put(keyStr[0], position[0]+","+position[1]);
        }

        response.setData(map);

        return response;
    }



    @RequestMapping(value = "/getRedisCarInfo" ,method = RequestMethod.POST)
    public PojoBaseResponse getWebSocketServer(String id,String carId)  throws IOException {
        PojoBaseResponse response=new PojoBaseResponse();
        String prefix = "carLocateInfo::";
        // *号 必须要加，否则无法模糊查询
        if(StringUtils.hasText(carId)){
            prefix=prefix+carId;
        }
        prefix=prefix+"*";

        // 获取所有的key
        Set<String> keys = redisTemplate.keys(prefix);
        List<CarPosition> list=new ArrayList<>();
        Map<String, String> map = new HashMap<>();
//        for(String key : keys){
//            String str=key.replace("carLocateInfo::","");
//            String[] keyStr=str.split("_");
//            map.put(keyStr[0],",121.280737");
////            map.put(keyStr[0], stringRedisTemplate.opsForValue().get(key));
//        }

        map.put("1","31.343375,121.280737");
        map.put("22","31.335508,121.281756");
        map.put("7","31.371762,121.255854");

        for (Map.Entry<String, String> entry : map.entrySet()) {
            CarPosition car=new CarPosition();
            car.setId(entry.getKey());
            String[] arr=entry.getValue().split(",");
            car.setLongitude(arr[1]);
            car.setLatitude(arr[0]);
            list.add(car);
        }

        String str= JSON.toJSONString(list);
        System.out.println(str);
        webSocketServer.sendInfo(str,"0114");
        response.setData(list);
        return response;
    }
}
