package com.hnu.ict.ids.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hnu.ict.ids.entity.IvsAppCarInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface IvsAppCarInfoMapper extends BaseMapper<IvsAppCarInfo> {


    /**
     * 根据车牌号查询
     * @param licenseNumber
     * @return
     */
    @Select("select * from ivs_app_car_info where license_number = #{licenseNumber}")
    IvsAppCarInfo getByLicenseNumber(@Param("licenseNumber") String licenseNumber);
}
