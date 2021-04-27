package com.hnu.ict.ids.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hnu.ict.ids.entity.OrderInfo;
import org.apache.ibatis.annotations.*;

import java.math.BigInteger;
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

    /**
     * 根据订单id查询
     * @param id
     * @return
     */
    @Select("select * from order_info where id = #{id}")
    OrderInfo getById(@Param("id") int id);



    @Delete("delete from order_info where source_order_id = #{sourceOrderId}")
    int deleteBySourceOrderId(@Param("sourceOrderId")String  sourceOrderId);


    /**
     * 查询未生成行程订单数据
     */
    List<OrderInfo> findNotTrave(@Param("statDate")String statDate,@Param("endDate")String endDate);


    List<OrderInfo> findNotTransportCapacity(@Param("statDate")String statDate,@Param("endDate")String endDate);


    @Select("SELECT sum(o.ticket_number) from order_info o where o.travel_id=#{travelId}")
    int getByTravelCount(@Param("travelId")BigInteger travelId);


    @Update("update order_info set ticket_number=ticket_number-#{sum}  where id =#{id}")
    void updateOrderNumber(@Param("sum")int sum,@Param("id")BigInteger id);


    /**
     * 根据行程id  查询对应订单
     * @param travelId
     * @return
     */
    List<OrderInfo> findOrderTravelId(@Param("id") String travelId);


    /**
     * 查询所未同步通过数据
     * @return
     */
    @Select("SELECT * from order_info o where o.status=2 and o.travel_id is not null")
    List<OrderInfo> findCompensatesOrderIinfo();

    @Select("SELECT * from order_info o where o.push_status=2 and o.status=2  and o.push_number<=4 ")
    List<OrderInfo> findPushFailedOrderIinfo();

    @Select("SELECT * from order_info o where o.order_no=#{orderNo} ")
    OrderInfo findOrderNo(@Param("orderNo")String orderNo);

}
