package com.hnu.ict.ids.control;

import com.hnu.common.respone.PojoBaseResponse;
import com.hnu.ict.ids.bean.PlatformInfoFrom;
import com.hnu.ict.ids.entity.IvsAppPlatformInfo;
import com.hnu.ict.ids.service.IvsAppPlatformInfoService;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Api(tags = "站点数据API")
@RestController
@ResponseBody
@RequestMapping("/platform")
public class IvsAppPlatformInfoControl {


    @Autowired
    IvsAppPlatformInfoService  ivsAppPlatformInfoService;


    @RequestMapping("/getPlatformAllInfo")
    public PojoBaseResponse getPlatformAllInfo(){
        PojoBaseResponse result=new PojoBaseResponse();
        List<PlatformInfoFrom> list=ivsAppPlatformInfoService.getPlatformAll();
        result.setData(list);
        return result;

    }
}
