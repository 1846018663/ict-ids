package com.hnu.ict.ids.service;

import com.hnu.ict.ids.entity.TravelInfo;

import java.math.BigInteger;

public interface TravelInfoService {

    TravelInfo getById(BigInteger id);
}
