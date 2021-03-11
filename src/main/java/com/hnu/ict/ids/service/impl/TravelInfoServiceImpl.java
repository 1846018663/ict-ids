package com.hnu.ict.ids.service.impl;

import com.hnu.ict.ids.entity.TravelInfo;
import com.hnu.ict.ids.mapper.TravelInfoMapper;
import com.hnu.ict.ids.service.TravelInfoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.Date;

@Service
public class TravelInfoServiceImpl implements TravelInfoService {

    Logger logger= LoggerFactory.getLogger(TravelInfoServiceImpl.class);

    @Autowired
    TravelInfoMapper travelInfoMapper;

    @Override
    public TravelInfo getCarTime(int carId,Date timeDate){
        logger.info("carId"+carId+"time"+timeDate);
        return travelInfoMapper.getCarTime(carId,timeDate);
    }
    @Override
    public TravelInfo getById(BigInteger id){
        return travelInfoMapper.getById(id);
    }

    @Override
    public int finDateTraveTotal(Date startDate, Date endDate){
        return travelInfoMapper.finDateTraveTotal(startDate,endDate);
    }
}
