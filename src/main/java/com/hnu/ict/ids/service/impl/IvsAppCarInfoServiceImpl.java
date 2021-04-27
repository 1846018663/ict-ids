package com.hnu.ict.ids.service.impl;

import com.hnu.ict.ids.bean.CarTypeTotal;
import com.hnu.ict.ids.entity.IvsAppCarInfo;
import com.hnu.ict.ids.mapper.IvsAppCarInfoMapper;
import com.hnu.ict.ids.service.IvsAppCarInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class IvsAppCarInfoServiceImpl implements IvsAppCarInfoService {

    @Autowired
    IvsAppCarInfoMapper ivsAppCarInfoMapper;

    @Override
    public IvsAppCarInfo getByCarId(int carId){
       return ivsAppCarInfoMapper.getByCarId(carId);
    }

    @Override
    public IvsAppCarInfo getByLicenseNumber(String licenseNumber,String cityCode){
        return ivsAppCarInfoMapper.getByLicenseNumber(licenseNumber,cityCode);
    }

    @Override
    public List<IvsAppCarInfo> findAll(String cityCode){
        return ivsAppCarInfoMapper.findAll(cityCode);
    }

    @Override
    public int getTotal(String cityCode){
        return ivsAppCarInfoMapper.getTotal(cityCode);
    }

    @Override
    public int getOffLine(String cityCode) {
        return ivsAppCarInfoMapper.getOffLine(cityCode);
    }


    @Override
    public int getOnLine(String cityCode){
        return ivsAppCarInfoMapper.getOnLine(cityCode);
    }

    @Override
    public List<CarTypeTotal> getCarTypeTotal(String cityCode){
        return ivsAppCarInfoMapper.getCarTypeTotal(cityCode);
    }
    @Override
    public int insert(IvsAppCarInfo ivsAppCarInfo){
        return ivsAppCarInfoMapper.insert(ivsAppCarInfo);
    }

    @Override
    public int update(IvsAppCarInfo ivsAppCarInfo){
        return ivsAppCarInfoMapper.updateById(ivsAppCarInfo);
    }
}
