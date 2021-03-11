package com.hnu.ict.ids.control;

import com.alibaba.fastjson.JSONObject;
import com.hnu.common.respone.PojoBaseResponse;
import com.hnu.ict.ids.WebSocket.WebSocketServer;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;
import java.util.Set;


@Api(tags = "redis数据API")
@RestController
@RequestMapping("/redis")
public class DemoController {

    @Resource
    private RedisTemplate redisTemplate;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;


    @Autowired
    private WebSocketServer webSocketServer;

    @RequestMapping("/test")
    public PojoBaseResponse test(String id)  throws IOException {
        PojoBaseResponse response=new PojoBaseResponse();

        // *号 必须要加，否则无法模糊查询
        String prefix = "carLocateInfo::*";
        // 获取所有的key
        Set<String> keys = redisTemplate.keys(prefix);
        System.out.println(keys);
        JSONObject json=new JSONObject();
        for(String key : keys){
            System.out.println(key);
            System.out.println("value值" +stringRedisTemplate.opsForValue().get(key));
            json.put(key,stringRedisTemplate.opsForValue().get(key));
        }
//        webSocketServer.sendInfo(json.toString(),id);

        return response;
    }
}
