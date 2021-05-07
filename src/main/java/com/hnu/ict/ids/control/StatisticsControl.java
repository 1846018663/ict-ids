package com.hnu.ict.ids.control;


import com.alibaba.fastjson.JSONObject;
import com.hnu.common.respone.PojoBaseResponse;
import com.hnu.ict.ids.bean.ResultBean;
import com.hnu.ict.ids.bean.TraveTrendBean;
import com.hnu.ict.ids.bean.TravelStatisticsBean;
import com.hnu.ict.ids.service.TravelInfoService;
import com.hnu.ict.ids.utils.DateUtil;
import com.hnu.ict.ids.utils.ParamsNotNull;
import io.swagger.annotations.Api;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Api(tags = "综合统计API")
@RestController
@RequestMapping("/report")
public class StatisticsControl {

    Logger logger= LoggerFactory.getLogger(StatisticsControl.class);

    @Autowired
    TravelInfoService travelInfoService;


    /**
     * 出行订单数量统计
     * @return
     */
    @RequestMapping(value = "/travelStatistics",method = RequestMethod.POST)
    @ParamsNotNull(str = "cityCode")
    public PojoBaseResponse getToDayTraveTotal(String cityCode){
        logger.info("出行订单数量统计1天7天1月");
        PojoBaseResponse result=new PojoBaseResponse();
        Date date=new Date();
        String time=DateUtil.getDateByString(date);
        String startTime=time+" 00:00:00";
        String endTime=time+" 23:59:59";
        //当天
        int total = travelInfoService.finDateTraveTotal(DateUtil.strToDate(startTime),DateUtil.strToDate(endTime),cityCode);
        //近7天统计
        String servenTime=DateUtil.getServen(date)+" 23:59:59";
        int servenTotal = travelInfoService.finDateTraveTotal(DateUtil.strToDate(startTime),DateUtil.strToDate(servenTime),cityCode);
        //本月统计
        String starMonth=DateUtil.getStarMonth();
        String endMonth=DateUtil.getEndMonth();
        int monthTotal = travelInfoService.finDateTraveTotal(DateUtil.strToDate(starMonth),DateUtil.strToDate(endMonth),cityCode);
        TravelStatisticsBean tatisticsBean=new TravelStatisticsBean();
        tatisticsBean.setTotal(total);
        tatisticsBean.setMonthTotal(monthTotal);
        tatisticsBean.setServenTotal(servenTotal);
        result.setData(tatisticsBean);

         return result;

    }



    @RequestMapping(value = "/traveTrendToDay" ,method = RequestMethod.POST)
    @ParamsNotNull(str = "cityCode")
    public PojoBaseResponse getTraveTrendToDay(String cityCode){
        logger.info("出行今日趋势");
        PojoBaseResponse result=new PojoBaseResponse();
        Date date=new Date();
        String time=DateUtil.getDateByString(date);
        String dateTime=time+" 00:00:00";
        List<Map<String,Object>> list= travelInfoService.getTraveTrendToDay(cityCode,dateTime);
        result.setData(list);
        return result;

    }


    @RequestMapping(value = "/traveTrendServen" ,method = RequestMethod.POST)
    @ParamsNotNull(str = "cityCode")
    public PojoBaseResponse getTraveTrendServen(String cityCode){
        logger.info("出行7日趋势");
        PojoBaseResponse result=new PojoBaseResponse();
        String time=DateUtil.getServen(new Date());
        List<Map<String,Object>> list= travelInfoService.getTraveTrendServen(cityCode,time);
        result.setData(list);
        return result;

    }


    @RequestMapping(value = "/startingPointRanking" ,method = RequestMethod.POST)
    @ParamsNotNull(str = "cityCode")
    public PojoBaseResponse startingPointRanking(String cityCode){
        logger.info("起点排行");
        PojoBaseResponse result=new PojoBaseResponse();
        List<Map<String,Object>> list= travelInfoService.startinPointRanking(cityCode);
        result.setData(list);
        return result;

    }


    @RequestMapping(value = "/destinationRanking" ,method = RequestMethod.POST)
    @ParamsNotNull(str = "cityCode")
    public PojoBaseResponse destinationRanking(String cityCode){
        logger.info("终点排行");
        PojoBaseResponse result=new PojoBaseResponse();
        List<Map<String,Object>> list= travelInfoService.destinationRanking(cityCode);
        result.setData(list);
        return result;

    }


    @RequestMapping(value = "/combinedTravel" ,method = RequestMethod.POST)
    @ParamsNotNull(str = "cityCode")
    public PojoBaseResponse combinedTravel(String cityCode){
        PojoBaseResponse result=new PojoBaseResponse();
        List<Map<String,Object>> list= travelInfoService.combinedTravel(cityCode);
        result.setData(list);
        return result;

    }



}
