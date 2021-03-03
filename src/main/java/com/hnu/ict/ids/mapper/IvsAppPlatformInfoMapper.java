package com.hnu.ict.ids.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hnu.ict.ids.entity.IvsAppPlatformInfo;
import com.hnu.ict.ids.entity.OrderInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface IvsAppPlatformInfoMapper extends BaseMapper<IvsAppPlatformInfo> {

    /**
     * 根据站台名称查询
     * @param PlatformName
     * @return
     */
    @Select("select * from ivs_app_platform_info where order_no = #{PlatformName}")
    IvsAppPlatformInfo getByPlatformName(@Param("PlatformName") String PlatformName);
}
