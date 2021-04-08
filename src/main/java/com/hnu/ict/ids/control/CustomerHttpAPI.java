package com.hnu.ict.ids.control;

import com.alibaba.fastjson.JSON;
import com.hnu.ict.ids.bean.CustomerHttpAPIBean;
import com.hnu.ict.ids.entity.OrderInfo;
import com.hnu.ict.ids.entity.TravelInfo;
import com.hnu.ict.ids.service.OrderInfoService;
import com.hnu.ict.ids.service.TravelInfoService;
import com.hnu.ict.ids.utils.DateUtil;
import com.hnu.ict.ids.utils.HttpClientUtil;
import io.swagger.annotations.Api;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Api(tags = "返回乘客服务系统")
@RestController
@RequestMapping("/customer")
public class CustomerHttpAPI {

    Logger logger= LoggerFactory.getLogger(CustomerHttpAPI.class);

    @Value("${passenger.service.callback.url}")
    private String callback_URL;

    @Resource
    TravelInfoService travelInfoService;
    @Resource
    OrderInfoService  orderInfoService;

    @RequestMapping(value="/successfulTrip", method = RequestMethod.POST)
    public String successfulTrip(BigInteger id){
        TravelInfo info= travelInfoService.getById(id);
        OrderInfo order= orderInfoService.getBySourceOrderId(info.getSourceTravelId());

        List<CustomerHttpAPIBean> list=new ArrayList<>();
        CustomerHttpAPIBean customerHttpAPIBean=new CustomerHttpAPIBean();
        customerHttpAPIBean.setDistance(info.getDistance().doubleValue());
        customerHttpAPIBean.setO_ids(info.getSourceTravelId());
        customerHttpAPIBean.setTravel_id(info.getId().toString());
        customerHttpAPIBean.setExpected_time(Integer.parseInt(info.getExpectedTime()));
        customerHttpAPIBean.setAll_travel_plat(info.getAllTravelPlat());
        customerHttpAPIBean.setDriver_content(info.getDriverContent());
        customerHttpAPIBean.setC_id(info.getCarId());
        customerHttpAPIBean.setDriver_id(info.getDriverId());
        customerHttpAPIBean.setReservation_status(info.getTravelStatus());
        customerHttpAPIBean.setIt_number(order.getTicketNumber());
        customerHttpAPIBean.setRet_status(0);
        customerHttpAPIBean.setOper_time(DateUtil.strToDayDate(new Date()));
//        List<TicketInfo> ticket_info=new ArrayList<>();
//        TicketInfo ticke=new TicketInfo();
//        ticke.setU_id(order.getBuyUid().intValue());
//        ticke.setSeat_number("");
//        ticket_info.add(ticke);

//        customerHttpAPIBean.setTickets(ticket_info);
        list.add(customerHttpAPIBean);
        String json=JSON.toJSONString(list);

        System.out.println("行程预约成功后的返回传参内容"+json);
        String body="";
        try {
            body= HttpClientUtil.doPostJson(callback_URL,json);
            System.out.println("行程预约成功后的返回结果:"+body);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return body;
    }



}
