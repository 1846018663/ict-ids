package com.hnu.ict.ids.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.hnu.ict.ids.exception.DemoError;
import com.hnu.common.exception.BaseBusinessException;
import com.hnu.ict.ids.entity.DemoEntity;
import com.hnu.ict.ids.mapper.DemoMapper;
import com.hnu.ict.ids.service.DemoService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @description: 业务实现
 * @author: yuanhx
 * @create: 2021/01/07
 */

@Service
public class DemoServiceImpl implements DemoService {

    @Autowired
    DemoMapper smsMapper;

    @Override
    public DemoEntity getSmsByMobile(String mobile) {
        if(StringUtils.isEmpty(mobile)){
            throw new BaseBusinessException(DemoError.VALID_CODE_NOT_EXIT);
        }
        return smsMapper.getSmsByMobile(mobile);
    }

}
