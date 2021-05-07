package com.hnu.ict.ids.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hnu.ict.ids.bean.PlatformInfoFrom;
import com.hnu.ict.ids.entity.IvsAppPlatformInfo;
import com.hnu.ict.ids.entity.OrderInfo;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface IvsAppPlatformInfoMapper extends BaseMapper<IvsAppPlatformInfo> {


    /**
     * 根据站台id查询
     * @param platforId
     * @return
     */
    @Select("select * from ivs_app_platform_info where p_id = #{platforId}")
    IvsAppPlatformInfo getByPlatformId(@Param("platforId") String platforId);

    /**
     * 根据站台名称查询
     * @param PlatformName
     * @return
     */
    @Select("select * from ivs_app_platform_info where order_no = #{PlatformName}")
    IvsAppPlatformInfo getByPlatformName(@Param("PlatformName") String PlatformName);


    @Select("select p_id as id ,p_name as name ,longitude as longitude,latitude as latitude, p_road_side as roadSide  from ivs_app_platform_info  where c_code=#{code}")
    List<PlatformInfoFrom> getPlatformAll(@Param("code") String code);


    /**宋puls
     * 根据状态查询站台总数
     * @param status
     * @return
     */
    @Select("select COUNT(p_id) from ivs_app_platform_info where p_status = #{status} and c_code=#{code}")
    int findbyStatusTotal(@Param("status") String status,@Param("code") String code);



    /**
     * 根据状态查询站台总数
     * @param
     * @return
     */
    @Select("select COUNT(p_id) from ivs_app_platform_info where c_code=#{code}")
    int findAllTotal(@Param("code") String code);


    @Insert("INSERT INTO ivs_app_platform_info  ( p_id,c_code, p_name, p_type, p_route_type, p_group_type, longitude, latitude,   p_status, p_from_code, p_to_code, p_branch_area, p_map_code, p_road_group, p_road_side, speed_limit, range_radius )  VALUES  ( #{platformInfo.pId},#{platformInfo.cCode}, #{platformInfo.pName},#{platformInfo.pType} ,#{platformInfo.pRouteType}, #{platformInfo.pGroupType}, #{platformInfo.longitude},#{platformInfo.latitude} ,#{platformInfo.pStatus}, #{platformInfo.pFromCode},#{platformInfo.pToCode} , #{platformInfo.pBranchArea},#{platformInfo.pMapCode} , #{platformInfo.pRoadGroup},#{platformInfo.pRoadSide} ,#{platformInfo.speedLimit} , #{platformInfo.rangeRadius} )")
    int insertIvsAppPlatformInfo(@Param("platformInfo") IvsAppPlatformInfo platformInfo);
}
