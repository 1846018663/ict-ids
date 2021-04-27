package com.hnu.ict.ids.service.impl;

import com.hnu.ict.ids.entity.TdCarOutboundInfo;
import com.hnu.ict.ids.mapper.TdCarOutboundInfoMapper;
import com.hnu.ict.ids.service.TdCarOutboundInfoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class TdCarOutboundInfoServiceImpl implements TdCarOutboundInfoService {
    Logger logger=LoggerFactory.getLogger(TdCarOutboundInfoServiceImpl.class);

    @Autowired
    TdCarOutboundInfoMapper tdCarOutboundInfoMapper;

    public int insert(TdCarOutboundInfo tdCarOutboundInfo){
        return tdCarOutboundInfoMapper.insert(tdCarOutboundInfo);
    }






}
