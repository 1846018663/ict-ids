package com.hnu.ict.ids.service;

import com.hnu.ict.ids.entity.OrderInfo;


public interface OrderInfoService {

    OrderInfo getBySourceOrderId(String sourceOrderId);

    void insertOrder(OrderInfo order);


    int deleteSourceOrderId(String sourceOrderId);


}
