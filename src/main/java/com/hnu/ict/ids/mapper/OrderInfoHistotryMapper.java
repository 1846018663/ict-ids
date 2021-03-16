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
    OrderInfo getBySourceOrderId(@Param("sourceOrderId") String sourceOrderId);


    @Insert("insert into order_info_histotry(order_source,source_order_id,order_no,begin_station_id,end_station_id,ticket_number,buy_uid,start_time,travel_id,create_time,travel_source) values(#{orderSource},#{sourceOrderId},#{orderNo},#{beginStationId},#{endStationId},#{ticketNumber},#{buyUid},#{startTime},#{travelId},#{createTime},#{travelSource})")
    int insertOrderInfoHistotry(OrderInfoHistotry order);



    @Delete("delete from order_info_histotry where source_order_id = #{sourceOrderId}")
    int deleteBySourceOrderId(@Param("sourceOrderId")String  sourceOrderId);


}
