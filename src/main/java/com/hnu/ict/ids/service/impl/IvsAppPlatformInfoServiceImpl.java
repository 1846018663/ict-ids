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
    public int findAllTotal(){
        return ivsAppPlatformInfoMapper.findAllTotal();
    }


    @Override
    public int findbyStatusTotal(String status){
        return ivsAppPlatformInfoMapper.findbyStatusTotal(status);
    }

    @Override
    public List<PlatformInfoFrom> getPlatformAll(){
        return ivsAppPlatformInfoMapper.getPlatformAll();
    }
}
