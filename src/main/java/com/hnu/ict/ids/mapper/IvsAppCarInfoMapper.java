package com.hnu.ict.ids.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hnu.ict.ids.entity.IvsAppCarInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface IvsAppCarInfoMapper extends BaseMapper<IvsAppCarInfo> {


    /**
     * 根据车牌号查询
     * @param licenseNumber
     * @return
     */
    @Select("select * from ivs_app_car_info where license_number = #{licenseNumber}")
    IvsAppCarInfo getByLicenseNumber(@Param("licenseNumber") String licenseNumber);

    @Select("select * from ivs_app_car_info")
    List<IvsAppCarInfo> findAll();


    /**
     * 查询所有车辆总数
     * @return
     */
    @Select("select  count(c_id) from ivs_app_car_info")
    int getTotal();


    /**
     * 查询所有离线状态
     * @return
     */
    @Select("select  count(c_id) from ivs_app_car_info where c_status=0" )
    int getOffLine();


    @Select("select  count(c_id) from ivs_app_car_info where c_status=1" )
    int getOnLine();

}
