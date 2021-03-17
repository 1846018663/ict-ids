package com.hnu.ict.ids.service;

import com.hnu.ict.ids.entity.TravelInfo;

import java.math.BigInteger;
import java.util.Date;
import java.util.List;
import java.util.Map;

public interface TravelInfoService {

    TravelInfo getById(BigInteger id);

    int finDateTraveTotal(Date startDate, Date endDate);


    TravelInfo getCarTime(int carId,Date timeDate);


    void addTravelInfoList(List<TravelInfo> list, Map<String,Integer> map);
}
