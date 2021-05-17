package com.hnu.ict.ids.service.impl;

import com.hnu.ict.ids.bean.TicketInfo;
import com.hnu.ict.ids.entity.TravelTicketInfo;
import com.hnu.ict.ids.mapper.TravelTicketInfoMapper;
import com.hnu.ict.ids.service.TravelTicketInfoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
public class TravelTicketInfoServiceImpl implements TravelTicketInfoService {
    Logger logger=LoggerFactory.getLogger(TravelTicketInfoServiceImpl.class);


    @Autowired
    TravelTicketInfoMapper travelTicketInfoMapper;


    @Transactional
    public void insertTravelTicketInfoList(List<TravelTicketInfo> list){
        for(TravelTicketInfo travelTicketInfo:list){
            travelTicketInfoMapper.insert(travelTicketInfo);
        }

    }

    public List<TravelTicketInfo> findPassengerSeating(String traveId){
        return  travelTicketInfoMapper.findPassengerSeating(traveId);
    }

    public TravelTicketInfo findTraveIdSeat(String traveId,Integer userId){
        return travelTicketInfoMapper.findTraveIdSeat(traveId,userId);
    }

   public void delTraveIdSeat(String traveId,Integer userId ){
        travelTicketInfoMapper.delTraveIdSeat(traveId,userId);
   }

   public void deldelTraveId(String traveId){
       travelTicketInfoMapper.deldelTraveId(traveId);
   }


}
