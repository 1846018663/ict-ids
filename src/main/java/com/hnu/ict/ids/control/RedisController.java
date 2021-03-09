package com.hnu.ict.ids.control;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author lulu
 * @Date 2019/6/22 17:02
 */
@RestController
public class RedisController {

    Logger logger= LoggerFactory.getLogger(RedisController.class);

//    @Autowired
//    private Comsumer1 comsumer1;
//    @Autowired
//    private Producer producer;
//    @Autowired
//    private Gson gson;
//    @Autowired
//    private RedisMessageListenerContainer messageListenerContainer;
//    @Autowired
//    private PubSubTable pubSubTable;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private RedisTemplate redisTemplate;


    @GetMapping("/sendMessage")
    public void p(String topic) {

        // 判断redis中是否存在key
//        boolean isExist = redisTemplate.hasKey("carLocateInfo::");

        String val = (String)redisTemplate.opsForValue().get("carLocateInfo::");
        logger.info("获取数据结果"+val);
    }

    @GetMapping("/unSubTopic")
    public void unSubTopci(String topic) {
//            messageListenerContainer.removeMessageListener(comsumer1, new ChannelTopic("监听"));
//            pubSubTable.removeComsumer(topic, comsumer1.getClass().getSimpleName());
    }

    @GetMapping("/subTopic")
    public void addChannel(String topic) {

//            messageListenerContainer.addMessageListener(comsumer1, new ChannelTopic("监听"));
//
//            comsumer1.addChannel(topic);



    }


}
