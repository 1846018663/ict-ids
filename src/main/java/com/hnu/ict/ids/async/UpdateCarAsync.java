package com.hnu.ict.ids.async;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.hnu.ict.ids.bean.TraveCarRequest;
import com.hnu.ict.ids.utils.HttpClientUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;


/**
 * 同步算法基础数据   异步同步车辆信息
 */
@Component
public class UpdateCarAsync {

    Logger logger= LoggerFactory.getLogger(UpdateCarAsync.class);


    @Value("${travel.algorithm.carInfo.update.url}")
    private String updateCarUrl;
    @Value("${sz.travel.algorithm.carInfo.update.url}")
    private String SZupdateCarUrl;

    @Async
    public void updateCar(List<TraveCarRequest> list){
        logger.info("异步完成算法数据同步----上海车辆信息"+JSON.toJSONString(list));
        try {
           String result= HttpClientUtil.doPostJson(updateCarUrl, JSON.toJSONString(list));
           logger.info("异步完成上海同步车辆信息结果"+result);
           JSONObject json=JSONObject.parseObject(result);

           if(json.getInteger("status")==1){
               logger.info("异步完成算法数据同步----上海车辆信息完成");
           }else{
               logger.info("异步完成算法数据同步----上海车辆信息失败");
           }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    @Async
    public void szUpdateCar(List<TraveCarRequest> list){
        logger.info("异步完成算法数据同步----深圳车辆信息"+JSON.toJSONString(list));
        try {
            String result= HttpClientUtil.doPostJson(SZupdateCarUrl, JSON.toJSONString(list));
            logger.info("异步完成深圳同步车辆信息结果"+result);
            JSONObject json=JSONObject.parseObject(result);
            if(json.getInteger("status")==1){
                logger.info("异步完成算法数据同步----深圳站台信息完成");
            }else{
                logger.info("异步完成算法数据同步----深圳站台信息失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
