package com.hnu.ict.ids.TimerTask;

import com.alibaba.fastjson.JSON;
import com.hnu.ict.ids.bean.OrderTask;
import com.hnu.ict.ids.entity.OrderInfo;
import com.hnu.ict.ids.service.OrderInfoService;
import com.hnu.ict.ids.utils.DateUtil;
import com.hnu.ict.ids.webHttp.CustomerWebAPI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

@Component
@EnableScheduling
public class DispatchTask {

    @Autowired
    OrderInfoService orderInfoService;

    /**
     * 每隔5分钟执行一次（按照 corn 表达式规则执行）0 0/5 * * * ?    0/10 * * * * ?
     */
    @Scheduled(cron = "0 0/5 * * * ?")
    public void job1() throws Exception {
        //第一步查询订单  查询没有行程id  且当前 开始时间大约30分钟内的订单
        Date stateDate=new Date();
        //30分钟毫秒
        long time=1000*60*30+stateDate.getTime();
        Date endDate=DateUtil.millisecondToDate(time);
        List<OrderInfo>  listOrder=orderInfoService.findNotTrave(DateUtil.getCurrentTime(stateDate),DateUtil.getCurrentTime(endDate));
        String body= null;
        if(listOrder.size()>0){
            List<OrderTask> list=new ArrayList<>();
            for (Iterator<OrderInfo> it = listOrder.iterator(); it.hasNext();) {
                OrderInfo info=it.next();
                for (int i=0;i<30;i++){
                    OrderTask task=new OrderTask();
                    task.setO_id(i);
                    task.setFrom_p_id(info.getBeginStationId());
                    task.setTo_p_id(info.getEndStationId());
                    task.setStart_time(DateUtil.getCurrentTime(info.getStartTime()));
                    task.setOrder_time(DateUtil.getCurrentTime(info.getCreateTime()));
                    task.setTicet_number(info.getTicketNumber());
                    list.add(task);
                }
            }
            String json=JSON.toJSONString(list);
            System.out.println("发送请求数据"+json);


            String url="http://47.111.139.187:5000/algorithm";

            try {
                CustomerWebAPI customerWebAPI=new CustomerWebAPI();
                body = customerWebAPI.doPost(url,json);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }





        System.out.println("执行任务job1："+ new Date());
    }
}
