package com.hnu.ict.ids.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hnu.ict.ids.entity.OrderUserLink;
import org.apache.ibatis.annotations.*;

import java.math.BigInteger;

@Mapper
public interface OrderUserLinkMapper  extends BaseMapper<OrderUserLink> {

    @Insert("insert into order_user_link(user_id,order_id,state) values(#{userId},#{orderId},#{state})")
    int insertOrderUserLink(OrderUserLink orderUserLink);

    @Update("update order_user_link set state=2  where user_id =#{userId} and order_id=#{orderId} and state=1" )
    void updateOrderUserLinkState(@Param("userId")BigInteger userId,@Param("orderId") BigInteger orderId);

    @Select("Select count(id)  from  order_user_link where order_id=#{orderId} and  state=1")
    int findRemove(@Param("orderId") BigInteger orderId);

}
