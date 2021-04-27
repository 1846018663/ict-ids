package com.hnu.ict.ids.Kafka;

import com.alibaba.fastjson.JSONObject;
import com.hnu.ict.ids.entity.TravelInfo;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@Component
public class KafkaProducer {
    @Autowired
    private KafkaTemplate<Object, Object> kafkaTemplate;


    // 发送消息
    public void sendMessage1(TravelInfo travelInfo ) {
        JSONObject key=new JSONObject();


        JSONObject value=new JSONObject();

        kafkaTemplate.send("vms_car_location",key, value);
    }
}
