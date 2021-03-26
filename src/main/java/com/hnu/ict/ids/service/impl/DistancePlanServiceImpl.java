package com.hnu.ict.ids.service.impl;


import com.hnu.ict.ids.entity.DistancePlan;
import com.hnu.ict.ids.mapper.DistancePlanMapper;
import com.hnu.ict.ids.service.DistancePlanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DistancePlanServiceImpl  implements DistancePlanService {

    @Autowired
    DistancePlanMapper distancePlanMapper;


    public DistancePlan findStartEnd(Integer startId, Integer endId){
        return  distancePlanMapper.findStartEnd(startId,endId);
    }
}
