package com.hnu.ict.ids.service;

import com.hnu.ict.ids.bean.CarTypeTotal;
import com.hnu.ict.ids.entity.IvsAppCarInfo;

import java.util.List;
import java.util.Map;

public interface IvsAppCarInfoService {

    IvsAppCarInfo getByLicenseNumber(String licenseNumber);


    List<IvsAppCarInfo> findAll();


    int getTotal();


    int getOffLine();


    int getOnLine();


    List<CarTypeTotal> getCarTypeTotal();
}
