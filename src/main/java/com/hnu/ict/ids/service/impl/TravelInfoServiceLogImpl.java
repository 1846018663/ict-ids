package com.hnu.ict.ids.service.impl;

import com.hnu.ict.ids.entity.TravelInfoLog;
import com.hnu.ict.ids.mapper.OrderInfoMapper;
import com.hnu.ict.ids.mapper.TravelInfoLogMapper;
import com.hnu.ict.ids.service.OrderInfoHistotryService;
import com.hnu.ict.ids.service.TravelInfoLogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TravelInfoServiceLogImpl implements TravelInfoLogService {

    Logger logger= LoggerFactory.getLogger(TravelInfoServiceLogImpl.class);

    @Autowired
    TravelInfoLogMapper travelInfoLogMapper;
    @Autowired
    OrderInfoMapper orderInfoMapper;
    @Autowired
    OrderInfoHistotryService orderInfoHistotryService;




    public int insert(TravelInfoLog travelInfolog){
        return travelInfoLogMapper.insert(travelInfolog);
    }
}
