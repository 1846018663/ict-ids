package com.hnu.ict.ids.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hnu.ict.ids.bean.TraveTrendBean;
import com.hnu.ict.ids.entity.TravelInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.math.BigInteger;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Mapper
public interface TravelInfoMapper extends BaseMapper<TravelInfo> {

    /**
     * 根据行程id查询
     * @param id
     * @return
     */
    @Select("select * from travel_info where id=#{id}")
    TravelInfo getById(@Param("id") BigInteger id);




    @Select("select count(id) from travel_info where create_time> #{startDate} and create_time<=#{endDate} and c_code=#{cityCode}")
    int finDateTraveTotal(@Param("startDate") Date startDate,@Param("endDate") Date endDate,@Param("cityCode")String cityCode);


    @Select("select * from travel_info where travel_status=1 and car_id=#{carId} ORDER BY start_time DESC LIMIT 1 ")
    TravelInfo getCarTime(@Param("carId")int carId,@Param("startTime")Date startTime);


    @Select("select * from travel_info where travel_id=#{travelId} ")
    TravelInfo findTravelId(@Param("travelId")String  travelId);


    int addTravelInfo(@Param("persons") List<TravelInfo> persons);//接口


    @Update("update travel_info set travel_status=9,it_number=0 where travel_id =#{travelId}")
    int updateTravlInfoStatus(@Param("travelId")String  travelId);


    @Select("select * from travel_info where push_status=0 and travel_status = 1")
    List<TravelInfo> findeNotPushStatus();



    @Select("SELECT time as dateTime, COUNT( * ) AS total FROM (SELECT id,DATE_FORMAT(concat( date( start_time ), ' ',floor(HOUR ( start_time )/8)*8, ':', MINUTE ( start_time ) )," +
            "            '%Y-%m-%d %H:00:00') AS time FROM travel_info WHERE c_code=#{cityCode} and start_time>=#{dateTime}) a GROUP BY DATE_FORMAT( time, '%Y-%m-%d %H:00:00' )  ORDER BY time;")
    List<Map<String,Object>> getTraveTrendToDay(@Param("cityCode")String cityCode,@Param("dateTime") String dateTime);

    @Select("SELECT DATE(start_time) AS dateTime, COUNT(*) AS total FROM travel_info WHERE c_code=#{cityCode} and start_time>=#{dateTime} GROUP BY dateTime ORDER BY dateTime")
    List<Map<String,Object>> getTraveTrendServen(@Param("cityCode")String cityCode,@Param("dateTime") String dateTime);


    @Select("select begin_station_name as platforName,count(begin_station_name) as total  from travel_info where c_code=#{cityCode} order by begin_station_name desc limit 10")
    List<Map<String,Object>> StartinPointRanking(@Param("cityCode")String cityCode);

    @Select("select end_station_name as platforName,count(end_station_name) as total from travel_info where c_code=#{cityCode}  order by begin_station_name desc limit 10")
    List<Map<String,Object>> destinationRanking(@Param("cityCode")String cityCode);

    @Select("select a.begin_station_name as  platforName,count(a.begin_station_id) as total from travel_info a inner join (select begin_station_id, end_station_id from travel_info where c_code=#{cityCode}) b on a.begin_station_id = b.end_station_id  and a.begin_station_id = b.end_station_id GROUP BY a.begin_station_id order by a.begin_station_id  limit 10")
    List<Map<String,Object>> combinedTravel(@Param("cityCode")String cityCode);


}
