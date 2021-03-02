package com.hnu.ict.ids.service;

import com.hnu.ict.ids.entity.DemoEntity;
import org.apache.ibatis.annotations.Param;

public interface DemoService {

    /**
     * @Description: 根据手机号获取短信信息
     * @Params:  * @param null : 
     * @return: 
     * @Author: yuanhx
     * @date: 2021/1/7
    */
    DemoEntity getSmsByMobile(@Param("mobile") String mobile);

}
