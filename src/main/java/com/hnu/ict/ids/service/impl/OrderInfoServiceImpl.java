package com.hnu.ict.ids.service.impl;

import com.hnu.ict.ids.entity.OrderInfo;
import com.hnu.ict.ids.entity.OrderInfoHistotry;
import com.hnu.ict.ids.mapper.OrderInfoHistotryMapper;
import com.hnu.ict.ids.mapper.OrderInfoMapper;

import com.hnu.ict.ids.service.OrderInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.Date;
import java.util.List;

/**
 * @description: 业务实现
 * @author: yuanhx
 * @create: 2021/01/07
 */

@Service
public class OrderInfoServiceImpl implements OrderInfoService {

    @Autowired
    OrderInfoMapper orderMapper;

    @Autowired
    OrderInfoHistotryMapper orderInfoHistotryMapper;


    public List<OrderInfo> findNotTrave(String statDate, String endDate){
        return orderMapper.findNotTrave(statDate,endDate);
    }

    @Override
    public OrderInfo getBySourceOrderId(String sourceOrderId) {

        return orderMapper.getBySourceOrderId(sourceOrderId);
    }

    @Override
    public void insertOrder(OrderInfo orderInfo){
        orderMapper.insertOrderInfo(orderInfo);
    }

    @Override
    public void deleteSourceOrderId(String sourceOrderId, OrderInfoHistotry orderInfoHistotry){
      orderMapper.deleteBySourceOrderId(sourceOrderId);
      orderInfoHistotryMapper.insertOrderInfoHistotry(orderInfoHistotry);


    }

    @Override
    public int getByTravelCount(BigInteger travelId){
        return orderMapper.getByTravelCount(travelId);
    }

}
