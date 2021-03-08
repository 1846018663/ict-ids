package com.hnu.ict.ids.service.impl;

import com.hnu.ict.ids.entity.OrderInfo;
import com.hnu.ict.ids.mapper.OrderInfoMapper;

import com.hnu.ict.ids.service.OrderInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @description: 业务实现
 * @author: yuanhx
 * @create: 2021/01/07
 */

@Service
public class OrderInfoServiceImpl implements OrderInfoService {

    @Autowired
    OrderInfoMapper orderMapper;


    @Override
    public OrderInfo getBySourceOrderId(String sourceOrderId) {

        return orderMapper.getBySourceOrderId(sourceOrderId);
    }

    @Override
    public void insertOrder(OrderInfo orderInfo){
        orderMapper.insertOrderInfo(orderInfo);
    }

    @Override
    public int deleteSourceOrderId(String sourceOrderId){
        return  orderMapper.deleteBySourceOrderId(sourceOrderId);
    }

}
