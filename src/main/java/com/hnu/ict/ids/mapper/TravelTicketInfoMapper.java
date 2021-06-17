package com.hnu.ict.ids.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hnu.ict.ids.entity.TravelTicketInfo;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface TravelTicketInfoMapper extends BaseMapper<TravelTicketInfo> {


    /**
     * 查询乘客系统需要的数据内容   座位号与uid
     * @param traveId
     * @return
     */
    @Select("SELECT * from travel_ticket_info  where travel_id=#{traveId}  ")
    List<TravelTicketInfo> findPassengerSeating(@Param("traveId") String traveId);

    @Select("SELECT * from travel_ticket_info where travel_id=#{traveId} and user_id=#{userId}")
    TravelTicketInfo findTraveIdSeat(@Param("traveId")String traveId,@Param("userId")Integer userId);


    @Delete("DELETE FROM travel_ticket_info where travel_id=#{traveId} and user_id=#{userId}")
    void delTraveIdSeat(@Param("traveId")String traveId,@Param("userId")Integer userId);


    @Delete("DELETE FROM travel_ticket_info where travel_id=#{traveId} ")
    void deldelTraveId(@Param("traveId")String  traveId);

}
