package com.hnu.ict.ids.service.impl;

import com.hnu.ict.ids.entity.IvsAppUserInfo;
import com.hnu.ict.ids.mapper.IvsAppUserInfoMapper;
import com.hnu.ict.ids.service.IvsAppUserInfoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class IvsAppUserInfoServiceImpl implements IvsAppUserInfoService {
    Logger logger=LoggerFactory.getLogger(IvsAppUserInfoServiceImpl.class);


    @Autowired
    IvsAppUserInfoMapper ivsAppUserInfoMapper;


    public int insert(IvsAppUserInfo ivsAppUserInfo){
        return ivsAppUserInfoMapper.insert(ivsAppUserInfo);
    }

    public int updateById(IvsAppUserInfo ivsAppUserInfo){
        return ivsAppUserInfoMapper.updateById(ivsAppUserInfo);
    }

    public IvsAppUserInfo findNumber(String number){
        return ivsAppUserInfoMapper.findNumber(number);
    }



}
