package com.hnu.ict.ids.async;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.hnu.ict.ids.Kafka.KafkaProducera;
import com.hnu.ict.ids.entity.TravelInfo;
import com.hnu.ict.ids.exception.ResultEntity;
import com.hnu.ict.ids.service.TravelInfoService;
import com.hnu.ict.ids.utils.HttpClientUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * 包车结果  异步回调接口
 */
@Component
public class ReqCallbackAsync {
    Logger logger= LoggerFactory.getLogger(ReqCallbackAsync.class);

    @Value("${passenger.service.charteredBus.Callback.url}")
    private String back_url;

    @Autowired
    TravelInfoService travelInfoService;

    @Autowired
    KafkaProducera kafkaProducera;

    @Async
    public void reqCallback(ResultEntity result, TravelInfo travelInfo){
        //异步处理
        logger.info("异步处理请求=================包车"+JSON.toJSONString(result));
        try {
            String req= HttpClientUtil.doPostJson(back_url, JSON.toJSONString(result));
            JSONObject jsonObject = JSONObject.parseObject(req);
            String code=jsonObject.getString("code");
            logger.info("异步处理请求=================包车结果"+code);
            if(code.equals("00008")){
                //修改推送状态  成功
                if(travelInfo!=null){
                    travelInfo.setPushStatus(1);
                    travelInfoService.updateById(travelInfo);
                    //消息队列通知大数据平台
                    kafkaProducera.getTripInfo(travelInfo,2);
                }

            }else{
                //修改推送状态  失败
                if(travelInfo!=null){
                    travelInfo.setPushStatus(2);
                    travelInfoService.updateById(travelInfo);
                }

            }

        } catch (Exception e) {
            e.printStackTrace();
            //修改推送状态  失败
            travelInfo.setPushStatus(2);
            travelInfoService.updateById(travelInfo);
        }
    }

}
