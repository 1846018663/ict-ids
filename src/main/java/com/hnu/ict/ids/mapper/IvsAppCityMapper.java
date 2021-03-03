package com.hnu.ict.ids.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hnu.ict.ids.entity.IvsAppCityInfo;
import com.hnu.ict.ids.entity.OrderInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface IvsAppCityMapper extends BaseMapper<IvsAppCityInfo> {

    @Select("select * from order_info where order_no = #{orderNo}")
    OrderInfo getByOrderNo(@Param("orderNo") String orderNo);
}
