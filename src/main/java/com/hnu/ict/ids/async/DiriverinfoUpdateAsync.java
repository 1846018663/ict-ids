package com.hnu.ict.ids.async;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.hnu.ict.ids.bean.CancelCharterCarBeanAsync;
import com.hnu.ict.ids.bean.DiriverinfoUpdateBeanAsync;
import com.hnu.ict.ids.entity.OrderInfo;
import com.hnu.ict.ids.entity.TravelInfo;
import com.hnu.ict.ids.utils.HttpClientUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DiriverinfoUpdateAsync {

    Logger logger= LoggerFactory.getLogger(DiriverinfoUpdateAsync.class);

    @Value("${travel.algorithm.diriverinfo.update.url}")
    private String diriverinfo_update_url;


    /**
     * 更新司机信息
     * @param list 司机信息集合
     */
    @Async
    public void diriverinfoUpdate(List<DiriverinfoUpdateBeanAsync> list) {
        //异步完成算法数据同步----站台信息
        logger.info("异步完成算法数据同步----司机信息"+ JSON.toJSONString(list));
        try {
            String result= HttpClientUtil.doPostJson(diriverinfo_update_url, JSON.toJSONString(list));
            JSONObject json=JSONObject.parseObject(result);
            logger.info("同步内容"+result);
            if(json.getInteger("status")==1){
                logger.info("异步完成算法数据同步----司机信息完成");
            }else{
                logger.info("异步完成算法数据同步----司机信息失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

}
