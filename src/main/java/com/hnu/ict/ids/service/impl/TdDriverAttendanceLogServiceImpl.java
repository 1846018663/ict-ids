package com.hnu.ict.ids.service.impl;

import com.hnu.ict.ids.entity.TdCarryingLog;
import com.hnu.ict.ids.entity.TdDriverAttendanceLog;
import com.hnu.ict.ids.mapper.TdCarryingLogMapper;
import com.hnu.ict.ids.mapper.TdDriverAttendanceLogMapper;
import com.hnu.ict.ids.service.TdCarryingLogEndService;
import com.hnu.ict.ids.service.TdDriverAttendanceLogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class TdDriverAttendanceLogServiceImpl implements TdDriverAttendanceLogService {
    Logger logger=LoggerFactory.getLogger(TdDriverAttendanceLogServiceImpl.class);

    @Autowired
    TdDriverAttendanceLogMapper tdDriverAttendanceLogMapper;

    public int insert(TdDriverAttendanceLog tdDriverAttendanceLog){
        return tdDriverAttendanceLogMapper.insert(tdDriverAttendanceLog);
    }






}
