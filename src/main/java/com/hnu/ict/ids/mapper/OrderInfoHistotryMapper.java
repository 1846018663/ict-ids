package com.hnu.ict.ids.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hnu.ict.ids.entity.OrderInfo;
import com.hnu.ict.ids.entity.OrderInfoHistotry;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface OrderInfoHistotryMapper extends BaseMapper<OrderInfoHistotry> {

    /**
     * 根据订单号查询
     * @param sourceOrderId
     * @return
     */
    @Select("select * from order_info_histotry where source_order_id = #{sourceOrderId}")
    OrderInfoHistotry getBySourceOrderId(@Param("sourceOrderId") String sourceOrderId);



    @Delete("delete from order_info_histotry where source_order_id = #{sourceOrderId}")
    int deleteBySourceOrderId(@Param("sourceOrderId")String  sourceOrderId);


}
