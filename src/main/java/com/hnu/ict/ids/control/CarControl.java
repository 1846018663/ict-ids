package com.hnu.ict.ids.control;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hnu.common.respone.BaseResponse;
import com.hnu.common.respone.PojoBaseResponse;
import com.hnu.ict.ids.entity.IvsAppCarInfo;
import com.hnu.ict.ids.exception.ResultEntity;
import com.hnu.ict.ids.exception.ResutlMessage;
import com.hnu.ict.ids.service.IvsAppCarInfoService;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Api(tags = "车辆信息API")
@RestController
@RequestMapping("/car")
public class CarControl {

    @Autowired
    IvsAppCarInfoService ivsAppCarInfoService;


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
    public BaseResponse getOnLine(){
        ResultEntity resultEntity=new ResultEntity();
        int total= ivsAppCarInfoService.getOnLine();
        resultEntity.setResult(total);

        return new BaseResponse();

    }




}
