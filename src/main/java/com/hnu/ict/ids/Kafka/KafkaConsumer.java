package com.hnu.ict.ids.Kafka;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.hnu.ict.ids.WebSocket.WebSocketServer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class KafkaConsumer {

    Logger logger= LoggerFactory.getLogger(KafkaProducer.class);

    @Autowired
    WebSocketServer webSocketServer;

    //获取车辆定位信息
    @KafkaListener(topics = "vms_car_location") //定义此消费者接收topic为“topic1”的消息
    public void listenCarLocation (ConsumerRecord<?, ?> record) throws Exception {
        System.out.printf("++++++++++++++++++topic = %s, offset = %d, value = %s \n", record.topic(), record.offset(), record.value());
        JSONObject jsonObject= JSON.parseObject( record.value().toString());

        webSocketServer.sendMessageAll(jsonObject.toString());
    }


    /*
      获取车辆定位信息
     */
    @KafkaListener(topics = "vms_car_location")
    public void onMessageCarLocation(String message)  throws Exception {
        //insertIntoDb(buffer);//这里为插入数据库代码
        System.out.println(message);
        webSocketServer.sendMessageAll(message);
    }

    /*
     获取车辆状态
    */
    //获取车辆定位信息
    @KafkaListener(topics = "vms_car_state")
    public void listenCarState (ConsumerRecord<?, ?> record) throws Exception {
        System.out.printf("++++++++++++++++++topic = %s, offset = %d, value = %s \n", record.topic(), record.offset(), record.value());
        JSONObject jsonObject= JSON.parseObject( record.value().toString());

        webSocketServer.sendMessageAll(jsonObject.toString());
    }


    /*
      获取车辆定位信息
     */
    @KafkaListener(topics = "vms_car_location")
    public void onMessageCarState(String message)  throws Exception {
        //insertIntoDb(buffer);//这里为插入数据库代码
        System.out.println(message);
        webSocketServer.sendMessageAll(message);
    }




}
