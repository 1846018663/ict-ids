package com.hnu.ict.ids.service.impl;

import com.hnu.ict.ids.entity.CarFormation;
import com.hnu.ict.ids.entity.TravelTicketInfo;
import com.hnu.ict.ids.mapper.CarFormationMapper;
import com.hnu.ict.ids.mapper.TravelTicketInfoMapper;
import com.hnu.ict.ids.service.CarFormationService;
import com.hnu.ict.ids.service.TravelTicketInfoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
public class CarFormationServiceImpl implements CarFormationService {
    Logger logger=LoggerFactory.getLogger(CarFormationServiceImpl.class);


    @Autowired
    CarFormationMapper carFormationMapper;


    public int insert(CarFormation carFormation){
        return carFormationMapper.insert(carFormation);
    }




}
