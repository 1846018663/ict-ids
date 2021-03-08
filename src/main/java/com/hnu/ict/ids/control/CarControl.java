package com.hnu.ict.ids.control;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
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
    public ResultEntity getCarStatus(String licenseNumber){
        ResultEntity resultEntity=new ResultEntity();
        if(StringUtils.hasText(licenseNumber)){
            IvsAppCarInfo car=ivsAppCarInfoService.getByLicenseNumber(licenseNumber);
            JSONObject obj = (JSONObject) JSONObject.toJSON(car);
            resultEntity.setCode(ResutlMessage.SUCCESS.getName());
            resultEntity.setMessage(ResutlMessage.SUCCESS.getValue());
            resultEntity.setResult(obj);
            return resultEntity;
        }else{
           List<IvsAppCarInfo> catList= ivsAppCarInfoService.findAll();
            JSONArray obj = (JSONArray) JSONArray.toJSON(catList);
            resultEntity.setCode(ResutlMessage.SUCCESS.getName());
            resultEntity.setMessage(ResutlMessage.SUCCESS.getValue());
            resultEntity.setResult(obj);
            return resultEntity;
        }

    }


    @ResponseBody
    @RequestMapping("/getTotal")
    public ResultEntity getTotal(){
        ResultEntity resultEntity=new ResultEntity();
        int total= 0;
        try {
            total = ivsAppCarInfoService.getTotal();
            resultEntity.setCode(ResutlMessage.SUCCESS.getName());
            resultEntity.setMessage(ResutlMessage.SUCCESS.getValue());
            resultEntity.setResult(total);
        } catch (Exception e) {
            e.printStackTrace();
            resultEntity.setCode(ResutlMessage.ERROR.getName());
            resultEntity.setMessage(ResutlMessage.ERROR.getValue());
        }

        return resultEntity;

    }


    @ResponseBody
    @RequestMapping("/getOffLine")
    public ResultEntity getOffLine(){
        ResultEntity resultEntity=new ResultEntity();
        int total= 0;
        try {
            total = ivsAppCarInfoService.getOffLine();
            resultEntity.setCode(ResutlMessage.SUCCESS.getName());
            resultEntity.setMessage(ResutlMessage.SUCCESS.getValue());
            resultEntity.setResult(total);
        } catch (Exception e) {
            e.printStackTrace();
            resultEntity.setCode(ResutlMessage.ERROR.getName());
            resultEntity.setMessage(ResutlMessage.ERROR.getValue());
        }

        return resultEntity;

    }


    @ResponseBody
    @RequestMapping("/getOnLine")
    public ResultEntity getOnLine(){
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

        return resultEntity;

    }




}
