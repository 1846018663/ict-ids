package com.hnu.ict.ids.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hnu.ict.ids.entity.TravelInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.math.BigInteger;
import java.util.Date;
import java.util.List;

@Mapper
public interface TravelInfoMapper extends BaseMapper<TravelInfo> {

    /**
     * 根据行程id查询
     * @param id
     * @return
     */
    @Select("select * from travel_info where id=#{id}")
    TravelInfo getById(@Param("id") BigInteger id);




    @Select("select count(id) from travel_info where create_time> #{startDate} and create_time<=#{endDate}")
    int finDateTraveTotal(@Param("startDate") Date startDate,@Param("endDate") Date endDate);


    @Select("select * from travel_info where travel_status=1 and car_id=#{carId} ")
    TravelInfo getCarTime(@Param("carId")int carId,@Param("startTime")Date startTime);


    @Select("select * from travel_info where travel_id=#{travelId} ")
    TravelInfo findTravelId(@Param("travelId")String  travelId);


    int addTravelInfo(@Param("persons") List<TravelInfo> persons);//接口


    @Update("update travel_info set travel_status=9  where travel_id =#{travelId}")
    int updateTravlInfoStatus(@Param("travelId")String  travelId);


    @Select("select * from travel_info where push_status=2 and travel_status = 0")
    List<TravelInfo> findeNotPushStatus();
}
