package com.hnu.ict.ids.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hnu.ict.ids.entity.TravelInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigInteger;

@Mapper
public interface TravelInfoMapper extends BaseMapper<TravelInfo> {

    @Select("select * from travel_info where id=#{id}")
    TravelInfo getById(@Param("id") BigInteger id);

}
