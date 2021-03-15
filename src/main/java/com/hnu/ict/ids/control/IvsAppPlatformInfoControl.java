package com.hnu.ict.ids.control;

import com.hnu.common.respone.PojoBaseResponse;
import com.hnu.ict.ids.bean.PlatformInfoFrom;
import com.hnu.ict.ids.entity.IvsAppPlatformInfo;
import com.hnu.ict.ids.service.IvsAppPlatformInfoService;
import com.hnu.ict.ids.utils.ParamsNotNull;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
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


    /**
     * 根据城市代码返回全部城市站点信息
     * @param cityCode
     * @return
     */
    @RequestMapping(value="/getPlatformAllInfo" , method = RequestMethod.POST)
    @ParamsNotNull(str ="cityCode")
    public PojoBaseResponse getPlatformAllInfo(String cityCode){
        PojoBaseResponse result=new PojoBaseResponse();
        List<PlatformInfoFrom> list=ivsAppPlatformInfoService.getPlatformAll(cityCode);
        result.setData(list);
        return result;

    }
}
