package com.hnu.ict.ids.webHttp;

import com.alibaba.fastjson.JSON;
import com.hnu.ict.ids.bean.CustomerHttpAPIBean;
import com.hnu.ict.ids.bean.TicketInfo;
import com.hnu.ict.ids.entity.OrderInfo;
import com.hnu.ict.ids.entity.TravelInfo;
import com.hnu.ict.ids.service.OrderInfoService;
import com.hnu.ict.ids.service.TravelInfoService;
import com.hnu.ict.ids.utils.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Resource;
import java.math.BigInteger;
import java.util.*;

public class CustomerHttpAPI {

    @Resource
    TravelInfoService travelInfoService;
    @Resource
    OrderInfoService  orderInfoService;

    public String SuccessfulTrip(BigInteger id){
        TravelInfo info= travelInfoService.getById(id);
        OrderInfo order= orderInfoService.getBySourceOrderId(info.getSourceTravelId());

        List<CustomerHttpAPIBean> list=new ArrayList<>();
        CustomerHttpAPIBean customerHttpAPIBean=new CustomerHttpAPIBean();
        customerHttpAPIBean.setDistance(info.getDistance().doubleValue());
        customerHttpAPIBean.setO_id(info.getId().longValue());
        customerHttpAPIBean.setTravel_id(info.getSourceTravelId());
        customerHttpAPIBean.setExpected_time(Integer.parseInt(info.getExpectedTime()));
        customerHttpAPIBean.setAll_travel_plat(info.getAllTravelPlat());
        customerHttpAPIBean.setDriver_content(info.getDriverContent());
        customerHttpAPIBean.setC_id(info.getCarId());
        customerHttpAPIBean.setDriver_id(info.getDriverId());
        customerHttpAPIBean.setReservation_status(info.getTravelStatus());
        customerHttpAPIBean.setIt_number(order.getTicketNumber());
        customerHttpAPIBean.setRet_status(0);
        customerHttpAPIBean.setOper_time(DateUtil.strToDayDate(new Date()));
        List<TicketInfo> ticket_info=new ArrayList<>();
        TicketInfo ticke=new TicketInfo();
        ticke.setU_id(order.getBuyUid().intValue());
        ticket_info.add(ticke);

        customerHttpAPIBean.setTicket_info(ticket_info);

        return JSON.toJSONString(list);
    }



}
