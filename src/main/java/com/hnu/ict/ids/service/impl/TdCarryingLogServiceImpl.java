package com.hnu.ict.ids.service.impl;

import com.hnu.ict.ids.entity.TdCarSuspendInfo;
import com.hnu.ict.ids.entity.TdCarryingLog;
import com.hnu.ict.ids.mapper.TdCarSuspendInfoMapper;
import com.hnu.ict.ids.mapper.TdCarryingLogMapper;
import com.hnu.ict.ids.service.TdCarSuspendInfoService;
import com.hnu.ict.ids.service.TdCarryingLogEndService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class TdCarryingLogServiceImpl implements TdCarryingLogEndService {
    Logger logger=LoggerFactory.getLogger(TdCarryingLogServiceImpl.class);

    @Autowired
    TdCarryingLogMapper tdCarryingLogMapper;

    public int insert(TdCarryingLog tdCarryingLog){
        return tdCarryingLogMapper.insert(tdCarryingLog);
    }






}
