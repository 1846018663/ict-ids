package com.hnu.ict.ids.service;

import com.hnu.ict.ids.entity.OrderInfo;
import com.hnu.ict.ids.entity.OrderInfoHistotry;
import com.hnu.ict.ids.exception.ResultEntity;

import java.util.List;


public interface OrderInfoService {

    OrderInfo getBySourceOrderId(String sourceOrderId);

    void insertOrder(OrderInfo order,String uIds);


    ResultEntity deleteSourceOrderId(OrderInfo order, OrderInfoHistotry orderInfoHistotry, String uIds);

    List<OrderInfo> findNotTrave(String statDate, String endDate);


    List<OrderInfo> findNotTransportCapacity(String statDate, String endDate);


    OrderInfo getById(int id);

    void updateByIdList(List<OrderInfo> list,Integer status);


    List<OrderInfo> findCompensatesOrderIinfo();

    List<OrderInfo> findOrderTravelId(String travelId);


    int deleteBySourceOrderId(String  sourceOrderId);

    void updateById(OrderInfo orderInfo);


    List<OrderInfo> findPushFailedOrderIinfo();


    OrderInfo findOrderNo(String orderNo);


}
