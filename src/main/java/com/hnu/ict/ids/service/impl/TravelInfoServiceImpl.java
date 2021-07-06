package com.hnu.ict.ids.service.impl;

import com.hnu.ict.ids.bean.TraveTrendBean;
import com.hnu.ict.ids.entity.OrderInfo;
import com.hnu.ict.ids.entity.OrderInfoHistotry;
import com.hnu.ict.ids.entity.TravelInfo;
import com.hnu.ict.ids.mapper.OrderInfoMapper;
import com.hnu.ict.ids.mapper.TravelInfoMapper;
import com.hnu.ict.ids.service.OrderInfoHistotryService;
import com.hnu.ict.ids.service.TravelInfoService;
import com.hnu.ict.ids.utils.DateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.util.*;

@Service
public class TravelInfoServiceImpl implements TravelInfoService {

    Logger logger= LoggerFactory.getLogger(TravelInfoServiceImpl.class);

    @Autowired
    TravelInfoMapper travelInfoMapper;
    @Autowired
    OrderInfoMapper orderInfoMapper;
    @Autowired
    OrderInfoHistotryService orderInfoHistotryService;

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
    public int finDateTraveTotal(Date startDate, Date endDate,String cityCode){
        return travelInfoMapper.finDateTraveTotal(startDate,endDate,cityCode);
    }


    @Transactional
    @Override
    public Boolean addTravelInfoList(List<TravelInfo> list, Map<String,String> map){
        try {
            //添加行程数据
            travelInfoMapper.addTravelInfo(list);
            //修改订单与行程对应关系
            for(Map.Entry<String,String> entry : map.entrySet()){
                //key为订单id    value为行程
                OrderInfo order= orderInfoMapper.getBySourceOrderId(entry.getKey());
                order.setTravelId(entry.getValue());
                order.setTravelSource(0);//默认算法来源  0
                order.setOrderStatus(6);

                OrderInfoHistotry orderInfoHistotry=new OrderInfoHistotry();
                BeanUtils.copyProperties(order,orderInfoHistotry);
                orderInfoHistotry.setId(null);
                orderInfoHistotry.setCreateTime(new Date());
                orderInfoHistotry.setOrderStatus(6);
                orderInfoHistotry.setOrderStatusName("新增行程成功");

                orderInfoMapper.updateById(order);
                orderInfoHistotryService.insert(orderInfoHistotry);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

    }

    public void updateById(TravelInfo travelInfo){
        travelInfoMapper.updateById(travelInfo);
    }

    @Transactional
    public void updateByIdList(List<TravelInfo> list,Integer status){
        for (int i=0;i<list.size();i++){
            TravelInfo info=list.get(i);
            TravelInfo travelInfo=travelInfoMapper.findTravelId(info.getTravelId());

            List<OrderInfo> orderList=orderInfoMapper.findOrderTravelId(travelInfo.getTravelId());
            for (OrderInfo order: orderList){
                OrderInfoHistotry orderInfoHistotry=new OrderInfoHistotry();
                BeanUtils.copyProperties(order,orderInfoHistotry);
                orderInfoHistotry.setId(null);
                orderInfoHistotry.setCreateTime(new Date());
                if(status==1){//成功
                    orderInfoHistotry.setOrderStatus(13);
                    orderInfoHistotry.setOrderStatusName("乘客服务系统接收成功");

                    order.setOrderStatus(13);
                }else{//失败
                    orderInfoHistotry.setOrderStatus(14);
                    orderInfoHistotry.setOrderStatusName("乘客服务系统接收失败");
                    order.setOrderStatus(14);
                }

                orderInfoMapper.updateById(order);
                orderInfoHistotryService.insert(orderInfoHistotry);
            }
            travelInfo.setPushStatus(status);
            travelInfo.setUpdateTime(new Date());
            travelInfoMapper.updateById(travelInfo);
        }
    }



    public List<TravelInfo> findeNotPushStatus(){
        return travelInfoMapper.findeNotPushStatus();
    }



    public List<Map<String,Object>> getTraveTrendToDay(String cityCode,String dateTime){
        List<Map<String,Object>> listMap=travelInfoMapper.getTraveTrendToDay(cityCode,dateTime);
          return  listMap;
    }


   public List<Map<String,Object>>  getTraveTrendServen(String cityCode,String dateTime){
       List<Map<String,Object>> listMap=travelInfoMapper.getTraveTrendServen(cityCode,dateTime);
       return listMap;
    }

    public List<Map<String,Object>> startinPointRanking(String cityCode){
        return travelInfoMapper.StartinPointRanking(cityCode);
    }

    public List<Map<String,Object>> destinationRanking(String cityCode){
        return travelInfoMapper.destinationRanking(cityCode);
    }
    public List<Map<String,Object>> combinedTravel(String cityCode){
        return travelInfoMapper.combinedTravel(cityCode);
    }


    public int insert(TravelInfo travelInfo){
        return travelInfoMapper.insert(travelInfo);
    }
}
