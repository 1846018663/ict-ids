package com.hnu.ict.ids.service.impl;

import com.hnu.ict.ids.entity.NetworkLog;
import com.hnu.ict.ids.mapper.NetworkLogMapper;
import com.hnu.ict.ids.service.NetworkLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class NetworkLogServerImpl implements NetworkLogService {

    @Autowired
    NetworkLogMapper networkLogMapper;

    public void insertNetworkLog(NetworkLog networkLog){
        networkLogMapper.insert(networkLog);
    }
}
