package com.hnu.ict.ids.service.impl;

import com.hnu.ict.ids.entity.DataCallBackResult;
import com.hnu.ict.ids.entity.TdCarryingLog;
import com.hnu.ict.ids.mapper.DataCallBackResultMapper;
import com.hnu.ict.ids.mapper.TdCarryingLogMapper;
import com.hnu.ict.ids.service.DataCallBackResultService;
import com.hnu.ict.ids.service.TdCarryingLogEndService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class DataCallBackResultServiceImpl implements DataCallBackResultService {
    Logger logger=LoggerFactory.getLogger(DataCallBackResultServiceImpl.class);

    @Autowired
    DataCallBackResultMapper dataCallBackResultMapper;

    public int insert(DataCallBackResult dataCallBackResult){
        return dataCallBackResultMapper.insert(dataCallBackResult);
    }






}
