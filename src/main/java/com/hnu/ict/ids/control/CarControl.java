package com.hnu.ict.ids.control;

import com.alibaba.fastjson.JSONObject;
import com.hnu.common.respone.PojoBaseResponse;
import com.hnu.ict.ids.bean.CarTrave;
import com.hnu.ict.ids.bean.CarTypeTotal;
import com.hnu.ict.ids.bean.CarTypeTotalFrom;
import com.hnu.ict.ids.entity.IvsAppCarInfo;
import com.hnu.ict.ids.entity.IvsAppPlatformInfo;
import com.hnu.ict.ids.entity.TravelInfo;
import com.hnu.ict.ids.service.IvsAppCarInfoService;
import com.hnu.ict.ids.service.IvsAppPlatformInfoService;
import com.hnu.ict.ids.service.TravelInfoService;
import com.hnu.ict.ids.utils.ParamsNotNull;
import io.swagger.annotations.Api;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.*;

@Api(tags = "车辆信息API")
@RestController
@RequestMapping("/car")
public class CarControl {

    Logger logger= LoggerFactory.getLogger(CarControl.class);

    @Autowired
    IvsAppCarInfoService ivsAppCarInfoService;

    @Autowired
    IvsAppPlatformInfoService ivsAppPlatformInfoService;

    @Resource
    TravelInfoService travelInfoService;

    /**
     * 通过车牌查询该车行程信息（该车调度信息）
     */
    @RequestMapping(value="/getCarTraveInfo" ,method = RequestMethod.POST)
    @ParamsNotNull(str ="carId")
    public PojoBaseResponse getCarTraveInfo(Integer carId){
        PojoBaseResponse result=new PojoBaseResponse();
         CarTrave carTrave=new CarTrave();
        //根据车牌查询车辆
        IvsAppCarInfo carinfo=ivsAppCarInfoService.getByCarId(carId);
         if(carinfo!=null){
             carTrave.setCarNo(carinfo.getLicenseNumber());
             carTrave.setCarType(carinfo.getCarType());
             carTrave.setCarStatus(carinfo.getCStatus());
             carTrave.setNuclear(carinfo.getCarSeatNumber());


             //根据车辆id查询当前时间
             Date time= new Date();
             TravelInfo travelInfo= travelInfoService.getCarTime(carinfo.getCId(),time);
             IvsAppPlatformInfo startPlatform=ivsAppPlatformInfoService.getByPlatformId(travelInfo.getBeginStationId().toString());
             IvsAppPlatformInfo  endPlatform=ivsAppPlatformInfoService.getByPlatformId(travelInfo.getEndStationId().toString());
             carTrave.setStartStationName(startPlatform.getPName());
             carTrave.setEndStationName(endPlatform.getPName());

             result.setData(carTrave);
         }

        return result;


    }


    /**
     * 车辆信息状态
     * @param licenseNumber   cityCode 城市编号
     * @return
     */
    @RequestMapping(value="/getCarStatus", method = RequestMethod.POST)
    @ParamsNotNull(str ="cityCode")
    public PojoBaseResponse getCarStatus(String licenseNumber,String cityCode){
        PojoBaseResponse result=new PojoBaseResponse();
        if(StringUtils.hasText(licenseNumber)){
            IvsAppCarInfo car=ivsAppCarInfoService.getByLicenseNumber(licenseNumber,cityCode);
            result.setData(car);
            return result;
        }else{
            List<IvsAppCarInfo> catList= ivsAppCarInfoService.findAll(cityCode);
            result.setData(catList);
            return result;
        }
    }


    /**
     * 统计所有车辆信息    传入城市编号
     * @param cityCode
     * @return
     */
    @RequestMapping(value="/getTotal",method = RequestMethod.POST)
    @ParamsNotNull(str ="cityCode")
    public PojoBaseResponse getTotal(String cityCode){
        PojoBaseResponse response=new PojoBaseResponse();
        int total= ivsAppCarInfoService.getTotal(cityCode);
        response.setData(total);
        return response;

    }

    /**
     * 统计离线车辆信息    传入城市编号
     * @param cityCode
     * @return
     */
    @RequestMapping(value="/getOffLine",method = RequestMethod.POST)
    @ParamsNotNull(str ="cityCode")
    public PojoBaseResponse getOffLine(String cityCode){
        PojoBaseResponse result=new PojoBaseResponse();
        int total=  ivsAppCarInfoService.getOffLine(cityCode);
        result.setData(total);

        return result;

    }


