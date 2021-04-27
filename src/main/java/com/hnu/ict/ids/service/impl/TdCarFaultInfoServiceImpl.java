package com.hnu.ict.ids.service.impl;

import com.hnu.ict.ids.entity.TdCarFaultInfo;
import com.hnu.ict.ids.entity.TdCarSuspendEnd;
import com.hnu.ict.ids.mapper.TdCarFaultInfoMapper;
import com.hnu.ict.ids.mapper.TdCarSuspendEndMapper;
import com.hnu.ict.ids.service.TdCarFaultInfoService;
import com.hnu.ict.ids.service.TdCarSuspendEndService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class TdCarFaultInfoServiceImpl implements TdCarFaultInfoService {
    Logger logger=LoggerFactory.getLogger(TdCarFaultInfoServiceImpl.class);

    @Autowired
    TdCarFaultInfoMapper tdCarFaultInfoMapper;

    public int insert(TdCarFaultInfo tdCarFaultInfo){
        return tdCarFaultInfoMapper.insert(tdCarFaultInfo);
    }






}
