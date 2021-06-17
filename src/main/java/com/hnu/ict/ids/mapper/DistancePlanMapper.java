package com.hnu.ict.ids.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hnu.ict.ids.entity.DistancePlan;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;


@Mapper
public interface DistancePlanMapper extends BaseMapper<DistancePlan> {


    @Select("SELECT * from distance_plan where start_id =#{startId} and end_id = #{endId} ")
    DistancePlan findStartEnd(@Param("startId") Integer startId,@Param("endId") Integer endId);
}
