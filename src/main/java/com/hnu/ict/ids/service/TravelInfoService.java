package com.hnu.ict.ids.service;

import com.hnu.ict.ids.entity.TravelInfo;

import java.math.BigInteger;
import java.util.Date;
import java.util.List;
import java.util.Map;

public interface TravelInfoService {

    TravelInfo getById(BigInteger id);

    TravelInfo findTravelId(String  travelId);

    int finDateTraveTotal(Date startDate, Date endDate);


    TravelInfo getCarTime(int carId,Date timeDate);


    Boolean addTravelInfoList(List<TravelInfo> list, Map<Integer,String> map);

    void updateById(TravelInfo travelInfo);
}
