package com.hnu.ict.ids.async;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.hnu.ict.ids.bean.UpdatePlatfromBean;
import com.hnu.ict.ids.utils.HttpClientUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;


@Component
public class UpdatePlatformAsync {

    Logger logger= LoggerFactory.getLogger(UpdatePlatformAsync.class);


    @Value("${travel.algorithm.update.platform.url}")
    private String updatePlatformUrl;

    @Async
    public void updatePlatformAlgorithm(List<UpdatePlatfromBean> list){
        //异步完成算法数据同步----站台信息
        logger.info("异步完成算法数据同步----站台信息"+JSON.toJSONString(list));
        try {
           String result= HttpClientUtil.doPostJson(updatePlatformUrl, JSON.toJSONString(list));
           JSONObject json=JSONObject.parseObject(result);
           if(json.getInteger("status")==1){
               logger.info("异步完成算法数据同步----站台信息完成");
           }else{
               logger.info("异步完成算法数据同步----站台信息失败");
           }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
