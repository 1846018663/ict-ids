package com.hnu.ict.ids.redis;

import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;


public class RedisKeyUpdateListener extends KeyUpdateEventMessageListener {

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
        System.out.println("监听修改事件"+deleteKey);
        // ......
    }
}

