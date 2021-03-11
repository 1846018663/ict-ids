package com.hnu.ict.ids.service;

import com.hnu.ict.ids.entity.OrderInfo;

import java.util.Date;
import java.util.List;


public interface OrderInfoService {

    OrderInfo getBySourceOrderId(String sourceOrderId);

    void insertOrder(OrderInfo order);


    int deleteSourceOrderId(String sourceOrderId);

    List<OrderInfo> findNotTrave(String statDate, String endDate);



}
