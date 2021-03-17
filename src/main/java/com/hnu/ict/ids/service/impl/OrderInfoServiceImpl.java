package com.hnu.ict.ids.service.impl;

import com.hnu.ict.ids.entity.OrderInfo;
import com.hnu.ict.ids.entity.OrderInfoHistotry;
import com.hnu.ict.ids.entity.OrderUserLink;
import com.hnu.ict.ids.mapper.OrderInfoHistotryMapper;
import com.hnu.ict.ids.mapper.OrderInfoMapper;

import com.hnu.ict.ids.mapper.OrderUserLinkMapper;
import com.hnu.ict.ids.service.OrderInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Autowired
    OrderUserLinkMapper orderUserLinkMapper;


    public List<OrderInfo> findNotTrave(String statDate, String endDate){
        return orderMapper.findNotTrave(statDate,endDate);
    }

    @Override
    public OrderInfo getBySourceOrderId(String sourceOrderId) {

        return orderMapper.getBySourceOrderId(sourceOrderId);
    }


    @Override
    @Transactional
    public void insertOrder(OrderInfo orderInfo,String uIds){
        orderMapper.insertOrderInfo(orderInfo);
        OrderInfo order=orderMapper.getBySourceOrderId(orderInfo.getSourceOrderId());
        //对关联   表进行添加
        String[]  ids=uIds.split(",");
        for (int i=0;i<ids.length;i++){
            OrderUserLink orderUserLink=new OrderUserLink();
            orderUserLink.setOrderId(order.getId());
            orderUserLink.setUserId(new BigInteger(ids[i]));
            orderUserLink.setState(1);
            orderUserLinkMapper.insertOrderUserLink(orderUserLink);
        }
    }

    @Transactional
    @Override
    public void deleteSourceOrderId(OrderInfo order, OrderInfoHistotry orderInfoHistotry,String uIds){
        String[] ids=uIds.split(",");
        for (int i=0;i< ids.length;i++){
            System.out.println(new BigInteger(ids[i])+"end of input integer"+order.getId());
            orderUserLinkMapper.updateOrderUserLinkState(new BigInteger(ids[i]),order.getId());
        }

        //修改订单表座位数
        orderMapper.updateOrderNumber(ids.length,order.getId());

        //判断订单所属成功是否全部移除
        int count=orderUserLinkMapper.findRemove(order.getId());
        if(count==0){
            orderMapper.deleteBySourceOrderId(order.getSourceOrderId());
            orderInfoHistotryMapper.insertOrderInfoHistotry(orderInfoHistotry);
        }

    }

    @Override
    public int getByTravelCount(BigInteger travelId){
        return orderMapper.getByTravelCount(travelId);
    }


    @Override
    public OrderInfo getById(int id){
        return orderMapper.getById(id);
    }
}
