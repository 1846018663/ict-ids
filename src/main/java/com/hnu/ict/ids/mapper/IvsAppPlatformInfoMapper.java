package com.hnu.ict.ids.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hnu.ict.ids.bean.PlatformInfoFrom;
import com.hnu.ict.ids.entity.IvsAppPlatformInfo;
import com.hnu.ict.ids.entity.OrderInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface IvsAppPlatformInfoMapper extends BaseMapper<IvsAppPlatformInfo> {

    /**
     * 根据站台名称查询
     * @param PlatformName
     * @return
     */
    @Select("select * from ivs_app_platform_info where order_no = #{PlatformName}")
    IvsAppPlatformInfo getByPlatformName(@Param("PlatformName") String PlatformName);


    @Select("select p_id as id ,p_name as name ,longitude as longitude,latitude as latitude from ivs_app_platform_info ")
    List<PlatformInfoFrom> getPlatformAll();


    /**
     * 根据状态查询站台总数
     * @param status
     * @return
     */
    @Select("select COUNT(p_id) from ivs_app_platform_info where p_status = #{status}")
    int findbyStatusTotal(@Param("status") String status);



    /**
     * 根据状态查询站台总数
     * @param
     * @return
     */
    @Select("select COUNT(p_id) from ivs_app_platform_info")
    int findAllTotal();
}
