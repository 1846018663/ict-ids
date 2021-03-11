package com.hnu.ict.ids.service.impl;

import com.hnu.ict.ids.bean.PlatformInfoFrom;
import com.hnu.ict.ids.entity.IvsAppPlatformInfo;
import com.hnu.ict.ids.mapper.IvsAppPlatformInfoMapper;
import com.hnu.ict.ids.service.IvsAppPlatformInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class IvsAppPlatformInfoServiceImpl implements IvsAppPlatformInfoService {

    @Autowired
    IvsAppPlatformInfoMapper ivsAppPlatformInfoMapper;

    @Override
    public int findAllTotal(String cityCode){
        return ivsAppPlatformInfoMapper.findAllTotal(cityCode);
    }


    @Override
    public int findbyStatusTotal(String status,String cityCode){
        return ivsAppPlatformInfoMapper.findbyStatusTotal(status,cityCode);
    }

    @Override
    public List<PlatformInfoFrom> getPlatformAll(String cityCode){
        return ivsAppPlatformInfoMapper.getPlatformAll(cityCode);
    }

    @Override
    public IvsAppPlatformInfo getByPlatformId(int platforId){
        return ivsAppPlatformInfoMapper.getByPlatformId(platforId);
    }
}
