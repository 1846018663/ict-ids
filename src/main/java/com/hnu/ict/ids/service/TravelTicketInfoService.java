package com.hnu.ict.ids.service;

import com.hnu.ict.ids.bean.TicketInfo;
import com.hnu.ict.ids.entity.TravelTicketInfo;

import java.util.List;

public interface TravelTicketInfoService {


    void insertTravelTicketInfoList(List<TravelTicketInfo>  list);


    List<TravelTicketInfo> findPassengerSeating(String traveId);
}
