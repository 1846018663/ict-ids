package com.hnu.ict.ids.service.impl;

import com.hnu.ict.ids.entity.TdCarSuspendEnd;
import com.hnu.ict.ids.mapper.TdCarSuspendEndMapper;
import com.hnu.ict.ids.service.TdCarSuspendEndService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class TdCarSuspendEndServiceImpl implements TdCarSuspendEndService {
    Logger logger=LoggerFactory.getLogger(TdCarSuspendEndServiceImpl.class);

    @Autowired
    TdCarSuspendEndMapper tdCarSuspendEndMapper;

    public int insert(TdCarSuspendEnd tdCarSuspendEnd){
        return tdCarSuspendEndMapper.insert(tdCarSuspendEnd);
    }






}
