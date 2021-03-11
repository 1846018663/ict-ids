package com.hnu.ict.ids.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hnu.ict.ids.entity.TravelInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigInteger;
import java.util.Date;

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

}
