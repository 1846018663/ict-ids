package com.hnu.ict.ids.redis;

import com.hnu.ict.ids.WebSocket.WebSocketServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;


public class RedisKeyUpdateListener extends KeyUpdateEventMessageListener {

    @Autowired
    WebSocketServer wbSocketServer;


    public RedisKeyUpdateListener(RedisMessageListenerContainer listenerContainer) {
        super(listenerContainer);
    }

    /**
     * 针对redis数据修改事件，进行数据处理
     * @param message message.toString()获取修改key
     * @param pattern
     */
    @Override
    public void onMessage(Message message, byte[] pattern) {
        String deleteKey = message.toString();
        String prefix = "carLocateInfo::*";
//        // *号 必须要加，否则无法模糊查询
//
//        // 获取所有的key
//        Set<String> keys = redisTemplate.keys(prefix);
//        System.out.println(keys);
//        JSONObject json=new JSONObject();
//        Map<String, Object> map = new HashMap<>();
//        for(String key : keys){
//
//            String str=key.replace("carLocateInfo::","");
//            String[] keyStr=str.split("_");
//
//            String[] position =stringRedisTemplate.opsForValue().get(key).split(",");
//            map.put(keyStr[0], position[0]+","+position[1]);
//        }
//        String str= JSON.toJSONString(list);
//        wbSocketServer.sendMessage(str);

    }
}

