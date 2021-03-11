package com.hnu.ict.ids.control;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hnu.common.respone.BaseResponse;
import com.hnu.common.respone.PojoBaseResponse;
import com.hnu.ict.ids.bean.CarTypeTotal;
import com.hnu.ict.ids.bean.CarTypeTotalFrom;
import com.hnu.ict.ids.entity.IvsAppCarInfo;
import com.hnu.ict.ids.exception.ResultEntity;
import com.hnu.ict.ids.exception.ResutlMessage;
import com.hnu.ict.ids.service.IvsAppCarInfoService;
import com.hnu.ict.ids.service.IvsAppPlatformInfoService;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.schema.Entry;

import java.util.*;

@Api(tags = "车辆信息API")
@RestController
@RequestMapping("/car")
public class CarControl {

    @Autowired
    IvsAppCarInfoService ivsAppCarInfoService;

    @Autowired
    IvsAppPlatformInfoService ivsAppPlatformInfoService;


    @ResponseBody
    @RequestMapping("/getCarStatus")
    public PojoBaseResponse getCarStatus(String licenseNumber){
        PojoBaseResponse result=new PojoBaseResponse();
        if(StringUtils.hasText(licenseNumber)){
            IvsAppCarInfo car=ivsAppCarInfoService.getByLicenseNumber(licenseNumber);
            result.setData(car);
            return result;
        }else{
            List<IvsAppCarInfo> catList= ivsAppCarInfoService.findAll();
            result.setData(catList);
            return result;
        }

    }


    @ResponseBody
    @RequestMapping("/getTotal")
    public PojoBaseResponse getTotal(){
        PojoBaseResponse response=new PojoBaseResponse();
        int total= ivsAppCarInfoService.getTotal();
        response.setData(total);

        return response;

    }


    @ResponseBody
    @RequestMapping("/getOffLine")
    public PojoBaseResponse getOffLine(){
        PojoBaseResponse result=new PojoBaseResponse();
        int total=  ivsAppCarInfoService.getOffLine();
        result.setData(total);

        return result;

    }


    @ResponseBody
    @RequestMapping("/getOnLine")
    public PojoBaseResponse getOnLine(){
        PojoBaseResponse result=new PojoBaseResponse();
        int carCount= ivsAppCarInfoService.getOnLine();
        result.setData(carCount);
        return result;

    }


    @ResponseBody
    @RequestMapping("/getCarCountAndStationCount")
    public PojoBaseResponse getCarCountAndStationCount(String status){
        PojoBaseResponse result=new PojoBaseResponse();
        int carCount= ivsAppCarInfoService.getOnLine();
        int stationCount=0;
        if(StringUtils.hasText(status)){
            stationCount=ivsAppPlatformInfoService.findbyStatusTotal(status);
        }else{
            stationCount=ivsAppPlatformInfoService.findAllTotal();
        }
        JSONObject json=new JSONObject();
        json.put("carCount",carCount);
        json.put("stationCount",stationCount);
        result.setData(json);

        return result;

    }


    @ResponseBody
    @RequestMapping("/getCarTypeTotal")
    public PojoBaseResponse getCarTypeTotal(){
        PojoBaseResponse result=new PojoBaseResponse();
        List<CarTypeTotal> list=ivsAppCarInfoService.getCarTypeTotal();

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



}
