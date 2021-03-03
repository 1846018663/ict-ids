package com.hnu.ict.ids.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hnu.ict.ids.entity.OrderInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface OrderInfoMapper extends BaseMapper<OrderInfo> {

    @Select("select * from order_info where mobile = #{orderNo}")
    OrderInfo getSmsByOrder(@Param("orderNo") String orderNo);
}
