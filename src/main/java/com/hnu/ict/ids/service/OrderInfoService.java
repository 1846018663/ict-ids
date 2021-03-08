package com.hnu.ict.ids.service;

import com.hnu.ict.ids.entity.OrderInfo;
import org.springframework.stereotype.Service;


public interface OrderInfoService {

    OrderInfo getBySourceOrderId(String sourceOrderId);

    void insertOrder(OrderInfo order);


    int deleteSourceOrderId(String sourceOrderId);


}
