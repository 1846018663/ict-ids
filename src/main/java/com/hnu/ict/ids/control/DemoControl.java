package com.hnu.ict.ids.control;

import com.hnu.ict.ids.exception.DemoError;
import com.hnu.common.exception.BaseBusinessException;
import com.hnu.common.respone.PojoBaseResponse;
import com.hnu.ict.ids.service.DemoService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @description: sms control
 * @author: yuanhx
 * @create: 2021/01/07
 */
@Api(tags = "短信服务API")
@RestController
@RequestMapping("sms")
public class DemoControl {

    private static final Logger LOGGER = LoggerFactory.getLogger(DemoControl.class);

    @Autowired
    private DemoService smsService;

    @ApiOperation("根据手机号码获取信息")
    @GetMapping("getSmsByMobile")
    public PojoBaseResponse getSmsByMobile(@RequestParam String mobile){
        PojoBaseResponse response = new PojoBaseResponse();
        if(StringUtils.isEmpty(mobile)){
            throw new BaseBusinessException(DemoError.MOBILE_IS_NULL);
        }
        response.setData(smsService.getSmsByMobile(mobile));
        return response;
    }

}
