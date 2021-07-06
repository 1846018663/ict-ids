package com.hnu.ict.ids.service;

import com.hnu.ict.ids.bean.CarTypeTotal;
import com.hnu.ict.ids.entity.IvsAppCarInfo;

import java.util.Date;
import java.util.List;
import java.util.Map;

public interface IvsAppCarInfoService {

    IvsAppCarInfo getByLicenseNumber(String licenseNumber,String cityCode);

    IvsAppCarInfo getByCarId(int carId);


    List<IvsAppCarInfo> findAll(String cityCode);


    int getTotal(String cityCode);


    int getOffLine(String cityCode);


    int getOnLine(String cityCode);


    List<CarTypeTotal> getCarTypeTotal(String cityCode);

    int insert(IvsAppCarInfo ivsAppCarInfo);

    int update(IvsAppCarInfo ivsAppCarInfo);

    void updateStatus(Date update);
}
