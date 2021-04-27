package com.hnu.ict.ids.service.impl;

import com.hnu.ict.ids.entity.TdCarSuspendInfo;
import com.hnu.ict.ids.mapper.TdCarSuspendInfoMapper;
import com.hnu.ict.ids.service.TdCarSuspendInfoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class TdCarSuspendInfoServiceImpl implements TdCarSuspendInfoService {
    Logger logger=LoggerFactory.getLogger(TdCarSuspendInfoServiceImpl.class);

    @Autowired
    TdCarSuspendInfoMapper tdCarSuspendInfoMapper;

    public int insert(TdCarSuspendInfo tdCarSuspendInfo){
        return tdCarSuspendInfoMapper.insert(tdCarSuspendInfo);
    }






}
