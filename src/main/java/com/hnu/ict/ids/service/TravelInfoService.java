package com.hnu.ict.ids.service;

import com.hnu.ict.ids.entity.TravelInfo;

import java.math.BigInteger;
import java.util.Date;

public interface TravelInfoService {

    TravelInfo getById(BigInteger id);

    int finDateTraveTotal(Date startDate, Date endDate);


    TravelInfo getCarTime(int carId,Date timeDate);


}