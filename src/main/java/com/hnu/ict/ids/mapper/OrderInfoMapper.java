package com.hnu.ict.ids.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hnu.ict.ids.entity.OrderInfo;
import org.apache.ibatis.annotations.*;

import java.util.Date;
import java.util.List;

@Mapper
public interface OrderInfoMapper extends BaseMapper<OrderInfo> {

    /**
     * 根据订单号查询
     * @param sourceOrderId
     * @return
     */
    @Select("select * from order_info where source_order_id = #{sourceOrderId}")
    OrderInfo getBySourceOrderId(@Param("sourceOrderId") String sourceOrderId);


    @Insert("insert into order_info(order_source,source_order_id,order_no,begin_station_id,end_station_id,ticket_number,buy_uid,start_time,travel_id,create_time) values(#{orderSource},#{sourceOrderId},#{orderNo},#{beginStationId},#{endStationId},#{ticketNumber},#{buyUid},#{startTime},#{travelId},#{createTime})")
    int insertOrderInfo(OrderInfo orderInfo);



    @Delete("delete from order_info where source_order_id = #{sourceOrderId}")
    int deleteBySourceOrderId(@Param("sourceOrderId")String  sourceOrderId);


    /**
     * 查询未生成行程订单数据
     */
  //  @Select("select * from order_info where travel_id is null and start_time>#{statDate} and start_time<=#{endDate}")
    List<OrderInfo> findNotTrave(@Param("statDate")String statDate,@Param("endDate")String endDate);

}
