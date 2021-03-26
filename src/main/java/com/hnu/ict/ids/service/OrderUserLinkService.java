package com.hnu.ict.ids.service;

import com.hnu.ict.ids.bean.TicketInfo;
import com.hnu.ict.ids.entity.OrderUserLink;

import java.util.List;

public interface OrderUserLinkService {


    List<OrderUserLink> findOrderNo(String orderNo);



}
