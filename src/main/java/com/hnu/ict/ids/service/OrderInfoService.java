package com.hnu.ict.ids.service;

import com.hnu.ict.ids.entity.OrderInfo;

public interface OrderInfoService {

    OrderInfo getByOrderNo(String orderNo);
}
