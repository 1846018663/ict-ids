package com.hnu.ict.ids.TimerTask;

import com.alibaba.fastjson.JSON;
import com.hnu.ict.ids.entity.OrderInfo;
import com.hnu.ict.ids.service.OrderInfoService;
import com.hnu.ict.ids.utils.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

@Component
@EnableScheduling
public class DispatchTask {

    @Autowired
    OrderInfoService orderInfoService;


    /**
     * 每隔5分钟执行一次（按照 corn 表达式规则执行）0 0/5 * * * ?
     */
    @Scheduled(cron = "0 0/5 * * * ?")
    public void job1() {
        //第一步查询订单  查询没有行程id  且当前 开始时间大约30分钟内的订单
        Date stateDate=new Date();
        //30分钟毫秒
        long time=1000*60*30+stateDate.getTime();
        Date endDate=DateUtil.millisecondToDate(time);
        List<OrderInfo>  listOrder=orderInfoService.findNotTrave(DateUtil.getCurrentTime(stateDate),DateUtil.getCurrentTime(endDate));
        System.out.println(stateDate+"执行任务0："+ endDate);
        String json=JSON.toJSONString(listOrder);
        System.out.println(json);
        System.out.println("执行任务job1："+ new Date());
    }
}
