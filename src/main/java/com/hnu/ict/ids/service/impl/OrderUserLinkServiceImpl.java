package com.hnu.ict.ids.service.impl;

import com.hnu.ict.ids.bean.TicketInfo;
import com.hnu.ict.ids.entity.OrderUserLink;
import com.hnu.ict.ids.mapper.OrderUserLinkMapper;
import com.hnu.ict.ids.service.OrderUserLinkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class OrderUserLinkServiceImpl implements OrderUserLinkService {

    @Autowired
    OrderUserLinkMapper orderUserLinkMapper;


    @Override
    public List<OrderUserLink> findOrderNo(String orderNo){
        return orderUserLinkMapper.findOrderNo(orderNo);
    }


}
