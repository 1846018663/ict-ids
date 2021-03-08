package com.hnu.ict.ids.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hnu.ict.ids.entity.IvsAppCarInfo;
import com.hnu.ict.ids.entity.IvsAppCityInfo;
import com.hnu.ict.ids.entity.OrderInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface IvsAppCityMapper extends BaseMapper<IvsAppCityInfo> {

    /**
     * 根据城市编号查询
     * @param CitCode
     * @return
     */
    @Select("select * from ivs_app_city_info where c_code = #{CitCode}")
    IvsAppCityInfo getByCitCode(@Param("CitCode") String CitCode);



}
