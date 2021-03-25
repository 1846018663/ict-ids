package com.hnu.ict.ids.service.impl;

import com.hnu.ict.ids.entity.OrderInfo;
import com.hnu.ict.ids.entity.TravelInfo;
import com.hnu.ict.ids.mapper.OrderInfoMapper;
import com.hnu.ict.ids.mapper.TravelInfoMapper;
import com.hnu.ict.ids.service.TravelInfoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class TravelInfoServiceImpl implements TravelInfoService {

    Logger logger= LoggerFactory.getLogger(TravelInfoServiceImpl.class);

    @Autowired
    TravelInfoMapper travelInfoMapper;
    @Autowired
    OrderInfoMapper orderInfoMapper;

    @Override
    public TravelInfo getCarTime(int carId,Date timeDate){
        logger.info("carId"+carId+"time"+timeDate);
        return travelInfoMapper.getCarTime(carId,timeDate);
    }

    public TravelInfo getById(BigInteger id){
        return travelInfoMapper.getById(id);
    }

    @Override
    public TravelInfo findTravelId(String  travelId){
        return travelInfoMapper.findTravelId(travelId);
    }

    @Override
    public int finDateTraveTotal(Date startDate, Date endDate){
        return travelInfoMapper.finDateTraveTotal(startDate,endDate);
    }


    @Transactional
    @Override
    public Boolean addTravelInfoList(List<TravelInfo> list, Map<Integer,String> map){
        try {
            //添加行程数据
            travelInfoMapper.addTravelInfo(list);
            //修改订单与行程对应关系
            for(Map.Entry<Integer,String> entry : map.entrySet()){
                //key为订单id    value为行程
                OrderInfo order= orderInfoMapper.getById(entry.getKey());
                order.setTravelId(entry.getValue());
                order.setTravelSource(0);//默认算法来源  0
                orderInfoMapper.updateById(order);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

    }
}
