package com.hnu.ict.ids.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hnu.ict.ids.bean.TicketInfo;
import com.hnu.ict.ids.entity.TravelTicketInfo;
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
    @Select("SELECT * from travel_ticket_info o where o.travel_id=#{traveId}  ")
    List<TravelTicketInfo> findPassengerSeating(@Param("traveId") String traveId);
}
