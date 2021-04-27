package com.hnu.ict.ids.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hnu.ict.ids.entity.IvsAppUserInfo;
import com.hnu.ict.ids.entity.OrderInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface IvsAppUserInfoMapper extends BaseMapper<IvsAppUserInfo> {

    /**
     * 根据手机号查询
     * @param phone
     * @return
     */
    @Select("select * from ivs_app_user_info where phone = #{phone}")
    IvsAppUserInfo getByPhone(@Param("phone") String phone);

    @Select("select * from ivs_app_user_info where number = #{number}")
    IvsAppUserInfo findNumber(@Param("number")String number);
}
