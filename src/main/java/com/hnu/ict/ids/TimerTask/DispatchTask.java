package com.hnu.ict.ids.TimerTask;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hnu.ict.ids.bean.OrderTask;
import com.hnu.ict.ids.entity.OrderInfo;
import com.hnu.ict.ids.entity.TravelInfo;
import com.hnu.ict.ids.service.OrderInfoService;
import com.hnu.ict.ids.service.TravelInfoService;
import com.hnu.ict.ids.utils.DateUtil;
import com.hnu.ict.ids.webHttp.CustomerWebAPI;
import com.hnu.ict.ids.webHttp.HttpClientUtil;
import netscape.javascript.JSObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import sun.net.www.http.HttpClient;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

@Component
@EnableScheduling
public class DispatchTask {


    @Value("${travel.algorithm.url}")
    private String URL;

    @Value("${passenger.service.callback.url}")
    private String callback_URL;

    @Autowired
    OrderInfoService orderInfoService;

    @Autowired
    TravelInfoService travelInfoService;

    /**
     * 每隔5分钟执行一次（按照 corn 表达式规则执行）0 0/5 * * * ?    0/10 * * * * ?
     */
    @Scheduled(cron = " 0 0/5 * * * ?")
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
            int k=30;
            for (Iterator<OrderInfo> it = listOrder.iterator(); it.hasNext();) {
                OrderInfo info=it.next();
                    OrderTask task=new OrderTask();
                    task.setO_id(info.getId().intValue());
                    task.setFrom_p_id(info.getBeginStationId());
                    task.setTo_p_id(info.getEndStationId());
                    task.setStart_time(DateUtil.getCurrentTime(info.getStartTime()));
                    task.setOrder_time(DateUtil.getCurrentTime(info.getCreateTime()));
                    task.setTicet_number(info.getTicketNumber());
                    list.add(task);
            }
            String json=JSON.toJSONString(list);


            System.out.println("发送请求数据"+json);
            try {
                body=HttpClientUtil.doPostJson(URL,json);
                System.out.println("接收数据"+body);
                JSONArray array=JSON.parseArray(body);
                List<TravelInfo> travelInfoList=new ArrayList<>();
                List<OrderInfo> orderInfoList=new ArrayList<>();
                for (int i=0;i<array.size();i++){
                    JSONObject object=array.getJSONObject(i);
                    OrderInfo orderInfo=orderInfoService.getById(object.getInteger("i_id"));
                    TravelInfo info=new TravelInfo();
                    info.setSourceTravelId(orderInfo.getSourceOrderId());
                    info.setBeginStationId(object.getInteger("from_p_id"));
                    info.setEndStationId(object.getInteger("to_p_id"));
                    info.setTravelStatus(1);
                    info.setStartTime(DateUtil.strToDate(object.getString("start_time")));
                    info.setDistance(new BigDecimal(object.getDouble("distance")));
                    info.setExpectedTime(object.getInteger("expected_time").toString());
                    info.setDriverContent(object.getString("driver_content"));
                    info.setAllTravelPlat(object.getString("all_travel_plat"));
                    info.setCarId(object.getInteger("car_id"));
//                    info.setDriverId(object.getInteger()); //司机id
                    info.setCreateTime(new Date());
                    travelInfoList.add(info);
                }

                //解析数据封装后   service批量处理


            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        System.out.println("执行任务job1："+ new Date());
    }



}
