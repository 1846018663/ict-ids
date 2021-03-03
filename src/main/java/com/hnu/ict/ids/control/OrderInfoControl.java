package com.hnu.ict.ids.control;


import com.hnu.ict.ids.entity.OrderInfo;
import com.hnu.ict.ids.service.OrderInfoService;
import io.swagger.annotations.Api;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "订单API")
@RestController
@RequestMapping("order")
public class OrderInfoControl {

    Logger logger= LoggerFactory.getLogger(OrderInfoControl.class);

    @Autowired
    OrderInfoService orderInfoService;


    @ResponseBody
    @RequestMapping("/add")
    public String addOrder(){

        return "成功";
    }


    @ResponseBody
    @RequestMapping("/findOrderNo")
    public String findOrderNo(String orderNo){
        logger.info(orderNo);
        if(orderNo==null){
            return "m没数据";
        }
       OrderInfo order= orderInfoService.getByOrderNo(orderNo);
        logger.info(order.toString());
        return "成功";
    }


}
