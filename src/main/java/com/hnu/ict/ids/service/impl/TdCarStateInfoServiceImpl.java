package com.hnu.ict.ids.service.impl;

import com.hnu.ict.ids.entity.TdCarFaultInfo;
import com.hnu.ict.ids.entity.TdCarStateInfo;
import com.hnu.ict.ids.mapper.TdCarFaultInfoMapper;
import com.hnu.ict.ids.mapper.TdCarStateInfoMapper;
import com.hnu.ict.ids.service.TdCarFaultInfoService;
import com.hnu.ict.ids.service.TdCarStateInfoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class TdCarStateInfoServiceImpl implements TdCarStateInfoService {
    Logger logger=LoggerFactory.getLogger(TdCarStateInfoServiceImpl.class);

    @Autowired
    TdCarStateInfoMapper tdCarStateInfoMapper;

    public int insert(TdCarStateInfo tdCarStateInfo){
        return tdCarStateInfoMapper.insert(tdCarStateInfo);
    }






}
