package com.hnu.ict.ids.service;

import com.hnu.ict.ids.bean.TraveTrendBean;
import com.hnu.ict.ids.entity.TravelInfo;

import java.math.BigInteger;
import java.util.Date;
import java.util.List;
import java.util.Map;

public interface TravelInfoService {

    TravelInfo getById(BigInteger id);

    TravelInfo findTravelId(String  travelId);

    int finDateTraveTotal(Date startDate, Date endDate,String cityCode);


    TravelInfo getCarTime(int carId,Date timeDate);


    Boolean addTravelInfoList(List<TravelInfo> list, Map<String,String> map);

    void updateById(TravelInfo travelInfo);


    void updateByIdList(List<TravelInfo> list,Integer status);



    List<TravelInfo> findeNotPushStatus();


    List<Map<String,Object>> getTraveTrendToDay(String cityCode,String dateTime);


    List<Map<String,Object>> getTraveTrendServen(String cityCode,String dateTime);


    List<Map<String,Object>> startinPointRanking(String cityCode);

    List<Map<String,Object>> destinationRanking(String cityCode);

    List<Map<String,Object>> combinedTravel(String cityCode);

    int insert(TravelInfo travelInfo);

}
