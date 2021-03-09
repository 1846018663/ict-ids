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
            JSONObject obj = (JSONObject) JSONObject.toJSON(car);
            result.setErrorCode(ResutlMessage.SUCCESS.getName());
            result.setErrorMessage(ResutlMessage.SUCCESS.getValue());
            result.setData(obj);
            return result;
        }else{
           List<IvsAppCarInfo> catList= ivsAppCarInfoService.findAll();
            JSONArray obj = (JSONArray) JSONArray.toJSON(catList);
            result.setErrorCode(ResutlMessage.SUCCESS.getName());
            result.setErrorMessage(ResutlMessage.SUCCESS.getValue());
            result.setData(obj);
            return result;
        }

    }


    @ResponseBody
    @RequestMapping("/getTotal")
    public PojoBaseResponse getTotal(){
        PojoBaseResponse response=new PojoBaseResponse();
        int total= 0;
        try {
            total = ivsAppCarInfoService.getTotal();
            response.setErrorCode(ResutlMessage.SUCCESS.getValue());
            response.setErrorMessage(ResutlMessage.SUCCESS.getValue());
            response.setData(total);
        } catch (Exception e) {
            e.printStackTrace();
            response.setErrorCode(ResutlMessage.ERROR.getName());
            response.setErrorMessage(ResutlMessage.ERROR.getValue());
        }

        return response;

    }


    @ResponseBody
    @RequestMapping("/getOffLine")
    public PojoBaseResponse getOffLine(){
        PojoBaseResponse result=new PojoBaseResponse();
        int total= 0;
        try {
            total = ivsAppCarInfoService.getOffLine();
            result.setErrorCode(ResutlMessage.SUCCESS.getName());
            result.setErrorMessage(ResutlMessage.SUCCESS.getValue());
            result.setData(total);

        } catch (Exception e) {
            e.printStackTrace();
            result.setErrorCode(ResutlMessage.ERROR.getName());
            result.setErrorMessage(ResutlMessage.ERROR.getValue());
        }

        return result;

    }


    @ResponseBody
    @RequestMapping("/getOnLine")
    public BaseResponse getOnLine(){
        ResultEntity resultEntity=new ResultEntity();
        int total= 0;
        try {
            total = ivsAppCarInfoService.getOnLine();
            resultEntity.setCode(ResutlMessage.SUCCESS.getName());
            resultEntity.setMessage(ResutlMessage.SUCCESS.getValue());
            resultEntity.setResult(total);
        } catch (Exception e) {
            e.printStackTrace();
            resultEntity.setCode(ResutlMessage.ERROR.getName());
            resultEntity.setMessage(ResutlMessage.ERROR.getValue());
        }

        return new BaseResponse();

    }




}