    /**
     * 统计在线车辆信息    传入城市编号
     * @param cityCode
     * @return
     */
    @RequestMapping(value="/getOnLine",method = RequestMethod.POST)
    @ParamsNotNull(str ="cityCode")
    public PojoBaseResponse getOnLine(String cityCode){
        PojoBaseResponse result=new PojoBaseResponse();
       /** int carCount= ivsAppCarInfoService.getOnLine(cityCode);
        result.setData(carCount);**/
        return result;

    }


    /**
     * 统计站台总数与车辆总数   传入城市编号
     * @param cityCode
     * @return
     */
    @RequestMapping(value="/getCarCountAndStationCount", method = RequestMethod.POST)
    @ParamsNotNull(str ="cityCode")
    public PojoBaseResponse getCarCountAndStationCount(String status,String cityCode){
        PojoBaseResponse result=new PojoBaseResponse();
        int carCount= ivsAppCarInfoService.getOnLine(cityCode);
        int stationCount=0;
        if(StringUtils.hasText(status)){
            stationCount=ivsAppPlatformInfoService.findbyStatusTotal(status,cityCode);
        }else{
            stationCount=ivsAppPlatformInfoService.findAllTotal(cityCode);
        }
        JSONObject json=new JSONObject();
        json.put("carCount",carCount);
        json.put("stationCount",stationCount);
        result.setData(json);

        return result;

    }


    /**
     * 统计站车辆类型总数   传入城市编号
     * @param cityCode
     * @return
     */
    @RequestMapping(value="/getCarTypeTotal", method = RequestMethod.POST)
    @ParamsNotNull(str ="cityCode")
    public PojoBaseResponse getCarTypeTotal(String cityCode){
        PojoBaseResponse result=new PojoBaseResponse();
        List<CarTypeTotal> list=ivsAppCarInfoService.getCarTypeTotal(cityCode);

        CarTypeTotalFrom from1=new CarTypeTotalFrom();
        CarTypeTotalFrom from2=new CarTypeTotalFrom();
        CarTypeTotalFrom from3=new CarTypeTotalFrom();


        for (Iterator<CarTypeTotal> it = list.iterator(); it.hasNext();) {
            CarTypeTotal car=it.next();

            if(car.getType()==1){
                if( car.getStatus()==1){
                    from1.setOnTotal(car.getTotal());
                    from1.setOffIdlerStatusCount(car.getTotal());
                }else{
                    from1.setOffTotal(car.getTotal());
                }

                from1.setType(car.getType());
            }
            if(car.getType()==2){
                if( car.getStatus()==1){
                    from2.setOnTotal(car.getTotal());
                    from2.setOffIdlerStatusCount(car.getTotal());
                }else{
                    from2.setOffTotal(car.getTotal());
                }

                from2.setType(car.getType());
            }
            if(car.getType()==3){
                if( car.getStatus()==1){
                    from3.setOnTotal(car.getTotal());
                    from3.setOffIdlerStatusCount(car.getTotal());
                }else{
                    from3.setOffTotal(car.getTotal());
                }

                from3.setType(car.getType());

            }
        }

        from1.setOnCarStatusCount(from1.getOnTotal()+from1.getOffTotal());
        from2.setOnCarStatusCount(from2.getOnTotal()+from2.getOffTotal());
        from3.setOnCarStatusCount(from3.getOnTotal()+from3.getOffTotal());

        List<CarTypeTotalFrom> lists=new ArrayList<CarTypeTotalFrom>();
        lists.add(from1);
        lists.add(from2);
        lists.add(from3);
        result.setData(lists);
        return result;

    }



    /**
     * 统计站台总数与车辆总数   传入城市编号
     * @param cityCode
     * @return
     */
    @RequestMapping(value="/getCar", method = RequestMethod.POST)
    public PojoBaseResponse getCar(String status,String cityCode){
        PojoBaseResponse result=new PojoBaseResponse();
        int carCount= ivsAppCarInfoService.getOnLine(cityCode);
        int stationCount=0;
        if(StringUtils.hasText(status)){
            stationCount=ivsAppPlatformInfoService.findbyStatusTotal(status,cityCode);
        }else{
            stationCount=ivsAppPlatformInfoService.findAllTotal(cityCode);
        }
        JSONObject json=new JSONObject();
        json.put("carCount",carCount);
        json.put("stationCount",stationCount);
        result.setData(json);

        return result;

    }



}
