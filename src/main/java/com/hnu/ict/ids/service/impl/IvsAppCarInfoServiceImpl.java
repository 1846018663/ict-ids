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
    public IvsAppCarInfo getByLicenseNumber(String licenseNumber){
        return ivsAppCarInfoMapper.getByLicenseNumber(licenseNumber);
    }

    @Override
    public List<IvsAppCarInfo> findAll(){
        return ivsAppCarInfoMapper.findAll();
    }

    @Override
    public int getTotal(){
        return ivsAppCarInfoMapper.getTotal();
    }

    @Override
    public int getOffLine() {
        return ivsAppCarInfoMapper.getOffLine();
    }


    @Override
    public int getOnLine(){
        return ivsAppCarInfoMapper.getOnLine();
    }


    public List<CarTypeTotal> getCarTypeTotal(){
        return ivsAppCarInfoMapper.getCarTypeTotal();
    }
}
