package com.hnu.ict.ids.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hnu.ict.ids.entity.OrderInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface OrderInfoMapper extends BaseMapper<OrderInfo> {

    /**
     * 根据订单号查询
     * @param orderNo
     * @return
     */
    @Select("select * from order_info where order_no = #{orderNo}")
    OrderInfo getByOrderNo(@Param("orderNo") String orderNo);
}
