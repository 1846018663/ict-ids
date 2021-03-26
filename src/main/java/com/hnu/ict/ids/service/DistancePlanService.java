package com.hnu.ict.ids.service;

import com.hnu.ict.ids.entity.DistancePlan;

public interface DistancePlanService {

    DistancePlan findStartEnd(Integer startId, Integer endId);

}
