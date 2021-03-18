package com.hnu.ict.ids.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hnu.ict.ids.entity.OrderUserLink;
import org.apache.ibatis.annotations.*;

import java.math.BigInteger;

@Mapper
public interface OrderUserLinkMapper  extends BaseMapper<OrderUserLink> {


    @Update("update order_user_link set state=2  where user_id =#{userId} and order_no=#{orderNo} and state=1" )
    void updateOrderUserLinkState(@Param("userId")BigInteger userId,@Param("orderNo") String orderNo);

    @Select("Select count(id)  from  order_user_link where order_no=#{orderNo} and  state=1")
    int findRemove(@Param("orderNo") String orderNo);

}
