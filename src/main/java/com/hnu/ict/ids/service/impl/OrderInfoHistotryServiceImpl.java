package com.hnu.ict.ids.service.impl;

import com.hnu.ict.ids.entity.OrderInfoHistotry;
import com.hnu.ict.ids.mapper.OrderInfoHistotryMapper;
import com.hnu.ict.ids.service.OrderInfoHistotryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class OrderInfoHistotryServiceImpl implements OrderInfoHistotryService {

    @Autowired
    OrderInfoHistotryMapper orderInfoHistotryMapper;

    @Override
    public void insert(OrderInfoHistotry orderInfoHistotry){
        orderInfoHistotryMapper.insert(orderInfoHistotry);
    }

}
