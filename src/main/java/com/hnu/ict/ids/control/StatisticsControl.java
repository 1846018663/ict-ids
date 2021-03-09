package com.hnu.ict.ids.control;


import com.hnu.common.exception.BaseBusinessException;
import com.hnu.common.respone.PojoBaseResponse;
import com.hnu.ict.ids.exception.ResultEntity;
import com.hnu.ict.ids.exception.ResutlMessage;
import com.hnu.ict.ids.service.TravelInfoService;
import com.hnu.ict.ids.utils.DateUtil;
import com.hnu.ict.ids.utils.ParamsNotNull;
import io.swagger.annotations.Api;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Calendar;
import java.util.Date;

@Api(tags = "综合统计API")
@RestController
@RequestMapping("/report")
public class StatisticsControl {

    Logger logger= LoggerFactory.getLogger(StatisticsControl.class);

    @Autowired
    TravelInfoService travelInfoService;


    @ResponseBody
    @RequestMapping("/getToDayTraveTotal")
    public PojoBaseResponse getToDayTraveTotal(){
        PojoBaseResponse result=new PojoBaseResponse();
        String time=DateUtil.getDateByString(new Date());
        String startTime=time+" 00:00:00";
        String endTime=time+" 23:59:59";

        int total = travelInfoService.finDateTraveTotal(DateUtil.strToDate(startTime),DateUtil.strToDate(endTime));
        result.setData(total);

         return result;


    }



    @ResponseBody
    @RequestMapping("/getWeekTraveTotal")
    @ParamsNotNull(str = "time")
    public PojoBaseResponse getWeekTraveTotal(String time){
        PojoBaseResponse result=new PojoBaseResponse();
        Date date=DateUtil.strToDayDate(time);
        //对时间进行校验格式
        String endTime=DateUtil.getDateByString(date);
        String startTime=DateUtil.getServen(date);

        int total = travelInfoService.finDateTraveTotal(DateUtil.strToDayDate(startTime),DateUtil.strToDayDate(endTime));
        result.setData(total);

        return result;

    }
}
