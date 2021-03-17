package com.hnu.ict.ids.service;

import com.hnu.ict.ids.entity.OrderInfo;
import com.hnu.ict.ids.entity.OrderInfoHistotry;

import java.math.BigInteger;
import java.util.Date;
import java.util.List;


public interface OrderInfoService {

    OrderInfo getBySourceOrderId(String sourceOrderId);

    void insertOrder(OrderInfo order,String uIds);


    void deleteSourceOrderId(OrderInfo order, OrderInfoHistotry orderInfoHistotry,String uIds);

    List<OrderInfo> findNotTrave(String statDate, String endDate);

    int getByTravelCount(BigInteger travelId);

    OrderInfo getById(int id);

}
