package com.hnu.ict.ids.service.impl;

import com.hnu.ict.ids.entity.TdCarTravelInfo;
import com.hnu.ict.ids.mapper.TdCarTravelInfoMapper;
import com.hnu.ict.ids.service.TdCarTravelInfoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class TdCarTravelInfoServiceImpl implements TdCarTravelInfoService {
    Logger logger=LoggerFactory.getLogger(TdCarTravelInfoServiceImpl.class);

    @Autowired
    TdCarTravelInfoMapper tdCarTravelInfoMapper;

    public int insert(TdCarTravelInfo tdCarTravelInfo){
        return tdCarTravelInfoMapper.insert(tdCarTravelInfo);
    }






}
