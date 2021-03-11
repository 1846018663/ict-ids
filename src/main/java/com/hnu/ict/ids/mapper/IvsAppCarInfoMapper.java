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
    @Select("select * from ivs_app_car_info where license_number = #{licenseNumber} and c_code=#{cityCode}")
    IvsAppCarInfo getByLicenseNumber(@Param("licenseNumber") String licenseNumber,@Param("cityCode")String cityCode);


    /**
     * 根据车牌号查询
     * @param licenseNumber
     * @return
     */
    @Select("select * from ivs_app_car_info where c_id = #{carId} ")
    IvsAppCarInfo getByCarId(@Param("carId")int carId);



    @Select("select * from ivs_app_car_info where c_code=#{cityCode}")
    List<IvsAppCarInfo> findAll(@Param("cityCode") String cityCode);


    /**
     * 查询所有车辆总数
     * @return
     */
    @Select("select  count(c_id) from ivs_app_car_info where c_code=#{cityCode}")
    int getTotal(@Param("cityCode") String cityCode);


    /**
     * 查询所有离线状态
     * @return
     */
    @Select("select  count(c_id) from ivs_app_car_info where c_status=0 and c_code=#{cityCode}" )
    int getOffLine(@Param("cityCode") String cityCode);


    @Select("select  count(c_id) from ivs_app_car_info where c_status=1 and c_code=#{cityCode}" )
    int getOnLine(@Param("cityCode") String cityCode);


    @Select("select car_type as type ,COUNT(c_id) as total,c_status as status,car_seat_number as onIdlerStatusCount from ivs_app_car_info where c_code=#{cityCode}  GROUP by car_type ,c_status")
    List<CarTypeTotal> getCarTypeTotal(@Param("cityCode") String cityCode);

}
