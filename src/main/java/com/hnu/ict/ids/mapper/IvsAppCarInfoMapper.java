package com.hnu.ict.ids.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hnu.ict.ids.bean.CarTypeTotal;
import com.hnu.ict.ids.entity.IvsAppCarInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

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


    @Select("select car_type as type ,COUNT(c_id) as total,c_status as status,car_seat_number as onIdlerStatusCount from ivs_app_car_info  GROUP by car_type ,c_status")
    List<CarTypeTotal> getCarTypeTotal();

}
